package org.apache.commons.jcs.yajcache.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

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
import org.apache.commons.jcs.yajcache.lang.annotation.NonNullable;
import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.jcs.yajcache.soft.SoftRefFileCache;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.IKeyedReadWriteLock;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.KeyedReadWriteLock;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Enumerates cache managers for getting, creating and removing named caches.
 */
// // http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheManager {
    inst;

    private static final boolean debug = true;
    private final AtomicInteger countGetCache = new AtomicInteger();

    private final AtomicInteger countCreateCache = new AtomicInteger();
    private final AtomicInteger countCreateFileCache = new AtomicInteger();

    private final AtomicInteger countRemoveCache = new AtomicInteger();
    private final AtomicInteger countRemoveFileCache = new AtomicInteger();

    // Cache name to Cache mapping.
    private final ConcurrentMap<String,ICache<?>> map = new ConcurrentHashMap<>();

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
    @SuppressWarnings("unchecked")
    public <V> ICache<V> getCache(@NonNullable final String name) {
        return (ICache<V>) map.get(name);
    }

    /**
     * Returns an existing safe cache for the specified name;
     * or null if such a safe cache cannot not found.
     */
    public <V> ICacheSafe<V> getSafeCache(@NonNullable final String name) {
        final ICache<V> c = getCache(name);

        if (c instanceof ICacheSafe<V> safeCache) {
            return safeCache;
        }
        return null;
    }

    /**
     * Returns an existing cache for the specified name and value type;
     * or null if not found.
     */
    public <V> ICache<V> getCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType)
    {
        if (debug) {
            countGetCache.incrementAndGet();
        }
        final ICache<V> c = getCache(name);
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
        final ICache<V> c = getCache(name, valueType);

        if (c instanceof ICacheSafe<V> safeCache) {
            return checkValueType(c, valueType) ? safeCache : null;
        }
        return null;
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
        ICache<V> c = getCache(name);

        if (c == null) {
            switch(cacheType) {
                case SOFT_REFERENCE:
                case SOFT_REFERENCE_SAFE:
                    c = tryCreateCache(name, valueType, cacheType);
                    break;
                case SOFT_REFERENCE_FILE:
                case SOFT_REFERENCE_FILE_SAFE:
                    c = tryCreateFileCache(name, valueType, cacheType);
                    break;
                default:
                    throw new AssertionError(cacheType);
            }
        }
        else {
            checkTypes(c, cacheType, valueType);
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
        return (ICacheSafe<V>) getCache(name, valueType, cacheType);
    }

    /**
     * Removes the specified cache, if it exists.
     */
    public <V> ICache<V> removeCache(@NonNullable final String name) {
        if (debug) {
            countRemoveCache.incrementAndGet();
        }
        @SuppressWarnings("unchecked")
        final ICache<V> c = (ICache<V>) map.remove(name);

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
                        countRemoveFileCache.incrementAndGet();
                    }
                    final Lock lock = keyedRWLock.writeLock(name);
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
    private @NonNullable <V> ICache<V> tryCreateCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        if (debug) {
            countCreateCache.incrementAndGet();
        }

        @SuppressWarnings("unchecked")
        final ICache<V> cache = (ICache<V>) map.compute(name, (k, v) -> {
            if (v == null) {
                return cacheType.createCache(k, valueType);
            } else if (checkValueType((ICache<V>) v, valueType)) {
                return v;
            } else {
                throw new ClassCastException(valueType + " is incompatible with " + v.getValueType());
            }
        });

        return cache;
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
    @SuppressWarnings("unchecked")
    private @NonNullable <V> ICache<V> tryCreateFileCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            @NonNullable final CacheType cacheType)
    {
        if (debug) {
            countCreateFileCache.incrementAndGet();
        }
        ICache<V> cache = null;
        final Lock lock = keyedRWLock.writeLock(name);
        lock.lock();
        try {
            cache = (ICache<V>) map.computeIfAbsent(name, k -> cacheType.createCache(k, valueType));
        } finally {
            lock.unlock();
        }
        return cache;
    }

    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateCacheRaceCondition(
            @NonNullable final String name, @NonNullable final Class<V> valueType, @NonNullable final CacheType cacheType)
    {
        return tryCreateCache(name, valueType, cacheType);
    }
    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateFileCacheRaceCondition(
            @NonNullable final String name, @NonNullable final Class<V> valueType, @NonNullable final CacheType cacheType)
    {
        return tryCreateCache(name, valueType, cacheType);
    }

    /**
     * Checks the compatibility of the given cacheType and valueType with the
     * given cache.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
    private <V> void checkTypes(final ICache<V> c,
            @NonNullable final CacheType cacheType, @NonNullable final Class<V> valueType)
    {
        if (c == null) {
            return;
        }
        if (!c.getCacheType().isAssignableFrom(cacheType)) {
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
    private <V> boolean checkValueType(@NonNullable final ICache<V> c, @NonNullable final Class<V> valueType)
    {
        final Class<V> cacheValueType = c.getValueType();
        return cacheValueType.isAssignableFrom(valueType);
    }

    /** Retrieves a read lock on the given file cache. */
    public Lock readLock(final SoftRefFileCache<?> cache) {
        return keyedRWLock.readLock(cache.getName());
    }

    /** Dispose all cache instances */
    public void dispose() {
        map.keySet().forEach(this::removeCache);
        map.clear();
    }

    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("\n")
            .append("countCreateCache", countCreateCache)
            .append("\n")
            .append("countCreateFileCache", countCreateFileCache)
            .append("\n")
            .append("countCreateFileCacheRace", countGetCache)
            .append("\n")
            .append("countRemoveCache", countRemoveCache)
            .append("\n")
            .append("countRemoveFileCache", countRemoveFileCache)
            .toString();
    }
}
