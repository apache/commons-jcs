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

import java.io.IOException;

import org.apache.commons.jcs3.engine.behavior.ICache;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;

/**
 * Used for Cache-to-Cache messaging purposes. These are used in the balking
 * facades in the lateral and remote caches.
 */
public class CacheAdaptor<K, V>
    implements ICacheListener<K, V>
{
    /** The cache we are adapting. */
    private final ICache<K, V> cache;

    /** The unique id of this listener. */
    private long listenerId;

    /**
     * Sets the listenerId attribute of the CacheAdaptor object
     * <p>
     * @param id
     *            The new listenerId value
     * @throws IOException
     */
    @Override
    public void setListenerId( final long id )
        throws IOException
    {
        this.listenerId = id;
    }

    /**
     * Gets the listenerId attribute of the CacheAdaptor object
     * <p>
     * @return The listenerId value
     * @throws IOException
     */
    @Override
    public long getListenerId()
        throws IOException
    {
        return this.listenerId;
    }

    /**
     * Constructor for the CacheAdaptor object
     * <p>
     * @param cache
     */
    public CacheAdaptor( final ICache<K, V> cache )
    {
        this.cache = cache;
    }

    /**
     * Puts an item into the cache.
     * <p>
     * @param item
     * @throws IOException
     */
    @Override
    public void handlePut( final ICacheElement<K, V> item )
        throws IOException
    {
        try
        {
            cache.update( item );
        }
        catch ( final IOException e )
        {
            // swallow
        }
    }

    /**
     * Removes an item.
     * <p>
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void handleRemove( final String cacheName, final K key )
        throws IOException
    {
        cache.remove( key );
    }

    /**
     * Clears the region.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleRemoveAll( final String cacheName )
        throws IOException
    {
        cache.removeAll();
    }

    /**
     * Shutdown call.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void handleDispose( final String cacheName )
        throws IOException
    {
        cache.dispose();
    }
}
