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
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.engine.behavior.ICacheObserver;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Intercepts the requests to the underlying ICacheObserver object so that the listeners can be
 * recorded locally for remote connection recovery purposes. (Durable subscription like those in JMS
 * is not implemented at this stage for it can be too expensive.)
 */
public class CacheWatchRepairable
    implements ICacheObserver
{
    /** The logger */
    private static final Log log = LogManager.getLog( CacheWatchRepairable.class );

    /** the underlying ICacheObserver. */
    private ICacheObserver cacheWatch;

    /** Map of cache regions. */
    private final ConcurrentMap<String, Set<ICacheListener<?, ?>>> cacheMap =
        new ConcurrentHashMap<>();

    /**
     * Replaces the underlying cache watch service and re-attaches all existing listeners to the new
     * cache watch.
     * <p>
     * @param cacheWatch The new cacheWatch value
     */
    public void setCacheWatch( final ICacheObserver cacheWatch )
    {
        this.cacheWatch = cacheWatch;
        cacheMap.forEach((cacheName, value) -> value.forEach(listener -> {
                try
                {
                    log.info( "Adding listener to cache watch. ICacheListener = "
                            + "{0} | ICacheObserver = {1}", listener, cacheWatch );
                    cacheWatch.addCacheListener( cacheName, listener );
                }
                catch ( final IOException ex )
                {
                    log.error( "Problem adding listener. ICacheListener = {0} | "
                            + "ICacheObserver = {1}", listener, cacheWatch, ex );
                }
        }));
    }

    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable object
     * <p>
     * @param cacheName The feature to be added to the CacheListener attribute
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    @Override
    public <K, V> void addCacheListener( final String cacheName, final ICacheListener<K, V> obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the
        // remote add-listener operation succeeds or fails.
        cacheMap.computeIfAbsent(cacheName, key -> new CopyOnWriteArraySet<>(Collections.singletonList(obj)));

        log.info( "Adding listener to cache watch. ICacheListener = {0} | "
                + "ICacheObserver = {1} | cacheName = {2}", obj, cacheWatch,
                cacheName );
        cacheWatch.addCacheListener( cacheName, obj );
    }

    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable object
     * <p>
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    @Override
    public <K, V> void addCacheListener( final ICacheListener<K, V> obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the
        // remote add-listener operation succeeds or fails.
        cacheMap.values().forEach(set -> set.add(obj));

        log.info( "Adding listener to cache watch. ICacheListener = {0} | "
                + "ICacheObserver = {1}", obj, cacheWatch );
        cacheWatch.addCacheListener( obj );
    }

    /**
     * Tell the server to release us.
     * <p>
     * @param cacheName
     * @param obj
     * @throws IOException
     */
    @Override
    public <K, V> void removeCacheListener( final String cacheName, final ICacheListener<K, V> obj )
        throws IOException
    {
        log.info( "removeCacheListener, cacheName [{0}]", cacheName );
        // Record the removal locally, regardless of whether the remote
        // remove-listener operation succeeds or fails.
        final Set<ICacheListener<?, ?>> listenerSet = cacheMap.get( cacheName );
        if ( listenerSet != null )
        {
            listenerSet.remove( obj );
        }
        cacheWatch.removeCacheListener( cacheName, obj );
    }

    /**
     * @param obj
     * @throws IOException
     */
    @Override
    public <K, V> void removeCacheListener( final ICacheListener<K, V> obj )
        throws IOException
    {
        log.info( "removeCacheListener, ICacheListener [{0}]", obj );

        // Record the removal locally, regardless of whether the remote
        // remove-listener operation succeeds or fails.
        cacheMap.values().forEach(set -> {
            log.debug("Before removing [{0}] the listenerSet = {1}", obj, set);
            set.remove( obj );
        });
        cacheWatch.removeCacheListener( obj );
    }
}
