package org.apache.commons.jcs.engine.memory.lru;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs.engine.CacheConstants;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.group.GroupAttrName;
import org.apache.commons.jcs.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs.engine.stats.behavior.IStats;
import org.apache.commons.jcs.utils.clhm.ConcurrentLinkedHashMap;
import org.apache.commons.jcs.utils.clhm.EvictionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a memory manager using Ben Manes ConcurrentLinkedHashMap.
 */
public class LHMLRUMemoryCache<K, V>
    extends AbstractMemoryCache<K, V>
{
    /** The Logger. */
    private static final Log log = LogFactory.getLog( LRUMemoryCache.class );

    /**
     * For post reflection creation initialization
     * <p>
     * @param hub
     */
    @Override
    public void initialize( CompositeCache<K, V> hub )
    {
        super.initialize( hub );
        log.info( "initialized LHMLRUMemoryCache for " + getCacheName() );
    }

    /**
     * Returns a ConcurrentLinkedHashMap
     * <p>
     * @return a ConcurrentLinkedHashMap
     */
    @Override
    public ConcurrentMap<K, MemoryElementDescriptor<K, V>> createMap()
    {
        EvictionListener<K, MemoryElementDescriptor<K, V>> listener = new EvictionListener<K, MemoryElementDescriptor<K, V>>()
        {
            @Override public void onEviction(K key, MemoryElementDescriptor<K, V> value)
            {
                ICacheElement<K, V> element = value.getCacheElement();

                if ( log.isDebugEnabled() )
                {
                    log.debug( "LHMLRU max size: " + getCacheAttributes().getMaxObjects()
                        + ".  Spooling element, key: " + key );
                }

                waterfal( element );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "LHMLRU size: " + map.size() );
                }
            }
        };

        ConcurrentMap<K, MemoryElementDescriptor<K, V>> map =
                new ConcurrentLinkedHashMap.Builder<K, MemoryElementDescriptor<K,V>>()
                .maximumWeightedCapacity(getCacheAttributes().getMaxObjects())
                .listener(listener)
                .build();

        return map;
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce Description of the Parameter
     * @throws IOException
     */
    @Override
    public void update( ICacheElement<K, V> ce )
        throws IOException
    {
        putCnt.incrementAndGet();
        map.put( ce.getKey(), new MemoryElementDescriptor<K, V>(ce) );
    }

    /**
     * Get an item from the cache
     * <p>
     * @param key Identifies item to find
     * @return ICacheElement&lt;K, V&gt; if found, else null
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( K key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item from cache " + getCacheName() + " for key " + key );
        }

        MemoryElementDescriptor<K, V> me = map.get( key );

        if ( me != null )
        {
            hitCnt.incrementAndGet();
            if ( log.isDebugEnabled() )
            {
                log.debug( getCacheName() + ": LHMLRUMemoryCache hit for " + key );
            }
            return me.getCacheElement();
        }
        else
        {
            missCnt.incrementAndGet();
            if ( log.isDebugEnabled() )
            {
                log.debug( getCacheName() + ": LHMLRUMemoryCache miss for " + key );
            }
        }

        return null;
    }

    /**
     * Removes an item from the cache. This method handles hierarchical removal. If the key is a
     * String and ends with the CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     * starting with the argument String will be removed.
     * <p>
     * @param key
     * @return true if removed
     * @throws IOException
     */
    @Override
    public boolean remove( K key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing item for key: " + key );
        }

        boolean removed = false;

        // handle partial removal
        if ( key instanceof String && ( (String) key ).endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext(); )
            {
                Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
                K k = entry.getKey();

                if ( k instanceof String && ( (String) k ).startsWith( key.toString() ) )
                {
                    itr.remove();
                    removed = true;
                }
            }
        }
        else if ( key instanceof GroupAttrName && ((GroupAttrName<?>)key).attrName == null )
        {
            // remove all keys of the same name hierarchy.
            for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext(); )
            {
                Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
                K k = entry.getKey();

                if ( k instanceof GroupAttrName &&
                    ((GroupAttrName<?>)k).groupId.equals(((GroupAttrName<?>)key).groupId) )
                {
                    itr.remove();
                    removed = true;
                }
            }
        }
        else
        {
            // remove single item.
            MemoryElementDescriptor<K, V> me = map.remove( key );
            if ( me != null )
            {
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Get an Array of the keys for all elements in the memory cache
     * <p>
     * @return An Object[]
     */
    @Override
    public Set<K> getKeySet()
    {
        return new LinkedHashSet<K>(map.keySet());
    }

    /**
     * This returns semi-structured information on the memory cache, such as the size, put count,
     * hit count, and miss count.
     * <p>
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        IStats stats = super.getStatistics();
        stats.setTypeName( "LHMLRU Memory Cache" );

        return stats;
    }

    // ---------------------------------------------------------- debug methods

    /**
     * Dump the cache entries from first to last for debugging.
     */
    public void dumpCacheEntries()
    {
        dumpMap();
    }

    /**
     * This can't be implemented.
     * <p>
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    @Override
    public int freeElements( int numberToFree )
        throws IOException
    {
        // can't be implemented using the LHM
        return 0;
    }
}
