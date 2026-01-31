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
package org.apache.commons.jcs4.jcache.extras.loader;

import java.util.HashMap;
import java.util.Map;

import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

public abstract class CacheLoaderAdapter<K, V> implements CacheLoader<K, V>, Factory<CacheLoader<K, V>>
{
    private static final long serialVersionUID = -2683385801194322067L;

    @Override
    public CacheLoader<K, V> create()
    {
        return this;
    }

    @Override
    public Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException
    {
        final Map<K, V> result = new HashMap<>();
        for (final K k : keys)
        {
            final V v = load(k);
            if (v != null)
            {
                result.put(k, v);
            }
        }
        return result;
    }
}
