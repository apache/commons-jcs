package org.apache.jcs.auxiliary.lateral;


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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPService;
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPListener;
import org.apache.jcs.auxiliary.lateral.javagroups.LateralJGService;
import org.apache.jcs.auxiliary.lateral.javagroups.LateralCacheJGListener;


/**
 * Creates lateral caches. Lateral caches are primarily used for removing non
 * laterally configured caches. Non laterally configured cache regions should
 * still bea ble to participate in removal. But if there is a non laterally
 * configured cache hub, then lateral removals may be necessary. For flat
 * webserver production environments, without a strong machine at the app server
 * level, distribution and search may need to occur at the lateral cache level.
 * This is currently not implemented in the lateral cache. TODO: - need
 * freeCache, release, getStats - need to find an interface acceptible for all -
 * cache managers or a manager within a type
 */
public class LateralCacheManager implements AuxiliaryCacheManager
{
    private final static Log log =
        LogFactory.getLog( LateralCacheManager.class );

    private static LateralCacheMonitor monitor;

    final static Map instances = new HashMap();
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
    private ILateralCacheService lateralService;

    /**
     * Wrapper of the lateral cache watch service; or wrapper of a zombie
     * service if failed to connect.
     */
    private LateralCacheWatchRepairable lateralWatch;

    /**
     * Gets the instance attribute of the LateralCacheManager class
     *
     * @return The instance value
     * @param lca
     */
    public static LateralCacheManager getInstance( ILateralCacheAttributes lca )
    {
        LateralCacheManager ins = ( LateralCacheManager ) instances.get( lca.toString() );
        if ( ins == null )
        {
            log.debug( "Instance is null, creating" );

            synchronized ( instances )
            {
                ins = ( LateralCacheManager ) instances.get( lca.toString() );
                if ( ins == null )
                {
                    ins = new LateralCacheManager( lca );
                    instances.put( lca.toString(), ins );
                }
            }
        }

        ins.clients++;
        // Fires up the monitoring daemon.
        if ( monitor == null )
        {
            monitor = LateralCacheMonitor.getInstance();
            // If the returned monitor is null, it means it's already started elsewhere.
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
     * @param lca
     */
    private LateralCacheManager( ILateralCacheAttributes lca )
    {
        this.lca = lca;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating lateral cache service, lca = " + lca );
        }

        // need to create the service based on the type

        try
        {
            if ( lca.getTransmissionType() == ILateralCacheAttributes.TCP )
            {
                log.debug( "Creating TCP service" );

                this.lateralService = new LateralTCPService( lca );
            }
            else if ( lca.getTransmissionType() == ILateralCacheAttributes.JAVAGROUPS )
            {
                log.debug( "Creating JAVAGROUPS service" );

                this.lateralService = new LateralJGService( lca );
            }

            else
            {
                log.error( "Type not recognized, must zombie" );

                throw new Exception( "no known transmission type for lateral cache." );
            }

            if ( this.lateralService == null )
            {
                log.error( "No service created, must zombie" );

                throw new Exception( "no service created for lateral cache." );
            }

            lateralWatch = new LateralCacheWatchRepairable();
            lateralWatch.setCacheWatch( new ZombieLateralCacheWatch() );

        }
        catch ( Exception ex )
        {
            // Failed to connect to the lateral server.
            // Configure this LateralCacheManager instance to use the
            // "zombie" services.

            log.error( "Failure, lateral instance will use zombie service", ex );

            lateralService = new ZombieLateralCacheService();
            lateralWatch = new LateralCacheWatchRepairable();
            lateralWatch.setCacheWatch( new ZombieLateralCacheWatch() );

            // Notify the cache monitor about the error, and kick off
            // the recovery process.
            LateralCacheMonitor.getInstance().notifyError();
        }
    }

    /**
     * Adds the lateral cache listener to the underlying cache-watch service.
     *
     * @param cacheName The feature to be added to the LateralCacheListener
     *      attribute
     * @param listener The feature to be added to the LateralCacheListener
     *      attribute
     * @exception IOException
     */
    public void addLateralCacheListener( String cacheName, ILateralCacheListener listener )
        throws IOException
    {
        synchronized ( caches )
        {
            lateralWatch.addCacheListener( cacheName, listener );
        }
    }

    /**
     * Called to access a precreated region or construct one with defaults.
     * Since all aux cache access goes through the manager, this will never be
     * called.
     *
     * @return The {3} value
     * @param cacheName
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        LateralCacheNoWait c = null;
        synchronized ( caches )
        {
            //c = (LateralCache)caches.get(cacheName);
            c = ( LateralCacheNoWait ) caches.get( cacheName );
            if ( c == null )
            {
                c = new LateralCacheNoWait( new LateralCache( lca, lateralService ) );
                caches.put( cacheName, c );
            }
        }

        try
        {
            if ( lca.getTransmissionType() == ILateralCacheAttributes.TCP )
            {
                addLateralCacheListener( cacheName, LateralTCPListener.getInstance( lca ) );
            }
            else if ( lca.getTransmissionType() == ILateralCacheAttributes.JAVAGROUPS )
            {
                addLateralCacheListener( cacheName, LateralCacheJGListener.getInstance( lca ) );
            }
        }
        catch ( IOException ioe )
        {
            log.error( ioe );
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        // TODO:  need listener repair

        // if ( log.isDebugEnabled() )
        // {
        //     log.debug("LateralManager stats : " + getStats());
        // }

        return c;
    }

    /**
     * Gets the cacheType attribute of the LateralCacheManager object
     *
     * @return The {3} value
     */
    public int getCacheType()
    {
        return LATERAL_CACHE;
    }

    /**
     * Gets the stats attribute of the LateralCacheManager object
     *
     * @return The {3} value
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

        synchronized ( caches )
        {
            this.lateralService = lateralService;
            // need to implment an observer for some types of laterals( http and tcp)
            //this.lateralWatch.setCacheWatch(lateralWatch);
            for ( Iterator en = caches.values().iterator(); en.hasNext(); )
            {
                LateralCacheNoWait cache = ( LateralCacheNoWait ) en.next();
                cache.fixCache( this.lateralService );
            }
        }
    }
}
