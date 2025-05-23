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

import org.apache.commons.jcs3.auxiliary.remote.RemoteCacheAttributes;

/** HTTP client specific settings. */
public class RemoteHttpCacheAttributes
    extends RemoteCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = -5944327125140505212L;

    /** HTTP version to use. */
    private static final String DEFAULT_HTTP_VERSION = "1.1";

    /** The default class name for the client.  */
    public static final String DEFAULT_REMOTE_HTTP_CLIENT_CLASS_NAME = RemoteHttpCacheClient.class.getName();

    /** The max connections allowed per host */
    private int maxConnectionsPerHost = 100;

    /** The socket timeout. */
    private int socketTimeoutMillis = 3000;

    /** The socket connections timeout */
    private int connectionTimeoutMillis = 5000;

    /** HTTP version to use. */
    private String httpVersion = DEFAULT_HTTP_VERSION;

    /** The cache name will be included on the parameters */
    private boolean includeCacheNameAsParameter = true;

    /** Keys and patterns will be included in the parameters */
    private boolean includeKeysAndPatternsAsParameter = true;

    /** Keys and patterns will be included in the parameters */
    private boolean includeRequestTypeasAsParameter = true;

    /** The complete URL to the service. */
    private String url;

    /** This allows users to inject their own client implementation. */
    private String remoteHttpClientClassName = DEFAULT_REMOTE_HTTP_CLIENT_CLASS_NAME;

    /**
     * @return the connectionTimeoutMillis
     */
    public int getConnectionTimeoutMillis()
    {
        return connectionTimeoutMillis;
    }

    /**
     * @return the httpVersion
     */
    public String getHttpVersion()
    {
        return httpVersion;
    }

    /**
     * @return the maxConnectionsPerHost
     */
    public int getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * @return the remoteHttpClientClassName
     */
    public String getRemoteHttpClientClassName()
    {
        return remoteHttpClientClassName;
    }

    /**
     * @return the socketTimeoutMillis
     */
    public int getSocketTimeoutMillis()
    {
        return socketTimeoutMillis;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return the includeCacheNameInURL
     */
    public boolean isIncludeCacheNameAsParameter()
    {
        return includeCacheNameAsParameter;
    }

    /**
     * @return the includeKeysAndPatternsInURL
     */
    public boolean isIncludeKeysAndPatternsAsParameter()
    {
        return includeKeysAndPatternsAsParameter;
    }

    /**
     * @return the includeRequestTypeasAsParameter
     */
    public boolean isIncludeRequestTypeasAsParameter()
    {
        return includeRequestTypeasAsParameter;
    }

    /**
     * @param connectionTimeoutMillis the connectionTimeoutMillis to set
     */
    public void setConnectionTimeoutMillis( final int connectionTimeoutMillis )
    {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    /**
     * @param httpVersion the httpVersion to set
     */
    public void setHttpVersion( final String httpVersion )
    {
        this.httpVersion = httpVersion;
    }

    /**
     * @param includeCacheNameInURL the includeCacheNameInURL to set
     */
    public void setIncludeCacheNameAsParameter( final boolean includeCacheNameInURL )
    {
        this.includeCacheNameAsParameter = includeCacheNameInURL;
    }

    /**
     * @param includeKeysAndPatternsInURL the includeKeysAndPatternsInURL to set
     */
    public void setIncludeKeysAndPatternsAsParameter( final boolean includeKeysAndPatternsInURL )
    {
        this.includeKeysAndPatternsAsParameter = includeKeysAndPatternsInURL;
    }

    /**
     * @param includeRequestTypeasAsParameter the includeRequestTypeasAsParameter to set
     */
    public void setIncludeRequestTypeasAsParameter( final boolean includeRequestTypeasAsParameter )
    {
        this.includeRequestTypeasAsParameter = includeRequestTypeasAsParameter;
    }

    /**
     * @param maxConnectionsPerHost the maxConnectionsPerHost to set
     */
    public void setMaxConnectionsPerHost( final int maxConnectionsPerHost )
    {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * @param remoteHttpClientClassName the remoteHttpClientClassName to set
     */
    public void setRemoteHttpClientClassName( final String remoteHttpClientClassName )
    {
        this.remoteHttpClientClassName = remoteHttpClientClassName;
    }

    /**
     * @param socketTimeoutMillis the socketTimeoutMillis to set
     */
    public void setSocketTimeoutMillis( final int socketTimeoutMillis )
    {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    /**
     * @param url the url to set
     */
    public void setUrl( final String url )
    {
        this.url = url;
    }

    /**
     * @return String details
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n RemoteHttpCacheAttributes" );
        buf.append( "\n maxConnectionsPerHost = [" + getMaxConnectionsPerHost() + "]" );
        buf.append( "\n socketTimeoutMillis = [" + getSocketTimeoutMillis() + "]" );
        buf.append( "\n httpVersion = [" + getHttpVersion() + "]" );
        buf.append( "\n connectionTimeoutMillis = [" + getConnectionTimeoutMillis() + "]" );
        buf.append( "\n includeCacheNameAsParameter = [" + isIncludeCacheNameAsParameter() + "]" );
        buf.append( "\n includeKeysAndPatternsAsParameter = [" + isIncludeKeysAndPatternsAsParameter() + "]" );
        buf.append( "\n includeRequestTypeasAsParameter = [" + isIncludeRequestTypeasAsParameter() + "]" );
        buf.append( "\n url = [" + getUrl() + "]" );
        buf.append( "\n remoteHttpClientClassName = [" + getRemoteHttpClientClassName() + "]" );
        buf.append( super.toString() );
        return buf.toString();
    }
}
