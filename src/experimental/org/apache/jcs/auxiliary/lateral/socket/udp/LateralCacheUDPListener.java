package org.apache.jcs.auxiliary.lateral.socket.udp;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashMap;

import org.apache.jcs.auxiliary.lateral.LateralCacheInfo;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;

import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// remove

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheUDPListener implements ILateralCacheListener, Serializable
{
    private final static Log log =
        LogFactory.getLog( LateralCacheUDPListener.class );

    /** Description of the Field */
    protected static transient CompositeCacheManager cacheMgr;

    /** Description of the Field */
    protected final static HashMap instances = new HashMap();

    // instance vars
    private LateralUDPReceiver receiver;
    private ILateralCacheAttributes ilca;
    private boolean inited = false;


    /**
     * Only need one since it does work for all regions, just reference by
     * multiple region names.
     *
     * @param ilca
     */
    protected LateralCacheUDPListener( ILateralCacheAttributes ilca ) { }


    /** Description of the Method */
    public void init()
    {

        try
        {
            // need to connect based on type
            receiver = new LateralUDPReceiver( ilca, this );
            Thread t = new Thread( receiver );
            t.start();
        }
        catch ( Exception ex )
        {
            log.error( ex );
            throw new IllegalStateException( ex.getMessage() );
        }

    }


    /**
     * let the lateral cache set a listener_id. Since there is only one
     * listerenr for all the regions and every region gets registered? the id
     * shouldn't be set if it isn't zero. If it is we assume that it is a
     * reconnect.
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException
    {
        LateralCacheInfo.listenerId = id;
        if ( log.isDebugEnabled() )
        {
            log.debug( "set listenerId = " + id );
        }
    }


    /**
     * Gets the listenerId attribute of the LateralCacheUDPListener object
     *
     * @return The listenerId value
     */
    public byte getListenerId()
        throws IOException
    {

        // set the manager since we are in use
        //getCacheManager();

        //p( "get listenerId" );
        if ( log.isDebugEnabled() )
        {
            log.debug( "get listenerId = " + LateralCacheInfo.listenerId );
        }
        return LateralCacheInfo.listenerId;
    }


    /**
     * Gets the instance attribute of the LateralCacheUDPListener class
     *
     * @return The instance value
     */
    public static ILateralCacheListener getInstance( ILateralCacheAttributes ilca )
    {
        //throws IOException, NotBoundException
        ILateralCacheListener ins = ( ILateralCacheListener ) instances.get( ilca.getUdpMulticastAddr() + ":" + ilca.getUdpMulticastPort() );
        if ( ins == null )
        {
            synchronized ( LateralCacheUDPListener.class )
            {
                if ( ins == null )
                {
                    ins = new LateralCacheUDPListener( ilca );
                    ins.init();
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "created new listener " + ilca.getUdpMulticastAddr() + ":" + ilca.getUdpMulticastPort() );
                }
                instances.put( ilca.getUdpMulticastAddr() + ":" + ilca.getUdpMulticastPort(), ins );
            }
        }
        return ins;
    }


    //////////////////////////// implements the ILateralCacheListener interface. //////////////
    /** */
    public void handlePut( ICacheElement cb )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "PUTTING ELEMENT FROM LATERAL" );
        }
        getCacheManager();
        ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cb.getCacheName() );
        cache.update( cb, true );
    }


    /** Description of the Method */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemove> cacheName=" + cacheName + ", key=" + key );
        }

        getCacheManager();
        // interface limitation here

        ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cacheName );
        cache.remove( key, true );
    }


    /** Description of the Method */
    public void handleRemoveAll( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleRemoveAll> cacheName=" + cacheName );
        }
        getCacheManager();
        ICache cache = cacheMgr.getCache( cacheName );
        cache.removeAll();
    }


    /** Description of the Method */
    public void handleDispose( String cacheName )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "handleDispose> cacheName=" + cacheName );
        }
        CompositeCacheManager cm = ( CompositeCacheManager ) cacheMgr;
        cm.freeCache( cacheName, true );
    }


    // override for new funcitonality
    /**
     * Gets the cacheManager attribute of the LateralCacheUDPListener object
     */
    protected void getCacheManager()
    {
        if ( cacheMgr == null )
        {
            cacheMgr = CompositeCacheManager.getInstance();

            if ( log.isDebugEnabled() )
            {
                log.debug( "had to get cacheMgr: " + cacheMgr );
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "already got cacheMgr: " + cacheMgr );
            }
        }
    }
}
