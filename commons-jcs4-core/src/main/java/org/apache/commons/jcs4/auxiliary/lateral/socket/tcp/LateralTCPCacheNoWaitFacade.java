package org.apache.commons.jcs4.auxiliary.lateral.socket.tcp;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.jcs4.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior.ILateralTCPCacheAttributes;
import org.apache.commons.jcs4.engine.CacheStatus;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.stats.Stats;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;
import org.apache.commons.jcs4.log.Log;

/**
 * Used to provide access to multiple services under nowait protection. Composite factory should
 * construct LateralTCPCacheNoWaitFacade to give to the composite cache out of caches it constructs
 * from the varies manager to lateral services. Perhaps the lateralcache factory should be able to
 * do this.
 */
public class LateralTCPCacheNoWaitFacade<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /** The logger */
    private static final Log log = Log.getLog( LateralTCPCacheNoWaitFacade.class );

    /**
     * The queuing facade to the client.
     */
    private final ConcurrentHashMap<String, LateralTCPCacheNoWait<K, V>> noWaitMap;

    /** The region name */
    private final String cacheName;

    /** A cache listener */
    private ILateralCacheListener<K, V> listener;

    /** User configurable attributes. */
    private final ILateralTCPCacheAttributes lateralCacheAttributes;

    /** Disposed state of this facade */
    private final AtomicBoolean disposed = new AtomicBoolean();

    /**
     * Constructs with the given lateral cache, and fires events to any listeners.
     *
     * @param listener the cache listener
     * @param noWaits the list of noWaits
     * @param cattr the configuration
     * @since 3.1
     */
    public LateralTCPCacheNoWaitFacade(final ILateralCacheListener<K, V> listener,
            final List<LateralTCPCacheNoWait<K, V>> noWaits, final ILateralTCPCacheAttributes cattr )
    {
        log.debug( "CONSTRUCTING NO WAIT FACADE" );
        this.listener = listener;
        this.noWaitMap = new ConcurrentHashMap<>();
        noWaits.forEach(noWait -> noWaitMap.put(noWait.getIdentityKey(), noWait));
        this.cacheName = cattr.getCacheName();
        this.lateralCacheAttributes = cattr;
    }

    /**
     * Adds a no wait to the list if it isn't already in the list.
     *
     * @param noWait
     * @return true if it wasn't already contained
     */
    public boolean addNoWait( final LateralTCPCacheNoWait<K, V> noWait )
    {
        if ( noWait == null )
        {
            return false;
        }

        final LateralTCPCacheNoWait<K,V> added =
                noWaitMap.putIfAbsent(noWait.getIdentityKey(), noWait);

        if (added != null)
        {
            log.debug( "No Wait already contained, [{0}]", noWait );
            return false;
        }

        return true;
    }

    /**
     * Tells you if the no wait is in the list or not.
     *
     * @param noWait
     * @return true if the noWait is in the list.
     */
    public boolean containsNoWait( final LateralTCPCacheNoWait<K, V> noWait )
    {
        return containsNoWait(noWait.getIdentityKey());
    }

    /**
     * Tells you if the no wait is in the list or not by checking for its
     * identifying key
     *
     * @param tcpServer the identifying key
     * @return true if the noWait is in the list.
     * @since 3.1
     */
    public boolean containsNoWait(final String tcpServer)
    {
        return noWaitMap.containsKey(tcpServer);
    }

    /** Adds a dispose request to the lateral cache. */
    @Override
    public void dispose()
    {
        if (disposed.compareAndSet(false, true))
        {
            if ( listener != null )
            {
                listener.dispose();
                listener = null;
            }

            noWaitMap.values().forEach(LateralTCPCacheNoWait::dispose);
            noWaitMap.clear();
        }
    }

    /**
     * Synchronously reads from the lateral cache.
     *
     * @param key
     * @return ICacheElement
     */
    @Override
    public ICacheElement<K, V> get( final K key )
    {
        return noWaitMap.values().stream()
            .map(nw -> nw.get(key))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * @return the AuxiliaryCacheAttributes.
     */
    @Override
    public ILateralTCPCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.lateralCacheAttributes;
    }

    /**
     * Gets the cacheName attribute of the LateralTCPCacheNoWaitFacade object.
     *
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Gets the cacheType attribute of the LateralTCPCacheNoWaitFacade object.
     *
     * @return The cacheType value
     */
    @Override
    public CacheType getCacheType()
    {
        return CacheType.LATERAL_CACHE;
    }

    /**
     * Return the keys in this cache.
     *
     * @see org.apache.commons.jcs4.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        final HashSet<K> allKeys = new HashSet<>();
        for (final LateralTCPCacheNoWait<K, V> nw : noWaitMap.values())
        {
            final Set<K> keys = nw.getKeySet();
            if (keys != null)
            {
                allKeys.addAll(keys);
            }
        }
        return allKeys;
    }

    /**
     * Synchronously reads from the lateral cache. Get a response from each! This will be slow.
     * Merge them.
     *
     * @param pattern
     * @return ICacheElement
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching(final String pattern)
    {
        return noWaitMap.values().stream()
                .flatMap(nw -> nw.getMatching(pattern).entrySet().stream())
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue));
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        if (keys != null && !keys.isEmpty())
        {
            return keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        this::get)).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            Entry::getValue));
        }

        return new HashMap<>();
    }

    /**
     * Return the size of the no wait list (for testing)
     *
     * @return the noWait list size.
     * @since 3.1
     */
    protected int getNoWaitSize()
    {
        return noWaitMap.size();
    }

    /**
     * No lateral invocation.
     * @return The size value
     */
    @Override
    public int getSize()
    {
        return 0;
        //cache.getSize();
    }

    /**
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats("Lateral Cache No Wait Facade");

        if (noWaitMap != null)
        {
            stats.addStatElement("Number of No Waits", Integer.valueOf(noWaitMap.size()));

            stats.addStatElements(noWaitMap.values().stream()
                    .flatMap(lcnw -> lcnw.getStatistics().getStatElements().stream())
                    .collect(Collectors.toList()));
        }

        return stats;
    }

    /**
     * Gets the status attribute of the LateralTCPCacheNoWaitFacade object
     * @return The status value
     */
    @Override
    public CacheStatus getStatus()
    {
        if (disposed.get())
        {
            return CacheStatus.DISPOSED;
        }

        if (noWaitMap.isEmpty() || listener != null)
        {
            return CacheStatus.ALIVE;
        }

        final List<CacheStatus> statii = noWaitMap.values().stream()
                .map(LateralTCPCacheNoWait::getStatus)
                .collect(Collectors.toList());

        // It's alive if ANY of its nowaits is alive
        if (statii.contains(CacheStatus.ALIVE))
        {
            return CacheStatus.ALIVE;
        }
        // It's alive if ANY of its nowaits is in error, but
        // none are alive, then it's in error
        if (statii.contains(CacheStatus.ERROR))
        {
            return CacheStatus.ERROR;
        }

        // Otherwise, it's been disposed, since it's the only status left
        return CacheStatus.DISPOSED;
    }

    /**
     * Adds a remove request to the lateral cache.
     *
     * @param key
     * @return always false.
     */
    @Override
    public boolean remove( final K key )
    {
        noWaitMap.values().forEach(nw -> nw.remove( key ));
        return false;
    }

    /**
     * Adds a removeAll request to the lateral cache.
     */
    @Override
    public void removeAll()
    {
        noWaitMap.values().forEach(LateralTCPCacheNoWait::removeAll);
    }

    /**
     * Removes a no wait from the list if it is already there.
     *
     * @param noWait
     * @return true if it was already in the array
     */
    public boolean removeNoWait( final LateralTCPCacheNoWait<K, V> noWait )
    {
        if (noWait == null)
        {
            return false;
        }

        return removeNoWait(noWait.getIdentityKey());
    }

    /**
     * Removes a no wait from the list if it is already there by its
     * identifying key
     *
     * @param tcpServer the identifying key.
     * @return true if it was already in the array
     * @since 3.1
     */
    public boolean removeNoWait(final String tcpServer)
    {
        if (tcpServer == null)
        {
            return false;
        }

        final LateralTCPCacheNoWait<K,V> contained = noWaitMap.remove(tcpServer);

        if (contained != null)
        {
            contained.dispose();
        }

        return contained != null;
    }

    /**
     * @return "LateralTCPCacheNoWaitFacade: " + cacheName;
     */
    @Override
    public String toString()
    {
        return "LateralTCPCacheNoWaitFacade: " + cacheName;
    }

    /**
     * Update the cache element in all lateral caches
     * @param ce the cache element
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
        throws IOException
    {
        log.debug("updating through lateral cache facade, noWaits.length = {0}",
                noWaitMap::size);

        for (final LateralTCPCacheNoWait<K, V> nw : noWaitMap.values())
        {
            nw.update( ce );
        }
    }
}
