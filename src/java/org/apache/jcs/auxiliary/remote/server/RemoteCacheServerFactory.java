package org.apache.jcs.auxiliary.remote.server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.jcs.auxiliary.remote.RemoteUtils;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheServiceAdmin;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * Provides remote cache services. This creates remote cache servers and can proxy command line
 * requests to a running server.
 */
public class RemoteCacheServerFactory
    implements IRemoteCacheConstants
{
    /** The logger */
    private final static Log log = LogFactory.getLog( RemoteCacheServerFactory.class );

    /** The single instance of the RemoteCacheServer object. */
    private static RemoteCacheServer remoteCacheServer;

    /** The name of the service. */
    private static String serviceName;

    /** Constructor for the RemoteCacheServerFactory object. */
    private RemoteCacheServerFactory()
    {
        super();
    }

    /**
     * This will allow you to get stats from the server, etc. Perhaps we should provide methods on
     * the factory to do this instead.
     * <p>
     * A remote cache is either a local cache or a cluster cache. <p.
     * @return Returns the remoteCacheServer.
     */
    public static RemoteCacheServer getRemoteCacheServer()
    {
        return remoteCacheServer;
    }

    // ///////////////////// Startup/shutdown methods. //////////////////
    /**
     * Starts up the remote cache server on this JVM, and binds it to the registry on the given host
     * and port.
     * <p>
     * A remote cache is either a local cache or a cluster cache.
     * <p>
     * @param host
     * @param port
     * @param propFile
     * @throws IOException
     */
    public static void startup( String host, int port, String propFile )
        throws IOException
    {
        if ( remoteCacheServer != null )
        {
            throw new IllegalArgumentException( "Server already started." );
        }

        synchronized ( RemoteCacheServer.class )
        {
            if ( remoteCacheServer != null )
            {
                return;
            }
            if ( host == null )
            {
                host = "";
            }
            if ( log.isInfoEnabled() )
            {
                log.info( "ConfigFileName = [" + propFile + "]" );
            }
            Properties props = RemoteUtils.loadProps( propFile );
            RemoteCacheServerAttributes rcsa = configureServerAttributes( propFile );
            // These should come from the file!
            rcsa.setRemotePort( port );
            rcsa.setRemoteHost( host );
            if ( log.isInfoEnabled() )
            {
                log.info( "Creating server with these attributes: " + rcsa );
            }

            serviceName = rcsa.getRemoteServiceName();

            RemoteUtils.configureCustomSocketFactory( rcsa.getRmiSocketFactoryTimeoutMillis() );

            // CONFIGURE THE EVENT LOGGER
            ICacheEventLogger cacheEventLogger = AuxiliaryCacheConfigurator
                .parseCacheEventLogger( props, IRemoteCacheConstants.PROPERTY_PREFIX );

            // CREATE SERVER
            remoteCacheServer = new RemoteCacheServer( rcsa );
            remoteCacheServer.setCacheEventLogger( cacheEventLogger );

            if ( log.isInfoEnabled() )
            {
                log.info( "Binding server to " + host + ":" + port + " with the name " + serviceName );
            }
            try
            {
                Naming.rebind( "//" + host + ":" + port + "/" + serviceName, remoteCacheServer );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host + ", port=" + port );
            }
        }
    }

    /**
     * Configures the RemoteCacheServerAttributes from the props file.
     * <p>
     * @param propFile
     * @return RemoteCacheServerAttributes
     * @throws IOException
     */
    protected static RemoteCacheServerAttributes configureServerAttributes( String propFile )
        throws IOException
    {
        Properties prop = RemoteUtils.loadProps( propFile );
        // Properties prop = PropertyLoader.loadProperties( propFile );

        RemoteCacheServerAttributes rcsa = configureRemoteCacheServerAttributes( prop );
        rcsa.setConfigFileName( propFile );

        return rcsa;
    }

    /**
     * Configure.
     * <p>
     * @param prop
     * @return RemoteCacheServerAttributesconfigureRemoteCacheServerAttributes
     */
    protected static RemoteCacheServerAttributes configureRemoteCacheServerAttributes( Properties prop )
    {
        // TODO: make automatic
        RemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();

        String servicePortStr = prop.getProperty( REMOTE_CACHE_SERVICE_PORT );
        try
        {
            int servicePort = Integer.parseInt( servicePortStr );
            rcsa.setServicePort( servicePort );
            log.debug( "Remote cache service uses port number " + servicePort + "." );
        }
        catch ( NumberFormatException ignore )
        {
            log.debug( "Remote cache service port property " + REMOTE_CACHE_SERVICE_PORT
                + " not specified.  An anonymous port will be used." );
        }

        String socketTimeoutMillisStr = prop.getProperty( SOCKET_TIMEOUT_MILLIS );
        try
        {
            int rmiSocketFactoryTimeoutMillis = Integer.parseInt( socketTimeoutMillisStr );
            rcsa.setRmiSocketFactoryTimeoutMillis( rmiSocketFactoryTimeoutMillis );
            log.debug( "Remote cache socket timeout " + rmiSocketFactoryTimeoutMillis + "ms." );
        }
        catch ( NumberFormatException ignore )
        {
            log.debug( "Remote cache socket timeout property " + SOCKET_TIMEOUT_MILLIS
                + " not specified.  The default will be used." );
        }

        String lccStr = prop.getProperty( REMOTE_LOCAL_CLUSTER_CONSISTENCY );
        if ( lccStr == null )
        {
            lccStr = "true";
        }
        boolean lcc = Boolean.valueOf( lccStr ).booleanValue();
        rcsa.setLocalClusterConsistency( lcc );

        String acgStr = prop.getProperty( REMOTE_ALLOW_CLUSTER_GET );
        if ( acgStr == null )
        {
            acgStr = "true";
        }
        boolean acg = Boolean.valueOf( acgStr ).booleanValue();
        rcsa.setAllowClusterGet( acg );

        // Register the RemoteCacheServer remote object in the registry.
        rcsa.setRemoteServiceName( prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim() );
        return rcsa;
    }

    /**
     * Unbinds the remote server.
     * <p>
     * @param host
     * @param port
     * @exception IOException
     */
    static void shutdownImpl( String host, int port )
        throws IOException
    {
        if ( remoteCacheServer == null )
        {
            return;
        }
        synchronized ( RemoteCacheServer.class )
        {
            if ( remoteCacheServer == null )
            {
                return;
            }
            log.info( "Unbinding host=" + host + ", port=" + port + ", serviceName=" + serviceName );
            try
            {
                Naming.unbind( "//" + host + ":" + port + "/" + serviceName );
            }
            catch ( MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host + ", port=" + port
                    + ", serviceName=" + serviceName );
            }
            catch ( NotBoundException ex )
            {
                // ignore.
            }
            remoteCacheServer.release();
            remoteCacheServer = null;
            // TODO: safer exit ?
            try
            {
                Thread.sleep( 2000 );
            }
            catch ( InterruptedException ex )
            {
                // swallow
            }
            System.exit( 0 );
        }
    }

    /**
     * Creates an local RMI registry on the default port, starts up the remote cache server, and
     * binds it to the registry.
     * <p>
     * A remote cache is either a local cache or a cluster cache.
     * <p>
     * @param args The command line arguments
     * @throws Exception
     */
    public static void main( String[] args )
        throws Exception
    {
        Properties prop = args.length > 0 ? RemoteUtils.loadProps( args[args.length - 1] ) : new Properties();

        int port;
        try
        {
            port = Integer.parseInt( prop.getProperty( "registry.port" ) );
        }
        catch ( NumberFormatException ex )
        {
            port = Registry.REGISTRY_PORT;
        }

        // shutdown
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-shutdown" ) != -1 )
        {
            String serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
            String registry = "//:" + port + "/" + serviceName;

            if ( log.isDebugEnabled() )
            {
                log.debug( "looking up server " + registry );
            }
            Object obj = Naming.lookup( registry );
            if ( log.isDebugEnabled() )
            {
                log.debug( "server found" );
            }
            IRemoteCacheServiceAdmin admin = (IRemoteCacheServiceAdmin) obj;
            try
            {
                admin.shutdown();
            }
            catch ( Exception ex )
            {
                log.error( "Problem calling shutdown.", ex );
            }
            log.debug( "done." );
            System.exit( 0 );
        }

        // STATS
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-stats" ) != -1 )
        {

            log.debug( "getting cache stats" );

            try
            {
                String serviceName = prop.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
                String registry = "//:" + port + "/" + serviceName;
                log.debug( "looking up server " + registry );
                Object obj = Naming.lookup( registry );
                log.debug( "server found" );

                log.debug( "obj = " + obj );
                IRemoteCacheServiceAdmin admin = (IRemoteCacheServiceAdmin) obj;

                try
                {
                    System.out.println( admin.getStats().toString() );
                    log.debug( admin.getStats() );
                }
                catch ( Exception es )
                {
                    log.error( es );
                }

            }
            catch ( Exception ex )
            {
                log.error( "Problem getting stats.", ex );
            }
            log.debug( "done." );
            System.exit( 0 );
        }

        // startup.
        String host = prop.getProperty( "registry.host" );

        if ( host == null || host.trim().equals( "" ) || host.trim().equals( "localhost" ) )
        {
            log.debug( "main> creating registry on the localhost" );
            port = RemoteUtils.createRegistry( port );
        }
        log.debug( "main> starting up RemoteCacheServer" );
        RemoteCacheServerFactory.startup( host, port, args.length > 0 ? args[0] : null );
        log.debug( "main> done" );
    }
}
