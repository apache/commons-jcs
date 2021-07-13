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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.auxiliary.remote.RemoteUtils;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.net.HostNameUtil;

/**
 * This servlet can be used to startup the JCS remote cache. It is easy to
 * deploy the remote server in a tomcat base. This give you an easy way to
 * monitor its activity.
 * <p>
 * <code>
 *  servlet&gt;
        &lt;servlet-name&gt;JCSRemoteCacheStartupServlet&lt;/servlet-name&gt;
        &lt;servlet-class&gt;
             org.apache.commons.jcs3.auxiliary.remote.server.RemoteCacheStartupServlet
        &lt;/servlet-class&gt;
        &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
    &lt;/servlet&gt;


    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;JCSRemoteCacheStartupServlet&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/jcs&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
 * </code>
 *
 * @author Aaron Smuts
 */
public class RemoteCacheStartupServlet
        extends HttpServlet
{
    /** Don't change */
    private static final long serialVersionUID = 1L;

    /** The logger */
    private static final Log log = LogManager.getLog(RemoteCacheStartupServlet.class);

    /** The default port to start the registry on. */
    private static final int DEFAULT_REGISTRY_PORT = 1101;

    /** properties file name */
    private static final String DEFAULT_PROPS_FILE_NAME = "/cache.ccf";

    /** properties file name, must set prior to calling get instance */
    private String propsFileName = DEFAULT_PROPS_FILE_NAME;

    /** Configuration properties */
    private int registryPort = DEFAULT_REGISTRY_PORT;

    /** Configuration properties */
    private String registryHost;

    /**
     * Starts the registry and then tries to bind to it.
     * <p>
     * Gets the port from a props file. Uses the local host name for the
     * registry host. Tries to start the registry, ignoring failure. Starts the
     * server.
     * <p>
     *
     * @throws ServletException
     */
    @Override
    public void init()
            throws ServletException
    {
        super.init();

        loadInitParams();
        final Properties props = loadPropertiesFromFile();

        if (registryHost == null)
        {
            // we will always use the local machine for the registry
            try
            {
                registryHost = HostNameUtil.getLocalHostAddress();
            }
            catch (final UnknownHostException e)
            {
                log.error("Could not get local address to use for the registry!", e);
            }
        }

        log.debug("registryHost = [{0}]", registryHost);

        try
        {
            if (InetAddress.getByName(registryHost).isLoopbackAddress())
            {
                log.warn("The local address [{0}] is a loopback address. Other machines must "
                        + "be able to use the address to reach this server.", registryHost);
            }
        }
        catch (final UnknownHostException e)
        {
            throw new ServletException("Could not resolve registry host " + registryHost, e);
        }

        try
        {
            if (props == null)
            {
                throw new ServletException("Could not load configuration from " + propsFileName);
            }

            RemoteCacheServerFactory.startup(registryHost, registryPort, props);
            log.info("Remote JCS Server started with properties from {0}", propsFileName);
        }
        catch (final IOException e)
        {
            throw new ServletException("Problem starting remote cache server.", e);
        }
    }

    /**
     * It just dumps the stats.
     * <p>
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        String stats = "";

        try
        {
            stats = CompositeCacheManager.getInstance().getStats();
        }
        catch (final CacheException e)
        {
            throw new ServletException(e);
        }

        log.info(stats);

        try
        {
            String characterEncoding = response.getCharacterEncoding();
            if (characterEncoding == null)
            {
                characterEncoding = StandardCharsets.UTF_8.name();
                response.setCharacterEncoding(characterEncoding);
            }
            final OutputStream os = response.getOutputStream();
            os.write(stats.getBytes(characterEncoding));
            os.close();
        }
        catch (final IOException e)
        {
            log.error("Problem writing response.", e);
        }
    }

    /**
     * shuts the cache down.
     */
    @Override
    public void destroy()
    {
        super.destroy();

        log.info("Shutting down remote cache ");

        try
        {
            RemoteCacheServerFactory.shutdownImpl(registryHost, registryPort);
        }
        catch (final IOException e)
        {
            log.error("Problem shutting down.", e);
        }

        try
        {
            CompositeCacheManager.getInstance().shutDown();
        }
        catch (final CacheException e)
        {
            log.error("Could not retrieve cache manager instance", e);
        }
    }

    /**
     * Load configuration values from config file if possible
     */
    private Properties loadPropertiesFromFile()
    {
        Properties props = null;

        try
        {
            props = RemoteUtils.loadProps(propsFileName);
            registryHost = props.getProperty("registry.host", registryHost);
            final String portS = props.getProperty("registry.port", String.valueOf(registryPort));
            setRegistryPort(portS);
        }
        catch (final IOException e)
        {
            log.error("Problem loading props.", e);
        }

        return props;
    }

    /**
     * Load configuration values from init params if possible
     */
    private void loadInitParams()
    {
        final ServletConfig config = getServletConfig();
        final String _propsFileName = config.getInitParameter("propsFileName");
        if (null != _propsFileName)
        {
            this.propsFileName = _propsFileName;
        }
        final String _registryHost = config.getInitParameter("registryHost");
        if (null != _registryHost)
        {
            this.registryHost = _registryHost;
        }
        final String regPortString = config.getInitParameter("registryPort");
        if (null != regPortString)
        {
            setRegistryPort(regPortString);
        }
    }

    /**
     * Set registry port from string If the string cannot be parsed, the default
     * value is used
     *
     * @param portS
     */
    private void setRegistryPort(final String portS)
    {
        try
        {
            this.registryPort = Integer.parseInt(portS);
        }
        catch (final NumberFormatException e)
        {
            log.error("Problem converting port to an int.", e);
            this.registryPort = DEFAULT_REGISTRY_PORT;
        }
    }
}
