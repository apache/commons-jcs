package org.apache.commons.jcs3.auxiliary.remote;

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
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheClient;
import org.apache.commons.jcs3.engine.CacheAdaptor;
import org.apache.commons.jcs3.engine.CacheEventQueueFactory;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

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
public class RemoteCacheNoWait<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /** log instance */
    private static final Log log = LogManager.getLog( RemoteCacheNoWait.class );

    /** The remote cache client */
    private final IRemoteCacheClient<K, V> remoteCacheClient;

    /** Event queue for queuing up calls like put and remove. */
    private ICacheEventQueue<K, V> cacheEventQueue;

    /** how many times get has been called. */
    private int getCount;

    /** how many times getMatching has been called. */
    private int getMatchingCount;

    /** how many times getMultiple has been called. */
    private int getMultipleCount;

    /** how many times remove has been called. */
    private int removeCount;

    /** how many times put has been called. */
    private int putCount;

    /**
     * Constructs with the given remote cache, and fires up an event queue for asynchronous
     * processing.
     * <p>
     * @param cache
     */
    public RemoteCacheNoWait( final IRemoteCacheClient<K, V> cache )
    {
        remoteCacheClient = cache;
        this.cacheEventQueue = createCacheEventQueue(cache);

        if ( remoteCacheClient.getStatus() == CacheStatus.ERROR )
        {
            cacheEventQueue.destroy();
        }
    }

    /**
     * Create a cache event queue from the parameters of the remote client
     * @param client the remote client
     */
    private ICacheEventQueue<K, V> createCacheEventQueue( final IRemoteCacheClient<K, V> client )
    {
        final CacheEventQueueFactory<K, V> factory = new CacheEventQueueFactory<>();
        return factory.createCacheEventQueue(
            new CacheAdaptor<>( client ),
            client.getListenerId(),
            client.getCacheName(),
            client.getAuxiliaryCacheAttributes().getEventQueuePoolName(),
            client.getAuxiliaryCacheAttributes().getEventQueueType() );
    }

    /**
     * Adds a put event to the queue.
     * <p>
     * @param element
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> element )
        throws IOException
    {
        putCount++;
        try
        {
            cacheEventQueue.addPutEvent( element );
        }
        catch ( final IOException e )
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
    @Override
    public ICacheElement<K, V> get( final K key )
        throws IOException
    {
        getCount++;
        try
        {
            return remoteCacheClient.get( key );
        }
        catch ( final UnmarshalException ue )
        {
            log.debug( "Retrying the get owing to UnmarshalException." );

            try
            {
                return remoteCacheClient.get( key );
            }
            catch ( final IOException ex )
            {
                log.info( "Failed in retrying the get for the second time. ", ex );
            }
        }
        catch ( final IOException ex )
        {
            // We don't want to destroy the queue on a get failure.
            // The RemoteCache will Zombie and queue.
            // Since get does not use the queue, I don't want to kill the queue.
            throw ex;
        }

        return null;
    }

    /**
     * @param pattern
     * @return Map
     * @throws IOException
     *
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String pattern )
        throws IOException
    {
        getMatchingCount++;
        try
        {
            return remoteCacheClient.getMatching( pattern );
        }
        catch ( final UnmarshalException ue )
        {
            log.debug( "Retrying the getMatching owing to UnmarshalException." );

            try
            {
                return remoteCacheClient.getMatching( pattern );
            }
            catch ( final IOException ex )
            {
                log.info( "Failed in retrying the getMatching for the second time.", ex );
            }
        }
        catch ( final IOException ex )
        {
            // We don't want to destroy the queue on a get failure.
            // The RemoteCache will Zombie and queue.
            // Since get does not use the queue, I don't want to kill the queue.
            throw ex;
        }

        return Collections.emptyMap();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys. Sends the getMultiple
     * request on to the server rather than looping through the requested keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final Set<K> keys )
        throws IOException
    {
        getMultipleCount++;
        try
        {
            return remoteCacheClient.getMultiple( keys );
        }
        catch ( final UnmarshalException ue )
        {
            log.debug( "Retrying the getMultiple owing to UnmarshalException..." );

            try
            {
                return remoteCacheClient.getMultiple( keys );
            }
            catch ( final IOException ex )
            {
                log.info( "Failed in retrying the getMultiple for the second time.", ex );
            }
        }
        catch ( final IOException ex )
        {
            // We don't want to destroy the queue on a get failure.
            // The RemoteCache will Zombie and queue.
            // Since get does not use the queue, I don't want to kill the queue.
            throw ex;
        }

        return new HashMap<>();
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        return remoteCacheClient.getKeySet();
    }

    /**
     * Adds a remove request to the remote cache.
     * <p>
     * @param key
     * @return if this was successful
     * @throws IOException
     */
    @Override
    public boolean remove( final K key )
        throws IOException
    {
        removeCount++;
        try
        {
            cacheEventQueue.addRemoveEvent( key );
        }
        catch ( final IOException e )
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
    @Override
    public void removeAll()
        throws IOException
    {
        try
        {
            cacheEventQueue.addRemoveAllEvent();
        }
        catch ( final IOException e )
        {
            log.error( "Problem adding RemoveAllEvent to queue.", e );
            cacheEventQueue.destroy();
            throw e;
        }
    }

    /** Adds a dispose request to the remote cache. */
    @Override
    public void dispose()
    {
        try
        {
            cacheEventQueue.addDisposeEvent();
        }
        catch ( final IOException e )
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
    @Override
    public int getSize()
    {
        return remoteCacheClient.getSize();
    }

    /**
     * No remote invocation.
     * <p>
     * @return The cacheType value
     */
    @Override
    public CacheType getCacheType()
    {
        return CacheType.REMOTE_CACHE;
    }

    /**
     * Returns the asyn cache status. An error status indicates either the remote connection is not
     * available, or the asyn queue has been unexpectedly destroyed. No remote invocation.
     * <p>
     * @return The status value
     */
    @Override
    public CacheStatus getStatus()
    {
        return cacheEventQueue.isWorking() ? remoteCacheClient.getStatus() : CacheStatus.ERROR;
    }

    /**
     * Gets the cacheName attribute of the RemoteCacheNoWait object
     * <p>
     * @return The cacheName value
     */
    @Override
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
    public void fixCache( final ICacheServiceNonLocal<?, ?> remote )
    {
        remoteCacheClient.fixCache( remote );
        resetEventQ();
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
        final ICacheEventQueue<K, V> previousQueue = cacheEventQueue;

        this.cacheEventQueue = createCacheEventQueue(this.remoteCacheClient);

        if ( previousQueue.isWorking() )
        {
            // we don't expect anything, it would have all gone to the zombie
            log.info( "resetEventQ, previous queue has [{0}] items queued up.",
                    previousQueue::size);
            previousQueue.destroy();
        }
    }

    /**
     * This is temporary. It allows the manager to get the lister.
     * <p>
     * @return the instance of the remote cache client used by this object
     */
    protected IRemoteCacheClient<K, V> getRemoteCache()
    {
        return remoteCacheClient;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return remoteCacheClient.getAuxiliaryCacheAttributes();
    }

    /**
     * This is for testing only. It allows you to take a look at the event queue.
     * <p>
     * @return ICacheEventQueue
     */
    protected ICacheEventQueue<K, V> getCacheEventQueue()
    {
        return this.cacheEventQueue;
    }

    /**
     * Returns the stats and the cache.toString().
     * <p>
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getStats() + "\n" + remoteCacheClient.toString();
    }

    /**
     * Returns the statistics in String form.
     * <p>
     * @return String
     */
    @Override
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * @return statistics about this communication
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Remote Cache No Wait" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        elems.add(new StatElement<>( "Status", getStatus() ) );

        // get the stats from the cache queue too
        final IStats cStats = this.remoteCacheClient.getStatistics();
        if ( cStats != null )
        {
            elems.addAll(cStats.getStatElements());
        }

        // get the stats from the event queue too
        final IStats eqStats = this.cacheEventQueue.getStatistics();
        elems.addAll(eqStats.getStatElements());

        elems.add(new StatElement<>( "Get Count", this.getCount) );
        elems.add(new StatElement<>( "GetMatching Count", this.getMatchingCount) );
        elems.add(new StatElement<>( "GetMultiple Count", this.getMultipleCount) );
        elems.add(new StatElement<>( "Remove Count", this.removeCount) );
        elems.add(new StatElement<>( "Put Count", this.putCount) );

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return "Remote Cache No Wait";
    }
}
