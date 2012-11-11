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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createGetRequest( String cacheName, K key, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createGetMatchingRequest( String cacheName, String pattern, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createGetMultipleRequest( String cacheName, Set<K> keys, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createRemoveRequest( String cacheName, K key, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static RemoteCacheRequest<String, String> createGetGroupKeysRequest( String cacheName, String groupName, long requesterId )
    {
        RemoteCacheRequest<String, String> request = new RemoteCacheRequest<String, String>();
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
     * Creates a GetGroupNames Request.
     * <p>
     * @param cacheName
     * @param requesterId
     * @return RemoteHttpCacheRequest
     */
	public static RemoteCacheRequest<String, String> createGetGroupNamesRequest( String cacheName, int requesterId)
	{
	    RemoteCacheRequest<String, String> request = new RemoteCacheRequest<String, String>();
	    request.setCacheName( cacheName );
	    request.setKey( cacheName );
	    request.setRequesterId( requesterId );
	    request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_GET_GROUP_NAMES );

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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createRemoveAllRequest( String cacheName, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createDisposeRequest( String cacheName, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createUpdateRequest( ICacheElement<K, V> cacheElement, long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
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
    public static <K extends Serializable, V extends Serializable> RemoteCacheRequest<K, V> createAliveCheckRequest( long requesterId )
    {
        RemoteCacheRequest<K, V> request = new RemoteCacheRequest<K, V>();
        request.setRequesterId( requesterId );
        request.setRequestType( RemoteCacheRequest.REQUEST_TYPE_ALIVE_CHECK );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Created: " + request );
        }

        return request;
    }


}
