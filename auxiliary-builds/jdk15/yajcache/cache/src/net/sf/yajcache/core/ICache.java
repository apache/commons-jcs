/*
 * ICache.java
 *
 * Created on 17 January 2005, 05:05
 */

package net.sf.yajcache.core;

import java.util.Map;



/**
 * Interface of a Cache.
 *
 * @author Hanson Char
 */
public interface ICache<V> extends Map<String,V> {
    /** Returns the cache name. */
    public String getName();
    /** Returns the value type of the cached items. */
    public Class<V> getValueType();
    /** Returns the value cached for the specified key. */
    public V get(String key);
}
