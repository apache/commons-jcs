package org.apache.commons.jcs3.engine.memory.shrinking;

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

import java.util.Set;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs3.engine.memory.behavior.IMemoryCache;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * A background memory shrinker. Memory problems and concurrent modification exception caused by
 * acting directly on an iterator of the underlying memory cache should have been solved.
 * @version $Id$
 */
public class ShrinkerThread<K, V>
    implements Runnable
{
    /** The logger */
    private static final Log log = LogManager.getLog( ShrinkerThread.class );

    /** The CompositeCache instance which this shrinker is watching */
    private final CompositeCache<K, V> cache;

    /** Maximum memory idle time for the whole cache */
    private final long maxMemoryIdleTime;

    /** Maximum number of items to spool per run. Default is -1, or no limit. */
    private final int maxSpoolPerRun;

    /** Should we limit the number spooled per run. If so, the maxSpoolPerRun will be used. */
    private boolean spoolLimit;

    /**
     * Constructor for the ShrinkerThread object.
     * <p>
     * @param cache The MemoryCache which the new shrinker should watch.
     */
    public ShrinkerThread( final CompositeCache<K, V> cache )
    {
        this.cache = cache;

        final long maxMemoryIdleTimeSeconds = cache.getCacheAttributes().getMaxMemoryIdleTimeSeconds();

        if ( maxMemoryIdleTimeSeconds < 0 )
        {
            this.maxMemoryIdleTime = -1;
        }
        else
        {
            this.maxMemoryIdleTime = maxMemoryIdleTimeSeconds * 1000;
        }

        this.maxSpoolPerRun = cache.getCacheAttributes().getMaxSpoolPerRun();
        if ( this.maxSpoolPerRun != -1 )
        {
            this.spoolLimit = true;
        }

    }

    /**
     * Main processing method for the ShrinkerThread object
     */
    @Override
    public void run()
    {
        shrink();
    }

    /**
     * This method is called when the thread wakes up. First the method obtains an array of keys for
     * the cache region. It iterates through the keys and tries to get the item from the cache
     * without affecting the last access or position of the item. The item is checked for
     * expiration, the expiration check has 3 parts:
     * <ol>
     * <li>Has the cacheattributes.MaxMemoryIdleTimeSeconds defined for the region been exceeded? If
     * so, the item should be move to disk.</li> <li>Has the item exceeded MaxLifeSeconds defined in
     * the element attributes? If so, remove it.</li> <li>Has the item exceeded IdleTime defined in
     * the element attributes? If so, remove it. If there are event listeners registered for the
     * cache element, they will be called.</li>
     * </ol>
     * TODO Change element event handling to use the queue, then move the queue to the region and
     *       access via the Cache.
     */
    protected void shrink()
    {
        log.debug( "Shrinking memory cache for: {0}", this.cache::getCacheName);

        final IMemoryCache<K, V> memCache = cache.getMemoryCache();

        try
        {
            final Set<K> keys = memCache.getKeySet();
            final int size = keys.size();
            log.debug( "Keys size: {0}", size );

            int spoolCount = 0;

            for (final K key : keys)
            {
                final ICacheElement<K, V> cacheElement = memCache.getQuiet( key );

                if ( cacheElement == null )
                {
                    continue;
                }

                final IElementAttributes attributes = cacheElement.getElementAttributes();

                boolean remove = false;

                final long now = System.currentTimeMillis();

                // If the element is not eternal, check if it should be
                // removed and remove it if so.
                if ( !attributes.getIsEternal() )
                {
                    remove = cache.isExpired( cacheElement, now,
                            ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                            ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

                    if ( remove )
                    {
                        memCache.remove( key );
                    }
                }

                // If the item is not removed, check is it has been idle
                // long enough to be spooled.

                if ( !remove && maxMemoryIdleTime != -1 )
                {
                    if ( !spoolLimit || spoolCount < this.maxSpoolPerRun )
                    {
                        final long lastAccessTime = attributes.getLastAccessTime();

                        if ( lastAccessTime + maxMemoryIdleTime < now )
                        {
                            log.debug( "Exceeded memory idle time: {0}", key );

                            // Shouldn't we ensure that the element is
                            // spooled before removing it from memory?
                            // No the disk caches have a purgatory. If it fails
                            // to spool that does not affect the
                            // responsibilities of the memory cache.

                            spoolCount++;

                            memCache.remove( key );
                            memCache.waterfal( cacheElement );
                        }
                    }
                    else
                    {
                        log.debug( "spoolCount = \"{0}\"; maxSpoolPerRun = \"{1}\"",
                                spoolCount, maxSpoolPerRun );

                        // stop processing if limit has been reached.
                        if ( spoolLimit && spoolCount >= this.maxSpoolPerRun )
                        {
                            return;
                        }
                    }
                }
            }
        }
        catch ( final Throwable t )
        {
            log.info( "Unexpected trouble in shrink cycle", t );

            // concurrent modifications should no longer be a problem
            // It is up to the IMemoryCache to return an array of keys

            // stop for now
            return;
        }
    }
}
