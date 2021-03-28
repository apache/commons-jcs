package org.apache.commons.jcs3.engine.memory.lru;

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
import java.util.Collections;
import java.util.Map;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs3.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This is a test memory manager using the jdk1.4 LinkedHashMap.
 */
public class LHMLRUMemoryCache<K, V>
    extends AbstractMemoryCache<K, V>
{
    /** The Logger. */
    private static final Log log = LogManager.getLog( LRUMemoryCache.class );

    /**
     * For post reflection creation initialization
     * <p>
     * @param hub
     */
    @Override
    public void initialize( final CompositeCache<K, V> hub )
    {
        super.initialize( hub );
        log.info( "initialized LHMLRUMemoryCache for {0}", this::getCacheName );
    }

    /**
     * Returns a synchronized LHMSpooler
     * <p>
     * @return Collections.synchronizedMap( new LHMSpooler() )
     */
    @Override
    public Map<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return Collections.synchronizedMap( new LHMSpooler() );
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce Description of the Parameter
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
        throws IOException
    {
        putCnt.incrementAndGet();
        map.put( ce.getKey(), new MemoryElementDescriptor<>(ce) );
    }

    /**
     * Update control structures after get
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    @Override
    protected void lockedGetElement(final MemoryElementDescriptor<K, V> me)
    {
        // empty
    }

    /**
     * Remove element from control structure
     * (guarded by the lock)
     *
     * @param me the memory element descriptor
     */
    @Override
    protected void lockedRemoveElement(final MemoryElementDescriptor<K, V> me)
    {
        // empty
    }

    /**
     * Removes all cached items from the cache control structures.
     * (guarded by the lock)
     */
    @Override
    protected void lockedRemoveAll()
    {
        // empty
    }

    /**
     * This returns semi-structured information on the memory cache, such as the size, put count,
     * hit count, and miss count.
     * <p>
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = super.getStatistics();
        stats.setTypeName( "LHMLRU Memory Cache" );

        return stats;
    }

    // ---------------------------------------------------------- debug methods

    /**
     * Dump the cache entries from first to last for debugging.
     */
    public void dumpCacheEntries()
    {
        dumpMap();
    }

    /**
     * This can't be implemented.
     * <p>
     * @param numberToFree
     * @return 0
     * @throws IOException
     */
    @Override
    public int freeElements( final int numberToFree )
        throws IOException
    {
        // can't be implemented using the LHM
        return 0;
    }

    // ---------------------------------------------------------- extended map

    /**
     * Implementation of removeEldestEntry in LinkedHashMap
     */
    protected class LHMSpooler
        extends java.util.LinkedHashMap<K, MemoryElementDescriptor<K, V>>
    {
        /** Don't change. */
        private static final long serialVersionUID = -1255907868906762484L;

        /**
         * Initialize to a small size--for now, 1/2 of max 3rd variable "true" indicates that it
         * should be access and not time governed. This could be configurable.
         */
        public LHMSpooler()
        {
            super( (int) ( getCacheAttributes().getMaxObjects() * .5 ), .75F, true );
        }

        /**
         * Remove eldest. Automatically called by LinkedHashMap.
         * <p>
         * @param eldest
         * @return true if removed
         */
        @SuppressWarnings("synthetic-access")
        @Override
        protected boolean removeEldestEntry( final Map.Entry<K, MemoryElementDescriptor<K, V>> eldest )
        {
            final ICacheElement<K, V> element = eldest.getValue().getCacheElement();

            if ( size() <= getCacheAttributes().getMaxObjects() )
            {
                return false;
            }
            log.debug( "LHMLRU max size: {0}. Spooling element, key: {1}",
                    () -> getCacheAttributes().getMaxObjects(), element::getKey);

            waterfal( element );

            log.debug( "LHMLRU size: {0}", () -> map.size() );
            return true;
        }
    }
}
