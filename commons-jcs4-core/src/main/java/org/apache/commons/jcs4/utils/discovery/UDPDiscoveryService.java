package org.apache.commons.jcs4.utils.discovery;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.behavior.IRequireScheduler;
import org.apache.commons.jcs4.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs4.log.Log;
import org.apache.commons.jcs4.utils.discovery.behavior.IDiscoveryListener;
import org.apache.commons.jcs4.utils.net.HostNameUtil;
import org.apache.commons.jcs4.utils.threadpool.DaemonThreadFactory;

/**
 * This service creates a listener that can create lateral caches and add them to the no wait list.
 * <p>
 * It also creates a sender that periodically broadcasts its availability.
 * </p>
 * <p>
 * The sender also broadcasts a request for other caches to broadcast their addresses.
 * </p>
 */
public class UDPDiscoveryService
    implements IShutdownObserver, IRequireScheduler
{
    /** The logger */
    private static final Log log = Log.getLog( UDPDiscoveryService.class );

    /** Manage thread that listens for messages */
    private ExecutorService udpReceiverExecutor;

    /** The runnable that the receiver thread runs */
    private UDPDiscoveryReceiver receiver;

    /** Attributes */
    private UDPDiscoveryAttributes udpDiscoveryAttributes;

    /** Used to serialize messages */
    private final IElementSerializer serializer;

    /** Is this shut down? */
    private final AtomicBoolean shutdown = new AtomicBoolean();

    /** This is a set of services that have been discovered. */
    private final ConcurrentMap<Integer, DiscoveredService> discoveredServices =
            new ConcurrentHashMap<>();

    /** This a list of regions that are configured to use discovery. */
    private final Set<String> cacheNames = new CopyOnWriteArraySet<>();

    /** Set of listeners. */
    private final Set<IDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    /** Detected multicast address */
    private InetAddress multicastAddress;

    /** Handle to cancel the scheduled broadcast task */
    private ScheduledFuture<?> broadcastTaskFuture;

    /** Handle to cancel the scheduled cleanup task */
    private ScheduledFuture<?> cleanupTaskFuture;

    /**
     * Constructs a new instance.
     *
     * @param attributes settings of service
     * @param serializer the serializer to use to send and receive messages
     * @since 3.1
     */
    public UDPDiscoveryService(final UDPDiscoveryAttributes attributes, final IElementSerializer serializer)
    {
        this.udpDiscoveryAttributes = attributes;
        this.serializer = serializer;
        this.udpReceiverExecutor = Executors.newSingleThreadExecutor(new DaemonThreadFactory("JCS-UDPReceiver-"));

        try
        {
            this.multicastAddress = InetAddress.getByName(
                    udpDiscoveryAttributes.udpDiscoveryAddr());

            // Set service address if still empty
            if (udpDiscoveryAttributes.serviceAddress() == null ||
                    udpDiscoveryAttributes.serviceAddress().isEmpty())
            {
                // Use same interface as for multicast
                NetworkInterface serviceInterface = null;
                if (udpDiscoveryAttributes.udpDiscoveryInterface() != null)
                {
                    serviceInterface = NetworkInterface.getByName(
                            udpDiscoveryAttributes.udpDiscoveryInterface());
                }
                else
                {
                    serviceInterface = HostNameUtil.getMulticastNetworkInterface();
                }

                try
                {
                    InetAddress serviceAddress = null;

                    for (final Enumeration<InetAddress> addresses = serviceInterface.getInetAddresses();
                            addresses.hasMoreElements();)
                    {
                        serviceAddress = addresses.nextElement();

                        if (multicastAddress instanceof Inet6Address)
                        {
                            if (serviceAddress instanceof Inet6Address &&
                                !serviceAddress.isLoopbackAddress() &&
                                !serviceAddress.isMulticastAddress() &&
                                serviceAddress.isLinkLocalAddress())
                            {
                                // if Multicast uses IPv6, try to publish our IPv6 address
                                break;
                            }
                        }
                        else if (serviceAddress instanceof Inet4Address &&
                            !serviceAddress.isLoopbackAddress() &&
                            !serviceAddress.isMulticastAddress() &&
                            serviceAddress.isSiteLocalAddress())
                        {
                            // if Multicast uses IPv4, try to publish our IPv4 address
                            break;
                        }
                    }

                    if (serviceAddress == null)
                    {
                        // Nothing found for given interface, fall back
                        serviceAddress = HostNameUtil.getLocalHostLANAddress();
                    }

                    this.udpDiscoveryAttributes = udpDiscoveryAttributes.withServiceAddress(
                            serviceAddress.getHostAddress());
                }
                catch ( final UnknownHostException e )
                {
                    log.error( "Couldn't get local host address", e );
                }
            }
        }
        catch ( final IOException e )
        {
            log.error( "Problem creating UDPDiscoveryService, address [{0}] "
                    + "port [{1}] we won't be able to find any other caches",
                    udpDiscoveryAttributes.udpDiscoveryAddr(),
                    udpDiscoveryAttributes.udpDiscoveryPort(), e );
        }

        // initiate sender broadcast
        initiateBroadcast();
    }

    /**
     * Adds a listener.
     *
     * @param listener
     * @return true if it wasn't already in the set
     */
    public boolean addDiscoveryListener( final IDiscoveryListener listener )
    {
        return discoveryListeners.add( listener );
    }

    /**
     * Add a service to the list. Update the held copy if we already know about it.
     *
     * @param discoveredService discovered service
     */
    protected void addOrUpdateService( final DiscoveredService discoveredService )
    {
        // We want to replace the old one, since we may add info that is not part of the equals.
        // The equals method on the object being added is intentionally restricted.
        discoveredServices.merge(discoveredService.hashCode(), discoveredService, (oldService, newService) -> {
            log.debug( "Set contains service." );
            log.debug( "Updating service in the set {0}", newService );

            // Update the list of cache names if it has changed.
            // need to update the time this sucks. add has no effect convert to a map
            if (!oldService.getCacheNames().equals(newService.getCacheNames()))
            {
                log.info( "List of cache names changed for service: {0}", newService );

                // replace it, we want to reset the payload and the last heard from time.
                return newService;
            }

            if (oldService.getLastHearFromTime() != newService.getLastHearFromTime())
            {
                return newService;
            }

            return oldService;
        });

        // Always Notify the listeners
        // If we don't do this, then if a region using the default config is initialized after notification,
        // it will never get the service in it's no wait list.
        // Leave it to the listeners to decide what to do.
        discoveryListeners.forEach(listener -> listener.addDiscoveredService(discoveredService));
    }

    /**
     * Adds a region to the list that is participating in discovery.
     *
     * @param cacheName
     */
    public void addParticipatingCacheName( final String cacheName )
    {
        cacheNames.add( cacheName );
    }

    /**
     * This goes through the list of services and removes those that we haven't heard from in longer
     * than the max idle time.
     *
     * @since 3.1
     */
    protected void cleanup()
    {
        final Instant now = Instant.now();

        // the listeners need to be notified.
        getDiscoveredServices().stream()
            .filter(service -> {
                if (now.isAfter(service.getLastHearFromTime().plus(udpDiscoveryAttributes.maxIdleTime())))
                {
                    log.info( "Removing service, since we haven't heard from it in "
                            + "{0}. service = {1}",
                            udpDiscoveryAttributes.maxIdleTime(), service );
                    return true;
                }

                return false;
            })
            // remove the bad ones
            // call this so the listeners get notified
            .forEach(this::removeDiscoveredService);
    }

    /**
     * Gets all the cache names we have facades for.
     *
     * @return ArrayList
     */
    protected ArrayList<String> getCacheNames()
    {
        return new ArrayList<>(cacheNames);
    }

    /**
     * @return the discoveredServices.
     */
    public Set<DiscoveredService> getDiscoveredServices()
    {
        return new HashSet<>(discoveredServices.values());
    }

    /**
     * Return the serializer implementation
     *
     * @return the serializer
     * @since 3.1
     */
    protected IElementSerializer getSerializer()
    {
        return serializer;
    }

    /**
     * Initial request that the other caches let it know their addresses.
     *
     * @since 3.1
     */
    private void initiateBroadcast()
    {
        log.debug( "Creating sender for discoveryAddress = [{0}] and "
                + "discoveryPort = [{1}] myHostName = [{2}] and port = [{3}]",
                udpDiscoveryAttributes::udpDiscoveryAddr,
                udpDiscoveryAttributes::udpDiscoveryPort,
                udpDiscoveryAttributes::serviceAddress,
                udpDiscoveryAttributes::servicePort);

        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                udpDiscoveryAttributes, serializer))
        {
            sender.requestBroadcast();

            log.debug( "Sent a request broadcast to the group" );
        }
        catch ( final IOException e )
        {
            log.error( "Problem sending a Request Broadcast", e );
        }
    }

    /**
     * Removes the discovered service from the list and calls the discovery listener.
     *
     * @param service
     */
    public void removeDiscoveredService( final DiscoveredService service )
    {
        if (discoveredServices.remove(service.hashCode()) != null)
        {
            log.info( "Removing {0}", service );
        }

        discoveryListeners.forEach(listener -> listener.removeDiscoveredService(service));
    }

    /**
     * Removes a listener.
     *
     * @param listener
     * @return true if it was in the set
     */
    public boolean removeDiscoveryListener( final IDiscoveryListener listener )
    {
        return discoveryListeners.remove( listener );
    }

    /**
     * Process the incoming message.
     */
    protected void processMessage(final UDPDiscoveryMessage message)
    {
        final DiscoveredService discoveredService = new DiscoveredService(message);

        switch (message.getMessageType())
        {
            case REMOVE:
                log.debug( "Removing service from set {0}", discoveredService );
                removeDiscoveredService( discoveredService );
                break;
            case REQUEST:
                // if this is a request message, have the service handle it and
                // return
                log.debug( "Message is a Request Broadcast, will have the service handle it." );
                serviceRequestBroadcast();
                break;
            case PASSIVE:
            default:
                log.debug( "Adding or updating service to set {0}", discoveredService );
                addOrUpdateService( discoveredService );
                break;
        }
    }

    /**
     * Send a passive broadcast in response to a request broadcast. Never send a request for a
     * request. We can respond to our own requests, since a request broadcast is not intended as a
     * connection request. We might want to only send messages, so we would send a request, but
     * never a passive broadcast.
     */
    private void serviceRequestBroadcast()
    {
        // create this connection each time.
        // more robust
        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                udpDiscoveryAttributes, serializer))
        {
            sender.passiveBroadcast(
                    udpDiscoveryAttributes.serviceAddress(),
                    udpDiscoveryAttributes.servicePort(),
                    getCacheNames() );

            log.debug( "Called sender to issue a passive broadcast" );
        }
        catch ( final IOException e )
        {
            log.error( "Problem calling the UDP Discovery Sender, address [{0}] "
                    + "port [{1}]",
                    udpDiscoveryAttributes.udpDiscoveryAddr(),
                    udpDiscoveryAttributes.udpDiscoveryPort(), e );
        }
    }

    /**
     * @see org.apache.commons.jcs4.engine.behavior.IRequireScheduler#setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
     */
    @Override
    public void setScheduledExecutorService(final ScheduledExecutorService scheduledExecutor)
    {
        this.broadcastTaskFuture = scheduledExecutor.scheduleAtFixedRate(
                this::serviceRequestBroadcast, 0, 15, TimeUnit.SECONDS);

        /** Removes things that have been idle for too long */
        // I'm going to use this as both, but it could happen
        // that something could hang around twice the time using this as the
        // delay and the idle time.
        this.cleanupTaskFuture = scheduledExecutor.scheduleAtFixedRate(
                this::cleanup, 0,
                udpDiscoveryAttributes.maxIdleTime().toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Shuts down the receiver.
     */
    @Override
    public void shutdown()
    {
        if (shutdown.compareAndSet(false, true))
        {
            // Stop the scheduled tasks
            if (broadcastTaskFuture != null)
            {
                broadcastTaskFuture.cancel(false);
            }
            if (cleanupTaskFuture != null)
            {
                cleanupTaskFuture.cancel(false);
            }

            udpReceiverExecutor.shutdown();
            if (receiver != null)
            {
                log.info( "Shutting down UDP discovery service receiver." );
                receiver.shutdown();
            }

            log.info( "Shutting down UDP discovery service sender." );
            // also call the shutdown on the sender thread itself, which
            // will result in a remove command.
            shutdownBroadcast();
        }
        else
        {
            log.debug( "Shutdown already called." );
        }
    }

    /**
     * Issues a remove broadcast to the others.
     *
     * @since 3.1
     */
    private void shutdownBroadcast()
    {
        // create this connection each time.
        // more robust
        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                udpDiscoveryAttributes, serializer))
        {
            sender.removeBroadcast(
                    udpDiscoveryAttributes.serviceAddress(),
                    udpDiscoveryAttributes.servicePort(),
                    getCacheNames() );

            log.debug( "Called sender to issue a remove broadcast in shutdown." );
        }
        catch ( final IOException e )
        {
            log.error( "Problem calling the UDP Discovery Sender", e );
        }
    }

    /**
     * Start necessary receiver thread
     */
    public void startup()
    {
        try
        {
            this.receiver = new UDPDiscoveryReceiver( this::processMessage,
                    udpDiscoveryAttributes.udpDiscoveryInterface(),
                    multicastAddress,
                    udpDiscoveryAttributes.udpDiscoveryPort() );
            this.receiver.setSerializer(serializer);
        }
        catch ( final IOException e )
        {
            log.error( "Problem creating UDPDiscoveryReceiver, address [{0}] "
                    + "port [{1}] we won't be able to find any other caches",
                    multicastAddress,
                    udpDiscoveryAttributes.udpDiscoveryPort(), e );
        }

        udpReceiverExecutor.execute(receiver);
    }
}
