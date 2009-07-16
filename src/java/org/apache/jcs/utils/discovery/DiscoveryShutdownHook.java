package org.apache.jcs.utils.discovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Calls shutdown on the manager when the vm is existing. On an orderly shutdown, then will result
 * in the manager issuing a remove message.
 * <p>
 * The service is also registered with the composite cache manager to receive shutdown notification.
 * <p>
 * @author Aaron Smuts
 */
public class DiscoveryShutdownHook
    extends Thread
{
    /** log instance */
    private static final Log log = LogFactory.getLog( DiscoveryShutdownHook.class );

    /** service to shut down */
    private UDPDiscoveryService service;

    /**
     * @param serviceArg service
     */
    public DiscoveryShutdownHook( UDPDiscoveryService serviceArg )
    {
        this.service = serviceArg;
    }

    /**
     * Just calls shutdown on the service.
     */
    public void run()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "UDP Discovery shutdown hook called." );
        }

        if ( service != null )
        {
            service.shutdown();
        }
        else
        {
            log.warn( "UDP Discovery Service was null." );
        }
    }
}
