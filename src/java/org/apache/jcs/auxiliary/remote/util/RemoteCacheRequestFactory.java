package org.apache.jcs.auxiliary.remote.util;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This creates request objects. You could write your own client and use the objects from this
 * factory.
 */
public class RemoteCacheRequestFactory
{
    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteCacheRequestFactory.class );

    /**
     * Creates a get Request.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createGetRequest( String cacheName, Serializable key, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setKey( key );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_GET );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a getMatching Request.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createGetMatchingRequest( String cacheName, String pattern, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setPattern( pattern );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_GET_MATCHING );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a getMultiple Request.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createGetMultipleRequest( String cacheName, Set<Serializable> keys, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setKeySet( keys );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_GET_MULTIPLE );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a remove Request.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createRemoveRequest( String cacheName, Serializable key, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setKey( key );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_REMOVE );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a GetGroupKeys Request.
     * <p>
     * @param cacheName
     * @param groupName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createGetGroupKeysRequest( String cacheName, String groupName, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setKey( groupName );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_GET_GROUP_KEYS );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a removeAll Request.
     * <p>
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createRemoveAllRequest( String cacheName, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_REMOVE_ALL );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates a dispose Request.
     * <p>
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createDisposeRequest( String cacheName, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setCacheName( cacheName );
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_DISPOSE );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates an Update Request.
     * <p>
     * @param cacheElement
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createUpdateRequest( ICacheElement cacheElement, long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
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
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_UPDATE );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }

    /**
     * Creates an alive check Request.
     * <p>
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteCacheRequest createAliveCheckRequest( long requesterId )
    {
        RemoteCacheRequest request = new RemoteCacheRequest();
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_ALIVE_CHECK );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }
}
