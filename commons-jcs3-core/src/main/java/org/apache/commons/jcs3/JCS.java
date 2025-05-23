package org.apache.commons.jcs3;

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

import java.util.Properties;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.GroupCacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;

/**
 * Simple class for using JCS. To use JCS in your application, you can use the static methods of
 * this class to get access objects (instances of this class) for your cache regions. One CacheAccess
 * object should be created for each region you want to access. If you have several regions, then
 * get instances for each. For best performance the getInstance call should be made in an
 * initialization method.
 */
public abstract class JCS
{
    /** Cache.ccf alternative. */
    private static String configFileName;

    /** Alternative configuration properties */
    private static Properties configProps;

    /** Cache manager use by the various forms of defineRegion and getAccess */
    private static CompositeCacheManager cacheMgr;

    /**
     * Helper method which checks to make sure the cacheMgr class field is set, and if not requests
     * an instance from CacheManagerFactory.
     *
     * @throws CacheException if the configuration cannot be loaded
     */
    private static CompositeCacheManager getCacheManager() throws CacheException
    {
        synchronized ( JCS.class )
        {
            if ( cacheMgr == null || !cacheMgr.isInitialized())
            {
                if ( configProps != null )
                {
                    cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
                    cacheMgr.configure( configProps );
                }
                else if ( configFileName != null )
                {
                    cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
                    cacheMgr.configure( configFileName );
                }
                else
                {
                    cacheMgr = CompositeCacheManager.getInstance();
                }
            }

            return cacheMgr;
        }
    }

    /**
     * Gets a GroupCacheAccess which accesses the provided region.
     *
     * @param region Region that return GroupCacheAccess will provide access to
     * @return A GroupCacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> GroupCacheAccess<K, V> getGroupCacheInstance( final String region )
        throws CacheException
    {
        final CompositeCache<GroupAttrName<K>, V> cache = getCacheManager().getCache( region );
        return new GroupCacheAccess<>( cache );
    }

    /**
     * Gets a GroupCacheAccess which accesses the provided region.
     *
     * @param region Region that return GroupCacheAccess will provide access to
     * @param icca CacheAttributes for region
     * @return A GroupCacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> GroupCacheAccess<K, V> getGroupCacheInstance( final String region, final ICompositeCacheAttributes icca )
        throws CacheException
    {
        final CompositeCache<GroupAttrName<K>, V> cache = getCacheManager().getCache( region, icca );
        return new GroupCacheAccess<>( cache );
    }

    /**
     * Gets a GroupCacheAccess which accesses the provided region.
     *
     * @param region Region that return CacheAccess will provide access to
     * @param icca CacheAttributes for region
     * @param eattr ElementAttributes for the region
     * @return A GroupCacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> GroupCacheAccess<K, V> getGroupCacheInstance( final String region, final ICompositeCacheAttributes icca,  final IElementAttributes eattr )
        throws CacheException
    {
        final CompositeCache<GroupAttrName<K>, V> cache = getCacheManager().getCache( region, icca, eattr );
        return new GroupCacheAccess<>( cache );
    }

    /**
     * Gets a CacheAccess which accesses the provided region.
     *
     * @param region Region that return CacheAccess will provide access to
     * @return A CacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> CacheAccess<K, V> getInstance( final String region )
        throws CacheException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( region );
        return new CacheAccess<>( cache );
    }

    /**
     * Gets a CacheAccess which accesses the provided region.
     *
     * @param region Region that return CacheAccess will provide access to
     * @param icca CacheAttributes for region
     * @return A CacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> CacheAccess<K, V> getInstance( final String region, final ICompositeCacheAttributes icca )
        throws CacheException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( region, icca );
        return new CacheAccess<>( cache );
    }

    /**
     * Gets a CacheAccess which accesses the provided region.
     *
     * @param region Region that return CacheAccess will provide access to
     * @param icca CacheAttributes for region
     * @param eattr ElementAttributes for the region
     * @return A CacheAccess which provides access to a given region.
     * @throws CacheException
     */
    public static <K, V> CacheAccess<K, V> getInstance( final String region, final ICompositeCacheAttributes icca,  final IElementAttributes eattr )
        throws CacheException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( region, icca, eattr );
        return new CacheAccess<>( cache );
    }

    /**
     * Sets the file name that the cache manager will be initialized with. Only matters before the
     * instance is initialized.
     *
     * @param configFileName
     */
    public static void setConfigFilename( final String configFileName )
    {
        JCS.configFileName = configFileName;
    }

    /**
     * Sets the properties that the cache manager will be initialized with. Only
     * matters before the instance is initialized.
     *
     * @param configProps
     */
    public static void setConfigProperties( final Properties configProps )
    {
        JCS.configProps = configProps;
    }

    /**
     * Shut down the cache manager and set the instance to null
     */
    public static void shutdown()
    {
        synchronized ( JCS.class )
        {
            if ( cacheMgr != null && cacheMgr.isInitialized())
            {
            	cacheMgr.shutDown();
            }

            cacheMgr = null;
        }
    }
}
