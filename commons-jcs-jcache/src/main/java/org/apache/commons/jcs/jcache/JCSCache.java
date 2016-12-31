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

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.jcache.jmx.JCSCacheMXBean;
import org.apache.commons.jcs.jcache.jmx.JCSCacheStatisticsMXBean;
import org.apache.commons.jcs.jcache.jmx.JMXs;
import org.apache.commons.jcs.jcache.proxy.ExceptionWrapperHandler;
import org.apache.commons.jcs.jcache.thread.DaemonThreadFactory;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;
import static org.apache.commons.jcs.jcache.serialization.Serializations.copy;

// TODO: configure serializer
public class JCSCache<K, V> implements Cache<K, V>
{
    private final CompositeCache<K, V> delegate;
    private final JCSCachingManager manager;
    private final JCSConfiguration<K, V> config;
    private final CacheLoader<K, V> loader;
    private final CacheWriter<? super K, ? super V> writer;
    private final ExpiryPolicy expiryPolicy;
    private final ObjectName cacheConfigObjectName;
    private final ObjectName cacheStatsObjectName;
    private final String name;
    private volatile boolean closed = false;
    private final Map<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>> listeners = new ConcurrentHashMap<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>>();
    private final Statistics statistics = new Statistics();
    private final ExecutorService pool;
    private final IElementSerializer serializer; // using json/xml should work as well -> don't force Serializable


    public JCSCache(final ClassLoader classLoader, final JCSCachingManager mgr,
                    final String cacheName, final JCSConfiguration<K, V> configuration,
                    final Properties properties, final CompositeCache<K, V> cache)
    {
        manager = mgr;

        name = cacheName;

        delegate = cache;
        if (delegate.getElementAttributes() == null)
        {
            delegate.setElementAttributes(new ElementAttributes());
        }
        delegate.getElementAttributes().addElementEventHandler(new EvictionListener(statistics));

        config = configuration;

        final int poolSize = Integer.parseInt(property(properties, cacheName, "pool.size", "3"));
        final DaemonThreadFactory threadFactory = new DaemonThreadFactory("JCS-JCache-" + cacheName + "-");
        pool = poolSize > 0 ? Executors.newFixedThreadPool(poolSize, threadFactory) : Executors.newCachedThreadPool(threadFactory);

        try
        {
            serializer = IElementSerializer.class.cast(classLoader.loadClass(property(properties, "serializer", cacheName, StandardSerializer.class.getName())).newInstance());
        }
        catch (final Exception e)
        {
            throw new IllegalArgumentException(e);
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
        final String cacheStr = name.replaceAll(",|:|=|\n", ".");
        try
        {
            cacheConfigObjectName = new ObjectName("javax.cache:type=CacheConfiguration,"
                    + "CacheManager=" + mgrStr + "," + "Cache=" + cacheStr);
            cacheStatsObjectName = new ObjectName("javax.cache:type=CacheStatistics,"
                    + "CacheManager=" + mgrStr + "," + "Cache=" + cacheStr);
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
        return properties.getProperty(cacheName + "." + name, properties.getProperty(name, defaultValue));
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
        final long getStart = Times.now(false);
        return doGetControllingExpiry(getStart, key, true, false, false, true);
    }

    private V doLoad(final K key, final boolean update, final long now, final boolean propagateLoadException)
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
            if (isNotZero(duration))
            {
                final IElementAttributes clone = delegate.getElementAttributes().clone();
                if (ElementAttributes.class.isInstance(clone))
                {
                    ElementAttributes.class.cast(clone).setCreateTime();
                }
                final ICacheElement<K, V> element = updateElement(key, v, duration, clone);
                try
                {
                    delegate.update(element);
                }
                catch (final IOException e)
                {
                    throw new CacheException(e);
                }
            }
        }
        return v;
    }

    private ICacheElement<K, V> updateElement(final K key, final V v, final Duration duration, final IElementAttributes attrs)
    {
        final ICacheElement<K, V> element = new CacheElement<K, V>(name, key, v);
        if (duration != null)
        {
            attrs.setTimeFactorForMilliseconds(1);
            final boolean eternal = duration.isEternal();
            attrs.setIsEternal(eternal);
            if (!eternal)
            {
                attrs.setLastAccessTimeNow();
            }
            // MaxLife = -1 to use IdleTime excepted if jcache.ccf asked for something else
        }
        element.setElementAttributes(attrs);
        return element;
    }

