package org.apache.jcs.auxiliary.remote.server.group;

import java.io.IOException;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;

import java.util.Properties;

import org.apache.tomcat.startup.Tomcat;

import org.apache.jcs.auxiliary.remote.RemoteUtils;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheServiceAdmin;
import org.apache.jcs.auxiliary.remote.server.RemoteCacheServerAttributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides remote session cache services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteGroupCacheServerFactory
     implements IRemoteCacheConstants
{
    private final static Log log =
        LogFactory.getLog( RemoteGroupCacheServerFactory.class );

    /** The single instance of the RemoteGroupCacheServer object. */
    private static RemoteGroupCacheServer instance;
    private static String serviceName;

    /** Constructor for the RemoteGroupCacheServerFactory object */
    private RemoteGroupCacheServerFactory() { }


    /////////////////////// Statup/shutdown methods. //////////////////
    /**
     * Starts up the remote session cache server on this JVM, and binds it to
     * the registry on the given host and port.
     */
    public static void startup( String host, int port, String propFile )
        throws IOException,
        NotBoundException
    {
        if ( instance != null )
        {
            throw new IllegalArgumentException( "Server already started." );
        }
        Properties prop = null;
        synchronized ( RemoteGroupCacheServer.class )
        {

            if ( instance != null )
            {
                return;
            }

            // TODO: make automatic
            RemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();

            log.debug( "Configuration file = " + propFile );
            rcsa.setConfigFileName( propFile );

            prop = RemoteUtils.loadProps( propFile );

            String servicePortStr = prop.getProperty( REMOTE_CACHE_SERVICE_PORT );
            int servicePort = -1;
            try
            {
                servicePort = Integer.parseInt( servicePortStr );

                rcsa.setServicePort( servicePort );
                log.debug( "Remote cache service uses port number " + servicePort + "." );
            }
            catch ( NumberFormatException ignore )
            {
                log.debug( "Remote cache service port property " + REMOTE_CACHE_SERVICE_PORT + " not specified.  An anonymous port will be used." );
            }

            String lccStr = prop.getProperty( REMOTE_LOCAL_CLUSTER_CONSISTENCY );
            if ( lccStr == null )
            {
                lccStr = "true";
            }
            boolean lcc = Boolean.valueOf( lccStr ).booleanValue();
            rcsa.setLocalClusterConsistency( lcc );

            instance = new RemoteGroupCacheServer( rcsa );

            if ( host == null )
            {
                host = "";
            }
            // Register the RemoteCacheServer remote object in the registry.
            serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
            log.debug( "main> binding server to " + host + ":" + port + " with the name " +
                serviceName );
            try
            {
                Naming.rebind( "//" + host + ":" + port + "/" + serviceName, instance );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host
                     + ", port=" + port );
            }

        }

        // Start up tomcat inside remote cache, useful for monitoring
        boolean tomcatOn = Boolean.valueOf( prop.getProperty( TOMCAT_ON, "true" ).trim() ).booleanValue();
        log.debug( "tomcatOn = " + tomcatOn );
        if ( tomcatOn )
        {
            String tomcatXml = prop.getProperty( TOMCAT_XML, "C:/dev/jakarta-turbine-stratum/props/remote.tomcat.xml" ).trim();
            startupTomcat( new String[]{"-config", tomcatXml} );
        }
    }

    /** Description of the Method */
    private static void startupTomcat( String[] arg )
    {
        String[] dftConfig = {"-config", "C:/dev/jakarta-turbine-stratum/bin/conf/remote.tomcat.xml"};

        if ( arg.length == 0 )
        {
            System.err.println( "Using default configuration: bin/conf/remote.tomcat.xml" );
            arg = dftConfig;
        }

        for ( int i = 0; i < arg.length; i++ )
        {
            try
            {
                Tomcat.main( arg );
                log.debug( "started tomcat" );
            }
            catch ( RuntimeException e )
            {
                log.error( e );
            }
        }
    }


    /** Description of the Method */
    static void shutdownImpl( String host, int port )
        throws IOException
    {
        if ( instance == null )
        {
            return;
        }
        synchronized ( RemoteGroupCacheServer.class )
        {
            if ( instance == null )
            {
                return;
            }
            log.debug( "Unbinding host=" + host + ", port=" + port + ", serviceName=" + serviceName );
            try
            {
                Naming.unbind( "//" + host + ":" + port + "/" + serviceName );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host
                     + ", port=" + port + ", serviceName=" + serviceName );
            }
            catch ( NotBoundException ex )
            {
                //ignore.
            }
            instance.release();
            instance = null;
            // TODO: safer exit ?
            try
            {
                Thread.currentThread().sleep( 2000 );
            }
            catch ( InterruptedException ex )
            {
            }
            System.exit( 0 );
        }
    }


    /**
     * Creates an local Remote registry on the default port, starts up the
     * remote session cache server, and binds it to the registry.
     *
     * @param args The command line arguments
     */
    public static void main( String[] args )
        throws Exception
    {
        Properties prop = args.length > 0 ? RemoteUtils.loadProps( args[0] ) : new Properties();

        // shutdown
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-shutdown" ) != -1 )
        {

            log.debug( "Shutting down cache" );

            try
            {

                int port = Registry.REGISTRY_PORT;

                if ( args.length > 1 )
                {
                    port = Integer.parseInt( args[1] );
                }

                String serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
                // shutdown.
                String registry = "//:" + port + "/" + serviceName;
                //if (debug) {
                log.debug( "looking up server " + registry );
                //}
                Object obj = Naming.lookup( registry );
                //if (debug) {
                log.debug( "server found" );
                //}

                log.debug( "obj = " + obj );
                IRemoteCacheServiceAdmin admin = ( IRemoteCacheServiceAdmin ) obj;

                try
                {
                    admin.shutdown( "", port );
                }
                catch ( Exception er )
                {
                    // ignore the error
                    // the connection will be closed by the server
                }

            }
            catch ( Exception ex )
            {
                log.error( ex );
            }
            log.debug( "done" );
            System.exit( 0 );
        }

        // STATS
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-stats" ) != -1 )
        {

            log.debug( "getting cache stats" );

            try
            {

                int port = Registry.REGISTRY_PORT;

                if ( args.length > 1 )
                {
                    port = Integer.parseInt( args[1] );
                }

                String serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
                String registry = "//:" + port + "/" + serviceName;
                log.debug( "looking up server " + registry );
                Object obj = Naming.lookup( registry );
                log.debug( "server found" );

                log.debug( "obj = " + obj );
                IRemoteCacheServiceAdmin admin = ( IRemoteCacheServiceAdmin ) obj;

                try
                {
                    log.debug( admin.getStats() );
                }
                catch ( Exception es )
                {
                    log.error( es );
                }

            }
            catch ( Exception ex )
            {
                log.error( ex );
            }
            log.debug( "done." );
            System.exit( 0 );
        }

        // startup.
        int port;
        try
        {
            port = Integer.parseInt( prop.getProperty( "registry.port" ) );
        }
        catch ( NumberFormatException ex )
        {
            port = Registry.REGISTRY_PORT;
        }
        String host = prop.getProperty( "registry.host" );
        if ( host == null || host.trim().equals( "" ) || host.trim().equals( "localhost" ) )
        {
            log.debug( "main> creating registry on the localhost" );
            port = RemoteUtils.createRegistry( port );
        }
        log.debug( "main> starting up RemoteGroupCacheServer" );
        RemoteGroupCacheServerFactory.startup( host, port, args.length > 0 ? args[0] :
            null );
        log.debug( "main> done" );
    }
}

