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
public interface ICacheServiceNonLocal<K extends Serializable, V extends Serializable>
    extends Remote, ICacheService<K, V>
{
    /**
     * Puts a cache item to the cache.
     * <p>
     * @param item
     * @param requesterId
     * @throws IOException
     */
    void update( ICacheElement<K, V> item, long requesterId )
        throws IOException;

    /**
     * Removes the given key from the specified cache.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    void remove( String cacheName, K key, long requesterId )
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
    ICacheElement<K, V> get( String cacheName, K key, long requesterId )
        throws IOException;

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement<K, V> element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    Map<K, ICacheElement<K, V>> getMultiple( String cacheName, Set<K> keys, long requesterId )
        throws IOException;

    /**
     * Gets multiple items from the cache matching the pattern.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return a map of K key to ICacheElement<K, V> element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    Map<K, ICacheElement<K, V>> getMatching( String cacheName, String pattern, long requesterId )
        throws IOException;

    /**
     * Gets the set of keys of objects currently in the group.
     * <p>
     * @param cacheName the name of the cache
     * @param groupName the name of the group
     * @return a Set of group keys.
     * @throws IOException
     */
    Set<K> getGroupKeys( String cacheName, String groupName )
        throws IOException;

    /**
     * Gets the set of group names in the cache
     * <p>
     * @param cacheName the name of the cache
     * @return a Set of group names.
     * @throws IOException
     */
    Set<String> getGroupNames( String cacheName )
        throws IOException;
}
