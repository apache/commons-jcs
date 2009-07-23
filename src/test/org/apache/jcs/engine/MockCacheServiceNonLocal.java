package org.apache.jcs.engine;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheServiceNonLocal;

/**
 * This is a mock impl of the non local cache service.
 */
public class MockCacheServiceNonLocal
    implements ICacheServiceNonLocal
{
    /** The key last passed to get */
    public Serializable lastGetKey;

    /** The pattern last passed to get */
    public String lastGetMatchingPattern;

    /** The keya last passed to getMatching */
    public Set lastGetMultipleKeys;

    /** The object that was last passed to update. */
    public Object lastUpdate;

    /** List of updates. */
    public List updateRequestList = new ArrayList();

    /** List of request ids. */
    public List updateRequestIdList = new ArrayList();

    /** The key that was last passed to remove. */
    public Object lastRemoveKey;

    /** The cache name that was last passed to removeAll. */
    public String lastRemoveAllCacheName;

    /**
     * @param cacheName
     * @param key
     * @param requesterId - identity of requester
     * @return null
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
    {
        lastGetKey = key;
        return null;
    }

    /**
     * @param cacheName
     * @param groupName
     * @return empty set
     */
    public Set getGroupKeys( String cacheName, String groupName )
    {
        return new HashSet();
    }

    /**
     * Set the last remove key.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId - identity of requester
     */
    public void remove( String cacheName, Serializable key, long requesterId )
    {
        lastRemoveKey = key;
    }

    /**
     * Set the lastRemoveAllCacheName to the cacheName.
     * <p>
     * @param cacheName - region name
     * @param requesterId - identity of requester
     * @throws IOException 
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        lastRemoveAllCacheName = cacheName;
    }

    /**
     * Set the last update item.
     * <p>
     * @param item
     * @param requesterId - identity of requester
     */
    public void update( ICacheElement item, long requesterId )
    {
        lastUpdate = item;
        updateRequestList.add( item );
        updateRequestIdList.add( new Long( requesterId ) );
    }

    /**
     * Do nothing.
     * <p>
     * @param cacheName
     */
    public void dispose( String cacheName )
    {
        return;
    }

    /**
     * @param cacheName
     * @param key
     * @return null
     */
    public ICacheElement get( String cacheName, Serializable key )
    {
        return get( cacheName, key, 0 );
    }

    /**
     * Do nothing.
     */
    public void release()
    {
        return;
    }

    /**
     * Set the last remove key.
     * <p>
     * @param cacheName
     * @param key
     */
    public void remove( String cacheName, Serializable key )
    {
        lastRemoveKey = key;
    }

    /**
     * Set the last remove all cache name.
     * <p>
     * @param cacheName
     */
    public void removeAll( String cacheName )
    {
        lastRemoveAllCacheName = cacheName;
    }

    /**
     * Set the last update item.
     * <p>
     * @param item
     */
    public void update( ICacheElement item )
    {
        lastUpdate = item;
    }

    /**
     * @param cacheName
     * @param keys
     * @param requesterId - identity of requester
     * @return empty map
     */
    public Map getMultiple( String cacheName, Set keys, long requesterId )
    {
        lastGetMultipleKeys = keys;
        return new HashMap();
    }

    /**
     * @param cacheName
     * @param keys
     * @return empty map
     */
    public Map getMultiple( String cacheName, Set keys )
    {
        return getMultiple( cacheName, keys, 0 );
    }

    /**
     * Returns an empty map. Zombies have no internal data.
     * <p>
     * @param cacheName
     * @param pattern
     * @return an empty map
     * @throws IOException
     */
    public Map getMatching( String cacheName, String pattern )
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
    public Map getMatching( String cacheName, String pattern, long requesterId )
        throws IOException
    {
        lastGetMatchingPattern = pattern;
        return new HashMap();
    }
}
