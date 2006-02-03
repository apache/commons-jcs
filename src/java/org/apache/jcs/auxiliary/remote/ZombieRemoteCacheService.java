package org.apache.jcs.auxiliary.remote;

/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.util.Set;
import java.util.Collections;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.ZombieCacheService;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Zombie adapter for the remote cache service.  It just balks.
 *  
 */
public class ZombieRemoteCacheService
    extends ZombieCacheService
    implements IRemoteCacheService
{

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#update(org.apache.jcs.engine.behavior.ICacheElement, long)
     */
    public void update( ICacheElement item, long listenerId )
    {
        // Zombies have no inner life
        return;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#remove(java.lang.String, java.io.Serializable, long)
     */
    public void remove( String cacheName, Serializable key, long listenerId )
    {
        // Zombies have no inner life
        return;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#removeAll(java.lang.String, long)
     */
    public void removeAll( String cacheName, long listenerId )
    {
        // Zombies have no inner life
        return;
    }

    public Set getGroupKeys( String cacheName, String groupName )
    {
        return Collections.EMPTY_SET;
    }
}
