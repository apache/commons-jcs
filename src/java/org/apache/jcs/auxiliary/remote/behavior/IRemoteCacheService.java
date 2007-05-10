package org.apache.jcs.auxiliary.remote.behavior;

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
import java.io.Serializable;
import java.rmi.Remote;
import java.util.Set;

import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

/**
 * Used to retrieve and update the remote cache.
 */
public interface IRemoteCacheService
    extends Remote, ICacheService
{

    /**
     * Puts a cache item to the cache.
     * <p>
     * @param item
     * @param requesterId
     * @throws ObjectExistsException
     * @throws IOException
     */
    public void update( ICacheElement item, long requesterId )
        throws ObjectExistsException, IOException;

    /**
     * Removes the given key from the specified cache.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException;

    /**
     * Remove all keys from the sepcified cache.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException;

    /**
     * Returns a cache bean from the specified cache; or null if the key does
     * not exist.
     * <p>
     * Adding the requestor id, allows the cache to determine the sournce of the
     * get.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
        throws IOException;

    /**
     * @param cacheName
     * @param groupName
     * @return A Set of keys
     * @throws java.rmi.RemoteException
     */
    public Set getGroupKeys( String cacheName, String groupName )
        throws java.rmi.RemoteException;
}
