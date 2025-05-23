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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs3.auxiliary.remote.CommonRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;

/**
 * These attributes are used to configure the remote cache server.
 */
public class RemoteCacheServerAttributes
    extends CommonRemoteCacheAttributes
    implements IRemoteCacheServerAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -2741662082869155365L;

    /** Should we try to keep the registry alive */
    private final static boolean DEFAULT_USE_REGISTRY_KEEP_ALIVE = true;

    /** Port the server will listen to */
    private int servicePort;

    /** Can a cluster remote get from other remotes */
    private boolean allowClusterGet = true;

    /** The config file, the initialization is multistage. Remote cache then composite cache. */
    private String configFileName = "";

    /** Should we try to keep the registry alive */
    private boolean useRegistryKeepAlive = DEFAULT_USE_REGISTRY_KEEP_ALIVE;

    /** The delay between runs */
    private long registryKeepAliveDelayMillis = 15 * 1000;

    /** Default constructor for the RemoteCacheAttributes object */
    public RemoteCacheServerAttributes()
    {
    }

    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    @Override
    public String getConfigFileName()
    {
        return configFileName;
    }

    /**
     * @return the registryKeepAliveDelayMillis
     */
    @Override
    public long getRegistryKeepAliveDelayMillis()
    {
        return registryKeepAliveDelayMillis;
    }

    /**
     * Gets the localPort attribute of the RemoteCacheAttributes object
     *
     * @return The localPort value
     */
    @Override
    public int getServicePort()
    {
        return this.servicePort;
    }

    /**
     * Should gets from non-cluster clients be allowed to get from other remote auxiliaries.
     *
     * @return The localClusterConsistency value
     */
    @Override
    public boolean isAllowClusterGet()
    {
        return allowClusterGet;
    }

    /**
     * Should we try to keep the registry alive
     *
     * @return the useRegistryKeepAlive
     */
    @Override
    public boolean isUseRegistryKeepAlive()
    {
        return useRegistryKeepAlive;
    }

    /**
     * Should we try to get from other cluster servers if we don't find the items locally.
     *
     * @param r The new localClusterConsistency value
     */
    @Override
    public void setAllowClusterGet( final boolean r )
    {
        allowClusterGet = r;
    }

    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    @Override
    public void setConfigFileName( final String s )
    {
        configFileName = s;
    }

    /**
     * @param registryKeepAliveDelayMillis the registryKeepAliveDelayMillis to set
     */
    @Override
    public void setRegistryKeepAliveDelayMillis( final long registryKeepAliveDelayMillis )
    {
        this.registryKeepAliveDelayMillis = registryKeepAliveDelayMillis;
    }

    /**
     * Sets the localPort attribute of the RemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    @Override
    public void setServicePort( final int p )
    {
        this.servicePort = p;
    }

    /**
     * Should we try to keep the registry alive
     *
     * @param useRegistryKeepAlive the useRegistryKeepAlive to set
     */
    @Override
    public void setUseRegistryKeepAlive( final boolean useRegistryKeepAlive )
    {
        this.useRegistryKeepAlive = useRegistryKeepAlive;
    }

    /**
     * @return String details
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder(super.toString());
        buf.append( "\n servicePort = [" + getServicePort() + "]" );
        buf.append( "\n allowClusterGet = [" + isAllowClusterGet() + "]" );
        buf.append( "\n configFileName = [" + getConfigFileName() + "]" );
        buf.append( "\n rmiSocketFactoryTimeoutMillis = [" + getRmiSocketFactoryTimeoutMillis() + "]" );
        buf.append( "\n useRegistryKeepAlive = [" + isUseRegistryKeepAlive() + "]" );
        buf.append( "\n registryKeepAliveDelayMillis = [" + getRegistryKeepAliveDelayMillis() + "]" );
        buf.append( "\n eventQueueType = [" + getEventQueueType() + "]" );
        buf.append( "\n eventQueuePoolName = [" + getEventQueuePoolName() + "]" );
        return buf.toString();
    }
}
