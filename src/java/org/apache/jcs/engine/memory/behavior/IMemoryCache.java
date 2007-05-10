package org.apache.jcs.engine.memory.behavior;

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
import java.util.Iterator;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 */
public interface IMemoryCache
{
    /**
     * Initialize the memory cache
     * <p>
     * @param cache
     *            The cache (region) this memory store is attached to.
     */
    public void initialize( CompositeCache cache );

    /**
     * Destroy the memory cache
     * <p>
     * @throws IOException
     */
    public void dispose()
        throws IOException;

    /**
     * Get the number of elements contained in the memory store
     * <p>
     * @return Element count
     */
    public int getSize();

    /**
     * Returns the historical and statistical data for a region's memory cache.
     * <p>
     * @return Statistics and Infor for the Memory Cache.
     */
    public IStats getStatistics();

    /**
     * Get an iterator for all elements in the memory cache. This should be
     * removed since it is fairly dangerous. Other classes should not be able to
     * directly access items in the memory cache.
     * <p>
     * @return An iterator
     */
    public Iterator getIterator();

    /**
     * Get an Array of the keys for all elements in the memory cache.
     * <p>
     * @return Object[]
     * @TODO This should probably be done in chunks with a range pased in. This
     *       will be a problem if someone puts a 1,000,000 or so items in a
     *       region.
     */
    public Object[] getKeyArray();

    /**
     * Removes an item from the cache
     * <p>
     * @param key
     *            Identifies item to be removed
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public boolean remove( Serializable key )
        throws IOException;

    /**
     * Removes all cached items from the cache.
     * <p>
     * @exception IOException
     *                Description of the Exception
     */
    public void removeAll()
        throws IOException;

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i>
     * according to its eviction policy. For example, the LRUMemoryCache will
     * remove the <i>numberToFree</i> least recently used items. These will be
     * spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are
     *         only 3, you will get 3.
     * @throws IOException
     */
    public int freeElements( int numberToFree )
        throws IOException;

    /**
     * Get an item from the cache
     * <p>
     * @param key
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public ICacheElement get( Serializable key )
        throws IOException;

    /**
     * Get an item from the cache without effecting its order or last access
     * time
     * <p>
     * @param key
     *            Description of the Parameter
     * @return The quiet value
     * @exception IOException
     *                Description of the Exception
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException;

    /**
     * Spools the item contained in the provided element to disk
     * <p>
     * @param ce
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public void waterfal( ICacheElement ce )
        throws IOException;

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public void update( ICacheElement ce )
        throws IOException;

    /**
     * Returns the CacheAttributes for the region.
     * <p>
     * @return The cacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     * Sets the CacheAttributes of the region.
     * <p>
     * @param cattr
     *            The new cacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

    /**
     * Gets the cache hub / region that uses the MemoryCache.
     * <p>
     * @return The cache value
     */
    public CompositeCache getCompositeCache();

    /**
     * Gets the set of keys of objects currently in the group.
     * <p>
     * @param group
     * @return a Set of group keys.
     */
    public Set getGroupKeys( String group );

}
