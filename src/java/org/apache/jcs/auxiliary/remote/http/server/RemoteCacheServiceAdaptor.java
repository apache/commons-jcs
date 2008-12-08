package org.apache.jcs.auxiliary.remote.http.server;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheRequest;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheResponse;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * The Servlet deserializes the request object. The request object is passed to the processor. The
 * processor then calls the service which does the work of talking to the cache.
 * <p>
 * This is essentially an adaptor on top of the service.
 */
public class RemoteCacheServiceAdaptor
{
    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteCacheServiceAdaptor.class );

    /** The service that does the work. */
    private IRemoteCacheService remoteCacheService;
    
    /** This is for testing without the factory. */
    protected RemoteCacheServiceAdaptor()
    {
        // for testing.
    }
    
    /**
     * Create a process with a cache manager.
     * <p>
     * @param cacheManager
     */
    public RemoteCacheServiceAdaptor( CompositeCacheManager cacheManager )
    {
        setRemoteCacheService( RemoteHttpCacheSeviceFactory.createRemoteHttpCacheService( cacheManager ) );
    }

    /**
     * Processes the request. It will call the appropriate method on the service
     * <p>
     * @param request
     * @return RemoteHttpCacheResponse, never null
     */
    public RemoteHttpCacheResponse processRequest( RemoteHttpCacheRequest request )
    {
        RemoteHttpCacheResponse response = new RemoteHttpCacheResponse();

        if ( request == null )
        {
            String message = "The request is null.  Cannot process";
            log.warn( message );
            response.setSuccess( false );
            response.setErrorMessage( message );
        }
        else
        {
            try
            {
                switch ( request.getRequestType() )
                {
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_GET:
                        ICacheElement element = getRemoteCacheService().get( request.getCacheName(),
                                                                                 request.getKey(),
                                                                                 request.getRequesterId() );
                        if ( element != null )
                        {
                            response.getPayload().put( element.getKey(), element );
                        }
                        break;
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MULTIPLE:
                        Map elementMap = getRemoteCacheService().getMultiple( request.getCacheName(),
                                                                                  request.getKeySet(),
                                                                                  request.getRequesterId() );
                        if ( elementMap != null )
                        {
                            response.getPayload().putAll( elementMap );
                        }
                        break;
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MATCHING:
                        Map elementMapMatching = getRemoteCacheService().getMatching( request.getCacheName(),
                                                                                          request.getPattern(),
                                                                                          request.getRequesterId() );
                        if ( elementMapMatching != null )
                        {
                            response.getPayload().putAll( elementMapMatching );
                        }
                        break;
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE:
                        getRemoteCacheService().remove( request.getCacheName(), request.getKey(),
                                                            request.getRequesterId() );
                        break;
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE_ALL:
                        getRemoteCacheService().removeAll( request.getCacheName(), request.getRequesterId() );
                        break;
                    case IRemoteHttpCacheConstants.REQUEST_TYPE_UPDATE:
                        getRemoteCacheService().update( request.getCacheElement(), request.getRequesterId() );
                        break;
                    default:
                        String message = "Unknown event type.  Cannot process " + request;
                        log.warn( message );
                        response.setSuccess( false );
                        response.setErrorMessage( message );
                        break;
                }
            }
            catch ( Exception e )
            {
                String message = "Problem processing request. " + request + " Error: " + e.getMessage();
                log.error( message, e );
                response.setSuccess( false );
                response.setErrorMessage( message );
            }
        }

        return response;
    }

    /**
     * @param remoteHttpCacheService the remoteHttpCacheService to set
     */
    public void setRemoteCacheService( IRemoteCacheService remoteHttpCacheService )
    {
        this.remoteCacheService = remoteHttpCacheService;
    }

    /**
     * @return the remoteHttpCacheService
     */
    public IRemoteCacheService getRemoteCacheService()
    {
        return remoteCacheService;
    }
}
