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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheFactory;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs4.engine.match.behavior.IKeyMatcher;
import org.apache.commons.jcs4.log.Log;

/** Create Disk File Caches */
public class FileDiskCacheFactory
    implements AuxiliaryCacheFactory
{
    /** The logger. */
    private static final Log log = Log.getLog( FileDiskCacheFactory.class );

    /** The auxiliary name. */
    private String name;

    /** Each region has an entry here. */
    private final ConcurrentMap<String, FileDiskCache<?, ?>> caches =
        new ConcurrentHashMap<>();

    /**
     * Create the cache. The same factory will be called multiple times by the
     * composite cache to create a cache for each region.
     *
     * @param attr config
     * @param cacheMgr The manager to use if needed
     * @param cacheEventLogger The event logger
     * @param elementSerializer The serializer
     * @param keyMatcher The key matcher
     * @return AuxiliaryCache
     */
    @Override
    public <K, V> FileDiskCache<K, V> createCache(
           final AuxiliaryCacheAttributes attr, final ICompositeCacheManager cacheMgr,
           final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer,
           IKeyMatcher<K> keyMatcher)
    {
        final FileDiskCacheAttributes idfca = (FileDiskCacheAttributes) attr;
        if ( log.isDebugEnabled() )
        {
            log.debug( "Creating DiskFileCache for attributes = " + idfca );
        }
        return getCache(idfca, cacheEventLogger, elementSerializer,
                keyMatcher);
    }

    /**
     * Gets an DiskFileCache for the supplied attributes. Will provide an existing cache for the name
     * attribute if one has been created, or will create a new cache.
     *
     * @param cacheAttributes Attributes the cache should have.
     * @return A cache, either from the existing set or newly created.
     */
    @SuppressWarnings("unchecked") // Need to cast because of common map for all caches
    private <K, V> FileDiskCache<K, V> getCache( final FileDiskCacheAttributes cacheAttributes,
            final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer,
            final IKeyMatcher<?> keyMatcher)
    {
        final FileDiskCacheAttributes myCacheAttributes = (FileDiskCacheAttributes) cacheAttributes.clone();
        final String cacheName = cacheAttributes.getCacheName();

        log.debug( "Getting cache named: " + cacheName );

        // Try to load the cache from the set that have already been
        // created. This only looks at the name attribute.
        return (FileDiskCache<K, V>) caches.computeIfAbsent(cacheName, k -> {
            FileDiskCache<K, V> newCache = new FileDiskCache<>(myCacheAttributes, elementSerializer);
            newCache.setCacheEventLogger(cacheEventLogger);
            newCache.setKeyMatcher((IKeyMatcher<K>) keyMatcher);
            return newCache;
        });
    }

    /**
     * Gets the name attribute of the DiskCacheFactory object
     *
     * @return The name value
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name attribute of the DiskCacheFactory object
     *
     * @param name The new name value
     */
    @Override
    public void setName( final String name )
    {
        this.name = name;
    }

    /**
     * Gets the class implementing the extended AuxiliaryCacheAttributes for this factory
     *
     * @return The class value
     */
    @Override
    public Class<? extends AuxiliaryCacheAttributes> getAttributeClass()
    {
        return FileDiskCacheAttributes.class;
    }
}
