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

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;
import org.apache.commons.jcs3.utils.threadpool.ThreadPoolManager;

/** Receives UDP Discovery messages. */
public class UDPDiscoveryReceiver
    implements Runnable, IShutdownObserver
{
    /** The log factory */
    private static final Log log = LogManager.getLog( UDPDiscoveryReceiver.class );

    /** The channel used for communication. */
    private DatagramChannel multicastChannel;

    /** The group membership key. */
    private MembershipKey multicastGroupKey;

    /** The selector. */
    private Selector selector;

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

    /** Serializer */
    private IElementSerializer serializer;

    /** Is it shutdown. */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Constructor for the UDPDiscoveryReceiver object.
     * <p>
     * We determine out own host using InetAddress
     *<p>
     * @param service
     * @param multicastInterfaceString
     * @param multicastAddressString
     * @param multicastPort
     * @throws IOException
     */
    public UDPDiscoveryReceiver( final UDPDiscoveryService service,
            final String multicastInterfaceString,
            final String multicastAddressString,
            final int multicastPort )
        throws IOException
    {
        this(service, multicastInterfaceString,
                InetAddress.getByName( multicastAddressString ),
                multicastPort);
    }

    /**
     * Constructor for the UDPDiscoveryReceiver object.
     * <p>
     * @param service
     * @param multicastInterfaceString
     * @param multicastAddress
     * @param multicastPort
     * @throws IOException
     * @since 3.1
     */
    public UDPDiscoveryReceiver( final UDPDiscoveryService service,
            final String multicastInterfaceString,
            final InetAddress multicastAddress,
            final int multicastPort )
        throws IOException
    {
        this.service = service;
        if (service != null)
        {
            this.serializer = service.getSerializer();
        }

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
    private void createSocket( final String multicastInterfaceString, final InetAddress multicastAddress,
            final int multicastPort )
        throws IOException
    {
        try
        {
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
                log.info("Using network interface {0}", multicastInterface::getDisplayName);
            }

            multicastChannel = DatagramChannel.open(
                    multicastAddress instanceof Inet6Address ?
                            StandardProtocolFamily.INET6 : StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, multicastInterface)
                    .bind(new InetSocketAddress(multicastPort));
            multicastChannel.configureBlocking(false);

            log.info("Joining Group: [{0}] on {1}", multicastAddress, multicastInterface);
            multicastGroupKey = multicastChannel.join(multicastAddress, multicastInterface);

            selector = Selector.open();
            multicastChannel.register(selector, SelectionKey.OP_READ);
        }
        catch ( final IOException e )
        {
            log.error( "Could not bind to multicast address [{0}:{1}]", multicastAddress,
                    multicastPort, e );
            throw e;
        }
    }

    private final ArrayBlockingQueue<UDPDiscoveryMessage> msgQueue =
            new ArrayBlockingQueue<>(maxPoolSize);

    /**
     * Wait for multicast message
     *
     * @return the object message
     * @throws IOException
     * @deprecated no longer used
     */
    @Deprecated
    public Object waitForMessage()
        throws IOException
    {
        try
        {
            return msgQueue.take();
        }
        catch (InterruptedException e)
        {
            throw new IOException("Interrupted waiting for message", e);
        }
    }

    /** Main processing method for the UDPDiscoveryReceiver object */
    @Override
    public void run()
    {
        try
        {
            log.debug( "Waiting for message." );

            while (!shutdown.get())
            {
                int activeKeys = selector.select();
                if (activeKeys == 0)
                {
                    continue;
                }

                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();)
                {
                    if (shutdown.get())
                    {
                        break;
                    }

                    SelectionKey key = i.next();

                    if (!key.isValid())
                    {
                        continue;
                    }

                    if (key.isReadable())
                    {
                        cnt.incrementAndGet();
                        log.debug( "{0} messages received.", this::getCnt );

                        DatagramChannel mc = (DatagramChannel) key.channel();

                        ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
                        InetSocketAddress sourceAddress =
                                (InetSocketAddress) mc.receive(byteBuffer);
                        byteBuffer.flip();

                        try
                        {
                            log.debug("Received packet from address [{0}]", sourceAddress);

                            Object obj = serializer.deSerialize(byteBuffer.array(), null);

                            if (obj instanceof UDPDiscoveryMessage)
                            {
                                // Ensure that the address we're supposed to send to is, indeed, the address
                                // of the machine on the other end of this connection.  This guards against
                                // instances where we don't exactly get the right local host address
                                final UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
                                msg.setHost(sourceAddress.getHostString());

                                log.debug( "Read object from address [{0}], object=[{1}]",
                                        sourceAddress, obj );

                                // Just to keep the functionality of the deprecated waitForMessage method
                                synchronized (msgQueue)
                                {
                                    // Check if queue full already?
                                    if (msgQueue.remainingCapacity() == 0)
                                    {
                                        // remove oldest element from queue
                                        msgQueue.remove();
                                    }

                                    msgQueue.add(msg);
                                }

                                pooledExecutor.execute(() -> handleMessage(msg));
                                log.debug( "Passed handler to executor." );
                            }
                        }
                        catch ( final IOException | ClassNotFoundException e )
                        {
                            log.error( "Error receiving multicast packet", e );
                        }

                        i.remove();
                    }
                }
            } // end while
        }
        catch ( final IOException e )
        {
            log.error( "Unexpected exception in UDP receiver.", e );
        }
    }

    /**
     * @param cnt The cnt to set.
     */
    public void setCnt( final int cnt )
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
     * For testing
     *
     * @param serializer the serializer to set
     * @since 3.1
     */
    protected void setSerializer(IElementSerializer serializer)
    {
        this.serializer = serializer;
    }

    /**
     * Separate thread run when a command comes into the UDPDiscoveryReceiver.
     * @deprectaed No longer used
     */
    @Deprecated
    public class MessageHandler
        implements Runnable
    {
        /** The message to handle. Passed in during construction. */
        private final UDPDiscoveryMessage message;

        /**
         * @param message
         */
        public MessageHandler( final UDPDiscoveryMessage message )
        {
            this.message = message;
        }

        /**
         * Process the message.
         */
        @Override
        public void run()
        {
            handleMessage(message);
        }
    }

    /**
     * Separate thread run when a command comes into the UDPDiscoveryReceiver.
     */
    private void handleMessage(UDPDiscoveryMessage message)
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
                processMessage(message);
            }
        }
    }

    /**
     * Process the incoming message.
     */
    private void processMessage(UDPDiscoveryMessage message)
    {
        final DiscoveredService discoveredService = new DiscoveredService(message);

        switch (message.getMessageType())
        {
            case REMOVE:
                log.debug( "Removing service from set {0}", discoveredService );
                service.removeDiscoveredService( discoveredService );
                break;
            case REQUEST:
                // if this is a request message, have the service handle it and
                // return
                log.debug( "Message is a Request Broadcast, will have the service handle it." );
                service.serviceRequestBroadcast();
                break;
            case PASSIVE:
            default:
                log.debug( "Adding or updating service to set {0}", discoveredService );
                service.addOrUpdateService( discoveredService );
                break;
        }
    }

    /** Shuts down the socket. */
    @Override
    public void shutdown()
    {
        if (shutdown.compareAndSet(false, true))
        {
            try
            {
                selector.close();
                multicastGroupKey.drop();
                multicastChannel.close();
            }
            catch ( final IOException e )
            {
                log.error( "Problem closing socket" );
            }
        }
    }
}
