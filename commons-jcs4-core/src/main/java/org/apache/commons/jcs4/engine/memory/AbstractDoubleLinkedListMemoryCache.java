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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.group.GroupAttrName;
import org.apache.commons.jcs4.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;
import org.apache.commons.jcs4.log.Log;
import org.apache.commons.jcs4.utils.struct.DoubleLinkedList;

/**
 * This class contains methods that are common to memory caches using the double linked list, such
 * as the LRU, MRU, FIFO, and LIFO caches.
 * <p>
 * Children can control the expiration algorithm by controlling the update and get. The last item in the list will be the one
 * removed when the list fills. For instance LRU should more items to the front as they are used. FIFO should simply add new items
 * to the front of the list.
 */
public abstract class AbstractDoubleLinkedListMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
    /** The logger. */
    private static final Log log = Log.getLog(AbstractDoubleLinkedListMemoryCache.class);

    static
    {
        cacheImplementationName = "Abstract DoubleLinkedList Memory Cache";
    }

    /** Thread-safe double linked list for lru */
    private DoubleLinkedList<MemoryElementDescriptor<K, V>>[] lists;

    /** Number of shards */
    private int shards;

    private static class AtomicCyclicCounter
    {
        private final int max;
        private final AtomicInteger counter;

        private AtomicCyclicCounter(int max)
        {
            this.max = max;
            counter = new AtomicInteger();
        }

        private int incrementAndGet()
        {
            return counter.accumulateAndGet(1, (index, inc) -> (++index >= max ? 0 : index));
        }
    }

    /** shard to spool */
    private AtomicCyclicCounter spoolShard;

    /**
     * Returns the current cache shard for the given key
     *
     * @param key the cache key
     * @return The shard
     */
    protected int spreadShard(K key)
    {
        return Math.abs(key.hashCode() % shards);
    }

    /**
     * Adds a new node to the start of the link list.
     * (guarded by the lock)
     *
     * @param me The MemoryElementDescriptor to be added to the start of the list
     */
    protected void addFirst(final MemoryElementDescriptor<K, V> me)
    {
        int shard = spreadShard(me.getCacheElement().key());
        lists[shard].addFirst(me);
        if ( log.isTraceEnabled() )
        {
            verifyCache(me.getCacheElement().key());
        }
    }

    /**
     * Adds a new node to the end of the link list.
     * (guarded by the lock)
     *
     * @param me The feature to be added to the end of the list
     */
    protected void addLast(final MemoryElementDescriptor<K,V> me)
    {
        int shard = spreadShard(me.getCacheElement().key());
        lists[shard].addLast(me);
        if ( log.isTraceEnabled() )
        {
            verifyCache(me.getCacheElement().key());
        }
    }

    /**
     * Adjust the list as needed for a get. This allows children to control the algorithm
     *
     * @param list the node list
     * @param me the current cache element
     */
    protected abstract void adjustListForGet(DoubleLinkedList<MemoryElementDescriptor<K, V>> list,
            MemoryElementDescriptor<K, V> me);

    /**
     * This is called by super initialize.
     *
     * @return new HashMap()
     */
    @Override
    protected ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
    {
        int maxObjects = getCacheAttributes().MaxObjects();
        int shards = getCacheAttributes().Shards();
        return new ConcurrentHashMap<>(maxObjects < 0 ? 16 : maxObjects, 0.75f, shards);
    }

    /**
     * This returns semi-structured information on the memory cache, such as the size, put count,
     * hit count, and miss count.
     *
     * @see org.apache.commons.jcs4.engine.memory.behavior.IMemoryCache#getStatistics()
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();
        stats.addStatElement("Shards", Integer.valueOf(shards));
        for (int i = 0; i < shards; i++)
        {
            stats.addStatElement("List Size " + i, Integer.valueOf(lists[i].size()));
        }

        return stats;
    }

    /**
     * For post reflection creation initialization.
     *
     * @param hub
     */
    @SuppressWarnings("unchecked")
    @Override
    public void initialize(final CompositeCache<K, V> hub)
    {
        super.initialize(hub);
        this.shards = getCacheAttributes().Shards();
        lists = new DoubleLinkedList[shards];
        for (int i = 0; i < shards; i++)
        {
            lists[i] = new DoubleLinkedList<>();
        }
        this.spoolShard = new AtomicCyclicCounter(shards);
        log.info("initialized MemoryCache for {0}", this::getCacheName);
    }

    /**
     * Wrap the cache element into an appropriate memory element descriptor
     *
     * @param ce The cache element
     * @return The memory element descriptor
     */
    @Override
    protected MemoryElementDescriptor<K, V> wrap(ICacheElement<K, V> ce)
    {
        return new MemoryElementDescriptor<>(ce);
    }

    /**
     * Update control structures after get
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void adjustGetElement(final MemoryElementDescriptor<K, V> me)
    {
        int shard = spreadShard(me.getCacheElement().key());
        adjustListForGet(lists[shard], me);
    }

    /**
     * Update control structures after update
     *
     * @param newNode The memory element descriptor of the current cache element
     * @throws IOException if spooling operation fails
     */
    @Override
    protected void adjustUpdateElement(MemoryElementDescriptor<K, V> newNode) throws IOException
    {
        int shard = spreadShard(newNode.getCacheElement().key());
        lists[shard].makeFirst(newNode);
    }

    /**
     * Removes all cached items from the cache control structures.
     */
    @Override
    protected void adjustRemoveAll()
    {
        Arrays.stream(lists).forEach(DoubleLinkedList::removeAll);
    }

    /**
     * Remove element from control structure
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void adjustRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        int shard = spreadShard(me.getCacheElement().key());
        lists[shard].remove(me);
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
        super.update(ce);

        // If we are over the max spool some
        spoolIfNeeded();
    }

    /**
     * If the max size has been reached, spool.
     * (guarded by the lock)
     *
     * @throws IOException
     */
    private void spoolIfNeeded() throws IOException
    {
        // The spool will put them in a disk event queue, so there is no
        // need to pre-queue the queuing. This would be a bit wasteful
        // and wouldn't save much time in this synchronous call.
        final int size = getSize();
        final int maxObjects = getCacheAttributes().MaxObjects();
        // If the element limit is reached, we need to spool
        if (maxObjects < 0 || size <= maxObjects)
        {
            return;
        }

        log.debug("In memory limit reached, spooling");

        // Write the last 'chunkSize' items to disk.
        final int chunkSizeCorrected = Math.min(size, getCacheAttributes().SpoolChunkSize());

        log.debug("About to spool to disk cache, map size: {0}, max objects: {1}, "
                + "maximum items to spool: {2}", () -> size,
                getCacheAttributes()::MaxObjects,
                () -> chunkSizeCorrected);

        freeElements(chunkSizeCorrected);

        // If this is out of the sync block it can detect a mismatch
        // where there is none.
//        if (log.isDebugEnabled() && getSize() != list.size())
//        {
//            log.debug("update: After spool, size mismatch: map.size() = {0}, "
//                    + "linked list size = {1}", getSize(), list.size());
//        }
    }

    /**
     * This spools the last element in the LRU, if one exists.
     *
     * @return ICacheElement&lt;K, V&gt; if there was a last element, else null.
     * @throws IOException
     */
    @Override
    protected ICacheElement<K, V> freeElement() throws IOException
    {
        ICacheElement<K, V> toSpool = null;

        int shard = this.spoolShard.incrementAndGet();
        final MemoryElementDescriptor<K, V> last = lists[shard].getLast();
        if (last != null)
        {
            toSpool = last.getCacheElement();
            if (toSpool == null)
            {
                throw new IOException("freeElement: last.ce is null!");
            }
            waterfall(toSpool);
            if (!remove(toSpool.key()))
            {
                log.warn("update: remove failed for key: {0}", toSpool::key);

                if (log.isTraceEnabled())
                {
                    verifyCache();
                }
            }
        }

        return toSpool;
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    private void dumpCacheEntries()
    {
        log.trace("dumpingCacheEntries");
        for (int i = 0; i < shards; i++)
        {
            for (MemoryElementDescriptor<K, V> me : lists[i])
            {
                log.trace("dumpCacheEntries> shard={0}, key={1}, val={2}", i,
                        me.getCacheElement().key(), me.getCacheElement().value());
            }
        }
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks consistency between
     * List and map.
     */
    private void verifyCache()
    {
        boolean found = false;
        Map<K, MemoryElementDescriptor<K, V>> mapView = getMapView();
        log.trace("verifycache[{0}]: map contains {1} elements, linked list "
                + "contains {2} elements", getCacheName(), getSize(),
                Arrays.stream(lists)
                    .mapToInt(DoubleLinkedList::size)
                    .sum());
        log.trace("verifycache: checking linked list by key ");
        for (int i = 0; i < shards; i++)
        {
            for (MemoryElementDescriptor<K, V> li : lists[i])
            {
                final K key = li.getCacheElement().key();
                if (!mapView.containsKey(key))
                {
                    log.error("verifycache[{0}]: map does not contain key : {1}",
                            getCacheName(), key);
                    log.error("key class={0}", key.getClass());
                    log.error("key hashCode={0}", key.hashCode());
                    log.error("key toString={0}", key.toString());
                    if (key instanceof GroupAttrName name)
                    {
                        log.error("GroupID hashCode={0}", name.groupId().hashCode());
                        log.error("GroupID.class={0}", name.groupId().getClass());
                        log.error("AttrName hashCode={0}", name.attrName().hashCode());
                        log.error("AttrName.class={0}", name.attrName().getClass());
                    }
                    dumpMap();
                }
                else if (mapView.get(key) == null)
                {
                    log.error("verifycache[{0}]: linked list retrieval returned "
                            + "null for key: {1}", getCacheName(), key);
                }
            }

            log.trace("verifycache: checking linked list by value ");
            for (MemoryElementDescriptor<K, V> li : lists[i])
            {
                if (!mapView.containsValue(li))
                {
                    log.error("verifycache[{0}]: map does not contain value: {1}",
                            getCacheName(), li);
                    dumpMap();
                }
            }
        }

        log.trace("verifycache: checking via keysets!");
        for (final Object val : mapView.keySet())
        {
            found = false;

            for (int i = 0; i < shards; i++)
            {
                for (MemoryElementDescriptor<K, V> li : lists[i])
                {
                    if (val.equals(li.getCacheElement().key()))
                    {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
            {
                log.error("verifycache[{0}]: key not found in list : {1}",
                        getCacheName(), val);
                dumpCacheEntries();
                if (mapView.containsKey(val))
                {
                    log.error("verifycache: map contains key");
                }
                else
                {
                    log.error("verifycache: map does NOT contain key, what the HECK!");
                }
            }
        }
    }

    /**
     * Logs an error if an element that should be in the cache is not.
     * <p>
     *
     * @param key
     */
    private void verifyCache(final K key)
    {
        boolean found = false;

        // go through the linked list looking for the key
        int shard = spreadShard(key);
        for (MemoryElementDescriptor<K, V> li : lists[shard])
        {
            if (li.getCacheElement().key() == key)
            {
                found = true;
                log.trace("verifycache(key) shard: {0}, key match: {1}", shard, key);
                break;
            }
        }
        if (!found)
        {
            log.error("verifycache(key)[{0}], shard {1}, couldn't find key! : {2}",
                    getCacheName(), shard, key);
        }
    }
}
