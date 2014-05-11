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

import org.apache.commons.jcs.jcache.jmx.JCSCacheMXBean;
import org.apache.commons.jcs.jcache.jmx.JCSCacheStatisticsMXBean;
import org.apache.commons.jcs.jcache.jmx.JMXs;
import org.apache.commons.jcs.jcache.lang.Subsitutor;
import org.apache.commons.jcs.jcache.proxy.ExceptionWrapperHandler;
import org.apache.commons.jcs.jcache.spi.CacheEvictor;
import org.apache.commons.jcs.jcache.thread.DaemonThreadFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;
import javax.cache.expiry.Duration;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.management.ObjectName;
import java.io.Closeable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;
import static org.apache.commons.jcs.jcache.serialization.Serializations.copy;

public class JCSCache<K, V, C extends CompleteConfiguration<K, V>> implements Cache<K, V>
{
    private static final Subsitutor SUBSTITUTOR = Subsitutor.Helper.INSTANCE;

    private final ConcurrentMap<JCSKey<K>, JCSElement<V>> delegate;
    private final JCSCachingManager manager;
    private final JCSConfiguration<K, V> config;
    private final CacheLoader<K, V> loader;
    private final CacheWriter<? super K, ? super V> writer;
    private final ExpiryPolicy expiryPolicy;
    private final ObjectName cacheConfigObjectName;
    private final ObjectName cacheStatsObjectName;
    private final String name;
    private final long maxSize;
    private final long maxDelete;
    private final CacheEvictor<K, V> evictor;
    private volatile boolean closed = false;
    private final Map<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>> listeners = new ConcurrentHashMap<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>>();
    private final Statistics statistics = new Statistics();
    private final ExecutorService pool;


    public JCSCache(final ClassLoader classLoader, final JCSCachingManager mgr,
                    final String cacheName,
                    final JCSConfiguration<K, V> configuration,
                    final Properties properties)
    {
        manager = mgr;

        name = cacheName;

        final int capacity = Integer.parseInt(property(properties, cacheName, "capacity", "1000"));
        final float loadFactor = Float.parseFloat(property(properties, cacheName, "loadFactor", "0.75"));
        final int concurrencyLevel = Integer.parseInt(property(properties, cacheName, "concurrencyLevel", "16"));
        delegate = new ConcurrentHashMap<JCSKey<K>, JCSElement<V>>(capacity, loadFactor, concurrencyLevel);

        config = configuration;

        final int poolSize = Integer.parseInt(property(properties, cacheName, "pool.size", "3"));
        final DaemonThreadFactory threadFactory = new DaemonThreadFactory("JCS-JCache-");
        pool = poolSize > 0 ? Executors.newFixedThreadPool(poolSize, threadFactory) : Executors.newCachedThreadPool(threadFactory);

        maxSize = Long.parseLong(property(properties, cacheName, "maxSize", "1000"));
        maxDelete = Long.parseLong(property(properties, cacheName, "maxDeleteByEvictionRun", "100"));
        final long evictionPause = Long.parseLong(properties.getProperty(cacheName + ".evictionPause", properties.getProperty("evictionPause", "30000")));
        final String evictorClass = property(properties, cacheName, "evictor", null);
        if (evictorClass != null)
        {
            try
            {
                evictor = CacheEvictor.class.cast(classLoader.loadClass(evictorClass).newInstance());
            }
            catch (final Exception e)
            {
                throw new IllegalStateException(e);
            }
        }
        else
        {
            evictor = null;
        }
        if (evictionPause > 0)
        {
            pool.submit(new EvictionThread<K, V>(this, evictionPause));
        }

        final Factory<CacheLoader<K, V>> cacheLoaderFactory = configuration.getCacheLoaderFactory();
        if (cacheLoaderFactory == null)
        {
            loader = NoLoader.INSTANCE;
        }
        else
        {
            loader = ExceptionWrapperHandler
                    .newProxy(classLoader, cacheLoaderFactory.create(), CacheLoaderException.class, CacheLoader.class);
        }

        final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory = configuration.getCacheWriterFactory();
        if (cacheWriterFactory == null)
        {
            writer = NoWriter.INSTANCE;
        }
        else
        {
            writer = ExceptionWrapperHandler
                    .newProxy(classLoader, cacheWriterFactory.create(), CacheWriterException.class, CacheWriter.class);
        }

        final Factory<ExpiryPolicy> expiryPolicyFactory = configuration.getExpiryPolicyFactory();
        if (expiryPolicyFactory == null)
        {
            expiryPolicy = new EternalExpiryPolicy();
        }
        else
        {
            expiryPolicy = expiryPolicyFactory.create();
        }

        for (final CacheEntryListenerConfiguration<K, V> listener : config.getCacheEntryListenerConfigurations())
        {
            listeners.put(listener, new JCSListener<K, V>(listener));
        }

        statistics.setActive(config.isStatisticsEnabled());

        final String mgrStr = manager.getURI().toString().replaceAll(",|:|=|\n", ".");
        try
        {
            cacheConfigObjectName = new ObjectName("javax.cache:type=CacheConfiguration,"
                    + "CacheManager=" + mgrStr + "," + "Cache=" + name);
            cacheStatsObjectName = new ObjectName("javax.cache:type=CacheStatistics,"
                    + "CacheManager=" + mgrStr + "," + "Cache=" + name);
        }
        catch (final Exception e)
        {
            throw new IllegalArgumentException(e);
        }
        if (config.isManagementEnabled())
        {
            JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
        }
        if (config.isStatisticsEnabled())
        {
            JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
        }
    }

