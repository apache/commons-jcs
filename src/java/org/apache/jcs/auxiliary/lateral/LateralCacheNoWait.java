package org.apache.jcs.auxiliary.lateral;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.Serializable;
import java.rmi.UnmarshalException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheService;
import org.apache.jcs.engine.CacheAdaptor;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;

//import org.apache.jcs.auxiliary.lateral.socket.tcp.*;

/**
 * Used to queue up update requests to the underlying cache. These requests will
 * be processed in their order of arrival via the cache event queue processor.
 *
 */
public class LateralCacheNoWait implements AuxiliaryCache
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
        }
    }

    /** Synchronously reads from the lateral cache. */
    public ICacheElement get( Serializable key )
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

    public Set getGroupKeys(String groupName)
    {
        return cache.getGroupKeys(groupName);
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
        return q.isAlive() ? cache.getStatus() : CacheConstants.STATUS_ERROR;
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
