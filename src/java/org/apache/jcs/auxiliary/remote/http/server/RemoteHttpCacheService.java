package org.apache.jcs.auxiliary.remote.http.server;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.logging.behavior.ICacheEventLogger;

/**
 * This does the work. It's called by the processor. The base class wraps the processing calls in
 * event logs, if an event logger is present.
 * <p>
 * For now we assume that all clients are non-cluster clients. And listener notification is not
 * supported.
 */
public class RemoteHttpCacheService
    extends AbstractRemoteCacheService
{
    /** The name used in the event logs. */
    private static final String EVENT_LOG_SOURCE_NAME = "RemoteHttpCacheServer";

    /** The configuration */
    private RemoteHttpCacheServerAttributes remoteHttpCacheServerAttributes;

    /**
     * Create a process with a cache manager.
     * <p>
     * @param cacheManager
     * @param remoteHttpCacheServerAttributes
     * @param cacheEventLogger
     */
    public RemoteHttpCacheService( ICompositeCacheManager cacheManager,
                                   RemoteHttpCacheServerAttributes remoteHttpCacheServerAttributes,
                                   ICacheEventLogger cacheEventLogger )
    {
        super( cacheManager, cacheEventLogger );
        setEventLogSourceName( EVENT_LOG_SOURCE_NAME );
        this.remoteHttpCacheServerAttributes = remoteHttpCacheServerAttributes;
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request or non-remote origins.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    public ICacheElement processGet( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGet( key );
        }
        else
        {
            return cache.get( key );
        }
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return Map
     * @throws IOException
     */
    public Map processGetMultiple( String cacheName, Set keys, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGetMultiple( keys );
        }
        else
        {
            return cache.getMultiple( keys );
        }
    }

    /**
     * Processes a get request.
     * <p>
     * If isAllowClusterGet is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map
     * @throws IOException
     */
    public Map processGetMatching( String cacheName, String pattern, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isAllowClusterGet();
        if ( keepLocal )
        {
            return cache.localGetMatching( pattern );
        }
        else
        {
            return cache.getMatching( pattern );
        }
    }

    /**
     * Processes an update request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param item
     * @param requesterId
     * @throws IOException
     */
    public void processUpdate( ICacheElement item, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( item.getCacheName() );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localUpdate( item );
        }
        else
        {
            cache.update( item );
        }
    }

    /**
     * Processes a remove request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void processRemove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localRemove( key );
        }
        else
        {
            cache.remove( key );
        }
    }

    /**
     * Processes a removeAll request.
     * <p>
     * If isLocalClusterConsistency is enabled we will treat this as a normal request of non-remote
     * origination.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void processRemoveAll( String cacheName, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );

        boolean keepLocal = !remoteHttpCacheServerAttributes.isLocalClusterConsistency();
        if ( keepLocal )
        {
            cache.localRemoveAll();
        }
        else
        {
            cache.removeAll();
        }
    }

    /**
     * Processes a shutdown request.
     * <p>
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void processDispose( String cacheName, long requesterId )
        throws IOException
    {
        CompositeCache cache = (CompositeCache) getCacheManager().getCache( cacheName );
        cache.dispose();
    }

    /**
     * This general method should be deprecated.
     * <p>
     * @throws IOException
     */
    public void release()
        throws IOException
    {
        //nothing.
    }

    /**
     * This is called by the event log.
     * <p>
     * @param requesterId
     * @return requesterId + ""
     */
    protected String getExtraInfoForRequesterId( long requesterId )
    {
        return requesterId + "";
    }
}
