package org.apache.commons.jcs.auxiliary.lateral.socket.tcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The factory holds an instance of this manager. This manager has a map of listeners, keyed to the
 * discovery configuration. I'm not using a static map, because I'm trying to make JCS
 * multi-instance.
 * <p>
 * During configuration, the factory is only created once per auxiliary definition. Two different
 * laterals cannot use the same discovery service. We will likely want to change this.
 */
public class LateralTCPDiscoveryListenerManager
{
    /** Map of available instances, keyed by port. Note, this is not static. */
    protected Map<String, LateralTCPDiscoveryListener> instances =
        Collections.synchronizedMap( new HashMap<String, LateralTCPDiscoveryListener>() );

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
        LateralTCPDiscoveryListener ins = instances.get( key );

        if ( ins == null )
        {
            ins = new LateralTCPDiscoveryListener( cacheMgr, cacheEventLogger, elementSerializer );

            instances.put( key, ins );

            if ( log.isInfoEnabled() )
            {
                log.info( "Created new discovery listener for " + key + " cacheName for request " + ilca.getCacheName() );
            }
        }

        return ins;
    }
}
