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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.jcs.yajcache.util.BeanUtils;
import org.apache.commons.jcs.yajcache.util.SerializeUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Safe Cache as a wrapper of an underlying cache.
 */
@CopyRightApache
public class SafeCacheWrapper<V> implements ICacheSafe<V>
{
    /** Underlying cache. */
    private final @NonNullable ICache<V> cache;

    private final CacheType cacheType;

    /**
     * Constructs a safe cache by wrapping an underlying cache.
     * @param cache underlying cache.
     */
    public SafeCacheWrapper(@NonNullable final ICache<V> cache)
    {
        this.cache = cache;

        switch(cache.getCacheType()) {
            case SOFT_REFERENCE:
                this.cacheType = CacheType.SOFT_REFERENCE_SAFE;
                break;
            case SOFT_REFERENCE_FILE:
                this.cacheType = CacheType.SOFT_REFERENCE_FILE_SAFE;
                break;
            default:
                throw new AssertionError(this);
        }
    }

    // ICache implementation by delegating to the underlying cache.

    @Override
    public String getName() {
        return this.cache.getName();
    }
    @Override
    public Class<V> getValueType() {
        return this.cache.getValueType();
    }
    @Override
    public V get(final String key) {
        return this.cache.get(key);
    }
    @Override
    public int size() {
        return this.cache.size();
    }
    @Override
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }
    @Override
    public boolean containsKey(final Object key) {
        return this.cache.containsKey(key);
    }
    @Override
    public boolean containsValue(final Object value) {
        return this.cache.containsValue(value);
    }
    @Override
    public V get(final Object key) {
        return this.cache.get(key);
    }
    @Override
    public V put(final String key, final V value) {
        return this.cache.put(key, value);
    }
    @Override
    public V remove(final Object key) {
        return this.cache.remove(key);
    }
    @Override
    public void clear() {
        this.cache.clear();
    }
    @Override
    public Set<String> keySet() {
        return this.cache.keySet();
    }
    @Override
    public Collection<V> values() {
        return this.cache.values();
    }
    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        return this.cache.entrySet();
    }

    // ICacheSafe implementation

    @Override
    public V getCopy(@NonNullable final String key) {
        final V val = this.cache.get(key);
        return this.dup(val);
    }
    @Override
    public V putCopy(@NonNullable final String key, @NonNullable final V value) {
        return this.cache.put(key, this.dup(value));
    }
    @Override
    public void putAll(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.cache.put(e.getKey(), e.getValue());
        }
    }
    @Override
    public void putAllCopies(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.cache.put(e.getKey(), this.dup(e.getValue()));
        }
    }
    @Override
    public V getBeanCopy(@NonNullable final String key) {
        final V val = this.cache.get(key);
        return BeanUtils.inst.cloneDeep(val);
    }
    @Override
    public V putBeanCopy(@NonNullable final String key, @NonNullable final V value) {
        return this.cache.put(key, BeanUtils.inst.cloneDeep(value));
    }
    @Override
    public void putAllBeanCopies(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.cache.put(e.getKey(), BeanUtils.inst.cloneDeep(e.getValue()));
        }
    }
    @Override
    public V getBeanClone(@NonNullable final String key) {
        final V val = this.cache.get(key);
        return BeanUtils.inst.cloneShallow(val);
    }
    @Override
    public V putBeanClone(@NonNullable final String key, @NonNullable final V value) {
        return this.cache.put(key, BeanUtils.inst.cloneShallow(value));
    }
    @Override
    public void putAllBeanClones(@NonNullable final Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet()) {
            this.cache.put(e.getKey(), BeanUtils.inst.cloneShallow(e.getValue()));
        }
    }
    private V dup(final V val) {
        if (val instanceof Serializable) {
            return (V)SerializeUtils.inst.dup((Serializable)val);
        }
        return val;
    }
    @Override
    @Implements(ICache.class)
    public CacheType getCacheType() {
        return this.cacheType;
    }
    @Override public String toString() {
        return new ToStringBuilder(this)
            .append(this.cache)
            .toString();
    }
}
