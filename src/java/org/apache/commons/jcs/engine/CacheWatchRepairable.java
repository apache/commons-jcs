package org.apache.commons.jcs.engine;

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

import org.apache.commons.jcs.engine.behavior.ICacheListener;
import org.apache.commons.jcs.engine.behavior.ICacheObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Intercepts the requests to the underlying ICacheObserver object so that the listeners can be
 * recorded locally for remote connection recovery purposes. (Durable subscription like those in JMS
 * is not implemented at this stage for it can be too expensive.)
 */
public class CacheWatchRepairable
    implements ICacheObserver
{
    /** The logger */
    private final static Log log = LogFactory.getLog( CacheWatchRepairable.class );

    /** the underlying ICacheObserver. */
    private ICacheObserver cacheWatch;

    /** Map of cache regions. */
    private final Map<String, Set<ICacheListener<? extends Serializable, ? extends Serializable>>> cacheMap =
        new HashMap<String, Set<ICacheListener<? extends Serializable, ? extends Serializable>>>();

    /**
     * Replaces the underlying cache watch service and reattached all existing listeners to the new
     * cache watch.
     * <p>
     * @param cacheWatch The new cacheWatch value
     */
    public void setCacheWatch( ICacheObserver cacheWatch )
    {
        this.cacheWatch = cacheWatch;
        synchronized ( cacheMap )
        {
            for (Map.Entry<String, Set<ICacheListener<? extends Serializable, ? extends Serializable>>> entry : cacheMap.entrySet())
            {
                String cacheName = entry.getKey();
                for (ICacheListener<? extends Serializable, ? extends Serializable> listener : entry.getValue())
                {
                    try
                    {
                        if ( log.isInfoEnabled() )
                        {
                            log.info( "Adding listener to cache watch. ICacheListener = " + listener
                                + " | ICacheObserver = " + cacheWatch );
                        }
                        cacheWatch.addCacheListener( cacheName, listener );
                    }
                    catch ( IOException ex )
                    {
                        log.error( "Problem adding listener. ICacheListener = " + listener + " | ICacheObserver = "
                            + cacheWatch, ex );
                    }
                }
            }
        }
    }

    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable object
     * <p>
     * @param cacheName The feature to be added to the CacheListener attribute
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    @Override
    public <K extends Serializable, V extends Serializable> void addCacheListener( String cacheName, ICacheListener<K, V> obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the
        // remote add-listener operation succeeds or fails.
        synchronized ( cacheMap )
        {
            Set<ICacheListener<? extends Serializable, ? extends Serializable>> listenerSet = cacheMap.get( cacheName );
            if ( listenerSet == null )
            {
                listenerSet = new HashSet<ICacheListener<? extends Serializable, ? extends Serializable>>();
                cacheMap.put( cacheName, listenerSet );
            }
            listenerSet.add( obj );
        }
        if ( log.isInfoEnabled() )
        {
            log.info( "Adding listener to cache watch. ICacheListener = " + obj
                + " | ICacheObserver = " + cacheWatch + " | cacheName = " + cacheName );
        }
        cacheWatch.addCacheListener( cacheName, obj );
    }

    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable object
     * <p>
     * @param obj The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    @Override
    public <K extends Serializable, V extends Serializable> void addCacheListener( ICacheListener<K, V> obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the
        // remote add-listener operation succeeds or fails.
        synchronized ( cacheMap )
        {
            for (Set<ICacheListener<? extends Serializable, ? extends Serializable>> listenerSet : cacheMap.values())
            {
                listenerSet.add( obj );
            }
        }
        if ( log.isInfoEnabled() )
        {
            log.info( "Adding listener to cache watch. ICacheListener = " + obj
                + " | ICacheObserver = " + cacheWatch );
        }
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
    public <K extends Serializable, V extends Serializable> void removeCacheListener( String cacheName, ICacheListener<K, V> obj )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "removeCacheListener, cacheName [" + cacheName + "]" );
        }
        // Record the removal locally, regardless of whether the remote
        // remove-listener operation succeeds or fails.
        synchronized ( cacheMap )
        {
            Set<ICacheListener<? extends Serializable, ? extends Serializable>> listenerSet = cacheMap.get( cacheName );
            if ( listenerSet != null )
            {
                listenerSet.remove( obj );
            }
        }
        cacheWatch.removeCacheListener( cacheName, obj );
    }

    /**
     * @param obj
     * @throws IOException
     */
    @Override
    public <K extends Serializable, V extends Serializable> void removeCacheListener( ICacheListener<K, V> obj )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "removeCacheListener, ICacheListener [" + obj + "]" );
        }

        // Record the removal locally, regardless of whether the remote
        // remove-listener operation succeeds or fails.
        synchronized ( cacheMap )
        {
            for (Set<ICacheListener<? extends Serializable, ? extends Serializable>> listenerSet : cacheMap.values())
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Before removing [" + obj + "] the listenerSet = " + listenerSet );
                }
                listenerSet.remove( obj );
            }
        }
        cacheWatch.removeCacheListener( obj );
    }
}
