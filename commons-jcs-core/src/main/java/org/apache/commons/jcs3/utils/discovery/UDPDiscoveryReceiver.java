package org.apache.commons.jcs3.utils.discovery;

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
import java.net.NetworkInterface;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.io.ObjectInputStreamClassLoaderAware;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryMessage.BroadcastType;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration;
import org.apache.commons.jcs3.utils.threadpool.ThreadPoolManager;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;

/** Receives UDP Discovery messages. */
public class UDPDiscoveryReceiver
    implements Runnable, IShutdownObserver
{
    /** The log factory */
    private static final Log log = LogManager.getLog( UDPDiscoveryReceiver.class );

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
    private final ExecutorService pooledExecutor;

    /** number of messages received. For debugging and testing. */
    private final AtomicInteger cnt = new AtomicInteger(0);

    /** Service to get cache names and handle request broadcasts */
    private final UDPDiscoveryService service;

    /** Multicast address */
    private final InetAddress multicastAddress;

    /** Is it shutdown. */
    private boolean shutdown = false;

    /**
     * Constructor for the LateralUDPReceiver object.
     * <p>
     * We determine out own host using InetAddress
     *<p>
     * @param service
     * @param multicastInterfaceString
     * @param multicastAddressString
     * @param multicastPort
     * @throws IOException
     */
    public UDPDiscoveryReceiver( UDPDiscoveryService service, String multicastInterfaceString,
            String multicastAddressString, int multicastPort )
        throws IOException
    {
        this.service = service;
        this.multicastAddress = InetAddress.getByName( multicastAddressString );

        // create a small thread pool to handle a barrage
        this.pooledExecutor = ThreadPoolManager.getInstance().createPool(
        		new PoolConfiguration(false, 0, maxPoolSize, maxPoolSize, 0,
        		        WhenBlockedPolicy.DISCARDOLDEST, maxPoolSize),
        		"JCS-UDPDiscoveryReceiver-", Thread.MIN_PRIORITY);

        log.info( "Constructing listener, [{0}:{1}]", multicastAddress, multicastPort );

        createSocket( multicastInterfaceString, multicastAddress, multicastPort );
    }

    /**
     * Creates the socket for this class.
     * <p>
     * @param multicastInterfaceString
     * @param multicastAddress
     * @param multicastPort
     * @throws IOException
     */
    private void createSocket( String multicastInterfaceString, InetAddress multicastAddress,
            int multicastPort )
        throws IOException
    {
        try
        {
            mSocket = new MulticastSocket( multicastPort );
            if (log.isInfoEnabled())
            {
                log.info( "Joining Group: [{0}]", multicastAddress );
            }

            // Use dedicated interface if specified
            NetworkInterface multicastInterface = null;
            if (multicastInterfaceString != null)
            {
                multicastInterface = NetworkInterface.getByName(multicastInterfaceString);
            }
            else
            {
                multicastInterface = HostNameUtil.getMulticastNetworkInterface();
            }
            if (multicastInterface != null)
            {
                log.info("Using network interface {0}", multicastInterface.getDisplayName());
                mSocket.setNetworkInterface(multicastInterface);
            }

            mSocket.joinGroup( multicastAddress );
        }
        catch ( IOException e )
        {
            log.error( "Could not bind to multicast address [{0}:{1}]", multicastAddress,
                    multicastPort, e );
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
            log.debug( "Waiting for message." );

            mSocket.receive( packet );

            log.debug( "Received packet from address [{0}]",
                    () -> packet.getSocketAddress() );

            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(mBuffer, 0, packet.getLength());
                 ObjectInputStream objectStream = new ObjectInputStreamClassLoaderAware(byteStream, null))
            {
                obj = objectStream.readObject();
            }

            if ( obj instanceof UDPDiscoveryMessage )
            {
            	// Ensure that the address we're supposed to send to is, indeed, the address
            	// of the machine on the other end of this connection.  This guards against
            	// instances where we don't exactly get the right local host address
            	UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
            	msg.setHost(packet.getAddress().getHostAddress());

                log.debug( "Read object from address [{0}], object=[{1}]",
                        packet.getSocketAddress(), obj );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error receiving multicast packet", e );
        }

        return obj;
    }

    /** Main processing method for the LateralUDPReceiver object */
    @Override
    public void run()
    {
        try
        {
            while ( !shutdown )
            {
                Object obj = waitForMessage();

                cnt.incrementAndGet();

                log.debug( "{0} messages received.", () -> getCnt() );

                UDPDiscoveryMessage message = null;

                try
                {
                    message = (UDPDiscoveryMessage) obj;
                    // check for null
                    if ( message != null )
                    {
                        MessageHandler handler = new MessageHandler( message );

                        pooledExecutor.execute( handler );

                        log.debug( "Passed handler to executor." );
                    }
                    else
                    {
                        log.warn( "message is null" );
                    }
                }
                catch ( ClassCastException cce )
                {
                    log.warn( "Received unknown message type", cce.getMessage() );
                }
            } // end while
        }
        catch ( IOException e )
        {
            log.error( "Unexpected exception in UDP receiver.", e );
            try
            {
                Thread.sleep( 100 );
                // TODO consider some failure count so we don't do this
                // forever.
            }
            catch ( InterruptedException e2 )
            {
                log.error( "Problem sleeping", e2 );
            }
        }
    }

    /**
     * @param cnt The cnt to set.
     */
    public void setCnt( int cnt )
    {
        this.cnt.set(cnt);
    }

    /**
     * @return Returns the cnt.
     */
    public int getCnt()
    {
        return cnt.get();
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
        @SuppressWarnings("synthetic-access")
        @Override
        public void run()
        {
            // consider comparing ports here instead.
            if ( message.getRequesterId() == CacheInfo.listenerId )
            {
                log.debug( "Ignoring message sent from self" );
            }
            else
            {
                log.debug( "Process message sent from another" );
                log.debug( "Message = {0}", message );

                if ( message.getHost() == null || message.getCacheNames() == null || message.getCacheNames().isEmpty() )
                {
                    log.debug( "Ignoring invalid message: {0}", message );
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
        @SuppressWarnings("synthetic-access")
        private void processMessage()
        {
            DiscoveredService discoveredService = new DiscoveredService();
            discoveredService.setServiceAddress( message.getHost() );
            discoveredService.setCacheNames( message.getCacheNames() );
            discoveredService.setServicePort( message.getPort() );
            discoveredService.setLastHearFromTime( System.currentTimeMillis() );

            // if this is a request message, have the service handle it and
            // return
            if ( message.getMessageType() == BroadcastType.REQUEST )
            {
                log.debug( "Message is a Request Broadcast, will have the service handle it." );
                service.serviceRequestBroadcast();
                return;
            }
            else if ( message.getMessageType() == BroadcastType.REMOVE )
            {
                log.debug( "Removing service from set {0}", discoveredService );
                service.removeDiscoveredService( discoveredService );
            }
            else
            {
                service.addOrUpdateService( discoveredService );
            }
        }
    }

    /** Shuts down the socket. */
    @Override
    public void shutdown()
    {
        if (!shutdown)
        {
            try
            {
                shutdown = true;
                mSocket.leaveGroup( multicastAddress );
                mSocket.close();
                pooledExecutor.shutdownNow();
            }
            catch ( IOException e )
            {
                log.error( "Problem closing socket" );
            }
        }
    }
}
