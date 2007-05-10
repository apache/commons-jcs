package org.apache.jcs.auxiliary.lateral;

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
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheEventQueueFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Used to queue up update requests to the underlying cache. These requests will be processed in
 * their order of arrival via the cache event queue processor.
 */
public class LateralCacheNoWait
    implements AuxiliaryCache
{
    private static final long serialVersionUID = -7251187566116178475L;

    private final static Log log = LogFactory.getLog( LateralCacheNoWait.class );

    private final LateralCache cache;

    private ICacheEventQueue q;

    private int getCount = 0;

    private int removeCount = 0;

    private int putCount = 0;

    /**
     * Constructs with the given lateral cache, and fires up an event queue for aysnchronous
     * processing.
     * <p>
     * @param cache
     */
    public LateralCacheNoWait( LateralCache cache )
    {
        this.cache = cache;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructing LateralCacheNoWait, LateralCache = [" + cache + "]" );
        }

        CacheEventQueueFactory fact = new CacheEventQueueFactory();
        this.q = fact.createCacheEventQueue( new CacheAdaptor( cache ), LateralCacheInfo.listenerId, cache
            .getCacheName(), cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
            .getAuxiliaryCacheAttributes().getEventQueueTypeFactoryCode() );

        // need each no wait to handle each of its real updates and removes,
        // since there may
        // be more than one per cache? alternative is to have the cache
        // perform updates using a different method that spcifies the listener
        // this.q = new CacheEventQueue(new CacheAdaptor(this),
        // LateralCacheInfo.listenerId, cache.getCacheName());
        if ( cache.getStatus() == CacheConstants.STATUS_ERROR )
        {
            q.destroy();
        }
    }

    /**
     * @param ce
     * @throws IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        putCount++;
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

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param key
     * @return ICacheElement if found, else null
     */
    public ICacheElement get( Serializable key )
    {
        getCount++;
        if ( this.getStatus() != CacheConstants.STATUS_ERROR )
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
        }
        return null;
    }

    public Set getGroupKeys( String groupName )
    {
        return cache.getGroupKeys( groupName );
    }

    /**
     * Adds a remove request to the lateral cache.
     * <p>
     * @param key
     * @return always false
     */
    public boolean remove( Serializable key )
    {
        removeCount++;
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
     * No lateral invocation.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }

    /**
     * No lateral invocation.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return cache.getCacheType();
    }

    /**
     * Returns the asyn cache status. An error status indicates either the lateral connection is not
     * available, or the asyn queue has been unexpectedly destroyed. No lateral invokation.
     * <p>
     * @return The status value
     */
    public int getStatus()
    {
        return q.isWorking() ? cache.getStatus() : CacheConstants.STATUS_ERROR;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWait object
     * <p>
     * @return The cacheName value
     */
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
    public void fixCache( ILateralCacheService lateral )
    {
        cache.fixCache( lateral );
        resetEventQ();
        return;
    }

    /**
     * Resets the event q by first destroying the existing one and starting up new one.
     */
    public void resetEventQ()
    {
        if ( q.isWorking() )
        {
            q.destroy();
        }
        CacheEventQueueFactory fact = new CacheEventQueueFactory();
        this.q = fact.createCacheEventQueue( new CacheAdaptor( cache ), LateralCacheInfo.listenerId, cache
            .getCacheName(), cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
            .getAuxiliaryCacheAttributes().getEventQueueTypeFactoryCode() );
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return cache.getAuxiliaryCacheAttributes();
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
        stats.setTypeName( "Lateral Cache No Wait" );

        ArrayList elems = new ArrayList();

        // IStatElement se = null;
        // no data gathered here

        // get the stats from the event queue too
        // get as array, convert to list, add list to our outer list
        IStats eqStats = this.q.getStatistics();

        IStatElement[] eqSEs = eqStats.getStatElements();
        List eqL = Arrays.asList( eqSEs );
        elems.addAll( eqL );

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Get Count" );
        se.setData( "" + this.getCount );
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

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( " LateralCacheNoWait " );
        buf.append( " Status = " + this.getStatus() );
        buf.append( " cache = [" + cache.toString() + "]" );
        return buf.toString();
    }
}
