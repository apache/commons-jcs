package org.apache.jcs.auxiliary.lateral;

/* ====================================================================
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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
            if ( lca.getTransmissionType() == lca.TCP )
            {
                log.debug( "Creating TCP service" );

                this.lateralService = new LateralTCPService( lca );
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
            if ( lca.getTransmissionType() == lca.TCP )
            {
                addLateralCacheListener( cacheName, LateralTCPListener.getInstance( lca ) );
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
