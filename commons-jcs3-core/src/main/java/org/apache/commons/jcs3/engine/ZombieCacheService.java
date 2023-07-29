package org.apache.commons.jcs3.engine;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheService;
import org.apache.commons.jcs3.engine.behavior.IZombie;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Zombie adapter for any cache service. Balks at every call.
 */
public class ZombieCacheService<K, V>
    implements ICacheService<K, V>, IZombie
{
    /** The logger. */
    private static final Log log = LogManager.getLog( ZombieCacheService.class );

    /**
     * @param item
     */
    public void put( final ICacheElement<K, V> item )
    {
        log.debug( "Zombie put for item {0}", item );
        // zombies have no inner life
    }

    /**
     * Does nothing.
     * <p>
     * @param item
     */
    @Override
    public void update( final ICacheElement<K, V> item )
    {
        // zombies have no inner life
    }

    /**
     * @param cacheName
     * @param key
     * @return null. zombies have no internal data
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key )
    {
        return null;
    }

    /**
     * Returns an empty map. Zombies have no internal data.
     * <p>
     * @param cacheName
     * @param keys
     * @return Collections.EMPTY_MAP
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys )
    {
        return Collections.emptyMap();
    }

    /**
     * Returns an empty map. Zombies have no internal data.
     * <p>
     * @param cacheName
     * @param pattern
     * @return Collections.EMPTY_MAP
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
    {
        return Collections.emptyMap();
    }

    /**
     * Logs the get to debug, but always balks.
     * <p>
     * @param cacheName
     * @param key
     * @param container
     * @return null always
     */
    public Serializable get( final String cacheName, final K key, final boolean container )
    {
        log.debug( "Zombie get for key [{0}] cacheName [{1}] container [{2}]",
                key, cacheName, container);
        // zombies have no inner life
        return null;
    }

    /**
     * @param cacheName
     * @param key
     */
    @Override
    public void remove( final String cacheName, final K key )
    {
        // zombies have no inner life
    }

    /**
     * @param cacheName
     */
    @Override
    public void removeAll( final String cacheName )
    {
        // zombies have no inner life
    }

    /**
     * @param cacheName
     */
    @Override
    public void dispose( final String cacheName )
    {
        // zombies have no inner life
    }

    /**
     * Frees all caches.
     */
    @Override
    public void release()
    {
        // zombies have no inner life
    }
}
