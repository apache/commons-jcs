
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
package org.apache.jcs.yajcache.soft;

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import org.apache.commons.lang.SerializationUtils;
import org.apache.jcs.yajcache.core.CacheEntry;
import org.apache.jcs.yajcache.core.ICache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.config.PerCacheConfig;
import org.apache.jcs.yajcache.core.ICacheChangeListener;
import org.apache.jcs.yajcache.core.CacheChangeSupport;
import org.apache.jcs.yajcache.file.CacheFileContent;
import org.apache.jcs.yajcache.file.CacheFileContentType;
import org.apache.jcs.yajcache.file.CacheFileDAO;
import org.apache.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.jcs.yajcache.lang.ref.KeyedSoftReference;
import org.apache.jcs.yajcache.util.concurrent.locks.IKeyedReadWriteLock;
import org.apache.jcs.yajcache.util.concurrent.locks.KeyedReadWriteLock;


/**
 * Cache implemented using Soft References.
 *
 * @author Hanson Char
 */
@CopyRightApache
@TODO("Annotate the thread-safetyness of the methods")
public class SoftRefFileCache<V> implements ICache<V> 
{
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue<V> refq = new ReferenceQueue<V>();
    private final @NonNullable String name;
    private final @NonNullable Class<V> valueType;
    private final @NonNullable ConcurrentMap<String, KeyedSoftReference<V>> map;
    private final @NonNullable PerCacheConfig config;

    
    private final @NonNullable KeyedRefCollector collector;
    private final IKeyedReadWriteLock krwl = new KeyedReadWriteLock();
    
//    private final @NonNullable ConcurrentMap<String, CacheOp[]> synMap = 
//            new ConcurrentHashMap<String, CacheOp[]>();
    
    private final @NonNullable CacheChangeSupport<V> cacheChangeSupport = 
            new CacheChangeSupport<V>(this);
    
    private volatile int countGet;
    private volatile int countGetHitMemory;
    private volatile int countGetHitFile;
    private volatile int countGetMiss;
    
    private volatile int countPut;
    private volatile int countRemove;
    
    public String getName() {
        return this.name;
    }
    public Class<V> getValueType() {
        return this.valueType;
    }
    public SoftRefFileCache(
            @NonNullable String name, @NonNullable Class<V> valueType,
            @NonNullable PerCacheConfig config,
            int initialCapacity,float loadFactor, int concurrencyLevel) 
    {
        map = new ConcurrentHashMap<String,KeyedSoftReference<V>>(
                initialCapacity, loadFactor, concurrencyLevel);
        collector = new KeyedRefCollector(refq, map);
        this.name = name;
        this.valueType = valueType;
        this.config = config;
    }
    public SoftRefFileCache(
            @NonNullable String name, @NonNullable Class<V> valueType,
            @NonNullable PerCacheConfig config,
            int initialCapacity) 
    {
        map = new ConcurrentHashMap<String,KeyedSoftReference<V>>(initialCapacity);
        collector = new KeyedRefCollector(refq, map);
        this.name = name;
        this.valueType = valueType;
        this.config = config;
    }
    
    public SoftRefFileCache(@NonNullable String name,
            @NonNullable Class<V> valueType,
            PerCacheConfig config) 
    {
        map = new ConcurrentHashMap<String,KeyedSoftReference<V>>();
        collector = new KeyedRefCollector(refq, map);
        this.name = name;
        this.valueType = valueType;
        this.config = config;
    }
    
    public boolean isEmpty() {
        collector.run();
        return map.isEmpty();
    }
    
    public int size() {
        collector.run();
        return map.size();
    }
    
