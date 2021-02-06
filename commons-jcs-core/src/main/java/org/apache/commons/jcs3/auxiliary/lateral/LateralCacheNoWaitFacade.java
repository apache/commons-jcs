package org.apache.commons.jcs3.auxiliary.lateral;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Used to provide access to multiple services under nowait protection. Composite factory should
 * construct LateralCacheNoWaitFacade to give to the composite cache out of caches it constructs
 * from the varies manager to lateral services. Perhaps the lateralcache factory should be able to
 * do this.
 */
public class LateralCacheNoWaitFacade<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /** The logger */
    private static final Log log = LogManager.getLog( LateralCacheNoWaitFacade.class );

    /**
     * The queuing facade to the client.
     * @deprecated Should not have been public in the first place
     */
    @Deprecated
    public LateralCacheNoWait<K, V>[] noWaits;

    /**
     * The queuing facade to the client.
     */
    private final CopyOnWriteArraySet<LateralCacheNoWait<K, V>> noWaitSet;

    /** The region name */
    private final String cacheName;

    /** A cache listener */
    private ILateralCacheListener<K, V> listener;

    /** User configurable attributes. */
    private final ILateralCacheAttributes lateralCacheAttributes;

    /** Disposed state of this facade */
    private boolean disposed;

    /**
     * Constructs with the given lateral cache, and fires events to any listeners.
     * <p>
     * @param noWaits
     * @param cattr
     */
    public LateralCacheNoWaitFacade(final ILateralCacheListener<K, V> listener, final LateralCacheNoWait<K, V>[] noWaits, final ILateralCacheAttributes cattr )
    {
        log.debug( "CONSTRUCTING NO WAIT FACADE" );
        this.listener = listener;
        this.noWaits = noWaits;
        this.noWaitSet = new CopyOnWriteArraySet<>(Arrays.asList(noWaits));
        this.cacheName = cattr.getCacheName();
        this.lateralCacheAttributes = cattr;
    }

    /**
     * Return the size of the no wait list (for testing)
     *
     * @return the noWait list size.
     */
    protected int getNoWaitSize()
    {
        return noWaitSet.size();
    }

    /**
     * Tells you if the no wait is in the list or not.
     * <p>
     * @param noWait
     * @return true if the noWait is in the list.
     */
    public boolean containsNoWait( final LateralCacheNoWait<K, V> noWait )
    {
        return noWaitSet.contains(noWait);
    }

    /**
     * Adds a no wait to the list if it isn't already in the list.
     * <p>
     * @param noWait
     * @return true if it wasn't already contained
     */
    @SuppressWarnings("unchecked") // No generic arrays in Java
    public synchronized boolean addNoWait( final LateralCacheNoWait<K, V> noWait )
    {
        if ( noWait == null )
        {
            return false;
        }

        final boolean added = noWaitSet.add(noWait);

        if (!added)
        {
            log.debug( "No Wait already contained, [{0}]", noWait );
            return false;
        }

        noWaits = noWaitSet.toArray(new LateralCacheNoWait[0]);

        return true;
    }

    /**
     * Removes a no wait from the list if it is already there.
     * <p>
     * @param noWait
     * @return true if it was already in the array
     */
    @SuppressWarnings("unchecked") // No generic arrays in java
    public synchronized boolean removeNoWait( final LateralCacheNoWait<K, V> noWait )
    {
        if ( noWait == null )
        {
            return false;
        }

        final boolean contained = noWaitSet.remove(noWait);

        if (!contained)
        {
            return false;
        }

        noWaits = noWaitSet.toArray(new LateralCacheNoWait[0]);

        return true;
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
                () -> noWaitSet.size());

        for (final LateralCacheNoWait<K, V> nw : noWaitSet)
        {
            nw.update( ce );
        }
    }

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param key
     * @return ICacheElement
     */
    @Override
    public ICacheElement<K, V> get( final K key )
    {
        return noWaitSet.stream()
            .map(nw -> nw.get(key))
            .filter(obj -> obj != null)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        if (keys != null && !keys.isEmpty())
        {
            final Map<K, ICacheElement<K, V>> elements = keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> get(key))).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            Entry::getValue));

            return elements;
        }

        return new HashMap<>();
    }

    /**
     * Synchronously reads from the lateral cache. Get a response from each! This will be slow.
     * Merge them.
     * <p>
     * @param pattern
     * @return ICacheElement
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching(final String pattern)
    {
        return noWaitSet.stream()
                .flatMap(nw -> nw.getMatching(pattern).entrySet().stream())
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue));
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        final HashSet<K> allKeys = new HashSet<>();
        for (final LateralCacheNoWait<K, V> nw : noWaitSet)
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
     * Adds a remove request to the lateral cache.
     * <p>
     * @param key
     * @return always false.
     */
    @Override
    public boolean remove( final K key )
    {
        noWaitSet.forEach(nw -> nw.remove( key ));
        return false;
    }

    /**
     * Adds a removeAll request to the lateral cache.
     */
    @Override
    public void removeAll()
    {
        noWaitSet.forEach(LateralCacheNoWait::removeAll);
    }

    /** Adds a dispose request to the lateral cache. */
    @Override
    public void dispose()
    {
        try
        {
            if ( listener != null )
            {
                listener.dispose();
                listener = null;
            }

            noWaitSet.forEach(LateralCacheNoWait::dispose);
        }
        finally
        {
            disposed = true;
        }
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
     * Gets the cacheType attribute of the LateralCacheNoWaitFacade object.
     * <p>
     * @return The cacheType value
     */
    @Override
    public CacheType getCacheType()
    {
        return CacheType.LATERAL_CACHE;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWaitFacade object.
     * <p>
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return "";
        //cache.getCacheName();
    }

    /**
     * Gets the status attribute of the LateralCacheNoWaitFacade object
     * @return The status value
     */
    @Override
    public CacheStatus getStatus()
    {
        if (disposed)
        {
            return CacheStatus.DISPOSED;
        }

        if (noWaitSet.isEmpty() || listener != null)
        {
            return CacheStatus.ALIVE;
        }

        final List<CacheStatus> statii = noWaitSet.stream()
                .map(LateralCacheNoWait::getStatus)
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
     * @return Returns the AuxiliaryCacheAttributes.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return this.lateralCacheAttributes;
    }

    /**
     * @return "LateralCacheNoWaitFacade: " + cacheName;
     */
    @Override
    public String toString()
    {
        return "LateralCacheNoWaitFacade: " + cacheName;
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return "Lateral Cache No Wait";
    }

    /**
     * getStats
     * @return String
     */
    @Override
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Lateral Cache No Wait Facade" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        if (noWaitSet != null)
        {
            elems.add(new StatElement<>("Number of No Waits", Integer.valueOf(noWaitSet.size())));

            elems.addAll(noWaitSet.stream()
                    .flatMap(lcnw -> lcnw.getStatistics().getStatElements().stream())
                    .collect(Collectors.toList()));
        }

        stats.setStatElements( elems );

        return stats;
    }
}
