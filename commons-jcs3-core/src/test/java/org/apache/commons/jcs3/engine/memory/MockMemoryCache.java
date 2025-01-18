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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;

/**
 * Mock implementation of a memory cache for testing things like the memory shrinker.
 */
public class MockMemoryCache<K, V>
    implements IMemoryCache<K, V>
{
    /** Config */
    private ICompositeCacheAttributes cacheAttr;

    /** Internal map */
    private final HashMap<K, ICacheElement<K, V>> map = new HashMap<>();

    /** The number of times waterfall was called. */
    public int waterfallCallCount;

    /** The number passed to the last call of free elements. */
    public int lastNumberOfFreedElements;

    /**
     * Destroy the memory cache
     * <p>
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        // nothing
    }

    /**
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    @Override
    public int freeElements( final int numberToFree )
        throws IOException
    {
        lastNumberOfFreedElements = numberToFree;
        return 0;
    }

    /**
     * @param key
     * @return (ICacheElement) map.get( key )
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final K key )
        throws IOException
    {
        return map.get( key );
    }

    /**
     * @return ICompositeCacheAttributes
     */
    @Override
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return cacheAttr;
    }

    /** @return null */
    @Override
    public CompositeCache<K, V> getCompositeCache()
    {
        return null;
    }

    /**
     * @param group
     * @return null
     */
    public Set<K> getGroupKeys( final String group )
    {
        return null;
    }

    /**
     * @return null
     */
    public Set<String> getGroupNames()
    {
        return null;
    }

    /**
     * @return map.keySet().toArray( */
    @Override
    public Set<K> getKeySet()
    {
        return new LinkedHashSet<>(map.keySet());
    }

    /**
     * @param keys
     * @return elements
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
        throws IOException
    {
        final Map<K, ICacheElement<K, V>> elements = new HashMap<>();

        if ( keys != null && !keys.isEmpty() )
        {
            for (final K key : keys) {
                final ICacheElement<K, V> element = get( key );

                if ( element != null )
                {
                    elements.put( key, element );
                }
            }
        }

        return elements;
    }

    /**
     * @param key
     * @return (ICacheElement) map.get( key )
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> getQuiet( final K key )
        throws IOException
    {
        return map.get( key );
    }

    /** @return size */
    @Override
    public int getSize()
    {
        return map.size();
    }

    /** @return stats */
    @Override
    public IStats getStatistics()
    {
        return null;
    }

    /**
     * Does nothing
     * @param cache
     */
    @Override
    public void initialize( final CompositeCache<K, V> cache )
    {
        // nothing
    }

    /**
     * @param key
     * @return map.remove( key ) != null
     * @throws IOException
     */
    @Override
    public boolean remove( final K key )
        throws IOException
    {
        return map.remove( key ) != null;
    }

    /**
     * @throws IOException
     */
    @Override
    public void removeAll()
        throws IOException
    {
        map.clear();
    }

    /**
     * @param cattr
     */
    @Override
    public void setCacheAttributes( final ICompositeCacheAttributes cattr )
    {
        this.cacheAttr = cattr;
    }

    /**
     * @param ce
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
        throws IOException
    {
        if ( ce != null )
        {
            map.put( ce.getKey(), ce );
        }
    }

    /**
     * @param ce
     * @throws IOException
     */
    @Override
    public void waterfal( final ICacheElement<K, V> ce )
        throws IOException
    {
        waterfallCallCount++;
    }
}
