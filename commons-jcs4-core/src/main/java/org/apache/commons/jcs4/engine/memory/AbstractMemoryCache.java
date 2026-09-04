package org.apache.commons.jcs4.engine.memory;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.jcs4.engine.behavior.ICache;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.group.GroupAttrName;
import org.apache.commons.jcs4.engine.control.group.GroupId;
import org.apache.commons.jcs4.engine.memory.behavior.IMemoryCache;
import org.apache.commons.jcs4.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs4.engine.stats.Stats;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;
import org.apache.commons.jcs4.log.Log;

/**
 * This base includes some common code for memory caches.
 */
public abstract class AbstractMemoryCache<K, V>
    implements IMemoryCache<K, V>
{
    /** Log instance */
    private static final Log log = Log.getLog( AbstractMemoryCache.class );

    /** The cache implementation name */
    protected static String cacheImplementationName = "Abstract Memory Cache";

    /** Cache Attributes.  Regions settings. */
    private ICompositeCacheAttributes cacheAttributes;

    /** The cache region spool method */
    private Consumer<ICacheElement<K,V>> waterfall;

    /** The cache region name this store is associated with */
    private String cacheName;

    /** The lock */
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    /** Map where items are stored by key.  This is created by the concrete child class. */
    private Map<K, MemoryElementDescriptor<K, V>> map;

    /** Number of hits */
    private AtomicLong hitCnt;

    /** Number of misses */
    private AtomicLong missCnt;

    /** Number of puts */
    private AtomicLong putCnt;

    /**
     * Children must implement this method. A FIFO implementation may use a tree map. An LRU might
     * use a hashtable.
     *
     * @return A Map
     */
    protected abstract Map<K, MemoryElementDescriptor<K, V>> createMap();

    /**
     * Get a read-only map view
     * @return a read-only map view
     */
    protected Map<K, MemoryElementDescriptor<K, V>> getMapView()
    {
        return Collections.unmodifiableMap(map);
    }

    /**
     * This instructs the memory cache to remove the <em>numberToFree</em> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <em>numberToFree</em> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     *
     * @param numberToFree
     * @return The number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     */
    @Override
    public int freeElements(final int numberToFree) throws IOException
    {
        int freed = 0;

        lock.writeLock().lock();
        try
        {
            freed = lockedFreeElements(numberToFree);
        }
        finally
        {
            lock.writeLock().unlock();
        }

        return freed;
    }

    /**
     * Prepares for shutdown. Reset statistics
     *
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        removeAll();
        hitCnt.set(0);
        missCnt.set(0);
        putCnt.set(0);
        log.info( "Memory Cache dispose called." );
    }
    /**
     * Dump the cache map for debugging.
     */
    protected void dumpMap()
    {
        if (log.isTraceEnabled())
        {
            log.trace("dumpingMap");
            map.forEach((key, value) ->
                log.trace("dumpMap> key={0}, val={1}", key, value.getCacheElement().value()));
        }
    }

    /**
     * Gets an item from the cache.
     *
     * @param key Identifies item to find
     * @return ICacheElement&lt;K, V&gt; if found, else null
     */
    @Override
    public ICacheElement<K, V> get(final K key)
    {
        ICacheElement<K, V> ce = null;

        log.debug("{0}: getting item for key {1}", this::getCacheName,
                () -> key);

        lock.writeLock().lock();
        try
        {
            final MemoryElementDescriptor<K, V> me = map.get(key);

            if (me != null)
            {
                hitCnt.incrementAndGet();
                lockedGetElement(me);
                ce = me.getCacheElement();

                log.debug("{0}: MemoryCache hit for {1}", this::getCacheName,
                        () -> key);
            }
            else
            {
                missCnt.incrementAndGet();

                log.debug("{0}: MemoryCache miss for {1}", this::getCacheName,
                        () -> key);
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }

        return ce;
    }

    /**
     * Returns the CacheAttributes.
     *
     * @return The CacheAttributes value
     */
    @Override
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttributes;
    }

    /**
     * Returns the cache (aka "region") name.
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Gets a set of the keys for all elements in the memory cache
     *
     * @return A set of keys
     */
    @Override
    public Set<K> getKeySet()
    {
        lock.readLock().lock();
        try
        {
            return Collections.unmodifiableSet(map.keySet());
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return A map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        if (keys != null)
        {
            return keys.stream()
                .map(key -> get(key))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ICacheElement::key,
                        element -> element));
        }

        return new HashMap<>();
    }

    /**
     * Gets an item from the cache without affecting its last access time or position. Not all memory
     * cache implementations can get quietly.
     *
     * @param key Identifies item to find
     * @return Element matching key if found, or null
     */
    @Override
    public ICacheElement<K, V> getQuiet( final K key )
    {
        ICacheElement<K, V> ce = null;

        final MemoryElementDescriptor<K, V> me = map.get( key );
        if ( me != null )
        {
            log.debug( "{0}: MemoryCache quiet hit for {1}",
                    this::getCacheName, () -> key );

            ce = me.getCacheElement();
        }
        else
        {
            log.debug( "{0}: MemoryCache quiet miss for {1}",
                    this::getCacheName, () -> key );
        }

        return ce;
    }

    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    @Override
    public int getSize()
    {
        lock.readLock().lock();
        try
        {
            return this.map.size();
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * @return statistics about the cache
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats(cacheImplementationName);

        stats.addStatElement("Put Count", putCnt);
        stats.addStatElement("Hit Count", hitCnt);
        stats.addStatElement("Miss Count", missCnt);
        stats.addStatElement("Map Size", Integer.valueOf(getSize()));

        return stats;
    }

    /**
     * For post reflection creation initialization
     *
     * @param hub
     */
    @Override
    public void initialize( final CompositeCache<K, V> hub )
    {
        hitCnt = new AtomicLong();
        missCnt = new AtomicLong();
        putCnt = new AtomicLong();

        this.cacheAttributes = hub.getCacheAttributes();
        final String attributeCacheName = this.cacheAttributes.cacheName();
        this.cacheName = attributeCacheName == null ? hub.getCacheName() : attributeCacheName;
        this.waterfall = ce -> hub.spoolToDisk(ce);
        this.map = createMap();

        log.info("initialized {0} for {1}", cacheImplementationName, cacheName);
    }

    /**
     * Wrap the cache element into an appropriate memory element descriptor
     *
     * @param ce The cache element
     * @return The memory element descriptor
     */
    protected abstract MemoryElementDescriptor<K, V> wrap(ICacheElement<K, V> ce);

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    protected abstract void lockedGetElement(MemoryElementDescriptor<K, V> me);

    /**
     * Update control structures after update
     * (guarded by the lock)
     *
     * @param newNode The memory element descriptor of the current cache element
     * @param oldNode The memory element descriptor of the previous cache element
     * @throws IOException if spooling operation fails
     */
    protected abstract void lockedUpdateElement(MemoryElementDescriptor<K, V> newNode,
            MemoryElementDescriptor<K, V> oldNode) throws IOException;

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    protected abstract void lockedRemoveAll();

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    protected abstract void lockedRemoveElement(MemoryElementDescriptor<K, V> me);

    /**
     * This instructs the memory cache to remove the <em>numberToFree</em> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <em>numberToFree</em> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     * (guarded by the lock)
     *
     * @param numberToFree
     * @return The number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     */
    protected abstract int lockedFreeElements(final int numberToFree) throws IOException;

    /**
     * Removes an item from the cache. This method handles hierarchical removal. If the key is a
     * String and ends with the CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     * starting with the argument String will be removed.
     * <p>
     *
     * @param key
     * @return true if the removal was successful
     */
    @Override
    public boolean remove(final K key)
    {
        log.debug("removing item for key: {0}", key);

        boolean removed = false;

        // handle partial removal
        if (key instanceof String s && s.endsWith(ICache.NAME_COMPONENT_DELIMITER))
        {
            removed = removeByHierarchy(s);
        }
        else if (key instanceof GroupAttrName gan && gan.attrName() == null)
        {
            removed = removeByGroup(gan.groupId());
        }
        else
        {
            // remove single item.
            lock.writeLock().lock();
            try
            {
                final MemoryElementDescriptor<K, V> me = map.remove(key);
                if (me != null)
                {
                    lockedRemoveElement(me);
                    removed = true;
                }
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        return removed;
    }

    /**
     * Removes all cached items from the cache.
     */
    @Override
    public void removeAll()
    {
        lock.writeLock().lock();
        try
        {
            map.clear();
            lockedRemoveAll();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove all keys of the same group hierarchy.
     * @param groupId The group attribute id
     * @return true if something has been removed
     */
    protected boolean removeByGroup(final GroupId groupId)
    {
        lock.writeLock().lock();
        try
        {
            // remove all keys of the same group hierarchy.
            return map.entrySet().removeIf(entry -> {
                final K k = entry.getKey();

                if (k instanceof GroupAttrName kgan && kgan.groupId().equals(groupId))
                {
                        lockedRemoveElement(entry.getValue());
                        return true;
                }

                return false;
            });
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Remove all keys of the same name hierarchy.
     *
     * @param keyString The key as string
     * @return true if something has been removed
     */
    protected boolean removeByHierarchy(final String keyString)
    {
        lock.writeLock().lock();
        try
        {
            // remove all keys of the same name hierarchy.
            return map.entrySet().removeIf(entry -> {
                final K k = entry.getKey();

                if (k instanceof String s && s.startsWith(keyString))
                {
                    lockedRemoveElement(entry.getValue());
                    return true;
                }

                return false;
            });
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Puts an item to the cache.
     *
     * @param ce Description of the Parameter
     * @throws IOException Description of the Exception
     */
    @Override
    public void update( ICacheElement<K, V> ce )
        throws IOException
    {
        putCnt.incrementAndGet();
        final MemoryElementDescriptor<K, V> newNode = wrap(ce);

        lock.writeLock().lock();
        try
        {
            final MemoryElementDescriptor<K, V> oldNode = map.put(ce.key(), newNode);
            lockedUpdateElement(newNode, oldNode);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Puts an item to the cache.
     *
     * @param ce The item
     */
    @Override
    public void waterfall( final ICacheElement<K, V> ce )
    {
        this.waterfall.accept(ce);
    }
}
