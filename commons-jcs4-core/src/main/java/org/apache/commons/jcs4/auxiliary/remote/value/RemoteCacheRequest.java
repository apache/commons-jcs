package org.apache.commons.jcs4.auxiliary.remote.value;

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

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;

/**
 * The basic request wrapper. The different types of requests are differentiated by their types.
 * <p>
 * Rather than creating sub object types, I created on object that has values for all types of
 * requests.
 */
public record RemoteCacheRequest<K, V>(
        /** The name of the region */
        String cacheName,

        /** The request type specifies the type of request: get, put, remove, . . */
        RemoteRequestType requestType,

        /** Used to identify the source. Same as listener id on the client side. */
        long requesterId,

        /** The key, if this request has a key. */
        K key,

        /** The keySet, if this request has a keySet. Only getMultiple requests. */
        Set<K> keySet,

        /** The pattern, if this request uses a pattern. Only getMatching requests. */
        String pattern,

        /** The ICacheEleemnt, if this request contains a value. Only update requests will have this. */
        ICacheElement<K, V> cacheElement
) implements Serializable
{
    /** Don't change. */
    private static final long serialVersionUID = -8858447417390442569L;


    /**
     * Construct a RemoteCacheRequest
     *
     * @param cacheName the name of the region
     * @param requestType the request type specifies the type of request: get, put, remove, ...
     * @param requesterId used to identify the source. Same as listener id on the client side.
     */
    public RemoteCacheRequest(final String cacheName, final RemoteRequestType requestType,
            final long requesterId)
    {
        this(cacheName, requestType, requesterId, null, null, null, null);
    }

    /**
     * Construct a RemoteCacheRequest with key
     *
     * @param cacheName the name of the region
     * @param requestType the request type specifies the type of request: get, put, remove, ...
     * @param requesterId used to identify the source. Same as listener id on the client side.
     * @param key the key
     */
    public RemoteCacheRequest(final String cacheName, final RemoteRequestType requestType,
            final long requesterId, final K key)
    {
        this(cacheName, requestType, requesterId, key, null, null, null);
    }

    /**
     * Construct a RemoteCacheRequest with KeySet
     *
     * @param cacheName the name of the region
     * @param requestType the request type specifies the type of request: get, put, remove, ...
     * @param requesterId used to identify the source. Same as listener id on the client side.
     * @param keySet the KeySet
     */
    public RemoteCacheRequest(final String cacheName, final RemoteRequestType requestType,
            final long requesterId, final Set<K> keySet)
    {
        this(cacheName, requestType, requesterId, null, keySet, null, null);
    }

    /**
     * Construct a RemoteCacheRequest with Pattern
     *
     * @param cacheName the name of the region
     * @param requestType the request type specifies the type of request: get, put, remove, ...
     * @param requesterId used to identify the source. Same as listener id on the client side.
     * @param pattern the Pattern
     */
    public RemoteCacheRequest(final String cacheName, final RemoteRequestType requestType,
            final long requesterId, final String pattern)
    {
        this(cacheName, requestType, requesterId, null, null, pattern, null);
    }

    /**
     * Construct a RemoteCacheRequest with CacheElement
     *
     * @param cacheName the name of the region
     * @param requestType the request type specifies the type of request: get, put, remove, ...
     * @param requesterId used to identify the source. Same as listener id on the client side.
     * @param cacheElement the CacheElement
     */
    public RemoteCacheRequest(final String cacheName, final RemoteRequestType requestType,
            final long requesterId, final ICacheElement<K, V> cacheElement)
    {
        this(cacheName, requestType, requesterId, cacheElement.key(), null, null, cacheElement);
    }

    /** @return string */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\nRemoteHttpCacheRequest" );
        buf.append( "\n requesterId [" + requesterId() + "]" );
        buf.append( "\n requestType [" + requestType() + "]" );
        buf.append( "\n cacheName [" + cacheName() + "]" );
        buf.append( "\n key [" + key() + "]" );
        buf.append( "\n keySet [" + keySet() + "]" );
        buf.append( "\n pattern [" + pattern() + "]" );
        buf.append( "\n cacheElement [" + cacheElement() + "]" );
        return buf.toString();
    }
}
