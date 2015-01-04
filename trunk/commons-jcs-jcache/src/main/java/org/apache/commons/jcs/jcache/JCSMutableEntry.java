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
package org.apache.commons.jcs.jcache;

import javax.cache.Cache;
import javax.cache.processor.MutableEntry;

public class JCSMutableEntry<K, V> implements MutableEntry<K, V>
{
    private final Cache<K, V> cache;
    private final K key;

    public JCSMutableEntry(final Cache<K, V> cache, final K key)
    {
        this.cache = cache;
        this.key = key;
    }

    @Override
    public boolean exists()
    {
        return cache.containsKey(key);
    }

    @Override
    public void remove()
    {
        cache.remove(key);
    }

    @Override
    public void setValue(final V value)
    {
        cache.put(key, value);
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return cache.get(key);
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        if (clazz.isInstance(this))
        {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }
}
