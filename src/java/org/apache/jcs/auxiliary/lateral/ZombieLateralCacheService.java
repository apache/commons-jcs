package org.apache.jcs.auxiliary.lateral;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.engine.ZombieCacheService;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Description of the Class
 *
 */
public class ZombieLateralCacheService extends ZombieCacheService implements ILateralCacheService
{

    /** Description of the Method */
    public void update( ICacheElement item, byte listenerId ) { }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key, byte listenerId ) { }


    /** Description of the Method */
    public void removeAll( String cacheName, byte listenerId ) { }

    public Set getGroupKeys(String cacheName, String groupName)
    {
        return Collections.EMPTY_SET;
    }
}
