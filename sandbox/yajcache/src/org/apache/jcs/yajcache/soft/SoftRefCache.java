
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
package org.apache.jcs.yajcache.soft;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.yajcache.config.PerCacheConfig;
import org.apache.jcs.yajcache.core.CacheEntry;
import org.apache.jcs.yajcache.core.CacheType;
import org.apache.jcs.yajcache.core.ICache;
import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.jcs.yajcache.lang.ref.KeyedSoftReference;
import org.apache.jcs.yajcache.util.EqualsUtils;


/**
 * Cache implemented using Soft References.
 *
 * @author Hanson Char
 */
@CopyRightApache
@TODO("Annotate the thread-safetyness of the methods")
public class SoftRefCache<V> implements ICache<V> {
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue<V> refq = new ReferenceQueue<V>();
    private final @NonNullable String name;
    private final @NonNullable Class<V> valueType;
    private final @NonNullable ConcurrentMap<String,KeyedSoftReference<String,V>> map;
    private final @NonNullable KeyedRefCollector<String> collector;
    private @NonNullable PerCacheConfig config;

    private AtomicInteger countGet = new AtomicInteger(0);
    private AtomicInteger countGetHitMemory = new AtomicInteger(0);
    private AtomicInteger countGetMiss = new AtomicInteger(0);
    private AtomicInteger countGetEmptyRef = new AtomicInteger(0);
    
    private AtomicInteger countPut = new AtomicInteger(0);
    private AtomicInteger countRemove = new AtomicInteger(0);

    public @NonNullable String getName() {
        return this.name;
    }
    public @NonNullable Class<V> getValueType() {
        return this.valueType;
    }
    public SoftRefCache(@NonNullable String name, @NonNullable Class<V> valueType, 
            int initialCapacity, float loadFactor, int concurrencyLevel) 
    {
        this.map = new ConcurrentHashMap<String,KeyedSoftReference<String,V>>(initialCapacity, loadFactor, concurrencyLevel);
        this.collector = new KeyedRefCollector<String>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }
    public SoftRefCache(
            @NonNullable String name, 
            @NonNullable Class<V> valueType, 
            int initialCapacity) 
    {
        this.map = new ConcurrentHashMap<String,KeyedSoftReference<String,V>>(initialCapacity);
        this.collector = new KeyedRefCollector<String>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }

    public SoftRefCache(
            @NonNullable String name, 
            @NonNullable Class<V> valueType)
    {
        this.map = new ConcurrentHashMap<String,KeyedSoftReference<String,V>>();
        this.collector = new KeyedRefCollector<String>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }

    public boolean isEmpty() {
        this.collector.run();
        return map.isEmpty();
    }

    public int size() {
        this.collector.run();
        return map.size();
    }

    // @tothink: SoftReference.get() doesn't seem to be thread-safe.
    // But do we really want to synchronize upon invoking get() ?
    // It's not thread-safe, but what's the worst consequence ?
    public V get(@NonNullable String key) {
        if (debug)
            this.countGet.incrementAndGet();
        this.collector.run();
        KeyedSoftReference<String,V> ref = map.get(key);
        
        if (ref == null) {
            if (debug)
                this.countGetMiss.incrementAndGet();
            return null;
        }
        V val = ref.get();
        
        if (val == null) {
            // Rarely gets here, if ever.
            // already garbage collected.  So try to clean up the key.
            if (debug)
                this.countGetEmptyRef.incrementAndGet();
            this.map.remove(key,  ref);
//            SoftRefCacheCleaner.inst.cleanupKey(this.map, key);
        }
        // cache value exists.
        // try to refresh the soft reference.
//        this.renewSoftReference(key, val);
        if (debug)
            this.countGetHitMemory.incrementAndGet();
        return val;
    }
//    private void renewSoftReference(String key, V val) {
//        if (debug)
//            log.debug("get: try to refresh the soft reference.");
//        KeyedSoftReference<V> oldRef = 
//                map.put(key, new KeyedSoftReference<V>(key, val, refq));
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
        return key == null ? null : this.get(key.toString());
    }
    public V put(@NonNullable String key, @NonNullable V value) {
        if (debug)
            this.countPut.incrementAndGet();
        this.collector.run();
        KeyedSoftReference<String,V> oldRef = 
                map.put(key, new KeyedSoftReference<String,V>(key, value, refq));
        
        if (oldRef == null)
            return null;
        V ret = oldRef.get();
        oldRef.clear();
        
        if (!EqualsUtils.inst.equals(value, ret)) {
            // value changed for the key
            this.publishFlushKey(key);
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
        if (debug)
            this.countRemove.incrementAndGet();
        this.collector.run();
        KeyedSoftReference<String,V> oldRef = map.remove(key);
        
        if (oldRef == null)
            return null;
        this.publishFlushKey(key);
        V ret = oldRef.get();
        oldRef.clear();
        return ret;
    }
    public V remove(@NonNullable Object key) {
        return key == null ? null : this.remove(key.toString());
    }
    public void clear() {
        this.collector.run();
        map.clear();
    }
    public @NonNullable Set<String> keySet() {
        this.collector.run();
        return map.keySet();
    }
    public @NonNullable Set<Map.Entry<String,V>> entrySet() {
        this.collector.run();
        Set<Map.Entry<String,KeyedSoftReference<String,V>>> fromSet = map.entrySet();
        Set<Map.Entry<String,V>> toSet = new HashSet<Map.Entry<String,V>>();
        
        for (final Map.Entry<String,KeyedSoftReference<String,V>> item : fromSet) {
            KeyedSoftReference<String,V> ref = item.getValue();
            V val = ref.get();
            
            if (val != null) {
                Map.Entry<String,V> e = new CacheEntry<V>(item.getKey(), val);
                toSet.add(e);
            }
        }
        return toSet;
    }
    public @NonNullable Collection<V> values() {
        this.collector.run();
        Collection<KeyedSoftReference<String,V>> fromSet = map.values();
        List<V> toCol = new ArrayList<V>(fromSet.size());
        
        for (final KeyedSoftReference<String,V> ref : fromSet) {
            V val = ref.get();
            
            if (val != null) {
                toCol.add(val);
            }
        }
        return toCol;
    }
    public boolean containsKey(@NonNullable Object key) {
        if (key == null)
            return false;
        return this.get(key.toString()) != null;
    }
    public boolean containsValue(@NonNullable Object value) {
        this.collector.run();
        Collection<KeyedSoftReference<String,V>> fromSet = map.values();
        
        for (final KeyedSoftReference<String,V> ref : fromSet) {
            V val = ref.get();
            
            if (EqualsUtils.inst.equals(value, val))
                return true;
        }
        return false;
    }
    /** Returns the number of Soft References collected by GC. */
    public int getCollectorCount() {
        return this.collector.getCount();
    }

    @NonNullable PerCacheConfig getConfig() {
        return config;
    }

    void setConfig(@NonNullable PerCacheConfig config) {
        this.config = config;
    }
    @Implements(ICache.class)
    public CacheType getCacheType() {
        return CacheType.SOFT_REFERENCE;
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
            .append("\n").append("name", this.getName())
            .append("\n").append("valueType", this.getValueType().getName())
            .append("\n").append("countGet", this.countGet)
            .append("\n").append("countGetEmptyRef", this.countGetEmptyRef)
            .append("\n").append("countGetMiss", this.countGetMiss)
            .append("\n").append("countGetHitMemory", this.countGetHitMemory)
            .append("\n").append("countPut", this.countPut)
            .append("\n").append("countRemove", this.countRemove)
            .append("\n").append("collector", this.collector)
            .toString();
    }
}
