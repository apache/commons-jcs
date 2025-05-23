package org.apache.commons.jcs3.access.behavior;

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

import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.stats.behavior.ICacheStats;

/**
 * ICacheAccessManagement defines the methods for cache management, cleanup and shutdown.
 */
public interface ICacheAccessManagement
{
    /**
     * Removes all of the elements from a region.
     *
     * @throws CacheException
     */
    void clear() throws CacheException;

    /**
     * Dispose this region. Flushes objects to and closes auxiliary caches. This is a shutdown
     * command!
     * <p>
     * To simply remove all elements from the region use clear().
     */
    void dispose();

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
    int freeMemoryElements( int numberToFree )
        throws CacheException;

    /**
     * Gets the ICompositeCacheAttributes of the cache region
     *
     * @return ICompositeCacheAttributes
     */
    ICompositeCacheAttributes getCacheAttributes();

    /**
     * GetElementAttributes will return an attribute object describing the current attributes
     * associated with the object name. If no name parameter is available, the attributes for the
     * region will be returned. The name object must override the Object.equals and Object.hashCode
     * methods.
     *
     * @return The elementAttributes value
     * @throws CacheException
     */
    IElementAttributes getDefaultElementAttributes()
        throws CacheException;

    /**
     * This returns the ICacheStats object with information on this region and its auxiliaries.
     * <p>
     * This data can be formatted as needed.
     *
     * @return ICacheStats
     */
    ICacheStats getStatistics();

    /**
     * @return A String version of the stats.
     */
    String getStats();

    /**
     * Sets the ICompositeCacheAttributes of the cache region
     *
     * @param cattr The new ICompositeCacheAttribute value
     */
    void setCacheAttributes( ICompositeCacheAttributes cattr );

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
    void setDefaultElementAttributes( IElementAttributes attr ) throws CacheException;
}
