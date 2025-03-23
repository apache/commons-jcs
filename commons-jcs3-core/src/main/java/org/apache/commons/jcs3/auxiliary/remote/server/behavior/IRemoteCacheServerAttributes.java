package org.apache.commons.jcs3.auxiliary.remote.server.behavior;

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

import org.apache.commons.jcs3.auxiliary.remote.behavior.ICommonRemoteCacheAttributes;

/**
 * This defines the minimal behavior for the objects that are used to configure
 * the remote cache server.
 */
public interface IRemoteCacheServerAttributes
    extends ICommonRemoteCacheAttributes
{
    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object.
     *
     * @return The configuration file name
     */
    String getConfigFileName();

    /**
     * @return the registryKeepAliveDelayMillis
     */
    long getRegistryKeepAliveDelayMillis();

    /**
     * Gets the localPort attribute of the IRemoteCacheAttributes object.
     *
     * @return The localPort value
     */
    int getServicePort();

    /**
     * Should we try to get remotely when the request does not come in from a
     * cluster. If local L1 asks remote server R1 for element A and R1 doesn't
     * have it, should R1 look remotely? The difference is between a local and a
     * remote update. The local update stays local. Normal updates, removes,
     * etc, stay local when they come from a client. If this is set to true,
     * then they can go remote.
     *
     * @return The localClusterConsistency value
     */
    boolean isAllowClusterGet();

    /**
     * Should we try to keep the registry alive
     *
     * @return the useRegistryKeepAlive
     */
    boolean isUseRegistryKeepAlive();

    /**
     * Should cluster updates be propagated to the locals.
     *
     * @param r
     *            The new localClusterConsistency value
     */
    void setAllowClusterGet( boolean r );

    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object.
     *
     * @param s
     *            The new configuration file name
     */
    void setConfigFileName( String s );

    /**
     * @param registryKeepAliveDelayMillis the registryKeepAliveDelayMillis to set
     */
    void setRegistryKeepAliveDelayMillis( long registryKeepAliveDelayMillis );

    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object.
     *
     * @param p
     *            The new localPort value
     */
    void setServicePort( int p );

    /**
     * Should we try to keep the registry alive
     *
     * @param useRegistryKeepAlive the useRegistryKeepAlive to set
     */
    void setUseRegistryKeepAlive( boolean useRegistryKeepAlive );
}
