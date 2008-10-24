package org.apache.jcs.auxiliary.remote;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AbstractAuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheClient;
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
 * The RemoteCacheNoWait wraps the RemoteCacheClient. The client holds a handle on the
 * RemoteCacheService.
 * <p>
 * Used to queue up update requests to the underlying cache. These requests will be processed in
 * their order of arrival via the cache event queue processor.
 * <p>
 * Typically errors will be handled down stream. We only need to kill the queue if an error makes it
 * to this level from the queue. That can only happen if the queue is damaged, since the events are
 * Processed asynchronously.
 * <p>
 * There is no reason to create a queue on startup if the remote is not healthy.
 * <p>
 * If the remote cache encounters an error it will zombie--create a balking facade for the service.
 * The Zombie will queue up items until the connection is restored. An alternative way to accomplish
 * the same thing would be to stop, not destroy the queue at this level. That way items would be
 * added to the queue and then when the connection is restored, we could start the worker threads
 * again. This is a better long term solution, but it requires some significant changes to the
 * complicated worker queues.
 */
public class RemoteCacheNoWait
    extends AbstractAuxiliaryCache
    implements AuxiliaryCache
{
    /** For serialization. Don't change. */
    private static final long serialVersionUID = -3104089136003714717L;

    /** log instance */
    private final static Log log = LogFactory.getLog( RemoteCacheNoWait.class );

    /** The remote cache client */
    private final IRemoteCacheClient remoteCacheClient;

    /** Event queue for queueing up calls like put and remove. */
    private ICacheEventQueue cacheEventQueue;

    /** how many times get has been called. */
    private int getCount = 0;

    /** how many times getMultiple has been called. */
    private int getMultipleCount = 0;

    /** how many times remove has been called. */
    private int removeCount = 0;

    /** how many times put has been called. */
    private int putCount = 0;

    /**
     * Constructs with the given remote cache, and fires up an event queue for asynchronous
     * processing.
     * <p>
     * @param cache
     */
    public RemoteCacheNoWait( IRemoteCacheClient cache )
    {
        remoteCacheClient = cache;

        CacheEventQueueFactory factory = new CacheEventQueueFactory();
        this.cacheEventQueue = factory.createCacheEventQueue( new CacheAdaptor( remoteCacheClient ), remoteCacheClient
            .getListenerId(), remoteCacheClient.getCacheName(), remoteCacheClient.getAuxiliaryCacheAttributes()
            .getEventQueuePoolName(), remoteCacheClient.getAuxiliaryCacheAttributes().getEventQueueType() );

        if ( remoteCacheClient.getStatus() == CacheConstants.STATUS_ERROR )
        {
            cacheEventQueue.destroy();
        }
    }

    /**
     * Adds a put event to the queue.
     * <p>
     * @param element
     * @throws IOException
     */
    public void update( ICacheElement element )
        throws IOException
    {
        putCount++;
        try
        {
            cacheEventQueue.addPutEvent( element );
        }
        catch ( IOException e )
        {
            log.error( "Problem adding putEvent to queue.", e );
            cacheEventQueue.destroy();
            throw e;
        }
    }

    /**
     * Synchronously reads from the remote cache.
     * <p>
     * @param key
     * @return element from the remote cache, or null if not present
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        getCount++;
        try
        {
            return remoteCacheClient.get( key );
        }
        catch ( UnmarshalException ue )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Retrying the get owing to UnmarshalException..." );
            }

            try
            {
                return remoteCacheClient.get( key );
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

    /** TODO fix this */
    public Map getMatching( String pattern )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * Gets multiple items from the cache based on the given set of keys. Sends the getMultiple
     * request on to the server rather than looping through the requested keys.
     * <p>
     * @param keys
     * @return a map of Serializable key to ICacheElement element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    public Map getMultiple( Set keys )
        throws IOException
    {
        getMultipleCount++;
        try
        {
            return remoteCacheClient.getMultiple( keys );
        }
        catch ( UnmarshalException ue )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Retrying the getMultiple owing to UnmarshalException..." );
            }

            try
            {
                return remoteCacheClient.getMultiple( keys );
            }
            catch ( IOException ex )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Failed in retrying the getMultiple for the second time. " + ex.getMessage() );
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

        return new HashMap();
    }

    /**
     * @param groupName
     * @return the keys for the group name
     * @throws IOException
     */
    public Set getGroupKeys( String groupName )
        throws IOException
    {
        return remoteCacheClient.getGroupKeys( groupName );
    }

    /**
     * Adds a remove request to the remote cache.
     * <p>
     * @param key
     * @return if this was successful
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        removeCount++;
        try
        {
            cacheEventQueue.addRemoveEvent( key );
        }
        catch ( IOException e )
        {
            log.error( "Problem adding RemoveEvent to queue.", e );
            cacheEventQueue.destroy();
            throw e;
        }
        return false;
    }

    /**
     * Adds a removeAll request to the remote cache.
     * <p>
     * @throws IOException
     */
    public void removeAll()
        throws IOException
    {
        try
        {
            cacheEventQueue.addRemoveAllEvent();
        }
        catch ( IOException e )
        {
            log.error( "Problem adding RemoveAllEvent to queue.", e );
            cacheEventQueue.destroy();
            throw e;
        }
    }

    /** Adds a dispose request to the remote cache. */
    public void dispose()
    {
        try
        {
            cacheEventQueue.addDisposeEvent();
        }
        catch ( IOException e )
        {
            log.error( "Problem adding DisposeEvent to queue.", e );
            cacheEventQueue.destroy();
        }
    }

    /**
     * No remote invocation.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return remoteCacheClient.getSize();
    }

    /**
     * No remote invocation.
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
        return cacheEventQueue.isWorking() ? remoteCacheClient.getStatus() : CacheConstants.STATUS_ERROR;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWait object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return remoteCacheClient.getCacheName();
    }

    /**
     * Replaces the remote cache service handle with the given handle and reset the event queue by
     * starting up a new instance.
     * <p>
     * @param remote
     */
    public void fixCache( IRemoteCacheService remote )
    {
        remoteCacheClient.fixCache( remote );
        resetEventQ();
        return;
    }

    /**
     * Resets the event q by first destroying the existing one and starting up new one.
     * <p>
     * There may be no good reason to kill the existing queue. We will sometimes need to set a new
     * listener id, so we should create a new queue. We should let the old queue drain. If we were
     * Connected to the failover, it would be best to finish sending items.
     */
    public void resetEventQ()
    {
        ICacheEventQueue previousQueue = cacheEventQueue;

        CacheEventQueueFactory fact = new CacheEventQueueFactory();
        this.cacheEventQueue = fact.createCacheEventQueue( new CacheAdaptor( remoteCacheClient ), remoteCacheClient
            .getListenerId(), remoteCacheClient.getCacheName(), remoteCacheClient.getAuxiliaryCacheAttributes()
            .getEventQueuePoolName(), remoteCacheClient.getAuxiliaryCacheAttributes().getEventQueueType() );

        if ( previousQueue.isWorking() )
        {
            // we don't expect anything, it would have all gone to the zombie
            if ( log.isInfoEnabled() )
            {
                log.info( "resetEventQ, previous queue has [" + previousQueue.size() + "] items queued up." );
            }
            previousQueue.destroy();
        }
    }

    /**
     * This is temporary. It allows the manager to get the lister.
     * <p>
     * @return the instance of the remote cache client used by this object
     */
    protected IRemoteCacheClient getRemoteCache()
    {
        return remoteCacheClient;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return remoteCacheClient.getAuxiliaryCacheAttributes();
    }

    /**
     * This is for testing only. It allows you to take a look at the event queue.
     * <p>
     * @return ICacheEventQueue
     */
    protected ICacheEventQueue getCacheEventQueue()
    {
        return this.cacheEventQueue;
    }

    /**
     * Returns the stats and the cache.toString().
     * <p>
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getStats() + "\n" + remoteCacheClient.toString();
    }

    /**
     * Returns the statistics in String form.
     * <p>
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * @return statistics about this communication
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
        IStats cStats = this.remoteCacheClient.getStatistics();
        if ( cStats != null )
        {
            IStatElement[] cSEs = cStats.getStatElements();
            List cL = Arrays.asList( cSEs );
            elems.addAll( cL );
        }

        // get the stats from the event queue too
        // get as array, convert to list, add list to our outer list
        IStats eqStats = this.cacheEventQueue.getStatistics();
        IStatElement[] eqSEs = eqStats.getStatElements();
        List eqL = Arrays.asList( eqSEs );
        elems.addAll( eqL );

        se = new StatElement();
        se.setName( "Get Count" );
        se.setData( "" + this.getCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "GetMultiple Count" );
        se.setData( "" + this.getMultipleCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Remove Count" );
        se.setData( "" + this.removeCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Put Count" );
        se.setData( "" + this.putCount );
        elems.add( se );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[elems.size()] );
        stats.setStatElements( ses );

        return stats;
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    public String getEventLoggingExtraInfo()
    {
        return "Remote Cache No Wait";
    }
}
