package org.apache.commons.jcs.yajcache.beans;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.yajcache.core.ICache;
import org.apache.commons.jcs.yajcache.lang.annotation.*;
/**
 */
@CopyRightApache
public class CachePutEvent<V> extends CacheChangeEvent<V> {
    private final @NonNullable String key;
    private final @NonNullable V value;

    public CachePutEvent(@NonNullable final ICache<V> cache,
            @NonNullable final String key, @NonNullable final V value)
    {
        super(cache);
        this.key = key;
        this.value = value;
    }
    public @NonNullable String getKey() {
        return key;
    }
    public @NonNullable V getValue() {
        return value;
    }

    @Override
    public boolean dispatch(@NonNullable final ICacheChangeHandler<V> handler) {
        return handler.handlePut(
                super.getCache().getName(), this.key, this.value);
    }
}
