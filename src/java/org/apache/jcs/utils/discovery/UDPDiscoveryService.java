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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.IShutdownObserver;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.discovery.behavior.IDiscoveryListener;
import org.apache.jcs.utils.net.HostNameUtil;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArraySet;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

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
    implements IShutdownObserver
{
    /** The logger */
    private final static Log log = LogFactory.getLog( UDPDiscoveryService.class );

    /** The background broadcaster. */
    private static ClockDaemon senderDaemon;

    /** thread that listens for messages */
    private Thread udpReceiverThread;

    /** the runanble that the receiver thread runs */
    private UDPDiscoveryReceiver receiver;

    /** the runnanble that sends messages via the clock daemon */
    private UDPDiscoverySenderThread sender = null;

    /** removes things that have been idle for too long */
    private UDPCleanupRunner cleanup;

    /** attributes */
    private UDPDiscoveryAttributes udpDiscoveryAttributes = null;

    /** is this shut down? */
    private boolean shutdown = false;

    /** This is a set of services that have been discovered. */
    private Set discoveredServices = new CopyOnWriteArraySet();

    /** This a list of regions that are configured to use discovery. */
    private Set cacheNames = new CopyOnWriteArraySet();

    /** handles add and remove. consider making into a set. */
    private IDiscoveryListener discoveryListener;

    /**
     * @param attributes
     * @param cacheEventLogger
     */
    public UDPDiscoveryService( UDPDiscoveryAttributes attributes, ICacheEventLogger cacheEventLogger )
    {
        udpDiscoveryAttributes = (UDPDiscoveryAttributes) attributes.clone();

        try
        {
            // todo, you should be able to set this
            udpDiscoveryAttributes.setServiceAddress( HostNameUtil.getLocalHostAddress() );
        }
        catch ( UnknownHostException e1 )
        {
            log.error( "Couldn't get localhost address", e1 );
        }

        try
        {
            // todo need some kind of recovery here.
            receiver = new UDPDiscoveryReceiver( this, getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                                                 getUdpDiscoveryAttributes().getUdpDiscoveryPort() );
            udpReceiverThread = new Thread( receiver );
            udpReceiverThread.setDaemon( true );
            // udpReceiverThread.setName( t.getName() + "--UDPReceiver" );
            udpReceiverThread.start();
        }
        catch ( Exception e )
        {
            log.error( "Problem creating UDPDiscoveryReceiver, address ["
                + getUdpDiscoveryAttributes().getUdpDiscoveryAddr() + "] port ["
                + getUdpDiscoveryAttributes().getUdpDiscoveryPort() + "] we won't be able to find any other caches", e );
        }

        // todo only do the passive if receive is enabled, perhaps set the
        // myhost to null or something on the request
        if ( senderDaemon == null )
        {
            senderDaemon = new ClockDaemon();
            senderDaemon.setThreadFactory( new MyThreadFactory() );
        }

        // create a sender thread
        sender = new UDPDiscoverySenderThread( getUdpDiscoveryAttributes(), this.getCacheNames() );

        senderDaemon.executePeriodically( 30 * 1000, sender, false );

        // add the cleanup daemon too
        cleanup = new UDPCleanupRunner( this );
        // I'm going to use this as both, but it could happen
        // that something could hang around twice the time suing this as the
        // delay and the idle time.
        senderDaemon.executePeriodically( this.getUdpDiscoveryAttributes().getMaxIdleTimeSec() * 1000, cleanup, false );

        // add shutdown hook that will issue a remove call.
        DiscoveryShutdownHook shutdownHook = new DiscoveryShutdownHook( this );
        Runtime.getRuntime().addShutdownHook( shutdownHook );
    }

    /**
     * Send a passive broadcast in response to a request broadcast. Never send a request for a
     * request. We can respond to our own requests, since a request broadcast is not intended as a
     * connection request. We might want to only send messages, so we would send a request, but
     * never a passive broadcast.
     */
    protected void serviceRequestBroadcast()
    {
        UDPDiscoverySender sender = null;
        try
        {
            // create this connection each time.
            // more robust
            sender = new UDPDiscoverySender( getUdpDiscoveryAttributes().getUdpDiscoveryAddr(),
                                             getUdpDiscoveryAttributes().getUdpDiscoveryPort() );

            sender.passiveBroadcast( getUdpDiscoveryAttributes().getServiceAddress(), getUdpDiscoveryAttributes()
                .getServicePort(), this.getCacheNames() );

            // todo we should consider sending a request broadcast every so
            // often.

            if ( log.isDebugEnabled() )
            {
                log.debug( "Called sender to issue a passive broadcast" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem calling the UDP Discovery Sender. address ["
                + getUdpDiscoveryAttributes().getUdpDiscoveryAddr() + "] port ["
                + getUdpDiscoveryAttributes().getUdpDiscoveryPort() + "]", e );
        }
        finally
        {
            try
            {
                if ( sender != null )
                {
                    sender.destroy();
                }
            }
            catch ( Exception e )
            {
                log.error( "Problem closing Passive Broadcast sender, while servicing a request broadcast.", e );
            }
        }
    }

    /**
     * Adds a region to the list that is participating in discovery.
     * <p>
     * @param cacheName
     */
    public void addParticipatingCacheName( String cacheName )
    {
        cacheNames.add( cacheName );
        sender.setCacheNames( getCacheNames() );
    }

    /**
     * Removes the discovered service from the list and calls the discovery listener.
     * <p>
     * @param service
     */
    public void removeDiscoveredService( DiscoveredService service )
    {
        getDiscoveredServices().remove( service );
        getDiscoveryListener().removeDiscoveredService( service );
    }

    /**
     * Add a service to the list. Update the held copy if we already know about it.
     * <p>
     * @param discoveredService discovered service
     */
    protected void addOrUpdateService( DiscoveredService discoveredService )
    {
        // Since this is a set we can add it over an over.
        // We want to replace the old one, since we may add info that is not part of the equals.
        // The equals method on the object being added is intentionally restricted.
        if ( !getDiscoveredServices().contains( discoveredService ) )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Set does not contain service. I discovered " + discoveredService );
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "Adding service in the set " + discoveredService );
            }
            getDiscoveredServices().add( discoveredService );

            // todo update some list of cachenames
            getDiscoveryListener().addDiscoveredService( discoveredService );
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Set contains service." );
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "Updating service in the set " + discoveredService );
            }
            Iterator it = getDiscoveredServices().iterator();
            // need to update the time this sucks. add has no effect convert to a map
            while ( it.hasNext() )
            {
                DiscoveredService service1 = (DiscoveredService) it.next();
                if ( discoveredService.equals( service1 ) )
                {
                    service1.setLastHearFromTime( discoveredService.getLastHearFromTime() );
                    break;
                }
            }
        }
    }

    /**
     * Get all the cache names we have facades for.
     * <p>
     * @return ArrayList
     */
    protected ArrayList getCacheNames()
    {
        ArrayList names = new ArrayList();
        names.addAll( cacheNames );
        return names;
    }

    /**
     * @param attr The UDPDiscoveryAttributes to set.
     */
    public void setUdpDiscoveryAttributes( UDPDiscoveryAttributes attr )
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
     * Allows us to set the daemon status on the clockdaemon
     * <p>
     * @author aaronsm
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
            t.setName( "JCS-UDPDiscoveryService-" + oldName );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }

    /**
     * Shuts down the receiver.
     */
    public void shutdown()
    {
        if ( !shutdown )
        {
            shutdown = true;

            if ( log.isInfoEnabled() )
            {
                log.info( "Shutting down UDP discovery service receiver." );
            }

            try
            {
                // no good way to do this right now.
                receiver.shutdown();
                udpReceiverThread.interrupt();
            }
            catch ( Exception e )
            {
                log.error( "Problem interrupting UDP receiver thread." );
            }

            if ( log.isInfoEnabled() )
            {
                log.info( "Shutting down UDP discovery service sender." );
            }

            try
            {
                // interrupt all the threads.
                senderDaemon.shutDown();
            }
            catch ( Exception e )
            {
                log.error( "Problem shutting down UDP sender." );
            }

            // also call the shutdown on the sender thread itself, which
            // will result in a remove command.
            try
            {
                sender.shutdown();
            }
            catch ( Exception e )
            {
                log.error( "Problem issuing remove broadcast via UDP sender." );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Shutdown already called." );
            }
        }
    }

    /**
     * Call shutdown to be safe.
     * <p>
     * @throws Throwable on error
     */
    public void finalize()
        throws Throwable
    {
        super.finalize();

        // TODO reconsider this, since it uses the logger
        shutdown();
    }

    /**
     * @param discoveredServices The discoveredServices to set.
     */
    public synchronized void setDiscoveredServices( Set discoveredServices )
    {
        this.discoveredServices = discoveredServices;
    }

    /**
     * @return Returns the discoveredServices.
     */
    public synchronized Set getDiscoveredServices()
    {
        return discoveredServices;
    }

    /**
     * @param discoveryListener the discoveryListener to set
     */
    public void setDiscoveryListener( IDiscoveryListener discoveryListener )
    {
        this.discoveryListener = discoveryListener;
    }

    /**
     * @return the discoveryListener
     */
    public IDiscoveryListener getDiscoveryListener()
    {
        return discoveryListener;
    }
}
