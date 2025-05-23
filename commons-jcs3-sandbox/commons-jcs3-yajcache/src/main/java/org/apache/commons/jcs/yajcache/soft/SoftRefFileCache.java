package org.apache.commons.jcs.yajcache.soft;

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

import org.apache.commons.jcs.yajcache.beans.CacheChangeSupport;
import org.apache.commons.jcs.yajcache.beans.ICacheChangeListener;
import org.apache.commons.jcs.yajcache.config.PerCacheConfig;
import org.apache.commons.jcs.yajcache.core.CacheEntry;
import org.apache.commons.jcs.yajcache.core.CacheManager;
import org.apache.commons.jcs.yajcache.core.CacheType;
import org.apache.commons.jcs.yajcache.core.ICache;
import org.apache.commons.jcs.yajcache.file.CacheFileContent;
import org.apache.commons.jcs.yajcache.file.CacheFileContentType;
import org.apache.commons.jcs.yajcache.file.CacheFileDAO;
import org.apache.commons.jcs.yajcache.file.CacheFileUtils;
import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.commons.jcs.yajcache.lang.ref.KeyedSoftReference;
import org.apache.commons.jcs.yajcache.util.CollectionUtils;
import org.apache.commons.jcs.yajcache.util.EqualsUtils;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.IKeyedReadWriteLock;
import org.apache.commons.jcs.yajcache.util.concurrent.locks.KeyedReadWriteLock;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * Cache implemented using Soft References.
 */
@CopyRightApache
@TODO("Annotate the thread-safetyness of the methods")
public class SoftRefFileCache<V> implements ICache<V>
{
    private static final boolean debug = true;
    private final Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue<V> refq = new ReferenceQueue<>();
    private final @NonNullable String name;
    private final @NonNullable Class<V> valueType;
    private final @NonNullable ConcurrentMap<String,KeyedSoftReference<String,V>> map;
    private PerCacheConfig config;

    private final @NonNullable KeyedRefCollector<String> collector;
    private final IKeyedReadWriteLock<String> keyedRWLock = new KeyedReadWriteLock<>();

    private final @NonNullable CacheChangeSupport<V> cacheChangeSupport =
            new CacheChangeSupport<>(this);

    private final AtomicInteger countGet = new AtomicInteger();

    private final AtomicInteger countGetHitMemory = new AtomicInteger();
    private final AtomicInteger countGetHitFile = new AtomicInteger();

    private final AtomicInteger countGetMissMemory = new AtomicInteger();
    private final AtomicInteger countGetMiss = new AtomicInteger();
    private final AtomicInteger countGetCorruptedFile = new AtomicInteger();
    private final AtomicInteger countGetEmptyRef = new AtomicInteger();

    private final AtomicInteger countPut = new AtomicInteger();
    private final AtomicInteger countPutClearRef = new AtomicInteger();
    private final AtomicInteger countPutMissMemory = new AtomicInteger();
    private final AtomicInteger countPutNewMemoryValue = new AtomicInteger();
    private final AtomicInteger countPutNewFileValue = new AtomicInteger();
    private final AtomicInteger countPutSerializable = new AtomicInteger();
    private final AtomicInteger countPutReadFile = new AtomicInteger();
    private final AtomicInteger countPutWriteFile = new AtomicInteger();

    private final AtomicInteger countRemove = new AtomicInteger();

