
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
package org.apache.jcs.yajcache.soft;

import java.io.Serializable;
import java.util.Map;
import org.apache.jcs.yajcache.core.ICacheSafe;
import org.apache.jcs.yajcache.util.BeanUtils;
import org.apache.jcs.yajcache.util.SerializeUtils;
import org.apache.jcs.yajcache.lang.annotation.*;
import org.apache.jcs.yajcache.config.PerCacheConfig;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TODO("Annotate the thread-safetyness of the methods")
public class SoftRefFileCacheSafe<V> extends SoftRefFileCache<V> 
        implements ICacheSafe<V> 
{
    public SoftRefFileCacheSafe(@NonNullable String name, @NonNullable Class<V> valueType, 
            PerCacheConfig config,
            int initialCapacity, float loadFactor, int concurrencyLevel) 
    {
        super(name, valueType, config, initialCapacity, loadFactor, concurrencyLevel);
    }
    public SoftRefFileCacheSafe(@NonNullable String name, @NonNullable Class<V> valueType, 
            PerCacheConfig config,
            int initialCapacity) 
    {
        super(name, valueType, config, initialCapacity);
    }

    public SoftRefFileCacheSafe(@NonNullable String name, 
        @NonNullable Class<V> valueType, PerCacheConfig config) 
    {
        super(name, valueType, config);
    }
    public V getCopy(@NonNullable String key) {
        V val = this.get(key);
        return this.dup(val);
    }
    public V putCopy(@NonNullable String key, @NonNullable V value) {
        return this.put(key, this.dup(value));
    }
    public void putAll(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), e.getValue());
    }
    public void putAllCopies(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), this.dup(e.getValue()));
    }
    public V getBeanCopy(@NonNullable String key) {
        V val = this.get(key);
        return BeanUtils.inst.cloneDeep(val);
    }
    public V putBeanCopy(@NonNullable String key, @NonNullable V value) {
        return this.put(key, BeanUtils.inst.cloneDeep(value));
    }
    public void putAllBeanCopies(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), BeanUtils.inst.cloneDeep(e.getValue()));
    }
    public V getBeanClone(@NonNullable String key) {
        V val = this.get(key);
        return BeanUtils.inst.cloneShallow(val);
    }
    public V putBeanClone(@NonNullable String key, @NonNullable V value) {
        return this.put(key, BeanUtils.inst.cloneShallow(value));
    }
    public void putAllBeanClones(@NonNullable Map<? extends String, ? extends V> map) {
        for (final Map.Entry<? extends String, ? extends V> e : map.entrySet())
            this.put(e.getKey(), BeanUtils.inst.cloneShallow(e.getValue()));
    }
    private V dup(V val) {
        if (val instanceof Serializable) {
            return (V)SerializeUtils.inst.dup((Serializable)val);
        }
        return val;
    }
}
