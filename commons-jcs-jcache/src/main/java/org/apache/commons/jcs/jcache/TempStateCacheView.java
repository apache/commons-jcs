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
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;

// kind of transactional view for a Cache<K, V>, to use with EntryProcessor
public class TempStateCacheView<K, V> implements Cache<K, V>
{
    private final JCSCache<K, V> cache;
    private final Map<K, V> put = new HashMap<K, V>();
    private final Collection<K> remove = new LinkedList<K>();
    private boolean removeAll = false;
    private boolean clear = false;

    public TempStateCacheView(final JCSCache<K, V> entries)
    {
        this.cache = entries;
    }

    public V get(final K key)
    {
        if (ignoreKey(key))
        {
            return null;
        }

        final V v = put.get(key);
        if (v != null)
        {
            return v;
        }

        // for an EntryProcessor we already incremented stats - to enhance
        // surely
        if (cache.getConfiguration(CompleteConfiguration.class).isStatisticsEnabled())
        {
            final Statistics statistics = cache.getStatistics();
            if (cache.containsKey(key))
            {
                statistics.increaseHits(-1);
            }
            else
            {
                statistics.increaseMisses(-1);
            }
        }
        return cache.get(key);
    }

    private boolean ignoreKey(final K key)
    {
        return removeAll || clear || remove.contains(key);
    }

    public Map<K, V> getAll(final Set<? extends K> keys)
    {
        final Map<K, V> v = new HashMap<K, V>(keys.size());
        final Set<K> missing = new HashSet<K>();
        for (final K k : keys)
        {
            final V value = put.get(k);
            if (value != null)
            {
                v.put(k, value);
            }
            else if (!ignoreKey(k))
            {
                missing.add(k);
            }
        }
        if (!missing.isEmpty())
        {
            v.putAll(cache.getAll(missing));
        }
        return v;
    }

    public boolean containsKey(final K key)
    {
        return !ignoreKey(key) && (put.containsKey(key) || cache.containsKey(key));
    }

    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues, final CompletionListener completionListener)
    {
        cache.loadAll(keys, replaceExistingValues, completionListener);
    }

    public void put(final K key, final V value)
    {
        assertNotNull(key, "key");
        assertNotNull(value, "value");
        put.put(key, value);
        remove.remove(key);
    }

    public V getAndPut(final K key, final V value)
    {
        final V v = get(key);
        put(key, value);
        return v;
    }

    public void putAll(final Map<? extends K, ? extends V> map)
    {
        put.putAll(map);
        for (final K k : map.keySet())
        {
            remove.remove(k);
        }
    }

    public boolean putIfAbsent(final K key, final V value)
    {
        if (!put.containsKey(key))
        {
            put.put(key, value);
            remove.remove(key);
            return true;
        }
        return false;
    }

    public boolean remove(final K key)
    {
        final boolean noop = put.containsKey(key);
        put.remove(key);
        if (!ignoreKey(key))
        {
            if (!noop)
            {
                remove.add(key);
            }
            return true;
        }
        return false;
    }

    public boolean remove(final K key, final V oldValue)
    {
        put.remove(key);
        if (!ignoreKey(key) && oldValue.equals(cache.get(key)))
        {
            remove.add(key);
            return true;
        }
        return false;
    }

    public V getAndRemove(final K key)
    {
        final V v = get(key);
        remove.add(key);
        put.remove(key);
        return v;
    }

    public boolean replace(final K key, final V oldValue, final V newValue)
    {
        if (oldValue.equals(get(key)))
        {
            put(key, newValue);
            return true;
        }
        return false;
    }

    public boolean replace(final K key, final V value)
    {
        if (containsKey(key))
        {
            remove(key);
            return true;
        }
        return false;
    }

    public V getAndReplace(final K key, final V value)
    {
        if (containsKey(key))
        {
            final V oldValue = get(key);
            put(key, value);
            return oldValue;
        }
        return null;
    }

    public void removeAll(final Set<? extends K> keys)
    {
        remove.addAll(keys);
        for (final K k : keys)
        {
            put.remove(k);
        }
    }

    @Override
    public void removeAll()
    {
        removeAll = true;
        put.clear();
        remove.clear();
    }

    @Override
    public void clear()
    {
        clear = true;
        put.clear();
        remove.clear();
    }

    public <C extends Configuration<K, V>> C getConfiguration(final Class<C> clazz)
    {
        return cache.getConfiguration(clazz);
    }

    public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) throws EntryProcessorException
    {
        return cache.invoke(key, entryProcessor, arguments);
    }

    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, final EntryProcessor<K, V, T> entryProcessor,
            final Object... arguments)
    {
        return cache.invokeAll(keys, entryProcessor, arguments);
    }

    @Override
    public String getName()
    {
        return cache.getName();
    }

    @Override
    public CacheManager getCacheManager()
    {
        return cache.getCacheManager();
    }

    @Override
    public void close()
    {
        cache.close();
    }

    @Override
    public boolean isClosed()
    {
        return cache.isClosed();
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        return cache.unwrap(clazz);
    }

    public void registerCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        cache.registerCacheEntryListener(cacheEntryListenerConfiguration);
    }

    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        cache.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterator<Entry<K, V>> iterator()
    {
        return cache.iterator();
    }

    public void merge()
    {
        if (removeAll)
        {
            cache.removeAll();
        }
        if (clear)
        {
            cache.clear();
        }

        for (final Map.Entry<K, V> entry : put.entrySet())
        {
            cache.put(entry.getKey(), entry.getValue());
        }
        put.clear();
        for (final K entry : remove)
        {
            cache.remove(entry);
        }
        remove.clear();
    }
}
