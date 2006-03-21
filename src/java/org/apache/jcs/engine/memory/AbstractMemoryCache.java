package org.apache.jcs.engine.memory;

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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.memory.shrinking.ShrinkerThread;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStats;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * Some common code for the LRU and MRU caches.
 * <p>
 * This keeps a static reference to a memory shrinker clock daemon. If this
 * region is configured to use the shrinker, the clock daemon will be setup to
 * run the shrinker on this region.
 * 
 * @version $Id$
 */
public abstract class AbstractMemoryCache
    implements MemoryCache, Serializable
{
    private final static Log log = LogFactory.getLog( AbstractMemoryCache.class );

    private static final int DEFAULT_CHUNK_SIZE = 2;

    /** The region name. This defines a namespace of sorts. */
    protected String cacheName;

    /**
     * Map where items are stored by key
     */
    protected Map map;

    /**
     * Region Elemental Attributes, used as a default.
     */
    public IElementAttributes attr;

    /**
     * Cache Attributes
     */
    public ICompositeCacheAttributes cattr;

    /**
     * The cache region this store is associated with
     */
    protected CompositeCache cache;

    /** status */
    protected int status;

    /** How many to spool at a time. TODO make configurable */
    protected int chunkSize = DEFAULT_CHUNK_SIZE;

    /**
     * The background memory shrinker, one for all regions.
     */
    private static ClockDaemon shrinkerDaemon;

    /**
     * Constructor for the LRUMemoryCache object
     */
    public AbstractMemoryCache()
    {
        status = CacheConstants.STATUS_ERROR;
        map = new Hashtable();
    }

    /**
     * For post reflection creation initialization
     * 
     * @param hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        this.cacheName = hub.getCacheName();
        this.cattr = hub.getCacheAttributes();
        this.cache = hub;

        status = CacheConstants.STATUS_ALIVE;

        if ( cattr.getUseMemoryShrinker() )
        {
            if ( shrinkerDaemon == null )
            {
                shrinkerDaemon = new ClockDaemon();
                shrinkerDaemon.setThreadFactory( new MyThreadFactory() );
            }
            shrinkerDaemon.executePeriodically( cattr.getShrinkerIntervalSeconds() * 1000, new ShrinkerThread( this ),
                                                false );

        }
    }

    /**
     * Removes an item from the cache
     * 
     * @param key
     *            Identifies item to be removed
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public abstract boolean remove( Serializable key )
        throws IOException;

    /**
     * Get an item from the cache
     * 
     * @param key
     *            Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException
     *                Description of the Exception
     */
    public abstract ICacheElement get( Serializable key )
        throws IOException;

    /**
     * Get an item from the cache without effecting its order or last access
     * time
     * 
     * @param key
     *            Description of the Parameter
     * @return The quiet value
     * @exception IOException
     *                Description of the Exception
     */
    public abstract ICacheElement getQuiet( Serializable key )
        throws IOException;

    /**
     * Puts an item to the cache.
     * 
     * @param ce
     *            Description of the Parameter
     * @exception IOException
     *                Description of the Exception
     */
    public abstract void update( ICacheElement ce )
        throws IOException;

    /**
     * Get an Array of the keys for all elements in the memory cache
     * 
     * @return An Object[]
     */
    public abstract Object[] getKeyArray();

    /**
     * Removes all cached items from the cache.
     * 
     * @exception IOException
     */
    public void removeAll()
        throws IOException
    {
        map = new Hashtable();
    }

    /**
     * Prepares for shutdown.
     * 
     * @exception IOException
     */
    public void dispose()
        throws IOException
    {
        log.info( "Memory Cache dispose called.  Shutting down shrinker thread if it is running." );
        if ( shrinkerDaemon != null )
        {
            shrinkerDaemon.shutDown();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.memory.MemoryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Abstract Memory Cache" );
        return stats;
    }

    /**
     * Returns the current cache size.
     * 
     * @return The size value
     */
    public int getSize()
    {
        return this.map.size();
    }

    /**
     * Returns the cache status.
     * 
     * @return The status value
     */
    public int getStatus()
    {
        return this.status;
    }

    /**
     * Returns the cache name.
     * 
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cattr.getCacheName();
    }

    /**
     * Puts an item to the cache.
     * 
     * @param ce
     * @exception IOException
     */
    public void waterfal( ICacheElement ce )
        throws IOException
    {
        this.cache.spoolToDisk( ce );
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     * 
     * @return The iterator value
     */
    public Iterator getIterator()
    {
        return map.entrySet().iterator();
    }

    /**
     * Returns the CacheAttributes.
     * 
     * @return The CacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cattr;
    }

    /**
     * Sets the CacheAttributes.
     * 
     * @param cattr
     *            The new CacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cattr = cattr;
    }

    /**
     * Gets the cache hub / region taht the MemoryCache is used by
     * 
     * @return The cache value
     */
    public CompositeCache getCompositeCache()
    {
        return this.cache;
    }

    public Set getGroupKeys( String groupName )
    {
        GroupId groupId = new GroupId( getCacheName(), groupName );
        HashSet keys = new HashSet();
        synchronized ( map )
        {
            for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) itr.next();
                Object k = entry.getKey();

                if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( groupId ) )
                {
                    keys.add( ( (GroupAttrName) k ).attrName );
                }
            }
        }
        return keys;
    }

    /**
     * Allows us to set the daemon status on the clockdaemon
     * 
     * @author aaronsm
     * 
     */
    class MyThreadFactory
        implements ThreadFactory
    {

        /*
         * (non-Javadoc)
         * 
         * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }
}
