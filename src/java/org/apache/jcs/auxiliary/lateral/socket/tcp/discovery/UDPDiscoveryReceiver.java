package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPCacheManager;
import org.apache.jcs.auxiliary.lateral.socket.tcp.TCPLateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IShutdownObserver;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * Receives UDP Discovery messages.
 */
public class UDPDiscoveryReceiver
    implements Runnable, IShutdownObserver
{
    private final static Log log = LogFactory.getLog( UDPDiscoveryReceiver.class );

    private final byte[] m_buffer = new byte[65536];

    private MulticastSocket m_socket;

    // todo consider using the threadpool manager to
    // get this thread pool
    // for now place a tight restrcition on the pool size
    private static final int maxPoolSize = 10;

    private PooledExecutor pooledExecutor = null;

    // number of messages received.
    private int cnt = 0;

    /**
     * Service to get cache names and hande request broadcasts
     */
    protected UDPDiscoveryService service = null;

    private String multicastAddressString = "";

    private int multicastPort = 0;

    private ICompositeCacheManager cacheMgr;

    private boolean shutdown = false;

    /**
     * Constructor for the LateralUDPReceiver object.
     * <p>
     * We determine out own host using InetAddress
     *
     * @param service
     * @param multicastAddressString
     * @param multicastPort
     * @param cacheMgr
     * @exception IOException
     */
    public UDPDiscoveryReceiver( UDPDiscoveryService service, String multicastAddressString, int multicastPort,
                                ICompositeCacheManager cacheMgr )
        throws IOException
    {
        this.service = service;
        this.multicastAddressString = multicastAddressString;
        this.multicastPort = multicastPort;
        this.cacheMgr = cacheMgr;

        // create a small thread pool to handle a barage
        pooledExecutor = new PooledExecutor( new BoundedBuffer( 100 ), maxPoolSize );
        pooledExecutor.discardOldestWhenBlocked();
        //pooledExecutor.setMinimumPoolSize(1);
        pooledExecutor.setThreadFactory( new MyThreadFactory() );

        if ( log.isInfoEnabled() )
        {
            log.info( "constructing listener, [" + this.multicastAddressString + ":" + this.multicastPort + "]" );
        }

        try
        {
            createSocket( this.multicastAddressString, this.multicastPort );
        }
        catch ( IOException ioe )
        {
            // consider eatign this so we can go on, or constructing the socket
            // later
            throw ioe;
        }
    }

    /**
     * Creates the socket for this class.
     *
     * @param multicastAddressString
     * @param multicastPort
     * @throws IOException
     */
    private void createSocket( String multicastAddressString, int multicastPort )
        throws IOException
    {
        try
        {
            m_socket = new MulticastSocket( multicastPort );
            m_socket.joinGroup( InetAddress.getByName( multicastAddressString ) );
        }
        catch ( IOException e )
        {
            log.error( "Could not bind to multicast address [" + multicastAddressString + ":" + multicastPort + "]", e );
            throw e;
        }
    }

    /**
     * Highly unreliable. If it is processing one message while another comes in ,
     * the second message is lost. This is for low concurency peppering.
     *
     * @return the object message
     * @throws IOException
     */
    public Object waitForMessage()
        throws IOException
    {
        final DatagramPacket packet = new DatagramPacket( m_buffer, m_buffer.length );

        Object obj = null;
        try
        {
            m_socket.receive( packet );

            final ByteArrayInputStream byteStream = new ByteArrayInputStream( m_buffer, 0, packet.getLength() );

            final ObjectInputStream objectStream = new ObjectInputStream( byteStream );

            obj = objectStream.readObject();

        }
        catch ( Exception e )
        {
            log.error( "Error receving multicast packet", e );
        }
        return obj;
    }

    /** Main processing method for the LateralUDPReceiver object */
    public void run()
    {
        try
        {
            while ( !shutdown )
            {
                Object obj = waitForMessage();

                // not thread safe, but just for debugging
                cnt++;

                if ( log.isDebugEnabled() )
                {
                    log.debug( getCnt() + " messages received." );
                }

                UDPDiscoveryMessage message = null;

                try
                {
                    message = (UDPDiscoveryMessage) obj;
                    // check for null
                    if ( message != null )
                    {
                        MessageHandler handler = new MessageHandler( message );

                        pooledExecutor.execute( handler );

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Passed handler to executor." );
                        }
                    }
                    else
                    {
                        log.warn( "message is null" );
                    }
                }
                catch ( ClassCastException cce )
                {
                    log.warn( "Received unknown message type " + cce.getMessage() );
                }
            } // end while
        }
        catch ( Exception e )
        {
            log.error( "Unexpected exception in UDP receiver.", e );
            try
            {
                Thread.sleep( 100 );
                // TODO consider some failure count so we don't do this
                // forever.
            }
            catch ( Exception e2 )
            {
                log.error( "Problem sleeping", e2 );
            }
        }
        return;
    }

    /**
     * @param cnt
     *            The cnt to set.
     */
    public void setCnt( int cnt )
    {
        this.cnt = cnt;
    }

    /**
     * @return Returns the cnt.
     */
    public int getCnt()
    {
        return cnt;
    }

    /**
     * Separate thread run when a command comes into the UDPDiscoveryReceiver.
     */
    public class MessageHandler
        implements Runnable
    {
        private UDPDiscoveryMessage message = null;

        /**
         * @param message
         */
        public MessageHandler( UDPDiscoveryMessage message )
        {
            this.message = message;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            // consider comparing ports here instead.
            if ( message.getRequesterId() == LateralCacheInfo.listenerId )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "from self" );
                }
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "from another" );
                    log.debug( "Message = " + message );
                }

                // if this is a request message, have the service handle it and
                // return
                if ( message.getMessageType() == UDPDiscoveryMessage.REQUEST_BROADCAST )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Message is a Request Broadcase, will have the service handle it." );
                    }
                    service.serviceRequestBroadcast();
                    return;
                }

                try
                {
                    // get a cache and add it to the no waits
                    // the add method should not add the same.
                    // we need the listener port from the original config.
                    ITCPLateralCacheAttributes lca = null;
                    if ( service.getTcpLateralCacheAttributes() != null )
                    {
                        lca = (ITCPLateralCacheAttributes) service.getTcpLateralCacheAttributes().copy();
                    }
                    else
                    {
                        lca = new TCPLateralCacheAttributes();
                    }
                    lca.setTransmissionType( LateralCacheAttributes.TCP );
                    lca.setTcpServer( message.getHost() + ":" + message.getPort() );
                    LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lca, cacheMgr );

                    ArrayList regions = message.getCacheNames();
                    if ( regions != null )
                    {
                        // for each region get the cache
                        Iterator it = regions.iterator();
                        while ( it.hasNext() )
                        {
                            String cacheName = (String) it.next();

                            try
                            {
                                ICache ic = lcm.getCache( cacheName );

                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( "Got cache, ic = " + ic );
                                }

                                // add this to the nowaits for this cachename
                                if ( ic != null )
                                {
                                    service.addNoWait( (LateralCacheNoWait) ic );
                                    if ( log.isDebugEnabled() )
                                    {
                                        log.debug( "Called addNoWait for cacheName " + cacheName );
                                    }
                                }
                            }
                            catch ( Exception e )
                            {
                                log.error( "Problem creating no wait", e );
                            }
                        }
                        // end while
                    }
                    else
                    {
                        log.warn( "No cache names found in message " + message );
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Problem getting lateral maanger", e );
                }
            }
        }
    }

    /**
     * Allows us to set the daemon status on the executor threads
     *
     * @author aaronsm
     *
     */
    class MyThreadFactory
        implements ThreadFactory
    {
        /*
         * (non-Javadoc)
         *
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.behavior.ShutdownObserver#shutdown()
     */
    public void shutdown()
    {
        try
        {
            shutdown = true;
            m_socket.close();
            pooledExecutor.shutdownNow();
        }
        catch ( Exception e )
        {
            log.error( "Problem closing socket" );
        }
    }
}
// end class
