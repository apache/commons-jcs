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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.io.ObjectInputStreamClassLoaderAware;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.threadpool.DaemonThreadFactory;

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

    /** The socket listener */
    private ListenerThread receiver;

    /** Configuration attributes */
    private ITCPLateralCacheAttributes tcpLateralCacheAttributes;

    /** The processor. We should probably use an event queue here. */
    private ExecutorService pooledExecutor;

    /** put count */
    private int putCnt = 0;

    /** remove count */
    private int removeCnt = 0;

    /** get count */
    private int getCnt = 0;

    /**
     * Use the vmid by default. This can be set for testing. If we ever need to run more than one
     * per vm, then we need a new technique.
     */
    private long listenerId = CacheInfo.listenerId;

    /** is this shut down? */
    private AtomicBoolean shutdown;

    /** is this terminated? */
    private AtomicBoolean terminated;

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class.
     * <p>
     * @param ilca ITCPLateralCacheAttributes
     * @param cacheMgr
     * @return The instance value
     */
    public static <K, V> LateralTCPListener<K, V>
        getInstance( ITCPLateralCacheAttributes ilca, ICompositeCacheManager cacheMgr )
    {
        @SuppressWarnings("unchecked") // Need to cast because of common map for all instances
        LateralTCPListener<K, V> ins = (LateralTCPListener<K, V>) instances.computeIfAbsent(
                String.valueOf( ilca.getTcpListenerPort() ),
                k -> {
                    LateralTCPListener<K, V> newIns = new LateralTCPListener<>( ilca );

                    newIns.init();
                    newIns.setCacheManager( cacheMgr );

                    log.info( "Created new listener {0}",
                            () -> ilca.getTcpListenerPort() );

                    return newIns;
                });

        return ins;
    }

    /**
     * Only need one since it does work for all regions, just reference by multiple region names.
     * <p>
     * @param ilca
     */
    protected LateralTCPListener( ITCPLateralCacheAttributes ilca )
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
            int port = getTcpLateralCacheAttributes().getTcpListenerPort();
            String host = getTcpLateralCacheAttributes().getTcpListenerHost();

            pooledExecutor = Executors.newCachedThreadPool(
                    new DaemonThreadFactory("JCS-LateralTCPListener-"));
            terminated = new AtomicBoolean(false);
            shutdown = new AtomicBoolean(false);

            ServerSocket serverSocket;
            if (host != null && host.length() > 0)
            {
                log.info( "Listening on {0}:{1}", host, port );
                // Resolve host name
                InetAddress inetAddress = InetAddress.getByName(host);
                //Bind the SocketAddress with inetAddress and port
                SocketAddress endPoint = new InetSocketAddress(inetAddress, port);

                serverSocket = new ServerSocket();
                serverSocket.bind(endPoint);
            }
            else
            {
                log.info( "Listening on port {0}", port );
                serverSocket = new ServerSocket( port );
            }
            serverSocket.setSoTimeout( acceptTimeOut );

            receiver = new ListenerThread(serverSocket);
            receiver.setDaemon( true );
            receiver.start();
        }
        catch ( IOException ex )
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
    public void setListenerId( long id )
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
    public void handlePut( ICacheElement<K, V> element )
        throws IOException
    {
        putCnt++;
        if ( log.isInfoEnabled() && getPutCnt() % 100 == 0 )
        {
            log.info( "Put Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    () -> getPutCnt() );
        }

        log.debug( "handlePut> cacheName={0}, key={1}",
                () -> element.getCacheName(), () -> element.getKey() );

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
    public void handleRemove( String cacheName, K key )
        throws IOException
    {
        removeCnt++;
        if ( log.isInfoEnabled() && getRemoveCnt() % 100 == 0 )
        {
            log.info( "Remove Count = {0}", () -> getRemoveCnt() );
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
    public void handleRemoveAll( String cacheName )
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
    public ICacheElement<K, V> handleGet( String cacheName, K key )
        throws IOException
    {
        getCnt++;
        if ( log.isInfoEnabled() && getGetCnt() % 100 == 0 )
        {
            log.info( "Get Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    () -> getGetCnt() );
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
    public Map<K, ICacheElement<K, V>> handleGetMatching( String cacheName, String pattern )
        throws IOException
    {
        getCnt++;
        if ( log.isInfoEnabled() && getGetCnt() % 100 == 0 )
        {
            log.info( "GetMatching Count (port {0}) = {1}",
                    () -> getTcpLateralCacheAttributes().getTcpListenerPort(),
                    () -> getGetCnt() );
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
    public Set<K> handleGetKeySet( String cacheName ) throws IOException
    {
    	return getCache( cacheName ).getKeySet(true);
    }

    /**
     * This marks this instance as terminated.
     * <p>
     * @see org.apache.commons.jcs3.engine.behavior.ICacheListener#handleDispose(java.lang.String)
     */
    @Override
    public void handleDispose( String cacheName )
        throws IOException
    {
        log.info( "handleDispose > cacheName={0} | Ignoring message. "
                + "Do not dispose from remote.", cacheName );

        // TODO handle active deregistration, rather than passive detection
        terminated.set(true);
    }

    @Override
    public synchronized void dispose()
    {
        terminated.set(true);
        notify();

        pooledExecutor.shutdownNow();
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
    protected CompositeCache<K, V> getCache( String name )
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
    public void setCacheManager( ICompositeCacheManager cacheMgr )
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
    public void setTcpLateralCacheAttributes( ITCPLateralCacheAttributes tcpLateralCacheAttributes )
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
     */
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
        public ListenerThread(ServerSocket serverSocket)
        {
            super();
            this.serverSocket = serverSocket;
        }

        /** Main processing method for the ListenerThread object */
        @SuppressWarnings("synthetic-access")
        @Override
        public void run()
        {
            try (ServerSocket ssck = serverSocket)
            {
                ConnectionHandler handler;

                outer: while ( true )
                {
                    log.debug( "Waiting for clients to connect " );

                    Socket socket = null;
                    inner: while (true)
                    {
                        // Check to see if we've been asked to exit, and exit
                        if (terminated.get())
                        {
                            log.debug("Thread terminated, exiting gracefully");
                            break outer;
                        }

                        try
                        {
                            socket = ssck.accept();
                            break inner;
                        }
                        catch (SocketTimeoutException e)
                        {
                            // No problem! We loop back up!
                            continue inner;
                        }
                    }

                    if ( socket != null && log.isDebugEnabled() )
                    {
                        InetAddress inetAddress = socket.getInetAddress();
                        log.debug( "Connected to client at {0}", inetAddress );
                    }

                    handler = new ConnectionHandler( socket );
                    pooledExecutor.execute( handler );
                }
            }
            catch ( IOException e )
            {
                log.error( "Exception caught in TCP listener", e );
            }
        }
    }

    /**
     * A Separate thread that runs when a command comes into the LateralTCPReceiver.
     */
    public class ConnectionHandler
        implements Runnable
    {
        /** The socket connection, passed in via constructor */
        private final Socket socket;

        /**
         * Construct for a given socket
         * @param socket
         */
        public ConnectionHandler( Socket socket )
        {
            this.socket = socket;
        }

        /**
         * Main processing method for the LateralTCPReceiverConnection object
         */
        @Override
        @SuppressWarnings({"unchecked", // Need to cast from Object
            "synthetic-access" })
        public void run()
        {
            try (ObjectInputStream ois =
                    new ObjectInputStreamClassLoaderAware( socket.getInputStream(), null ))
            {
                while ( true )
                {
                    LateralElementDescriptor<K, V> led =
                            (LateralElementDescriptor<K, V>) ois.readObject();

                    if ( led == null )
                    {
                        log.debug( "LateralElementDescriptor is null" );
                        continue;
                    }
                    if ( led.requesterId == getListenerId() )
                    {
                        log.debug( "from self" );
                    }
                    else
                    {
                        log.debug( "receiving LateralElementDescriptor from another led = {0}",
                                led );

                        handle( led );
                    }
                }
            }
            catch ( EOFException e )
            {
                log.info( "Caught EOFException, closing connection.", e );
            }
            catch ( SocketException e )
            {
                log.info( "Caught SocketException, closing connection.", e );
            }
            catch ( Exception e )
            {
                log.error( "Unexpected exception.", e );
            }
        }

        /**
         * This calls the appropriate method, based on the command sent in the Lateral element
         * descriptor.
         * <p>
         * @param led
         * @throws IOException
         */
        @SuppressWarnings("synthetic-access")
        private void handle( LateralElementDescriptor<K, V> led )
            throws IOException
        {
            String cacheName = led.ce.getCacheName();
            K key = led.ce.getKey();
            Serializable obj = null;

            switch (led.command)
            {
                case UPDATE:
                    handlePut( led.ce );
                    break;

                case REMOVE:
                    // if a hashcode was given and filtering is on
                    // check to see if they are the same
                    // if so, then don't remove, otherwise issue a remove
                    if ( led.valHashCode != -1 )
                    {
                        if ( getTcpLateralCacheAttributes().isFilterRemoveByHashCode() )
                        {
                            ICacheElement<K, V> test = getCache( cacheName ).localGet( key );
                            if ( test != null )
                            {
                                if ( test.getVal().hashCode() == led.valHashCode )
                                {
                                    log.debug( "Filtering detected identical hashCode [{0}], "
                                            + "not issuing a remove for led {1}",
                                            led.valHashCode, led );
                                    return;
                                }
                                else
                                {
                                    log.debug( "Different hashcodes, in cache [{0}] sent [{1}]",
                                            test.getVal().hashCode(), led.valHashCode );
                                }
                            }
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
                    obj = (Serializable) handleGetMatching( cacheName, (String) key );
                    break;

                case GET_KEYSET:
                	obj = (Serializable) handleGetKeySet(cacheName);
                    break;

                default: break;
            }

            if (obj != null)
            {
                ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );
                oos.writeObject( obj );
                oos.flush();
            }
        }
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

            receiver.interrupt();
        }
        else
        {
            log.debug( "Shutdown already called." );
        }
    }
}
