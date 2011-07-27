package org.apache.jcs.auxiliary.remote.http.client;

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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.jcs.auxiliary.remote.http.client.behavior.IRemoteHttpCacheClient;
import org.apache.jcs.auxiliary.remote.http.server.RemoteCacheServiceAdaptor;
import org.apache.jcs.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.jcs.engine.behavior.ICacheElement;

/** This is the service used by the remote http auxiliary cache. */
public class RemoteHttpCacheClient
    implements IRemoteHttpCacheClient
{
    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteCacheServiceAdaptor.class );

    /** The internal client. */
    private IRemoteCacheDispatcher remoteDispatcher;

    /** The remote attributes */
    private RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /** Set to true when initialize is called */
    private boolean initialized = false;

    /** For factory construction. */
    public RemoteHttpCacheClient()
    {
        // does nothing
    }

    /**
     * Constructs a client.
     * <p>
     * @param attributes
     */
    public RemoteHttpCacheClient( RemoteHttpCacheAttributes attributes )
    {
        setRemoteHttpCacheAttributes( attributes );
        initialize( attributes );
    }

    /**
     * The provides an extension point. If you want to extend this and use a special dispatcher,
     * here is the place to do it.
     * <p>
     * @param attributes
     */
    public void initialize( RemoteHttpCacheAttributes attributes )
    {
        setRemoteDispatcher( new RemoteHttpCacheDispatcher( attributes ) );

        if ( log.isInfoEnabled() )
        {
            log.info( "Created remote Dispatcher." + getRemoteDispatcher() );
        }
        setInitialized( true );
    }

    /**
     * Create a request, process, extract the payload.
     * <p>
     * @param cacheName
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        return get( cacheName, key, 0 );
    }

    /**
     * Create a request, process, extract the payload.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }
        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createGetRequest( cacheName, key,
                                                                                                requesterId );

        RemoteCacheResponse remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Get [" + key + "] = " + remoteHttpCacheResponse );
        }

        ICacheElement retval = null;
        if ( remoteHttpCacheResponse != null && remoteHttpCacheResponse.getPayload() != null )
        {
            retval = (ICacheElement)remoteHttpCacheResponse.getPayload().get( key );
        }
        return retval;
    }

    /**
     * Gets multiple items from the cache matching the pattern.
     * <p>
     * @param cacheName
     * @param pattern
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    public Map<Serializable, ICacheElement> getMatching( String cacheName, String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, 0 );
    }

    /**
     * Gets multiple items from the cache matching the pattern.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache matching the pattern.
     * @throws IOException
     */
    public Map<Serializable, ICacheElement> getMatching( String cacheName, String pattern, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName,
                                                                                                        pattern,
                                                                                                        requesterId );

        RemoteCacheResponse remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( log.isDebugEnabled() )
        {
            log.debug( "GetMatching [" + pattern + "] = " + remoteHttpCacheResponse );
        }

        return (Map)remoteHttpCacheResponse.getPayload();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param cacheName
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    public Map<Serializable, ICacheElement> getMultiple( String cacheName, Set<Serializable> keys )
        throws IOException
    {
        return getMultiple( cacheName, keys, 0 );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    public Map<Serializable, ICacheElement> getMultiple( String cacheName, Set<Serializable> keys, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName,
                                                                                                        keys,
                                                                                                        requesterId );

        RemoteCacheResponse remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( log.isDebugEnabled() )
        {
            log.debug( "GetMultiple [" + keys + "] = " + remoteHttpCacheResponse );
        }

        return (Map)remoteHttpCacheResponse.getPayload();
    }

    /**
     * Removes the given key from the specified cache.
     * <p>
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, 0 );
    }

    /**
     * Removes the given key from the specified cache.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createRemoveRequest( cacheName, key,
                                                                                                   requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Remove all keys from the specified cache.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, 0 );
    }

    /**
     * Remove all keys from the sepcified cache.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName,
                                                                                                      requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Puts a cache item to the cache.
     * <p>
     * @param item
     * @throws IOException
     */
    public void update( ICacheElement item )
        throws IOException
    {
        update( item, 0 );
    }

    /**
     * Puts a cache item to the cache.
     * <p>
     * @param cacheElement
     * @param requesterId
     * @throws IOException
     */
    public void update( ICacheElement cacheElement, long requesterId )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createUpdateRequest( cacheElement,
                                                                                                   requesterId );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Frees the specified cache.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    public void dispose( String cacheName )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createDisposeRequest( cacheName, 0 );

        getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );
    }

    /**
     * Frees the specified cache.
     * <p>
     * @throws IOException
     */
    public void release()
        throws IOException
    {
        // noop
    }

    /**
     * @param cacheName
     * @param groupName
     * @return A Set of keys
     * @throws IOException
     */
    public Set<Serializable> getGroupKeys( String cacheName, String groupName )
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createGetGroupKeysRequest( cacheName,
                                                                                                         groupName, 0 );

        RemoteCacheResponse remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( remoteHttpCacheResponse != null && remoteHttpCacheResponse.getPayload() != null )
        {
            return (Set<Serializable>)remoteHttpCacheResponse.getPayload().get( groupName );
        }

        return Collections.emptySet();
    }

    /**
     * Make and alive request.
     * <p>
     * @return true if we make a successful alive request.
     * @throws IOException
     */
    public boolean isAlive()
        throws IOException
    {
        if ( !isInitialized() )
        {
            String message = "The Remote Http Client is not initialized.  Cannot process request.";
            log.warn( message );
            throw new IOException( message );
        }

        RemoteCacheRequest remoteHttpCacheRequest = RemoteCacheRequestFactory.createAliveCheckRequest( 0 );
        RemoteCacheResponse remoteHttpCacheResponse = getRemoteDispatcher().dispatchRequest( remoteHttpCacheRequest );

        if ( remoteHttpCacheResponse != null )
        {
            return remoteHttpCacheResponse.isSuccess();
        }

        return false;
    }

    /**
     * @param remoteDispatcher the remoteDispatcher to set
     */
    public void setRemoteDispatcher( IRemoteCacheDispatcher remoteDispatcher )
    {
        this.remoteDispatcher = remoteDispatcher;
    }

    /**
     * @return the remoteDispatcher
     */
    public IRemoteCacheDispatcher getRemoteDispatcher()
    {
        return remoteDispatcher;
    }

    /**
     * @param remoteHttpCacheAttributes the remoteHttpCacheAttributes to set
     */
    public void setRemoteHttpCacheAttributes( RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        this.remoteHttpCacheAttributes = remoteHttpCacheAttributes;
    }

    /**
     * @return the remoteHttpCacheAttributes
     */
    public RemoteHttpCacheAttributes getRemoteHttpCacheAttributes()
    {
        return remoteHttpCacheAttributes;
    }

    /**
     * @param initialized the initialized to set
     */
    protected void setInitialized( boolean initialized )
    {
        this.initialized = initialized;
    }

    /**
     * @return the initialized
     */
    protected boolean isInitialized()
    {
        return initialized;
    }
}