    private static String property(final Properties properties, final String cacheName, final String name, final String defaultValue)
    {
        final String property = properties.getProperty(cacheName + "." + name, properties.getProperty(name, defaultValue));
        if (property == null)
        {
            return null;
        }
        return SUBSTITUTOR.substitute(property);
    }

    private void assertNotClosed()
    {
        if (isClosed())
        {
            throw new IllegalStateException("cache closed");
        }
    }

    @Override
    public V get(final K key)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        return doGetControllingExpiry(new JCSKey<K>(key), true, false, false, true);
    }

    private V doLoad(final K key, final boolean update, final boolean propagateLoadException)
    {
        V v = null;
        try
        {
            v = loader.load(key);
        }
        catch (final CacheLoaderException e)
        {
            if (propagateLoadException)
            {
                throw e;
            }
        }
        if (v != null)
        {
            final Duration duration = update ? expiryPolicy.getExpiryForUpdate() : expiryPolicy.getExpiryForCreation();
            if (duration == null || !duration.isZero())
            {
                final JCSKey<K> jcsKey = new JCSKey<K>(key);
                jcsKey.access(Times.now(false));
                delegate.put(jcsKey, new JCSElement<V>(v, duration));
                evictIfMaxSize();
            }
        }
        return v;
    }

    private void touch(final JCSKey<K> key, final JCSElement<V> elt)
    {
        if (config.isStoreByValue())
        {
            final Serializable copy = copy(manager.getClassLoader(), Serializable.class.cast(key.getKey()));
            delegate.put(new JCSKey<K>((K) copy), elt);
        }
    }

    @Override
    public Map<K, V> getAll(final Set<? extends K> keys)
    {
        assertNotClosed();
        for (final K k : keys)
        {
            assertNotNull(k, "key");
        }

        final Map<K, V> result = new HashMap<K, V>();
        for (final K key : keys) {
            assertNotNull(key, "key");

            final JCSKey<K> cacheKey = new JCSKey<K>(key);
            final JCSElement<V> elt = delegate.get(cacheKey);
            V val = elt != null ? elt.getElement() : null;
            if (val == null && config.isReadThrough())
            {
                val = doLoad(key, false, false);
                if (val != null)
                {
                    result.put(key, val);
                }
            }
            else if (elt != null)
            {
                elt.update(expiryPolicy.getExpiryForAccess());
                if (elt.isExpired())
                {
                    expires(cacheKey);
                }
                else
                {
                    touch(cacheKey, elt);
                    result.put(key, val);
                }
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(final K key)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        return delegate.get(new JCSKey<K>(key)) != null;
    }

    @Override
    public void put(final K key, final V rawValue)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(rawValue, "value");

        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long start = Times.now(false); // needed for access (eviction)

        final JCSKey<K> cacheKey = new JCSKey<K>(key);
        final JCSElement<V> oldElt = delegate.get(cacheKey);
        final V old = oldElt != null ? oldElt.getElement() : null;

        final boolean storeByValue = config.isStoreByValue();
        final V value = storeByValue ? (V) copy(manager.getClassLoader(), Serializable.class.cast(rawValue)) : rawValue;

        final boolean created = old == null;
        final JCSElement<V> element = new JCSElement<V>(value, created ? expiryPolicy.getExpiryForCreation()
                : expiryPolicy.getExpiryForUpdate());
        if (element.isExpired())
        {
            if (!created)
            {
                expires(cacheKey);
            }
        }
        else
        {
            writer.write(new JCSEntry<K, V>(key, value));
            final JCSKey<K> jcsKey = storeByValue ? new JCSKey<K>((K) copy(manager.getClassLoader(), Serializable.class.cast(key))) : cacheKey;
            jcsKey.access(start);
            delegate.put(jcsKey, element);
            for (final JCSListener<K, V> listener : listeners.values())
            {
                if (created)
                {
                    listener.onCreated(Arrays.<CacheEntryEvent<? extends K, ? extends V>> asList(new JCSCacheEntryEvent<K, V>(this,
                            EventType.CREATED, null, key, value)));
                }
                else
                {
                    listener.onUpdated(Arrays.<CacheEntryEvent<? extends K, ? extends V>> asList(new JCSCacheEntryEvent<K, V>(this,
                            EventType.UPDATED, old, key, value)));
                }
            }

            if (statisticsEnabled)
            {
                statistics.increasePuts(1);
                statistics.addPutTime(System.currentTimeMillis() - start);
            }

            evictIfMaxSize();
        }
    }

    private void expires(final JCSKey<K> cacheKey)
    {
        final JCSElement<V> elt = delegate.remove(cacheKey);
        for (final JCSListener<K, V> listener : listeners.values())
        {
            listener.onExpired(Arrays.<CacheEntryEvent<? extends K, ? extends V>> asList(new JCSCacheEntryEvent<K, V>(this,
                    EventType.REMOVED, null, cacheKey.getKey(), elt.getElement())));
        }
    }

    @Override
    public V getAndPut(final K key, final V value)
    {
        assertNotClosed();
        final V v = doGetControllingExpiry(new JCSKey<K>(key), false, false, true, false);
        put(key, value);
        return v;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map)
    {
        assertNotClosed();
        final TempStateCacheView<K, V> view = new TempStateCacheView<K, V>(this);
        for (final Map.Entry<? extends K, ? extends V> e : map.entrySet())
        {
            view.put(e.getKey(), e.getValue());
        }
        view.merge();
    }

    @Override
    public boolean putIfAbsent(final K key, final V value)
    {
        if (!containsKey(key))
        {
            put(key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(final K key)
    {
        assertNotClosed();
        assertNotNull(key, "key");

        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long start = Times.now(!statisticsEnabled);

        writer.delete(key);
        final JCSKey<K> cacheKey = new JCSKey<K>(key);
        final JCSElement<V> v = delegate.remove(cacheKey);
        final V value = v != null && v.getElement() != null ? v.getElement() : null;
        boolean remove = v != null;
        if (v != null && v.isExpired())
        {
            remove = false;
        }
        for (final JCSListener<K, V> listener : listeners.values())
        {
            listener.onRemoved(Arrays.<CacheEntryEvent<? extends K, ? extends V>> asList(new JCSCacheEntryEvent<K, V>(this,
                    EventType.REMOVED, null, key, value)));
        }
        if (remove && statisticsEnabled)
        {
            statistics.increaseRemovals(1);
            statistics.addRemoveTime(Times.now(false) - start);
        }
        return remove;
    }

    @Override
    public boolean remove(final K key, final V oldValue)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        final JCSKey<K> cacheKey = new JCSKey<K>(key);
        final V v = doGetControllingExpiry(cacheKey, false, false, false, false);
        final boolean found = v != null;
        if (found)
        {
            if (v.equals(oldValue))
            {
                remove(key);
                return true;
            }
            delegate.get(new JCSKey<K>(key)).update(expiryPolicy.getExpiryForAccess());
        }
        return false;
    }

    @Override
    public V getAndRemove(final K key)
    {
        assertNotClosed();
        final V v = doGetControllingExpiry(new JCSKey<K>(key), false, false, true, false);
        remove(key);
        return v;
    }

    private V doGetControllingExpiry(final JCSKey<K> key, final boolean updateAcess, final boolean forceDoLoad, final boolean skipLoad,
            final boolean propagateLoadException)
    {
        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long getStart = Times.now(false);
        final JCSElement<V> elt = delegate.get(key);
        V v = elt != null ? elt.getElement() : null;
        if (v == null && (config.isReadThrough() || forceDoLoad))
        {
            if (!skipLoad)
            {
                v = doLoad(key.getKey(), false, propagateLoadException);
            }
        }
        else if (statisticsEnabled)
        {
            if (v != null)
            {
                statistics.increaseHits(1);
            }
            else
            {
                statistics.increaseMisses(1);
            }
        }

        if (updateAcess && elt != null)
        {
            key.access(getStart);
            elt.update(expiryPolicy.getExpiryForAccess());
            if (elt.isExpired())
            {
                expires(key);
            }
            else
            {
                touch(key, elt);
            }
        }
        if (statisticsEnabled && v != null)
        {
            statistics.addGetTime(Times.now(!statisticsEnabled) - getStart);
        }
        return v;
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        assertNotNull(newValue, "newValue");
        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final JCSKey<K> cacheKey = new JCSKey<K>(key);
        final JCSElement<V> elt = delegate.get(cacheKey);
        if (elt != null)
        {
            V value = elt.getElement();
            if (elt.isExpired())
            {
                value = null;
            }
            if (value != null && statisticsEnabled)
            {
                statistics.increaseHits(1);
            }
            if (value == null && config.isReadThrough())
            {
                value = doLoad(key, false, false);
            }
            if (value != null && value.equals(oldValue))
            {
                put(key, newValue);
                return true;
            }
            else if (value != null)
            {
                elt.update(expiryPolicy.getExpiryForAccess());
                touch(cacheKey, elt);
            }
        }
        else if (statisticsEnabled)
        {
            statistics.increaseMisses(1);
        }
        return false;
    }

    @Override
    public boolean replace(final K key, final V value)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");
        boolean statisticsEnabled = config.isStatisticsEnabled();
        if (containsKey(key))
        {
            if (statisticsEnabled)
            {
                statistics.increaseHits(1);
            }
            put(key, value);
            return true;
        }
        else if (statisticsEnabled)
        {
            statistics.increaseMisses(1);
        }
        return false;
    }

    @Override
    public V getAndReplace(final K key, final V value)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");

        final boolean statisticsEnabled = config.isStatisticsEnabled();

        final JCSKey<K> cacheKey = new JCSKey<K>(key);
        final JCSElement<V> elt = delegate.get(cacheKey);
        if (elt != null)
        {
            V oldValue = elt.getElement();
            if (oldValue == null && config.isReadThrough())
            {
                oldValue = doLoad(key, false, false);
            }
            else if (statisticsEnabled)
            {
                statistics.increaseHits(1);
            }
            put(key, value);
            return oldValue;
        }
        else if (statisticsEnabled)
        {
            statistics.increaseMisses(1);
        }
        return null;
    }

    @Override
    public void removeAll(final Set<? extends K> keys)
    {
        assertNotClosed();
        assertNotNull(keys, "keys");
        for (final K k : keys)
        {
            remove(k);
        }
    }

    @Override
    public void removeAll()
    {
        assertNotClosed();
        for (final JCSKey<K> k : delegate.keySet())
        {
            remove(k.getKey());
        }
    }

    @Override
    public void clear()
    {
        assertNotClosed();
        delegate.clear();
    }

    @Override
    public <C2 extends Configuration<K, V>> C2 getConfiguration(final Class<C2> clazz)
    {
        assertNotClosed();
        return clazz.cast(config);
    }

    @Override
    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues, final CompletionListener completionListener)
    {
        assertNotClosed();
        assertNotNull(keys, "keys");
        for (final K k : keys)
        {
            assertNotNull(k, "a key");
        }
        pool.submit(new Runnable()
        {
            @Override
            public void run()
            {
                doLoadAll(keys, replaceExistingValues, completionListener);
            }
        });
    }

    private void doLoadAll(final Set<? extends K> keys, final boolean replaceExistingValues, final CompletionListener completionListener)
    {
        try
        {
            for (final K k : keys)
            {
                if (replaceExistingValues)
                {
                    doLoad(k, containsKey(k), completionListener != null);
                    continue;
                }
                else if (containsKey(k))
                {
                    continue;
                }
                doGetControllingExpiry(new JCSKey<K>(k), true, true, false, completionListener != null);
            }
        }
        catch (final RuntimeException e)
        {
            if (completionListener != null)
            {
                completionListener.onException(e);
                return;
            }
        }
        if (completionListener != null)
        {
            completionListener.onCompletion();
        }
    }

    @Override
    public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) throws EntryProcessorException
    {
        final TempStateCacheView<K, V> view = new TempStateCacheView<K, V>(this);
        final T t = doInvoke(view, key, entryProcessor, arguments);
        view.merge();
        return t;
    }

    private <T> T doInvoke(final TempStateCacheView<K, V> view, final K key, final EntryProcessor<K, V, T> entryProcessor,
            final Object... arguments)
    {
        assertNotClosed();
        assertNotNull(entryProcessor, "entryProcessor");
        assertNotNull(key, "key");
        try
        {
            if (config.isStatisticsEnabled())
            {
                if (containsKey(key))
                {
                    statistics.increaseHits(1);
                }
                else
                {
                    statistics.increaseMisses(1);
                }
            }
            return entryProcessor.process(new JCSMutableEntry<K, V>(view, key), arguments);
        }
        catch (final Exception ex)
        {
            return throwEntryProcessorException(ex);
        }
    }

    private static <T> T throwEntryProcessorException(final Exception ex)
    {
        if (EntryProcessorException.class.isInstance(ex))
        {
            throw EntryProcessorException.class.cast(ex);
        }
        throw new EntryProcessorException(ex);
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(final Set<? extends K> keys, final EntryProcessor<K, V, T> entryProcessor,
            final Object... arguments)
    {
        assertNotClosed();
        assertNotNull(entryProcessor, "entryProcessor");
        final Map<K, EntryProcessorResult<T>> results = new HashMap<K, EntryProcessorResult<T>>();
        for (final K k : keys)
        {
            try
            {
                final T invoke = invoke(k, entryProcessor, arguments);
                if (invoke != null)
                {
                    results.put(k, new EntryProcessorResult<T>()
                    {
                        @Override
                        public T get() throws EntryProcessorException
                        {
                            return invoke;
                        }
                    });
                }
            }
            catch (final Exception e)
            {
                results.put(k, new EntryProcessorResult<T>()
                {
                    @Override
                    public T get() throws EntryProcessorException
                    {
                        return throwEntryProcessorException(e);
                    }
                });
            }
        }
        return results;
    }

    @Override
    public void registerCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        assertNotClosed();
        if (listeners.containsKey(cacheEntryListenerConfiguration))
        {
            throw new IllegalArgumentException(cacheEntryListenerConfiguration + " already registered");
        }
        listeners.put(cacheEntryListenerConfiguration, new JCSListener<K, V>(cacheEntryListenerConfiguration));
        config.addListener(cacheEntryListenerConfiguration);
    }

    @Override
    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration)
    {
        assertNotClosed();
        listeners.remove(cacheEntryListenerConfiguration);
        config.removeListener(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterator<Entry<K, V>> iterator()
    {
        assertNotClosed();
        final Iterator<JCSKey<K>> keys = new HashSet<JCSKey<K>>(delegate.keySet()).iterator();
        return new Iterator<Entry<K, V>>()
        {
            private K lastKey = null;

            @Override
            public boolean hasNext()
            {
                return keys.hasNext();
            }

            @Override
            public Entry<K, V> next()
            {
                final JCSKey<K> next = keys.next();
                lastKey = next.getKey();
                return new JCSEntry<K, V>(lastKey, get(lastKey));
            }

            @Override
            public void remove()
            {
                if (isClosed() || lastKey == null)
                {
                    throw new IllegalStateException(isClosed() ? "cache closed" : "call next() before remove()");
                }
                JCSCache.this.remove(lastKey);
            }
        };
    }

    @Override
    public String getName()
    {
        assertNotClosed();
        return name;
    }

    @Override
    public CacheManager getCacheManager()
    {
        assertNotClosed();
        return manager;
    }

    @Override
    public synchronized void close()
    {
        if (isClosed())
        {
            return;
        }

        manager.release(getName());
        closed = true;
        pool.shutdownNow();
        close(loader);
        close(writer);
        close(expiryPolicy);
        for (final JCSListener<K, V> listener : listeners.values())
        {
            close(listener);
        }
        listeners.clear();
        JMXs.unregister(cacheConfigObjectName);
        JMXs.unregister(cacheStatsObjectName);
        delegate.clear();
    }

    private static void close(final Object potentiallyCloseable)
    {
        if (Closeable.class.isInstance(potentiallyCloseable))
        {
            Closeable.class.cast(potentiallyCloseable);
        }
    }

    @Override
    public boolean isClosed()
    {
        return closed;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        assertNotClosed();
        if (clazz.isInstance(this))
        {
            return clazz.cast(this);
        }
        if (clazz.isAssignableFrom(Map.class) || clazz.isAssignableFrom(ConcurrentMap.class))
        {
            return clazz.cast(delegate);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }

    public Statistics getStatistics()
    {
        return statistics;
    }

    public void enableManagement()
    {
        config.managementEnabled();
        JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
    }

    public void disableManagement()
    {
        config.managementDisabled();
        JMXs.unregister(cacheConfigObjectName);
    }

    public void enableStatistics()
    {
        config.statisticsEnabled();
        statistics.setActive(true);
        JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
    }

    public void disableStatistics()
    {
        config.statisticsDisabled();
        statistics.setActive(false);
        JMXs.unregister(cacheStatsObjectName);
    }

    private static class EvictionThread<K, V> implements Runnable
    {
        private final long pause;
        private final JCSCache<K, V, ?> cache;

        public EvictionThread(final JCSCache<K, V, ?> cache, final long evictionPause)
        {
            this.cache = cache;
            this.pause = evictionPause;
        }

        @Override
        public void run()
        {
            while (!cache.isClosed())
            {
                cache.evict();
                try
                {
                    Thread.sleep(pause);
                }
                catch (final InterruptedException e)
                {
                    Thread.interrupted();
                    break;
                }
            }
        }
    }

    private void evictIfMaxSize()
    {
        if (delegate.size() > maxSize)
        {
            pool.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    evict();
                }
            });
        }
    }

    private void evict()
    {
        if (isClosed())
        {
            return;
        }

        if (evictor != null)
        {
            evictor.evict(this);
        }
        else
        {
            defaultEviction();
        }
    }

    private void defaultEviction()
    {
        final ConcurrentMap<JCSKey<K>, ? extends JCSElement<?>> map = delegate;
        try
        {
            final TreeSet<JCSKey<K>> treeSet = new TreeSet<JCSKey<K>>(new Comparator<JCSKey<K>>()
            {
                @Override
                public int compare(final JCSKey<K> o1, final JCSKey<K> o2)
                {
                    final long l = o2.lastAccess() - o1.lastAccess(); // inverse
                    if (l == 0)
                    {
                        return o1.hashCode() - o2.hashCode();
                    }
                    return (int) l;
                }
            });
            treeSet.addAll(map.keySet());

            int delete = 0;
            for (final JCSKey<K> key : treeSet)
            {
                if (delete >= maxDelete) {
                    break;
                }
                final JCSElement<?> elt = map.get(key);
                if (elt != null) {
                    if (elt.isExpired())
                    {
                        map.remove(key);
                        statistics.increaseEvictions(1);
                        delete++;
                    }
                }
            }

            if (delete >= maxDelete && maxSize > 0 && map.size() > maxSize)
            {
                for (final JCSKey<K> key : treeSet)
                {
                    if (delete >= maxDelete) {
                        break;
                    }
                    final JCSElement<?> elt = map.get(key);
                    if (elt != null) {
                        map.remove(key);
                        statistics.increaseEvictions(1);
                        delete++;
                    }
                }
            }
        }
        catch (final Exception e)
        {
            // no-op
        }
    }
}
