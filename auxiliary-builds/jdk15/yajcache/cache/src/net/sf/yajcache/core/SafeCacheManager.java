/* ========================================================================
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 * ========================================================================
 */
/*
 * $Revision$ $Date$ 
 */

package net.sf.yajcache.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.yajcache.soft.SoftRefCacheSafe;
import net.sf.yajcache.annotate.*;

/**
 * @author Hanson Char
 */
public enum SafeCacheManager {
    inst;
    // Cache name to Cache mapping.
    private final ConcurrentMap<String,ICacheSafe> map = 
            new ConcurrentHashMap<String, ICacheSafe>();
    /** 
     * Returns the cache for the specified name and value type;  
     * Creates the cache if necessary.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type.
     */
    public <V> ICacheSafe<V> getCache(String name, Class<V> valueType)
    {
        ICacheSafe c = this.map.get(name);
               
        if (c == null)
            c = this.createCache(name, valueType);
        else
            CacheManagerUtils.inst.checkValueType(c, valueType);
        return c;
    }
    /** 
     * Returns an existing cache for the specified name; or null if not found.
     */
    public ICacheSafe getCache(String name) {
        return this.map.get(name);
    }
    /**
     * Removes the specified cache, if it exists.
     */
    public ICacheSafe removeCache(String name) {
        ICacheSafe c = this.map.remove(name);
        
        if (c != null) {
            c.clear();
        }
        return c;
    }
    /** 
     * Creates the specified cache if not already created.
     * 
     * @return either the cache created by the current thread, or
     * an existing cache created earlier by another thread.
     */
    private <V> ICacheSafe<V> createCache(String name, Class<V> valueType) {
        ICacheSafe c = new SoftRefCacheSafe<V>(name, valueType);
        ICacheSafe old = this.map.putIfAbsent(name, c);

        if (old != null) {
            // race condition: cache already created by another thread.
            CacheManagerUtils.inst.checkValueType(old, valueType);
            c = old;
        }
        return c;
    }

    @TestOnly("Used soley to simluate a race condition during cache creation")
    <V> ICacheSafe<V> testCreateCacheRaceCondition(
            String name, Class<V> valueType)
    {
        return this.createCache(name, valueType);
    }
}
