package org.apache.commons.jcs3.engine.memory.behavior;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;

/** For the framework. Insures methods a MemoryCache needs to access. */
public interface IMemoryCache<K, V>
{
    /**
     * Destroy the memory cache
     *
     * @throws IOException
     */
    void dispose()
        throws IOException;

    /**
     * This instructs the memory cache to remove the <em>numberToFree</em>
     * according to its eviction policy. For example, the LRUMemoryCache will
     * remove the <em>numberToFree</em> least recently used items. These will be
     * spooled to disk if a disk auxiliary is available.
     *
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are
     *         only 3, you will get 3.
     * @throws IOException
     */
    int freeElements( int numberToFree )
        throws IOException;

    /**
     * Gets an item from the cache
     *
     * @param key
     *            Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException
     *                Description of the Exception
     */
    ICacheElement<K, V> get( K key )
        throws IOException;

    /**
     * Returns the CacheAttributes for the region.
     *
     * @return The cacheAttributes value
     */
    ICompositeCacheAttributes getCacheAttributes();

    /**
     * Gets the cache hub / region that uses the MemoryCache.
     *
     * @return The cache value
     */
    CompositeCache<K, V> getCompositeCache();

    /**
     * Gets a set of the keys for all elements in the memory cache.
     *
     * @return a set of the key type
     * TODO This should probably be done in chunks with a range passed in. This
     *       will be a problem if someone puts a 1,000,000 or so items in a
     *       region.
     */
    Set<K> getKeySet();

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map
     * if there is no data in cache for any of these keys
     * @throws IOException
     */
    Map<K, ICacheElement<K, V>> getMultiple( Set<K> keys )
        throws IOException;

    /**
     * Gets an item from the cache without effecting its order or last access
     * time
     *
     * @param key
     *            Description of the Parameter
     * @return The quiet value
     * @throws IOException
     *                Description of the Exception
     */
    ICacheElement<K, V> getQuiet( K key )
        throws IOException;

    /**
     * Gets the number of elements contained in the memory store
     *
     * @return Element count
     */
    int getSize();

    /**
     * Returns the historical and statistical data for a region's memory cache.
     *
     * @return Statistics and Info for the Memory Cache.
     */
    IStats getStatistics();

    /**
     * Initialize the memory cache
     *
     * @param cache The cache (region) this memory store is attached to.
     */
    void initialize( CompositeCache<K, V> cache );

    /**
     * Removes an item from the cache
     *
     * @param key
     *            Identifies item to be removed
     * @return Description of the Return Value
     * @throws IOException
     *                Description of the Exception
     */
    boolean remove( K key )
        throws IOException;

    /**
     * Removes all cached items from the cache.
     *
     * @throws IOException
     *                Description of the Exception
     */
    void removeAll()
        throws IOException;

    /**
     * Sets the CacheAttributes of the region.
     *
     * @param cattr
     *            The new cacheAttributes value
     */
    void setCacheAttributes( ICompositeCacheAttributes cattr );

    /**
     * Puts an item to the cache.
     *
     * @param ce
     *            Description of the Parameter
     * @throws IOException
     *                Description of the Exception
     */
    void update( ICacheElement<K, V> ce )
        throws IOException;

    /**
     * Spools the item contained in the provided element to disk
     *
     * @param ce
     *            Description of the Parameter
     * @throws IOException
     *                Description of the Exception
     */
    void waterfal( ICacheElement<K, V> ce ) // FIXME: Correct typo before 4.0, see JCS-222
        throws IOException;
}
