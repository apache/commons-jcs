package org.apache.jcs.engine.behavior;

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
import java.util.Map;
import java.util.Set;

/**
 * Used to retrieve and update non local caches, such as the remote and lateral caches. Unlike
 * ICacheService, the methods here have a requester id. This allows us to avoid propagating events
 * to ourself.
 * <p>
 * TODO consider not extending ICacheService
 */
public interface ICacheServiceNonLocal
    extends Remote, ICacheService
{
    /**
     * Puts a cache item to the cache.
     * <p>
     * @param item
     * @param requesterId
     * @throws IOException
     */
    void update( ICacheElement item, long requesterId )
        throws IOException;

    /**
     * Removes the given key from the specified cache.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    void remove( String cacheName, Serializable key, long requesterId )
        throws IOException;

    /**
     * Remove all keys from the specified cache.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    void removeAll( String cacheName, long requesterId )
        throws IOException;

    /**
     * Returns a cache bean from the specified cache; or null if the key does not exist.
     * <p>
     * Adding the requestor id, allows the cache to determine the sournce of the get.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    ICacheElement get( String cacheName, Serializable key, long requesterId )
        throws IOException;

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    Map<Serializable, ICacheElement> getMultiple( String cacheName, Set<Serializable> keys, long requesterId )
        throws IOException;

    /**
     * Gets multiple items from the cache matching the pattern.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    Map<Serializable, ICacheElement> getMatching( String cacheName, String pattern, long requesterId )
        throws IOException;

    /**
     * Likely not implemented.  This probably should be deprecated.
     * <p>
     * @param cacheName
     * @param groupName
     * @return A Set of keys
     * @throws IOException
     */
    Set<Serializable> getGroupKeys( String cacheName, String groupName )
        throws IOException;
}
