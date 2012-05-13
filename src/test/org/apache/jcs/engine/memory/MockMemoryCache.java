package org.apache.jcs.engine.memory;

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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;
import org.apache.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Mock implementation of a memory cache for testing things like the memory shrinker.
 * <p>
 * @author Aaron Smuts
 */
public class MockMemoryCache<K extends Serializable, V extends Serializable>
    implements IMemoryCache<K, V>
{
    /** Config */
    private ICompositeCacheAttributes cacheAttr;

    /** Internal map */
    private final HashMap<K, ICacheElement<K, V>> map = new HashMap<K, ICacheElement<K, V>>();

    /** The number of times waterfall was called. */
    public int waterfallCallCount = 0;

    /** The number passed to the last call of free elements. */
    public int lastNumberOfFreedElements = 0;

    /**
     * Does nothing
     * @param cache
     */
    public void initialize( CompositeCache<K, V> cache )
    {
        // nothing
    }

    /**
     * Destroy the memory cache
     * <p>
     * @throws IOException
     */
    public void dispose()
        throws IOException
    {
        // nothing
    }

    /** @return size */
    public int getSize()
    {
        return map.size();
    }

    /** @return stats */
    public IStats getStatistics()
    {
        return null;
    }

    /** @return null */
    public Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> getIterator()
    {
        return null;
    }

    /**
     * @return map.keySet().toArray( */
    public Set<K> getKeySet()
    {
        return new LinkedHashSet<K>(map.keySet());
    }

    /**
     * @param key
     * @return map.remove( key ) != null
     * @throws IOException
     */
    public boolean remove( K key )
        throws IOException
    {
        return map.remove( key ) != null;
    }

    /**
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        map.clear();
    }

    /**
     * @param key
     * @return (ICacheElement) map.get( key )
     * @throws IOException
     */
    public ICacheElement<K, V> get( K key )
        throws IOException
    {
        return map.get( key );
    }

    /**
     * @param keys
     * @return elements
     * @throws IOException
     */
    public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
        throws IOException
    {
        Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

        if ( keys != null && !keys.isEmpty() )
        {
            Iterator<K> iterator = keys.iterator();

            while ( iterator.hasNext() )
            {
                K key = iterator.next();

                ICacheElement<K, V> element = get( key );

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
    public ICacheElement<K, V> getQuiet( K key )
        throws IOException
    {
        return map.get( key );
    }

    /**
     * @param ce
     * @throws IOException
     */
    public void waterfal( ICacheElement<K, V> ce )
        throws IOException
    {
        waterfallCallCount++;
    }

    /**
     * @param ce
     * @throws IOException
     */
    public void update( ICacheElement<K, V> ce )
        throws IOException
    {
        if ( ce != null )
        {
            map.put( ce.getKey(), ce );
        }
    }

    /**
     * @return ICompositeCacheAttributes
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return cacheAttr;
    }

    /**
     * @param cattr
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheAttr = cattr;
    }

    /** @return null */
    public CompositeCache<K, V> getCompositeCache()
    {
        return null;
    }

    /**
     * @param group
     * @return null
     */
    public Set<K> getGroupKeys( String group )
    {
        return null;
    }

    /**
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    public int freeElements( int numberToFree )
        throws IOException
    {
        lastNumberOfFreedElements = numberToFree;
        return 0;
    }
}
