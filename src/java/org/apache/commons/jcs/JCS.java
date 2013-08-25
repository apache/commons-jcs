package org.apache.commons.jcs;

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

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.jcs.access.GroupCacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

/**
 * Simple class for using JCS. To use JCS in your application, you can use the static methods of
 * this class to get access objects (instances of this class) for your cache regions. Ideally this
 * class should be all you need to import to use JCS. One JCS should be created for each region you
 * want to access. If you have several regions, then get instances for each. For best performance
 * the getInstance call should be made in an initialization method.
 */
public class JCS<K extends Serializable, V extends Serializable>
    extends GroupCacheAccess<K, V>
{
    /** cache.ccf alternative. */
    private static String configFilename = null;

    /** alternative configuration properties */
    private static Properties configProps = null;

    /** The manager returns cache instances. */
    private static CompositeCacheManager cacheMgr;

    /**
     * Protected constructor for use by the static factory methods.
     * <p>
     * @param cacheControl Cache which the instance will provide access to
     */
    protected JCS( CompositeCache<K, V> cacheControl )
    {
        super( cacheControl );
    }

    /**
     * Get a JCS which accesses the provided region.
     * <p>
     * @param region Region that return JCS will provide access to
     * @return A JCS which provides access to a given region.
     * @exception CacheException
     */
    public static <K extends Serializable, V extends Serializable> JCS<K, V> getInstance( String region )
        throws CacheException
    {
        CompositeCache<K, V> cache = getCacheManager().getCache( region );
        return new JCS<K, V>( cache );
    }

    /**
     * Get a JCS which accesses the provided region.
     * <p>
     * @param region Region that return JCS will provide access to
     * @param icca CacheAttributes for region
     * @return A JCS which provides access to a given region.
     * @exception CacheException
     */
    public static <K extends Serializable, V extends Serializable> JCS<K, V> getInstance( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        CompositeCache<K, V> cache = getCacheManager().getCache( region, icca );
        return new JCS<K, V>( cache );
    }

    /**
     * Gets an instance of CompositeCacheManager and stores it in the cacheMgr class field, if it is
     * not already set. Unlike the implementation in CacheAccess, the cache manager is a
     * CompositeCacheManager. NOTE: This can be moved up into GroupCacheAccess.
     *
     * @throws CacheException if the configuration cannot be loaded
     */
    protected static CompositeCacheManager getCacheManager() throws CacheException
    {
        synchronized ( JCS.class )
        {
            if ( cacheMgr == null )
            {
                if ( configProps != null )
                {
                    cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
                    cacheMgr.configure( configProps );
                }
                else if ( configFilename != null )
                {
                    cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
                    cacheMgr.configure( configFilename );
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
     * Set the filename that the cache manager will be initialized with. Only matters before the
     * instance is initialized.
     * <p>
     * @param configFilename
     */
    public static void setConfigFilename( String configFilename )
    {
        JCS.configFilename = configFilename;
    }

    /**
     * Set the properties that the cache manager will be initialized with. Only
     * matters before the instance is initialized.
     *
     * @param configProps
     */
    public static void setConfigProperties( Properties configProps )
    {
        JCS.configProps = configProps;
    }
}
