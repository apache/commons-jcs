package org.apache.jcs.auxiliary.remote.http.client;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheRequest;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * This creates request objects. You could write your own client and use the objects from this
 * factory.
 */
public class RemoteHttpClientRequestFactory
{
    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteHttpClientRequestFactory.class );
    
    /**
     * Creates a get Request.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
    public static RemoteHttpCacheRequest createGetRequest( String cacheName, Serializable key, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
        request.setCacheName( cacheName );
        request.setKey( key );
        request.setRequesterId( requesterId );
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_GET );

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
    public static RemoteHttpCacheRequest createGetMatchingRequest( String cacheName, String pattern, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
        request.setCacheName( cacheName );
        request.setPattern( pattern );
        request.setRequesterId( requesterId );
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MATCHING );

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
    public static RemoteHttpCacheRequest createGetMultipleRequest( String cacheName, Set keys, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
        request.setCacheName( cacheName );
        request.setKeySet( keys );
        request.setRequesterId( requesterId );
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MULTIPLE );

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
    public static RemoteHttpCacheRequest createRemoveRequest( String cacheName, Serializable key, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
        request.setCacheName( cacheName );
        request.setKey( key );
        request.setRequesterId( requesterId );
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE );

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
    public static RemoteHttpCacheRequest createRemoveAllRequest( String cacheName, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
        request.setCacheName( cacheName );
        request.setRequesterId( requesterId );
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE_ALL );

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
    public static RemoteHttpCacheRequest createUpdateRequest( ICacheElement cacheElement, long requesterId )
    {
        RemoteHttpCacheRequest request = new RemoteHttpCacheRequest();
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
        request.setRequestType( IRemoteHttpCacheConstants.REQUEST_TYPE_UPDATE );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }
}
