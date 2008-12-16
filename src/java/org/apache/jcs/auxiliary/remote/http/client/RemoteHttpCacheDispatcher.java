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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.jcs.auxiliary.remote.util.RemoteCacheRequestUtil;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.jcs.utils.serialization.StandardSerializer;

/** Calls the service. */
public class RemoteHttpCacheDispatcher
    extends AbstractHttpClient
    implements IRemoteCacheDispatcher
{
    /** Named of the parameter */
    private static final String PARAMETER_REQUEST_TYPE = "RequestType";

    /** Named of the parameter */
    private static final String PARAMETER_KEY = "Key";

    /** Named of the parameter */
    private static final String PARAMETER_CACHE_NAME = "CacheName";

    /** The Logger. */
    private final static Log log = LogFactory.getLog( RemoteHttpCacheDispatcher.class );

    /** This needs to be standard, since the other side is standard */
    private StandardSerializer serializer = new StandardSerializer();

    /**
     * @param remoteHttpCacheAttributes
     */
    public RemoteHttpCacheDispatcher( RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        super( remoteHttpCacheAttributes );
    }

    /**
     * All requests will go through this method.
     * <p>
     * TODO consider taking in a URL instead of using the one in the configuration.
     * <p>
     * @param remoteCacheRequest
     * @return RemoteCacheResponse
     * @throws IOException
     */
    public RemoteCacheResponse dispatchRequest( RemoteCacheRequest remoteCacheRequest )
        throws IOException
    {
        try
        {
            byte[] requestAsByteArray = serializer.serialize( remoteCacheRequest );

            String url = addParameters( remoteCacheRequest, getRemoteHttpCacheAttributes().getUrl() );

            byte[] responseAsByteArray = processRequest( requestAsByteArray, url );

            RemoteCacheResponse remoteCacheResponse = null;
            try
            {
                remoteCacheResponse = (RemoteCacheResponse) serializer.deSerialize( responseAsByteArray );
            }
            catch ( ClassNotFoundException e )
            {
                log.error( "Couldn't deserialize the response.", e );
            }
            return remoteCacheResponse;
        }
        catch ( Exception e )
        {
            log.error( "Problem dispatching request.", e );
            throw new IOException( e.getMessage() );
        }
    }

    /**
     * @param requestAsByteArray
     * @param url
     * @return byte[] - the response
     * @throws IOException
     * @throws HttpException
     */
    protected byte[] processRequest( byte[] requestAsByteArray, String url )
        throws IOException, HttpException
    {
        PostMethod post = new PostMethod( url );
        RequestEntity requestEntity = new ByteArrayRequestEntity( requestAsByteArray );
        post.setRequestEntity( requestEntity );
        doWebserviceCall( post );
        byte[] response = post.getResponseBody();
        return response;
    }

    /**
     * @param remoteCacheRequest
     * @param baseUrl
     * @return String
     */
    protected String addParameters( RemoteCacheRequest remoteCacheRequest, String baseUrl )
    {
        StringBuffer url = new StringBuffer( baseUrl );

        try
        {
            if ( baseUrl != null && ( baseUrl.indexOf( "?" ) == -1 ) )
            {
                url.append( "?" );
            }

            if ( getRemoteHttpCacheAttributes().isIncludeCacheNameAsParameter() )
            {
                if ( remoteCacheRequest.getCacheName() != null )
                {
                    url.append( PARAMETER_CACHE_NAME + "="
                        + URLEncoder.encode( remoteCacheRequest.getCacheName(), "UTF-8" ) );
                }
            }
            if ( getRemoteHttpCacheAttributes().isIncludeKeysAndPatternsAsParameter() )
            {
                String keyValue = "";
                switch ( remoteCacheRequest.getRequestType() )
                {
                    case RemoteCacheRequest.REQUEST_TYPE_GET:
                        keyValue = remoteCacheRequest.getKey() + "";
                        break;
                    case RemoteCacheRequest.REQUEST_TYPE_REMOVE:
                        keyValue = remoteCacheRequest.getKey() + "";
                        break;
                    case RemoteCacheRequest.REQUEST_TYPE_GET_MATCHING:
                        keyValue = remoteCacheRequest.getPattern();
                        break;
                    case RemoteCacheRequest.REQUEST_TYPE_GET_MULTIPLE:
                        keyValue = remoteCacheRequest.getKeySet() + "";
                        break;
                    case RemoteCacheRequest.REQUEST_TYPE_GET_GROUP_KEYS:
                        keyValue = remoteCacheRequest.getKey() + "";
                        break;
                    case RemoteCacheRequest.REQUEST_TYPE_UPDATE:
                        keyValue = remoteCacheRequest.getCacheElement().getKey() + "";
                        break;
                    default:
                        break;
                }
                String encodedKeyValue = URLEncoder.encode( keyValue, "UTF-8" );
                url.append( "&" + PARAMETER_KEY + "=" + encodedKeyValue );
            }
            if ( getRemoteHttpCacheAttributes().isIncludeRequestTypeasAsParameter() )
            {
                url.append( "&"
                    + PARAMETER_REQUEST_TYPE
                    + "="
                    + URLEncoder.encode( RemoteCacheRequestUtil
                        .getRequestTypeName( remoteCacheRequest.getRequestType() ), "UTF-8" ) );
            }
        }
        catch ( UnsupportedEncodingException e )
        {
            log.error( "Couldn't encode URL.", e );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Url: " + url.toString() );
        }

        return url.toString();
    }

    /**
     * Called before the executeMethod on the client.
     * <p>
     * @param post http method
     * @return HttpState
     * @throws IOException
     */
    public HttpState preProcessWebserviceCall( HttpMethod post )
        throws IOException
    {
        // do nothing. Child can override.
        return null;
    }

    /**
     * Called after the executeMethod on the client.
     * <p>
     * @param post http method
     * @param httpState state
     * @throws IOException
     */
    public void postProcessWebserviceCall( HttpMethod post, HttpState httpState )
        throws IOException
    {
        // do nothing. Child can override.
    }
}
