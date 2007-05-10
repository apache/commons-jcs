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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.props.PropertyLoader;

/**
 *Starts the registry and runs the server via the factory.
 *<p>
 * @author Aaron Smuts
 *
 */
public class RemoteCacheServerStartupUtil
{
    private final static Log log = LogFactory.getLog( RemoteCacheServerStartupUtil.class );

    private static final int DEFAULT_REGISTRY_PORT = 1101;


    /**
     * Starts the registry on port "registry.port"
     * <p>
     * @param propsFileName
     * @return RemoteCacheServer
     */
    public static RemoteCacheServer startServerUsingProperties( String propsFileName )
    {
        // TODO load from props file or get as init param or get from jndi, or
        // all three
        int registryPort = DEFAULT_REGISTRY_PORT;

        try
        {
            Properties props = PropertyLoader.loadProperties( propsFileName );
            if ( props != null )
            {
                String portS = props.getProperty( "registry.port", String.valueOf( DEFAULT_REGISTRY_PORT ) );

                try
                {
                    registryPort = Integer.parseInt( portS );
                }
                catch ( NumberFormatException e )
                {
                    log.error( "Problem converting port to an int.", e );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem loading props.", e );
        }
        catch ( Throwable t )
        {
            log.error( "Problem loading props.", t );
        }

        // we will always use the local machine for the registry
        String registryHost;
        try
        {
            registryHost = InetAddress.getLocalHost().getHostAddress();

            if ( log.isDebugEnabled() )
            {
                log.debug( "registryHost = [" + registryHost + "]" );
            }

            if ( "localhost".equals( registryHost ) || "127.0.0.1".equals( registryHost ) )
            {
                log.warn( "The local address [" + registryHost
                    + "] is INVALID.  Other machines must be able to use the address to reach this server." );
            }

            try
            {
                LocateRegistry.createRegistry( registryPort );
            }
            catch ( RemoteException e )
            {
                log.error( "Problem creating registry.  It may already be started. " + e.getMessage() );
            }
            catch ( Throwable t )
            {
                log.error( "Problem creating registry.", t );
            }

            try
            {
                RemoteCacheServerFactory.startup( registryHost, registryPort, "/" + propsFileName );
            }
            catch ( IOException e )
            {
                log.error( "Problem starting remote cache server.", e );
            }

            catch ( Throwable t )
            {
                log.error( "Problem starting remote cache server.", t );
            }
        }
        catch ( UnknownHostException e )
        {
            log.error( "Could not get local address to use for the registry!", e );
        }

        return RemoteCacheServerFactory.getRemoteCacheServer();
    }

}
