package org.apache.jcs.auxiliary.lateral.behavior;

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
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

/**
 * Used to retrieve and update the lateral cache.
 */
public interface ILateralCacheService
    extends ICacheService
{
    /**
     * Puts a cache item to the cache.
     * <p>
     * @param item
     * @param requesterId
     * @throws IOException
     */
    public void update( ICacheElement item, long requesterId )
        throws IOException;

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
     * @param cacheName
     * @param groupName
     * @return
     */
    public Set getGroupKeys( String cacheName, String groupName );
}
