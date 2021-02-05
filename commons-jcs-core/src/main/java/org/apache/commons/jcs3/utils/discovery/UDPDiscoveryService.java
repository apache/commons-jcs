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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs3.engine.behavior.IRequireScheduler;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.discovery.behavior.IDiscoveryListener;
import org.apache.commons.jcs3.utils.net.HostNameUtil;

/**
 * This service creates a listener that can create lateral caches and add them to the no wait list.
 * <p>
 * It also creates a sender that periodically broadcasts its availability.
 * <p>
 * The sender also broadcasts a request for other caches to broadcast their addresses.
 * <p>
 * @author Aaron Smuts
 */
public class UDPDiscoveryService
    implements IShutdownObserver, IRequireScheduler
{
    /** The logger */
    private static final Log log = LogManager.getLog( UDPDiscoveryService.class );

    /** thread that listens for messages */
    private Thread udpReceiverThread;

    /** the runnable that the receiver thread runs */
    private UDPDiscoveryReceiver receiver;

    /** attributes */
    private UDPDiscoveryAttributes udpDiscoveryAttributes;

    /** is this shut down? */
    private AtomicBoolean shutdown = new AtomicBoolean(false);

    /** This is a set of services that have been discovered. */
    private final Set<DiscoveredService> discoveredServices = new CopyOnWriteArraySet<>();

    /** This a list of regions that are configured to use discovery. */
    private final Set<String> cacheNames = new CopyOnWriteArraySet<>();

    /** Set of listeners. */
    private final Set<IDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    /** Handle to cancel the scheduled broadcast task */
    private ScheduledFuture<?> broadcastTaskFuture = null;

    /** Handle to cancel the scheduled cleanup task */
    private ScheduledFuture<?> cleanupTaskFuture = null;

    /**
     * @param attributes
     */
    public UDPDiscoveryService( final UDPDiscoveryAttributes attributes)
    {
        udpDiscoveryAttributes = attributes.clone();

        try
        {
            // todo, you should be able to set this
            udpDiscoveryAttributes.setServiceAddress( HostNameUtil.getLocalHostAddress() );
        }
        catch ( final UnknownHostException e )
        {
            log.error( "Couldn't get localhost address", e );
        }

        try
        {
            // todo need some kind of recovery here.
            receiver = new UDPDiscoveryReceiver( this,
                    getUdpDiscoveryAttributes().getUdpDiscoveryInterface(),
                    getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                    getUdpDiscoveryAttributes().getUdpDiscoveryPort() );
        }
        catch ( final IOException e )
        {
            log.error( "Problem creating UDPDiscoveryReceiver, address [{0}] "
                    + "port [{1}] we won't be able to find any other caches",
                    getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                    getUdpDiscoveryAttributes().getUdpDiscoveryPort(), e );
        }

        // initiate sender broadcast
        initiateBroadcast();
    }

    /**
     * @see org.apache.commons.jcs3.engine.behavior.IRequireScheduler#setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
     */
    @Override
    public void setScheduledExecutorService(final ScheduledExecutorService scheduledExecutor)
    {
        this.broadcastTaskFuture = scheduledExecutor.scheduleAtFixedRate(
                () -> serviceRequestBroadcast(), 0, 15, TimeUnit.SECONDS);

        /** removes things that have been idle for too long */
        // I'm going to use this as both, but it could happen
        // that something could hang around twice the time using this as the
        // delay and the idle time.
        this.cleanupTaskFuture = scheduledExecutor.scheduleAtFixedRate(
                () -> cleanup(), 0,
                getUdpDiscoveryAttributes().getMaxIdleTimeSec(), TimeUnit.SECONDS);
    }

    /**
     * This goes through the list of services and removes those that we haven't heard from in longer
     * than the max idle time.
     */
    protected void cleanup()
    {
        final long now = System.currentTimeMillis();

        // the listeners need to be notified.
        getDiscoveredServices().stream()
            .filter(service -> {
                if (now - service.getLastHearFromTime() > getUdpDiscoveryAttributes().getMaxIdleTimeSec() * 1000)
                {
                    log.info( "Removing service, since we haven't heard from it in "
                            + "{0} seconds. service = {1}",
                            getUdpDiscoveryAttributes().getMaxIdleTimeSec(), service );
                    return true;
                }

                return false;
            })
            // remove the bad ones
            // call this so the listeners get notified
            .forEach(service -> removeDiscoveredService(service));
    }

    /**
     * Initial request that the other caches let it know their addresses.
     */
    public void initiateBroadcast()
    {
        log.debug( "Creating sender thread for discoveryAddress = [{0}] and "
                + "discoveryPort = [{1}] myHostName = [{2}] and port = [{3}]",
                () -> getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                () -> getUdpDiscoveryAttributes().getUdpDiscoveryPort(),
                () -> getUdpDiscoveryAttributes().getServiceAddress(),
                () -> getUdpDiscoveryAttributes().getServicePort() );

        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                getUdpDiscoveryAttributes().getUdpDiscoveryPort(),
                getUdpDiscoveryAttributes().getUdpTTL()))
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
     * Send a passive broadcast in response to a request broadcast. Never send a request for a
     * request. We can respond to our own requests, since a request broadcast is not intended as a
     * connection request. We might want to only send messages, so we would send a request, but
     * never a passive broadcast.
     */
    protected void serviceRequestBroadcast()
    {
        // create this connection each time.
        // more robust
        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                getUdpDiscoveryAttributes().getUdpDiscoveryPort(),
                getUdpDiscoveryAttributes().getUdpTTL()))
        {
            sender.passiveBroadcast(
                    getUdpDiscoveryAttributes().getServiceAddress(),
                    getUdpDiscoveryAttributes().getServicePort(),
                    this.getCacheNames() );

            log.debug( "Called sender to issue a passive broadcast" );
        }
        catch ( final IOException e )
        {
            log.error( "Problem calling the UDP Discovery Sender, address [{0}] "
                    + "port [{1}]",
                    getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                    getUdpDiscoveryAttributes().getUdpDiscoveryPort(), e );
        }
    }

    /**
     * Issues a remove broadcast to the others.
     */
    protected void shutdownBroadcast()
    {
        // create this connection each time.
        // more robust
        try (UDPDiscoverySender sender = new UDPDiscoverySender(
                getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                getUdpDiscoveryAttributes().getUdpDiscoveryPort(),
                getUdpDiscoveryAttributes().getUdpTTL()))
        {
            sender.removeBroadcast(
                    getUdpDiscoveryAttributes().getServiceAddress(),
                    getUdpDiscoveryAttributes().getServicePort(),
                    this.getCacheNames() );

            log.debug( "Called sender to issue a remove broadcast in shutdown." );
        }
        catch ( final IOException e )
        {
            log.error( "Problem calling the UDP Discovery Sender", e );
        }
    }

    /**
     * Adds a region to the list that is participating in discovery.
     * <p>
     * @param cacheName
     */
    public void addParticipatingCacheName( final String cacheName )
    {
        cacheNames.add( cacheName );
    }

    /**
     * Removes the discovered service from the list and calls the discovery listener.
     * <p>
     * @param service
     */
    public void removeDiscoveredService( final DiscoveredService service )
    {
        final boolean contained = getDiscoveredServices().remove( service );

        if ( contained )
        {
            log.info( "Removing {0}", service );
        }

        getDiscoveryListeners().forEach(listener -> listener.removeDiscoveredService(service));
    }

    /**
     * Add a service to the list. Update the held copy if we already know about it.
     * <p>
     * @param discoveredService discovered service
     */
    protected void addOrUpdateService( final DiscoveredService discoveredService )
    {
        final Set<DiscoveredService> discoveredServices = getDiscoveredServices();
        // Since this is a set we can add it over an over.
        // We want to replace the old one, since we may add info that is not part of the equals.
        // The equals method on the object being added is intentionally restricted.
        if ( !discoveredServices.contains( discoveredService ) )
        {
            log.info( "Set does not contain service. I discovered {0}", discoveredService );
            log.debug( "Adding service in the set {0}", discoveredService );
            discoveredServices.add( discoveredService );
        }
        else
        {
            log.debug( "Set contains service." );
            log.debug( "Updating service in the set {0}", discoveredService );

            // Update the list of cache names if it has changed.
            // need to update the time this sucks. add has no effect convert to a map
            DiscoveredService theOldServiceInformation = discoveredServices.stream()
                .filter(service -> discoveredService.equals(service))
                .findFirst()
                .orElse(null);

            if (theOldServiceInformation != null &&
                !theOldServiceInformation.getCacheNames().equals(discoveredService.getCacheNames()))
            {
                log.info( "List of cache names changed for service: {0}",
                        discoveredService );
            }

            // replace it, we want to reset the payload and the last heard from time.
            discoveredServices.remove( discoveredService );
            discoveredServices.add( discoveredService );
        }

        // Always Notify the listeners
        // If we don't do this, then if a region using the default config is initialized after notification,
        // it will never get the service in it's no wait list.
        // Leave it to the listeners to decide what to do.
        getDiscoveryListeners().forEach(listener -> listener.addDiscoveredService( discoveredService));
    }

    /**
     * Get all the cache names we have facades for.
     * <p>
     * @return ArrayList
     */
    protected ArrayList<String> getCacheNames()
    {
        return new ArrayList<>(cacheNames);
    }

    /**
     * @param attr The UDPDiscoveryAttributes to set.
     */
    public void setUdpDiscoveryAttributes( final UDPDiscoveryAttributes attr )
    {
        this.udpDiscoveryAttributes = attr;
    }

    /**
     * @return Returns the lca.
     */
    public UDPDiscoveryAttributes getUdpDiscoveryAttributes()
    {
        return this.udpDiscoveryAttributes;
    }

    /**
     * Start necessary receiver thread
     */
    public void startup()
    {
        udpReceiverThread = new Thread(receiver);
        udpReceiverThread.setDaemon(true);
        // udpReceiverThread.setName( t.getName() + "--UDPReceiver" );
        udpReceiverThread.start();
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

            // no good way to do this right now.
            if (receiver != null)
            {
                log.info( "Shutting down UDP discovery service receiver." );
                receiver.shutdown();
                udpReceiverThread.interrupt();
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
     * @return Returns the discoveredServices.
     */
    public Set<DiscoveredService> getDiscoveredServices()
    {
        return discoveredServices;
    }

    /**
     * @return the discoveryListeners
     */
    private Set<IDiscoveryListener> getDiscoveryListeners()
    {
        return discoveryListeners;
    }

    /**
     * @return the discoveryListeners
     */
    public Set<IDiscoveryListener> getCopyOfDiscoveryListeners()
    {
        return new HashSet<>(getDiscoveryListeners());
    }

    /**
     * Adds a listener.
     * <p>
     * @param listener
     * @return true if it wasn't already in the set
     */
    public boolean addDiscoveryListener( final IDiscoveryListener listener )
    {
        return getDiscoveryListeners().add( listener );
    }

    /**
     * Removes a listener.
     * <p>
     * @param listener
     * @return true if it was in the set
     */
    public boolean removeDiscoveryListener( final IDiscoveryListener listener )
    {
        return getDiscoveryListeners().remove( listener );
    }
}
