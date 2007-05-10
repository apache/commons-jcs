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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.utils.props.PropertyLoader;

/**
 * This servlet can be used to startup the JCS remote cache. It is easy to
 * deploy the remote server in a tomcat base. This give you an easy way to
 * monitor its activity.
 * <p>
 *
 * <code>
 *  <servlet>
        <servlet-name>JCSRemoteCacheStartupServlet</servlet-name>
        <servlet-class>
             org.apache.jcs.auxiliary.remote.server.RemoteCacheStartupServlet
        </servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>


    <servlet-mapping>
        <servlet-name>JCSRemoteCacheStartupServlet</servlet-name>
        <url-pattern>/jcs</url-pattern>
    </servlet-mapping>
 * </code>
 *
 *
 * @author Aaron Smuts
 *
 */
public class RemoteCacheStartupServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private final static Log log = LogFactory.getLog( RemoteCacheStartupServlet.class );

    private static final int DEFAULT_REGISTRY_PORT = 1101;

    private static final String DEFAULT_PROPS_FILE_NAME = "cache.ccf";

    /**
     * Starts the registry and then tries to bind to it.
     * <p>
     * Gets the port from a props file. Uses the local host name for the rgistry
     * host. Tries to start the registry, ignoreing failure. Starts the server.
     */
    public void init()
        throws ServletException
    {
        super.init();
        // TODO load from props file or get as init param or get from jndi, or
        // all three
        int registryPort = DEFAULT_REGISTRY_PORT;

        try
        {
            Properties props = PropertyLoader.loadProperties( DEFAULT_PROPS_FILE_NAME );
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
                RemoteCacheServerFactory.startup( registryHost, registryPort, "/" + DEFAULT_PROPS_FILE_NAME );
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
    }

    /**
     * It just dumps the stats.
     */
    protected void service( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {

        String stats = CompositeCacheManager.getInstance().getStats();
        if ( log.isInfoEnabled() )
        {
            log.info( stats );
        }

        try
        {
            OutputStream os = response.getOutputStream();
            os.write( stats.getBytes() );
            os.close();
        }
        catch ( IOException e )
        {
            log.error( "Problem writing response.", e );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {
        super.destroy();

        log.info( "Shutting down remote cache " );

        CompositeCacheManager.getInstance().shutDown();
    }
}
