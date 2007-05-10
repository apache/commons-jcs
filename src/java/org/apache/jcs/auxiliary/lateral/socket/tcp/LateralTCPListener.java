package org.apache.jcs.auxiliary.lateral.socket.tcp;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * Listens for connections from other TCP lateral caches and handles them. The
 * initialization method starts a listening thread, which creates a socket
 * server. When messages are received they are passed to a pooled executor which
 * then calls the appropriate handle method.
 */
public class LateralTCPListener
    implements ILateralCacheListener, Serializable
{
    private static final long serialVersionUID = -9107062664967131738L;

    private final static Log log = LogFactory.getLog( LateralTCPListener.class );

    /** How long the server will block on an accept(). 0 is infinte. */
    private final static int acceptTimeOut = 0;

    /** The CacheHub this listener is associated with */
    private transient ICompositeCacheManager cacheManager;

    /** Map of available instances, keyed by port */
    protected final static HashMap instances = new HashMap();

    /** The socket listener */
    private ListenerThread receiver;

    private ITCPLateralCacheAttributes tcpLateralCacheAttributes;

    private int port;

    private PooledExecutor pooledExecutor;

    private int putCnt = 0;

    private int removeCnt = 0;

    private int getCnt = 0;

    /**
     * Use the vmid by default. This can be set for testing. If we ever need to
     * run more than one per vm, then we need a new technique.
     */
    private long listenerId = LateralCacheInfo.listenerId;

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class.
     * <p>
     * @param ilca
     *            ITCPLateralCacheAttributes
     * @param cacheMgr
     * @return The instance value
     */
    public synchronized static ILateralCacheListener getInstance( ITCPLateralCacheAttributes ilca,
                                                                 ICompositeCacheManager cacheMgr )
    {
        ILateralCacheListener ins = (ILateralCacheListener) instances.get( String.valueOf( ilca.getTcpListenerPort() ) );

        if ( ins == null )
        {
            ins = new LateralTCPListener( ilca );

            ins.init();

            ins.setCacheManager( cacheMgr );

            instances.put( String.valueOf( ilca.getTcpListenerPort() ), ins );

            if ( log.isDebugEnabled() )
            {
                log.debug( "created new listener " + ilca.getTcpListenerPort() );
            }
        }

        return ins;
    }

    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
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
    public void init()
    {
        try
        {
            this.port = getTcpLateralCacheAttributes().getTcpListenerPort();

            receiver = new ListenerThread();
            receiver.setDaemon( true );
            receiver.start();

            pooledExecutor = new PooledExecutor();
            pooledExecutor.setThreadFactory( new MyThreadFactory() );
        }
        catch ( Exception ex )
        {
            log.error( ex );

            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * Let the lateral cache set a listener_id. Since there is only one
     * listerenr for all the regions and every region gets registered? the id
     * shouldn't be set if it isn't zero. If it is we assume that it is a
     * reconnect.
     * <p>
     * By default, the listener id is the vmid.
     * <p>
     * The service should set this value. This value will never be changed by a
     * server we connect to. It needs to be non static, for unit tests.
     * <p>
     * The service will use the value it sets in all send requests to the
     * sender.
     * <p>
     * @param id
     *            The new listenerId value
     * @throws IOException
     */
    public void setListenerId( long id )
        throws IOException
    {
        this.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }

    /**
     * Gets the listenerId attribute of the LateralCacheTCPListener object
     * <p>
     * @return The listenerId value
     * @throws IOException
     */
    public long getListenerId()
        throws IOException
    {
        return this.listenerId;
    }

    /**
     * Increments the put count. Gets the cache that was injected by the lateral
     * factory. Calls put on the cache.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheListener#handlePut(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void handlePut( ICacheElement element )
        throws IOException
    {
        putCnt++;
        if ( log.isInfoEnabled() )
        {
            if ( getPutCnt() % 100 == 0 )
            {
                log.info( "Put Count (port " + getTcpLateralCacheAttributes().getTcpListenerPort() + ") = "
                    + getPutCnt() );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "handlePut> cacheName=" + element.getCacheName() + ", key=" + element.getKey() );
        }

        getCache( element.getCacheName() ).localUpdate( element );
    }

    /**
     * Increments the remove count. Gets the cache that was injected by the
     * lateral factory. Calls remove on the cache.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemove(java.lang.String,
     *      java.io.Serializable)
     */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        removeCnt++;
        if ( log.isInfoEnabled() )
        {
            if ( getRemoveCnt() % 100 == 0 )
            {
                log.info( "Remove Count = " + getRemoveCnt() );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCache( cacheName ).localRemove( key );
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls removeAll
     * on the cache.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleRemoveAll(java.lang.String)
     */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }

        getCache( cacheName ).localRemoveAll();
    }

    /**
     * Gets the cache that was injected by the lateral factory. Calls get on the
     * cache.
     * <p>
     * @param cacheName
     * @param key
     * @return Serializable
     * @throws IOException
     */
    public Serializable handleGet( String cacheName, Serializable key )
        throws IOException
    {
        getCnt++;
        if ( log.isInfoEnabled() )
        {
            if ( getGetCnt() % 100 == 0 )
            {
                log.info( "Get Count (port " + getTcpLateralCacheAttributes().getTcpListenerPort() + ") = "
                    + getGetCnt() );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "handleGet> cacheName=" + cacheName + ", key = " + key );
        }

        return getCache( cacheName ).localGet( key );
    }

    /**
     * Right now this does nothing.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheListener#handleDispose(java.lang.String)
     */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "handleDispose > cacheName=" + cacheName );
        }

        // TODO handle active deregistration, rather than passive detection
        // through error
        // getCacheManager().freeCache( cacheName, true );
    }

    /**
     * Gets the cacheManager attribute of the LateralCacheTCPListener object.
     * <p>
     * Normally this is set by the factory. If it wasn't set the listener
     * defaults to the expected singleton behavior of the cache amanger.
     * <p>
     * @param name
     * @return CompositeCache
     */
    protected CompositeCache getCache( String name )
    {
        if ( getCacheManager() == null )
        {
            // revert to singleton on failure
            setCacheManager( CompositeCacheManager.getInstance() );

            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr = " + getCacheManager() );
            }
        }

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
     * @param cacheMgr
     *            The cacheMgr to set.
     */
    public void setCacheManager( ICompositeCacheManager cacheMgr )
    {
        this.cacheManager = cacheMgr;
    }

    /**
     * @return Returns the cacheMgr.
     */
    public ICompositeCacheManager getCacheManager()
    {
        return cacheManager;
    }

    /**
     * @param tcpLateralCacheAttributes
     *            The tcpLateralCacheAttributes to set.
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
     * Processes commands from the server socket. There should be one listener
     * for each configured TCP lateral.
     */
    public class ListenerThread
        extends Thread
    {
        /** Main processing method for the ListenerThread object */
        public void run()
        {
            try
            {
                log.info( "Listening on port " + port );

                ServerSocket serverSocket = new ServerSocket( port );
                serverSocket.setSoTimeout( acceptTimeOut );

                ConnectionHandler handler;

                while ( true )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Waiting for clients to connect " );
                    }

                    Socket socket = serverSocket.accept();

                    if ( log.isDebugEnabled() )
                    {
                        InetAddress inetAddress = socket.getInetAddress();

                        log.debug( "Connected to client at " + inetAddress );
                    }

                    handler = new ConnectionHandler( socket );

                    pooledExecutor.execute( handler );
                }
            }
            catch ( Exception e )
            {
                log.error( "Exception caught in TCP listener", e );
            }
        }
    }

    /**
     * A Separate thread taht runs when a command comes into the
     * LateralTCPReceiver.
     */
    public class ConnectionHandler
        implements Runnable
    {
        private Socket socket;

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
        public void run()
        {
            ObjectInputStream ois;

            try
            {
                ois = new ObjectInputStream( socket.getInputStream() );
            }
            catch ( Exception e )
            {
                log.error( "Could not open ObjectInputStream on " + socket, e );

                return;
            }

            LateralElementDescriptor led;

            try
            {
                while ( true )
                {
                    led = (LateralElementDescriptor) ois.readObject();

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
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "receiving LateralElementDescriptor from another" + "led = " + led
                                + ", led.command = " + led.command + ", led.ce = " + led.ce );
                        }

                        handle( led );
                    }
                }
            }
            catch ( java.io.EOFException e )
            {
                log.info( "Caught java.io.EOFException closing connection." );
            }
            catch ( java.net.SocketException e )
            {
                log.info( "Caught java.net.SocketException closing connection." );
            }
            catch ( Exception e )
            {
                log.error( "Unexpected exception.", e );
            }

            try
            {
                ois.close();
            }
            catch ( Exception e )
            {
                log.error( "Could not close object input stream.", e );
            }
        }

        /**
         * This calls the appropriate method, based on the command sent in the
         * Lateral element descriptor.
         * <p>
         * @param led
         * @throws IOException
         */
        private void handle( LateralElementDescriptor led )
            throws IOException
        {
            String cacheName = led.ce.getCacheName();
            Serializable key = led.ce.getKey();

            if ( led.command == LateralElementDescriptor.UPDATE )
            {
                handlePut( led.ce );
            }
            else if ( led.command == LateralElementDescriptor.REMOVE )
            {
                // if a hashcode was given and filtering is on
                // check to see if they are the same
                // if so, then don't remvoe, otherwise issue a remove
                if ( led.valHashCode != -1 )
                {
                    if ( getTcpLateralCacheAttributes().isFilterRemoveByHashCode() )
                    {
                        ICacheElement test = getCache( cacheName ).localGet( key );
                        if ( test != null )
                        {
                            if ( test.getVal().hashCode() == led.valHashCode )
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Filtering detected identical hashCode [" + led.valHashCode
                                        + "], not issuing a remove for led " + led );
                                }
                                return;
                            }
                            else
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Different hashcodes, in cache [" + test.getVal().hashCode()
                                        + "] sent [" + led.valHashCode + "]" );
                                }
                            }
                        }
                    }
                }
                handleRemove( cacheName, key );
            }
            else if ( led.command == LateralElementDescriptor.REMOVEALL )
            {
                handleRemoveAll( cacheName );
            }
            else if ( led.command == LateralElementDescriptor.GET )
            {
                Serializable obj = handleGet( cacheName, key );

                ObjectOutputStream oos = new ObjectOutputStream( socket.getOutputStream() );

                if ( oos != null )
                {
                    oos.writeObject( obj );
                    oos.flush();
                }
            }
        }
    }

    /**
     * Allows us to set the daemon status on the executor threads
     * <p>
     * @author aaronsm
     */
    class MyThreadFactory
        implements ThreadFactory
    {
        /*
         * (non-Javadoc)
         * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }
}
