package org.apache.commons.jcs4.auxiliary.remote.http.server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs4.auxiliary.AbstractCacheEventLogSupport;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger.CacheEventType;
import org.apache.commons.jcs4.log.Log;

/**
 * This class contains common methods for remote cache services. Eventually I hope to extract out
 * much of the RMI server to use this as well. I'm starting with the HTTP service.
 */
public abstract class AbstractRemoteCacheService<K, V>
    extends AbstractCacheEventLogSupport<K, V>
    implements ICacheServiceNonLocal<K, V>
{
    /** The interval at which we will log updates. */
    private final static int logInterval = 100;

    /** Log instance */
    private static final Log log = Log.getLog( AbstractRemoteCacheService.class );

    /** The central hub */
    private ICompositeCacheManager cacheManager;

    /** Name of the event log source. */
    private String eventLogSourceName = "AbstractRemoteCacheService";

    /** Number of puts into the cache. */
    private AtomicInteger puts = new AtomicInteger();

    /**
     * Creates the super with the needed items.
     *
     * @param cacheManager
     * @param cacheEventLogger
     */
    public AbstractRemoteCacheService( final ICompositeCacheManager cacheManager, final ICacheEventLogger cacheEventLogger )
    {
        this.cacheManager = cacheManager;
        setCacheEventLogger(cacheEventLogger);
    }

    /**
     * Frees the specified remote cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void dispose( final String cacheName )
        throws IOException
    {
        dispose( cacheName, 0 );
    }

    /**
     * Frees the specified remote cache.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void dispose( final String cacheName, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent(cacheName, "none",
                CacheEventType.DISPOSE_EVENT, () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            processDispose( cacheName, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Returns a cache value from the specified remote cache; or null if the cache or key does not
     * exist.
     *
     * @param cacheName
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key )
        throws IOException
    {
        return this.get( cacheName, key, 0 );
    }

    /**
     * Returns a cache bean from the specified cache; or null if the key does not exist.
     * <p>
     * Adding the requestor id, allows the cache to determine the source of the get.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        ICacheElement<K, V> element = null;
        final ICacheEvent<K> cacheEvent = createICacheEvent(cacheName, key,
                CacheEventType.GET_EVENT, () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            element = processGet( cacheName, key, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
        return element;
    }

    /**
     * @return the cacheManager
     */
    protected ICompositeCacheManager getCacheManager()
    {
        return cacheManager;
    }

    /**
     * @return the eventLogSourceName
     */
    @Override
    protected String getEventLogSourceName()
    {
        return eventLogSourceName;
    }

    /**
     * Ip address for the client, if one is stored.
     * <p>
     * Protected for testing.
     *
     * @param requesterId
     * @return String
     */
    protected abstract String getExtraInfoForRequesterId( long requesterId );

    /**
     * Return the keys in this cache.
     *
     * @see org.apache.commons.jcs4.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet( final String cacheName )
    {
        return processGetKeySet( cacheName );
    }

    /**
     * Gets all matching items.
     *
     * @param cacheName
     * @param pattern
     * @return Map of keys and wrapped objects
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, 0 );
    }

    /**
     * Gets all matching keys.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map of keys and wrapped objects
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent(cacheName, pattern,
                CacheEventType.GETMATCHING_EVENT, () -> getExtraInfoForRequesterId(requesterId));
        try
        {
            return processGetMatching( cacheName, pattern, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param cacheName
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys )
        throws IOException
    {
        return this.getMultiple( cacheName, keys, 0 );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
        throws IOException
    {
        final ICacheEvent<Serializable> cacheEvent = createICacheEvent(cacheName,
                (Serializable) keys, CacheEventType.GETMULTIPLE_EVENT,
                () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            return processGetMultiple( cacheName, keys, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Log some details.
     *
     * @param item
     */
    private void logUpdateInfo( final ICacheElement<K, V> item )
    {
        if ( log.isInfoEnabled() )
        {
            int p = puts.incrementAndGet();
            if ( p % logInterval == 0 )
            {
                log.info( "puts = {0}", p );
            }
        }

        log.debug( "In update, put [{0}] in [{1}]", item::key,
                item::cacheName);
    }

    /**
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    abstract void processDispose( String cacheName, long requesterId )
        throws IOException;

    /**
     * Returns a cache bean from the specified cache; or null if the key does not exist.
     * <p>
     * Adding the requestor id, allows the cache to determine the source of the get.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    abstract ICacheElement<K, V> processGet( String cacheName, K key, long requesterId )
        throws IOException;

    /**
     * Gets the set of keys of objects currently in the cache.
     *
     * @param cacheName
     * @return Set
     */
    public Set<K> processGetKeySet( final String cacheName )
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        return cache.getKeySet();
    }

    /**
     * Retrieves all matching keys.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map of keys and wrapped objects
     * @throws IOException
     */
    abstract Map<K, ICacheElement<K, V>> processGetMatching( String cacheName, String pattern, long requesterId )
        throws IOException;

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    abstract Map<K, ICacheElement<K, V>> processGetMultiple( String cacheName, Set<K> keys, long requesterId )
        throws IOException;

    /**
     * Remove the key from the cache region and don't tell the source listener about it.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    abstract void processRemove( String cacheName, K key, long requesterId )
        throws IOException;

    /**
     * Remove all keys from the specified remote cache.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    abstract void processRemoveAll( String cacheName, long requesterId )
        throws IOException;

    /**
     * The internal processing is wrapped in event logging calls.
     *
     * @param item
     * @param requesterId
     * @throws IOException
     */
    abstract void processUpdate( ICacheElement<K, V> item, long requesterId )
        throws IOException;

    /**
     * Removes the given key from the specified remote cache. Defaults the listener id to 0.
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void remove( final String cacheName, final K key )
        throws IOException
    {
        remove( cacheName, key, 0 );
    }

    /**
     * Remove the key from the cache region and don't tell the source listener about it.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void remove( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent(cacheName, key,
                CacheEventType.REMOVE_EVENT, () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            processRemove( cacheName, key, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Remove all keys from the specified remote cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName )
        throws IOException
    {
        removeAll( cacheName, 0 );
    }

    /**
     * Remove all keys from the specified remote cache.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent(cacheName, "all",
                CacheEventType.REMOVEALL_EVENT, () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            processRemoveAll( cacheName, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * @param cacheManager the cacheManager to set
     */
    protected void setCacheManager( final ICompositeCacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    /**
     * @param eventLogSourceName the eventLogSourceName to set
     */
    protected void setEventLogSourceName( final String eventLogSourceName )
    {
        this.eventLogSourceName = eventLogSourceName;
    }

    /**
     * @param item
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> item )
        throws IOException
    {
        update( item, 0 );
    }

    /**
     * The internal processing is wrapped in event logging calls.
     *
     * @param item
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> item, final long requesterId )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( item,
                CacheEventType.UPDATE_EVENT, () -> getExtraInfoForRequesterId( requesterId ));
        try
        {
            logUpdateInfo( item );

            processUpdate( item, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }
}
