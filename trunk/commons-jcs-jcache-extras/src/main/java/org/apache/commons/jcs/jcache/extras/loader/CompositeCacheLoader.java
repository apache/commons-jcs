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
package org.apache.commons.jcs.jcache.extras.loader;

import org.apache.commons.jcs.jcache.extras.closeable.Closeables;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CompositeCacheLoader<K, V> implements CacheLoader<K, V>, Closeable, Factory<CacheLoader<K, V>>
{
    private final CacheLoader<K, V>[] delegates;

    public CompositeCacheLoader(final CacheLoader<K, V>... delegates)
    {
        this.delegates = delegates;
    }

    @Override
    public V load(final K key) throws CacheLoaderException
    {
        for (final CacheLoader<K, V> delegate : delegates)
        {
            final V v = delegate.load(key);
            if (v != null)
            {
                return v;
            }
        }
        return null;
    }

    @Override
    public Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException
    {
        final Collection<K> list = new ArrayList<K>();
        for (final K k : keys)
        {
            list.add(k);
        }

        final Map<K, V> result = new HashMap<K, V>();
        for (final CacheLoader<K, V> delegate : delegates)
        {
            final Map<K, V> v = delegate.loadAll(list);
            if (v != null)
            {
                result.putAll(v);
                list.removeAll(v.keySet());
                if (list.isEmpty())
                {
                    return v;
                }
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException
    {
        Closeables.close(delegates);
    }

    @Override
    public CacheLoader<K, V> create()
    {
        return this;
    }
}
