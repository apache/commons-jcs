
package org.apache.jcs.auxiliary.remote;

import java.io.InputStream;
import java.io.IOException;

import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteUtils
{
    private final static Log log =
        LogFactory.getLog( RemoteUtils.class );

    /** Constructor for the RemoteUtils object */
    private RemoteUtils() { }


    /**
     * Creates and exports a registry on the specified port of the local host.
     */
    public static int createRegistry( int port )
        throws RemoteException
    {
        log.debug( "createRegistry> setting security manager" );
        System.setSecurityManager( new RMISecurityManager() );
        if ( port < 1024 )
        {
            port = Registry.REGISTRY_PORT;
        }
        log.debug( "createRegistry> creating registry" );
        LocateRegistry.createRegistry( port );
        return port;
    }


    /** Description of the Method */
    public static Properties loadProps( String propFile )
        throws IOException
    {
        InputStream is = RemoteUtils.class.getResourceAsStream( propFile );
        Properties props = new Properties();
        try
        {
            props.load( is );
            if ( log.isDebugEnabled() )
            {
                log.debug( "props.size=" + props.size() + ", " + props );
            }
        }
        catch ( Exception ex )
        {
            // ignore;
        }
        finally
        {
            if ( is != null )
            {
                is.close();
            }
        }
        return props;
    }
}

