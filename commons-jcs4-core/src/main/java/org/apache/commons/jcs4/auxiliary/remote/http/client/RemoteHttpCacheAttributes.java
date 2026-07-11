package org.apache.commons.jcs4.auxiliary.remote.http.client;

import java.time.Duration;

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

import org.apache.commons.jcs4.auxiliary.remote.RemoteCacheAttributes;

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
    private Duration socketTimeout = Duration.ofSeconds(3);

    /** The socket connections timeout */
    private Duration connectionTimeout = Duration.ofSeconds(5);

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
     * @return The connectionTimeout
     */
    public Duration getConnectionTimeout()
    {
        return connectionTimeout;
    }

    /**
     * @return The httpVersion
     */
    public String getHttpVersion()
    {
        return httpVersion;
    }

    /**
     * @return The maxConnectionsPerHost
     */
    public int getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * @return The remoteHttpClientClassName
     */
    public String getRemoteHttpClientClassName()
    {
        return remoteHttpClientClassName;
    }

    /**
     * @return The socketTimeout
     */
    public Duration getSocketTimeout()
    {
        return socketTimeout;
    }

    /**
     * @return The url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return The includeCacheNameInURL
     */
    public boolean isIncludeCacheNameAsParameter()
    {
        return includeCacheNameAsParameter;
    }

    /**
     * @return The includeKeysAndPatternsInURL
     */
    public boolean isIncludeKeysAndPatternsAsParameter()
    {
        return includeKeysAndPatternsAsParameter;
    }

    /**
     * @return The includeRequestTypeasAsParameter
     */
    public boolean isIncludeRequestTypeasAsParameter()
    {
        return includeRequestTypeasAsParameter;
    }

    /**
     * @param connectionTimeout The connectionTimeout to set
     */
    public void setConnectionTimeout( final Duration connectionTimeout )
    {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @param httpVersion The httpVersion to set
     */
    public void setHttpVersion( final String httpVersion )
    {
        this.httpVersion = httpVersion;
    }

    /**
     * @param includeCacheNameInURL The includeCacheNameInURL to set
     */
    public void setIncludeCacheNameAsParameter( final boolean includeCacheNameInURL )
    {
        this.includeCacheNameAsParameter = includeCacheNameInURL;
    }

    /**
     * @param includeKeysAndPatternsInURL The includeKeysAndPatternsInURL to set
     */
    public void setIncludeKeysAndPatternsAsParameter( final boolean includeKeysAndPatternsInURL )
    {
        this.includeKeysAndPatternsAsParameter = includeKeysAndPatternsInURL;
    }

    /**
     * @param includeRequestTypeasAsParameter The includeRequestTypeasAsParameter to set
     */
    public void setIncludeRequestTypeasAsParameter( final boolean includeRequestTypeasAsParameter )
    {
        this.includeRequestTypeasAsParameter = includeRequestTypeasAsParameter;
    }

    /**
     * @param maxConnectionsPerHost The maxConnectionsPerHost to set
     */
    public void setMaxConnectionsPerHost( final int maxConnectionsPerHost )
    {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * @param remoteHttpClientClassName The remoteHttpClientClassName to set
     */
    public void setRemoteHttpClientClassName( final String remoteHttpClientClassName )
    {
        this.remoteHttpClientClassName = remoteHttpClientClassName;
    }

    /**
     * @param socketTimeout The socketTimeout to set
     */
    public void setSocketTimeout( final Duration socketTimeout )
    {
        this.socketTimeout = socketTimeout;
    }

    /**
     * @param url The url to set
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
        buf.append( "\n socketTimeout = [" + getSocketTimeout() + "]" );
        buf.append( "\n httpVersion = [" + getHttpVersion() + "]" );
        buf.append( "\n connectionTimeout = [" + getConnectionTimeout() + "]" );
        buf.append( "\n includeCacheNameAsParameter = [" + isIncludeCacheNameAsParameter() + "]" );
        buf.append( "\n includeKeysAndPatternsAsParameter = [" + isIncludeKeysAndPatternsAsParameter() + "]" );
        buf.append( "\n includeRequestTypeasAsParameter = [" + isIncludeRequestTypeasAsParameter() + "]" );
        buf.append( "\n url = [" + getUrl() + "]" );
        buf.append( "\n remoteHttpClientClassName = [" + getRemoteHttpClientClassName() + "]" );
        buf.append( super.toString() );
        return buf.toString();
    }
}
