package org.apache.commons.jcs3.auxiliary.lateral;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheAdaptor;
import org.apache.commons.jcs3.engine.CacheEventQueueFactory;
import org.apache.commons.jcs3.engine.CacheInfo;
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
 * Used to queue up update requests to the underlying cache. These requests will be processed in
 * their order of arrival via the cache event queue processor.
 */
public class LateralCacheNoWait<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /** The logger. */
    private static final Log log = LogManager.getLog( LateralCacheNoWait.class );

    /** The cache */
    private final LateralCache<K, V> cache;

    /** Identify this object */
    private String identityKey;

    /** The event queue */
    private ICacheEventQueue<K, V> eventQueue;

    /** times get called */
    private int getCount;

    /** times remove called */
    private int removeCount;

    /** times put called */
    private int putCount;

    /**
     * Constructs with the given lateral cache, and fires up an event queue for asynchronous
     * processing.
     * <p>
     * @param cache
     */
    public LateralCacheNoWait( final LateralCache<K, V> cache )
    {
        this.cache = cache;
        this.identityKey = cache.getCacheName();
        this.setCacheEventLogger(cache.getCacheEventLogger());
        this.setElementSerializer(cache.getElementSerializer());

        log.debug( "Constructing LateralCacheNoWait, LateralCache = [{0}]", cache );

        final CacheEventQueueFactory<K, V> fact = new CacheEventQueueFactory<>();
        this.eventQueue = fact.createCacheEventQueue( new CacheAdaptor<>( cache ),
                CacheInfo.listenerId, cache.getCacheName(),
                getAuxiliaryCacheAttributes().getEventQueuePoolName(),
                getAuxiliaryCacheAttributes().getEventQueueType() );

        // need each no wait to handle each of its real updates and removes,
        // since there may
        // be more than one per cache? alternative is to have the cache
        // perform updates using a different method that specifies the listener
        // this.q = new CacheEventQueue(new CacheAdaptor(this),
        // LateralCacheInfo.listenerId, cache.getCacheName());
        if ( cache.getStatus() == CacheStatus.ERROR )
        {
            eventQueue.destroy();
        }
    }

    /**
     * The identifying key to this no wait
     *
     * @return the identity key
     * @since 3.1
     */
    public String getIdentityKey()
    {
        return identityKey;
    }

    /**
     * Set the identifying key to this no wait
     *
     * @param identityKey the identityKey to set
     * @since 3.1
     */
    public void setIdentityKey(String identityKey)
    {
        this.identityKey = identityKey;
    }

    /**
     * @param ce
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
        throws IOException
    {
        putCount++;
        try
        {
            eventQueue.addPutEvent( ce );
        }
        catch ( final IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param key
     * @return ICacheElement&lt;K, V&gt; if found, else null
     */
    @Override
    public ICacheElement<K, V> get( final K key )
    {
        getCount++;
        if ( this.getStatus() != CacheStatus.ERROR )
        {
            try
            {
                return cache.get( key );
            }
            catch ( final UnmarshalException ue )
            {
                log.debug( "Retrying the get owing to UnmarshalException..." );
                try
                {
                    return cache.get( key );
                }
                catch ( final IOException ex )
                {
                    log.error( "Failed in retrying the get for the second time." );
                    eventQueue.destroy();
                }
            }
            catch ( final IOException ex )
            {
                eventQueue.destroy();
            }
        }
        return null;
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        if ( keys != null && !keys.isEmpty() )
        {
            return keys.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        this::get)).entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Entry::getKey,
                            Entry::getValue));
        }

        return new HashMap<>();
    }

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param pattern
     * @return ICacheElement&lt;K, V&gt; if found, else empty
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching(final String pattern)
    {
        getCount++;
        if ( this.getStatus() != CacheStatus.ERROR )
        {
            try
            {
                return cache.getMatching( pattern );
            }
            catch ( final UnmarshalException ue )
            {
                log.debug( "Retrying the get owing to UnmarshalException." );
                try
                {
                    return cache.getMatching( pattern );
                }
                catch ( final IOException ex )
                {
                    log.error( "Failed in retrying the get for the second time." );
                    eventQueue.destroy();
                }
            }
            catch ( final IOException ex )
            {
                eventQueue.destroy();
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        try
        {
            return cache.getKeySet();
        }
        catch ( final IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
        return Collections.emptySet();
    }

    /**
     * Adds a remove request to the lateral cache.
     * <p>
     * @param key
     * @return always false
     */
    @Override
    public boolean remove( final K key )
    {
        removeCount++;
        try
        {
            eventQueue.addRemoveEvent( key );
        }
        catch ( final IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
        return false;
    }

    /** Adds a removeAll request to the lateral cache. */
    @Override
    public void removeAll()
    {
        try
        {
            eventQueue.addRemoveAllEvent();
        }
        catch ( final IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /** Adds a dispose request to the lateral cache. */
    @Override
    public void dispose()
    {
        try
        {
            eventQueue.addDisposeEvent();
        }
        catch ( final IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /**
     * No lateral invocation.
     * <p>
     * @return The size value
     */
    @Override
    public int getSize()
    {
        return cache.getSize();
    }

    /**
     * No lateral invocation.
     * <p>
     * @return The cacheType value
     */
    @Override
    public CacheType getCacheType()
    {
        return cache.getCacheType();
    }

    /**
     * Returns the async cache status. An error status indicates either the lateral connection is not
     * available, or the asyn queue has been unexpectedly destroyed. No lateral invocation.
     * <p>
     * @return The status value
     */
    @Override
    public CacheStatus getStatus()
    {
        return eventQueue.isWorking() ? cache.getStatus() : CacheStatus.ERROR;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWait object
     * <p>
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return cache.getCacheName();
    }

    /**
     * Replaces the lateral cache service handle with the given handle and reset the queue by
     * starting up a new instance.
     * <p>
     * @param lateral
     */
    public void fixCache( final ICacheServiceNonLocal<K, V> lateral )
    {
        cache.fixCache( lateral );
        resetEventQ();
    }

    /**
     * Resets the event q by first destroying the existing one and starting up new one.
     */
    public void resetEventQ()
    {
        if ( eventQueue.isWorking() )
        {
            eventQueue.destroy();
        }
        final CacheEventQueueFactory<K, V> fact = new CacheEventQueueFactory<>();
        this.eventQueue = fact.createCacheEventQueue( new CacheAdaptor<>( cache ),
                CacheInfo.listenerId, cache.getCacheName(),
                getAuxiliaryCacheAttributes().getEventQueuePoolName(),
                getAuxiliaryCacheAttributes().getEventQueueType() );
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    @Override
    public ILateralCacheAttributes getAuxiliaryCacheAttributes()
    {
        return cache.getAuxiliaryCacheAttributes();
    }

    /**
     * getStats
     * @return String
     */
    @Override
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return "Lateral Cache No Wait";
    }

    /**
     * @return statistics about this communication
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Lateral Cache No Wait" );

        // get the stats from the event queue too
        final IStats eqStats = this.eventQueue.getStatistics();
        final ArrayList<IStatElement<?>> elems = new ArrayList<>(eqStats.getStatElements());

        elems.add(new StatElement<>( "Get Count", Integer.valueOf(this.getCount) ) );
        elems.add(new StatElement<>( "Remove Count", Integer.valueOf(this.removeCount) ) );
        elems.add(new StatElement<>( "Put Count", Integer.valueOf(this.putCount) ) );
        elems.add(new StatElement<>( "Attributes", cache.getAuxiliaryCacheAttributes() ) );

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * @return debugging info.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( " LateralCacheNoWait " );
        buf.append( " Status = " + this.getStatus() );
        buf.append( " cache = [" + cache.toString() + "]" );
        return buf.toString();
    }
}
