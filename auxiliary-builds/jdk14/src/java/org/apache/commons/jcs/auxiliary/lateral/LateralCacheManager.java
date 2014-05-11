package org.apache.commons.jcs.auxiliary.lateral;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheManager;
import org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.LateralCacheJGListener;
import org.apache.commons.jcs.auxiliary.lateral.javagroups.LateralJGService;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Creates lateral caches. Lateral caches are primarily used for removing non
 * laterally configured caches. Non laterally configured cache regions should
 * still bea ble to participate in removal. But if there is a non laterally
 * configured cache hub, then lateral removals may be necessary. For flat
 * webserver production environments, without a strong machine at the app server
 * level, distribution and search may need to occur at the lateral cache level.
 * This is currently not implemented in the lateral cache.
 * <p>
 *
 * TODO: - need freeCache, release, getStats - need to find an interface
 *        acceptible for all - cache managers or a manager within a type
 *
 * @deprecated use individual cache managers
 */
public class LateralCacheManager
    implements ILateralCacheManager
{
    private static final Log log = LogFactory.getLog( LateralCacheManager.class );

    private static LateralCacheMonitor monitor;

    static final Map instances = new HashMap();

    // each manager instance has caches
    final Map caches = new HashMap();

    /**
     * Description of the Field
     */
    protected ILateralCacheAttributes lca;

    private int clients;

    /**
     * Handle to the lateral cache service; or a zombie handle if failed to
     * connect.
     */
    private ICacheServiceNonLocal lateralService;

    /**
     * Wrapper of the lateral cache watch service; or wrapper of a zombie
     * service if failed to connect.
     */
    private LateralCacheWatchRepairable lateralWatch;

    private ICompositeCacheManager cacheMgr;

    /**
     * Returns an instance of the LateralCacheManager.
     *
     * @param lca
     * @param cacheMgr
     *            this allows the auxiliary to be passed a cache manager.
     * @return
     */
    public static LateralCacheManager getInstance( ILateralCacheAttributes lca, ICompositeCacheManager cacheMgr )
    {
        LateralCacheManager ins = (LateralCacheManager) instances.get( lca.toString() );
        synchronized ( instances )
        {
            if ( ins == null )
            {
                log.info( "Instance for [" + lca.toString() + "] is null, creating" );

                ins = (LateralCacheManager) instances.get( lca.toString() );
                if ( ins == null )
                {
                    ins = new LateralCacheManager( lca, cacheMgr );
                    instances.put( lca.toString(), ins );
                }
            }
        }

        ins.clients++;
        // Fires up the monitoring daemon.
        if ( monitor == null )
        {
            monitor = new LateralCacheMonitor( ins );
            // Should never be null
            if ( monitor != null )
            {
                Thread t = new Thread( monitor );
                t.setDaemon( true );
                t.start();
            }
        }
        return ins;
    }

    /**
     * Constructor for the LateralCacheManager object
     *
     * @param lcaA
     * @param cacheMgr
     */
    private LateralCacheManager( ILateralCacheAttributes lcaA, ICompositeCacheManager cacheMgr )
    {
        this.lca = lcaA;

        this.cacheMgr = cacheMgr;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating lateral cache service, lca = " + this.lca );
        }

        // need to create the service based on the type

        try
        {
            if ( this.lca.getTransmissionType() == ILateralCacheAttributes.JAVAGROUPS )
            {
                log.debug( "Creating JAVAGROUPS service" );

                this.lateralService = new LateralJGService( this.lca );
            }

            else
            {
                log.error( "Type not recognized, must zombie" );

                throw new Exception( "no known transmission type for lateral cache." );
            }

            if ( this.lateralService == null )
            {
                log.error( "No service created, must zombie" );

                throw new Exception( "No service created for lateral cache." );
            }

            this.lateralWatch = new LateralCacheWatchRepairable();
            this.lateralWatch.setCacheWatch( new ZombieLateralCacheWatch() );

        }
        catch ( Exception ex )
        {
            // Failed to connect to the lateral server.
            // Configure this LateralCacheManager instance to use the
            // "zombie" services.

            log.error( "Failure, lateral instance will use zombie service", ex );

            this.lateralService = new ZombieLateralCacheService();
            this.lateralWatch = new LateralCacheWatchRepairable();
            this.lateralWatch.setCacheWatch( new ZombieLateralCacheWatch() );

            // Notify the cache monitor about the error, and kick off
            // the recovery process.
            monitor.notifyError();
        }
    }

    /**
     * Adds the lateral cache listener to the underlying cache-watch service.
     *
     * @param cacheName
     *            The feature to be added to the LateralCacheListener attribute
     * @param listener
     *            The feature to be added to the LateralCacheListener attribute
     * @throws IOException
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
    public AuxiliaryCache getCache( String cacheName )
    {
        LateralCacheNoWait c = null;
        synchronized ( this.caches )
        {
            c = (LateralCacheNoWait) this.caches.get( cacheName );
            if ( c == null )
            {
                LateralCacheAttributes attr = (LateralCacheAttributes) lca.copy();
                attr.setCacheName( cacheName );
                LateralCache cache = new LateralCache( attr, this.lateralService, monitor );
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Created cache for noWait, cache = [" + cache + "]" );
                }
                c = new LateralCacheNoWait( cache );
                this.caches.put( cacheName, c );

                log.info( "Created LateralCacheNoWait for " + this.lca + " LateralCacheNoWait = [" + c + "]" );
            }
        }

        // don't create a listener if we are not receiving.
        if ( lca.isReceive() )
        {
            try
            {
                if ( this.lca.getTransmissionType() == ILateralCacheAttributes.JAVAGROUPS )
                {
                    addLateralCacheListener( cacheName, LateralCacheJGListener.getInstance( this.lca, cacheMgr ) );
                }
            }
            catch ( IOException ioe )
            {
                log.error( "Problem creating lateral listener", ioe );
            }
            catch ( Exception e )
            {
                log.error( "Problem creating lateral listener", e );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Not creating a listener since we are not receiving." );
            }
        }

        // TODO: need listener repair

        return c;
    }

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
    public void fixCaches( ICacheServiceNonLocal lateralService, ILateralCacheObserver lateralWatch )
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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheManager#getInstances()
     */
    public Map getInstances()
    {
        return instances;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheManager#getCaches()
     */
    public Map getCaches()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jcs.auxiliary.lateral.behavior.ILateralCacheManager#fixService()
     */
    public Object fixService()
        throws IOException
    {
        Object service = null;
        try
        {
            // no op
        }
        catch ( Exception ex )
        {
            log.error( "Can't fix " + ex.getMessage() );
            throw new IOException( "Can't fix " + ex.getMessage() );
        }
        return service;
    }
}
