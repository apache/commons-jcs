package org.apache.jcs.utils.discovery;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.IShutdownObserver;

/** Receives UDP Discovery messages. */
public class UDPDiscoveryReceiver
    implements Runnable, IShutdownObserver
{
    /** The log factory */
    protected final static Log log = LogFactory.getLog( UDPDiscoveryReceiver.class );

    /** buffer */
    private final byte[] mBuffer = new byte[65536];

    /** The socket used for communication. */
    private MulticastSocket mSocket;

    /**
     * TODO: Consider using the threadpool manager to get this thread pool. For now place a tight
     * restriction on the pool size
     */
    private static final int maxPoolSize = 2;

    /** The processor */
    private ThreadPoolExecutor pooledExecutor = null;

    /** number of messages received. For debugging and testing. */
    private int cnt = 0;

    /** Service to get cache names and handle request broadcasts */
    protected UDPDiscoveryService service = null;

    /** Address */
    private String multicastAddressString = "";

    /** The port */
    private int multicastPort = 0;

    /** Is it shutdown. */
    private boolean shutdown = false;

    /**
     * Constructor for the LateralUDPReceiver object.
     * <p>
     * We determine out own host using InetAddress
     *<p>
     * @param service
     * @param multicastAddressString
     * @param multicastPort
     * @exception IOException
     */
    public UDPDiscoveryReceiver( UDPDiscoveryService service, String multicastAddressString, int multicastPort )
        throws IOException
    {
        this.service = service;
        this.multicastAddressString = multicastAddressString;
        this.multicastPort = multicastPort;

        // create a small thread pool to handle a barrage
        pooledExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(maxPoolSize, new MyThreadFactory());
        pooledExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        //pooledExecutor.setMinimumPoolSize(1);

        if ( log.isInfoEnabled() )
        {
            log.info( "Constructing listener, [" + this.multicastAddressString + ":" + this.multicastPort + "]" );
        }

        try
        {
            createSocket( this.multicastAddressString, this.multicastPort );
        }
        catch ( IOException ioe )
        {
            // consider eating this so we can go on, or constructing the socket
            // later
            throw ioe;
        }
    }

    /**
     * Creates the socket for this class.
     * <p>
     * @param multicastAddressString
     * @param multicastPort
     * @throws IOException
     */
    private void createSocket( String multicastAddressString, int multicastPort )
        throws IOException
    {
        try
        {
            mSocket = new MulticastSocket( multicastPort );
            if ( log.isInfoEnabled() )
            {
                log.info( "Joining Group: [" + InetAddress.getByName( multicastAddressString ) + "]" );
            }
            mSocket.joinGroup( InetAddress.getByName( multicastAddressString ) );
        }
        catch ( IOException e )
        {
            log.error( "Could not bind to multicast address [" + InetAddress.getByName( multicastAddressString ) + ":" + multicastPort + "]", e );
            throw e;
        }
    }

    /**
     * Highly unreliable. If it is processing one message while another comes in, the second
     * message is lost. This is for low concurrency peppering.
     * <p>
     * @return the object message
     * @throws IOException
     */
    public Object waitForMessage()
        throws IOException
    {
        final DatagramPacket packet = new DatagramPacket( mBuffer, mBuffer.length );

        Object obj = null;
        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Waiting for message." );
            }

            mSocket.receive( packet );

            if ( log.isDebugEnabled() )
            {
                log.debug( "Received packet from address [" + packet.getSocketAddress() + "]" );
            }

            final ByteArrayInputStream byteStream = new ByteArrayInputStream( mBuffer, 0, packet.getLength() );
            final ObjectInputStream objectStream = new ObjectInputStream( byteStream );
            obj = objectStream.readObject();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Read object from address [" + packet.getSocketAddress() + "], object=[" + obj + "]" );
            }
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
     * @param cnt The cnt to set.
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
        /** The message to handle. Passed in during construction. */
        private UDPDiscoveryMessage message = null;

        /**
         * @param message
         */
        public MessageHandler( UDPDiscoveryMessage message )
        {
            this.message = message;
        }

        /**
         * Process the message.
         */
        public void run()
        {
            // consider comparing ports here instead.
            if ( message.getRequesterId() == UDPDiscoveryInfo.listenerId )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Ignoring message sent from self" );
                }
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Process message sent from another" );
                    log.debug( "Message = " + message );
                }

                if ( message.getHost() == null || message.getCacheNames() == null || message.getCacheNames().isEmpty() )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Ignoring invalid message: " + message );
                    }
                }
                else
                {
                    processMessage();
                }
            }
        }

        /**
         * Process the incoming message.
         */
        private void processMessage()
        {
            DiscoveredService discoveredService = new DiscoveredService();
            discoveredService.setServiceAddress( message.getHost() );
            discoveredService.setCacheNames( message.getCacheNames() );
            discoveredService.setServicePort( message.getPort() );
            discoveredService.setLastHearFromTime( System.currentTimeMillis() );

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
            else if ( message.getMessageType() == UDPDiscoveryMessage.REMOVE_BROADCAST )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Removing service from set " + discoveredService );
                }
                service.removeDiscoveredService( discoveredService );
            }
            else
            {
                service.addOrUpdateService( discoveredService );
            }
        }
    }

    /**
     * Allows us to set the daemon status on the executor threads
     */
    class MyThreadFactory
        implements ThreadFactory
    {
        /**
         * Sets the thread to daemon.
         * <p>
         * @param runner
         * @return a daemon thread
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            String oldName = t.getName();
            t.setName( "JCS-UDPDiscoveryReceiver-" + oldName );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }

    /** Shuts down the socket. */
    public void shutdown()
    {
        try
        {
            shutdown = true;
            mSocket.leaveGroup( InetAddress.getByName( multicastAddressString ) );
            mSocket.close();
            pooledExecutor.shutdownNow();
        }
        catch ( Exception e )
        {
            log.error( "Problem closing socket" );
        }
    }
}
