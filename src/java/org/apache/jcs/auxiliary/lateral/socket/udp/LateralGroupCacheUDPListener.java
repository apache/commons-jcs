package org.apache.jcs.auxiliary.lateral.socket.udp;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheListener;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.control.CacheHub;
import org.apache.jcs.engine.control.group.GroupAttrName;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralGroupCacheUDPListener
    extends LateralCacheUDPListener
    implements ILateralCacheListener, Serializable
{
    private final static Log log =
        LogFactory.getLog( LateralGroupCacheUDPListener.class );

    /**
     * Constructor for the LateralGroupCacheUDPListener object
     *
     * @param ilca
     */
    protected LateralGroupCacheUDPListener( ILateralCacheAttributes ilca )
    {
        super( ilca );
        log.debug( "creating LateralGroupCacheUDPListener" );
    }

    /**
     * Gets the instance attribute of the LateralGroupCacheUDPListener class
     *
     * @return The instance value
     */
    public static ILateralCacheListener getInstance( ILateralCacheAttributes ilca )
    {
        //throws IOException, NotBoundException
        ILateralCacheListener ins = ( ILateralCacheListener ) instances.get( ilca.getUdpMulticastAddr() + ":" + ilca.getUdpMulticastPort() );
        if ( ins == null )
        {
            synchronized ( LateralGroupCacheUDPListener.class )
            {
                if ( ins == null )
                {
                    ins = new LateralGroupCacheUDPListener( ilca );
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


    // just need to put new logic for remove( key, int ) into groupcache
    // or have the existing double arg method call the single arg method which is
    // overridden in the group cache.
    /*
     * not necessary
     */
    /** Description of the Method */
    public void handlePut( ICacheElement cb )
        throws IOException
    {

        log.debug( "PUTTING ELEMENT FROM REMOTE" );

        // could put this in the group cache.
        if ( cb.getKey() instanceof GroupAttrName )
        {
            try
            {
                log.debug( "putting gi for ga method" );

                // need to lean up the group putting
                /*
                 * GroupCache cache = (GroupCache)cacheMgr.getCache(cb.getCacheName());
                 * GroupAttrName gan = (GroupAttrName)cb.getKey();
                 * GroupId groupId = new GroupId( gan.groupId );
                 * cache.putGAN( gan, cb.getVal(), cb.getElementAttributes(), false);
                 */
                ICompositeCache cache = ( ICompositeCache ) cacheMgr.getCache( cb.getCacheName() );
                cache.update( cb, false );

            }
            catch ( Exception ioe )
            {
            }
            return;
        }
        super.handlePut( cb );
    }


    // override for new funcitonality
    // lazy init is too slow, find a better way
    /**
     * Gets the cacheManager attribute of the LateralGroupCacheUDPListener
     * object
     */
    protected void getCacheManager()
    {
        try
        {
            if ( cacheMgr == null )
            {
                cacheMgr = CacheHub.getInstance();

                if ( log.isDebugEnabled() )
                {
                    log.debug( " groupcache cacheMgr = " + cacheMgr );
                }
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "already got groupcache cacheMgr = " + cacheMgr );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

}
