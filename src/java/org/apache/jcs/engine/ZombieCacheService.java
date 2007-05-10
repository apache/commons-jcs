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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

import org.apache.jcs.engine.behavior.IZombie;

/**
 * Zombie adapter for any cache service. balks at every call.
 */
public class ZombieCacheService
    implements ICacheService, IZombie
{

    private static final Log log = LogFactory.getLog( ZombieCacheService.class );

    /**
     * @param item
     */
    public void put( ICacheElement item )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Zombie put for item " + item );
        }
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement item )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#get(java.lang.String,
     *      java.io.Serializable)
     */
    public ICacheElement get( String cacheName, Serializable key )
    {
        return null;
    }

    /**
     * Logs the get to debug, but always balks.
     * <p>
     * @param cacheName
     * @param key
     * @param container
     * @return null always
     */
    public Serializable get( String cacheName, Serializable key, boolean container )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Zombie get for key [" + key + "] cacheName [" + cacheName + "] container [" + container + "]" );
        }
        // zombies have no inner life
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#remove(java.lang.String,
     *      java.io.Serializable)
     */
    public void remove( String cacheName, Serializable key )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#removeAll(java.lang.String)
     */
    public void removeAll( String cacheName )
    {
        // zombies have no inner life
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#dispose(java.lang.String)
     */
    public void dispose( String cacheName )
    {
        // zombies have no inner life
        return;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheService#release()
     */
    public void release()
    {
        // zombies have no inner life
        return;
    }

}
