package org.apache.jcs.auxiliary.remote.server.behavior;

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

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * This defeines the minimal behavior for the objects that are used to configure
 * the remote cache server.
 */
public interface IRemoteCacheServerAttributes
    extends AuxiliaryCacheAttributes
{
    /** A remote cache is either a local cache or a cluster cache */
    public static int LOCAL = 0;

    /** A remote cache is either a local cache or a cluster cache */
    public static int CLUSTER = 1;

    /**
     * Gets the remoteTypeName attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The remoteTypeName value
     */
    public String getRemoteTypeName();

    /**
     * Sets the remoteTypeName attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param s
     *            The new remoteTypeName value
     */
    public void setRemoteTypeName( String s );

    /**
     * Gets the remoteType attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The remoteType value
     */
    public int getRemoteType();

    /**
     * Sets the remoteType attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param p
     *            The new remoteType value
     */
    public void setRemoteType( int p );

    /**
     * Gets the remoteHost attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The remoteHost value
     */
    public String getRemoteHost();

    /**
     * Sets the remoteHost attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param s
     *            The new remoteHost value
     */
    public void setRemoteHost( String s );

    /**
     * Gets the remotePort attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The remotePort value
     */
    public int getRemotePort();

    /**
     * Sets the remotePort attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param p
     *            The new remotePort value
     */
    public void setRemotePort( int p );

    /**
     * Gets the localPort attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The localPort value
     */
    public int getServicePort();

    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param p
     *            The new localPort value
     */
    public void setServicePort( int p );

    /**
     * Gets the clusterServers attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The clusterServers value
     */
    public String getClusterServers();

    /**
     * Sets the clusterServers attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param s
     *            The new clusterServers value
     */
    public void setClusterServers( String s );

    /**
     * Gets the removeUponRemotePut attribute of the IRemoteCacheAttributes.
     * object.
     * <p>
     * @return The removeUponRemotePut value
     */
    public boolean getRemoveUponRemotePut();

    /**
     * Sets the removeUponRemotePut attribute of the IRemoteCacheAttributes.
     * object
     * <p>
     * @param r
     *            The new removeUponRemotePut value
     */
    public void setRemoveUponRemotePut( boolean r );

    /**
     * Gets the getOnly attribute of the IRemoteCacheAttributes object.
     * A remote cache is either a local cache or a cluster cache
     * @return The getOnly value
     */
    public boolean getGetOnly();

    /**
     * Sets the getOnly attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param r
     *            The new getOnly value
     */
    public void setGetOnly( boolean r );

    /**
     * Should cluster updates be propogated to the locals.
     * <p>
     * @return The localClusterConsistency value
     */
    public boolean getLocalClusterConsistency();

    /**
     * Should cluster updates be propogated to the locals.
     * <p>
     * @param r
     *            The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r );

    /**
     * Should we try to get remotely when the request does not come in from a
     * cluster. If local L1 asks remote server R1 for element A and R1 doesn't
     * have it, should R1 look remotely? The difference is between a local and a
     * remote update. The local update stays local. Normal updates, removes,
     * etc, stay local when they come from a client. If this is set to true,
     * then they can go remote.
     * <p>
     * @return The localClusterConsistency value
     */
    public boolean getAllowClusterGet();

    /**
     * Should cluster updates be propogated to the locals.
     * <p>
     * @param r
     *            The new localClusterConsistency value
     */
    public void setAllowClusterGet( boolean r );

    /**
     * Gets the ConfigFileName attribute of the IRemoteCacheAttributes object.
     * <p>
     * @return The clusterServers value
     */
    public String getConfigFileName();

    /**
     * Sets the ConfigFileName attribute of the IRemoteCacheAttributes object.
     * <p>
     * @param s
     *            The new clusterServers value
     */
    public void setConfigFileName( String s );
}