    private void touch(final K key, final ICacheElement<K, V> element)
    {
        if (config.isStoreByValue())
        {
            final K copy = copy(serializer, manager.getClassLoader(), key);
            try
            {
                delegate.update(new CacheElement<K, V>(name, copy, element.getVal(), element.getElementAttributes()));
            }
            catch (final IOException e)
            {
                throw new CacheException(e);
            }
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

        final long now = Times.now(false);
        final Map<K, V> result = new HashMap<K, V>();
        for (final K key : keys) {
            assertNotNull(key, "key");

            final ICacheElement<K, V> elt = delegate.get(key);
            V val = elt != null ? elt.getVal() : null;
            if (val == null && config.isReadThrough())
            {
                val = doLoad(key, false, now, false);
                if (val != null)
                {
                    result.put(key, val);
                }
            }
            else if (elt != null)
            {
                final Duration expiryForAccess = expiryPolicy.getExpiryForAccess();
                if (isNotZero(expiryForAccess))
                {
                    touch(key, elt);
                    result.put(key, val);
                }
                else
                {
                    expires(key);
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
        return delegate.get(key) != null;
    }

    @Override
    public void put(final K key, final V rawValue)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(rawValue, "value");

        final ICacheElement<K, V> oldElt = delegate.get(key);
        final V old = oldElt != null ? oldElt.getVal() : null;

        final boolean storeByValue = config.isStoreByValue();
        final V value = storeByValue ? copy(serializer, manager.getClassLoader(), rawValue) : rawValue;

        final boolean created = old == null;
        final Duration duration = created ? expiryPolicy.getExpiryForCreation() : expiryPolicy.getExpiryForUpdate();
        if (isNotZero(duration))
        {
            final boolean statisticsEnabled = config.isStatisticsEnabled();
            final long start = Times.now(false);

            final K jcsKey = storeByValue ? copy(serializer, manager.getClassLoader(), key) : key;
            final ICacheElement<K, V> element = updateElement( // reuse it to create basic structure
                    jcsKey, value, created ? null : duration,
                    oldElt != null ? oldElt.getElementAttributes() : delegate.getElementAttributes().clone());
            if (created && duration != null) { // set maxLife
                final IElementAttributes copy = element.getElementAttributes();
                copy.setTimeFactorForMilliseconds(1);
                final boolean eternal = duration.isEternal();
                copy.setIsEternal(eternal);
                if (ElementAttributes.class.isInstance(copy)) {
                    ElementAttributes.class.cast(copy).setCreateTime();
                }
                if (!eternal)
                {
                    copy.setIsEternal(false);
                    if (duration == expiryPolicy.getExpiryForAccess())
                    {
                        element.getElementAttributes().setIdleTime(duration.getTimeUnit().toMillis(duration.getDurationAmount()));
                    }
                    else
                        {
                        element.getElementAttributes().setMaxLife(duration.getTimeUnit().toMillis(duration.getDurationAmount()));
                    }
                }
                element.setElementAttributes(copy);
            }
            writer.write(new JCSEntry<K, V>(jcsKey, value));
            try
            {
                delegate.update(element);
            }
            catch (final IOException e)
            {
                throw new CacheException(e);
            }
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
        }
        else
        {
            if (!created)
            {
                expires(key);
            }
        }
    }

    private static boolean isNotZero(final Duration duration)
    {
        return duration == null || !duration.isZero();
    }

    private void expires(final K cacheKey)
    {
        final ICacheElement<K, V> elt = delegate.get(cacheKey);
        delegate.remove(cacheKey);
        for (final JCSListener<K, V> listener : listeners.values())
        {
            listener.onExpired(Arrays.<CacheEntryEvent<? extends K, ? extends V>> asList(new JCSCacheEntryEvent<K, V>(this,
                    EventType.REMOVED, null, cacheKey, elt.getVal())));
        }
    }

    @Override
    public V getAndPut(final K key, final V value)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");
        final long getStart = Times.now(false);
        final V v = doGetControllingExpiry(getStart, key, false, false, true, false);
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
        final K cacheKey = key;

        final ICacheElement<K, V> v = delegate.get(cacheKey);
        delegate.remove(cacheKey);

        final V value = v != null && v.getVal() != null ? v.getVal() : null;
        boolean remove = v != null;
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
        final long getStart = Times.now(false);
        final V v = doGetControllingExpiry(getStart, key, false, false, false, false);
        if (oldValue.equals(v))
        {
            remove(key);
            return true;
        }
        else if (v != null)
        {
            // weird but just for stats to be right (org.jsr107.tck.expiry.CacheExpiryTest.removeSpecifiedEntryShouldNotCallExpiryPolicyMethods())
            expiryPolicy.getExpiryForAccess();
        }
        return false;
    }

    @Override
    public V getAndRemove(final K key)
    {
        assertNotClosed();
        assertNotNull(key, "key");
        final long getStart = Times.now(false);
        final V v = doGetControllingExpiry(getStart, key, false, false, true, false);
        remove(key);
        return v;
    }

    private V doGetControllingExpiry(final long getStart, final K key, final boolean updateAcess, final boolean forceDoLoad, final boolean skipLoad,
            final boolean propagateLoadException)
    {
        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final ICacheElement<K, V> elt = delegate.get(key);
        V v = elt != null ? elt.getVal() : null;
        if (v == null && (config.isReadThrough() || forceDoLoad))
        {
            if (!skipLoad)
            {
                v = doLoad(key, false, getStart, propagateLoadException);
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
            final Duration expiryForAccess = expiryPolicy.getExpiryForAccess();
            if (!isNotZero(expiryForAccess))
            {
                expires(key);
            }
            else if (expiryForAccess != null && (!elt.getElementAttributes().getIsEternal() || !expiryForAccess.isEternal()))
            {
                try
                {
                    delegate.update(updateElement(key, elt.getVal(), expiryForAccess, elt.getElementAttributes()));
                }
                catch (final IOException e)
                {
                    throw new CacheException(e);
                }
            }
        }
        if (statisticsEnabled && v != null)
        {
            statistics.addGetTime(Times.now(false) - getStart);
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
        final ICacheElement<K, V> elt = delegate.get(key);
        if (elt != null)
        {
            V value = elt.getVal();
            if (value != null && statisticsEnabled)
            {
                statistics.increaseHits(1);
            }
            if (value == null && config.isReadThrough())
            {
                value = doLoad(key, false, Times.now(false), false);
            }
            if (value != null && value.equals(oldValue))
            {
                put(key, newValue);
                return true;
            }
            else if (value != null)
            {
                final Duration expiryForAccess = expiryPolicy.getExpiryForAccess();
                if (expiryForAccess != null && (!elt.getElementAttributes().getIsEternal() || !expiryForAccess.isEternal()))
                {
                    try
                    {
                        delegate.update(updateElement(key, elt.getVal(), expiryForAccess, elt.getElementAttributes()));
                    }
                    catch (final IOException e)
                    {
                        throw new CacheException(e);
                    }
                }
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

        final ICacheElement<K, V> elt = delegate.get(key);
        if (elt != null)
        {
            V oldValue = elt.getVal();
            if (oldValue == null && config.isReadThrough())
            {
                oldValue = doLoad(key, false, Times.now(false), false);
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
        for (final K k : delegate.getKeySet())
        {
            remove(k);
        }
    }

    @Override
    public void clear()
    {
        assertNotClosed();
        try
        {
            delegate.removeAll();
        }
        catch (final IOException e)
        {
            throw new CacheException(e);
        }
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
            final long now = Times.now(false);
            for (final K k : keys)
            {
                if (replaceExistingValues)
                {
                    doLoad(k, containsKey(k), now, completionListener != null);
                    continue;
                }
                else if (containsKey(k))
                {
                    continue;
                }
                doGetControllingExpiry(now, k, true, true, false, completionListener != null);
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
        final Iterator<K> keys = new HashSet<K>(delegate.getKeySet()).iterator();
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
                lastKey = keys.next();
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

        for (final Runnable task : pool.shutdownNow()) {
            task.run();
        }

        manager.release(getName());
        closed = true;
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
        try
        {
            delegate.removeAll();
        }
        catch (final IOException e)
        {
            throw new CacheException(e);
        }
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
}
