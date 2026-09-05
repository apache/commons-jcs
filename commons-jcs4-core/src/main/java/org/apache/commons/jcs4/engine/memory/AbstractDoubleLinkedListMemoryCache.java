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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private DoubleLinkedList<MemoryElementDescriptor<K, V>> list;

    /**
     * Adds a new node to the start of the link list.
     * (guarded by the lock)
     *
     * @param me The MemoryElementDescriptor to be added to the start of the list
     */
    protected void addFirst(final MemoryElementDescriptor<K, V> me)
    {
        list.addFirst(me);
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
        list.addLast(me);
        if ( log.isTraceEnabled() )
        {
            verifyCache(me.getCacheElement().key());
        }
    }

    /**
     * Adjust the list as needed for a get. This allows children to control the algorithm
     * (guarded by the lock)
     *
     * @param list the node list
     * @param me the current cache element
     */
    protected abstract void adjustListForGet(DoubleLinkedList<MemoryElementDescriptor<K, V>> list, MemoryElementDescriptor<K, V> me);

    /**
     * Children implement this to control the cache expiration algorithm
     * <p>
     *
     * @param me the current cache element
     */
    protected abstract void adjustListForUpdate(MemoryElementDescriptor<K, V> me);

    /**
     * This is called by super initialize.
     *
     * @return new HashMap()
     */
    @Override
    protected Map<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new ConcurrentHashMap<>();
    }

    /**
     * @see org.apache.commons.jcs4.engine.memory.AbstractMemoryCache#get(Object)
     */
    @Override
    public ICacheElement<K, V> get(final K key)
    {
        final ICacheElement<K, V> ce = super.get(key);

        if (log.isTraceEnabled())
        {
            verifyCache();
        }

        return ce;
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
        stats.addStatElement("List Size", Integer.valueOf(list.size()));

        return stats;
    }

    /**
     * For post reflection creation initialization.
     * <p>
     *
     * @param hub
     */
    @Override
    public void initialize(final CompositeCache<K, V> hub)
    {
        super.initialize(hub);
        list = new DoubleLinkedList<>();
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
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void lockedGetElement(final MemoryElementDescriptor<K, V> me)
    {
        adjustListForGet(list, me);
    }

    /**
     * Update control structures after update
     * (guarded by the lock)
     *
     * @param newNode The memory element descriptor of the current cache element
     * @param oldNode The memory element descriptor of the previous cache element
     * @throws IOException if spooling operation fails
     */
    @Override
    protected void lockedUpdateElement(MemoryElementDescriptor<K, V> newNode,
            MemoryElementDescriptor<K, V> oldNode) throws IOException
    {
        adjustListForUpdate(newNode);

        // If the node was the same as an existing node, remove it.
        if (oldNode != null && newNode.getCacheElement().key().equals(oldNode.getCacheElement().key()))
        {
            list.remove(oldNode);
        }

        // If we are over the max spool some
        spoolIfNeeded();
    }

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    @Override
    protected void lockedRemoveAll()
    {
        list.removeAll();
    }

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void lockedRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        list.remove(me);
    }

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
    @Override
    protected int lockedFreeElements(final int numberToFree) throws IOException
    {
        int freed = 0;

        for (; freed < numberToFree; freed++)
        {
            final ICacheElement<K, V> element = spoolLastElement();
            if (element == null)
            {
                break;
            }
        }

        return freed;
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
        // If the element limit is reached, we need to spool
        if (size <= getCacheAttributes().MaxObjects())
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
        if (log.isDebugEnabled() && getSize() != list.size())
        {
            log.debug("update: After spool, size mismatch: map.size() = {0}, "
                    + "linked list size = {1}", getSize(), list.size());
        }
    }

    /**
     * This spools the last element in the LRU, if one exists.
     * (guarded by the lock)
     *
     * @return ICacheElement&lt;K, V&gt; if there was a last element, else null.
     * @throws Error
     */
    private ICacheElement<K, V> spoolLastElement() throws Error
    {
        ICacheElement<K, V> toSpool = null;

        final MemoryElementDescriptor<K, V> last = list.getLast();
        if (last != null)
        {
            toSpool = last.getCacheElement();
            if (toSpool == null)
            {
                throw new Error("update: last.ce is null!");
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
        for (MemoryElementDescriptor<K, V> me : list)
        {
            log.trace("dumpCacheEntries> key={0}, val={1}",
                    me.getCacheElement().key(), me.getCacheElement().value());
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
                list.size());
        log.trace("verifycache: checking linked list by key ");
        for (MemoryElementDescriptor<K, V> li : list)
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
        for (MemoryElementDescriptor<K, V> li : list)
        {
            if (!mapView.containsValue(li))
            {
                log.error("verifycache[{0}]: map does not contain value: {1}",
                        getCacheName(), li);
                dumpMap();
            }
        }

        log.trace("verifycache: checking via keysets!");
        for (final Object val : mapView.keySet())
        {
            found = false;

            for (MemoryElementDescriptor<K, V> li : list)
            {
                if (val.equals(li.getCacheElement().key()))
                {
                    found = true;
                    break;
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
        for (MemoryElementDescriptor<K, V> li : list)
        {
            if (li.getCacheElement().key() == key)
            {
                found = true;
                log.trace("verifycache(key) key match: {0}", key);
                break;
            }
        }
        if (!found)
        {
            log.error("verifycache(key)[{0}], couldn't find key! : {1}",
                    getCacheName(), key);
        }
    }
}
