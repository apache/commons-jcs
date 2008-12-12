package org.apache.jcs.auxiliary.remote.http.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class simply configures the http multithreaded connection manager.
 * <p>
 * This class does common functions required by basic WSRequestDispatchers such as loading the
 * properties files.
 * <p>
 * This is abstract because it can't do anything. It used to require the reload properties method to
 * be implemented. I was able to get a default implementation of that method here as well. Child
 * classes can overwrite whatever they want.
 */
public abstract class AbstractHttpClient
{
    /** The connection manager. */
    private MultiThreadedHttpConnectionManager connectionManager;

    /** The client */
    private HttpClient httpClient;

    /** Configuration settings. */
    private RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /** The Logger. */
    private final static Log log = LogFactory.getLog( AbstractHttpClient.class );

    /**
     * Sets the default Properties File and Heading, and creates the HttpClient and connection
     * manager.
     * <p>
     * @param remoteHttpCacheAttributes
     */
    public AbstractHttpClient( RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        setRemoteHttpCacheAttributes( remoteHttpCacheAttributes );
        setConnectionManager( new MultiThreadedHttpConnectionManager() );

        // THIS IS NOT THREAD SAFE:
        // setHttpClient( new HttpClient() );
        // THIS IS:
        setHttpClient( new HttpClient( getConnectionManager() ) );

        configureClient();
    }

    /**
     * Configures the http client.
     */
    public void configureClient()
    {
        if ( getRemoteHttpCacheAttributes().getMaxConnectionsPerHost() > 0 )
        {
            getConnectionManager().getParams().setMaxTotalConnections(
                                                                       getRemoteHttpCacheAttributes()
                                                                           .getMaxConnectionsPerHost() );
        }

        getConnectionManager().getParams().setSoTimeout( getRemoteHttpCacheAttributes().getSocketTimeoutMillis() );

        String httpVersion = getRemoteHttpCacheAttributes().getHttpVersion();
        if ( httpVersion != null )
        {
            if ( "1.1".equals( httpVersion ) )
            {
                getHttpClient().getParams().setParameter( "http.protocol.version", HttpVersion.HTTP_1_1 );
            }
            else if ( "1.0".equals( httpVersion ) )
            {
                getHttpClient().getParams().setParameter( "http.protocol.version", HttpVersion.HTTP_1_0 );
            }
            else
            {
                log.warn( "Unrecognized value for 'httpVersion': [" + httpVersion + "]" );
            }
        }

        getConnectionManager().getParams()
            .setConnectionTimeout( getRemoteHttpCacheAttributes().getConnectionTimeoutMillis() );

        // By default we instruct HttpClient to ignore cookies.
        String cookiePolicy = CookiePolicy.IGNORE_COOKIES;
        getHttpClient().getParams().setCookiePolicy( cookiePolicy );
    }

    /**
     * @return Returns the httpClient.
     */
    public HttpClient getHttpClient()
    {
        return httpClient;
    }

    /**
     * @param httpClient The httpClient to set.
     */
    public void setHttpClient( HttpClient httpClient )
    {
        this.httpClient = httpClient;
    }

    /**
     * @return Returns the connectionManager.
     */
    public MultiThreadedHttpConnectionManager getConnectionManager()
    {
        return connectionManager;
    }

    /**
     * @param connectionManager The connectionManager to set.
     */
    public void setConnectionManager( MultiThreadedHttpConnectionManager connectionManager )
    {
        this.connectionManager = connectionManager;
    }

    /**
     * @param remoteHttpCacheAttributes the remoteHttpCacheAttributes to set
     */
    public void setRemoteHttpCacheAttributes( RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        this.remoteHttpCacheAttributes = remoteHttpCacheAttributes;
    }

    /**
     * @return the remoteHttpCacheAttributes
     */
    public RemoteHttpCacheAttributes getRemoteHttpCacheAttributes()
    {
        return remoteHttpCacheAttributes;
    }
}
