package org.apache.commons.jcs3.auxiliary.remote.server;

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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.commons.jcs3.auxiliary.remote.RemoteUtils;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceAdmin;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.config.OptionConverter;
import org.apache.commons.jcs3.utils.config.PropertySetter;
import org.apache.commons.jcs3.utils.threadpool.DaemonThreadFactory;

/**
 * Provides remote cache services. This creates remote cache servers and can proxy command line
 * requests to a running server.
 */
public class RemoteCacheServerFactory
    implements IRemoteCacheConstants
{
    /** The logger */
    private static final Log log = LogManager.getLog( RemoteCacheServerFactory.class );

    /** The single instance of the RemoteCacheServer object. */
    private static RemoteCacheServer<?, ?> remoteCacheServer;

    /** The name of the service. */
    private static String serviceName = IRemoteCacheConstants.REMOTE_CACHE_SERVICE_VAL;

    /** Executes the registry keep alive. */
    private static ScheduledExecutorService keepAliveDaemon;

    /** A reference to the registry. */
    private static Registry registry;

    /** Constructor for the RemoteCacheServerFactory object. */
    private RemoteCacheServerFactory()
    {
    }

    /**
     * This will allow you to get stats from the server, etc. Perhaps we should provide methods on
     * the factory to do this instead.
     * <p>
     * A remote cache is either a local cache or a cluster cache.
     * </p>
     * @return Returns the remoteCacheServer.
     */
    @SuppressWarnings("unchecked") // Need cast to specific RemoteCacheServer
    public static <K, V> RemoteCacheServer<K, V> getRemoteCacheServer()
    {
        return (RemoteCacheServer<K, V>)remoteCacheServer;
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
     * @param props
     * @throws IOException
     */
    public static void startup( String host, final int port, final Properties props)
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

            final RemoteCacheServerAttributes rcsa = configureRemoteCacheServerAttributes(props);

            // These should come from the file!
            rcsa.setRemoteLocation( Objects.toString(host, ""), port );
            log.info( "Creating server with these attributes: {0}", rcsa );

            setServiceName( rcsa.getRemoteServiceName() );

            final RMISocketFactory customRMISocketFactory = configureObjectSpecificCustomFactory( props );

            RemoteUtils.configureGlobalCustomSocketFactory( rcsa.getRmiSocketFactoryTimeoutMillis() );

            // CONFIGURE THE EVENT LOGGER
            final ICacheEventLogger cacheEventLogger = configureCacheEventLogger( props );

            // CREATE SERVER
            if ( customRMISocketFactory != null )
            {
                remoteCacheServer = new RemoteCacheServer<>( rcsa, props, customRMISocketFactory );
            }
            else
            {
                remoteCacheServer = new RemoteCacheServer<>( rcsa, props );
            }

            remoteCacheServer.setCacheEventLogger( cacheEventLogger );

            // START THE REGISTRY
        	registry = RemoteUtils.createRegistry(port);

            // REGISTER THE SERVER
            registerServer( serviceName, remoteCacheServer );

            // KEEP THE REGISTRY ALIVE
            if ( rcsa.isUseRegistryKeepAlive() )
            {
                if ( keepAliveDaemon == null )
                {
                    keepAliveDaemon = Executors.newScheduledThreadPool(1,
                            new DaemonThreadFactory("JCS-RemoteCacheServerFactory-"));
                }
                keepAliveDaemon.scheduleAtFixedRate(() -> keepAlive(host, port, cacheEventLogger),
                        0, rcsa.getRegistryKeepAliveDelayMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Tries to lookup the server. If unsuccessful it will rebind the server using the factory
     * rebind method.
     *
     * @param registryHost - Hostname of the registry
     * @param registryPort - the port on which to start the registry
     * @param cacheEventLogger the event logger for error messages
     * @since 3.1
     */
    protected static void keepAlive(String registryHost, int registryPort, ICacheEventLogger cacheEventLogger)
    {
        String namingURL = RemoteUtils.getNamingURL(registryHost, registryPort, serviceName);
        log.debug( "looking up server {0}", namingURL );

        try
        {
            final Object obj = Naming.lookup( namingURL );

            // Successful connection to the remote server.
            final String message = "RMI registry looks fine.  Found [" + obj + "] in registry [" + namingURL + "]";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "Naming.lookup", message );
            }
            log.debug( message );
        }
        catch ( final Exception ex )
        {
            // Failed to connect to the remote server.
            final String message = "Problem finding server at [" + namingURL
                + "].  Will attempt to start registry and rebind.";
            log.error( message, ex );
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RegistryKeepAliveRunner", "Naming.lookup", message + ":" + ex.getMessage() );
            }

            registry = RemoteUtils.createRegistry(registryPort);

            if ( cacheEventLogger != null )
            {
                if (registry != null)
                {
                    cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "createRegistry",
                            "Successfully created registry [" + serviceName + "]." );
                }
                else
                {
                    cacheEventLogger.logError( "RegistryKeepAliveRunner", "createRegistry",
                            "Could not start registry [" + serviceName + "]." );
                }
            }
        }

        try
        {
            registerServer(serviceName, remoteCacheServer);

            final String message = "Successfully rebound server to registry [" + serviceName + "].";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RegistryKeepAliveRunner", "registerServer", message );
            }
            log.info( message );
        }
        catch ( final RemoteException e )
        {
            final String message = "Could not rebind server to registry [" + serviceName + "].";
            log.error( message, e );
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RegistryKeepAliveRunner", "registerServer", message + ":"
                    + e.getMessage() );
            }
        }
    }

    /**
     * Tries to get the event logger.
     * <p>
     * @param props configuration properties
     * @return ICacheEventLogger, may be null
     */
    protected static ICacheEventLogger configureCacheEventLogger( final Properties props )
    {
        return AuxiliaryCacheConfigurator
                .parseCacheEventLogger( props, IRemoteCacheConstants.CACHE_SERVER_PREFIX );
    }

    /**
     * This configures an object specific custom factory. This will be configured for just this
     * object in the registry. This can be null.
     * <p>
     * @param props
     * @return RMISocketFactory
     */
    protected static RMISocketFactory configureObjectSpecificCustomFactory( final Properties props )
    {
        final RMISocketFactory customRMISocketFactory =
            OptionConverter.instantiateByKey( props, CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX, null );

        if ( customRMISocketFactory != null )
        {
            PropertySetter.setProperties( customRMISocketFactory, props, CUSTOM_RMI_SOCKET_FACTORY_PROPERTY_PREFIX
                + "." );
            log.info( "Will use server specific custom socket factory. {0}",
                    customRMISocketFactory );
        }
        else
        {
            log.info( "No server specific custom socket factory defined." );
        }
        return customRMISocketFactory;
    }

    /**
     * Registers the server with the registry. I broke this off because we might want to have code
     * that will restart a dead registry. It will need to rebind the server.
     * <p>
     * @param serviceName the name of the service
     * @param server the server object to bind
     * @throws RemoteException
     */
    protected static void registerServer(final String serviceName, final Remote server )
        throws RemoteException
    {
        if ( server == null )
        {
            throw new RemoteException( "Cannot register the server until it is created." );
        }

        if ( registry == null )
        {
            throw new RemoteException( "Cannot register the server: Registry is null." );
        }

        log.info( "Binding server to {0}", serviceName );

        registry.rebind( serviceName, server );
    }

    /**
     * Configure.
     * <p>
     * jcs.remotecache.serverattributes.ATTRIBUTENAME=ATTRIBUTEVALUE
     * <p>
     * @param prop
     * @return RemoteCacheServerAttributesconfigureRemoteCacheServerAttributes
     */
    protected static RemoteCacheServerAttributes configureRemoteCacheServerAttributes( final Properties prop )
    {
        final RemoteCacheServerAttributes rcsa = new RemoteCacheServerAttributes();

        // configure automatically
        PropertySetter.setProperties( rcsa, prop, CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX + "." );

        return rcsa;
    }

    /**
     * Unbinds the remote server.
     * <p>
     * @param host
     * @param port
     * @throws IOException
     */
    static void shutdownImpl( final String host, final int port )
        throws IOException
    {
        synchronized ( RemoteCacheServer.class )
        {
            if ( remoteCacheServer == null )
            {
                return;
            }
            log.info( "Unbinding host={0}, port={1}, serviceName={2}",
                    host, port, getServiceName() );
            try
            {
                Naming.unbind( RemoteUtils.getNamingURL(host, port, getServiceName()) );
            }
            catch ( final MalformedURLException ex )
            {
                // impossible case.
                throw new IllegalArgumentException( ex.getMessage() + "; host=" + host + ", port=" + port
                    + ", serviceName=" + getServiceName() );
            }
            catch ( final NotBoundException ex )
            {
                // ignore.
            }
            remoteCacheServer.release();
            remoteCacheServer = null;

            // Shut down keepalive scheduler
            if ( keepAliveDaemon != null )
            {
                keepAliveDaemon.shutdownNow();
                keepAliveDaemon = null;
            }

            // Try to release registry
            if (registry != null)
            {
            	UnicastRemoteObject.unexportObject(registry, true);
            	registry = null;
            }
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
    public static void main( final String[] args )
        throws Exception
    {
        final Properties prop = args.length > 0 ? RemoteUtils.loadProps( args[args.length - 1] ) : new Properties();

        int port;
        try
        {
            port = Integer.parseInt( prop.getProperty( "registry.port" ) );
        }
        catch ( final NumberFormatException ex )
        {
            port = Registry.REGISTRY_PORT;
        }

        // shutdown
        if ( args.length > 0 && args[0].toLowerCase().indexOf( "-shutdown" ) != -1 )
        {
            try
            {
                final ICacheServiceAdmin admin = lookupCacheServiceAdmin(prop, port);
                admin.shutdown();
            }
            catch ( final Exception ex )
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
                final ICacheServiceAdmin admin = lookupCacheServiceAdmin(prop, port);

                try
                {
//                    System.out.println( admin.getStats().toString() );
                    log.debug( admin.getStats() );
                }
                catch ( final IOException es )
                {
                    log.error( es );
                }
            }
            catch ( final Exception ex )
            {
                log.error( "Problem getting stats.", ex );
            }
            log.debug( "done." );
            System.exit( 0 );
        }

        // startup.
        final String hostName = prop.getProperty( "registry.host" );
        final InetAddress host = InetAddress.getByName(hostName);

        if (host.isLoopbackAddress())
        {
            log.debug( "main> creating registry on the localhost" );
            RemoteUtils.createRegistry( port );
        }
        log.debug( "main> starting up RemoteCacheServer" );
        startup( host.getHostName(), port, prop);
        log.debug( "main> done" );
    }

    /**
     * Look up the remote cache service admin instance
     *
     * @param config the configuration properties
     * @param port the local port
     * @return the admin object instance
     *
     * @throws Exception if lookup fails
     */
    private static ICacheServiceAdmin lookupCacheServiceAdmin(final Properties config, final int port) throws Exception
    {
        final String remoteServiceName = config.getProperty( REMOTE_CACHE_SERVICE_NAME, REMOTE_CACHE_SERVICE_VAL ).trim();
        final String registry = RemoteUtils.getNamingURL("", port, remoteServiceName);

        log.debug( "looking up server {0}", registry );
        final Object obj = Naming.lookup( registry );
        log.debug( "server found" );

        return (ICacheServiceAdmin) obj;
    }

    /**
     * @param serviceName the serviceName to set
     */
    protected static void setServiceName( final String serviceName )
    {
        RemoteCacheServerFactory.serviceName = serviceName;
    }

    /**
     * @return the serviceName
     */
    protected static String getServiceName()
    {
        return serviceName;
    }
}
