package org.apache.jcs.access;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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

import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.control.CacheHub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which provides interface for all access to the cache. An instance of
 * this class is tied to a specific cache region. Static methods are provided to
 * get such instances.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @created February 13, 2002
 * @version $Id$
 */
public class CacheAccess implements ICacheAccess
{
    private final static Log log =
        LogFactory.getLog( CacheAccess.class );

    /**
     * Cache manager use by the various forms of defineRegion and getAccess
     */
    private static CacheHub cacheMgr;

    /**
     * Cache that a given instance of this class provides access to. Should this
     * be the inteface?
     */
    protected Cache cacheControl;

    /**
     * Constructor for the CacheAccess object.
     *
     * @param cacheControl The cache which the created instance accesses
     */
    protected CacheAccess( Cache cacheControl )
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

        return new CacheAccess( ( Cache ) cacheMgr.getCache( name ) );
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

        return new CacheAccess( ( Cache ) cacheMgr.getCache( name, cattr ) );
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
            ( Cache ) cacheMgr.getCache( name, cattr, attr ) );
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

        return new CacheAccess( ( Cache ) cacheMgr.getCache( region ) );
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

        return new CacheAccess( ( Cache ) cacheMgr.getCache( region, icca ) );
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
                    cacheMgr = CacheHub.getInstance();
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
        else
        {
            put( key, value );
        }
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
        // Call put with the contained caches default attributes.

        put( ( Serializable ) name,
             ( Serializable ) obj,
             cacheControl.getElementAttributes() );
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
        catch( IOException e )
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
        catch( IOException e )
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
