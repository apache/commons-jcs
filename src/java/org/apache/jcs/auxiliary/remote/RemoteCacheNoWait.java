package org.apache.jcs.auxiliary.remote;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.rmi.UnmarshalException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheType;

/**
 * Used to queue up update requests to the underlying cache. These requests will
 * be processed in their order of arrival via the cache event queue processor.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheNoWait implements AuxiliaryCache
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheNoWait.class );

    private final RemoteCache cache;
    private ICacheEventQueue q;

    /**
     * Constructs with the given remote cache, and fires up an event queue for
     * aysnchronous processing.
     *
     * @param cache
     */
    public RemoteCacheNoWait( RemoteCache cache )
    {
        this.cache = cache;
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), RemoteCacheInfo.listenerId, cache.getCacheName() );
        if ( cache.getStatus() == CacheConstants.STATUS_ERROR )
        {
            q.destroy();
        }
    }

    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        try
        {
            q.addPutEvent( ce );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
            throw ex;
        }
    }

    /** Synchronously reads from the remote cache. */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        try
        {
            return cache.get( key );
        }
        catch ( UnmarshalException ue )
        {
            log.debug( "Retrying the get owing to UnmarshalException..." );
            try
            {
                return cache.get( key );
            }
            catch ( IOException ex )
            {
                log.debug( "Failed in retrying the get for the second time." );
                q.destroy();
            }
        }
        catch ( IOException ex )
        {
            q.destroy();
            throw ex;
        }
        return null;
    }

    public Set getGroupKeys(String groupName)
        throws IOException
    {
        return cache.getGroupKeys(groupName);
    }

    /** Adds a remove request to the remote cache. */
    public boolean remove( Serializable key )
        throws IOException
    {
        try
        {
            q.addRemoveEvent( key );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
            throw ex;
        }
        return false;
    }

    /** Adds a removeAll request to the remote cache. */
    public void removeAll()
        throws IOException
    {
        try
        {
            q.addRemoveAllEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
            throw ex;
        }
    }

    /** Adds a dispose request to the remote cache. */
    public void dispose()
    {
        try
        {
            q.addDisposeEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
    }

    /**
     * No remote invokation.
     *
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }

    /**
     * No remote invokation.
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.REMOTE_CACHE;
        //return cache.getCacheType();
    }

    /**
     * Returns the asyn cache status. An error status indicates either the
     * remote connection is not available, or the asyn queue has been
     * unexpectedly destroyed. No remote invokation.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return q.isAlive() ? cache.getStatus() : CacheConstants.STATUS_ERROR;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWait object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cache.getCacheName();
    }

    /**
     * Replaces the remote cache service handle with the given handle and reset
     * the event queue by starting up a new instance.
     */
    public void fixCache( IRemoteCacheService remote )
    {
        cache.fixCache( remote );
        resetEventQ();
        return;
    }

    /**
     * Resets the event q by first destroying the existing one and starting up
     * new one.
     */
    public void resetEventQ()
    {
        if ( q.isAlive() )
        {
            q.destroy();
        }
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), RemoteCacheInfo.listenerId, cache.getCacheName() );
    }

    /** Description of the Method */
    public String toString()
    {
        return "RemoteCacheNoWait: " + cache.toString();
    }
}
