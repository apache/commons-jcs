/*
 * CacheManager.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import net.sf.yajcache.soft.SoftRefCache;

/**
 * @author Hanson Char
 */
public enum CacheManager {
    inst;
    // Cache name to Cache mapping.
    private final ConcurrentMap<String,ICache<?>> map = 
                new ConcurrentHashMap<String, ICache<?>>();
    /** 
     * Returns the cache for the specified name and value type;  
     * Creates the cache if necessary.
     *
     * @throws ClassCastException if the cache already exists for an
     * incompatible value type.
     */
//    @SuppressWarnings({"unchecked"})
    public <V> ICache<V> getCache(String name, Class<V> valueType)
    {
        ICache c = this.map.get(name);
               
        if (c == null)
            c = this.createCache(name, valueType);
        else
            CacheManagerUtils.inst.checkValueType(c, valueType);
        return c;
    }
    /** 
     * Returns an existing cache for the specified name; or null if not found.
     */
    public ICache getCache(String name) {
        return this.map.get(name);
    }
    /**
     * Removes the specified cache, if it exists.
     */
    public ICache removeCache(String name) {
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
     * an existing cache created earlier by another thread.
     */
//    @SuppressWarnings({"unchecked"})
    private <V> ICache<V> createCache(String name, Class<V> valueType) {
        ICache<V> c = new SoftRefCache<V>(name, valueType);
        ICache old = this.map.putIfAbsent(name, c);

        if (old != null) {
            // race condition: cache already created by another thread.
            CacheManagerUtils.inst.checkValueType(old, valueType);
            c = old;
        }
        return c;
    }
    /**
     * This package private method is used soley to simluate a race condition 
     * during cache creation for testing purposes.
     */
    <V> ICache<V> testCreateCacheRaceCondition(String name, Class<V> valueType) 
    {
        return this.createCache(name, valueType);
    }
}
