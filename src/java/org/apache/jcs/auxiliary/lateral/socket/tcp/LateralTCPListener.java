package org.apache.jcs.auxiliary.lateral.socket.tcp;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.control.CacheHub;

/**
 * Listens for connections from other TCP lateral caches and handles them.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
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
    protected static transient CacheHub cacheMgr;

    /** Map of available instances, keyed by port */
    protected final static HashMap instances = new HashMap();

    // ---------- instance variables

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
    public void setListenerId( byte id )
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
    public byte getListenerId()
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

        CacheHub cm = ( CacheHub ) cacheMgr;
        cm.freeCache( cacheName, true );
    }

    /**
     * Gets the cacheManager attribute of the LateralCacheTCPListener object
     */
    protected ICompositeCache getCache( String name )
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CacheHub.getInstance();

            if ( log.isDebugEnabled() )
            {
                log.debug( "cacheMgr = " + cacheMgr );
            }
        }

        return ( ICompositeCache ) cacheMgr.getCache( name );
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
                ;
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
