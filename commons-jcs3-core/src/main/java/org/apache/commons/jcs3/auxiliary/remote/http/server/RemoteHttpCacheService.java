package org.apache.commons.jcs3.auxiliary.remote.http.server;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;

/**
 * This does the work. It's called by the processor. The base class wraps the processing calls in
 * event logs, if an event logger is present.
 * <p>
 * For now we assume that all clients are non-cluster clients. And listener notification is not
 * supported.
 */
public class RemoteHttpCacheService<K, V>
    extends AbstractRemoteCacheService<K, V>
{
    /** The name used in the event logs. */
    private static final String EVENT_LOG_SOURCE_NAME = "RemoteHttpCacheServer";

    /** The configuration */
    private final RemoteHttpCacheServerAttributes remoteHttpCacheServerAttributes;

    /**
     * Create a process with a cache manager.
     * <p>
     * @param cacheManager
     * @param remoteHttpCacheServerAttributes
     * @param cacheEventLogger
     */
    public RemoteHttpCacheService( final ICompositeCacheManager cacheManager,
                                   final RemoteHttpCacheServerAttributes remoteHttpCacheServerAttributes,
                                   final ICacheEventLogger cacheEventLogger )
    {
        super( cacheManager, cacheEventLogger );
        setEventLogSourceName( EVENT_LOG_SOURCE_NAME );
        this.remoteHttpCacheServerAttributes = remoteHttpCacheServerAttributes;
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request or non-remote origins.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> processGet( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGet( key );
        }
        return cache.get( key );
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return Map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> processGetMultiple( final String cacheName, final Set<K> keys, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGetMultiple( keys );
        }
        return cache.getMultiple( keys );
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> processGetMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGetMatching( pattern );
        }
        return cache.getMatching( pattern );
    }

    /**
     * Processes an update request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param item
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void processUpdate( final ICacheElement<K, V> item, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( item.getCacheName() );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localUpdate( item );
        }
        else
        {
            cache.update( item );
        }
    }

    /**
     * Processes a remove request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void processRemove( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localRemove( key );
        }
        else
        {
            cache.remove( key );
        }
    }

    /**
     * Processes a removeAll request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void processRemoveAll( final String cacheName, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );

        final boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localRemoveAll();
        }
        else
        {
            cache.removeAll();
        }
    }

    /**
     * Processes a shutdown request.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void processDispose( final String cacheName, final long requesterId )
        throws IOException
    {
        final CompositeCache<K, V> cache = getCacheManager().getCache( cacheName );
        cache.dispose();
    }

    /**
     * This general method should be deprecated.
     * <p>
     * @throws IOException
     */
    @Override
    public void release()
        throws IOException
    {
        //nothing.
    }

    /**
     * This is called by the event log.
     * <p>
     * @param requesterId
     * @return requesterId + ""
     */
    @Override
    protected String getExtraInfoForRequesterId( final long requesterId )
    {
        return requesterId + "";
    }
}
