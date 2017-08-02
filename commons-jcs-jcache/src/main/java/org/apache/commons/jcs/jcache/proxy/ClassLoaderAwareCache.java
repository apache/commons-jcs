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
package org.apache.commons.jcs.jcache.proxy;

import org.apache.commons.jcs.jcache.JCSCache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// don't use a proxy, reflection is too slow here :(
public class ClassLoaderAwareCache<K, V> implements Cache<K, V>
{
    private final ClassLoader loader;
    private final JCSCache<K, V> delegate;

    public ClassLoaderAwareCache(final ClassLoader loader, final JCSCache<K, V> delegate)
    {
        this.loader = loader;
        this.delegate = delegate;
    }

    private ClassLoader before(final Thread thread)
    {
        final ClassLoader tccl = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        return tccl;
    }

    public V get(final K key)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.get(key);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public Map<K, V> getAll(final Set<? extends K> keys)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getAll(keys);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean containsKey(final K key)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.containsKey(key);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void loadAll(final Set<? extends K> keys, boolean replaceExistingValues, final CompletionListener completionListener)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.loadAll(keys, replaceExistingValues, completionListener);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void put(final K key, final V value)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.put(key, value);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public V getAndPut(final K key, final V value)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getAndPut(key, value);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void putAll(final Map<? extends K, ? extends V> map)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.putAll(map);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean putIfAbsent(final K key, final V value)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.putIfAbsent(key, value);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean remove(final K key)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.remove(key);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean remove(final K key, final V oldValue)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.remove(key, oldValue);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public V getAndRemove(final K key)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getAndRemove(key);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean replace(final K key, final V oldValue, final V newValue)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.replace(key, oldValue, newValue);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public boolean replace(final K key, final V value)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.replace(key, value);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public V getAndReplace(final K key, final V value)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getAndReplace(key, value);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void removeAll(final Set<? extends K> keys)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.removeAll(keys);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public void removeAll()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.removeAll();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public void clear()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.clear();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public <C extends Configuration<K, V>> C getConfiguration(final Class<C> clazz)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getConfiguration(clazz);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) throws EntryProcessorException
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.invoke(key, entryProcessor, arguments);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.invokeAll(keys, entryProcessor, arguments);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public String getName()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getName();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public CacheManager getCacheManager()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.getCacheManager();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public void close()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.close();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public boolean isClosed()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.isClosed();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.unwrap(clazz);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void registerCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.registerCacheEntryListener(cacheEntryListenerConfiguration);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            delegate.deregisterCacheEntryListener(cacheEntryListenerConfiguration);
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public Iterator<Entry<K, V>> iterator()
    {
        final Thread thread = Thread.currentThread();
        final ClassLoader loader = before(thread);
        try
        {
            return delegate.iterator();
        }
        finally
        {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (ClassLoaderAwareCache.class.isInstance(obj))
        {
            return delegate.equals(ClassLoaderAwareCache.class.cast(obj).delegate);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    public static <K extends Serializable, V extends Serializable> Cache<K, V> wrap(final ClassLoader loader, final JCSCache<K, V> delegate)
    {
        ClassLoader dontWrapLoader = ClassLoaderAwareCache.class.getClassLoader();
        while (dontWrapLoader != null)
        {
            if (loader == dontWrapLoader)
            {
                return delegate;
            }
            dontWrapLoader = dontWrapLoader.getParent();
        }
        return new ClassLoaderAwareCache<K, V>(loader, delegate);
    }

    public static <K extends Serializable, V extends Serializable> JCSCache<K, V> getDelegate(final Cache<?, ?> cache)
    {
        if (JCSCache.class.isInstance(cache))
        {
            return (JCSCache<K, V>) cache;
        }
        return ((ClassLoaderAwareCache<K, V>) cache).delegate;
    }
}
