/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
 */

package org.apache.jcs.yajcache.core;

import org.apache.jcs.yajcache.soft.SoftRefCache;
import org.apache.jcs.yajcache.soft.SoftRefFileCache;

import org.apache.jcs.yajcache.lang.annotation.*;

/**
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheType {
    SOFT_REFERENCE,
    SOFT_REFERENCE_SAFE,
    SOFT_REFERENCE_FILE,
    SOFT_REFERENCE_FILE_SAFE;

    <V> ICache<V> createCache(String name, @NonNullable Class<V> valueType)
    {
        switch(this) {
            case SOFT_REFERENCE:
                return new SoftRefCache<V>(name, valueType);
            case SOFT_REFERENCE_SAFE:
                return new SafeCacheWrapper<V>(new SoftRefCache<V>(name, valueType));
            case SOFT_REFERENCE_FILE:
                return new SoftRefFileCache<V>(name, valueType);
            case SOFT_REFERENCE_FILE_SAFE:
                return new SafeCacheWrapper<V>(new SoftRefFileCache<V>(name, valueType));
        }
        throw new AssertionError(this);
    }
    <V> ICacheSafe<V> createSafeCache(String name, @NonNullable Class<V> valueType)
    {
        switch(this) {
            case SOFT_REFERENCE_SAFE:
                return new SafeCacheWrapper<V>(new SoftRefCache<V>(name, valueType));
            case SOFT_REFERENCE_FILE_SAFE:
                return new SafeCacheWrapper<V>(new SoftRefFileCache<V>(name, valueType));
        }
        throw new UnsupportedOperationException("");
    }
}
