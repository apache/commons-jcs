package org.apache.jcs.auxiliary.lateral;

import java.io.IOException;
import java.io.Serializable;

import java.rmi.UnmarshalException;

import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.jcs.auxiliary.lateral.socket.tcp.*;

/**
 * Used to queue up update requests to the underlying cache. These requests will
 * be processed in their order of arrival via the cache event queue processor.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheNoWait implements ICache
{
    private final static Log log =
        LogFactory.getLog( LateralCacheNoWait.class );

    private final LateralCache cache;
    private ICacheEventQueue q;

    /**
     * Constructs with the given lateral cache, and fires up an event queue for
     * aysnchronous processing.
     *
     * @param cache
     */
    public LateralCacheNoWait( LateralCache cache )
    {
        this.cache = cache;
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), LateralCacheInfo.listenerId, cache.getCacheName() );

        // need each no wait to handle each of its real updates and removes, since there may
        // be more than one per cache?  alternativve is to have the cache
        // perform updates using a different method that spcifies the listener
        //this.q = new CacheEventQueue(new CacheAdaptor(this), LateralCacheInfo.listenerId, cache.getCacheName());
        if ( cache.getStatus() == cache.STATUS_ERROR )
        {
            q.destroy();
        }
    }


    /** Adds a put request to the lateral cache. */
    public void put( Serializable key, Serializable value )
        throws IOException
    {
        put( key, value, null );
    }


    /** Description of the Method */
    public void put( Serializable key, Serializable value, IElementAttributes attr )
        throws IOException
    {
        try
        {
            CacheElement ce = new CacheElement( cache.getCacheName(), key, value );
            ce.setElementAttributes( attr );
            update( ce );
        }
        catch ( IOException ex )
        {
            log.error( ex );
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
        }
    }


    /** Synchronously reads from the lateral cache. */
    public Serializable get( Serializable key )
    {
        return get( key, true );
    }


    /** Description of the Method */
    public Serializable get( Serializable key, boolean container )
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
                log.error( "Failed in retrying the get for the second time." );
                q.destroy();
            }
        }
        catch ( IOException ex )
        {
            q.destroy();
        }
        return null;
    }


    /** Adds a remove request to the lateral cache. */
    public boolean remove( Serializable key )
    {
        try
        {
            q.addRemoveEvent( key );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
        return false;
    }


    /** Adds a removeAll request to the lateral cache. */
    public void removeAll()
    {
        try
        {
            q.addRemoveAllEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            q.destroy();
        }
    }


    /** Adds a dispose request to the lateral cache. */
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
     * No lateral invokation.
     *
     * @return The stats value
     */
    public String getStats()
    {
        return cache.getStats();
    }


    /**
     * No lateral invokation.
     *
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }


    /**
     * No lateral invokation.
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return cache.getCacheType();
    }


    /**
     * Returns the asyn cache status. An error status indicates either the
     * lateral connection is not available, or the asyn queue has been
     * unexpectedly destroyed. No lateral invokation.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return q.isAlive() ? cache.getStatus() : cache.STATUS_ERROR;
    }


    /**
     * Gets the cacheName attribute of the LateralCacheNoWait object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cache.getCacheName();
    }


    /**
     * Replaces the lateral cache service handle with the given handle and reset
     * the event queue by starting up a new instance.
     */
    public void fixCache( ILateralCacheService lateral )
    {
        cache.fixCache( lateral );
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
        this.q = new CacheEventQueue( new CacheAdaptor( cache ), LateralCacheInfo.listenerId, cache.getCacheName() );
    }


    /** Description of the Method */
    public String toString()
    {
        return "LateralCacheNoWait: " + cache.toString();
    }
}
