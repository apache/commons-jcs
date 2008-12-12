package org.apache.jcs.auxiliary.remote.http.client;

import org.apache.jcs.auxiliary.remote.RemoteCacheAttributes;

/** Http client specific settings. */
public class RemoteHttpCacheAttributes
    extends RemoteCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = -5944327125140505212L;

    /** http verison to use. */
    private static final String DEFAULT_HTTP_VERSION = "1.0";

    /** The max connections allowed per host */
    private int maxConnectionsPerHost = 100;

    /** The socket timeout. */
    private int socketTimeoutMillis = 3000;

    /** The socket connections timeout */
    private int connectionTimeoutMillis = 5000;

    /** http verison to use. */
    private String httpVersion = DEFAULT_HTTP_VERSION;

    /** The cache name will be included on the parameters */
    private boolean includeCacheNameAsParameter = true;

    /** keys and patterns will be included in the parameters */
    private boolean includeKeysAndPatternsAsParameter = true;

    /** keys and patterns will be included in the parameters */
    private boolean includeRequestTypeasAsParameter = true;
    
    /** The complete URL to the service. */
    private String url;

    /**
     * @param maxConnectionsPerHost the maxConnectionsPerHost to set
     */
    public void setMaxConnectionsPerHost( int maxConnectionsPerHost )
    {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    /**
     * @return the maxConnectionsPerHost
     */
    public int getMaxConnectionsPerHost()
    {
        return maxConnectionsPerHost;
    }

    /**
     * @param socketTimeoutMillis the socketTimeoutMillis to set
     */
    public void setSocketTimeoutMillis( int socketTimeoutMillis )
    {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    /**
     * @return the socketTimeoutMillis
     */
    public int getSocketTimeoutMillis()
    {
        return socketTimeoutMillis;
    }

    /**
     * @param httpVersion the httpVersion to set
     */
    public void setHttpVersion( String httpVersion )
    {
        this.httpVersion = httpVersion;
    }

    /**
     * @return the httpVersion
     */
    public String getHttpVersion()
    {
        return httpVersion;
    }

    /**
     * @param connectionTimeoutMillis the connectionTimeoutMillis to set
     */
    public void setConnectionTimeoutMillis( int connectionTimeoutMillis )
    {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    /**
     * @return the connectionTimeoutMillis
     */
    public int getConnectionTimeoutMillis()
    {
        return connectionTimeoutMillis;
    }

    /**
     * @param includeCacheNameInURL the includeCacheNameInURL to set
     */
    public void setIncludeCacheNameAsParameter( boolean includeCacheNameInURL )
    {
        this.includeCacheNameAsParameter = includeCacheNameInURL;
    }

    /**
     * @return the includeCacheNameInURL
     */
    public boolean isIncludeCacheNameAsParameter()
    {
        return includeCacheNameAsParameter;
    }

    /**
     * @param includeKeysAndPatternsInURL the includeKeysAndPatternsInURL to set
     */
    public void setIncludeKeysAndPatternsAsParameter( boolean includeKeysAndPatternsInURL )
    {
        this.includeKeysAndPatternsAsParameter = includeKeysAndPatternsInURL;
    }

    /**
     * @return the includeKeysAndPatternsInURL
     */
    public boolean isIncludeKeysAndPatternsAsParameter()
    {
        return includeKeysAndPatternsAsParameter;
    }

    /**
     * @param includeRequestTypeasAsParameter the includeRequestTypeasAsParameter to set
     */
    public void setIncludeRequestTypeasAsParameter( boolean includeRequestTypeasAsParameter )
    {
        this.includeRequestTypeasAsParameter = includeRequestTypeasAsParameter;
    }

    /**
     * @return the includeRequestTypeasAsParameter
     */
    public boolean isIncludeRequestTypeasAsParameter()
    {
        return includeRequestTypeasAsParameter;
    }

    /**
     * @param url the url to set
     */
    public void setUrl( String url )
    {
        this.url = url;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @return String details
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n RemoteHttpCacheAttributes" );
        buf.append( "\n maxConnectionsPerHost = [" + getMaxConnectionsPerHost() + "]" );
        buf.append( "\n socketTimeoutMillis = [" + getSocketTimeoutMillis() + "]" );
        buf.append( "\n httpVersion = [" + getHttpVersion() + "]" );
        buf.append( "\n connectionTimeoutMillis = [" + getConnectionTimeoutMillis() + "]" );
        buf.append( "\n includeCacheNameAsParameter = [" + isIncludeCacheNameAsParameter() + "]" );
        buf.append( "\n includeKeysAndPatternsAsParameter = [" + isIncludeKeysAndPatternsAsParameter() + "]" );
        buf.append( "\n includeRequestTypeasAsParameter = [" + isIncludeRequestTypeasAsParameter() + "]" );
        buf.append( "\n url = [" + getUrl() + "]" );
        buf.append( super.toString() );
        return buf.toString();
    }
}
