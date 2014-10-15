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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
public class CacheEntry<V> implements Map.Entry<String,V> {
    private @NonNullable final String key;
    private @NonNullable V value;
    /** Creates a new instance of CacheEntry */
    public CacheEntry(@NonNullable String key, @NonNullable V val) {
        this.key = key;
        this.value = val;
    }

    public @NonNullable String getKey() {
        return key;
    }

    public @NonNullable V getValue() {
        return value;
    }

    public V setValue(@NonNullable V val) {
        V ret = this.value;
        this.value = val;
        return ret;
    }
    @Override public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override @NonNullable public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
