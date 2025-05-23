/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jcs.auxiliary.disk.file;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogFactory;

/**
 * This is a non singleton. It creates caches on a per region basis.
 */
public class FileDiskCacheManager
{
    /** The logger */
    private static final Log log = LogFactory.getLog( FileDiskCacheManager.class );

    /** Each region has an entry here. */
    private final ConcurrentMap<String, FileDiskCache<?, ?>> caches =
        new ConcurrentHashMap<>();

    /** Lock cache initialization */
    private final Lock lock = new ReentrantLock();

    /** User configurable attributes */
    private final FileDiskCacheAttributes defaultCacheAttributes;

    /** Event logger */
    private final ICacheEventLogger eventLogger;

    /** Custom serializer */
    private final IElementSerializer elementSerializer;

    /**
     * Constructor for the DiskFileCacheManager object
     *
     * @param defaultCacheAttributes Default attributes for caches managed by the instance.
     * @param cacheEventLogger
     * @param elementSerializer
     */
    protected FileDiskCacheManager( final FileDiskCacheAttributes defaultCacheAttributes, final ICacheEventLogger cacheEventLogger,
                                  final IElementSerializer elementSerializer )
    {
        this.defaultCacheAttributes = defaultCacheAttributes;
        this.elementSerializer = elementSerializer;
        this.eventLogger = cacheEventLogger;
    }

    /**
     * Gets an DiskFileCache for the supplied name using the default attributes.
     *
     * @param cacheName Name that will be used when creating attributes.
     * @return A cache.
     */
    public <K, V> FileDiskCache<K, V> getCache( final String cacheName )
    {
        final FileDiskCacheAttributes cacheAttributes = (FileDiskCacheAttributes) defaultCacheAttributes.clone();

        cacheAttributes.setCacheName( cacheName );

        return getCache( cacheAttributes );
    }

    /**
     * Gets an DiskFileCache for the supplied attributes. Will provide an existing cache for the name
     * attribute if one has been created, or will create a new cache.
     *
     * @param cacheAttributes Attributes the cache should have.
     * @return A cache, either from the existing set or newly created.
     */
    @SuppressWarnings("unchecked") // Need to cast because of common map for all caches
    public <K, V> FileDiskCache<K, V> getCache( final FileDiskCacheAttributes cacheAttributes )
    {
        FileDiskCache<K, V> cache = null;

        final String cacheName = cacheAttributes.getCacheName();

        log.debug( "Getting cache named: " + cacheName );

        // Try to load the cache from the set that have already been
        // created. This only looks at the name attribute.
        cache = (FileDiskCache<K, V>) caches.get( cacheName );

        if (cache == null)
        {
            lock.lock();

            try
            {
                cache = (FileDiskCache<K, V>) caches.get( cacheName );

                // If it was not found, create a new one using the supplied
                // attributes
                if ( cache == null )
                {
                    cache = new FileDiskCache<>( cacheAttributes, elementSerializer );
                    cache.setCacheEventLogger( eventLogger );
                    caches.put( cacheName, cache );
                }
            }
            finally
            {
                lock.unlock();
            }
        }

        return cache;
    }
}
