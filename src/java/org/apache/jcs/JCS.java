package org.apache.jcs;

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

import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Simple class for using JCS. To use JCS in your application, you can use the static methods of
 * this class to get access objects (instances of this class) for your cache regions. Ideally this
 * class should be all you need to import to use JCS. One JCS should be created for each region you
 * want to access. If you have several regions, then get instances for each. For best performance
 * the getInstance call should be made in an initialization method.
 */
public class JCS
    extends GroupCacheAccess
{
    /** cache.ccf alternative. */
    private static String configFilename = null;

    /** The manager returns cache instances. */
    private static CompositeCacheManager cacheMgr;

    /**
     * Protected constructor for use by the static factory methods.
     * <p>
     * @param cacheControl Cache which the instance will provide access to
     */
    protected JCS( CompositeCache cacheControl )
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
    public static JCS getInstance( String region )
        throws CacheException
    {
        ensureCacheManager();

        return new JCS( cacheMgr.getCache( region ) );
    }

    /**
     * Get a JCS which accesses the provided region.
     * <p>
     * @param region Region that return JCS will provide access to
     * @param icca CacheAttributes for region
     * @return A JCS which provides access to a given region.
     * @exception CacheException
     */
    public static JCS getInstance( String region, ICompositeCacheAttributes icca )
        throws CacheException
    {
        ensureCacheManager();

        return new JCS( cacheMgr.getCache( region, icca ) );
    }

    /**
     * Gets an instance of CompositeCacheManager and stores it in the cacheMgr class field, if it is
     * not already set. Unlike the implementation in CacheAccess, the cache manager is a
     * CompositeCacheManager. NOTE: This can will be moved up into GroupCacheAccess.
     */
    protected static synchronized void ensureCacheManager()
    {
        if ( cacheMgr == null )
        {
            if ( configFilename == null )
            {
                cacheMgr = CompositeCacheManager.getInstance();
            }
            else
            {
                cacheMgr = CompositeCacheManager.getUnconfiguredInstance();

                cacheMgr.configure( configFilename );
            }
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
}
