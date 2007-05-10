package org.apache.jcs.auxiliary.lateral;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.engine.ZombieCacheService;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * The ZombieLateralCacheService is used as a facade when the lateral is not
 * available. It balks when the lateral is in error. When lateral service is
 * restored, this is replaced by a live facade.
 *
 */
public class ZombieLateralCacheService
    extends ZombieCacheService
    implements ILateralCacheService
{

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#update(org.apache.jcs.engine.behavior.ICacheElement,
     *      long)
     */
    public void update( ICacheElement item, long listenerId )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#remove(java.lang.String,
     *      java.io.Serializable, long)
     */
    public void remove( String cacheName, Serializable key, long listenerId )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#removeAll(java.lang.String,
     *      long)
     */
    public void removeAll( String cacheName, long listenerId )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService#getGroupKeys(java.lang.String,
     *      java.lang.String)
     */
    public Set getGroupKeys( String cacheName, String groupName )
    {
        return Collections.EMPTY_SET;
    }
}
