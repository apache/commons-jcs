package org.apache.commons.jcs3.access;

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

import org.apache.commons.jcs3.access.behavior.ICacheAccessManagement;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.stats.behavior.ICacheStats;

/**
 * This class provides the common methods for all types of access to the cache.
 * <p>
 * An instance of this class is tied to a specific cache region. Static methods are provided to get
 * such instances.
 * <p>
 * Using this class you can retrieve an item, the item's wrapper, and the element's configuration.  You can also put an
 * item in the cache, remove an item, and clear a region.
 * <p>
 * The JCS class is the preferred way to access these methods.
 */
public abstract class AbstractCacheAccess<K, V>
    implements ICacheAccessManagement
{
    /**
     * The cache that a given instance of this class provides access to.
     * <p>
     * TODO Should this be the interface?
     */
    private final CompositeCache<K, V> cacheControl;

    /**
     * Constructor for the CacheAccess object.
     *
     * @param cacheControl The cache which the created instance accesses
     */
    protected AbstractCacheAccess( final CompositeCache<K, V> cacheControl )
    {
        this.cacheControl = cacheControl;
    }

    /**
     * Removes all of the elements from a region.
     *
     * @throws CacheException
     */
    @Override
    public void clear()
        throws CacheException
    {
        try
        {
            this.getCacheControl().removeAll();
        }
        catch ( final IOException e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Dispose this region. Flushes objects to and closes auxiliary caches. This is a shutdown
     * command!
     * <p>
     * To simply remove all elements from the region use clear().
     */
    @Override
    public void dispose()
    {
        this.getCacheControl().dispose();
    }

    /**
     * This instructs the memory cache to remove the <em>numberToFree</em> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <em>numberToFree</em> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     *
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     * @throws CacheException
     */
    @Override
    public int freeMemoryElements( final int numberToFree )
        throws CacheException
    {
        int numFreed = -1;
        try
        {
            numFreed = this.getCacheControl().getMemoryCache().freeElements( numberToFree );
        }
        catch ( final IOException ioe )
        {
            final String message = "Failure freeing memory elements.";
            throw new CacheException( message, ioe );
        }
        return numFreed;
    }

    /**
     * Gets the ICompositeCacheAttributes of the cache region.
     *
     * @return ICompositeCacheAttributes, the controllers config info, defined in the top section of
     *         a region definition.
     */
    @Override
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.getCacheControl().getCacheAttributes();
    }

    public CompositeCache<K, V> getCacheControl() {
        return cacheControl;
    }

    /**
     * Gets a copy of  the default element attributes used by this region. This does not provide
     * a reference to the element attributes.
     * <p>
     * Each time an element is added to the cache without element attributes, the default element
     * attributes are cloned.
     *
     * @return the default element attributes used by this region.
     * @throws CacheException
     */
    @Override
    public IElementAttributes getDefaultElementAttributes()
        throws CacheException
    {
        return this.getCacheControl().getElementAttributes();
    }

    /**
     * This returns the ICacheStats object with information on this region and its auxiliaries.
     * <p>
     * This data can be formatted as needed.
     *
     * @return ICacheStats
     */
    @Override
    public ICacheStats getStatistics()
    {
        return this.getCacheControl().getStatistics();
    }

    /**
     * @return A String version of the stats.
     */
    @Override
    public String getStats()
    {
        return this.getCacheControl().getStats();
    }

    /**
     * Sets the ICompositeCacheAttributes of the cache region.
     *
     * @param cattr The new ICompositeCacheAttribute value
     */
    @Override
    public void setCacheAttributes( final ICompositeCacheAttributes cattr )
    {
        this.getCacheControl().setCacheAttributes( cattr );
    }

    /**
     * This method is does not reset the attributes for items already in the cache. It could
     * potentially do this for items in memory, and maybe on disk (which would be slow) but not
     * remote items. Rather than have unpredictable behavior, this method just sets the default
     * attributes. Items subsequently put into the cache will use these defaults if they do not
     * specify specific attributes.
     *
     * @param attr the default attributes.
     * @throws CacheException if something goes wrong.
     */
    @Override
    public void setDefaultElementAttributes( final IElementAttributes attr )
        throws CacheException
    {
        this.getCacheControl().setElementAttributes( attr );
    }

}
