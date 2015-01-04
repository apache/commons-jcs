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
 * Interface of a Cache.
 *
 * @author Hanson Char
 */
@CopyRightApache
@ThreadSafety(ThreadSafetyType.SAFE)
public interface ICache<V> extends Map<String,V> {
    /** Returns the cache name. */
    @ThreadSafety(ThreadSafetyType.IMMUTABLE)
    public @NonNullable String getName();
    /** Returns the value type of the cached items. */
    @ThreadSafety(ThreadSafetyType.IMMUTABLE)
    public @NonNullable Class<V> getValueType();
    /** Returns the value cached for the specified key. */
    @ThreadSafety(
        value=ThreadSafetyType.SAFE,
        caveat="This method itself is thread-safe.  However,"
             + " the thread-safetyness of the return value cannot be guaranteed"
             + " by this interface."
    )
    public V get(String key);
    /** Returns the cache type. */
    @ThreadSafety(ThreadSafetyType.IMMUTABLE)
    public @NonNullable CacheType getCacheType();
}
