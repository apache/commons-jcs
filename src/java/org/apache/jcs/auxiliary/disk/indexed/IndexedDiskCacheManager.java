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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.disk.AbstractDiskCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * Cache manager for IndexedDiskCaches. This manages the instances of the disk cache.
 */
public class IndexedDiskCacheManager
    extends AbstractDiskCacheManager
{
    /** Don't change */
    private static final long serialVersionUID = -4153287154512274626L;

    /** The logger */
    private final static Log log = LogFactory.getLog( IndexedDiskCacheManager.class );

    /** Singleton instance. */
    private static IndexedDiskCacheManager instance;

    /** Each region has an entry here. */
    private Hashtable caches = new Hashtable();

    /** User configurable attributes */
    private IndexedDiskCacheAttributes defaultCacheAttributes;

    /**
     * Constructor for the IndexedDiskCacheManager object
     * <p>
     * @param defaultCacheAttributes Default attributes for caches managed by the instance.
     * @param cacheEventLogger
     * @param elementSerializer
     */
    private IndexedDiskCacheManager( IndexedDiskCacheAttributes defaultCacheAttributes,
                                     ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
    {
        this.defaultCacheAttributes = defaultCacheAttributes;
        setElementSerializer( elementSerializer );
        setCacheEventLogger( cacheEventLogger );
    }

    /**
     * Gets the singleton instance of the manager
     * <p>
     * @param defaultCacheAttributes If the instance has not yet been created, it will be
     *            initialized with this set of default attributes.
     * @param cacheEventLogger
     * @param elementSerializer
     * @return The instance value
     */
    public static IndexedDiskCacheManager getInstance( IndexedDiskCacheAttributes defaultCacheAttributes,
                                                       ICacheEventLogger cacheEventLogger,
                                                       IElementSerializer elementSerializer )
    {
        synchronized ( IndexedDiskCacheManager.class )
        {
            if ( instance == null )
            {
                instance = new IndexedDiskCacheManager( defaultCacheAttributes, cacheEventLogger, elementSerializer );
            }
        }
        return instance;
    }

    /**
     * Gets an IndexedDiskCache for the supplied name using the default attributes.
     * <p>
     * @param cacheName Name that will be used when creating attributes.
     * @return A cache.
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        IndexedDiskCacheAttributes cacheAttributes = (IndexedDiskCacheAttributes) defaultCacheAttributes.copy();

        cacheAttributes.setCacheName( cacheName );

        return getCache( cacheAttributes );
    }

    /**
     * Get an IndexedDiskCache for the supplied attributes. Will provide an existing cache for the
     * name attribute if one has been created, or will create a new cache.
     * <p>
     * @param cacheAttributes Attributes the cache should have.
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
                cache = new IndexedDiskCache( cacheAttributes, getElementSerializer() );
                cache.setCacheEventLogger( getCacheEventLogger() );
                caches.put( cacheName, cache );
            }
        }

        return cache;
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
}
