package org.apache.jcs.auxiliary.remote;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to provide access to multiple services under nowait protection. factory
 * should construct NoWaitFacade to give to the composite cache out of caches it
 * constructs from the varies manager to lateral services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheNoWaitFacade implements ICache
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheNoWaitFacade.class );

    /** Description of the Field */
    public RemoteCacheNoWait[] noWaits;

    private String cacheName;

    // holds failover and cluster information
    RemoteCacheAttributes rca;


    /**
     * Gets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade
     * object
     *
     * @return The remoteCacheAttributes value
     */
    public RemoteCacheAttributes getRemoteCacheAttributes()
    {
        return rca;
    }


    /**
     * Sets the remoteCacheAttributes attribute of the RemoteCacheNoWaitFacade
     * object
     *
     * @param rca The new remoteCacheAttributes value
     */
    public void setRemoteCacheAttributes( RemoteCacheAttributes rca )
    {
        this.rca = rca;
    }

    /**
     * Constructs with the given remote cache, and fires events to any
     * listeners.
     *
     * @param noWaits
     * @param rca
     */
    //public RemoteCacheNoWaitFacade(RemoteCacheNoWait[] noWaits, String cacheName) {
    public RemoteCacheNoWaitFacade( RemoteCacheNoWait[] noWaits, RemoteCacheAttributes rca )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "CONSTRUCTING NO WAIT FACADE" );
        }
        this.noWaits = noWaits;
        this.rca = rca;
        this.cacheName = rca.getCacheName();
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
            CacheElement ce = new CacheElement( cacheName, key, value );
            ce.setElementAttributes( attr );
            update( ce );
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }


    /** Description of the Method */
    public void update( ICacheElement ce )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "updating through cache facade, noWaits.length = " + noWaits.length );
        }
        int i = 0;
        try
        {
            for ( ; i < noWaits.length; i++ )
            {
                noWaits[i].update( ce );
                // an initial move into a zombie will lock this to primary
                // recovery.  will not discover other servers until primary reconnect
                // and subsequent error
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
            // can handle failover here?  Is it safe to try the others?
            // check to see it the noWait is now a zombie
            // if it is a zombie, then move to the next in the failover list
            // will need to keep them in order or a count
            failover( i );
            // should start a failover thread
            // should probably only failover if there is only one in the noWait list
            // should start a background thread to set the original as the primary
            // if we are in failover state
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
        for ( int i = 0; i < noWaits.length; i++ )
        {
            try
            {
                Object obj = noWaits[i].get( key, container );
                if ( obj != null )
                {
                    return ( Serializable ) obj;
                }
            }
            catch ( Exception ex )
            {
                log.debug( "Failed to get." );
            }
            return null;
        }
        return null;
    }


    /** Adds a remove request to the lateral cache. */
    public boolean remove( Serializable key )
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[i].remove( key );
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
        return false;
    }


    /** Adds a removeAll request to the lateral cache. */
    public void removeAll()
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[i].removeAll();
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }


    /** Adds a dispose request to the lateral cache. */
    public void dispose()
    {
        try
        {
            for ( int i = 0; i < noWaits.length; i++ )
            {
                noWaits[i].dispose();
            }
        }
        catch ( Exception ex )
        {
            log.error( ex );
        }
    }


    /**
     * No lateral invokation.
     *
     * @return The stats value
     */
    public String getStats()
    {
        return "";
        //cache.getStats();
    }


    /**
     * No lateral invokation.
     *
     * @return The size value
     */
    public int getSize()
    {
        return 0;
        //cache.getSize();
    }


    /**
     * Gets the cacheType attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.REMOTE_CACHE;
    }


    /**
     * Gets the cacheName attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return "";
        //cache.getCacheName();
    }


    // need to do something with this
    /**
     * Gets the status attribute of the RemoteCacheNoWaitFacade object
     *
     * @return The status value
     */
    public int getStatus()
    {
        return 0;
        //q.isAlive() ? cache.getStatus() : cache.STATUS_ERROR;
    }


    /** Description of the Method */
    public String toString()
    {
        return "RemoteCacheNoWaitFacade: " + cacheName + ", rca = " + rca;
    }

    /** Description of the Method */
    protected void failover( int i )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "in failover for " + i );
        }
        //if ( noWaits.length == 1 ) {
        if ( rca.getRemoteType() == rca.LOCAL )
        {
            if ( noWaits[i].getStatus() == STATUS_ERROR )
            {
                // start failover, primary recovery process
                RemoteCacheFailoverRunner runner = new RemoteCacheFailoverRunner( this );
                // If the returned monitor is null, it means it's already started elsewhere.
                if ( runner != null )
                {
                    runner.notifyError();
                    Thread t = new Thread( runner );
                    t.setDaemon( true );
                    t.start();
                }
            }
            else
            {
                log.info( "the noWait is not in error" );
            }
        }
    }

}
