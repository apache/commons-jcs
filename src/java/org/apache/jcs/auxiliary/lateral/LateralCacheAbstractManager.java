package org.apache.jcs.auxiliary.lateral;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheManager;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

/**
 * Creates lateral caches. Lateral caches are primarily used for removing non
 * laterally configured caches. Non laterally configured cache regions should
 * still be able to participate in removal. But if there is a non laterally
 * configured cache hub, then lateral removals may be necessary. For flat
 * webserver production environments, without a strong machine at the app server
 * level, distribution and search may need to occur at the lateral cache level.
 * This is currently not implemented in the lateral cache.
 * <p>
 *
 * @TODO: - need freeCache, release, getStats - need to find an interface
 *        acceptible for all - cache managers or a manager within a type
 */
public abstract class LateralCacheAbstractManager
    implements ILateralCacheManager
{
    private final static Log log = LogFactory.getLog( LateralCacheAbstractManager.class );

    /**
     * Each manager instance has caches.
     */
    protected final Map caches = new HashMap();

    /**
     * Description of the Field
     */
    protected ILateralCacheAttributes lca;

    /**
     * Handle to the lateral cache service; or a zombie handle if failed to
     * connect.
     */
    private ILateralCacheService lateralService;

    /**
     * Wrapper of the lateral cache watch service; or wrapper of a zombie
     * service if failed to connect.
     */
    private LateralCacheWatchRepairable lateralWatch;

    /**
     * Adds the lateral cache listener to the underlying cache-watch service.
     *
     * @param cacheName
     *            The feature to be added to the LateralCacheListener attribute
     * @param listener
     *            The feature to be added to the LateralCacheListener attribute
     * @exception IOException
     */
    public void addLateralCacheListener( String cacheName, ILateralCacheListener listener )
        throws IOException
    {
        synchronized ( this.caches )
        {
            this.lateralWatch.addCacheListener( cacheName, listener );
        }
    }

    /**
     * Called to access a precreated region or construct one with defaults.
     * Since all aux cache access goes through the manager, this will never be
     * called.
     * <p>
     * After getting the manager instance for a server, the factory gets a cache
     * for the region name it is constructing.
     * <p>
     * There should be one manager per server and one cache per region per
     * manager.
     *
     * @return AuxiliaryCache
     * @param cacheName
     */
    public abstract AuxiliaryCache getCache( String cacheName );

    /**
     * Gets the cacheType attribute of the LateralCacheManager object
     *
     * @return The cache type value
     */
    public int getCacheType()
    {
        return LATERAL_CACHE;
    }

    /**
     * Gets the stats attribute of the LateralCacheManager object
     *
     * @return String
     */
    public String getStats()
    {
        // add something here
        return "";
    }

    /**
     * Fixes up all the caches managed by this cache manager.
     *
     * @param lateralService
     * @param lateralWatch
     */
    public void fixCaches( ILateralCacheService lateralService, ILateralCacheObserver lateralWatch )
    {
        log.debug( "Fixing lateral caches:" );

        synchronized ( this.caches )
        {
            this.lateralService = lateralService;
            // need to implment an observer for some types of laterals( http and
            // tcp)
            //this.lateralWatch.setCacheWatch(lateralWatch);
            for ( Iterator en = this.caches.values().iterator(); en.hasNext(); )
            {
                LateralCacheNoWait cache = (LateralCacheNoWait) en.next();
                cache.fixCache( this.lateralService );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheManager#getCaches()
     */
    public Map getCaches()
    {
        return caches;
    }
}
