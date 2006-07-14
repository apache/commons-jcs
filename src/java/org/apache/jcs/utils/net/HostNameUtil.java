package org.apache.jcs.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple utility for getting the local host name.
 * <p>
 * @author Aaron Smuts
 */
public class HostNameUtil
{
    private final static Log log = LogFactory.getLog( HostNameUtil.class );

    /**
     * Gets the address for the local machine.
     * <p>
     * @return InetAddress.getLocalHost().getHostAddress(), or unknown if there
     *         is an error.
     */
    public static String getLocalHostAddress()
    {
        String hostAddress = "unknown";
        try
        {
            // todo, you should be able to set this
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            if ( log.isDebugEnabled() )
            {
                log.debug( "hostAddress = [" + hostAddress + "]" );
            }
        }
        catch ( UnknownHostException e1 )
        {
            log.error( "Couldn't get localhost address", e1 );
        }
        return hostAddress;
    }
}
