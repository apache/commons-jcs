package org.apache.jcs.access.behavior;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 */
public interface ICacheAccess
{

    // Static methods that should be implemented
    //public static void defineRegion( String name, CacheAttributes cattr,
    //                                 Attributes attr )  throws CacheException;
    //public static void defineRegion( String name, CacheAttributes cattr )
    //    throws CacheException;
    //public static void defineRegion( String name )  throws CacheException;
    //public static CacheAccess getAccess( String region );


    /** Description of the Method */
    Object get( Object name );


    /**
     * Puts in cache if an item does not exist with the name in that region.
     */
    void putSafe( Object name, Object obj )
        throws CacheException;

    /** Puts and/or overides an element with the name in that region. */
    void put( Object name, Object obj )
        throws CacheException;


    /** Description of the Method */
    void put( Object name, Object obj, IElementAttributes attr )
        throws CacheException;


    /**
     * Removes an item or all items. Should be called remove.
     *
     * @deprecated
     * @see #remove
     */
    void destroy()
        throws CacheException;

    /** Description of the Method */
    void remove()
        throws CacheException;

    /**
     * Description of the Method
     *
     * @deprecated
     * @see #remove
     */
    void destroy( Object name )
        throws CacheException;

    /** Description of the Method */
    void remove( Object name )
        throws CacheException;

//    /**
//     *  Remove either the entire region of elements or the the specified element
//     *  from other caches specified in in the cache.properties file as lateral
//     *  caches.
//     */
//    void removeLateralDirect();
//    /**
//     *  Description of the Method
//     *
//     */
//    void removeLateralDirect( Serializable key );

    /**
     * ResetAttributes allows for some of the attributes of a region to be reset
     * in particular expiration time attriubtes, time to live, default time to
     * live and idle time, and event handlers. The cacheloader object and
     * attributes set as flags can't be reset with resetAttributes, the object
     * must be destroyed and redefined to cache those parameters. Changing
     * default settings on groups and regions will not affect existing objects.
     * Only object loaded after the reset will use the new defaults. If no name
     * argument is provided, the reset is applied to the region.
     */
    void resetElementAttributes( IElementAttributes attr )
        throws CacheException;


    /** Description of the Method */
    void resetElementAttributes( Object name, IElementAttributes attr )
        throws CacheException;


    /**
     * GetElementAttributes will return an attribute object describing the current
     * attributes associated with the object name. If no name parameter is
     * available, the attributes for the region will be returned. The name
     * object must override the Object.equals and Object.hashCode methods.
     *
     * @return The elementAttributes value
     */
    IElementAttributes getElementAttributes()
        throws CacheException;


    /**
     * Gets the elementAttributes attribute of the ICacheAccess object
     *
     * @return The elementAttributes value
     */
    IElementAttributes getElementAttributes( Object name )
        throws CacheException;


    /**
     * Gets the ICompositeCacheAttributes of the cache region
     *
     * @return
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     * Sets the ICompositeCacheAttributes of the cache region
     *
     * @param cattr The new ICompositeCacheAttribute value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

}
// end interface
