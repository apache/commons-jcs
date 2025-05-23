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

import org.apache.commons.jcs3.log.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * This class simply configures the http multithreaded connection manager.
 * <p>
 * This is abstract because it can do anything. Child classes can overwrite whatever they want.
 */
public abstract class AbstractHttpClient
{
    /** The Logger. */
    private static final Log log = Log.getLog( AbstractHttpClient.class );

    /** The client */
    private final HttpClient httpClient;

    /** The protocol version */
    private final HttpVersion httpVersion;

    /** Configuration settings. */
    private final RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /**
     * Sets the default Properties File and Heading, and creates the HttpClient and connection
     * manager.
     *
     * @param remoteHttpCacheAttributes
     */
    public AbstractHttpClient( final RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        this.remoteHttpCacheAttributes = remoteHttpCacheAttributes;

        final String httpVersion = getRemoteHttpCacheAttributes().getHttpVersion();
        if ( "1.1".equals( httpVersion ) )
        {
            this.httpVersion = HttpVersion.HTTP_1_1;
        }
        else if ( "1.0".equals( httpVersion ) )
        {
            this.httpVersion = HttpVersion.HTTP_1_0;
        }
        else
        {
            log.warn( "Unrecognized value for 'httpVersion': [{0}], defaulting to 1.1",
                    httpVersion );
            this.httpVersion = HttpVersion.HTTP_1_1;
        }

        final HttpClientBuilder builder = HttpClientBuilder.create();
        configureClient(builder);
        this.httpClient = builder.build();
    }

    /**
     * Configures the http client.
     *
     * @param builder client builder to configure
     */
    protected void configureClient(final HttpClientBuilder builder)
    {
        if ( getRemoteHttpCacheAttributes().getMaxConnectionsPerHost() > 0 )
        {
            builder.setMaxConnTotal(getRemoteHttpCacheAttributes().getMaxConnectionsPerHost());
            builder.setMaxConnPerRoute(getRemoteHttpCacheAttributes().getMaxConnectionsPerHost());
        }

        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(getRemoteHttpCacheAttributes().getConnectionTimeoutMillis())
                .setSocketTimeout(getRemoteHttpCacheAttributes().getSocketTimeoutMillis())
                // By default we instruct HttpClient to ignore cookies.
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build());
    }

    /**
     * Execute the web service call
     *
     * @param builder builder for the post request
     * @return the call response
     * @throws IOException on i/o error
     */
    protected final HttpResponse doWebserviceCall( final RequestBuilder builder )
        throws IOException
    {
        preProcessWebserviceCall( builder.setVersion(httpVersion) );
        final HttpUriRequest request = builder.build();
        final HttpResponse httpResponse = this.httpClient.execute( request );
        postProcessWebserviceCall( request, httpResponse );

        return httpResponse;
    }

    /**
     * @return the remoteHttpCacheAttributes
     */
    protected RemoteHttpCacheAttributes getRemoteHttpCacheAttributes()
    {
        return remoteHttpCacheAttributes;
    }

    /**
     * Called after the execute call on the client.
     *
     * @param request http request
     * @param httpState result of execution
     * @throws IOException
     */
    protected abstract void postProcessWebserviceCall( HttpUriRequest request, HttpResponse httpState )
        throws IOException;

    /**
     * Called before the execute call on the client.
     *
     * @param requestBuilder http method request builder
     * @throws IOException
     */
    protected abstract void preProcessWebserviceCall( RequestBuilder requestBuilder )
        throws IOException;
}
