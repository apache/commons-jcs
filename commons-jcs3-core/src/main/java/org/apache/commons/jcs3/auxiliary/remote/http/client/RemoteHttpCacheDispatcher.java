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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheDispatcher;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;

/** Calls the service. */
public class RemoteHttpCacheDispatcher
    extends AbstractHttpClient
    implements IRemoteCacheDispatcher
{
    /** Parameter encoding */
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    /** Named of the parameter */
    private static final String PARAMETER_REQUEST_TYPE = "RequestType";

    /** Named of the parameter */
    private static final String PARAMETER_KEY = "Key";

    /** Named of the parameter */
    private static final String PARAMETER_CACHE_NAME = "CacheName";

    /** The Logger. */
    private static final Log log = Log.getLog( RemoteHttpCacheDispatcher.class );

    /** This needs to be standard, since the other side is standard */
    private final StandardSerializer serializer = new StandardSerializer();

    /**
     * @param remoteHttpCacheAttributes
     */
    public RemoteHttpCacheDispatcher( final RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        super( remoteHttpCacheAttributes );
    }

    /**
     * All requests will go through this method.
     * <p>
     * TODO consider taking in a URL instead of using the one in the configuration.
     *
     * @param remoteCacheRequest
     * @return RemoteCacheResponse
     * @throws IOException
     */
    @Override
    public <K, V, T>
        RemoteCacheResponse<T> dispatchRequest( final RemoteCacheRequest<K, V> remoteCacheRequest )
        throws IOException
    {
        try
        {
            final byte[] requestAsByteArray = serializer.serialize( remoteCacheRequest );

            final byte[] responseAsByteArray = processRequest( requestAsByteArray,
                    remoteCacheRequest,
                    getRemoteHttpCacheAttributes().getUrl());

            RemoteCacheResponse<T> remoteCacheResponse = null;
            try
            {
                remoteCacheResponse = serializer.deSerialize( responseAsByteArray, null );
            }
            catch ( final ClassNotFoundException e )
            {
                log.error( "Couldn't deserialize the response.", e );
            }
            return remoteCacheResponse;
        }
        catch ( final Exception e )
        {
            throw new IOException("Problem dispatching request.", e);
        }
    }

    /**
     * Called after the execute call on the client.
     *
     * @param request http request
     * @param httpState result of execution
     * @throws IOException
     */
    @Override
    protected void postProcessWebserviceCall( final HttpUriRequest request, final HttpResponse httpState )
        throws IOException
    {
        // do nothing. Child can override.
    }

    /**
     * Called before the execute call on the client.
     *
     * @param requestBuilder http method request builder
     * @throws IOException
     */
    @Override
    protected void preProcessWebserviceCall( final RequestBuilder requestBuilder )
        throws IOException
    {
        // do nothing. Child can override.
    }

    /**
     * Process single request
     *
     * @param requestAsByteArray request body
     * @param remoteCacheRequest the cache request
     * @param url target url
     * @return byte[] - the response
     * @throws IOException
     * @throws HttpException
     */
    protected <K, V> byte[] processRequest( final byte[] requestAsByteArray,
            final RemoteCacheRequest<K, V> remoteCacheRequest, final String url )
        throws IOException, HttpException
    {
        final RequestBuilder builder = RequestBuilder.post( url ).setCharset( DEFAULT_ENCODING );

        if ( getRemoteHttpCacheAttributes().isIncludeCacheNameAsParameter()
            && remoteCacheRequest.getCacheName() != null )
        {
            builder.addParameter( PARAMETER_CACHE_NAME, remoteCacheRequest.getCacheName() );
        }
        if ( getRemoteHttpCacheAttributes().isIncludeKeysAndPatternsAsParameter() )
        {
            String keyValue = "";
            switch ( remoteCacheRequest.getRequestType() )
            {
                case GET:
                case REMOVE:
                case GET_KEYSET:
                    keyValue = remoteCacheRequest.getKey().toString();
                    break;
                case GET_MATCHING:
                    keyValue = remoteCacheRequest.getPattern();
                    break;
                case GET_MULTIPLE:
                    keyValue = remoteCacheRequest.getKeySet().toString();
                    break;
                case UPDATE:
                    keyValue = remoteCacheRequest.getCacheElement().getKey().toString();
                    break;
                default:
                    break;
            }
            builder.addParameter( PARAMETER_KEY, keyValue );
        }
        if ( getRemoteHttpCacheAttributes().isIncludeRequestTypeasAsParameter() )
        {
            builder.addParameter( PARAMETER_REQUEST_TYPE,
                remoteCacheRequest.getRequestType().toString() );
        }

        builder.setEntity(new ByteArrayEntity( requestAsByteArray ));
        final HttpResponse httpResponse = doWebserviceCall( builder );
        return EntityUtils.toByteArray( httpResponse.getEntity() );
    }
}
