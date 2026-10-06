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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import org.apache.commons.jcs.yajcache.lang.annotation.NonNullable;
import org.apache.commons.jcs.yajcache.soft.SoftRefCache;
import org.apache.commons.jcs.yajcache.soft.SoftRefFileCache;

/**
 * Enumerates cache types.
 */
// // http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheType {
    SOFT_REFERENCE {
        @Override
        public <V> ICache<V> createCache(String name, Class<V> valueType)
        {
            return new SoftRefCache<>(name, valueType);
        }
    },
    SOFT_REFERENCE_SAFE {
        @Override
        public <V> ICacheSafe<V> createCache(String name, Class<V> valueType)
        {
            return new SafeCacheWrapper<>(new SoftRefCache<>(name, valueType));
        }
    },
    SOFT_REFERENCE_FILE {
        @Override
        public <V> ICache<V> createCache(String name, Class<V> valueType)
        {
            return new SoftRefFileCache<>(name, valueType);
        }
    },
    SOFT_REFERENCE_FILE_SAFE {
        @Override
        public <V> ICacheSafe<V> createCache(String name, Class<V> valueType)
        {
            return new SafeCacheWrapper<>(new SoftRefFileCache<>(name, valueType));
        }
    };


    /** Instantiates and returns a new instance of cache of the current type. */
    public abstract <V> ICache<V> createCache(final String name, @NonNullable final Class<V> valueType);

    /**
     * Returns true if cache of the given cache type can be used as
     * cache of the current cache type;
     * false otherwise.
     */
    public boolean isAssignableFrom(final CacheType from) {
        switch(this) {
            case SOFT_REFERENCE:
                return true;
            case SOFT_REFERENCE_SAFE:
                switch(from) {
                    case SOFT_REFERENCE_SAFE:
                    case SOFT_REFERENCE_FILE_SAFE:
                        return true;
                    default:
                        return false;
                }
            case SOFT_REFERENCE_FILE:
                switch(from) {
                    case SOFT_REFERENCE_FILE:
                    case SOFT_REFERENCE_FILE_SAFE:
                        return true;
                    default:
                        return false;
                }
            case SOFT_REFERENCE_FILE_SAFE:
                return from == SOFT_REFERENCE_FILE_SAFE;
        }
        throw new AssertionError(this);
    }
}
