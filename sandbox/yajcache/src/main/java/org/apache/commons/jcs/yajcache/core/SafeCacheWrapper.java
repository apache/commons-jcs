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
 *
 * @author Hanson Char
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
    public SafeCacheWrapper(@NonNullable ICache<V> cache)
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

    public String getName() {
        return this.cache.getName();
    }
    public Class<V> getValueType() {
        return this.cache.getValueType();
    }
    public V get(String key) {
        return this.cache.get(key);
    }
    public int size() {
        return this.cache.size();
    }
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }
    public boolean containsKey(Object key) {
        return this.cache.containsKey(key);
    }
    public boolean containsValue(Object value) {
        return this.cache.containsValue(value);
    }
    public V get(Object key) {
        return this.cache.get(key);
    }
    public V put(String key, V value) {
        return this.cache.put(key, value);
    }
    public V remove(Object key) {
        return this.cache.remove(key);
    }
    public void clear() {
        this.cache.clear();
    }
    public Set<String> keySet() {
        return this.cache.keySet();
    }
    public Collection<V> values() {
        return this.cache.values();
    }
    public Set<Map.Entry<String, V>> entrySet() {
        return this.cache.entrySet();
    }

    // ICacheSafe implementation

    public V getCopy(@NonNullable String key) {
        V val = this.cache.get(key);
        return this.dup(val);
    }
    public V putCopy(@NonNullable String key, @NonNullable V value) {
        return this.cache.put(key, this.dup(value));
    }
    public void putAll(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.cache.put(e.getKey(), e.getValue());
    }
    public void putAllCopies(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.cache.put(e.getKey(), this.dup(e.getValue()));
    }
    public V getBeanCopy(@NonNullable String key) {
        V val = this.cache.get(key);
        return BeanUtils.inst.cloneDeep(val);
    }
    public V putBeanCopy(@NonNullable String key, @NonNullable V value) {
        return this.cache.put(key, BeanUtils.inst.cloneDeep(value));
    }
    public void putAllBeanCopies(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.cache.put(e.getKey(), BeanUtils.inst.cloneDeep(e.getValue()));
    }
    public V getBeanClone(@NonNullable String key) {
        V val = this.cache.get(key);
        return BeanUtils.inst.cloneShallow(val);
    }
    public V putBeanClone(@NonNullable String key, @NonNullable V value) {
        return this.cache.put(key, BeanUtils.inst.cloneShallow(value));
    }
    public void putAllBeanClones(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.cache.put(e.getKey(), BeanUtils.inst.cloneShallow(e.getValue()));
    }
    private V dup(V val) {
        if (val instanceof Serializable) {
            return (V)SerializeUtils.inst.dup((Serializable)val);
        }
        return val;
    }
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
