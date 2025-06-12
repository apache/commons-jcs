package org.apache.commons.jcs3.auxiliary.remote.http.client;

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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.commons.jcs3.auxiliary.remote.http.client.behavior.IRemoteHttpCacheClient;
import org.apache.commons.jcs3.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.log.Log;

/** This is the service used by the remote http auxiliary cache. */
public class RemoteHttpCacheClient<K, V>
    implements IRemoteHttpCacheClient<K, V>
{
    /** The Logger. */
    private static final Log log = Log.getLog( RemoteHttpCacheClient.class );

    /** The internal client. */
    private IRemoteCacheDispatcher remoteDispatcher;

    /** The remote attributes */
    private RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /** Set to true when initialize is called */
    private boolean initialized;

    /** For factory construction. */
    public RemoteHttpCacheClient()
    {
        // does nothing
    }

    /**
     * Constructs a client.
     *
     * @param attributes
     */
    public RemoteHttpCacheClient( final RemoteHttpCacheAttributes attributes )
    {
        setRemoteHttpCacheAttributes( attributes );
        initialize( attributes );
    }

    /**
     * Frees the specified cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void dispose( final String cacheName )
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createDisposeRequest( cacheName, 0 );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Create a request, process, extract the payload.
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
        return get( cacheName, key, 0 );
    }

    /**
     * Create a request, process, extract the payload.
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
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized. Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }
        final RemoteCacheRequest<K, Serializable> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        final RemoteCacheResponse<ICacheElement<K, V>> remoteHttpCacheResponse =
            getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        log.debug( "Get [{0}] = {1}", key, remoteHttpCacheResponse );

        if ( remoteHttpCacheResponse != null)
        {
            return remoteHttpCacheResponse.getPayload();
        }

        return null;
    }

    /**
     * Return the keys in this cache.
     *
     * @param cacheName the name of the cache
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet( final String cacheName ) throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<String, String> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createGetKeySetRequest(cacheName, 0 );

        final RemoteCacheResponse<Set<K>> remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( remoteHttpCacheResponse != null && remoteHttpCacheResponse.getPayload() != null )
        {
            return remoteHttpCacheResponse.getPayload();
        }

        return Collections.emptySet();
    }

    /**
     * Gets multiple items from the cache matching the pattern.
     *
     * @param cacheName
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, 0 );
    }

    /**
     * Gets multiple items from the cache matching the pattern.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized. Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern, requesterId );

        final RemoteCacheResponse<Map<K, ICacheElement<K, V>>> remoteHttpCacheResponse =
            getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        log.debug( "GetMatching [{0}] = {1}", pattern, remoteHttpCacheResponse );

        return remoteHttpCacheResponse.getPayload();
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
        return getMultiple( cacheName, keys, 0 );
    }

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
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys, requesterId );

        final RemoteCacheResponse<Map<K, ICacheElement<K, V>>> remoteHttpCacheResponse =
            getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        log.debug( "GetMultiple [{0}] = {1}", keys, remoteHttpCacheResponse );

        return remoteHttpCacheResponse.getPayload();
    }

    /**
     * @return the remoteDispatcher
     */
    public IRemoteCacheDispatcher getRemoteDispatcher()
    {
        return remoteDispatcher;
    }

    /**
     * @return the remoteHttpCacheAttributes
     */
    public RemoteHttpCacheAttributes getRemoteHttpCacheAttributes()
    {
        return remoteHttpCacheAttributes;
    }

    /**
     * The provides an extension point. If you want to extend this and use a special dispatcher,
     * here is the place to do it.
     *
     * @param attributes
     */
    @Override
    public void initialize( final RemoteHttpCacheAttributes attributes )
    {
        setRemoteDispatcher( new RemoteHttpCacheDispatcher( attributes ) );

        log.info( "Created remote Dispatcher. {0}", this::getRemoteDispatcher);
        setInitialized( true );
    }

    /**
     * Make and alive request.
     *
     * @return true if we make a successful alive request.
     * @throws IOException
     */
    @Override
    public boolean isAlive()
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest = RemoteCacheRequestFactory.createAliveCheckRequest( 0 );
        final RemoteCacheResponse<String> remoteHttpCacheResponse =
            getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( remoteHttpCacheResponse != null )
        {
            return remoteHttpCacheResponse.isSuccess();
        }

        return false;
    }

    /**
     * @return the initialized
     */
    protected boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Frees the specified cache.
     *
     * @throws IOException
     */
    @Override
    public void release()
        throws IOException
    {
        // noop
    }

    /**
     * Removes the given key from the specified cache.
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
     * Removes the given key from the specified cache.
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
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Remove all keys from the specified cache.
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
     * Remove all keys from the specified cache.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName, final long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * @param initialized the initialized to set
     */
    protected void setInitialized( final boolean initialized )
    {
        this.initialized = initialized;
    }

    /**
     * @param remoteDispatcher the remoteDispatcher to set
     */
    public void setRemoteDispatcher( final IRemoteCacheDispatcher remoteDispatcher )
    {
        this.remoteDispatcher = remoteDispatcher;
    }

    /**
     * @param remoteHttpCacheAttributes the remoteHttpCacheAttributes to set
     */
    public void setRemoteHttpCacheAttributes( final RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        this.remoteHttpCacheAttributes = remoteHttpCacheAttributes;
    }

    /**
     * Puts a cache item to the cache.
     *
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
     * Puts a cache item to the cache.
     *
     * @param cacheElement
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> cacheElement, final long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            final String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        final RemoteCacheRequest<K, V> remoteHttpCacheRequest =
            RemoteCacheRequestFactory.createUpdateRequest( cacheElement, requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }
}
