package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCache;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheMonitor;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheNoWait;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheWatchRepairable;
import org.apache.commons.jcs3.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs3.engine.ZombieCacheWatch;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.behavior.IShutdownObserver;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryManager;
import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryService;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * Constructs a LateralCacheNoWaitFacade for the given configuration. Each lateral service / local
 * relationship is managed by one manager. This manager can have multiple caches. The remote
 * relationships are consolidated and restored via these managers.
 * <p>
 * The facade provides a front to the composite cache so the implementation is transparent.
 */
public class LateralTCPCacheFactory
    extends AbstractAuxiliaryCacheFactory
{
    /** The logger */
    private static final Log log = LogManager.getLog( LateralTCPCacheFactory.class );

    /** Address to service map. */
    private ConcurrentHashMap<String, ICacheServiceNonLocal<?, ?>> csnlInstances;

    /** Map of available discovery listener instances, keyed by port. */
    private ConcurrentHashMap<String, LateralTCPDiscoveryListener> lTCPDLInstances;

    /** Monitor thread */
    private LateralCacheMonitor monitor;

    /**
     * Wrapper of the lateral cache watch service; or wrapper of a zombie
     * service if failed to connect.
     */
    private CacheWatchRepairable lateralWatch;

    /**
     * Creates a TCP lateral.
     * <p>
     * @param <K> cache key type
     * @param <V> cache value type
     * @param iaca the cache configuration object
     * @param cacheMgr the cache manager
     * @param cacheEventLogger the event logger
     * @param elementSerializer the serializer to use when sending or receiving
     * @return a LateralCacheNoWaitFacade
     */
    @Override
    public <K, V> LateralCacheNoWaitFacade<K, V> createCache(
            final AuxiliaryCacheAttributes iaca, final ICompositeCacheManager cacheMgr,
           final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer )
    {
        final ITCPLateralCacheAttributes lac = (ITCPLateralCacheAttributes) iaca;
        final ArrayList<LateralCacheNoWait<K, V>> noWaits = new ArrayList<>();

        // pairs up the tcp servers and set the tcpServer value and
        // get the manager and then get the cache
        // no servers are required.
        if ( lac.getTcpServers() != null )
        {
            final String servers[] = lac.getTcpServers().split("\\s*,\\s*");
            log.debug( "Configured for [{0}] servers.", servers.length );

            for (final String server : servers)
            {
                log.debug( "tcp server = {0}", server );
                final ITCPLateralCacheAttributes lacClone = (ITCPLateralCacheAttributes) lac.clone();
                lacClone.setTcpServer( server );

                final LateralCacheNoWait<K, V> lateralNoWait = createCacheNoWait(lacClone, cacheEventLogger, elementSerializer);

                addListenerIfNeeded( lacClone, cacheMgr );
                monitorCache(lateralNoWait);
                noWaits.add( lateralNoWait );
            }
        }

        final ILateralCacheListener<K, V> listener = createListener( lac, cacheMgr );

        // create the no wait facade.
        final LateralCacheNoWaitFacade<K, V> lcnwf =
            new LateralCacheNoWaitFacade<>(listener, noWaits, lac);

        // create udp discovery if available.
        createDiscoveryService( lac, lcnwf, cacheMgr, cacheEventLogger, elementSerializer );

        return lcnwf;
    }

    /**
     * Create a LateralCacheNoWait for the server configured in lca
     *
     * @param <K> cache key type
     * @param <V> cache value type
     * @param lca the cache configuration object
     * @param cacheEventLogger the event logger
     * @param elementSerializer the serializer to use when sending or receiving
     * @return a LateralCacheNoWait
     */
    public <K, V> LateralCacheNoWait<K, V> createCacheNoWait( final ITCPLateralCacheAttributes lca,
            final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer )
    {
        final ICacheServiceNonLocal<K, V> lateralService = getCSNLInstance(lca, elementSerializer);

        final LateralCache<K, V> cache = new LateralCache<>( lca, lateralService, this.monitor );
        cache.setCacheEventLogger( cacheEventLogger );
        cache.setElementSerializer( elementSerializer );

        log.debug( "Created cache for noWait, cache [{0}]", cache );

        final LateralCacheNoWait<K, V> lateralNoWait = new LateralCacheNoWait<>( cache );
        lateralNoWait.setIdentityKey(lca.getTcpServer());

        log.info( "Created LateralCacheNoWait for [{0}] LateralCacheNoWait = [{1}]",
                lca, lateralNoWait );

        return lateralNoWait;
    }

    /**
     * Initialize this factory
     */
    @Override
    public void initialize()
    {
        this.csnlInstances = new ConcurrentHashMap<>();
        this.lTCPDLInstances = new ConcurrentHashMap<>();

        // Create the monitoring daemon thread
        this.monitor = new LateralCacheMonitor(this);
        this.monitor.setDaemon( true );
        this.monitor.start();

        this.lateralWatch = new CacheWatchRepairable();
        this.lateralWatch.setCacheWatch( new ZombieCacheWatch() );
    }

    /**
     * Dispose of this factory, clean up shared resources
     */
    @Override
    public void dispose()
    {
        for (final ICacheServiceNonLocal<?, ?> service : this.csnlInstances.values())
        {
            try
            {
                service.dispose("");
            }
            catch (final IOException e)
            {
                log.error("Could not dispose service " + service, e);
            }
        }

        this.csnlInstances.clear();

        // TODO: shut down discovery listeners
        this.lTCPDLInstances.clear();

        if (this.monitor != null)
        {
            this.monitor.notifyShutdown();
            try
            {
                this.monitor.join(5000);
            }
            catch (final InterruptedException e)
            {
                // swallow
            }
            this.monitor = null;
        }
    }

    /**
     * Returns an instance of the cache service.
     * <p>
     * @param <K> cache key type
     * @param <V> cache value type
     * @param lca configuration for the creation of a new service instance
     *
     * @return ICacheServiceNonLocal&lt;K, V&gt;
     *
     * @deprecated Specify serializer
     */
    @Deprecated
    public <K, V> ICacheServiceNonLocal<K, V> getCSNLInstance( final ITCPLateralCacheAttributes lca )
    {
        return getCSNLInstance(lca, new StandardSerializer());
    }

    /**
     * Returns an instance of the cache service.
     * <p>
     * @param <K> cache key type
     * @param <V> cache value type
     * @param lca configuration for the creation of a new service instance
     * @param elementSerializer the serializer to use when sending or receiving
     *
     * @return ICacheServiceNonLocal&lt;K, V&gt;
     * @since 3.1
     */
    // Need to cast because of common map for all cache services
    @SuppressWarnings("unchecked")
    public <K, V> ICacheServiceNonLocal<K, V> getCSNLInstance(final ITCPLateralCacheAttributes lca,
            final IElementSerializer elementSerializer)
    {
        final String key = lca.getTcpServer();

        return (ICacheServiceNonLocal<K, V>) csnlInstances.compute(key, (name, service) -> {

            ICacheServiceNonLocal<?, ?> newService = service;

            // If service creation did not succeed last time, force retry
            if (service instanceof ZombieCacheServiceNonLocal)
            {
                log.info("Disposing of zombie service instance for [{0}]", name);
                newService = null;
            }

            if (newService == null)
            {
                log.info( "Instance for [{0}] is null, creating", name );

                // Create the service
                try
                {
                    log.info( "Creating TCP service, lca = {0}", lca );

                    newService = new LateralTCPService<>(lca, elementSerializer);
                }
                catch ( final IOException ex )
                {
                    // Failed to connect to the lateral server.
                    // Configure this LateralCacheManager instance to use the
                    // "zombie" services.
                    log.error( "Failure, lateral instance will use zombie service", ex );

                    newService = new ZombieCacheServiceNonLocal<>(lca.getZombieQueueMaxSize());

                    // Notify the cache monitor about the error, and kick off
                    // the recovery process.
                    monitor.notifyError();
                }
            }

            return newService;
        });
    }

    /**
     * Add cache instance to monitor
     *
     * @param cache the cache instance
     * @since 3.1
     */
    public void monitorCache(final LateralCacheNoWait<?, ?> cache)
    {
        monitor.addCache(cache);
    }

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class.
     * <p>
     * @param ilca ITCPLateralCacheAttributes
     * @param cacheManager a reference to the global cache manager
     * @param cacheEventLogger Reference to the cache event logger for auxiliary cache creation
     * @param elementSerializer Reference to the cache element serializer for auxiliary cache
     *
     * @return The instance value
     */
    private LateralTCPDiscoveryListener getDiscoveryListener(final ITCPLateralCacheAttributes ilca,
            final ICompositeCacheManager cacheManager, final ICacheEventLogger cacheEventLogger,
            final IElementSerializer elementSerializer)
    {
        final String key = ilca.getUdpDiscoveryAddr() + ":" + ilca.getUdpDiscoveryPort();

        return lTCPDLInstances.computeIfAbsent(key, key1 -> {
            log.info("Created new discovery listener for cacheName {0} and request {1}",
                    ilca.getCacheName(), key1);
            return new LateralTCPDiscoveryListener( this.getName(),
                    (CompositeCacheManager) cacheManager,
                    cacheEventLogger, elementSerializer);
        });
    }

    /**
     * Add listener for receivers
     * <p>
     * @param iaca cache configuration attributes
     * @param cacheMgr the composite cache manager
     */
    private void addListenerIfNeeded( final ITCPLateralCacheAttributes iaca, final ICompositeCacheManager cacheMgr )
    {
        // don't create a listener if we are not receiving.
        if ( iaca.isReceive() )
        {
            try
            {
                addLateralCacheListener(iaca.getCacheName(), createListener(iaca, cacheMgr));
            }
            catch ( final IOException ioe )
            {
                log.error("Problem creating lateral listener", ioe);
            }
        }
        else
        {
            log.debug( "Not creating a listener since we are not receiving." );
        }
    }

    /**
     * Adds the lateral cache listener to the underlying cache-watch service.
     * <p>
     * @param cacheName The feature to be added to the LateralCacheListener attribute
     * @param listener The feature to be added to the LateralCacheListener attribute
     * @throws IOException
     */
    private <K, V> void addLateralCacheListener( final String cacheName, final ILateralCacheListener<K, V> listener )
        throws IOException
    {
        synchronized ( this.lateralWatch )
        {
            lateralWatch.addCacheListener( cacheName, listener );
        }
    }

    /**
     * Makes sure a listener gets created. It will get monitored as soon as it
     * is used.
     * <p>
     * This should be called by create cache.
     * <p>
     * @param attr  ITCPLateralCacheAttributes
     * @param cacheMgr the composite cache manager
     *
     * @return the listener if created, else null
     */
    private static <K, V> ILateralCacheListener<K, V> createListener( final ITCPLateralCacheAttributes attr,
            final ICompositeCacheManager cacheMgr )
    {
        ILateralCacheListener<K, V> listener = null;

        // don't create a listener if we are not receiving.
        if ( attr.isReceive() )
        {
            log.info( "Getting listener for {0}", attr );

            // make a listener. if one doesn't exist
            listener = LateralTCPListener.getInstance( attr, cacheMgr );

            // register for shutdown notification
            cacheMgr.registerShutdownObserver( (IShutdownObserver) listener );
        }
        else
        {
            log.debug( "Not creating a listener since we are not receiving." );
        }

        return listener;
    }

    /**
     * Creates the discovery service. Only creates this for tcp laterals right now.
     * <p>
     * @param lac ITCPLateralCacheAttributes
     * @param lcnwf the lateral facade
     * @param cacheMgr a reference to the global cache manager
     * @param cacheEventLogger Reference to the cache event logger for auxiliary cache creation
     * @param elementSerializer Reference to the cache element serializer for auxiliary cache
     */
    private synchronized <K, V> void createDiscoveryService(
            final ITCPLateralCacheAttributes lac,
            final LateralCacheNoWaitFacade<K, V> lcnwf,
            final ICompositeCacheManager cacheMgr,
            final ICacheEventLogger cacheEventLogger,
            final IElementSerializer elementSerializer )
    {
        UDPDiscoveryService discovery = null;

        // create the UDP discovery for the TCP lateral
        if ( lac.isUdpDiscoveryEnabled() )
        {
            // One can be used for all regions
            final LateralTCPDiscoveryListener discoveryListener =
                    getDiscoveryListener(lac, cacheMgr, cacheEventLogger, elementSerializer);
            discoveryListener.addNoWaitFacade( lac.getCacheName(), lcnwf );

            // need a factory for this so it doesn't
            // get dereferenced, also we don't want one for every region.
            discovery = UDPDiscoveryManager.getInstance().getService(
                    lac.getUdpDiscoveryAddr(), lac.getUdpDiscoveryPort(),
                    lac.getTcpListenerHost(), lac.getTcpListenerPort(), lac.getUdpTTL(),
                    cacheMgr, elementSerializer);

            discovery.addParticipatingCacheName( lac.getCacheName() );
            discovery.addDiscoveryListener( discoveryListener );

            log.info( "Registered TCP lateral cache [{0}] with UDPDiscoveryService.",
                    lac::getCacheName);
        }
    }
}
