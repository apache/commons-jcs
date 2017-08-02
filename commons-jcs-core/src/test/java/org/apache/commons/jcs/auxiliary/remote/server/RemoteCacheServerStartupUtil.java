package org.apache.commons.jcs.auxiliary.remote.server;

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
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.jcs.auxiliary.remote.RemoteUtils;
import org.apache.commons.jcs.utils.net.HostNameUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *Starts the registry and runs the server via the factory.
 *<p>
 * @author Aaron Smuts
 */
public class RemoteCacheServerStartupUtil
{
    /** The logger */
    private static final Log log = LogFactory.getLog( RemoteCacheServerStartupUtil.class );

    /** Registry to use in the test. */
    private static final int DEFAULT_REGISTRY_PORT = 1101;

    /**
     * Starts the registry on port "registry.port"
     * <p>
     * @param propsFileName
     * @return RemoteCacheServer
     */
    public static <K, V> RemoteCacheServer<K, V> startServerUsingProperties( String propsFileName )
    {
        // TODO load from props file or get as init param or get from jndi, or
        // all three
        int registryPort = DEFAULT_REGISTRY_PORT;

        Properties props = null;
        try
        {
            props = RemoteUtils.loadProps(propsFileName);
        }
        catch (IOException e)
        {
            log.error( "Problem loading configuration from " + propsFileName, e);
        }
        
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

        // we will always use the local machine for the registry
        try
        {
            String registryHost = HostNameUtil.getLocalHostAddress();

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
                RemoteCacheServerFactory.startup( registryHost, registryPort, props );
            }
            catch ( IOException e )
            {
                log.error( "Problem starting remote cache server.", e );
            }
        }
        catch ( UnknownHostException e )
        {
            log.error( "Could not get local address to use for the registry!", e );
        }

        return RemoteCacheServerFactory.getRemoteCacheServer();
    }
}
