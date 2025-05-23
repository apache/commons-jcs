package org.apache.commons.jcs3.engine.memory.soft;

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
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs3.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs3.engine.memory.util.SoftReferenceElementDescriptor;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;

/**
 * A JCS IMemoryCache that has {@link SoftReference} to all its values.
 * This cache does not respect {@link ICompositeCacheAttributes#getMaxObjects()}
 * as overflowing is handled by Java GC.
 * <p>
 * The cache also has strong references to a maximum number of objects given by
 * the maxObjects parameter
 * </p>
 */
public class SoftReferenceMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
    /** The logger. */
    private static final Log log = Log.getLog(SoftReferenceMemoryCache.class);

    /**
     * Strong references to the maxObjects number of newest objects.
     * <p>
     * Trimming is done by {@link #trimStrongReferences()} instead of by
     * overriding removeEldestEntry to be able to control waterfalling as easy
     * as possible
     */
    private LinkedBlockingQueue<ICacheElement<K, V>> strongReferences;

    /**
     * @see org.apache.commons.jcs3.engine.memory.AbstractMemoryCache#createMap()
     */
    @Override
    public ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new ConcurrentHashMap<>();
    }

    /**
     * This can't be implemented.
     *
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    @Override
    public int freeElements(final int numberToFree) throws IOException
    {
        return 0;
    }

    /**
     * @see org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet()
    {
        final Set<K> keys = new HashSet<>();
        for (final Map.Entry<K, MemoryElementDescriptor<K, V>> e : map.entrySet())
        {
            final SoftReferenceElementDescriptor<K, V> sred = (SoftReferenceElementDescriptor<K, V>) e.getValue();
            if (sred.getCacheElement() != null)
            {
                keys.add(e.getKey());
            }
        }

        return keys;
    }

    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    @Override
    public int getSize()
    {
        int size = 0;
        for (final MemoryElementDescriptor<K, V> me : map.values())
        {
            final SoftReferenceElementDescriptor<K, V> sred = (SoftReferenceElementDescriptor<K, V>) me;
            if (sred.getCacheElement() != null)
            {
                size++;
            }
        }
        return size;
    }

    /**
     * @return statistics about the cache
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();
        stats.setTypeName("Soft Reference Memory Cache");

        final List<IStatElement<?>> elems = stats.getStatElements();
        final int emptyrefs = map.size() - getSize();
        elems.add(new StatElement<>("Empty References", Integer.valueOf(emptyrefs)));
        elems.add(new StatElement<>("Strong References", Integer.valueOf(strongReferences.size())));

        return stats;
    }

    /**
     * For post reflection creation initialization
     *
     * @param hub
     */
    @Override
    public synchronized void initialize( final CompositeCache<K, V> hub )
    {
        super.initialize( hub );
        strongReferences = new LinkedBlockingQueue<>();
        log.info( "initialized Soft Reference Memory Cache for {0}",
                this::getCacheName );
    }

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    @Override
    protected void lockedGetElement(final MemoryElementDescriptor<K, V> me)
    {
        final ICacheElement<K, V> val = me.getCacheElement();
        val.getElementAttributes().setLastAccessTimeNow();

        // update the ordering of the strong references
        strongReferences.add(val);
        trimStrongReferences();
    }

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    @Override
    protected void lockedRemoveAll()
    {
        strongReferences.clear();
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
        strongReferences.remove(me.getCacheElement());
    }

    /**
     * Trim the number of strong references to equal or below the number given
     * by the maxObjects parameter.
     */
    private void trimStrongReferences()
    {
        final int max = getCacheAttributes().getMaxObjects();
        final int startsize = strongReferences.size();

        for (int cursize = startsize; cursize > max; cursize--)
        {
            final ICacheElement<K, V> ce = strongReferences.poll();
            waterfal(ce);
        }
    }

    /**
     * Puts an item to the cache.
     *
     * @param ce Description of the Parameter
     * @throws IOException Description of the Exception
     */
    @Override
    public void update(final ICacheElement<K, V> ce) throws IOException
    {
        putCnt.incrementAndGet();
        ce.getElementAttributes().setLastAccessTimeNow();

        lock.lock();

        try
        {
            map.put(ce.getKey(), new SoftReferenceElementDescriptor<>(ce));
            strongReferences.add(ce);
            trimStrongReferences();
        }
        finally
        {
            lock.unlock();
        }
    }
}
