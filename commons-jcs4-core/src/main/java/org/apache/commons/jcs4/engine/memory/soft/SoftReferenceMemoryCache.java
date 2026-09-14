package org.apache.commons.jcs4.engine.memory.soft;

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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs4.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs4.engine.memory.util.SoftReferenceElementDescriptor;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;

/**
 * A JCS IMemoryCache that has {@link SoftReference} to all its values.
 * This cache does not respect {@link ICompositeCacheAttributes#MaxObjects()}
 * as overflowing is handled by Java GC.
 * <p>
 * The cache also has strong references to a maximum number of objects given by
 * the maxObjects parameter
 * </p>
 */
public class SoftReferenceMemoryCache<K, V> extends AbstractMemoryCache<K, V>
{
    static
    {
        cacheImplementationName = "SoftReference Memory Cache";
    }

    /**
     * Strong references to the maxObjects number of newest objects.
     * <p>
     * Trimming is done by {@link #trimStrongReferences()} instead of by
     * overriding removeEldestEntry to be able to control waterfalling as easy
     * as possible
     */
    private LinkedBlockingQueue<ICacheElement<K, V>> strongReferences;

    /**
     * @see org.apache.commons.jcs4.engine.memory.AbstractMemoryCache#createMap()
     */
    @Override
    protected ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new ConcurrentHashMap<>();
    }

    /**
     * @see org.apache.commons.jcs4.engine.memory.behavior.IMemoryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet()
    {
        return getMapView().entrySet().stream()
                .filter(e -> e.getValue().getCacheElement() != null)
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    @Override
    public int getSize()
    {
        long size = getMapView().values().stream()
                .filter(v -> v.getCacheElement() != null)
                .count();

        return (int) size;
    }

    /**
     * @return statistics about the cache
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();

        final int emptyrefs = super.getSize() - getSize();
        stats.addStatElement("Empty References", Integer.valueOf(emptyrefs));
        stats.addStatElement("Strong References", Integer.valueOf(strongReferences.size()));

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
        strongReferences = new LinkedBlockingQueue<>();
        super.initialize( hub );
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
        return new SoftReferenceElementDescriptor<>(ce);
    }

    /**
     * Update control structures after get
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void adjustGetElement(final MemoryElementDescriptor<K, V> me)
    {
        final ICacheElement<K, V> val = me.getCacheElement();

        // update the ordering of the strong references
        strongReferences.add(val);
        trimStrongReferences();
    }

    /**
     * Update control structures after update
     * (guarded by the lock)
     *
     * @param newNode The memory element descriptor of the current cache element
     */
    @Override
    protected void adjustUpdateElement(MemoryElementDescriptor<K, V> newNode)
    {
        final ICacheElement<K, V> val = newNode.getCacheElement();

        // update the ordering of the strong references
        strongReferences.add(val);
        trimStrongReferences();
    }

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    @Override
    protected void adjustRemoveAll()
    {
        strongReferences.clear();
    }

    /**
     * Remove element from control structure
     *
     * @param me The memory element descriptor
     */
    @Override
    protected void adjustRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        strongReferences.remove(me.getCacheElement());
    }

    /**
     * Cannot be implemented
     *
     * @return ICacheElement&lt;K, V&gt; if there was a last element, else null.
     * @throws IOException
     */
    @Override
    protected ICacheElement<K, V> freeElement() throws IOException
    {
        return null;
    }

    /**
     * Trim the number of strong references to equal or below the number given
     * by the maxObjects parameter.
     */
    private void trimStrongReferences()
    {
        final int max = getCacheAttributes().MaxObjects();
        final int startsize = strongReferences.size();

        if (max > 0)
        {
            for (int cursize = startsize; cursize > max; cursize--)
            {
                final ICacheElement<K, V> ce = strongReferences.poll();
                if (ce != null)
                {
                    waterfall(ce);
                }
                else
                {
                    break;
                }
            }
        }
    }
}
