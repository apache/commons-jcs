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
package org.apache.commons.jcs3.jcache;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

public class JCSCacheEntryEvent<K, V> extends CacheEntryEvent<K, V>
{
    /** Serial version */
    private static final long serialVersionUID = 4761272981003897488L;

    private final V old;
    private final K key;
    private final V value;

    public JCSCacheEntryEvent(final Cache<K, V> source, final EventType eventType, final V old, final K key, final V value)
    {
        super(source, eventType);
        this.old = old;
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getOldValue()
    {
        return old;
    }

    @Override
    public V getValue()
    {
        return value;
    }

    @Override
    public boolean isOldValueAvailable()
    {
        return old != null;
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
