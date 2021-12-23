package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * Listens for connections from other TCP lateral caches and handles them. The initialization method
 * starts a listening thread, which creates a socket server. When messages are received they are
 * passed to a pooled executor which then calls the appropriate handle method.
 */
public class LateralTCPListener<K, V>
    implements ILateralCacheListener<K, V>, IShutdownObserver
{
    /** The logger */
    private static final Log log = LogManager.getLog( LateralTCPListener.class );

    /** How long the server will block on an accept(). 0 is infinite. */
    private static final int acceptTimeOut = 1000;

    /** The CacheHub this listener is associated with */
    private transient ICompositeCacheManager cacheManager;

    /** Map of available instances, keyed by port */
    private static final ConcurrentHashMap<String, ILateralCacheListener<?, ?>> instances =
        new ConcurrentHashMap<>();

    /** Configuration attributes */
    private ITCPLateralCacheAttributes tcpLateralCacheAttributes;

    /** The listener thread */
    private Thread listenerThread;

    /**
     * Serializer for reading and writing
     */
    private IElementSerializer serializer;

    /** put count */
    private int putCnt;

    /** remove count */
    private int removeCnt;

    /** get count */
    private int getCnt;

    /**
     * Use the vmid by default. This can be set for testing. If we ever need to run more than one
     * per vm, then we need a new technique.
     */
    private long listenerId = CacheInfo.listenerId;

    /** is this shut down? */
    private final AtomicBoolean shutdown = new AtomicBoolean();

    /** is this terminated? */
    private final AtomicBoolean terminated = new AtomicBoolean();

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class.
     * <p>
     * @param ilca ITCPLateralCacheAttributes
     * @param cacheMgr
     * @return The instance value
     */
    @SuppressWarnings("unchecked") // Need to cast because of common map for all instances
    public static <K, V> LateralTCPListener<K, V>
        getInstance( final ITCPLateralCacheAttributes ilca, final ICompositeCacheManager cacheMgr )
    {
        return (LateralTCPListener<K, V>) instances.computeIfAbsent(
                String.valueOf( ilca.getTcpListenerPort() ),
                k -> {
                    final LateralTCPListener<K, V> newIns = new LateralTCPListener<>( ilca );

                    newIns.init();
                    newIns.setCacheManager( cacheMgr );

                    log.info("Created new listener {0}", ilca::getTcpListenerPort);

                    return newIns;
                });
    }

    /**
     * Only need one since it does work for all regions, just reference by multiple region names.
     * <p>
     * @param ilca
     */
    protected LateralTCPListener( final ITCPLateralCacheAttributes ilca )
    {
        this.setTcpLateralCacheAttributes( ilca );
    }

    /**
     * This starts the ListenerThread on the specified port.
     */
    @Override
    public synchronized void init()
    {
        try
        {
            final int port = getTcpLateralCacheAttributes().getTcpListenerPort();
            final String host = getTcpLateralCacheAttributes().getTcpListenerHost();

            terminated.set(false);
            shutdown.set(false);

            serializer = new StandardSerializer();

            final ServerSocketChannel serverSocket = ServerSocketChannel.open();

            SocketAddress endPoint;

            if (host != null && !host.isEmpty())
            {
                log.info( "Listening on {0}:{1}", host, port );
                //Bind the SocketAddress with host and port
                endPoint = new InetSocketAddress(host, port);
            }
            else
            {
                log.info( "Listening on port {0}", port );
                endPoint = new InetSocketAddress(port);
            }

            serverSocket.bind(endPoint);
            serverSocket.configureBlocking(false);

            listenerThread = new Thread(() -> runListener(serverSocket),
                    "JCS-LateralTCPListener-" + host + ":" + port);
            listenerThread.setDaemon(true);
            listenerThread.start();
        }
        catch ( final IOException ex )
        {
            throw new IllegalStateException( ex );
        }
    }

    /**
     * Let the lateral cache set a listener_id. Since there is only one listener for all the
     * regions and every region gets registered? the id shouldn't be set if it isn't zero. If it is
     * we assume that it is a reconnect.
     * <p>
     * By default, the listener id is the vmid.
     * <p>
     * The service should set this value. This value will never be changed by a server we connect
     * to. It needs to be non static, for unit tests.
     * <p>
     * The service will use the value it sets in all send requests to the sender.
     * <p>
     * @param id The new listenerId value
     * @throws IOException
     */
    @Override
    public void setListenerId( final long id )
        throws IOException
    {
        this.listenerId = id;
        log.debug( "set listenerId = {0}", id );
    }

    /**
     * Gets the listenerId attribute of the LateralCacheTCPListener object
     * <p>
     * @return The listenerId value
     * @throws IOException
     */
    @Override
    public long getListenerId()
        throws IOException
    {
        return this.listenerId;
    }

    /**
     * Increments the put count. Gets the cache that was injected by the lateral factory. Calls put
     * on the cache.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheListener#handlePut(org.apache.commons.jcs3.engine.behavior.ICacheElement)
     */
    @Override
    public void handlePut( final ICacheElement<K, V> element )
        throws IOException
    {
        putCnt++;
        if ( log.isInfoEnabled() && getPutCnt() % 100 == 0 )
        {
            log.info( "Put Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    this::getPutCnt);
        }

        log.debug( "handlePut> cacheName={0}, key={1}",
                element::getCacheName, element::getKey);

        getCache( element.getCacheName() ).localUpdate( element );
    }

    /**
     * Increments the remove count. Gets the cache that was injected by the lateral factory. Calls
     * remove on the cache.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheListener#handleRemove(java.lang.String,
     *      Object)
     */
    @Override
    public void handleRemove( final String cacheName, final K key )
        throws IOException
    {
        removeCnt++;
        if ( log.isInfoEnabled() && getRemoveCnt() % 100 == 0 )
        {
            log.info( "Remove Count = {0}", this::getRemoveCnt);
        }

        log.debug( "handleRemove> cacheName={0}, key={1}", cacheName, key );

        getCache( cacheName ).localRemove( key );
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls removeAll on the cache.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheListener#handleRemoveAll(java.lang.String)
     */
    @Override
    public void handleRemoveAll( final String cacheName )
        throws IOException
    {
        log.debug( "handleRemoveAll> cacheName={0}", cacheName );

        getCache( cacheName ).localRemoveAll();
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls get on the cache.
     * <p>
     * @param cacheName
     * @param key
     * @return a ICacheElement
     * @throws IOException
     */
    public ICacheElement<K, V> handleGet( final String cacheName, final K key )
        throws IOException
    {
        getCnt++;
        if ( log.isInfoEnabled() && getGetCnt() % 100 == 0 )
        {
            log.info( "Get Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    this::getGetCnt);
        }

        log.debug( "handleGet> cacheName={0}, key={1}", cacheName, key );

        return getCache( cacheName ).localGet( key );
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls get on the cache.
     * <p>
     * @param cacheName the name of the cache
     * @param pattern the matching pattern
     * @return Map
     * @throws IOException
     */
    public Map<K, ICacheElement<K, V>> handleGetMatching( final String cacheName, final String pattern )
        throws IOException
    {
        getCnt++;
        if ( log.isInfoEnabled() && getGetCnt() % 100 == 0 )
        {
            log.info( "GetMatching Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    this::getGetCnt);
        }

        log.debug( "handleGetMatching> cacheName={0}, pattern={1}", cacheName, pattern );

        return getCache( cacheName ).localGetMatching( pattern );
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls getKeySet on the cache.
     * <p>
     * @param cacheName the name of the cache
     * @return a set of keys
     * @throws IOException
     */
    public Set<K> handleGetKeySet( final String cacheName ) throws IOException
    {
    	return getCache( cacheName ).getKeySet(true);
    }

    /**
     * This marks this instance as terminated.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheListener#handleDispose(java.lang.String)
     */
    @Override
    public void handleDispose( final String cacheName )
        throws IOException
    {
        log.info( "handleDispose > cacheName={0} | Ignoring message. "
                + "Do not dispose from remote.", cacheName );

        // TODO handle active deregistration, rather than passive detection
        dispose();
    }

    @Override
    public synchronized void dispose()
    {
        if (terminated.compareAndSet(false, true))
        {
            notify();
            listenerThread.interrupt();
        }
    }

    /**
     * Gets the cacheManager attribute of the LateralCacheTCPListener object.
     * <p>
     * Normally this is set by the factory. If it wasn't set the listener defaults to the expected
     * singleton behavior of the cache manager.
     * <p>
     * @param name
     * @return CompositeCache
     */
    protected CompositeCache<K, V> getCache( final String name )
    {
        return getCacheManager().getCache( name );
    }

    /**
     * This is roughly the number of updates the lateral has received.
     * <p>
     * @return Returns the putCnt.
     */
    public int getPutCnt()
    {
        return putCnt;
    }

    /**
     * @return Returns the getCnt.
     */
    public int getGetCnt()
    {
        return getCnt;
    }

    /**
     * @return Returns the removeCnt.
     */
    public int getRemoveCnt()
    {
        return removeCnt;
    }

    /**
     * @param cacheMgr The cacheMgr to set.
     */
    @Override
    public void setCacheManager( final ICompositeCacheManager cacheMgr )
    {
        this.cacheManager = cacheMgr;
    }

    /**
     * @return Returns the cacheMgr.
     */
    @Override
    public ICompositeCacheManager getCacheManager()
    {
        return cacheManager;
    }

    /**
     * @param tcpLateralCacheAttributes The tcpLateralCacheAttributes to set.
     */
    public void setTcpLateralCacheAttributes( final ITCPLateralCacheAttributes tcpLateralCacheAttributes )
    {
        this.tcpLateralCacheAttributes = tcpLateralCacheAttributes;
    }

    /**
     * @return Returns the tcpLateralCacheAttributes.
     */
    public ITCPLateralCacheAttributes getTcpLateralCacheAttributes()
    {
        return tcpLateralCacheAttributes;
    }

    /**
     * Processes commands from the server socket. There should be one listener for each configured
     * TCP lateral.
     * @deprecated No longer used
     */
    @Deprecated
    public class ListenerThread
        extends Thread
    {
        /** The socket listener */
        private final ServerSocket serverSocket;

        /**
         * Constructor
         *
         * @param serverSocket
         */
        public ListenerThread(final ServerSocket serverSocket)
        {
            this.serverSocket = serverSocket;
        }

        /** Main processing method for the ListenerThread object */
        @Override
        public void run()
        {
            runListener(serverSocket.getChannel());
        }
    }

    /**
     * Processes commands from the server socket. There should be one listener for each configured
     * TCP lateral.
     */
    private void runListener(final ServerSocketChannel serverSocket)
    {
        try (Selector selector = Selector.open())
        {
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            log.debug("Waiting for clients to connect");

            // Check to see if we've been asked to exit, and exit
            while (!terminated.get())
            {
                int activeKeys = selector.select(acceptTimeOut);
                if (activeKeys == 0)
                {
                    continue;
                }

                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();)
                {
                    if (terminated.get())
                    {
                        break;
                    }

                    SelectionKey key = i.next();

                    if (!key.isValid())
                    {
                        continue;
                    }

                    if (key.isAcceptable())
                    {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        if (client == null)
                        {
                            //may happen in non-blocking mode
                            continue;
                        }

                        log.info("Connected to client at {0}", client.getRemoteAddress());

                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }

                    if (key.isReadable())
                    {
                        handleClient(key);
                    }

                    i.remove();
                }
            }

            log.debug("Thread terminated, exiting gracefully");

            //close all registered channels
            selector.keys().forEach(key -> {
                try
                {
                    key.channel().close();
                }
                catch (IOException e)
                {
                    log.warn("Problem closing channel", e);
                }
            });
        }
        catch (final IOException e)
        {
            log.error( "Exception caught in TCP listener", e );
        }
        finally
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                log.error( "Exception closing TCP listener", e );
            }
        }
    }

    /**
     * A Separate thread that runs when a command comes into the LateralTCPReceiver.
     * @deprecated No longer used
     */
    @Deprecated
    public class ConnectionHandler
        implements Runnable
    {
        /** The socket connection, passed in via constructor */
        private final Socket socket;

        /**
         * Construct for a given socket
         * @param socket
         */
        public ConnectionHandler( final Socket socket )
        {
            this.socket = socket;
        }

        /**
         * Main processing method for the LateralTCPReceiverConnection object
         */
        @Override
        public void run()
        {
            try (InputStream is = socket.getInputStream())
            {
                while ( true )
                {
                    final LateralElementDescriptor<K, V> led =
                            serializer.deSerializeFrom(is, null);

                    if ( led == null )
                    {
                        log.debug( "LateralElementDescriptor is null" );
                        continue;
                    }
                    if ( led.getRequesterId() == getListenerId() )
                    {
                        log.debug( "from self" );
                    }
                    else
                    {
                        log.debug( "receiving LateralElementDescriptor from another led = {0}",
                                led );

                        Object obj = handleElement(led);
                        if (obj != null)
                        {
                            OutputStream os = socket.getOutputStream();
                            serializer.serializeTo(obj, os);
                            os.flush();
                        }
                    }
                }
            }
            catch (final IOException e)
            {
                log.info("Caught {0}, closing connection.", e.getClass().getSimpleName(), e);
            }
            catch (final ClassNotFoundException e)
            {
                log.error( "Deserialization failed reading from socket", e );
            }
        }
    }

    /**
     * A Separate thread that runs when a command comes into the LateralTCPReceiver.
     */
    private void handleClient(final SelectionKey key)
    {
        final SocketChannel socketChannel = (SocketChannel) key.channel();

        try
        {
            final LateralElementDescriptor<K, V> led =
                    serializer.deSerializeFrom(socketChannel, null);

            if ( led == null )
            {
                log.debug("LateralElementDescriptor is null");
                return;
            }

            if ( led.getRequesterId() == getListenerId() )
            {
                log.debug( "from self" );
            }
            else
            {
                log.debug( "receiving LateralElementDescriptor from another led = {0}",
                        led );

                Object obj = handleElement(led);
                if (obj != null)
                {
                    serializer.serializeTo(obj, socketChannel);
                }
            }
        }
        catch (final IOException e)
        {
            log.info("Caught {0}, closing connection.", e.getClass().getSimpleName(), e);
            try
            {
                socketChannel.close();
            }
            catch (IOException e1)
            {
                log.error("Error while closing connection", e );
            }
        }
        catch (final ClassNotFoundException e)
        {
            log.error( "Deserialization failed reading from socket", e );
        }
    }

    /**
     * This calls the appropriate method, based on the command sent in the Lateral element
     * descriptor.
     * <p>
     * @param led the lateral element
     * @return a possible response
     * @throws IOException
     */
    private Object handleElement(final LateralElementDescriptor<K, V> led) throws IOException
    {
        final String cacheName = led.getPayload().getCacheName();
        final K key = led.getPayload().getKey();
        Object obj = null;

        switch (led.getCommand())
        {
            case UPDATE:
                handlePut(led.getPayload());
                break;

            case REMOVE:
                // if a hashcode was given and filtering is on
                // check to see if they are the same
                // if so, then don't remove, otherwise issue a remove
                if (led.getValHashCode() != -1 &&
                    getTcpLateralCacheAttributes().isFilterRemoveByHashCode())
                {
                    final ICacheElement<K, V> test = getCache( cacheName ).localGet( key );
                    if ( test != null )
                    {
                        if ( test.getVal().hashCode() == led.getValHashCode() )
                        {
                            log.debug( "Filtering detected identical hashCode [{0}], "
                                    + "not issuing a remove for led {1}",
                                    led.getValHashCode(), led );
                            return null;
                        }
                        log.debug( "Different hashcodes, in cache [{0}] sent [{1}]",
                                test.getVal()::hashCode, led::getValHashCode );
                    }
                }
                handleRemove( cacheName, key );
                break;

            case REMOVEALL:
                handleRemoveAll( cacheName );
                break;

            case GET:
                obj = handleGet( cacheName, key );
                break;

            case GET_MATCHING:
                obj = handleGetMatching( cacheName, (String) key );
                break;

            case GET_KEYSET:
                obj = handleGetKeySet(cacheName);
                break;

            default: break;
        }

        return obj;
    }

    /**
     * Shuts down the receiver.
     */
    @Override
    public void shutdown()
    {
        if ( shutdown.compareAndSet(false, true) )
        {
            log.info( "Shutting down TCP Lateral receiver." );
            dispose();
        }
        else
        {
            log.debug( "Shutdown already called." );
        }
    }
}