    // @tothink: SoftReference.get() doesn't seem to be thread-safe.
    // But do we really want to synchronize upon invoking get() ?
    // It's not thread-safe, but what's the worst consequence ?
    public V get(@NonNullable String key) {
        collector.run();
        Lock lock = this.krwl.readLock(key);
        lock.lock();
        try {
            return doGet(key);
        } finally {
            lock.unlock();
        }
    }
    private V doGet(String key) {
        KeyedSoftReference<V> ref = map.get(key);
        V val = null;

        if (ref != null)
            val = ref.get();

        if (val == null) {
            // Not in memory.  
            if (ref != null) {
                // GC'd.  So try to clean up the key/ref pair.
                this.map.remove(key,  ref);
            }
            // Get from the file system.
            CacheFileContent cfc = CacheFileDAO.inst.readCacheItem(this.name, key);
                
            if (cfc == null) {
                // Not in file system.
                return null;
            }
            // Found in file system.
            val = (V)cfc.deserialize();
            
            if (val == null) {
                // Corrupted file.  Try remove it from file system.
                CacheFileDAO.inst.removeCacheItem(this.name, key);
                return null;
            }
            // Resurrect item back to memory.
            map.putIfAbsent(key,
                    new KeyedSoftReference<V>(key, val, refq));
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
    
    public V get(@NonNullable Object key) {
        return this.get(key.toString());
    }
    public V put(@NonNullable String key, @NonNullable V value) {
        this.collector.run();
        Lock lock = this.krwl.writeLock(key);
        lock.lock();
        try {
            return doPut(key, value);
        } finally {
            lock.unlock();
        }
    }
    private V doPut(@NonNullable String key, @NonNullable V value) {
        KeyedSoftReference<V> oldRef =
                map.put(key, new KeyedSoftReference<V>(key, value, refq));
        V ret = null;
        
        if (oldRef != null) {
            ret = oldRef.get();
            oldRef.clear();
        }
        if (ret == null) {
            // Not in memory.
            if (value instanceof Serializable) {
                // Try the file system.
                CacheFileContent cfc =
                        CacheFileDAO.inst.readCacheItem(this.name, key);
                if (cfc != null)
                    ret = (V)cfc.deserialize();
                if (!value.equals(ret)) {
                    // Considered new value being put to memory.  
                    // So persist to file system.
                    byte[] ba = SerializationUtils.serialize((Serializable)value);
                    CacheFileDAO.inst.writeCacheItem(
                        this.name, CacheFileContentType.JAVA_SERIALIZATION, key, ba);
                }
            }
            return ret;
        }
        // ret must be non-null.
        // Found in memory
        if (!value.equals(ret)) {
            // Different value being put to memory.
            if (value instanceof Serializable) {
                // Persist to file system.
                byte[] ba = SerializationUtils.serialize((Serializable)value);
                CacheFileDAO.inst.writeCacheItem(
                    this.name, CacheFileContentType.JAVA_SERIALIZATION, key, ba);
            }
        }
        return ret;
    }

    @TODO(
        value="Queue up a flush event for the key.",
        details="This is useful for synchronizing caches in a cluster environment."
    )
    private void publishFlushKey(@NonNullable String key) {
    }
    
    public void putAll(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), e.getValue());
    }
    public V remove(@NonNullable String key) {
        this.collector.run();
        Lock lock = this.krwl.writeLock(key);
        lock.lock();
        try {
            return doRemove(key);
        } finally {
            lock.unlock();
        }
    }
    private V doRemove(@NonNullable String key) {
        KeyedSoftReference<V> oldRef = map.remove(key);
        V ret = null;

        if (oldRef != null) {
            // may exist in memory
            ret = oldRef.get();
            oldRef.clear();
        }
        if (ret == null) {
            // not exist or no longer exist in memory;
            // so check the file system.
            CacheFileContent cfc =
                    CacheFileDAO.inst.readCacheItem(this.name, key);
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
        CacheFileDAO.inst.removeCacheItem(this.name, key);
        return ret;
    }
    public V remove(@NonNullable Object key) {
        return key == null ? null : this.remove(key.toString());
    }
    public void clear() {
//        this.collector.run();
        map.clear();
        this.cacheChangeSupport.fireCacheClear();
    }
    public @NonNullable Set<String> keySet() {
//        this.collector.run();
        return map.keySet();
    }
    public @NonNullable Set<Map.Entry<String,V>> entrySet() {
//        this.collector.run();
        Set<Map.Entry<String,KeyedSoftReference<V>>> fromSet = map.entrySet();
        Set<Map.Entry<String,V>> toSet = new HashSet<Map.Entry<String,V>>();
        
        for (final Map.Entry<String, KeyedSoftReference<V>> item : fromSet) {
            KeyedSoftReference<V> ref = item.getValue();
            V val = ref.get();
            
            if (val != null) {
                Map.Entry<String,V> e = new CacheEntry<V>(item.getKey(), val);
                toSet.add(e);
            }
        }
        return toSet;
    }
    public @NonNullable Collection<V> values() {
//        this.collector.run();
        Collection<KeyedSoftReference<V>> fromSet = map.values();
        List<V> toCol = new ArrayList<V>(fromSet.size());
        
        for (final KeyedSoftReference<V> ref : fromSet) {
            V val = ref.get();
            
            if (val != null) {
                toCol.add(val);
            }
        }
        return toCol;
    }
    public boolean containsKey(@NonNullable Object key) {
        return this.get(key.toString()) != null;
    }
    public boolean containsValue(@NonNullable Object value) {
//        this.collector.run();
        Collection<KeyedSoftReference<V>> fromSet = map.values();
        
        for (final KeyedSoftReference<V> ref : fromSet) {
            V val = ref.get();
            
            if (value.equals(val))
                return true;
        }
        return false;
    }
    /** Returns the number of Soft References collected by GC. */
//    public int getCollectorCount() {
//        return this.collector.getCount();
//    }
    public void addCacheChangeListener(@NonNullable ICacheChangeListener<V> listener)
    {
        this.cacheChangeSupport.addCacheChangeListener(listener);
    }
    public void removeCacheChangeListener(@NonNullable ICacheChangeListener<V> listener) 
    {
        this.cacheChangeSupport.removeCacheChangeListener(listener);
    }
}
