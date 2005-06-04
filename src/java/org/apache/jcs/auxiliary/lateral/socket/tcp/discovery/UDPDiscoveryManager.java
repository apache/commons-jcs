package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;

/**
 * This manages UDPDiscovery Services. We should end up with one service per
 * Lateral Cache Manager Instance. One service works for multiple regions. We
 * don't want a connection for each region.
 * 
 * @author Aaron Smuts
 *  
 */
public class UDPDiscoveryManager
{
    private final static Log log = LogFactory.getLog( UDPDiscoveryManager.class );

    private static UDPDiscoveryManager INSTANCE = new UDPDiscoveryManager();

    private Map services = new HashMap();

    private UDPDiscoveryManager()
    {
        // noopt
    }

    /**
     * Singelton
     * 
     * @return UDPDiscoveryManager
     */
    public static UDPDiscoveryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns the UDP Discovery service associated with this instance.
     * 
     * @param lca
     * @return
     */
    public synchronized UDPDiscoveryService getService( LateralCacheAttributes lca )
    {
        String key = lca.getTcpServer() + ":" + lca.getTcpListenerPort();

        UDPDiscoveryService service = (UDPDiscoveryService) services.get( key );
        if ( service == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Creating service for " + key );
            }

            service = new UDPDiscoveryService( lca );
            services.put( key, service );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Returning service [" + service + "] for lca [" + lca + "]" );
        }

        return service;
    }

}
