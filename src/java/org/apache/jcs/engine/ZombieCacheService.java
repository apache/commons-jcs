package org.apache.jcs.engine;

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

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Zombie adapter for any cache service.  balks at every call.
 *  
 */
public class ZombieCacheService
    implements ICacheService, IZombie
{

    /*
     * 
     */
    public void put( ICacheElement item )
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement item )
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#get(java.lang.String, java.io.Serializable)
     */
    public ICacheElement get( String cacheName, Serializable key )
    {
        return null;
    }


    /**
     * 
     * @param cacheName
     * @param key
     * @param container
     * @return
     */
    public Serializable get( String cacheName, Serializable key, boolean container )
    {
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#remove(java.lang.String, java.io.Serializable)
     */
    public void remove( String cacheName, Serializable key )
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#removeAll(java.lang.String)
     */
    public void removeAll( String cacheName )
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#dispose(java.lang.String)
     */
    public void dispose( String cacheName )
    {
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#release()
     */
    public void release()
    {
    }

}
