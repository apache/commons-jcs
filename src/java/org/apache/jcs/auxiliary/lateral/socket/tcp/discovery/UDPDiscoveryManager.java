package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.socket.tcp.behavior.ITCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;

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
     *            ITCPLateralCacheAttributes
     * @param cacheMgr
     * @return
     */
    public synchronized UDPDiscoveryService getService( ITCPLateralCacheAttributes lca, ICompositeCacheManager cacheMgr )
    {
        UDPDiscoveryService service = getService( lca.getTcpServer(), lca.getTcpListenerPort(), lca.getTcpListenerPort(), cacheMgr );
        
        // TODO find a way to remote these attributes from the service, the manager needs it on disocvery.
        service.setTcpLateralCacheAttributes( lca );
        return service;
    }

    /**
     * Creates a service for the address and port if one doesn't exist already.
     * <p>
     * TODO we may need to key this using the listener port too
     * 
     * @param discoveryAddress
     * @param discoveryPort
     * @param servicePort
     * @param cacheMgr
     * @return
     */
    public synchronized UDPDiscoveryService getService( String discoveryAddress, int discoveryPort, int servicePort,
                                                       ICompositeCacheManager cacheMgr )
    {
        String key = discoveryAddress + ":" + discoveryPort;

        UDPDiscoveryService service = (UDPDiscoveryService) services.get( key );
        if ( service == null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Creating service for " + key );
            }

            service = new UDPDiscoveryService( discoveryAddress, discoveryPort, servicePort, cacheMgr );
            services.put( key, service );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Returning service [" + service + "] for key [" + key + "]" );
        }

        return service;

    }

}
