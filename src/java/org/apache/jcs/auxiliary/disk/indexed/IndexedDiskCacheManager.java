package org.apache.jcs.auxiliary.disk.indexed;

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

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.engine.behavior.ICache;

/**
 * Cache manager for IndexedDiskCaches. This manages the instances of the disk
 * cache.
 */
public class IndexedDiskCacheManager
    implements AuxiliaryCacheManager
{
    private static final long serialVersionUID = -4153287154512274626L;

    private final static Log log = LogFactory.getLog( IndexedDiskCacheManager.class );

    private static int clients;

    private static IndexedDiskCacheManager instance;

    private Hashtable caches = new Hashtable();

    private IndexedDiskCacheAttributes defaultCacheAttributes;

    /**
     * Constructor for the IndexedDiskCacheManager object
     * <p>
     * @param defaultCacheAttributes
     *            Default attributes for caches managed by the instance.
     */
    private IndexedDiskCacheManager( IndexedDiskCacheAttributes defaultCacheAttributes )
    {
        this.defaultCacheAttributes = defaultCacheAttributes;
    }

    /**
     * Gets the singleton instance of the manager
     * <p>
     * @param defaultCacheAttributes
     *            If the instance has not yet been created, it will be
     *            initialized with this set of default attributes.
     * @return The instance value
     */
    public static IndexedDiskCacheManager getInstance( IndexedDiskCacheAttributes defaultCacheAttributes )
    {
        synchronized ( IndexedDiskCacheManager.class )
        {
            if ( instance == null )
            {
                instance = new IndexedDiskCacheManager( defaultCacheAttributes );
            }
        }

        clients++;

        return instance;
    }

    /**
     * Gets an IndexedDiskCache for the supplied name using the default
     * attributes.
     * <p>
     * @param cacheName
     *            Name that will be used when creating attributes.
     * @return A cache.
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        IndexedDiskCacheAttributes cacheAttributes = (IndexedDiskCacheAttributes) defaultCacheAttributes.copy();

        cacheAttributes.setCacheName( cacheName );

        return getCache( cacheAttributes );
    }

    /**
     * Get an IndexedDiskCache for the supplied attributes. Will provide an
     * existing cache for the name attribute if one has been created, or will
     * create a new cache.
     * <p>
     * @param cacheAttributes
     *            Attributes the cache should have.
     * @return A cache, either from the existing set or newly created.
     */
    public AuxiliaryCache getCache( IndexedDiskCacheAttributes cacheAttributes )
    {
        AuxiliaryCache cache = null;

        String cacheName = cacheAttributes.getCacheName();

        log.debug( "Getting cache named: " + cacheName );

        synchronized ( caches )
        {
            // Try to load the cache from the set that have already been
            // created. This only looks at the name attribute.

            cache = (AuxiliaryCache) caches.get( cacheName );

            // If it was not found, create a new one using the supplied
            // attributes

            if ( cache == null )
            {
                cache = new IndexedDiskCache( cacheAttributes );

                caches.put( cacheName, cache );
            }
        }

        return cache;
    }

    /**
     * Disposes the cache with the given name, if found in the set of managed
     * caches.
     * <p>
     * @param cacheName
     *            Name of cache to dispose.
     */
    public void freeCache( String cacheName )
    {
        ICache cache = (ICache) caches.get( cacheName );

        if ( cache != null )
        {
            try
            {
                cache.dispose();
            }
            catch ( Exception e )
            {
                log.error( "Failure disposing cache: " + cacheName, e );
            }
        }
    }

    /**
     * Gets the cacheType attribute of the DiskCacheManager object
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }

    /**
     * Releases the cache manager instance. When all clients have released the
     * cache manager, all contained caches will be disposed.
     */
    public void release()
    {
        clients--;

        if ( --clients != 0 )
        {
            return;
        }

        synchronized ( caches )
        {
            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                ICache cache = (ICache) allCaches.nextElement();

                if ( cache != null )
                {
                    try
                    {
                        cache.dispose();
                    }
                    catch ( Exception e )
                    {
                        log.error( "Failure disposing cache: " + cache.getCacheName(), e );
                    }
                }
            }
        }
    }
}