    @Override
    public @NonNullable String getName() {
        return this.name;
    }
    @Override
    public @NonNullable Class<V> getValueType() {
        return this.valueType;
    }
    public SoftRefFileCache(
            @NonNullable final String name, @NonNullable final Class<V> valueType,
            final int initialCapacity, final float loadFactor, final int concurrencyLevel)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
        CacheFileUtils.inst.mkCacheDirs(this.name);
    }
    public SoftRefFileCache(
            @NonNullable final String name, @NonNullable final Class<V> valueType,
            final int initialCapacity)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap(initialCapacity);
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
        CacheFileUtils.inst.mkCacheDirs(this.name);
    }

    public SoftRefFileCache(@NonNullable final String name,
            @NonNullable final Class<V> valueType)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap();
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
        CacheFileUtils.inst.mkCacheDirs(this.name);
    }
    /** Only an approximation. */
    @Override
    public boolean isEmpty() {
        return this.isMemoryCacheEmpty() && this.isCacheDirEmpty();
    }
    public boolean isMemoryCacheEmpty() {
        this.collector.run();
        return this.map.isEmpty();
    }
    public boolean isCacheDirEmpty() {
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            return CacheFileUtils.inst.isCacheDirEmpty(this.name);
        } finally {
            cacheLock.unlock();
        }
    }
    @Override
    public int size() {
        return Math.max(this.getMemoryCacheSize(), this.getCacheDirSize());
    }
    public int getMemoryCacheSize() {
        this.collector.run();
        return this.map.size();
    }
    public int getCacheDirSize() {
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            return CacheFileUtils.inst.getCacheDirSize(this.name);
        } finally {
            cacheLock.unlock();
        }
    }

    // @tothink: SoftReference.get() doesn't seem to be thread-safe.
    // But do we really want to synchronize upon invoking get() ?
    // It's not thread-safe, but what's the worst consequence ?
    @Override
    public V get(@NonNullable final String key) {
        collector.run();
        if (debug) {
            this.countGet.incrementAndGet();
        }
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            final Lock lock = this.keyedRWLock.readLock(key);
            lock.lock();
            try {
                return doGet(key);
            } finally {
                lock.unlock();
            }
        } finally {
            cacheLock.unlock();
        }
    }
    private V doGet(final String key) {
        final KeyedSoftReference<String,V> ref = map.get(key);
        V val = null;

        if (ref != null) {
            val = ref.get();

            if (debug && val == null) {
                this.countGetEmptyRef.incrementAndGet();
            }
        } else if (debug) {
            this.countGetMissMemory.incrementAndGet();
        }
        if (val == null) {
            // Not in memory.
            if (ref != null) {
                // Rarely gets here, if ever.
                // GC'd.  So try to clean up the key/ref pair.
                this.map.remove(key,  ref);
            }
            final CacheFileContent cfc = CacheFileDAO.inst.readCacheItem(this.name, key);

            if (cfc == null) {
                // Not in file system.
                if (debug) {
                    this.countGetMiss.incrementAndGet();
                }
                return null;
            }
            // Found in file system.
            if (debug) {
                this.countGetHitFile.incrementAndGet();
            }
            val = (V)cfc.deserialize();

            if (val == null) {
                // Corrupted file.  Try remove it from file system.
                if (debug) {
                    this.countGetCorruptedFile.incrementAndGet();
                }
                // Don't think I need to put a read lock on the cache for removal.
                CacheFileDAO.inst.removeCacheItem(this.name, key);
                return null;
            }
            // Resurrect item back to memory.
            map.putIfAbsent(key,
                    new KeyedSoftReference<>(key, val, refq));
        } else if (debug) {
            this.countGetHitMemory.incrementAndGet();
        }
        // cache value exists.
        return val;
    }
    //    private void renewSoftReference(String key, V val) {
    //        if (debug)
    //            log.debug("get: try to refresh the soft reference.");
    //        KeyedSoftRef<V> oldRef =
    //                map.put(key, new KeyedSoftRef<V>(key, val, refq));
    //        // Check for race conditon.
    //        if (oldRef == null) {
    //            // key has just been removed by another thread.
    //            if (debug)
    //                log.debug("get: key has just been removed by another thread.");
    //            return;
    //        }
    //        V oldVal = oldRef.get();
    //        // if oldVal is null, it means the GC just cleared it.
    //        while (oldVal != null && oldVal != val) {
    //            // race condition occurred
    //            // put back the old stuff
    //            if (debug)
    //                log.debug("get: race condition occurred. put back the old stuff");
    //            val = oldVal;
    //            oldRef = map.put(key, oldRef);
    //
    //            if (oldRef == null) {
    //                // key has just been removed by another thread.
    //                if (debug)
    //                    log.debug("get: key has just been removed by another thread.");
    //                oldRef = map.remove(key);
    //
    //                if (oldRef == null) {
    //                    // again, key has just been removed by another thread.
    //                    if (debug)
    //                        log.debug("again: key has just been removed by another thread.");
    //                    break;
    //                }
    //            }
    //            oldVal = oldRef.get();
    //        }
    //        return;
    //    }

    @Override
    public V get(@NonNullable final Object key) {
        return this.get(key.toString());
    }
    @Override
    public V put(@NonNullable final String key, @NonNullable final V value) {
        this.collector.run();

        if (debug) {
            this.countPut.incrementAndGet();
        }
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            final Lock lock = this.keyedRWLock.writeLock(key);
            lock.lock();
            try {
                return doPut(key, value);
            } finally {
                lock.unlock();
            }
        } finally {
            cacheLock.unlock();
        }
    }
    private V doPut(@NonNullable final String key, @NonNullable final V value) {
        final KeyedSoftReference<String,V> oldRef =
                map.put(key, new KeyedSoftReference<>(key, value, refq));
        V ret = null;

        if (oldRef != null) {
            ret = oldRef.get();
            oldRef.clear();

            if (debug) {
                this.countPutClearRef.incrementAndGet();
            }
        }
        if (ret == null) {
            // Not in memory.
            if (debug) {
                this.countPutMissMemory.incrementAndGet();
            }
            if (value instanceof Serializable) {
                // Try the file system.
                if (debug) {
                    this.countPutSerializable.incrementAndGet();
                }
                final CacheFileContent cfc = CacheFileDAO.inst.readCacheItem(this.name, key);

                if (cfc != null) {
                    if (debug) {
                        this.countPutReadFile.incrementAndGet();
                    }
                    ret = (V)cfc.deserialize();
                }
                if (!EqualsUtils.inst.equals(value, ret)) {
                    // Considered new value being put to memory.
                    // So persist to file system.
                    if (debug) {
                        this.countPutNewFileValue.incrementAndGet();
                        this.countPutWriteFile.incrementAndGet();
                    }
                    final byte[] ba = SerializationUtils.serialize((Serializable)value);
                    CacheFileDAO.inst.writeCacheItem(
                        this.name, CacheFileContentType.JAVA_SERIALIZATION, key, ba);
                }
            }
            return ret;
        }
        // ret must be non-null.
        // Found in memory
        if (!EqualsUtils.inst.equals(value, ret)) {
            if (debug) {
                this.countPutNewMemoryValue.incrementAndGet();
            }
            // Different value being put to memory.
            if (value instanceof Serializable) {
                // Persist to file system.
                if (debug) {
                    this.countPutSerializable.incrementAndGet();
                    this.countPutWriteFile.incrementAndGet();
                }
                final byte[] ba = SerializationUtils.serialize((Serializable)value);
                CacheFileDAO.inst.writeCacheItem(
                    this.name, CacheFileContentType.JAVA_SERIALIZATION, key, ba);
            }
        }
        return ret;
    }

    @TODO(
        value="Queue up a flush beans for the key.",
        details="This is useful for synchronizing caches in a cluster environment."
    )
    private void publishFlushKey(@NonNullable final String key) {
    }

    @Override
    public void putAll(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    public V remove(@NonNullable final String key) {
        this.collector.run();

        if (debug) {
            this.countRemove.incrementAndGet();
        }
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            final Lock lock = this.keyedRWLock.writeLock(key);
            lock.lock();
            try {
                return doRemove(key);
            } finally {
                lock.unlock();
            }
        } finally {
            cacheLock.unlock();
        }
    }
    private V doRemove(@NonNullable final String key) {
        final KeyedSoftReference<String,V> oldRef = map.remove(key);
        V ret = null;

        if (oldRef != null) {
            // may exist in memory
            ret = oldRef.get();
            oldRef.clear();
        }
        if (ret == null) {
            // not exist or no longer exist in memory;
            // so check the file system.
            final CacheFileContent cfc = CacheFileDAO.inst.readCacheItem(this.name, key);

            if (cfc == null) {
                // not exist in file system.
                return null;
            }
            if (cfc != null) {
                // If corrupted, invoking deserialize will return null.
                ret = (V)cfc.deserialize();
            }
        }
        // Must exist the file system, corrupted or not.
        // Don't think I need to put a read lock on the cache for removal.
        CacheFileDAO.inst.removeCacheItem(this.name, key);
        return ret;
    }
    @Override
    public V remove(@NonNullable final Object key) {
        return key == null ? null : this.remove(key.toString());
    }
    @Override
    public void clear() {
        for (final String key : this.map.keySet()) {
            this.remove(key);
        }
    }
    @Override
    public @NonNullable Set<String> keySet() {
        final Set<String> kset = map.keySet();
        String[] list = null;
        final Lock cacheLock = CacheManager.inst.readLock(this);
        cacheLock.lock();
        try {
            list = CacheFileUtils.inst.getCacheDirList(this.name);
        } finally {
            cacheLock.unlock();
        }
        if (list != null) {
            kset.addAll(Arrays.asList(list));
        }
        return kset;
    }
    @Override
    @UnsupportedOperation
    public Set<Map.Entry<String,V>> entrySet() {
        throw new UnsupportedOperationException("Only memoryEntrySet and keySet are supported.");
    }
    public @NonNullable Set<Map.Entry<String,V>> memoryEntrySet() {
//        this.collector.run();
        final Set<Map.Entry<String,KeyedSoftReference<String,V>>> fromSet = map.entrySet();
        final Set<Map.Entry<String,V>> toSet = new HashSet<>();

        for (final Map.Entry<String, KeyedSoftReference<String,V>> item : fromSet) {
            final KeyedSoftReference<String,V> ref = item.getValue();
            final V val = ref.get();

            if (val != null) {
                final Map.Entry<String,V> e = new CacheEntry<>(item.getKey(), val);
                toSet.add(e);
            }
        }
        return toSet;
    }
    @Override
    @UnsupportedOperation
    public @NonNullable Collection<V> values() {
        throw new UnsupportedOperationException("Only memoryValues and keySet are supported.");
    }
    public @NonNullable Collection<V> memoryValues() {
        final Collection<KeyedSoftReference<String,V>> fromSet = map.values();
        final List<V> toCol = new ArrayList<>(fromSet.size());

        for (final KeyedSoftReference<String,V> ref : fromSet) {
            final V val = ref.get();

            if (val != null) {
                toCol.add(val);
            }
        }
        return toCol;
    }
    @Override
    public boolean containsKey(@NonNullable final Object key) {
        return this.get(key.toString()) != null;
    }
    @Override
    @UnsupportedOperation
    public boolean containsValue(@NonNullable final Object value) {
        throw new UnsupportedOperationException("Only memoryContainsValue is supported.");
    }
    public boolean memoryContainsValue(@NonNullable final Object value) {
        final Collection<KeyedSoftReference<String,V>> fromSet = map.values();

        for (final KeyedSoftReference<String,V> ref : fromSet) {
            final V val = ref.get();

            if (EqualsUtils.inst.equals(value, val)) {
                return true;
            }
        }
        return false;
    }
    /** Returns the number of Soft References collected by GC. */
