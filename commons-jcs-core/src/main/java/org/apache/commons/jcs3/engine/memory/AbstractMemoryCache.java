package org.apache.commons.jcs3.engine.memory;

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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.engine.behavior.ICache;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.control.group.GroupId;
import org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache;
import org.apache.commons.jcs3.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This base includes some common code for memory caches.
 */
public abstract class AbstractMemoryCache<K, V>
    implements IMemoryCache<K, V>
{
    /** Log instance */
    private static final Log log = LogManager.getLog( AbstractMemoryCache.class );

    /** Cache Attributes.  Regions settings. */
    private ICompositeCacheAttributes cacheAttributes;

    /** The cache region this store is associated with */
    private CompositeCache<K, V> cache;

    /** How many to spool at a time. */
    protected int chunkSize;

    protected final Lock lock = new ReentrantLock();

    /** Map where items are stored by key.  This is created by the concrete child class. */
    protected Map<K, MemoryElementDescriptor<K, V>> map;// TODO privatise

    /** number of hits */
    protected AtomicLong hitCnt;

    /** number of misses */
    protected AtomicLong missCnt;

    /** number of puts */
    protected AtomicLong putCnt;

    /**
     * For post reflection creation initialization
     * <p>
     * @param hub
     */
    @Override
    public void initialize( final CompositeCache<K, V> hub )
    {
        hitCnt = new AtomicLong(0);
        missCnt = new AtomicLong(0);
        putCnt = new AtomicLong(0);

        this.cacheAttributes = hub.getCacheAttributes();
        this.chunkSize = cacheAttributes.getSpoolChunkSize();
        this.cache = hub;

        this.map = createMap();
    }

    /**
     * Children must implement this method. A FIFO implementation may use a tree map. An LRU might
     * use a hashtable. The map returned should be threadsafe.
     * <p>
     * @return a threadsafe Map
     */
    public abstract Map<K, MemoryElementDescriptor<K, V>> createMap();

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
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
                .filter(element -> element != null)
                .collect(Collectors.toMap(
                        ICacheElement::getKey,
                        element -> element));
        }

        return new HashMap<>();
    }

    /**
     * Get an item from the cache without affecting its last access time or position. Not all memory
     * cache implementations can get quietly.
     * <p>
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
                    () -> getCacheName(), () -> key );

            ce = me.getCacheElement();
        }
        else
        {
            log.debug( "{0}: MemoryCache quiet miss for {1}",
                    () -> getCacheName(), () -> key );
        }

        return ce;
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce Description of the Parameter
     * @throws IOException Description of the Exception
     */
    @Override
    public abstract void update( ICacheElement<K, V> ce )
        throws IOException;

    /**
     * Removes all cached items from the cache.
     * <p>
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
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    protected abstract void lockedRemoveAll();

    /**
     * Prepares for shutdown. Reset statistics
     * <p>
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
     * @return statistics about the cache
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Abstract Memory Cache" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();
        stats.setStatElements(elems);

        elems.add(new StatElement<>("Put Count", putCnt));
        elems.add(new StatElement<>("Hit Count", hitCnt));
        elems.add(new StatElement<>("Miss Count", missCnt));
        elems.add(new StatElement<>( "Map Size", Integer.valueOf(getSize()) ) );

        return stats;
    }

    /**
     * Returns the current cache size.
     * <p>
     * @return The size value
     */
    @Override
    public int getSize()
    {
        return this.map.size();
    }

    /**
     * Returns the cache (aka "region") name.
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        final String attributeCacheName = this.cacheAttributes.getCacheName();
        if(attributeCacheName != null)
        {
            return attributeCacheName;
        }
        return cache.getCacheName();
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce the item
     */
    @Override
    public void waterfal( final ICacheElement<K, V> ce )
    {
        this.cache.spoolToDisk( ce );
    }

    // ---------------------------------------------------------- debug method
    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        if (log.isTraceEnabled())
        {
            log.trace("dumpingMap");
            map.forEach((key, value) ->
                log.trace("dumpMap> key={0}, val={1}",key, key,
                        value.getCacheElement().getVal()));
        }
    }

    /**
     * Returns the CacheAttributes.
     * <p>
     * @return The CacheAttributes value
     */
    @Override
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttributes;
    }

    /**
     * Sets the CacheAttributes.
     * <p>
     * @param cattr The new CacheAttributes value
     */
    @Override
    public void setCacheAttributes( final ICompositeCacheAttributes cattr )
    {
        this.cacheAttributes = cattr;
    }

    /**
     * Gets the cache hub / region that the MemoryCache is used by
     * <p>
     * @return The cache value
     */
    @Override
    public CompositeCache<K, V> getCompositeCache()
    {
        return this.cache;
    }

    /**
     * Remove all keys of the same group hierarchy.
     * @param key the key
     * @return true if something has been removed
     */
    protected boolean removeByGroup(final K key)
    {
        final GroupId groupId = ((GroupAttrName<?>) key).groupId;

        // remove all keys of the same group hierarchy.
        return map.entrySet().removeIf(entry -> {
            final K k = entry.getKey();

            if (k instanceof GroupAttrName && ((GroupAttrName<?>) k).groupId.equals(groupId))
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
        else if (key instanceof GroupAttrName && ((GroupAttrName<?>) key).attrName == null)
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
     * Get an Array of the keys for all elements in the memory cache
     *
     * @return An Object[]
     */
    @Override
    public Set<K> getKeySet()
    {
        return new LinkedHashSet<>(map.keySet());
    }

    /**
     * Get an item from the cache.
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

        log.debug("{0}: getting item for key {1}", () -> getCacheName(),
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

            log.debug("{0}: MemoryCache hit for {1}", () -> getCacheName(),
                    () -> key);
        }
        else
        {
            missCnt.incrementAndGet();

            log.debug("{0}: MemoryCache miss for {1}", () -> getCacheName(),
                    () -> key);
        }

        return ce;
    }

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    protected abstract void lockedGetElement(MemoryElementDescriptor<K, V> me);
}
