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

import java.util.Map;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
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
    public V getCopy(@NonNullable String key);
    /**
     * If the cache value is Serializable, puts a deep clone copy of
     * the given value to the cache.  Else, the behavior is the same as put.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The given value is guaranteed to be thread-safe"
            + " only if the value type is Serializable."
    )
    public V putCopy(@NonNullable String key, @NonNullable V value);
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
    public void putAllCopies(@NonNullable Map<? extends String, ? extends V> map);
    /**
     * Treats the cache value as a Java Bean and returns a deep clone copy of
     * the cached value.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The return value is guaranteed to be thread-safe"
            + " only if the return value is a Java Bean."
    )
    public V getBeanCopy(@NonNullable String key);
    /**
     * Treats the cache value as a Java Bean and puts a deep clone copy of
     * the given value to the cache.
     */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        note="The given value is guaranteed to be thread-safe"
            + " only if the value is a Java Bean."
    )
    public V putBeanCopy(@NonNullable String key, @NonNullable V value);
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
    public void putAllBeanCopies(@NonNullable Map<? extends String, ? extends V> map);
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
    public V getBeanClone(@NonNullable String key);
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
    public V putBeanClone(@NonNullable String key, @NonNullable V value);
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
    public void putAllBeanClones(@NonNullable Map<? extends String, ? extends V> map);
}
