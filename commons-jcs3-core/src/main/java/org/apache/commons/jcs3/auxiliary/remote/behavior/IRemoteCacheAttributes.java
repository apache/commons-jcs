package org.apache.commons.jcs3.auxiliary.remote.behavior;

import java.util.List;

import org.apache.commons.jcs3.auxiliary.remote.RemoteLocation;

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

/**
 * This specifies what a remote cache configuration object should look like.
 */
public interface IRemoteCacheAttributes
    extends ICommonRemoteCacheAttributes
{
    /**
     * If RECEIVE is false then the remote cache will not register a listener with the remote
     * server. This allows you to configure a remote server as a repository from which you can get
     * and to which you put, but from which you do not receive any notifications. That is, you will
     * not receive updates or removes.
     * <p>
     * If you set this option to false, you should set your local memory size to 0.
     */
    boolean DEFAULT_RECEIVE = true;

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     */
    int DEFAULT_ZOMBIE_QUEUE_MAX_SIZE = 1000;

    /**
     * Gets the failoverIndex attribute of the IRemoteCacheAttributes object.
     * <p>
     * This specifies which server in the list we are listening to if the number is greater than 0
     * we will try to move to 0 position the primary is added as position 1 if it is present
     *
     * @return The failoverIndex value
     */
    int getFailoverIndex();

    /**
     * Gets the failovers attribute of the IRemoteCacheAttributes object
     *
     * @return The failovers value
     */
    List<RemoteLocation> getFailovers();

    /**
     * Gets the failoverServers attribute of the IRemoteCacheAttributes object
     *
     * @return The failoverServers value
     */
    String getFailoverServers();

    /**
     * -1 and 0 mean no timeout, this is the default if the timeout is -1 or 0, no threadpool will
     * be used.
     *
     * @return the time in millis
     */
    int getGetTimeoutMillis();

    /**
     * Gets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @return The localPort value
     */
    int getLocalPort();

    /**
     * The thread pool the remote cache should use. At first this will only be for gets.
     * <p>
     * The default name is "remote_cache_client"
     *
     * @return the name of the pool
     */
    String getThreadPoolName();

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @return the zombieQueueMaxSize.
     */
    int getZombieQueueMaxSize();

    /**
     * If RECEIVE is false then the remote cache will not register a listener with the remote
     * server. This allows you to configure a remote server as a repository from which you can get
     * and to which you put, but from which you do not receive any notifications. That is, you will
     * not receive updates or removes.
     * <p>
     * If you set this option to false, you should set your local memory size to 0.
     * <p>
     * The remote cache manager uses this value to decide whether or not to register a listener.
     * <p>
     * It makes no sense to configure a cluster remote cache to no receive.
     * <p>
     * Since a non-receiving remote cache client will not register a listener, it will not have a
     * listener id assigned from the server. As such the remote server cannot determine if it is a
     * cluster or a normal client. It will assume that it is a normal client.
     *
     * @return the receive value.
     */
    boolean isReceive();

    /**
     * Sets the failoverIndex attribute of the IRemoteCacheAttributes object
     *
     * @param p The new failoverIndex value
     */
    void setFailoverIndex( int p );

    /**
     * Sets the failovers attribute of the IRemoteCacheAttributes object
     *
     * @param failovers The new failovers value
     */
    void setFailovers( List<RemoteLocation> failovers );

    /**
     * Sets the failoverServers attribute of the IRemoteCacheAttributes object
     *
     * @param s The new failoverServers value
     */
    void setFailoverServers( String s );

    /**
     * -1 means no timeout, this is the default if the timeout is -1 or 0, no threadpool will be
     * used. If the timeout is greater than 0 a threadpool will be used for get requests.
     *
     * @param millis
     */
    void setGetTimeoutMillis( int millis );

    /**
     * Sets the localPort attribute of the IRemoteCacheAttributes object
     *
     * @param p The new localPort value
     */
    void setLocalPort( int p );

    /**
     * By default this option is true. If you set it to false, you will not receive updates or
     * removes from the remote server.
     *
     * @param receive
     */
    void setReceive( boolean receive );

    /**
     * Sets the name of the pool to use. Pools should be defined in the cache.ccf.
     *
     * @param name
     */
    void setThreadPoolName( String name );

    /**
     * The number of elements the zombie queue will hold. This queue is used to store events if we
     * loose our connection with the server.
     *
     * @param zombieQueueMaxSize The zombieQueueMaxSize to set.
     */
    void setZombieQueueMaxSize( int zombieQueueMaxSize );
}
