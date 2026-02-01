package org.apache.commons.jcs4.auxiliary.remote;

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

import java.util.List;

import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheAttributes;

/**
 * These objects are used to configure the remote cache client.
 */
public class RemoteCacheAttributes
    extends CommonRemoteCacheAttributes
    implements IRemoteCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = -1555143736942374000L;

    /**
     * Failover servers will be used by local caches one at a time. Listeners will be registered
     * with all cluster servers. If we add a get from cluster attribute we will have the ability to
     * chain clusters and have them get from each other.
     */
    private String failoverServers = "";

    /** Callback */
    private int localPort;

    /** What failover server we are connected to. */
    private int failoverIndex;

    /** List of failover server addresses */
    private List<RemoteLocation> failovers;

    /** Default name is remote_cache_client */
    private String threadPoolName = "remote_cache_client";

    /** Must be greater than 0 for a pool to be used. */
    private int getTimeoutMillis = -1;

    /**
     * Can we receive from the server. You might have a 0 local store and keep everything on the
     * remote. If so, you don't want to be notified of updates.
     */
    private boolean receive = DEFAULT_RECEIVE;

    /** If the primary fails, we will queue items before reconnect.  This limits the number of items that can be queued. */
    private int zombieQueueMaxSize = DEFAULT_ZOMBIE_QUEUE_MAX_SIZE;

    /** Default constructor for the RemoteCacheAttributes object */
    public RemoteCacheAttributes()
    {
    }

    /**
     * Gets the failoverIndex attribute of the RemoteCacheAttributes object.
     *
     * @return The failoverIndex value
     */
    @Override
    public int getFailoverIndex()
    {
        return failoverIndex;
    }

    /**
     * Gets the failovers attribute of the RemoteCacheAttributes object.
     *
     * @return The failovers value
     */
    @Override
    public List<RemoteLocation> getFailovers()
    {
        return this.failovers;
    }

    /**
     * Gets the failoverServers attribute of the RemoteCacheAttributes object.
     *
     * @return The failoverServers value
     */
    @Override
    public String getFailoverServers()
    {
        return this.failoverServers;
    }

    /**
     * @return getTimeoutMillis
     */
    @Override
    public int getGetTimeoutMillis()
    {
        return getTimeoutMillis;
    }

    /**
     * Gets the localPort attribute of the RemoteCacheAttributes object.
     *
     * @return The localPort value
     */
    @Override
    public int getLocalPort()
    {
        return this.localPort;
    }

    /**
     * @return the name of the pool
     */
    @Override
    public String getThreadPoolName()
    {
        return threadPoolName;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @return the zombieQueueMaxSize.
     */
    @Override
    public int getZombieQueueMaxSize()
    {
        return zombieQueueMaxSize;
    }

    /**
     * If RECEIVE is false then the remote cache will not register a listener with the remote
     * server. This allows you to configure a remote server as a repository from which you can get
     * and to which you put, but from which you do not receive any notifications. That is, you will
     * not receive updates or removes.
     * <p>
     * If you set this option to false, you should set your local memory size to 0.
     * <p>
     * The remote cache manager uses this value to decide whether or not to register a listener.
     * @return the receive value.
     */
    @Override
    public boolean isReceive()
    {
        return this.receive;
    }

    /**
     * Sets the failoverIndex attribute of the RemoteCacheAttributes object.
     *
     * @param p The new failoverIndex value
     */
    public void setFailoverIndex( final int p )
    {
        this.failoverIndex = p;
    }

    /**
     * Sets the failovers attribute of the RemoteCacheAttributes object.
     *
     * @param failovers The new failovers value
     */
    public void setFailovers( final List<RemoteLocation> failovers )
    {
        this.failovers = failovers;
    }

    /**
     * Sets the failoverServers attribute of the RemoteCacheAttributes object.
     *
     * @param s The new failoverServers value
     */
    public void setFailoverServers( final String s )
    {
        this.failoverServers = s;
    }

    /**
     * @param millis
     */
    public void setGetTimeoutMillis( final int millis )
    {
        getTimeoutMillis = millis;
    }

    /**
     * Sets the localPort attribute of the RemoteCacheAttributes object
     * @param p The new localPort value
     */
    public void setLocalPort( final int p )
    {
        this.localPort = p;
    }

    /**
     * By default this option is true. If you set it to false, you will not receive updates or
     * removes from the remote server.
     *
     * @param receive
     */
    public void setReceive( final boolean receive )
    {
        this.receive = receive;
    }

    /**
     * @param name
     */
    public void setThreadPoolName( final String name )
    {
        threadPoolName = name;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    public void setZombieQueueMaxSize( final int zombieQueueMaxSize )
    {
        this.zombieQueueMaxSize = zombieQueueMaxSize;
    }

    /**
     * @return String, all the important values that can be configured
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder(super.toString());
        buf.append( "\n receive = [" + isReceive() + "]" );
        buf.append( "\n getTimeoutMillis = [" + getGetTimeoutMillis() + "]" );
        buf.append( "\n threadPoolName = [" + getThreadPoolName() + "]" );
        buf.append( "\n localClusterConsistency = [" + isLocalClusterConsistency() + "]" );
        buf.append( "\n zombieQueueMaxSize = [" + getZombieQueueMaxSize() + "]" );
        return buf.toString();
    }
}
