package org.apache.jcs.auxiliary.lateral.socket.tcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IElementSerializer;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * The factory holds an instance of this maanger. This manager has a map of listeners, keyed to the
 * discovery configuration. I'm not using a static map, because I'm trying to make JCS
 * multi-instance.
 * <p>
 * During configuration, the factory is only created once per auxiliary definition. Two different
 * laterals canot use the same discovery service. We will likey wantt o change this.
 */
public class LateralTCPDiscoveryListenerManager
{
    /** Map of available instances, keyed by port. Note, this is not static. */
    protected Map instances = Collections.synchronizedMap( new HashMap() );

    /** The logger */
    private final static Log log = LogFactory.getLog( LateralTCPDiscoveryListenerManager.class );

    /** Does nothing. */
    public LateralTCPDiscoveryListenerManager()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Creating new LateralTCPDiscoveryListenerManager" );
        }
    }

    /**
     * Gets the instance attribute of the LateralCacheTCPListener class.
     * <p>
     * @param ilca ITCPLateralCacheAttributes
     * @param cacheMgr
     * @param cacheEventLogger
     * @param elementSerializer
     * @return The instance value
     */
    public synchronized LateralTCPDiscoveryListener getDiscoveryListener( ITCPLateralCacheAttributes ilca,
                                                                          ICompositeCacheManager cacheMgr,
                                                                          ICacheEventLogger cacheEventLogger,
                                                                          IElementSerializer elementSerializer )
    {
        String key = ilca.getUdpDiscoveryAddr() + ":" + ilca.getUdpDiscoveryPort();
        LateralTCPDiscoveryListener ins = (LateralTCPDiscoveryListener) instances.get( key );

        if ( ins == null )
        {
            ins = new LateralTCPDiscoveryListener( cacheMgr, cacheEventLogger, elementSerializer );

            instances.put( String.valueOf( ilca.getTcpListenerPort() ), ins );

            if ( log.isDebugEnabled() )
            {
                log.debug( "created new listener " + ilca.getTcpListenerPort() );
            }
        }

        return ins;
    }
}
