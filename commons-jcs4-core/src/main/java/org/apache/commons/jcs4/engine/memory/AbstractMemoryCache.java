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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

    /** Cache Attributes.  Regions settings. */
    private ICompositeCacheAttributes cacheAttributes;

    /** The cache region this store is associated with */
    private CompositeCache<K, V> cache;

    /** How many to spool at a time. */
    protected int chunkSize;

    protected final Lock lock = new ReentrantLock();

    /** Map where items are stored by key.  This is created by the concrete child class. */
    protected Map<K, MemoryElementDescriptor<K, V>> map; // TODO privatise

    /** Number of hits */
    protected AtomicLong hitCnt;

    /** Number of misses */
    protected AtomicLong missCnt;

    /** Number of puts */
    protected AtomicLong putCnt;

    /**
     * Children must implement this method. A FIFO implementation may use a tree map. An LRU might
     * use a hashtable. The map returned should be threadsafe.
     *
     * @return a threadsafe Map
     */
    public abstract Map<K, MemoryElementDescriptor<K, V>> createMap();

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
                log.trace("dumpMap> key={0}, val={1}", key, value.getCacheElement().getVal()));
        }
    }

    /**
     * Gets an item from the cache.
     * <p>
     *
     * @param key Identifies item to find
     * @return ICacheElement&lt;K, V&gt; if found, else null
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get(final K key) throws IOException
    {
        ICacheElement<K, V> ce = null;

        log.debug("{0}: getting item for key {1}", this::getCacheName,
                () -> key);

        final MemoryElementDescriptor<K, V> me = map.get(key);

        if (me != null)
        {
            hitCnt.incrementAndGet();
            ce = me.getCacheElement();

            lock.lock();
            try
            {
                lockedGetElement(me);
            }
            finally
            {
                lock.unlock();
            }

            log.debug("{0}: MemoryCache hit for {1}", this::getCacheName,
                    () -> key);
        }
        else
        {
            missCnt.incrementAndGet();

            log.debug("{0}: MemoryCache miss for {1}", this::getCacheName,
                    () -> key);
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
        final String attributeCacheName = this.cacheAttributes.cacheName();
        if(attributeCacheName != null)
        {
            return attributeCacheName;
        }
        return cache.getCacheName();
    }

    /**
     * Gets the cache hub / region that the MemoryCache is used by
     *
     * @return The cache value
     */
    @Override
    public CompositeCache<K, V> getCompositeCache()
    {
        return this.cache;
    }

    /**
     * Gets a set of the keys for all elements in the memory cache
     *
     * @return a set of keys
     */
    @Override
    public Set<K> getKeySet()
    {
        return new LinkedHashSet<>(map.keySet());
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
        throws IOException
    {
        if (keys != null)
        {
            return keys.stream()
                .map(key -> {
                    try
                    {
                        return get(key);
                    }
                    catch (final IOException e)
                    {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ICacheElement::getKey,
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
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> getQuiet( final K key )
        throws IOException
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
        return this.map.size();
    }

    /**
     * @return statistics about the cache
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats("Abstract Memory Cache");

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
        this.chunkSize = cacheAttributes.spoolChunkSize();
        this.cache = hub;

        this.map = createMap();
    }

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    protected abstract void lockedGetElement(MemoryElementDescriptor<K, V> me);

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    protected abstract void lockedRemoveAll();

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    protected abstract void lockedRemoveElement(MemoryElementDescriptor<K, V> me);

    /**
     * Removes an item from the cache. This method handles hierarchical removal. If the key is a
     * String and ends with the CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     * starting with the argument String will be removed.
     * <p>
     *
     * @param key
     * @return true if the removal was successful
     * @throws IOException
     */
    @Override
    public boolean remove(final K key) throws IOException
    {
        log.debug("removing item for key: {0}", key);

        boolean removed = false;

        // handle partial removal
        if (key instanceof String && ((String) key).endsWith(ICache.NAME_COMPONENT_DELIMITER))
        {
            removed = removeByHierarchy(key);
        }
        else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName() == null)
        {
            removed = removeByGroup(key);
        }
        else
        {
            // remove single item.
            lock.lock();
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
                lock.unlock();
            }
        }

        return removed;
    }

    /**
     * Removes all cached items from the cache.
     *
     * @throws IOException
     */
    @Override
    public void removeAll() throws IOException
    {
        lock.lock();
        try
        {
            lockedRemoveAll();
            map.clear();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Remove all keys of the same group hierarchy.
     * @param key the key
     * @return true if something has been removed
     */
    protected boolean removeByGroup(final K key)
    {
        final GroupId groupId = ((GroupAttrName<?>) key).groupId();

        // remove all keys of the same group hierarchy.
        return map.entrySet().removeIf(entry -> {
            final K k = entry.getKey();

            if (k instanceof GroupAttrName && ((GroupAttrName<?>) k).groupId().equals(groupId))
            {
                lock.lock();
                try
                {
                    lockedRemoveElement(entry.getValue());
                    return true;
                }
                finally
                {
                    lock.unlock();
                }
            }

            return false;
        });
    }

    /**
     * Remove all keys of the same name hierarchy.
     *
     * @param key the key
     * @return true if something has been removed
     */
    protected boolean removeByHierarchy(final K key)
    {
        final String keyString = key.toString();

        // remove all keys of the same name hierarchy.
        return map.entrySet().removeIf(entry -> {
            final K k = entry.getKey();

            if (k instanceof String && ((String) k).startsWith(keyString))
            {
                lock.lock();
                try
                {
                    lockedRemoveElement(entry.getValue());
                    return true;
                }
                finally
                {
                    lock.unlock();
                }
            }

            return false;
        });
    }

    /**
     * Sets the CacheAttributes.
     *
     * @param cattr The new CacheAttributes value
     */
    @Override
    public void setCacheAttributes( final ICompositeCacheAttributes cattr )
    {
        this.cacheAttributes = cattr;
    }

    /**
     * Puts an item to the cache.
     *
     * @param ce Description of the Parameter
     * @throws IOException Description of the Exception
     */
    @Override
    public abstract void update( ICacheElement<K, V> ce )
        throws IOException;

    /**
     * Puts an item to the cache.
     *
     * @param ce the item
     */
    @Override
    public void waterfal( final ICacheElement<K, V> ce )
    {
        this.cache.spoolToDisk( ce );
    }
}
