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

import java.util.Map;
import net.sf.yajcache.annotate.*;

/**
 *
 * @author Hanson Char
 */
@ThreadSafety(ThreadSafetyType.SAFE)
public interface ICacheSafe<V> extends ICache<V> {
    /**
     * If the cache value is Serializable, returns a deep clone copy of
     * the cached value.  Else, the behavior is the same as get.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The return value is guaranteed to be thread-safe"
            + " only if the return value type is Serializable."
    )
    public V getCopy(String key);
    /**
     * If the cache value is Serializable, puts a deep clone copy of
     * the given value to the cache.  Else, the behavior is the same as put.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The given value is guaranteed to be thread-safe"
            + " only if the value type is Serializable."
    )
    public V putCopy(String key, V value);
    /**
     * If the cache value is Serializable, puts the deep clone copies of
     * the given values to the cache.  Else, the behavior is the same as putAll.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="The thread-safetyness of the given map cannot be guaranteed.",
        note="The given values in the map is guaranteed to be thread-safe"
            + " only if the value type is Serializable."
    )
    public void putAllCopies(Map<? extends String, ? extends V> map);
    /**
     * Treats the cache value as a Java Bean and returns a deep clone copy of
     * the cached value.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The return value is guaranteed to be thread-safe"
            + " only if the return value is a Java Bean."
    )
    public V getBeanCopy(String key);
    /**
     * Treats the cache value as a Java Bean and puts a deep clone copy of
     * the given value to the cache.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The given value is guaranteed to be thread-safe"
            + " only if the value is a Java Bean."
    )
    public V putBeanCopy(String key, V value);
    /**
     * Treats the cache value as a Java Bean and puts the deep clone copies of
     * the given values to the cache.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="The thread-safetyness of the given map cannot be guaranteed" 
             + " by this interface.",
        note="The given values in the map is guaranteed to be thread-safe"
           + " only if the values are Java Beans."
    )
    public void putAllBeanCopies(Map<? extends String, ? extends V> map);
    /**
     * Treats the cache value as a Java Bean and returns a shallow clone copy of
     * the cached value.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="The thread-safetyness of the members of the return value cannot"
            + " be garanteed by this interface.",
        note="The return value is guaranteed to be thread-safe"
            + " only if the return value is a JavaBean."
    )
    public V getBeanClone(String key);
    /**
     * Treats the cache value as a Java Bean and puts a shallow clone copy of
     * the given value to the cache.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="The thread-safetyness of the members of the given value cannot"
            + " be garanteed by this interface.",
        note="The given value is guaranteed to be thread-safe"
            + " only if the value is a Java Bean."
    )
    public V putBeanClone(String key, V value);
    /**
     * Treats the cache value as a Java Bean and puts the shallow clone copies of
     * the given values to the cache.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="The thread-safetyness of the given map cannot be guaranteed" 
             + " by this interface."
             + " Also, the thread-safetyness of the members of each value"
             + " in the map cannot be garanteed.",
        note="The given values in the map is guaranteed to be thread-safe"
           + " only if the values are Java Beans."
    )
    public void putAllBeanClones(Map<? extends String, ? extends V> map);
}
