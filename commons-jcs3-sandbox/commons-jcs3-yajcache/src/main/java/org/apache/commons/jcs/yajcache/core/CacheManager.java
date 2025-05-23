package org.apache.commons.jcs.yajcache.core;

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

import org.apache.commons.jcs.yajcache.file.CacheFileUtils;
import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.jcs.yajcache.soft.SoftRefFileCache;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.IKeyedReadWriteLock;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.KeyedReadWriteLock;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * Cache Manager for getting, creating and removing named caches.
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheManager {
    inst;

    private static final boolean debug = true;
    private final AtomicInteger countGetCache = new AtomicInteger();

    private final AtomicInteger countCreateCache = new AtomicInteger();
    private final AtomicInteger countCreateCacheRace = new AtomicInteger();
    private final AtomicInteger countCreateFileCache = new AtomicInteger();
    private final AtomicInteger countCreateFileCacheRace = new AtomicInteger();

    private final AtomicInteger countRemoveCache = new AtomicInteger();
    private final AtomicInteger countRemoveFileCache = new AtomicInteger();

    // Cache name to Cache mapping.
    private final ConcurrentMap<String,ICache<?>> map =
                new ConcurrentHashMap<>();
    /**
     * Used for entire cache with external IO,
     * so cache create/removal won't conflict with normal get/put operations.
     */
    private final IKeyedReadWriteLock<String> keyedRWLock =
            new KeyedReadWriteLock<>();
    /**
     * Returns an existing cache for the specified name;
     * or null if not found.
     */
    public ICache getCache(@NonNullable final String name) {
        return this.map.get(name);
    }
    /**
     * Returns an existing safe cache for the specified name;
     * or null if such a safe cache cannot not found.
     */
    public ICacheSafe getSafeCache(@NonNullable final String name) {
        final ICache c = this.getCache(name);

        if (!(c instanceof ICacheSafe)) {
            return null;
        }
        return (ICacheSafe)c;
    }
    /**
     * Returns an existing cache for the specified name and value type;
     * or null if not found.
     */
//    @SuppressWarnings({"unchecked"})
    public <V> ICache<V> getCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType)
    {
        if (debug) {
            this.countGetCache.incrementAndGet();
        }
        final ICache c = this.map.get(name);
        return c != null && checkValueType(c, valueType) ? c : null;
    }
    /**
     * Returns an existing safe cache for the specified name and value type;
     * or null if such a safe cache cannot be found.
     */
    public <V> ICacheSafe<V> getSafeCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType)
    {
        final ICache<V> c = this.getCache(name, valueType);

        if (!(c instanceof ICacheSafe)) {
            return null;
        }
        return checkValueType(c, valueType) ? (ICacheSafe<V>)c : null;
    }
    /**
     * Returns a cache for the specified name, value type and cache type.
     * Creates the cache if necessary.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
    public @NonNullable <V> ICache<V> getCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        ICache c = this.map.get(name);

        if (c == null) {
            switch(cacheType) {
                case SOFT_REFERENCE:
                case SOFT_REFERENCE_SAFE:
                    c = this.tryCreateCache(name, valueType, cacheType);
                    break;
                case SOFT_REFERENCE_FILE:
                case SOFT_REFERENCE_FILE_SAFE:
                    c = this.tryCreateFileCache(name, valueType, cacheType);
                    break;
                default:
                    throw new AssertionError(cacheType);
            }
        }
        else {
            this.checkTypes(c, cacheType, valueType);
        }
        return c;
    }
    /**
     * Returns a safe cache for the specified name, value type and cache type.
     * Creates the cache if necessary.
     *
     * @throws IllegalArgumentException if the cache type specified is not a
     *  safe cache type.
     * @throws ClassCastException if the cache already exists for an
     *  incompatible value type or cache type.
     */
    public @NonNullable <V> ICacheSafe<V> getSafeCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        switch(cacheType) {
            case SOFT_REFERENCE_SAFE:
            case SOFT_REFERENCE_FILE_SAFE:
                break;
            default:
                throw new IllegalArgumentException(cacheType.toString());
        }
        return (ICacheSafe<V>)this.getCache(name, valueType, cacheType);
    }
    /**
     * Removes the specified cache, if it exists.
     */
    public ICache removeCache(@NonNullable final String name) {
        if (debug) {
            this.countRemoveCache.incrementAndGet();
        }
        final ICache c = this.map.remove(name);

        if (c != null) {
            final CacheType cacheType = c.getCacheType();

            switch(cacheType) {
                case SOFT_REFERENCE:
                case SOFT_REFERENCE_SAFE:
                    c.clear();
                    break;
                case SOFT_REFERENCE_FILE:
                case SOFT_REFERENCE_FILE_SAFE:
                    if (debug) {
                        this.countRemoveFileCache.incrementAndGet();
                    }
                    final Lock lock = this.keyedRWLock.writeLock(name);
                    lock.lock();
                    try {
                        // Clear will delete the files as well.
                        c.clear();
                        // Delete the cache directory.
                        CacheFileUtils.inst.rmCacheDir(name);
                    } finally {
                        lock.unlock();
                    }
                    break;
                default:
                    throw new AssertionError(cacheType);
            }
        }
        return c;
    }
    /**
     * Creates the specified cache if not already created.
     *
     * @return either the cache created by the current thread, or
     * an existing cache created by another thread due to data race.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
//    @SuppressWarnings({"unchecked"})
    private @NonNullable <V> ICache<V> tryCreateCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        if (debug) {
            this.countCreateCache.incrementAndGet();
        }
        final ICache<V> newCache = cacheType.createCache(name, valueType);
//        SoftRefFileCache<V> newCache = new SoftRefFileCache<V>(name, valueType);
//        newCache.addCacheChangeListener(new CacheFileManager<V>(newCache));
        final ICache oldCache = this.map.putIfAbsent(name, newCache);

        if (oldCache != null) {
            // race condition: cache already created by another thread.
            if (debug) {
                this.countCreateCacheRace.incrementAndGet();
            }
            this.checkTypes(oldCache, cacheType, valueType);
            return oldCache;
        }
        return newCache;
    }
    /**
     * Creates the specified file cache if not already created.
     *
     * @return either the file cache created by the current thread, or
     * an existing file cache created by another thread due to data race.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
    private @NonNullable <V> ICache<V> tryCreateFileCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        if (debug) {
            this.countCreateFileCache.incrementAndGet();
        }
        ICache<V> newCache = null;
        ICache oldCache = null;
        final Lock lock = this.keyedRWLock.writeLock(name);
        lock.lock();
        try {
            newCache = cacheType.createCache(name, valueType);
            oldCache = this.map.putIfAbsent(name, newCache);
        } finally {
            lock.unlock();
        }

        if (oldCache != null) {
            // race condition: cache already created by another thread.
            if (debug) {
                this.countCreateFileCacheRace.incrementAndGet();
            }
            this.checkTypes(oldCache, cacheType, valueType);
            return oldCache;
        }
        return newCache;
    }

    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateCacheRaceCondition(
            @NonNullable final String name, @NonNullable final Class<V> valueType, @NonNullable final CacheType cacheType)
    {
        return this.tryCreateCache(name, valueType, cacheType);
    }
    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateFileCacheRaceCondition(
            @NonNullable final String name, @NonNullable final Class<V> valueType, @NonNullable final CacheType cacheType)
    {
        return this.tryCreateCache(name, valueType, cacheType);
    }
    /**
     * Checks the compatibility of the given cacheType and valueType with the
     * given cache.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
    private <V> void checkTypes(final ICache c,
            @NonNullable final CacheType cacheType, @NonNullable final Class<V> valueType)
    {
        if (c == null) {
            return;
        }
        if (!c.getCacheType().isAsssignableFrom(cacheType)) {
            throw new ClassCastException("Cache " + c.getName()
                + " of type " + c.getCacheType()
                + " already exists and cannot be used for cache type " + cacheType);
        }
        if (!checkValueType(c, valueType)) {
            throw new ClassCastException("Cache " + c.getName()
                + " of value type " + c.getValueType()
                + " already exists and cannot be used for value type " + valueType);
        }
    }
    /**
     * Checks the compatibility of the given valueType with the
     * given cache.
     *
     * @return true if the valueType is compatible with the cache;
     *  false otherwise.
     */
    private boolean checkValueType(@NonNullable final ICache c, @NonNullable final Class<?> valueType)
    {
        final Class<?> cacheValueType = c.getValueType();
        return cacheValueType.isAssignableFrom(valueType);
    }
    /** Retrieves a read lock on the given file cache. */
    public Lock readLock(final SoftRefFileCache<?> cache) {
        return this.keyedRWLock.readLock(cache.getName());
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("\n")
            .append("countCreateCache", this.countCreateCache)
            .append("\n")
            .append("countCreateCacheRace", this.countCreateCacheRace)
            .append("\n")
            .append("countCreateFileCache", this.countCreateFileCache)
            .append("\n")
            .append("countCreateFileCacheRace", this.countCreateFileCacheRace)
            .append("\n")
            .append("countCreateFileCacheRace", this.countGetCache)
            .append("\n")
            .append("countRemoveCache", this.countRemoveCache)
            .append("\n")
            .append("countRemoveFileCache", this.countRemoveFileCache)
            .toString();
    }
}
