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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This is a mock impl of the remote cache service.
 * <p>
 * @author admin
 */
public class MockRemoteCacheService
    implements IRemoteCacheService
{
    /** The object that was last passed to update. */
    public Object lastUpdate;

    /** The key that was last passed to remove. */
    public Object lastRemoveKey;

    /** The cache name that was last passed to removeAll. */
    public String lastRemoveAllCacheName;

    /**
     * @param cacheName 
     * @param key 
     * @param requesterId 
     * @return null
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
    {
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
     * @param requesterId 
     */
    public void remove( String cacheName, Serializable key, long requesterId )
    {
        lastRemoveKey = key;
    }

    /**
     * Set the lastRemoveAllCacheName to the cacheName.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#removeAll(java.lang.String,
     *      long)
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
     * @param requesterId 
     */
    public void update( ICacheElement item, long requesterId )
    {
        lastUpdate = item;
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
        return null;
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
     * @param requesterId 
     * @return empty map
     */
    public Map getMultiple( String cacheName, Set keys, long requesterId )
    {
        return new HashMap();
    }

    /**
     * @param cacheName 
     * @param keys 
     * @return empty map
     */
    public Map getMultiple( String cacheName, Set keys )
    {
        return new HashMap();
    }

}
