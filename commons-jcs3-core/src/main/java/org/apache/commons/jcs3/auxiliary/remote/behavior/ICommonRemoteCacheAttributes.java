package org.apache.commons.jcs3.auxiliary.remote.behavior;

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

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.RemoteLocation;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;

/**
 * This specifies what a remote cache configuration object should look like.
 */
public interface ICommonRemoteCacheAttributes
    extends AuxiliaryCacheAttributes
{
    /** The default timeout for the custom RMI socket factory */
    int DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MILLIS = 10000;

    /**
     * Gets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @return The clusterServers value
     */
    String getClusterServers();

    /**
     * Gets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @return The getOnly value
     */
    boolean getGetOnly();

    /**
     * Gets the location attribute of the RemoteCacheAttributes object.
     *
     * @return The remote location value
     */
    RemoteLocation getRemoteLocation();

    /**
     * Gets the remoteServiceName attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteServiceName value
     */
    String getRemoteServiceName();

    /**
     * Gets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteType value
     */
    RemoteType getRemoteType();

    /**
     * Gets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @return The remoteTypeName value
     */
    String getRemoteTypeName();

    /**
     * Gets the removeUponRemotePut attribute of the IRemoteCacheAttributes object
     *
     * @return The removeUponRemotePut value
     */
    boolean getRemoveUponRemotePut();

    /**
     * This sets a general timeout on the rmi socket factory. By default the socket factory will
     * block forever.
     * <p>
     * We have a default setting. The default rmi behavior should never be used.
     *
     * @return int milliseconds
     */
    int getRmiSocketFactoryTimeoutMillis();

    /**
     * Should cluster updates be propagated to the locals
     *
     * @return The localClusterConsistency value
     */
    boolean isLocalClusterConsistency();

    /**
     * Sets the clusterServers attribute of the IRemoteCacheAttributes object
     *
     * @param s The new clusterServers value
     */
    void setClusterServers( String s );

    /**
     * Sets the getOnly attribute of the IRemoteCacheAttributes object
     *
     * @param r The new getOnly value
     */
    void setGetOnly( boolean r );

    /**
     * Should cluster updates be propagated to the locals
     *
     * @param r The new localClusterConsistency value
     */
    void setLocalClusterConsistency( boolean r );

    /**
     * Sets the location attribute of the RemoteCacheAttributes object.
     *
     * @param location The new location value
     */
    void setRemoteLocation( RemoteLocation location );

    /**
     * Sets the location attribute of the RemoteCacheAttributes object.
     *
     * @param host The new remoteHost value
     * @param port The new remotePort value
     */
    void setRemoteLocation( String host, int port );

    /**
     * Sets the remoteServiceName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteServiceName value
     */
    void setRemoteServiceName( String s );

    /**
     * Sets the remoteType attribute of the IRemoteCacheAttributes object
     *
     * @param p The new remoteType value
     */
    void setRemoteType( RemoteType p );

    /**
     * Sets the remoteTypeName attribute of the IRemoteCacheAttributes object
     *
     * @param s The new remoteTypeName value
     */
    void setRemoteTypeName( String s );

    /**
     * Sets the removeUponRemotePut attribute of the IRemoteCacheAttributes object
     *
     * @param r The new removeUponRemotePut value
     */
    void setRemoveUponRemotePut( boolean r );

    /**
     * This sets a general timeout on the RMI socket factory. By default the socket factory will
     * block forever.
     *
     * @param rmiSocketFactoryTimeoutMillis
     */
    void setRmiSocketFactoryTimeoutMillis( int rmiSocketFactoryTimeoutMillis );
}
