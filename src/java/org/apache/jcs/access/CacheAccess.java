package org.apache.jcs.access;


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


import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.access.behavior.ICacheAccess;

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.InvalidHandleException;
import org.apache.jcs.access.exception.ObjectExistsException;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;

import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which provides interface for all access to the cache. An instance of
 * this class is tied to a specific cache region. Static methods are provided to
 * get such instances.
 *
 * @version $Id$
 */
public class CacheAccess implements ICacheAccess
{
    private static final Log log =
        LogFactory.getLog( CacheAccess.class );

    /**
     * Cache manager use by the various forms of defineRegion and getAccess
     */
    private static CompositeCacheManager cacheMgr;

    /**
     * Cache that a given instance of this class provides access to. Should this
     * be the inteface?
     */
    protected CompositeCache cacheControl;

    /**
     * Constructor for the CacheAccess object.
     *
     * @param cacheControl The cache which the created instance accesses
     */
    public CacheAccess( CompositeCache cacheControl )
    {
        this.cacheControl = cacheControl;
    }

    // ----------------------------- static methods for access to cache regions

    /**
     * Define a new cache region with the given name. In the oracle
     * specification, these attributes are global and not region specific,
     * regional overirdes is a value add each region should be able to house
     * both cache and element attribute sets. It is more efficient to define a
     * cache in the props file and then strictly use the get access method. Use
     * of the define region outside of an initialization block should be
     * avoided.
     *
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
     *
     * @param name Name that will identify the region
     * @param cattr CompositeCacheAttributes for the region
     * @return CacheAccess instance for the new region
     * @exception CacheException
     */
    public static CacheAccess defineRegion( String name,
                                            CompositeCacheAttributes cattr )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( name, cattr ) );
    }

    /**
     * Define a new cache region with the specified name and attributes and
     * return a CacheAccess to it.
     *
     * @param name Name that will identify the region
     * @param cattr CompositeCacheAttributes for the region
     * @param attr Attributes for the region
     * @return CacheAccess instance for the new region
     * @exception CacheException
     */
    public static CacheAccess defineRegion( String name,
                                            CompositeCacheAttributes cattr,
                                            IElementAttributes attr )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess(
            cacheMgr.getCache( name, cattr, attr ) );
    }

    /**
     * Get a CacheAccess instance for the given region.
     *
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
     * Get a CacheAccess instance for the given region with the given
     * attributes.
     *
     * @param region Name that identifies the region
     * @param icca
     * @return CacheAccess instance for region
     * @exception CacheException
     */
    public static CacheAccess getAccess( String region,
                                         ICompositeCacheAttributes icca )
        throws CacheException
    {
        ensureCacheManager();

        return new CacheAccess( cacheMgr.getCache( region, icca ) );
    }

    /**
     * Helper method which checks to make sure the cacheMgr class field is set,
     * and if not requests an instance from CacheManagerFactory.
     */
    protected static void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            synchronized ( CacheAccess.class )
            {
                if ( cacheMgr == null )
                {
                    cacheMgr = CompositeCacheManager.getInstance();
                }
            }
        }
    }

    // ------------------------------------------------------- instance methods

    /**
     * Retrieve an object from the cache region this instance provides access
     * to.
     *
     * @param name Key the object is stored as
     * @return The object if found or null
     */
    public Object get( Object name )
    {
        ICacheElement element = cacheControl.get( ( Serializable ) name );

        return ( element != null ) ? element.getVal() : null;
    }

    /**
     * Place a new object in the cache, associated with key name. If there is
     * currently an object associated with name in the region an
     * ObjectExistsException is thrown. Names are scoped to a region so they
     * must be unique within the region they are placed.
     *
     * @param key Key object will be stored with
     * @param value Object to store
     * @exception CacheException
     */
    public void putSafe( Object key, Object value )
        throws CacheException
    {
        if ( cacheControl.get( ( Serializable ) key ) != null )
        {
            throw new ObjectExistsException( "Object exists for key " + key );
        }
        put( key, value );
    }

    /**
     * Place a new object in the cache, associated with key name. If there is
     * currently an object associated with name in the region it is replaced.
     * Names are scoped to a region so they must be unique within the region
     * they are placed.
     *
     * @param name Key object will be stored with
     * @param obj Object to store
     * @exception CacheException
     */
    public void put( Object name, Object obj )
        throws CacheException
    {
        // Call put with a copy of the contained caches default attributes.

        put( name,
             obj,
             cacheControl.getElementAttributes().copy() );
    }

    /**
     * Place a new object in the cache. This form allows attributes to associate
     * with the object may be specified with attr.
     */
    public void put( Object key, Object val, IElementAttributes attr )
        throws CacheException
    {
        if ( key == null  )
        {
            throw new CacheException( "Key must not be null" );
        }
        else if ( val == null )
        {
            throw new CacheException( "Value must not be null" );
        }

        // Create the element and update. This may throw an IOException which
        // should be wrapped by cache access.

        try
        {
            CacheElement ce = new CacheElement( cacheControl.getCacheName(),
                              (Serializable) key,
                              (Serializable) val );

            ce.setElementAttributes( attr );

            cacheControl.update( ce );
        }
        catch ( Exception e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Destory the region and all objects within it. After calling this method,
     * the Cache object can no longer be used as it will be closed.
     *
     * @exception CacheException
     *
     * @deprecated
     */
    public void destroy()
        throws CacheException
    {
        try
        {
            cacheControl.removeAll();
        }
        catch ( IOException e )
        {
            throw new CacheException( e );
        }
    }

    /** Description of the Method */
    public void remove()
        throws CacheException
    {
        try
        {
            cacheControl.removeAll();
        }
        catch ( IOException e )
        {
            throw new CacheException( e );
        }
    }

    /**
     * Invalidate all objects associated with key name, removing all references
     * to the objects from the cache.
     *
     * @param name Key that specifies object to invalidate
     * @exception CacheException
     *
     * @deprecated  use remove
     */
    public void destroy( Object name )
        throws CacheException
    {
        cacheControl.remove( ( Serializable ) name );
    }

    /** Description of the Method */
    public void remove( Object name )
        throws CacheException
    {
        cacheControl.remove( ( Serializable ) name );
    }


    // TODO: rethink the point of these methods
//    /**
//     * Remove the entire region of elements from other caches specified in the
//     * cache.properties file as lateral caches.
//     */
//    public void removeLateralDirect()
//    {
//        cacheControl.removeLateralDirect( "ALL" );
//    }
//    /**
//     * Remove the specified element from other caches specified in in the
//     * cache.properties file as lateral caches.
//     *
//     * @param key Key identifying object to remove
//     */
//    public void removeLateralDirect( Serializable key )
//    {
//        cacheControl.removeLateralDirect( key );
//    }

    /**
     * If there are any auxiliary caches associated with this cache, save all
     * objects to them.
     */
    public void save()
    {
        cacheControl.save();
    }

    /**
     * ResetAttributes allows for some of the attributes of a region to be reset
     * in particular expiration time attriubtes, time to live, default time to
     * live and idle time, and event handlers. Changing default settings on
     * groups and regions will not affect existing objects. Only object loaded
     * after the reset will use the new defaults. If no name argument is
     * provided, the reset is applied to the region. NOTE: this method is
     * currently not implemented.
     *
     * @param attr New attributes for this region.
     * @exception CacheException
     * @exception InvalidHandleException
     */
    public void resetElementAttributes( IElementAttributes attr )
        throws CacheException, InvalidHandleException
    {
        // Not implemented
    }

    /**
     * Reset attributes for a particular element in the cache. NOTE: this method
     * is currently not implemented.
     *
     * @param name Key of object to reset attributes for
     * @param attr New attributes for the object
     * @exception CacheException
     * @exception InvalidHandleException
     */
    public void resetElementAttributes( Object name, IElementAttributes attr )
        throws CacheException, InvalidHandleException
    {
        // Not implemented
    }

    /**
     * GetElementAttributes will return an attribute object describing the current
     * attributes associated with the object name.
     *
     * @return Attributes for this region
     * @exception CacheException
     */
    public IElementAttributes getElementAttributes()
        throws CacheException
    {
        return cacheControl.attr;
    }

    /**
     * GetElementAttributes will return an attribute object describing the current
     * attributes associated with the object name. The name object must override
     * the Object.equals and Object.hashCode methods.
     *
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
            attr = cacheControl.getElementAttributes( ( Serializable ) name );
        }
        catch ( IOException ioe )
        {
            log.error( "Failure getting element attributes", ioe );
        }

        return attr;
    }

    /**
     * Dispose this region. Flushes objects to and closes auxiliary caches.
     */
    protected void dispose()
    {
        cacheControl.dispose();
    }

    /**
     * Gets the ICompositeCacheAttributes of the cache region
     *
     * @return
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return cacheControl.getCacheAttributes();
    }

    /**
     * Sets the ICompositeCacheAttributes of the cache region.
     *
     * @param cattr The new ICompositeCacheAttribute value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        cacheControl.setCacheAttributes( cattr );
    }

    // -------------------------------- methods for testing and error reporting

    // protected void dumpMap()
    // {
    //     cacheControl.dumpMap();
    // }
    // protected void dumpCacheEntries()
    // {
    //     cacheControl.dumpCacheEntries();
    // }
}
