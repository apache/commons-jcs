/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.util.Map;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jcs.yajcache.annotate.*;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
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
    @Override public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
