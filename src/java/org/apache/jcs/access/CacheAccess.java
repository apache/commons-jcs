package org.apache.jcs.access;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.behavior.ICacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.apache.jcs.access.exception.InvalidHandleException;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.stats.behavior.ICacheStats;

/**
 * This class provides an interface for all types of access to the cache.
 * <p>
 * An instance of this class is tied to a specific cache region. Static methods are provided to get
 * such instances.
 * <p>
 * Using this class you can retrieve an item, the items wrapper, the element configuration, put an
 * item in the cache, remove an item, and clear a region.
 * <p>
 * The JCS class is the prefered way to access these methods.
 */
public class CacheAccess
    implements ICacheAccess
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( CacheAccess.class );

    /**
     * Cache manager use by the various forms of defineRegion and getAccess
     */
    private static CompositeCacheManager cacheMgr;

    /**
     * The cache that a given instance of this class provides access to.
     * <p>
     * @TODO Should this be the inteface?
     */
    protected CompositeCache cacheControl;

    /**
     * Constructor for the CacheAccess object.
     * <p>
     * @param cacheControl The cache which the created instance accesses
     */
    public CacheAccess( CompositeCache cacheControl )
    {
        this.cacheControl = cacheControl;
    }

    // ----------------------------- static methods for access to cache regions

    /**
     * Define a new cache region with the given name. In the oracle specification, these attributes
     * are global and not region specific, regional overirdes is a value add each region should be
     * able to house both cache and element attribute sets. It is more efficient to define a cache
     * in the props file and then strictly use the get access method. Use of the define region
     * outside of an initialization block should be avoided.
     * <p>
     * @param name Name that will identify the region
     * @return CacheAccess instance for the new region
     * @exception CacheException
     */
    public static CacheAccess defineRegion( String name )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( name ) );
    }

    /**
     * Define a new cache region with the specified name and attributes.
     * <p>
     * @param name Name that will identify the region
     * @param cattr CompositeCacheAttributes for the region
     * @return CacheAccess instance for the new region
     * @exception CacheException
     */
    public static CacheAccess defineRegion( String name, ICompositeCacheAttributes cattr )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( name, cattr ) );
    }

    /**
     * Define a new cache region with the specified name and attributes and return a CacheAccess to
     * it.
     * <p>
     * @param name Name that will identify the region
     * @param cattr CompositeCacheAttributes for the region
     * @param attr Attributes for the region
     * @return CacheAccess instance for the new region
     * @exception CacheException
     */
    public static CacheAccess defineRegion( String name, ICompositeCacheAttributes cattr, IElementAttributes attr )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( name, cattr, attr ) );
    }

    /**
     * Get a CacheAccess instance for the given region.
     * <p>
     * @param region Name that identifies the region
     * @return CacheAccess instance for region
     * @exception CacheException
     */
    public static CacheAccess getAccess( String region )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( region ) );
    }

    /**
     * Get a CacheAccess instance for the given region with the given attributes.
     * <p>
     * @param region Name that identifies the region
     * @param icca
     * @return CacheAccess instance for region
     * @exception CacheException
     */
    public static CacheAccess getAccess( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( region, icca ) );
    }

    /**
     * Helper method which checks to make sure the cacheMgr class field is set, and if not requests
     * an instance from CacheManagerFactory.
     */
    protected static void ensureCacheManager()
    {
        synchronized ( CacheAccess.class )
        {
            if ( cacheMgr == null )
            {
                cacheMgr = CompositeCacheManager.getInstance();
            }
        }
    }

    // ------------------------------------------------------- instance methods

    /**
     * Retrieve an object from the cache region this instance provides access to.
     * <p>
     * @param name Key the object is stored as
     * @return The object if found or null
     */
    public Object get( Object name )
    {
        ICacheElement element = this.cacheControl.get( (Serializable) name );

        return ( element != null ) ? element.getVal() : null;
    }

    /**
     * This method returns the ICacheElement wrapper which provides access to element info and other
     * attributes.
     * <p>
     * This returns a reference to the wrapper. Any modifications will be reflected in the cache. No
     * defensive copy is made.
     * <p>
     * This method is most useful if you want to determine things such as the how long the element
     * has been in the cache.
     * <p>
     * The last access time in teh ElementAttributes should be current.
     * <p>
     * @param name Key the object is stored as
     * @return The ICacheElement if the object is found or null
     */
    public ICacheElement getCacheElement( Object name )
    {
        return this.cacheControl.get( (Serializable) name );
    }

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object
     * associated with name in the region an ObjectExistsException is thrown. Names are scoped to a
     * region so they must be unique within the region they are placed.
     * <p>
     * @param key Key object will be stored with
     * @param value Object to store
     * @exception CacheException and ObjectExistsException is thrown if the item is already in the
     *                cache.
     */
    public void putSafe( Object key, Object value )
        throws CacheException
    {
        if ( this.cacheControl.get( (Serializable) key ) != null )
        {
            throw new ObjectExistsException( "putSafe failed.  Object exists in the cache for key [" + key
                + "].  Remove first or use a non-safe put to override the value." );
        }
        put( key, value );
    }

    /**
     * Place a new object in the cache, associated with key name. If there is currently an object
     * associated with name in the region it is replaced. Names are scoped to a region so they must
     * be unique within the region they are placed. ObjectExistsException
     * @param name Key object will be stored with
     * @param obj Object to store
     * @exception CacheException
     */
    public void put( Object name, Object obj )
        throws CacheException
    {
        // Call put with a copy of the contained caches default attributes.
        // the attributes are copied by the cacheControl
        put( name, obj, this.cacheControl.getElementAttributes() );
    }

    /**
     * Constructs a cache element with these attribures, and puts it into the cache.
     * <p>
     * If the key or the value is null, and InvalidArgumentException is thrown.
     * <p>
     * @see org.apache.jcs.access.behavior.ICacheAccess#put(java.lang.Object, java.lang.Object,
     *      org.apache.jcs.engine.behavior.IElementAttributes)
     */
    public void put( Object key, Object val, IElementAttributes attr )
        throws CacheException
    {
        if ( key == null )
        {
            throw new InvalidArgumentException( "Key must not be null" );
        }
        else if ( val == null )
        {
            throw new InvalidArgumentException( "Value must not be null" );
        }

        // Create the element and update. This may throw an IOException which
        // should be wrapped by cache access.
        try
        {
            CacheElement ce = new CacheElement( this.cacheControl.getCacheName(), (Serializable) key,
                                                (Serializable) val );

            ce.setElementAttributes( attr );

            this.cacheControl.update( ce );
        }
        catch ( Exception e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Destory the region and all objects within it. After calling this method, the Cache object can
     * no longer be used as it will be closed.
     * <p>
     * @exception CacheException
     * @deprecated
     */
    public void destroy()
        throws CacheException
    {
        try
        {
            this.cacheControl.removeAll();
        }
        catch ( IOException e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Removes all of the elements from a region.
     * <p>
     * @deprecated use clear()
     * @throws CacheException
     */
    public void remove()
        throws CacheException
    {
        clear();
    }

    /**
     * Removes all of the elements from a region.
     * <p>
     * @throws CacheException
     */
    public void clear()
        throws CacheException
    {
        try
        {
            this.cacheControl.removeAll();
        }
        catch ( IOException e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Invalidate all objects associated with key name, removing all references to the objects from
     * the cache.
     * <p>
     * @param name Key that specifies object to invalidate
     * @exception CacheException
     * @deprecated use remove
     */
    public void destroy( Object name )
        throws CacheException
    {
        this.cacheControl.remove( (Serializable) name );
    }

    /**
     * Removes a single item by name.
     * <p>
     * @param name the name of the item to remove.
     * @throws CacheException
     */
    public void remove( Object name )
        throws CacheException
    {
        this.cacheControl.remove( (Serializable) name );
    }

    /**
     * ResetAttributes allows for some of the attributes of a region to be reset in particular
     * expiration time attriubtes, time to live, default time to live and idle time, and event
     * handlers. Changing default settings on groups and regions will not affect existing objects.
     * Only object loaded after the reset will use the new defaults. If no name argument is
     * provided, the reset is applied to the region.
     * <p>
     * NOTE: this method is does not reset the attributes for items already in the cache. It could
     * potentially do this for items in memory, and maybe on disk (which would be slow) but not
     * remote items. Rather than have unpredicatble behavior, this method just sets the default
     * attributes.
     * <p>
     * TODO is should be renamed "setDefaultElementAttributes"
     * <p>
     * @deprecated As of release 1.3
     * @see setDefaultElementAttributes
     * @param attr New attributes for this region.
     * @exception CacheException
     * @exception InvalidHandleException
     */
    public void resetElementAttributes( IElementAttributes attr )
        throws CacheException, InvalidHandleException
    {
        this.cacheControl.setElementAttributes( attr );
    }

    /**
     * This method is does not reset the attributes for items already in the cache. It could
     * potentially do this for items in memory, and maybe on disk (which would be slow) but not
     * remote items. Rather than have unpredicatble behavior, this method just sets the default
     * attributes. Items subsequently put into the cache will use these defaults if they do not
     * specify specific attributes.
     * <p>
     * @param attr the default attributes.
     * @throws CacheException if something goes wrong.
     */
    public void setDefaultElementAttributes( IElementAttributes attr )
        throws CacheException
    {
        this.cacheControl.setElementAttributes( attr );
    }

    /**
     * Reset attributes for a particular element in the cache. NOTE: this method is currently not
     * implemented.
     * <p>
     * @param name Key of object to reset attributes for
     * @param attr New attributes for the object
     * @exception CacheException
     * @exception InvalidHandleException if the item does not exist.
     */
    public void resetElementAttributes( Object name, IElementAttributes attr )
        throws CacheException, InvalidHandleException
    {
        ICacheElement element = this.cacheControl.get( (Serializable) name );
        if ( element == null )
        {
            throw new InvalidHandleException( "Object for name [" + name + "] is not in the cache" );
        }

        // Although it will work currently, don't assume pass by reference here,
        // i.e. don't do this:
        // element.setElementAttributes( attr );
        // Another reason to call put is to force the changes to be distributed.
        put( element.getKey(), element.getVal(), attr );

    }

    /**
     * GetElementAttributes will return an attribute object describing the current attributes
     * associated with the object name.
     * <p>
     * This was confusing, so I created a new method with a clear name.
     * <p>
     * @deprecated As of release 1.3
     * @see getDefaultElementAttributes
     * @return Attributes for this region
     * @exception CacheException
     */
    public IElementAttributes getElementAttributes()
        throws CacheException
    {
        return this.cacheControl.getElementAttributes();
    }

    /**
     * Retrieves A COPY OF the default element attributes used by this region. This does not provide
     * a reference to the element attributes.
     * <p>
     * Each time an element is added to the cache without element attributes, the default element
     * attributes are cloned.
     * <p>
     * @return the deafualt element attributes used by this region.
     * @throws CacheException
     */
    public IElementAttributes getDefaultElementAttributes()
        throws CacheException
    {
        return this.cacheControl.getElementAttributes();
    }

    /**
     * GetElementAttributes will return an attribute object describing the current attributes
     * associated with the object name. The name object must override the Object.equals and
     * Object.hashCode methods.
     * <p>
     * @param name Key of object to get attributes for
     * @return Attributes for the object, null if object not in cache
     * @exception CacheException
     */
    public IElementAttributes getElementAttributes( Object name )
        throws CacheException
    {
        IElementAttributes attr = null;

        try
        {
            attr = this.cacheControl.getElementAttributes( (Serializable) name );
        }
        catch ( IOException ioe )
        {
            log.error( "Failure getting element attributes", ioe );
        }

        return attr;
    }

    /**
     * This returns the ICacheStats object with information on this region and its auxiliaries.
     * <p>
     * This data can be formatted as needed.
     * <p>
     * @return ICacheStats
     */
    public ICacheStats getStatistics()
    {
        return this.cacheControl.getStatistics();
    }

    /**
     * @return A String version of the stats.
     */
    public String getStats()
    {
        return this.cacheControl.getStats();
    }

    /**
     * Dispose this region. Flushes objects to and closes auxiliary caches. This is a shutdown
     * command!
     * <p>
     * To simply remove all elements from the region use clear().
     */
    public void dispose()
    {
        this.cacheControl.dispose();
    }

    /**
     * Gets the ICompositeCacheAttributes of the cache region.
     * <p>
     * @return ICompositeCacheAttributes, the controllers config info, defined in the top section of
     *         a region definition.
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheControl.getCacheAttributes();
    }

    /**
     * Sets the ICompositeCacheAttributes of the cache region.
     * <p>
     * @param cattr The new ICompositeCacheAttribute value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheControl.setCacheAttributes( cattr );
    }

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <i>numberToFree</i> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     * @throws CacheException
     */
    public int freeMemoryElements( int numberToFree )
        throws CacheException
    {
        int numFreed = -1;
        try
        {
            numFreed = this.cacheControl.getMemoryCache().freeElements( numberToFree );
        }
        catch ( IOException ioe )
        {
            String message = "Failure freeing memory elements.  ";
            log.error( message, ioe );
            throw new CacheException( message + ioe.getMessage() );
        }
        return numFreed;
    }
}
