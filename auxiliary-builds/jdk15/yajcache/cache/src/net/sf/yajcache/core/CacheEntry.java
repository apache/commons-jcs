/*
 * CacheEntry.java
 *
 * Created on 17 January 2005, 05:42
 */

package net.sf.yajcache.core;

import java.util.Map;

/**
 *
 * @author Hanson Char
 */
public class CacheEntry<V> implements Map.Entry<String,V> {
    private final String key;
    private V value;
    /** Creates a new instance of CacheEntry */
    public CacheEntry(String key, V val) {
        this.key = key;
        this.value = val;
    }

    public String getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V val) {
        V ret = this.value;
        this.value = val;
        return ret;
    }
    
}
