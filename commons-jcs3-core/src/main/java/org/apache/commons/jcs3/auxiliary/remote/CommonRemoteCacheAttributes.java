package org.apache.commons.jcs3.auxiliary.remote;

import java.util.Objects;

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

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.ICommonRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheConstants;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;

/**
 * Attributes common to remote cache client and server.
 */
public class CommonRemoteCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements ICommonRemoteCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -1555143736942374000L;

    /** The service name */
    private String remoteServiceName = IRemoteCacheConstants.REMOTE_CACHE_SERVICE_VAL;

    /** Server host and port */
    private RemoteLocation location;

    /** Cluster chain */
    private String clusterServers = "";

    /** THe type of remote cache, local or cluster */
    private RemoteType remoteType = RemoteType.LOCAL;

    /** Should we issue a local remove if we get a put from a remote server */
    private boolean removeUponRemotePut = true;

    /** Can we receive from or put to the remote. this probably shouldn't be used. Use receive. */
    private boolean getOnly;

    /** Should we put and get from the clusters. */
    private boolean localClusterConsistency;

    /** Read and connect timeout */
    private int rmiSocketFactoryTimeoutMillis = DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MILLIS;

    /** Default constructor for the RemoteCacheAttributes object */
    public CommonRemoteCacheAttributes()
    {
    }

    /**
     * Gets the clusterServers attribute of the RemoteCacheAttributes object.
     *
     * @return The clusterServers value
     */
    @Override
    public String getClusterServers()
    {
        return this.clusterServers;
    }

    /**
     * Gets the getOnly attribute of the RemoteCacheAttributes object.
     *
     * @return The getOnly value
     */
    @Override
    public boolean getGetOnly()
    {
        return this.getOnly;
    }

    /**
     * Gets the location attribute of the RemoteCacheAttributes object.
     *
     * @return The remote location value
     */
    @Override
    public RemoteLocation getRemoteLocation()
    {
        return this.location;
    }

    /**
     * Gets the remoteServiceName attribute of the RemoteCacheAttributes object.
     *
     * @return The remoteServiceName value
     */
    @Override
    public String getRemoteServiceName()
    {
        return this.remoteServiceName;
    }

    /**
     * Gets the remoteType attribute of the RemoteCacheAttributes object.
     *
     * @return The remoteType value
     */
    @Override
    public RemoteType getRemoteType()
    {
        return remoteType;
    }

    /**
     * Gets the remoteTypeName attribute of the RemoteCacheAttributes object.
     *
     * @return The remoteTypeName value
     */
    @Override
    public String getRemoteTypeName()
    {
        return Objects.toString(remoteType, RemoteType.LOCAL.toString());
    }

    /**
     * Gets the removeUponRemotePut attribute of the RemoteCacheAttributes object.
     *
     * @return The removeUponRemotePut value
     */
    @Override
    public boolean getRemoveUponRemotePut()
    {
        return this.removeUponRemotePut;
    }

    /**
     * @return the rmiSocketFactoryTimeoutMillis.
     */
    @Override
    public int getRmiSocketFactoryTimeoutMillis()
    {
        return rmiSocketFactoryTimeoutMillis;
    }

    /**
     * Should cluster updates be propagated to the locals.
     *
     * @return The localClusterConsistency value
     */
    @Override
    public boolean isLocalClusterConsistency()
    {
        return localClusterConsistency;
    }

    /**
     * Sets the clusterServers attribute of the RemoteCacheAttributes object.
     *
     * @param s The new clusterServers value
     */
    @Override
    public void setClusterServers( final String s )
    {
        this.clusterServers = s;
    }

    /**
     * Sets the getOnly attribute of the RemoteCacheAttributes object
     * @param r The new getOnly value
     */
    @Override
    public void setGetOnly( final boolean r )
    {
        this.getOnly = r;
    }

    /**
     * Should cluster updates be propagated to the locals.
     *
     * @param r The new localClusterConsistency value
     */
    @Override
    public void setLocalClusterConsistency( final boolean r )
    {
        this.localClusterConsistency = r;
    }

    /**
     * Sets the location attribute of the RemoteCacheAttributes object.
     *
     * @param location The new location value
     */
    @Override
    public void setRemoteLocation( final RemoteLocation location )
    {
        this.location = location;
    }

    /**
     * Sets the location attribute of the RemoteCacheAttributes object.
     *
     * @param host The new remoteHost value
     * @param port The new remotePort value
     */
    @Override
    public void setRemoteLocation( final String host, final int port )
    {
        this.location = new RemoteLocation(host, port);
    }

    /**
     * Sets the remoteServiceName attribute of the RemoteCacheAttributes object.
     *
     * @param s The new remoteServiceName value
     */
    @Override
    public void setRemoteServiceName( final String s )
    {
        this.remoteServiceName = s;
    }

    /**
     * Sets the remoteType attribute of the RemoteCacheAttributes object.
     *
     * @param p The new remoteType value
     */
    @Override
    public void setRemoteType( final RemoteType p )
    {
        this.remoteType = p;
    }

    /**
     * Sets the remoteTypeName attribute of the RemoteCacheAttributes object.
     *
     * @param s The new remoteTypeName value
     */
    @Override
    public void setRemoteTypeName( final String s )
    {
        this.remoteType = RemoteType.valueOf(s);
    }

    /**
     * Sets the removeUponRemotePut attribute of the RemoteCacheAttributes object.
     *
     * @param r The new removeUponRemotePut value
     */
    @Override
    public void setRemoveUponRemotePut( final boolean r )
    {
        this.removeUponRemotePut = r;
    }

    /**
     * @param rmiSocketFactoryTimeoutMillis The rmiSocketFactoryTimeoutMillis to set.
     */
    @Override
    public void setRmiSocketFactoryTimeoutMillis( final int rmiSocketFactoryTimeoutMillis )
    {
        this.rmiSocketFactoryTimeoutMillis = rmiSocketFactoryTimeoutMillis;
    }

    /**
     * @return String, all the important values that can be configured
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n RemoteCacheAttributes " );
        if (this.location != null)
        {
            buf.append( "\n remoteHost = [" + this.location.getHost() + "]" );
            buf.append( "\n remotePort = [" + this.location.getPort() + "]" );
        }
        buf.append( "\n cacheName = [" + getCacheName() + "]" );
        buf.append( "\n remoteType = [" + remoteType + "]" );
        buf.append( "\n removeUponRemotePut = [" + this.removeUponRemotePut + "]" );
        buf.append( "\n getOnly = [" + getOnly + "]" );
        return buf.toString();
    }
}
