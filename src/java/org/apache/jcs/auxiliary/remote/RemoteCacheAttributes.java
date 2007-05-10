package org.apache.jcs.auxiliary.remote;

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

import org.apache.jcs.auxiliary.AbstractAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheConstants;

/**
 * These objects are used to configure the remote cache client.
 */
public class RemoteCacheAttributes
    extends AbstractAuxiliaryCacheAttributes
    implements IRemoteCacheAttributes
{
    private static final long serialVersionUID = -1555143736942374000L;

    private String remoteServiceName = IRemoteCacheConstants.REMOTE_CACHE_SERVICE_VAL;

    private String remoteHost;

    private int remotePort;

    /**
     * Failover servers will be used by local caches one at a time. Listeners will be registered
     * with all cluster servers. If we add a get from cluster attribute we will have the ability to
     * chain clusters and have them get from each other.
     */
    private String failoverServers = "";

    private String clusterServers = "";

    private int localPort = 0;

    private int remoteType = LOCAL;

    // what failover server we are connected to.
    private int failoverIndex = 0;

    private String[] failovers;

    private boolean removeUponRemotePut = true;

    private boolean getOnly = false;

    private boolean localClusterConsistency = false;

    // default name is remote_cache_client
    private String threadPoolName = "remote_cache_client";

    // must be greater than 0 for a pool to be used.
    private int getTimeoutMillis = -1;

    private int rmiSocketFactoryTimeoutMillis = DEFAULT_RMI_SOCKET_FACTORY_TIMEOUT_MILLIS;

    private boolean receive = DEFAULT_RECEIVE;

    private int zombieQueueMaxSize = DEFAULT_ZOMBIE_QUEUE_MAX_SIZE;

    /** Default constructor for the RemoteCacheAttributes object */
    public RemoteCacheAttributes()
    {
        super();
    }

    /**
     * Gets the remoteTypeName attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The remoteTypeName value
     */
    public String getRemoteTypeName()
    {
        if ( remoteType == LOCAL )
        {
            return "LOCAL";
        }
        else if ( remoteType == CLUSTER )
        {
            return "CLUSTER";
        }
        return "LOCAL";
    }

    /**
     * Sets the remoteTypeName attribute of the RemoteCacheAttributes object.
     * <p>
     * @param s The new remoteTypeName value
     */
    public void setRemoteTypeName( String s )
    {
        if ( s.equals( "LOCAL" ) )
        {
            remoteType = LOCAL;
        }
        else if ( s.equals( "CLUSTER" ) )
        {
            remoteType = CLUSTER;
        }
    }

    /**
     * Gets the failoverIndex attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The failoverIndex value
     */
    public int getFailoverIndex()
    {
        return failoverIndex;
    }

    /**
     * Sets the failoverIndex attribute of the RemoteCacheAttributes object.
     * <p>
     * @param p The new failoverIndex value
     */
    public void setFailoverIndex( int p )
    {
        this.failoverIndex = p;
    }

    /**
     * Gets the failovers attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The failovers value
     */
    public String[] getFailovers()
    {
        return this.failovers;
    }

    /**
     * Sets the failovers attribute of the RemoteCacheAttributes object.
     * <p>
     * @param f The new failovers value
     */
    public void setFailovers( String[] f )
    {
        this.failovers = f;
    }

    /**
     * Gets the remoteType attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The remoteType value
     */
    public int getRemoteType()
    {
        return remoteType;
    }

    /**
     * Sets the remoteType attribute of the RemoteCacheAttributes object.
     * <p>
     * @param p The new remoteType value
     */
    public void setRemoteType( int p )
    {
        this.remoteType = p;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#copy()
     */
    public AuxiliaryCacheAttributes copy()
    {
        try
        {
            return (AuxiliaryCacheAttributes) this.clone();
        }
        catch ( Exception e )
        {
            // swallow
        }
        return this;
    }

    /**
     * Gets the remoteServiceName attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The remoteServiceName value
     */
    public String getRemoteServiceName()
    {
        return this.remoteServiceName;
    }

    /**
     * Sets the remoteServiceName attribute of the RemoteCacheAttributes object.
     * <p>
     * @param s The new remoteServiceName value
     */
    public void setRemoteServiceName( String s )
    {
        this.remoteServiceName = s;
    }

    /**
     * Gets the remoteHost attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The remoteHost value
     */
    public String getRemoteHost()
    {
        return this.remoteHost;
    }

    /**
     * Sets the remoteHost attribute of the RemoteCacheAttributes object.
     * <p>
     * @param s The new remoteHost value
     */
    public void setRemoteHost( String s )
    {
        this.remoteHost = s;
    }

    /**
     * Gets the remotePort attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The remotePort value
     */
    public int getRemotePort()
    {
        return this.remotePort;
    }

    /**
     * Sets the remotePort attribute of the RemoteCacheAttributes object.
     * <p>
     * @param p The new remotePort value
     */
    public void setRemotePort( int p )
    {
        this.remotePort = p;
    }

    /**
     * Gets the clusterServers attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The clusterServers value
     */
    public String getClusterServers()
    {
        return this.clusterServers;
    }

    /**
     * Sets the clusterServers attribute of the RemoteCacheAttributes object.
     * <p>
     * @param s The new clusterServers value
     */
    public void setClusterServers( String s )
    {
        this.clusterServers = s;
    }

    /**
     * Gets the failoverServers attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The failoverServers value
     */
    public String getFailoverServers()
    {
        return this.failoverServers;
    }

    /**
     * Sets the failoverServers attribute of the RemoteCacheAttributes object.
     * <p>
     * @param s The new failoverServers value
     */
    public void setFailoverServers( String s )
    {
        this.failoverServers = s;
    }

    /**
     * Gets the localPort attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The localPort value
     */
    public int getLocalPort()
    {
        return this.localPort;
    }

    /**
     * Sets the localPort attribute of the RemoteCacheAttributes object
     * @param p The new localPort value
     */
    public void setLocalPort( int p )
    {
        this.localPort = p;
    }

    /**
     * Gets the removeUponRemotePut attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The removeUponRemotePut value
     */
    public boolean getRemoveUponRemotePut()
    {
        return this.removeUponRemotePut;
    }

    /**
     * Sets the removeUponRemotePut attribute of the RemoteCacheAttributes object.
     * <p>
     * @param r The new removeUponRemotePut value
     */
    public void setRemoveUponRemotePut( boolean r )
    {
        this.removeUponRemotePut = r;
    }

    /**
     * Gets the getOnly attribute of the RemoteCacheAttributes object.
     * <p>
     * @return The getOnly value
     */
    public boolean getGetOnly()
    {
        return this.getOnly;
    }

    /**
     * Sets the getOnly attribute of the RemoteCacheAttributes object
     * @param r The new getOnly value
     */
    public void setGetOnly( boolean r )
    {
        this.getOnly = r;
    }

    /**
     * Should cluster updates be propogated to the locals.
     * <p>
     * @return The localClusterConsistency value
     */
    public boolean getLocalClusterConsistency()
    {
        return localClusterConsistency;
    }

    /**
     * Should cluster updates be propogated to the locals.
     * <p>
     * @param r The new localClusterConsistency value
     */
    public void setLocalClusterConsistency( boolean r )
    {
        this.localClusterConsistency = r;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes#getThreadPoolName()
     */
    public String getThreadPoolName()
    {
        return threadPoolName;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes#setThreadPoolName(java.lang.String)
     */
    public void setThreadPoolName( String name )
    {
        threadPoolName = name;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes#getGetTimeoutMillis()
     */
    public int getGetTimeoutMillis()
    {
        return getTimeoutMillis;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes#setGetTimeoutMillis(int)
     */
    public void setGetTimeoutMillis( int millis )
    {
        getTimeoutMillis = millis;
    }

    /**
     * @param rmiSocketFactoryTimeoutMillis The rmiSocketFactoryTimeoutMillis to set.
     */
    public void setRmiSocketFactoryTimeoutMillis( int rmiSocketFactoryTimeoutMillis )
    {
        this.rmiSocketFactoryTimeoutMillis = rmiSocketFactoryTimeoutMillis;
    }

    /**
     * @return Returns the rmiSocketFactoryTimeoutMillis.
     */
    public int getRmiSocketFactoryTimeoutMillis()
    {
        return rmiSocketFactoryTimeoutMillis;
    }

    /**
     * By default this option is true. If you set it to false, you will not receive updates or
     * removes from the remote server.
     * <p>
     * @param receive
     */
    public void setReceive( boolean receive )
    {
        this.receive = receive;
    }

    /**
     * If RECEIVE is false then the remote cache will not register a listener with the remote
     * server. This allows you to configure a remote server as a repository from which you can get
     * and to which you put, but from which you do not reveive any notifications. That is, you will
     * not receive updates or removes.
     * <p>
     * If you set this option to false, you should set your locl memory size to 0.
     * <p>
     * The remote cache manager uses this value to decide whether or not to register a listener.
     * @return the receive value.
     */
    public boolean isReceive()
    {
        return this.receive;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our conenction with the server.
     * <p>
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    public void setZombieQueueMaxSize( int zombieQueueMaxSize )
    {
        this.zombieQueueMaxSize = zombieQueueMaxSize;
    }

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our conenction with the server.
     * <p>
     * @return Returns the zombieQueueMaxSize.
     */
    public int getZombieQueueMaxSize()
    {
        return zombieQueueMaxSize;
    }

    /**
     * @return String, all the important values that can be configured
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n RemoteCacheAttributes " );
        buf.append( "\n remoteHost = [" + this.remoteHost + "]" );
        buf.append( "\n remotePort = [" + this.remotePort + "]" );
        buf.append( "\n cacheName = [" + this.cacheName + "]" );
        buf.append( "\n removeUponRemotePut = [" + this.removeUponRemotePut + "]" );
        buf.append( "\n getOnly = [" + getOnly + "]" );
        buf.append( "\n receive = [" + isReceive() + "]" );
        buf.append( "\n getTimeoutMillis = [" + getGetTimeoutMillis() + "]" );
        buf.append( "\n threadPoolName = [" + getThreadPoolName() + "]" );
        buf.append( "\n remoteType = [" + remoteType + "]" );
        buf.append( "\n localClusterConsistency = [" + getLocalClusterConsistency() + "]" );
        buf.append( "\n zombieQueueMaxSize = [" + getZombieQueueMaxSize() + "]" );
        return buf.toString();
    }

}
