package org.apache.jcs.auxiliary.remote;

/*
 * Copyright 2001-2004 The Apache Software Foundation. Licensed under the Apache License, Version
 * 2.0 (the "License") you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheEventQueueFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Used to queue up update requests to the underlying cache. These requests will be processed in
 * their order of arrival via the cache event queue processor.
 * <p>
 * Typically errors will be handled down stream. We only need to kill the queue if an error makes it
 * to this level from the queue. That can only happen if the queue is damaged, since the events are
 * procesed asynchronously.
 * <p>
 * There is no reason to create a queue on startup if the remote is not healthy.
 * <p>
 * If the remote cache encounters an error it will zombie--create a blaking facade for the service.
 * The Zombie will queue up items until the conenction is restored. An alternative way to accomplish
 * the same thing would be to stop, not destroy the queue at this level. That way items would be
 * added to the queue and then when the connection is restored, we could start the worker threads
 * again. This is a better long term solution, but it requires some significnat changes to the
 * complicated worker queues.
 */
public class RemoteCacheNoWait
    implements AuxiliaryCache
{
    private static final long serialVersionUID = -3104089136003714717L;

    private final static Log log = LogFactory.getLog( RemoteCacheNoWait.class );

    private final RemoteCache cache;

    private ICacheEventQueue q;

    /**
     * Constructs with the given remote cache, and fires up an event queue for aysnchronous
     * processing.
     * <p>
     * @param cache
     */
    public RemoteCacheNoWait( RemoteCache cache )
    {
        this.cache = cache;
        CacheEventQueueFactory fact = new CacheEventQueueFactory();
        this.q = fact.createCacheEventQueue( new CacheAdaptor( cache ), cache.getListenerId(), cache.getCacheName(),
                                             cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
                                                 .getAuxiliaryCacheAttributes().getEventQueueTypeFactoryCode() );

        if ( cache.getStatus() == CacheConstants.STATUS_ERROR )
        {
            q.destroy();
        }
    }

    /**
     * Adds a put event to the queue.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICache#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        try
        {
            q.addPutEvent( ce );
        }
        catch ( IOException ex )
        {
            log.error( "Problem adding putEvent to queue.", ex );
            q.destroy();
            throw ex;
        }
    }

    /**
     * Synchronously reads from the remote cache.
     * <p>
     * @param key
     * @return
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        try
        {
            return cache.get( key );
        }
        catch ( UnmarshalException ue )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Retrying the get owing to UnmarshalException..." );
            }
            try
            {
                return cache.get( key );
            }
            catch ( IOException ex )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Failed in retrying the get for the second time. " + ex.getMessage() );
                }
            }
        }
        catch ( IOException ex )
        {
            // We don't want to destroy the queue on a get failure.
            // The RemoteCache will Zombie and queue.
            // Since get does not use the queue, I dont want to killing the queue.
            throw ex;
        }
        return null;
    }

    public Set getGroupKeys( String groupName )
        throws IOException
    {
        return cache.getGroupKeys( groupName );
    }

    /**
     * Adds a remove request to the remote cache.
     * <p>
     * @param key
     * @return
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        try
        {
            q.addRemoveEvent( key );
        }
        catch ( IOException ex )
        {
            log.error( "Problem adding RemoveEvent to queue.", ex );
            q.destroy();
            throw ex;
        }
        return false;
    }

    /**
     * Adds a removeAll request to the remote cache.
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        try
        {
            q.addRemoveAllEvent();
        }
        catch ( IOException ex )
        {
            log.error( "Problem adding RemoveAllEvent to queue.", ex );
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
            log.error( "Problem adding DisposeEvent to queue.", ex );
            q.destroy();
        }
    }

    /**
     * No remote invocation.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }

    /**
     * No remote invokation.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.REMOTE_CACHE;
    }

    /**
     * Returns the asyn cache status. An error status indicates either the remote connection is not
     * available, or the asyn queue has been unexpectedly destroyed. No remote invocation.
     * <p>
     * @return The status value
     */
    public int getStatus()
    {
        return q.isWorking() ? cache.getStatus() : CacheConstants.STATUS_ERROR;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWait object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cache.getCacheName();
    }

    /**
     * Replaces the remote cache service handle with the given handle and reset the event queue by
     * starting up a new instance.
     * <p>
     * @param remote
     */
    public void fixCache( IRemoteCacheService remote )
    {
        cache.fixCache( remote );
        resetEventQ();
        return;
    }

    /**
     * Resets the event q by first destroying the existing one and starting up new one.
     * <p>
     * There may be no good reason to kill the existing queue. We will sometimes need to set a new
     * listener id, so we should create a new queue. We should let the old queue drain. If we were
     * conencted to the failover, it would be best to finish sending items.
     */
    public void resetEventQ()
    {
        ICacheEventQueue previousQueue = q;

        CacheEventQueueFactory fact = new CacheEventQueueFactory();
        this.q = fact.createCacheEventQueue( new CacheAdaptor( cache ), cache.getListenerId(), cache.getCacheName(),
                                             cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
                                                 .getAuxiliaryCacheAttributes().getEventQueueTypeFactoryCode() );

        if ( previousQueue.isWorking() )
        {
            // we don't expect anything, it would have all gone to the zombie
            if ( log.isInfoEnabled() )
            {
                log.info( "resetEventQ, previous queue has [" + previousQueue.size() + "] items queued up." );
            }
            // TODO consider waiting.
            previousQueue.destroy();
        }
    }

    /**
     * This is temporary. It allows the manager to get the lister.
     * <p>
     * @return
     */
    protected RemoteCache getRemoteCache()
    {
        return cache;
    }

    /**
     * Returns the stats and the cache.toString().
     * <p>
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getStats() + "\n" + cache.toString();
    }

    /**
     * getStats
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.AuxiliaryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Remote Cache No Wait" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Status" );
        int status = this.getStatus();
        if ( status == CacheConstants.STATUS_ERROR )
        {
            se.setData( "ERROR" );
        }
        else if ( status == CacheConstants.STATUS_ALIVE )
        {
            se.setData( "ALIVE" );
        }
        else if ( status == CacheConstants.STATUS_DISPOSED )
        {
            se.setData( "DISPOSED" );
        }
        else
        {
            se.setData( "" + status );
        }
        elems.add( se );

        // no data gathered here

        // get the stats from the cache queue too
        // get as array, convert to list, add list to our outer list
        IStats cStats = this.cache.getStatistics();
        IStatElement[] cSEs = cStats.getStatElements();
        List cL = Arrays.asList( cSEs );
        elems.addAll( cL );

        // get the stats from the event queue too
        // get as array, convert to list, add list to our outer list
        IStats eqStats = this.q.getStatistics();
        IStatElement[] eqSEs = eqStats.getStatElements();
        List eqL = Arrays.asList( eqSEs );
        elems.addAll( eqL );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

}
