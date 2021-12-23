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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheNoWait;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.discovery.DiscoveredService;
import org.apache.commons.jcs3.utils.discovery.behavior.IDiscoveryListener;

/**
 * This knows how to add and remove discovered services. It observes UDP discovery events.
 * <p>
 * We can have one listener per region, or one shared by all regions.
 */
public class LateralTCPDiscoveryListener
    implements IDiscoveryListener
{
    /** The log factory */
    private static final Log log = LogManager.getLog( LateralTCPDiscoveryListener.class );

    /**
     * Map of no wait facades. these are used to determine which regions are locally configured to
     * use laterals.
     */
    private final ConcurrentMap<String, LateralCacheNoWaitFacade<?, ?>> facades =
        new ConcurrentHashMap<>();

    /**
     * List of regions that are configured differently here than on another server. We keep track of
     * this to limit the amount of info logging.
     */
    private final CopyOnWriteArrayList<String> knownDifferentlyConfiguredRegions =
        new CopyOnWriteArrayList<>();

    /** The name of the cache factory */
    private final String factoryName;

    /** Reference to the cache manager for auxiliary cache access */
    private final CompositeCacheManager cacheManager;

    /** Reference to the cache event logger for auxiliary cache creation */
    private final ICacheEventLogger cacheEventLogger;

    /** Reference to the cache element serializer for auxiliary cache creation */
    private final IElementSerializer elementSerializer;

    /**
     * This plugs into the udp discovery system. It will receive add and remove events.
     * <p>
     * @param factoryName the name of the related cache factory
     * @param cacheManager the global cache manager
     * @deprecated Use constructor with four parameters
     */
    @Deprecated
    protected LateralTCPDiscoveryListener( final String factoryName, final ICompositeCacheManager cacheManager )
    {
        this(factoryName, (CompositeCacheManager) cacheManager, null, null);
    }

    /**
     * This plugs into the udp discovery system. It will receive add and remove events.
     * <p>
     * @param factoryName the name of the related cache factory
     * @param cacheManager the global cache manager
     * @param cacheEventLogger Reference to the cache event logger for auxiliary cache creation
     * @param elementSerializer Reference to the cache element serializer for auxiliary cache
     * creation
     * @since 3.1
     */
    protected LateralTCPDiscoveryListener( final String factoryName,
            final CompositeCacheManager cacheManager,
            final ICacheEventLogger cacheEventLogger,
            final IElementSerializer elementSerializer)
    {
        this.factoryName = factoryName;
        this.cacheManager = cacheManager;
        this.cacheEventLogger = cacheEventLogger;
        this.elementSerializer = elementSerializer;
    }

    /**
     * Adds a nowait facade under this cachename. If one already existed, it will be overridden.
     * <p>
     * This adds nowaits to a facade for the region name. If the region has no facade, then it is
     * not configured to use the lateral cache, and no facade will be created.
     * <p>
     * @param cacheName - the region name
     * @param facade - facade (for region) =&gt; multiple lateral clients.
     * @return true if the facade was not already registered.
     */
    public boolean addNoWaitFacade( final String cacheName, final LateralCacheNoWaitFacade<?, ?> facade )
    {
        final boolean isNew = !containsNoWaitFacade( cacheName );

        // override or put anew, it doesn't matter
        facades.put( cacheName, facade );
        knownDifferentlyConfiguredRegions.remove( cacheName );

        return isNew;
    }

    /**
     * Allows us to see if the facade is present.
     * <p>
     * @param cacheName - facades are for a region
     * @return do we contain the no wait. true if so
     */
    public boolean containsNoWaitFacade( final String cacheName )
    {
        return facades.containsKey( cacheName );
    }

    /**
     * Allows us to see if the facade is present and if it has the no wait.
     * <p>
     * @param cacheName - facades are for a region
     * @param noWait - is this no wait in the facade
     * @return do we contain the no wait. true if so
     */
    public <K, V> boolean containsNoWait( final String cacheName, final LateralCacheNoWait<K, V> noWait )
    {
        @SuppressWarnings("unchecked") // Need to cast because of common map for all facades
        final
        LateralCacheNoWaitFacade<K, V> facade =
            (LateralCacheNoWaitFacade<K, V>)facades.get( noWait.getCacheName() );

        if ( facade == null )
        {
            return false;
        }

        return facade.containsNoWait( noWait );
    }

    /**
     * When a broadcast is received from the UDP Discovery receiver, for each cacheName in the
     * message, the add no wait will be called here. To add a no wait, the facade is looked up for
     * this cache name.
     * <p>
     * Each region has a facade. The facade contains a list of end points--the other tcp lateral
     * services.
     * <p>
     * @param noWait
     * @return true if we found the no wait and added it. False if the no wait was not present or if
     *         we already had it.
     */
    protected <K, V> boolean addNoWait( final LateralCacheNoWait<K, V> noWait )
    {
        @SuppressWarnings("unchecked") // Need to cast because of common map for all facades
        final
        LateralCacheNoWaitFacade<K, V> facade =
            (LateralCacheNoWaitFacade<K, V>)facades.get( noWait.getCacheName() );
        log.debug( "addNoWait > Got facade for {0} = {1}", noWait.getCacheName(), facade );

        return addNoWait(noWait, facade);
    }

    /**
     * When a broadcast is received from the UDP Discovery receiver, for each cacheName in the
     * message, the add no wait will be called here.
     * <p>
     * @param noWait the no wait
     * @param facade the related facade
     * @return true if we found the no wait and added it. False if the no wait was not present or if
     *         we already had it.
     * @since 3.1
     */
    protected <K, V> boolean addNoWait(final LateralCacheNoWait<K, V> noWait,
            final LateralCacheNoWaitFacade<K, V> facade)
    {
        if ( facade != null )
        {
            final boolean isNew = facade.addNoWait( noWait );
            log.debug( "Called addNoWait, isNew = {0}", isNew );
            return isNew;
        }
        if ( knownDifferentlyConfiguredRegions.addIfAbsent( noWait.getCacheName() ) )
        {
            log.info( "addNoWait > Different nodes are configured differently "
                    + "or region [{0}] is not yet used on this side.",
                    noWait::getCacheName);
        }
        return false;
    }

    /**
     * Look up the facade for the name. If it doesn't exist, then the region is not configured for
     * use with the lateral cache. If it is present, remove the item from the no wait list.
     * <p>
     * @param noWait
     * @return true if we found the no wait and removed it. False if the no wait was not present.
     */
    protected <K, V> boolean removeNoWait( final LateralCacheNoWait<K, V> noWait )
    {
        @SuppressWarnings("unchecked") // Need to cast because of common map for all facades
        final
        LateralCacheNoWaitFacade<K, V> facade =
            (LateralCacheNoWaitFacade<K, V>)facades.get( noWait.getCacheName() );
        log.debug( "removeNoWait > Got facade for {0} = {1}", noWait.getCacheName(), facade);

        return removeNoWait(facade, noWait.getCacheName(), noWait.getIdentityKey());
    }

    /**
     * Remove the item from the no wait list.
     * <p>
     * @param facade
     * @param cacheName
     * @param tcpServer
     * @return true if we found the no wait and removed it. False if the no wait was not present.
     * @since 3.1
     */
    protected <K, V> boolean removeNoWait(final LateralCacheNoWaitFacade<K, V> facade,
            final String cacheName, final String tcpServer)
    {
        if ( facade != null )
        {
            final boolean removed = facade.removeNoWait(tcpServer);
            log.debug( "Called removeNoWait, removed {0}", removed );
            return removed;
        }
        if (knownDifferentlyConfiguredRegions.addIfAbsent(cacheName))
        {
            log.info( "addNoWait > Different nodes are configured differently "
                    + "or region [{0}] is not yet used on this side.",
                    cacheName);
        }
        return false;
    }

    /**
     * Creates the lateral cache if needed.
     * <p>
     * We could go to the composite cache manager and get the the cache for the region. This would
     * force a full configuration of the region. One advantage of this would be that the creation of
     * the later would go through the factory, which would add the item to the no wait list. But we
     * don't want to do this. This would force this client to have all the regions as the other.
     * This might not be desired. We don't want to send or receive for a region here that is either
     * not used or not configured to use the lateral.
     * <p>
     * Right now, I'm afraid that the region will get puts if another instance has the region
     * configured to use the lateral and our address is configured. This might be a bug, but it
     * shouldn't happen with discovery.
     * <p>
     * @param service
     */
    @Override
    public void addDiscoveredService( final DiscoveredService service )
    {
        // get a cache and add it to the no waits
        // the add method should not add the same.
        // we need the listener port from the original config.
        final ArrayList<String> regions = service.getCacheNames();
        final String serverAndPort = service.getServiceAddress() + ":" + service.getServicePort();

        if ( regions != null )
        {
            // for each region get the cache
            for (final String cacheName : regions)
            {
                final LateralCacheNoWaitFacade<?, ?> facade = facades.get(cacheName);
                log.debug( "Got cache facade {0}", facade );

                // add this to the nowaits for this cachename
                if (facade != null)
                {
                    // skip caches already there
                    if (facade.containsNoWait(serverAndPort))
                    {
                        continue;
                    }

                    final ITCPLateralCacheAttributes lca =
                            (ITCPLateralCacheAttributes) facade.getAuxiliaryCacheAttributes().clone();
                    lca.setTcpServer(serverAndPort);

                    LateralTCPCacheFactory factory =
                            (LateralTCPCacheFactory) cacheManager.registryFacGet(factoryName);

                    LateralCacheNoWait<?, ?> noWait =
                            factory.createCacheNoWait(lca, cacheEventLogger, elementSerializer);
                    factory.monitorCache(noWait);

                    if (addNoWait(noWait))
                    {
                        log.debug("Added NoWait for cacheName [{0}] at {1}", cacheName, serverAndPort);
                    }
                }
            }
        }
        else
        {
            log.warn( "No cache names found in message {0}", service );
        }
    }

    /**
     * Removes the lateral cache.
     * <p>
     * We need to tell the manager that this instance is bad, so it will reconnect the sender if it
     * comes back.
     * <p>
     * @param service
     */
    @Override
    public void removeDiscoveredService( final DiscoveredService service )
    {
        // get a cache and add it to the no waits
        // the add method should not add the same.
        // we need the listener port from the original config.
        final ArrayList<String> regions = service.getCacheNames();
        final String serverAndPort = service.getServiceAddress() + ":" + service.getServicePort();

        if ( regions != null )
        {
            // for each region get the cache
            for (final String cacheName : regions)
            {
                final LateralCacheNoWaitFacade<?, ?> facade = facades.get(cacheName);
                log.debug( "Got cache facade {0}", facade );

                // remove this from the nowaits for this cachename
                if (facade != null && removeNoWait(facade, cacheName, serverAndPort))
                {
                    log.debug("Removed NoWait for cacheName [{0}] at {1}", cacheName, serverAndPort);
                }
            }
        }
        else
        {
            log.warn( "No cache names found in message {0}", service );
        }
    }
}
