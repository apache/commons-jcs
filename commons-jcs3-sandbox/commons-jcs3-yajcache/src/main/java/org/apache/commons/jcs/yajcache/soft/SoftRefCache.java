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

import org.apache.commons.jcs.yajcache.config.PerCacheConfig;
import org.apache.commons.jcs.yajcache.core.CacheEntry;
import org.apache.commons.jcs.yajcache.core.CacheType;
import org.apache.commons.jcs.yajcache.core.ICache;
import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.jcs.yajcache.lang.ref.KeyedRefCollector;
import org.apache.commons.jcs.yajcache.lang.ref.KeyedSoftReference;
import org.apache.commons.jcs.yajcache.util.CollectionUtils;
import org.apache.commons.jcs.yajcache.util.EqualsUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

/**
 * Cache implemented using {@link KeyedSoftReference} and {@link ConcurrentHashMap}.
 */
@CopyRightApache
@TODO("Annotate the thread-safetyness of the methods")
public class SoftRefCache<V> implements ICache<V> {
    private static final boolean debug = true;
    private final Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private final @NonNullable ReferenceQueue<V> refq = new ReferenceQueue<>();
    private final @NonNullable String name;
    private final @NonNullable Class<V> valueType;
    private final @NonNullable ConcurrentMap<String,KeyedSoftReference<String,V>> map;
    private final @NonNullable KeyedRefCollector<String> collector;
    private @NonNullable PerCacheConfig config;

    private final AtomicInteger countGet = new AtomicInteger();
    private final AtomicInteger countGetHitMemory = new AtomicInteger();
    private final AtomicInteger countGetMiss = new AtomicInteger();
    private final AtomicInteger countGetEmptyRef = new AtomicInteger();

    private final AtomicInteger countPut = new AtomicInteger();
    private final AtomicInteger countRemove = new AtomicInteger();
    /** Returns the cache name. */
    @Override
    public @NonNullable String getName() {
        return this.name;
    }
    /** Returns the value type of the cache. */
    @Override
    public @NonNullable Class<V> getValueType() {
        return this.valueType;
    }
    public SoftRefCache(@NonNullable final String name, @NonNullable final Class<V> valueType,
            final int initialCapacity, final float loadFactor, final int concurrencyLevel)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }
    public SoftRefCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType,
            final int initialCapacity)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap(initialCapacity);
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }
    public SoftRefCache(
            @NonNullable final String name,
            @NonNullable final Class<V> valueType)
    {
        this.map = CollectionUtils.inst.newConcurrentHashMap();
        this.collector = new KeyedRefCollector<>(refq, map);
        this.name = name;
        this.valueType = valueType;
    }

    @Override
    public boolean isEmpty() {
        this.collector.run();
        return map.isEmpty();
    }

    @Override
    public int size() {
        this.collector.run();
        return map.size();
    }

    @Override
    public V get(@NonNullable final String key) {
        if (debug) {
            this.countGet.incrementAndGet();
        }
        this.collector.run();
        final KeyedSoftReference<String,V> ref = map.get(key);

        if (ref == null) {
            if (debug) {
                this.countGetMiss.incrementAndGet();
            }
            return null;
        }
        final V val = ref.get();

        if (val == null) {
            // Rarely gets here, if ever.
            // already garbage collected.  So try to clean up the key.
            if (debug) {
                this.countGetEmptyRef.incrementAndGet();
            }
            this.map.remove(key,  ref);
        }
        // cache value exists.
        // try to refresh the soft reference.
        if (debug) {
            this.countGetHitMemory.incrementAndGet();
        }
        return val;
    }

    @Override
    public V get(@NonNullable final Object key) {
        return key == null ? null : this.get(key.toString());
    }
    @Override
    public V put(@NonNullable final String key, @NonNullable final V value) {
        if (debug) {
            this.countPut.incrementAndGet();
        }
        this.collector.run();
        final KeyedSoftReference<String,V> oldRef =
                map.put(key, new KeyedSoftReference<>(key, value, refq));

        if (oldRef == null) {
            return null;
        }
        final V ret = oldRef.get();
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
    private void publishFlushKey(@NonNullable final String key) {
    }

    @Override
    public void putAll(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    public V remove(@NonNullable final String key) {
        if (debug) {
            this.countRemove.incrementAndGet();
        }
        this.collector.run();
        final KeyedSoftReference<String,V> oldRef = map.remove(key);

        if (oldRef == null) {
            return null;
        }
        this.publishFlushKey(key);
        final V ret = oldRef.get();
        oldRef.clear();
        return ret;
    }
    @Override
    public V remove(@NonNullable final Object key) {
        return key == null ? null : this.remove(key.toString());
    }
    @Override
    public void clear() {
        this.collector.run();
        map.clear();
    }
    @Override
    public @NonNullable Set<String> keySet() {
        this.collector.run();
        return map.keySet();
    }
    @Override
    public @NonNullable Set<Map.Entry<String,V>> entrySet() {
        this.collector.run();
        final Set<Map.Entry<String,KeyedSoftReference<String,V>>> fromSet = map.entrySet();
        final Set<Map.Entry<String,V>> toSet = new HashSet<>();

        for (final Map.Entry<String,KeyedSoftReference<String,V>> item : fromSet) {
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
    public @NonNullable Collection<V> values() {
        this.collector.run();
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
        if (key == null) {
            return false;
        }
        return this.get(key.toString()) != null;
    }
    @Override
    public boolean containsValue(@NonNullable final Object value) {
        this.collector.run();
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
    public int getCollectorCount() {
        return this.collector.getCount();
    }

    @NonNullable PerCacheConfig getConfig() {
        return config;
    }

    void setConfig(@NonNullable final PerCacheConfig config) {
        this.config = config;
    }
    @Override
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
