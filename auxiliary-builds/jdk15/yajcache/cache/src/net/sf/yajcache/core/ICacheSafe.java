/*
 * ICacheSafe.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.core;

import java.util.Map;

/**
 *
 * @author Hanson Char
 */
public interface ICacheSafe<V> extends ICache<V> {
    /**
     * If the cache value is Serializable, returns a deep clone copy of
     * the cached value.  Else, the behavior is the same as get.
     */
    public V getCopy(String key);
    /**
     * If the cache value is Serializable, puts a deep clone copy of
     * the given value to the cache.  Else, the behavior is the same as put.
     */
    public V putCopy(String key, V value);
    /**
     * If the cache value is Serializable, puts the deep clone copies of
     * the given values to the cache.  Else, the behavior is the same as putAll.
     */
    public void putAllCopies(Map<? extends String, ? extends V> map);
    /**
     * Treats the cache value as a Java Bean and returns a deep clone copy of
     * the cached value.
     */
    public V getBeanCopy(String key);
    /**
     * Treats the cache value as a Java Bean and puts a deep clone copy of
     * the given value to the cache.
     */
    public V putBeanCopy(String key, V value);
    /**
     * Treats the cache value as a Java Bean and puts the deep clone copies of
     * the given values to the cache.
     */
    public void putAllBeanCopies(Map<? extends String, ? extends V> map);
    /**
     * Treats the cache value as a Java Bean and returns a shallow clone copy of
     * the cached value.
     */
    public V getBeanClone(String key);
    /**
     * Treats the cache value as a Java Bean and puts a shallow clone copy of
     * the given value to the cache.
     */
    public V putBeanClone(String key, V value);
    /**
     * Treats the cache value as a Java Bean and puts the shallow clone copies of
     * the given values to the cache.
     */
    public void putAllBeanClones(Map<? extends String, ? extends V> map);
}
