package org.apache.jcs.access.behavior;

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

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * ICacheAccess defines the behavior for client access.
 */
public interface ICacheAccess
{
    /**
     * Basic get method.
     * <p>
     * @param name
     * @return Object or null if not found.
     */
    Object get( Object name );

    /**
     * Puts in cache if an item does not exist with the name in that region.
     * <p>
     * @param name
     * @param obj
     * @throws CacheException
     */
    void putSafe( Object name, Object obj )
        throws CacheException;

    /**
     * Puts and/or overides an element with the name in that region.
     * <p>
     * @param name
     * @param obj
     * @throws CacheException
     */
    void put( Object name, Object obj )
        throws CacheException;

    /**
     * Description of the Method
     * <p>
     * @param name
     * @param obj
     * @param attr
     * @throws CacheException
     */
    void put( Object name, Object obj, IElementAttributes attr )
        throws CacheException;

    /**
     * Removes an item or all items. Should be called remove.
     * <p>
     * @throws CacheException
     * @deprecated
     * @see #remove
     */
    void destroy()
        throws CacheException;

    /**
     * Old remove all method.
     * @throws CacheException
     */
    void remove()
        throws CacheException;

    /**
     * The older removeall method.
     * <p>
     * @param name
     * @throws CacheException
     * @deprecated
     * @see #remove
     */
    void destroy( Object name )
        throws CacheException;

    /**
     * Remove an object for this key if one exists, else do nothing.
     * <p>
     * @param name
     * @throws CacheException
     */
    void remove( Object name )
        throws CacheException;

    /**
     * ResetAttributes allows for some of the attributes of a region to be reset
     * in particular expiration time attriubtes, time to live, default time to
     * live and idle time, and event handlers. The cacheloader object and
     * attributes set as flags can't be reset with resetAttributes, the object
     * must be destroyed and redefined to cache those parameters. Changing
     * default settings on groups and regions will not affect existing objects.
     * Only object loaded after the reset will use the new defaults. If no name
     * argument is provided, the reset is applied to the region.
     * <p>
     * @param attr
     * @throws CacheException
     */
    void resetElementAttributes( IElementAttributes attr )
        throws CacheException;

    /**
     * Reset the attributes on the object matching this key name.
     * <p>
     * @param name
     * @param attr
     * @throws CacheException
     */
    void resetElementAttributes( Object name, IElementAttributes attr )
        throws CacheException;

    /**
     * GetElementAttributes will return an attribute object describing the
     * current attributes associated with the object name. If no name parameter
     * is available, the attributes for the region will be returned. The name
     * object must override the Object.equals and Object.hashCode methods.
     * <p>
     * @return The elementAttributes value
     * @throws CacheException
     */
    IElementAttributes getElementAttributes()
        throws CacheException;

    /**
     * Gets the elementAttributes attribute of the ICacheAccess object
     * <p>
     * @param name
     * @return The elementAttributes value
     * @throws CacheException
     */
    IElementAttributes getElementAttributes( Object name )
        throws CacheException;

    /**
     * Gets the ICompositeCacheAttributes of the cache region
     * <p>
     * @return ICompositeCacheAttributes
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     * Sets the ICompositeCacheAttributes of the cache region
     * <p>
     * @param cattr
     *            The new ICompositeCacheAttribute value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i>
     * according to its eviction policy. For example, the LRUMemoryCache will
     * remove the <i>numberToFree</i> least recently used items. These will be
     * spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are
     *         only 3, you will get 3.
     * @throws CacheException
     */
    public int freeMemoryElements( int numberToFree )
        throws CacheException;
}
