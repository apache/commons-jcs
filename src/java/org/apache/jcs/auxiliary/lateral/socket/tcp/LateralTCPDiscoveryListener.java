package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.jcs.utils.discovery.DiscoveredService;
import org.apache.jcs.utils.discovery.behavior.IDiscoveryListener;

/**
 * This knows how to add and remove discovered services.
 * <p>
 * We can have one listener per region, or one shared by all regions.
 */
public class LateralTCPDiscoveryListener
    implements IDiscoveryListener
{
    /** The log factory */
    private final static Log log = LogFactory.getLog( LateralTCPDiscoveryListener.class );

    /**
     * Map of no wait facades. these are used to determine which regions are locally configured to
     * use laterals.
     */
    private Map facades = Collections.synchronizedMap( new HashMap() );

    /** The cache manager. */
    private ICompositeCacheManager cacheMgr;

    /** The event logger. */
    protected ICacheEventLogger cacheEventLogger;

    /** The serializer. */
    protected IElementSerializer elementSerializer;

    /**
     * This plugs into the udp discovery system. It will receive add and remove events.
     * <p>
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     */
    protected LateralTCPDiscoveryListener( ICompositeCacheManager cacheMgr, ICacheEventLogger cacheEventLogger,
                                           IElementSerializer elementSerializer )
    {
        this.cacheMgr = cacheMgr;
        this.cacheEventLogger = cacheEventLogger;
        this.elementSerializer = elementSerializer;
    }

    /**
     * Adds a nowait facade under this cachename. If one already existed, it will be overridden.
     * <p>
     * This adds nowaits to a facade for the region name. If the region has no facade, then it is
     * not configured to use the lateral cache, and no facade will be created.
     * <p>
     * @param facade - facade (for region) => multiple lateral clients.
     * @param cacheName
     * @return true if the facade was not already registered.
     */
    public synchronized boolean addNoWaitFacade( String cacheName, LateralCacheNoWaitFacade facade )
    {
        boolean isNew = !facades.containsKey( cacheName );

        // override or put anew, it doesn't matter
        facades.put( cacheName, facade );

        return isNew;
    }

    /**
     * When a broadcast is received from the UDP Discovery receiver, for each cacheName in the
     * message, the add no wait will be called here. To add a no wait, the facade is looked up for
     * this cache name.
     * <p>
     * Each region has a facade. The facade contains a list of end points--the other tcp lateral
     * services.
     * <p>
     * @param noWait
     */
    protected void addNoWait( LateralCacheNoWait noWait )
    {
        LateralCacheNoWaitFacade facade = (LateralCacheNoWaitFacade) facades.get( noWait.getCacheName() );
        if ( log.isDebugEnabled() )
        {
            log.debug( "Got facade for " + noWait.getCacheName() + " = " + facade );
        }

        if ( facade != null )
        {
            boolean isNew = facade.addNoWait( noWait );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Called addNoWait, isNew = " + isNew );
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "addNoWait > Different nodes are configured differently or region [" + noWait.getCacheName()
                    + "] is not yet used on this side.  " );
            }
        }
    }

    /**
     * Look up the facade for the name. If it doesn't exist, then the region is not configured for
     * use with the lateral cache. If it is present, remove the item from the no wait list.
     * <p>
     * @param noWait
     */
    protected void removeNoWait( LateralCacheNoWait noWait )
    {
        LateralCacheNoWaitFacade facade = (LateralCacheNoWaitFacade) facades.get( noWait.getCacheName() );
        if ( log.isDebugEnabled() )
        {
            log.debug( "Got facade for " + noWait.getCacheName() + " = " + facade );
        }

        if ( facade != null )
        {
            boolean removed = facade.removeNoWait( noWait );
            if ( log.isDebugEnabled() )
            {
                log.debug( "Called removeNoWait, removed " + removed );
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "removeNoWait > Different nodes are configured differently or region ["
                    + noWait.getCacheName() + "] is not yet used on this side.  " );
            }
        }
    }

    /**
     * Creates the lateral cache if needed.
     * <p>
     * We could go to the composite cache manager and get the the cache for the region. This would
     * force a full configuration of the region. One advantage of this would be that the creation of
     * the later would go through the factory, which would add the item to the no wait list. But we
     * don't want to do this. This would force this client to have all the regions as the other.
     * This might not be desired. We don't want to send or recieve for a region here that is either
     * not used or not configured to use the lateral.
     * <p>
     * Right now, I'm afraid that the region will get puts if another instance has the region
     * configured to use the lateral and our address is configured. This might be a bug, but it
     * shouldn't happen with discovery.
     * <p>
     * @param service
     */
    public void addDiscoveredService( DiscoveredService service )
    {
        // get a cache and add it to the no waits
        // the add method should not add the same.
        // we need the listener port from the original config.
        LateralTCPCacheManager lcm = findManagerForServiceEndPoint( service );

        ArrayList regions = service.getCacheNames();
        if ( regions != null )
        {
            // for each region get the cache
            Iterator it = regions.iterator();
            while ( it.hasNext() )
            {
                String cacheName = (String) it.next();

                try
                {
                    ICache ic = lcm.getCache( cacheName );

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Got cache, ic = " + ic );
                    }

                    // add this to the nowaits for this cachename
                    if ( ic != null )
                    {
                        addNoWait( (LateralCacheNoWait) ic );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Called addNoWait for cacheName [" + cacheName + "]" );
                        }
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Problem creating no wait", e );
                }
            }
        }
        else
        {
            log.warn( "No cache names found in message " + service );
        }
    }

    /**
     * Removes the lateral cache.
     * <p>
     * We need to tell the manager that this instance is bad, so it will reconnect the sender if it
     * comes back.
     * <p>
     * @param service
     */
    public void removeDiscoveredService( DiscoveredService service )
    {
        // get a cache and add it to the no waits
        // the add method should not add the same.
        // we need the listener port from the original config.
        LateralTCPCacheManager lcm = findManagerForServiceEndPoint( service );

        ArrayList regions = service.getCacheNames();
        if ( regions != null )
        {
            // for each region get the cache
            Iterator it = regions.iterator();
            while ( it.hasNext() )
            {
                String cacheName = (String) it.next();

                try
                {
                    ICache ic = lcm.getCache( cacheName );

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Got cache, ic = " + ic );
                    }

                    // remove this to the nowaits for this cachename
                    if ( ic != null )
                    {
                        removeNoWait( (LateralCacheNoWait) ic );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Called removeNoWait for cacheName [" + cacheName + "]" );
                        }
                    }
                }
                catch ( Exception e )
                {
                    log.error( "Problem removing no wait", e );
                }
            }
        }
        else
        {
            log.warn( "No cache names found in message " + service );
        }
    }

    /**
     * Gets the appropriate manager.
     * <p>
     * @param service
     * @return LateralTCPCacheManager configured for that end point.
     */
    private LateralTCPCacheManager findManagerForServiceEndPoint( DiscoveredService service )
    {
        ITCPLateralCacheAttributes lca = new TCPLateralCacheAttributes();
        lca.setTransmissionType( LateralCacheAttributes.TCP );
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        LateralTCPCacheManager lcm = LateralTCPCacheManager.getInstance( lca, cacheMgr, cacheEventLogger,
                                                                         elementSerializer );
        return lcm;
    }
}
