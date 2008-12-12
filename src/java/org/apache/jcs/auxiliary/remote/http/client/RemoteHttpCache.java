package org.apache.jcs.auxiliary.remote.http.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.AbstractRemoteAuxiliaryCache;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheService;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

/**
 * This uses an http client as the service.
 */
public class RemoteHttpCache
    extends AbstractRemoteAuxiliaryCache
{
    /** Don't change. */
    private static final long serialVersionUID = -5329231850422826461L;

    /** The logger. */
    private final static Log log = LogFactory.getLog( RemoteHttpCache.class );

    /** Keep the child copy here for the restore process. */
    private RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /**
     * Constructor for the RemoteCache object. This object communicates with a remote cache server.
     * One of these exists for each region. This also holds a reference to a listener. The same
     * listener is used for all regions for one remote server. Holding a reference to the listener
     * allows this object to know the listener id assigned by the remote cache.
     * <p>
     * @param remoteHttpCacheAttributes
     * @param remote
     * @param listener
     */
    public RemoteHttpCache( RemoteHttpCacheAttributes remoteHttpCacheAttributes, IRemoteCacheService remote,
                            IRemoteCacheListener listener )
    {
        super( remoteHttpCacheAttributes, remote, listener );

        setRemoteHttpCacheAttributes( remoteHttpCacheAttributes );
    }

    /**
     * Nothing right now. This should setup a zombie and initiate recovery.
     * <p>
     * @param ex
     * @param msg
     * @param eventName
     * @throws IOException
     */
    protected void handleException( Exception ex, String msg, String eventName )
        throws IOException
    {
        // we should not switch if the existing is a zombie.
        if ( !( getRemoteCacheService() instanceof ZombieRemoteCacheService ) )
        {
            String message = "Disabling remote cache due to error: " + msg;
            logError( cacheName, "", message );
            log.error( message, ex );

            setRemoteCacheService( new ZombieRemoteCacheService( getRemoteCacheAttributes().getZombieQueueMaxSize() ) );

            RemoteHttpCacheMonitor.getInstance().notifyError( this );
        }

        if ( ex instanceof IOException )
        {
            throw (IOException) ex;
        }
        throw new IOException( ex.getMessage() );
    }

    /**
     * @return url of service
     */
    public String getEventLoggingExtraInfo()
    {
        return null;
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
