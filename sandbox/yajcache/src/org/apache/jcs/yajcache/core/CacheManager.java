
/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.jcs.yajcache.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.util.concurrent.locks.IKeyedReadWriteLock;
import org.apache.jcs.yajcache.util.concurrent.locks.KeyedReadWriteLock;

/**
 * Cache Manager for getting, creating and removing named caches.
 *
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheManager {
    inst;
    // Cache name to Cache mapping.
    private final ConcurrentMap<String,ICache<?>> map = 
                new ConcurrentHashMap<String, ICache<?>>();
    private final IKeyedReadWriteLock<String> keyedRWLock = 
            new KeyedReadWriteLock<String>();
    /** 
     * Returns an existing cache for the specified name; 
     * or null if not found.
     */
    public ICache getCache(@NonNullable String name) {
        return this.map.get(name);
    }
    /** 
     * Returns an existing safe cache for the specified name; 
     * or null if such a safe cache cannot not found.
     */
    public ICacheSafe getSafeCache(@NonNullable String name) {
        ICache c = this.getCache(name);
        
        if (c == null || !(c instanceof ICacheSafe))
            return null;
        return (ICacheSafe)c;
    }
    /** 
     * Returns an existing cache for the specified name and value type;  
     * or null if not found.
     */
//    @SuppressWarnings({"unchecked"})
    public <V> ICache<V> getCache(
            @NonNullable String name,
            @NonNullable Class<V> valueType)
    {
        ICache c = this.map.get(name);
        return c != null && this.checkValueType(c, valueType) ? c : null;
    }
    /** 
     * Returns an existing safe cache for the specified name and value type;  
     * or null if such a safe cache cannot be found.
     */
    public <V> ICacheSafe<V> getSafeCache(
            @NonNullable String name,
            @NonNullable Class<V> valueType)
    {
        ICache<V> c = this.getCache(name, valueType);

        if (!(c instanceof ICacheSafe))
            return null;
        return this.checkValueType(c, valueType) ? (ICacheSafe<V>)c : null;
    }
    /** 
     * Returns a cache for the specified name, value type and cache type.
     * Creates the cache if necessary.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type or incompatible cache type.
     */
    public @NonNullable <V> ICache<V> getCache(
            @NonNullable String name,
            @NonNullable Class<V> valueType,
            @NonNullable CacheType cacheType)
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
            @NonNullable String name,
            @NonNullable Class<V> valueType,
            @NonNullable CacheType cacheType)
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
    @TODO("Handle file cache by deleting the dir")
    public ICache removeCache(@NonNullable String name) {
        ICache c = this.map.remove(name);
        
        if (c != null) {
            c.clear();
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
            @NonNullable String name, 
            @NonNullable Class<V> valueType,
            @NonNullable CacheType cacheType)
    {
        ICache<V> newCache = cacheType.createCache(name, valueType);
//        SoftRefFileCache<V> newCache = new SoftRefFileCache<V>(name, valueType);
//        newCache.addCacheChangeListener(new CacheFileManager<V>(newCache));
        ICache oldCache = this.map.putIfAbsent(name, newCache);

        if (oldCache != null) {
            // race condition: cache already created by another thread.
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
            @NonNullable String name, 
            @NonNullable Class<V> valueType,
            @NonNullable CacheType cacheType)
    {
        Lock lock = this.keyedRWLock.writeLock(name);
        lock.lock();
        ICache<V> newCache = null;
        ICache oldCache = null;
        try {
            newCache = cacheType.createCache(name, valueType);
//        SoftRefFileCache<V> newCache = new SoftRefFileCache<V>(name, valueType);
//        newCache.addCacheChangeListener(new CacheFileManager<V>(newCache));
            oldCache = this.map.putIfAbsent(name, newCache);
        } finally {
            lock.unlock();
        }

        if (oldCache != null) {
            // race condition: cache already created by another thread.
            this.checkTypes(oldCache, cacheType, valueType);
            return oldCache;
        }
        return newCache;
    }

    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateCacheRaceCondition(
            @NonNullable String name, @NonNullable Class<V> valueType, @NonNullable CacheType cacheType) 
    {
        return this.tryCreateCache(name, valueType, cacheType);
    }
    @TestOnly("Used solely to simluate a race condition during cache creation ")
    @NonNullable <V> ICache<V> testCreateFileCacheRaceCondition(
            @NonNullable String name, @NonNullable Class<V> valueType, @NonNullable CacheType cacheType) 
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
    private <V> void checkTypes(ICache c, 
            @NonNullable CacheType cacheType, @NonNullable Class<V> valueType) 
    {
        if (c == null)
            return;
        if (!c.getCacheType().isAsssignableFrom(cacheType))
            throw new ClassCastException("Cache " + c.getName()
                + " of type " + c.getCacheType() 
                + " already exists and cannot be used for cache type " + cacheType);
        if (!this.checkValueType(c, valueType))
            throw new ClassCastException("Cache " + c.getName()
                + " of value type " + c.getValueType() 
                + " already exists and cannot be used for value type " + valueType);
        return;
    }
    /**
     * Checks the compatibility of the given valueType with the
     * given cache.
     *
     * @return true if the valueType is compatible with the cache;
     *  false otherwise.
     */
    private boolean checkValueType(@NonNullable ICache c, @NonNullable Class<?> valueType) 
    {
        Class<?> cacheValueType = c.getValueType();
        return cacheValueType.isAssignableFrom(valueType);
    }
}
