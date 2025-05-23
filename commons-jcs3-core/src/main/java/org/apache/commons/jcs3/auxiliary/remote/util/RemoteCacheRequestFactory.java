package org.apache.commons.jcs3.auxiliary.remote.util;

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

import java.util.Set;

import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.log.Log;

/**
 * This creates request objects. You could write your own client and use the objects from this
 * factory.
 */
public class RemoteCacheRequestFactory
{
    /** The Logger. */
    private static final Log log = Log.getLog( RemoteCacheRequestFactory.class );

    /**
     * Creates an alive check Request.
     *
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createAliveCheckRequest( final long requesterId )
    {

        return createRequest(null, RemoteRequestType.ALIVE_CHECK, requesterId);
    }

    /**
     * Creates a dispose Request.
     *
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createDisposeRequest( final String cacheName, final long requesterId )
    {

        return createRequest(cacheName, RemoteRequestType.DISPOSE, requesterId);
    }

    /**
     * Creates a GetKeySet Request.
     *
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest<String, String> createGetKeySetRequest( final String cacheName, final long requesterId )
    {
        final RemoteCacheRequest<String, String> request = createRequest(cacheName, RemoteRequestType.GET_KEYSET, requesterId);
        request.setKey( cacheName );

        return request;
    }

    /**
     * Creates a getMatching Request.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createGetMatchingRequest( final String cacheName, final String pattern, final long requesterId )
    {
        final RemoteCacheRequest<K, V> request = createRequest(cacheName, RemoteRequestType.GET_MATCHING, requesterId);
        request.setPattern( pattern );

        return request;
    }

    /**
     * Creates a getMultiple Request.
     *
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createGetMultipleRequest( final String cacheName, final Set<K> keys, final long requesterId )
    {
        final RemoteCacheRequest<K, V> request = createRequest(cacheName, RemoteRequestType.GET_MULTIPLE, requesterId);
        request.setKeySet(keys);

        return request;
    }

    /**
     * Creates a get Request.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createGetRequest( final String cacheName, final K key, final long requesterId )
    {
        final RemoteCacheRequest<K, V> request = createRequest(cacheName, RemoteRequestType.GET, requesterId);
        request.setKey( key );

        return request;
    }

    /**
     * Creates a removeAll Request.
     *
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createRemoveAllRequest( final String cacheName, final long requesterId )
    {

        return createRequest(cacheName, RemoteRequestType.REMOVE_ALL, requesterId);
    }

    /**
     * Creates a remove Request.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createRemoveRequest( final String cacheName, final K key, final long requesterId )
    {
        final RemoteCacheRequest<K, V> request = createRequest(cacheName, RemoteRequestType.REMOVE, requesterId);
        request.setKey( key );

        return request;
    }

    /**
     * Create generic request
     * @param cacheName cache name
     * @param requestType type of request
     * @param requesterId id of requester
     * @return the request
     */
    private static <K, V> RemoteCacheRequest<K, V> createRequest(final String cacheName, final RemoteRequestType requestType, final long requesterId)
    {
        final RemoteCacheRequest<K, V> request = new RemoteCacheRequest<>();
        request.setCacheName( cacheName );
        request.setRequestType( requestType );
        request.setRequesterId( requesterId );

        log.debug( "Created: {0}", request );

        return request;
    }

    /**
     * Creates an Update Request.
     *
     * @param cacheElement
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static <K, V> RemoteCacheRequest<K, V> createUpdateRequest( final ICacheElement<K, V> cacheElement, final long requesterId )
    {
        final RemoteCacheRequest<K, V> request = createRequest(null, RemoteRequestType.UPDATE, requesterId);
        if ( cacheElement != null )
        {
            request.setCacheName( cacheElement.getCacheName() );
            request.setCacheElement( cacheElement );
            request.setKey( cacheElement.getKey() );
        }
        else
        {
            log.error( "Can't create a proper update request for a null cache element." );
        }

        return request;
    }
}
