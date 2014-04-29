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

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.HashMap;
import java.util.Map;

public class NoLoader<K, V> implements CacheLoader<K, V>
{
    public static final NoLoader INSTANCE = new NoLoader();

    private NoLoader()
    {
        // no-op
    }

    @Override
    public V load(K key) throws CacheLoaderException
    {
        return null;
    }

    @Override
    public Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException
    {
        final Map<K, V> entries = new HashMap<K, V>();
        for (final K k : keys)
        {
            entries.put(k, null);
        }
        return entries;
    }
}
