package org.apache.commons.jcs4.auxiliary.remote.behavior;

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

import java.io.IOException;
import java.rmi.Remote;

import org.apache.commons.jcs4.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheListener;

/**
 * Listens for remote cache event notification ( rmi callback ).
 * The methods of the base interface are re-declared here because of a fix
 * in Java 8u241 and above. See https://bugs.openjdk.org/browse/JDK-8237213
 * for an explanation.
 */
public interface IRemoteCacheListener<K, V>
    extends ICacheListener<K, V>, Remote
{
    /**
     * De-Registers itself.
     *
     * @throws IOException
     */
    void dispose()
        throws IOException;

    /**
     * Gets the listenerId attribute of the ICacheListener object
     *
     * @return The listenerId value
     * @throws IOException
     */
    @Override
    long getListenerId()
        throws IOException;

    /**
     * This is for debugging. It allows the remote cache server to log the address of any listeners
     * that register.
     *
     * @return the local host address.
     * @throws IOException
     */
    String getLocalHostAddress()
        throws IOException;

    /**
     * Gets the remoteType attribute of the IRemoteCacheListener object
     *
     * @return The remoteType value
     * @throws IOException
     */
    RemoteType getRemoteType()
        throws IOException;

    /**
     * Notifies the subscribers for freeing up the named cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    void handleDispose( String cacheName )
        throws IOException;

    /**
     * Notifies the subscribers for a cache entry update.
     *
     * @param item
     * @throws IOException
     */
    @Override
    void handlePut( ICacheElement<K, V> item )
        throws IOException;

    /**
     * Notifies the subscribers for a cache entry removal.
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    void handleRemove( String cacheName, K key )
        throws IOException;

    /**
     * Notifies the subscribers for a cache remove-all.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    void handleRemoveAll( String cacheName )
        throws IOException;

    /**
     * sets unique identifier of listener home
     *
     * @param id The new listenerId value
     * @throws IOException
     */
    @Override
    void setListenerId( long id )
        throws IOException;
}
