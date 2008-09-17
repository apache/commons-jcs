package org.apache.jcs.auxiliary.remote.server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * This class tries to keep the resitry alive. If if is able to create a registry, it will also
 * rebind the remote cache server.
 */
public class RegistryKeepAliveRunner
    implements Runnable
{
    /** The logger */
    private final static Log log = LogFactory.getLog( RegistryKeepAliveRunner.class );

    /** Hostname of the registry */
    private String registryHost;

    /** the port on which to start the registry */
    private int registryPort;

    /** The name of the service to look for. */
    private String serviceName;

    /** An optional event logger */
    private ICacheEventLogger cacheEventLogger;

    /**
     * @param registryHost - Hostname of the registry
     * @param registryPort - the port on which to start the registry
     * @param serviceName
     */
    public RegistryKeepAliveRunner( String registryHost, int registryPort, String serviceName )
    {
        this.registryHost = registryHost;
        this.registryPort = registryPort;
        this.serviceName = serviceName;
    }

    /**
     * Tries to lookup the server. If unsuccessful it will rebind the server using the factory
     * rebind method.
     * <p>
     */
    public void run()
    {
        checkAndRestoreIfNeeded();
    }

    /**
     * Tries to lookup the server. If unsuccessful it will rebind the server using the factory
     * rebind method.
     */
    protected void checkAndRestoreIfNeeded()
    {
        String registry = "//" + registryHost + ":" + registryPort + "/" + serviceName;
        if ( log.isDebugEnabled() )
        {
            log.debug( "looking up server " + registry );
        }
        try
        {
            Object obj = Naming.lookup( registry );

            // Successful connection to the remote server.
            String message = "RMI registry looks fine.  Found [" + obj + "] in registry [" + registry + "]";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "Naming.lookup", message );
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( message );
            }
            obj = null;
        }
        catch ( Exception ex )
        {
            // Failed to connect to the remote server.
            String message = "Problem finding server at [" + registry
                + "].  Will attempt to start registry and rebind.";
            log.error( message, ex );
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RegistryKeepAliveRunner", "Naming.lookup", message + ":" + ex.getMessage() );
            }
            createAndRegister( registry );
        }
    }

    /**
     * Creates the registry and registers the server.
     * <p>
     * @param registry
     */
    protected void createAndRegister( String registry )
    {
        createReqistry( registry );

        registerServer( registry );
    }

    /**
     * Try to create the registry. Log errors
     * <p>
     * @param registry
     */
    protected void createReqistry( String registry )
    {
        try
        {
            LocateRegistry.createRegistry( registryPort );
            String message = "Successfully created registry [" + registry + "].";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "createRegistry", message );
            }
        }
        catch ( RemoteException e )
        {
            String message = "Could not start registry [" + registry + "].";
            log.error( message, e );
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RegistryKeepAliveRunner", "createRegistry", message + ":" + e.getMessage() );
            }
        }
    }

    /**
     * Try to rebind the server.
     * <p>
     * @param registry
     */
    protected void registerServer( String registry )
    {
        try
        {
            // try to rebind anyway
            RemoteCacheServerFactory.registerServer( registryHost, registryPort, serviceName );
            String message = "Successfully rebound server to registry [" + registry + "].";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "registerServer", message );
            }
            if ( log.isInfoEnabled() )
            {
                log.info( message );
            }
        }
        catch ( RemoteException e )
        {
            String message = "Could not rebind server to registry [" + registry + "].";
            log.error( message, e );
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RegistryKeepAliveRunner", "registerServer", message + ":"
                    + e.getMessage() );
            }
        }
    }

    /**
     * Allows it to be injected.
     * <p>
     * @param cacheEventLogger
     */
    public void setCacheEventLogger( ICacheEventLogger cacheEventLogger )
    {
        this.cacheEventLogger = cacheEventLogger;
    }
}
