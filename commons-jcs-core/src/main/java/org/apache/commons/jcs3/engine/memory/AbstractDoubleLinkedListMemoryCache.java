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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.struct.DoubleLinkedList;

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
    private static final Log log = LogManager.getLog(AbstractDoubleLinkedListMemoryCache.class);

    /** thread-safe double linked list for lru */
    protected DoubleLinkedList<MemoryElementDescriptor<K, V>> list; // TODO privatise

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
     * This is called by super initialize.
     *
     * NOTE: should return a thread safe map
     *
     * <p>
     *
     * @return new ConcurrentHashMap()
     */
    @Override
    public ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new ConcurrentHashMap<>();
    }

    /**
     * Calls the abstract method updateList.
     * <p>
     * If the max size is reached, an element will be put to disk.
     * <p>
     *
     * @param ce
     *            The cache element, or entry wrapper
     * @throws IOException
     */
    @Override
    public final void update(final ICacheElement<K, V> ce) throws IOException
    {
        putCnt.incrementAndGet();

        lock.lock();
        try
        {
            final MemoryElementDescriptor<K, V> newNode = adjustListForUpdate(ce);

            // this should be synchronized if we were not using a ConcurrentHashMap
            final K key = newNode.getCacheElement().getKey();
            final MemoryElementDescriptor<K, V> oldNode = map.put(key, newNode);

            // If the node was the same as an existing node, remove it.
            if (oldNode != null && key.equals(oldNode.getCacheElement().getKey()))
            {
                list.remove(oldNode);
            }
        }
        finally
        {
            lock.unlock();
        }

        // If we are over the max spool some
        spoolIfNeeded();
    }

    /**
     * Children implement this to control the cache expiration algorithm
     * <p>
     *
     * @param ce
     * @return MemoryElementDescriptor the new node
     * @throws IOException
     */
    protected abstract MemoryElementDescriptor<K, V> adjustListForUpdate(ICacheElement<K, V> ce) throws IOException;

    /**
     * If the max size has been reached, spool.
     * <p>
     *
     * @throws Error
     */
    private void spoolIfNeeded() throws Error
    {
        final int size = map.size();
        // If the element limit is reached, we need to spool

        if (size <= this.getCacheAttributes().getMaxObjects())
        {
            return;
        }

        log.debug("In memory limit reached, spooling");

        // Write the last 'chunkSize' items to disk.
        final int chunkSizeCorrected = Math.min(size, chunkSize);

        log.debug("About to spool to disk cache, map size: {0}, max objects: {1}, "
                + "maximum items to spool: {2}", () -> size,
                this.getCacheAttributes()::getMaxObjects,
                () -> chunkSizeCorrected);

        // The spool will put them in a disk event queue, so there is no
        // need to pre-queue the queuing. This would be a bit wasteful
        // and wouldn't save much time in this synchronous call.
        lock.lock();

        try
        {
            freeElements(chunkSizeCorrected);

            // If this is out of the sync block it can detect a mismatch
            // where there is none.
            if (log.isDebugEnabled() && map.size() != list.size())
            {
                log.debug("update: After spool, size mismatch: map.size() = {0}, "
                        + "linked list size = {1}", map.size(), list.size());
            }
        }
        finally
        {
            lock.unlock();
        }

        log.debug("update: After spool map size: {0} linked list size = {1}",
                () -> map.size(), () -> list.size());
    }

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <i>numberToFree</i> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     * <p>
     *
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     */
    @Override
    public int freeElements(final int numberToFree)
    {
        int freed = 0;

        lock.lock();

        try
        {
            for (; freed < numberToFree; freed++)
            {
                final ICacheElement<K, V> element = spoolLastElement();
                if (element == null)
                {
                    break;
                }
            }
        }
        finally
        {
            lock.unlock();
        }

        return freed;
    }

    /**
     * This spools the last element in the LRU, if one exists.
     * The method is called guarded by the lock
     * <p>
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
            getCompositeCache().spoolToDisk(toSpool);
            if (map.remove(toSpool.getKey()) == null)
            {
                log.warn("update: remove failed for key: {0}", toSpool.getKey());

                if (log.isTraceEnabled())
                {
                    verifyCache();
                }
            }

            list.remove(last);
        }

        return toSpool;
    }

    /**
     * @see org.apache.commons.jcs3.engine.memory.AbstractMemoryCache#get(java.lang.Object)
     */
    @Override
    public ICacheElement<K, V> get(final K key) throws IOException
    {
        final ICacheElement<K, V> ce = super.get(key);

        if (log.isTraceEnabled())
        {
            verifyCache();
        }

        return ce;
    }

    /**
     * Adjust the list as needed for a get. This allows children to control the algorithm
     * <p>
     *
     * @param me
     */
    protected abstract void adjustListForGet(MemoryElementDescriptor<K, V> me);

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    @Override
    protected void lockedGetElement(final MemoryElementDescriptor<K, V> me)
    {
        adjustListForGet(me);
    }

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    @Override
    protected void lockedRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        list.remove(me);
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

    // --------------------------- internal methods (linked list implementation)
    /**
     * Adds a new node to the start of the link list.
     * <p>
     *
     * @param ce
     *            The feature to be added to the First
     * @return MemoryElementDescriptor
     */
    protected MemoryElementDescriptor<K, V> addFirst(final ICacheElement<K, V> ce)
    {
        lock.lock();
        try
        {
            final MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<>(ce);
            list.addFirst(me);
            if ( log.isTraceEnabled() )
            {
                verifyCache(ce.getKey());
            }
            return me;
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Adds a new node to the end of the link list.
     * <p>
     *
     * @param ce
     *            The feature to be added to the First
     * @return MemoryElementDescriptor
     */
    protected MemoryElementDescriptor<K, V> addLast(final ICacheElement<K, V> ce)
    {
        lock.lock();
        try
        {
            final MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<>(ce);
            list.addLast(me);
            if ( log.isTraceEnabled() )
            {
                verifyCache(ce.getKey());
            }
            return me;
        }
        finally
        {
            lock.unlock();
        }
    }

    // ---------------------------------------------------------- debug methods

    /**
     * Dump the cache entries from first to list for debugging.
     */
    @SuppressWarnings("unchecked")
    // No generics for public fields
    private void dumpCacheEntries()
    {
        log.trace("dumpingCacheEntries");
        for (MemoryElementDescriptor<K, V> me = list.getFirst(); me != null; me = (MemoryElementDescriptor<K, V>) me.next)
        {
            log.trace("dumpCacheEntries> key={0}, val={1}",
                    me.getCacheElement().getKey(), me.getCacheElement().getVal());
        }
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks consistency between
     * List and map.
     */
    @SuppressWarnings("unchecked")
    // No generics for public fields
    private void verifyCache()
    {
        boolean found = false;
        log.trace("verifycache[{0}]: map contains {1} elements, linked list "
                + "contains {2} elements", getCacheName(), map.size(),
                list.size());
        log.trace("verifycache: checking linked list by key ");
        for (MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next)
        {
            final K key = li.getCacheElement().getKey();
            if (!map.containsKey(key))
            {
                log.error("verifycache[{0}]: map does not contain key : {1}",
                        getCacheName(), key);
                log.error("key class={0}", key.getClass());
                log.error("key hashcode={0}", key.hashCode());
                log.error("key toString={0}", key.toString());
                if (key instanceof GroupAttrName)
                {
                    final GroupAttrName<?> name = (GroupAttrName<?>) key;
                    log.error("GroupID hashcode={0}", name.groupId.hashCode());
                    log.error("GroupID.class={0}", name.groupId.getClass());
                    log.error("AttrName hashcode={0}", name.attrName.hashCode());
                    log.error("AttrName.class={0}", name.attrName.getClass());
                }
                dumpMap();
            }
            else if (map.get(key) == null)
            {
                log.error("verifycache[{0}]: linked list retrieval returned "
                        + "null for key: {1}", getCacheName(), key);
            }
        }

        log.trace("verifycache: checking linked list by value ");
        for (MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next)
        {
            if (!map.containsValue(li))
            {
                log.error("verifycache[{0}]: map does not contain value: {1}",
                        getCacheName(), li);
                dumpMap();
            }
        }

        log.trace("verifycache: checking via keysets!");
        for (final Object val : map.keySet())
        {
            found = false;

            for (MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next)
            {
                if (val.equals(li.getCacheElement().getKey()))
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
                if (map.containsKey(val))
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
    @SuppressWarnings("unchecked")
    // No generics for public fields
    private void verifyCache(final K key)
    {
        boolean found = false;

        // go through the linked list looking for the key
        for (MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next)
        {
            if (li.getCacheElement().getKey() == key)
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

    /**
     * This returns semi-structured information on the memory cache, such as the size, put count,
     * hit count, and miss count.
     * <p>
     *
     * @see org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache#getStatistics()
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();
        stats.setTypeName( /* add algorithm name */"Memory Cache");

        final List<IStatElement<?>> elems = stats.getStatElements();

        elems.add(new StatElement<>("List Size", Integer.valueOf(list.size())));

        return stats;
    }
}
