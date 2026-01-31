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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheServiceNonLocal;

/**
 * This is a mock impl of the remote cache service.
 */
public class MockRemoteCacheService<K, V>
    implements ICacheServiceNonLocal<K, V>
{
    /** The key last passed to get */
    public K lastGetKey;

    /** The pattern last passed to get */
    public String lastGetMatchingPattern;

    /** The keya last passed to getMatching */
    public Set<K> lastGetMultipleKeys;

    /** The object that was last passed to update. */
    public ICacheElement<K, V> lastUpdate;

    /** List of updates. */
    public List<ICacheElement<K, V>> updateRequestList = new ArrayList<>();

    /** List of request ids. */
    public List<Long> updateRequestIdList = new ArrayList<>();

    /** The key that was last passed to remove. */
    public K lastRemoveKey;

    /** The cache name that was last passed to removeAll. */
    public String lastRemoveAllCacheName;

    /**
     * Do nothing.
     *
     * @param cacheName
     */
    @Override
    public void dispose( final String cacheName )
    {
    }

    /**
     * @param cacheName
     * @param key
     * @return null
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key )
    {
        return get( cacheName, key, 0 );
    }

    /**
     * @param cacheName
     * @param key
     * @param requesterId
     * @return null
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key, final long requesterId )
    {
        lastGetKey = key;
        return null;
    }

    /**
     * @param cacheName
     * @return empty set
     */
    @Override
    public Set<K> getKeySet( final String cacheName )
    {
        return new HashSet<>();
    }

    /**
     * Returns an empty map. Zombies have no internal data.
     *
     * @param cacheName
     * @param pattern
     * @return an empty map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, 0 );
    }

    /**
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        lastGetMatchingPattern = pattern;
        return new HashMap<>();
    }

    /**
     * @param cacheName
     * @param keys
     * @return empty map
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys )
    {
        return getMultiple( cacheName, keys, 0 );
    }

    /**
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return empty map
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
    {
        lastGetMultipleKeys = keys;
        return new HashMap<>();
    }

    /**
     * Do nothing.
     */
    @Override
    public void release()
    {
    }

    /**
     * Sets the last remove key.
     *
     * @param cacheName
     * @param key
     */
    @Override
    public void remove( final String cacheName, final K key )
    {
        lastRemoveKey = key;
    }

    /**
     * Sets the last remove key.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     */
    @Override
    public void remove( final String cacheName, final K key, final long requesterId )
    {
        lastRemoveKey = key;
    }

    /**
     * Sets the last remove all cache name.
     *
     * @param cacheName
     */
    @Override
    public void removeAll( final String cacheName )
    {
        lastRemoveAllCacheName = cacheName;
    }

    /**
     * Sets the lastRemoveAllCacheName to the cacheName.
     */
    @Override
    public void removeAll( final String cacheName, final long requesterId )
        throws IOException
    {
        lastRemoveAllCacheName = cacheName;
    }

    /**
     * Sets the last update item.
     *
     * @param item
     */
    @Override
    public void update( final ICacheElement<K, V> item )
    {
        lastUpdate = item;
    }

    /**
     * Sets the last update item.
     *
     * @param item
     * @param requesterId
     */
    @Override
    public void update( final ICacheElement<K, V> item, final long requesterId )
    {
        lastUpdate = item;
        updateRequestList.add( item );
        updateRequestIdList.add( Long.valueOf( requesterId ) );
    }
}
