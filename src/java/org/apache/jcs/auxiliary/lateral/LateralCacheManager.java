package org.apache.jcs.auxiliary.lateral;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheManager;

import org.apache.jcs.auxiliary.lateral.LateralCache;
import org.apache.jcs.auxiliary.lateral.LateralCacheMonitor;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheWatchRepairable;
import org.apache.jcs.auxiliary.lateral.ZombieLateralCacheService;
import org.apache.jcs.auxiliary.lateral.ZombieLateralCacheWatch;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheObserver;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

// TODO: make these dymanically configurable
// refactor config class so it only has the used info for each?
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralGroupCacheTCPListener;
import org.apache.jcs.auxiliary.lateral.socket.tcp.LateralTCPService;

import org.apache.jcs.auxiliary.lateral.socket.udp.LateralGroupCacheUDPListener;
import org.apache.jcs.auxiliary.lateral.socket.udp.LateralUDPService;

import org.apache.jcs.auxiliary.lateral.xmlrpc.LateralGroupCacheXMLRPCListener;
import org.apache.jcs.auxiliary.lateral.xmlrpc.LateralXMLRPCService;

import org.apache.jcs.auxiliary.lateral.javagroups.LateralGroupCacheJGListener;
import org.apache.jcs.auxiliary.lateral.javagroups.LateralJGService;


import org.apache.jcs.engine.CacheWatchRepairable;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import  org.apache.jcs.auxiliary.*;
//import  org.apache.jcs.auxiliary.lateral.http.*;

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
public class LateralCacheManager implements IAuxiliaryCacheManager
{
    private final static Log log =
        LogFactory.getLog( LateralCacheManager.class );

    //static ArrayList defaultServers;
    ICacheAttributes defaultCattr;

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
            if ( lca.getTransmissionType() == lca.UDP )
            {
                // need to allow for this to be a service.
                // should wrap sender and new kind of receiver?

                log.debug( "Creating UDP service" );

                this.lateralService = new LateralUDPService( lca );
            }
            else if ( lca.getTransmissionType() == lca.HTTP )
            {
                log.debug( "[NOT] Creating HTTP service" );

                // FIXME: Why is this disabled?
                //this.lateralService = new LateralHTTPService( lca );
            }
            else if ( lca.getTransmissionType() == lca.TCP )
            {
                log.debug( "Creating TCP service" );

                this.lateralService = new LateralTCPService( lca );
            }
            else if ( lca.getTransmissionType() == lca.XMLRPC )
            {
                log.debug( "Creating XMLRPC service" );

                this.lateralService = new LateralXMLRPCService( lca );
            }
            else if ( lca.getTransmissionType() == lca.JAVAGROUPS )
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
    public ICache getCache( String cacheName )
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
            // need to set listener based on transmissionType
            if ( lca.getTransmissionType() == lca.UDP )
            {
                addLateralCacheListener( cacheName, LateralGroupCacheUDPListener.getInstance( lca ) );
            }
            else
                if ( lca.getTransmissionType() == lca.TCP )
            {
                addLateralCacheListener( cacheName, LateralGroupCacheTCPListener.getInstance( lca ) );
            }
            else
                if ( lca.getTransmissionType() == lca.XMLRPC )
            {
                addLateralCacheListener( cacheName, LateralGroupCacheXMLRPCListener.getInstance( lca ) );
            }
            else
                if ( lca.getTransmissionType() == lca.JAVAGROUPS )
            {
                addLateralCacheListener( cacheName, LateralGroupCacheJGListener.getInstance( lca ) );
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
            for ( Iterator en = caches.values().iterator(); en.hasNext();  )
            {
                LateralCacheNoWait cache = ( LateralCacheNoWait ) en.next();
                cache.fixCache( this.lateralService );
            }
        }
    }
}
