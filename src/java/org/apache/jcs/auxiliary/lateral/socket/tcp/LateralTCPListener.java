package org.apache.jcs.auxiliary.lateral.socket.tcp;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Listens for connections from other TCP lateral caches and handles them.
 *
 * @version $Id$
 */
public class LateralTCPListener
    implements ILateralCacheListener, Serializable
{
    private final static Log log =
        LogFactory.getLog( LateralTCPListener.class );

    /** How long the server will block on an accept(). 0 is infinte. */
    private final static int acceptTimeOut = 0;

    /** The CacheHub this listener is associated with */
    protected static transient CompositeCacheManager cacheMgr;

    /** Map of available instances, keyed by port */
    protected final static HashMap instances = new HashMap();

    // ----------------------------------------------------- instance variables

    /** The socket listener */
    private ListenerThread receiver;

    private ILateralCacheAttributes ilca;
    private int port;

    private PooledExecutor pooledExecutor = new PooledExecutor();

    // -------------------------------------------------------- factory methods

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class
     *
     * @return The instance value
     */
    public synchronized static ILateralCacheListener
        getInstance( ILateralCacheAttributes ilca )
    {
        ILateralCacheListener ins = ( ILateralCacheListener )
            instances.get( String.valueOf( ilca.getTcpListenerPort() ) );

        if ( ins == null )
        {
            ins = new LateralTCPListener( ilca );

            ins.init();

            instances.put( String.valueOf( ilca.getTcpListenerPort() ), ins );

            if ( log.isDebugEnabled() )
            {
                log.debug( "created new listener " + ilca.getTcpListenerPort() );
            }
        }

        return ins;
    }

    // ------------------------------------------------------- instance methods

    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param ilca
     */
    protected LateralTCPListener( ILateralCacheAttributes ilca )
    {
        this.ilca = ilca;
    }

    /** Description of the Method */
    public void init()
    {
        try
        {
            this.port = ilca.getTcpListenerPort();

            receiver = new ListenerThread();

            receiver.start();
        }
        catch ( Exception ex )
        {
            log.error( ex );

            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * let the lateral cache set a listener_id. Since there is only one
     * listerenr for all the regions and every region gets registered? the id
     * shouldn't be set if it isn't zero. If it is we assume that it is a
     * reconnect.
     *
     * @param id The new listenerId value
     */
    public void setListenerId( long id )
        throws IOException
    {
        LateralCacheInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }

    /**
     * Gets the listenerId attribute of the LateralCacheTCPListener object
     *
     * @return The listenerId value
     */
    public long getListenerId()
        throws IOException
    {
        return LateralCacheInfo.listenerId;
    }

    // ---------------------------------------- interface ILateralCacheListener

    public void handlePut( ICacheElement element )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handlePut> cacheName=" + element.getCacheName() + ", key=" + element.getKey() );
        }

        // This was the following, however passing true in for updateRemotes
        // causes an a loop, since the element will the be sent to the sender.
        // Passing false in fixes things, but I'm not sure I understand all
        // the details yet.
        //
        // getCache( element.getCacheName() )
        //    .update( element, CacheConstants.REMOTE_INVOKATION );

        getCache( element.getCacheName() ).localUpdate( element );
    }

    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCache( cacheName ).localRemove( key );
    }

    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }

        getCache( cacheName ).localRemoveAll();
    }

    public Serializable handleGet( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleGet> cacheName=" + cacheName + ", key = " + key );
        }

        return getCache( cacheName ).localGet( key );
    }

    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }

        CompositeCacheManager cm = ( CompositeCacheManager ) cacheMgr;
        cm.freeCache( cacheName, true );
    }

    /**
     * Gets the cacheManager attribute of the LateralCacheTCPListener object
     */
    protected CompositeCache getCache( String name )
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();

            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr = " + cacheMgr );
            }
        }

        return cacheMgr.getCache( name );
    }

    // ---------------------------------------------------------- inner classes

    /**
     * Processes commands from the server socket. There should be one listener
     * for each configured TCP lateral.
     */
    public class ListenerThread extends Thread
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
     * Separate thread run when a command comes into the LateralTCPReceiver.
     */
    public class ConnectionHandler implements Runnable
    {
        private Socket socket;

        /** Construct for a given socket */
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
                log.error( "Could not open ObjectInputStream to " + socket, e );

                return;
            }

            LateralElementDescriptor led;

            try
            {
                while ( true )
                {
                    led = ( LateralElementDescriptor ) ois.readObject();

                    if ( led == null )
                    {
                        log.debug( "LateralElementDescriptor is null" );
                        continue;
                    }
                    if ( led.requesterId == LateralCacheInfo.listenerId )
                    {
                        log.debug( "from self" );
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "receiving LateralElementDescriptor from another"
                                       + "led = " + led
                                       + ", led.command = " + led.command
                                       + ", led.ce = " + led.ce );
                        }

                        handle( led );
                    }
                }
            }
            catch ( java.io.EOFException e )
            {
                log.info( "Caught java.io.EOFException closing conneciton." );
            }
            catch ( java.net.SocketException e )
            {
                log.info( "Caught java.net.SocketException closing conneciton." );
            }
            catch ( Exception e )
            {
                log.error( "Unexpected exception. Closing conneciton", e );
            }

            try
            {
                ois.close();
            }
            catch ( Exception e )
            {
                log.error( "Could not close connection", e );
            }
        }

        private void handle( LateralElementDescriptor led ) throws IOException
        {
            String cacheName = led.ce.getCacheName();
            Serializable key = led.ce.getKey();

            if ( led.command == LateralElementDescriptor.UPDATE )
            {
                handlePut( led.ce );
            }
            else if ( led.command == LateralElementDescriptor.REMOVE )
            {
                handleRemove( cacheName, key );
            }
            else if ( led.command == LateralElementDescriptor.REMOVEALL )
            {
                handleRemoveAll( cacheName );
            }
            else if ( led.command == LateralElementDescriptor.GET )
            {
                Serializable obj = handleGet( cacheName, key );

                ObjectOutputStream oos =
                    new ObjectOutputStream( socket.getOutputStream() );

                if ( oos != null )
                {
                    oos.writeObject( obj );
                    oos.flush();
                }
            }
        }
    }
}