//    public int getCollectorCount() {
//        return this.collector.getCount();
//    }
    public void addCacheChangeListener(@NonNullable final ICacheChangeListener<V> listener)
    {
        this.cacheChangeSupport.addCacheChangeListener(listener);
    }
    public void removeCacheChangeListener(@NonNullable final ICacheChangeListener<V> listener)
    {
        this.cacheChangeSupport.removeCacheChangeListener(listener);
    }

    public PerCacheConfig getConfig() {
        return config;
    }

    public void setConfig(final PerCacheConfig config) {
        this.config = config;
    }
    @Override
    @Implements(ICache.class)
    public CacheType getCacheType() {
        return CacheType.SOFT_REFERENCE_FILE;
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("\n").append("name", this.getName())
            .append("\n").append("valueType", this.getValueType().getName())
            .append("\n").append("countGet", this.countGet)
            .append("\n").append("countGetHitMemory", this.countGetHitMemory)
            .append("\n").append("countGetHitFile", this.countGetHitFile)
            .append("\n").append("countGetMissMemory", this.countGetMissMemory)
            .append("\n").append("countGetEmptyRef", this.countGetEmptyRef)
            .append("\n").append("countGetMiss", this.countGetMiss)
            .append("\n").append("countGetCorruptedFile", this.countGetCorruptedFile)
            .append("\n").append("countPut", this.countPut)
            .append("\n").append("countPutClearRef", this.countPutClearRef)
            .append("\n").append("countPutMissMemory", this.countPutMissMemory)
            .append("\n").append("countPutNewFileValue", this.countPutNewFileValue)
            .append("\n").append("countPutNewMemoryValue", this.countPutNewMemoryValue)
            .append("\n").append("countPutReadFile", this.countPutReadFile)
            .append("\n").append("countPutSerializable", this.countPutSerializable)
            .append("\n").append("countPutWriteFile", this.countPutWriteFile)
            .append("\n").append("countRemove", this.countRemove)
            .append("\n").append("collector", this.collector)
            .append("\n").append("keyedRWLock", this.keyedRWLock)
            .toString();
    }
}
