package org.apache.commons.jcs.jcache;

import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.jcache.jmx.JCSCacheMXBean;
import org.apache.commons.jcs.jcache.jmx.JCSCacheStatisticsMXBean;
import org.apache.commons.jcs.jcache.jmx.JMXs;
import org.apache.commons.jcs.jcache.proxy.ExceptionWrapperHandler;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;
import org.apache.commons.jcs.utils.threadpool.ThreadPoolManager;

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
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;

// TODO: get statistics locally correct then correct even if distributed
public class JCSCache<K extends Serializable, V extends Serializable, C extends CompleteConfiguration<K, V>> implements Cache<K, V> {
    private static final String POOL_SIZE_PROPERTY = ThreadPoolManager.PROP_NAME_ROOT + ".size";

    private final CacheAccess<K, JCSElement<V>> delegate;
    private final CacheManager manager;
    private final JCSConfiguration<K, V> config;
    private final CacheLoader<K, V> loader;
    private final CacheWriter<? super K, ? super V> writer;
    private final ExpiryPolicy expiryPolicy;
    private final ObjectName cacheConfigObjectName;
    private final ObjectName cacheStatsObjectName;
    private volatile boolean closed = false;
    private final Map<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>> listeners = new ConcurrentHashMap<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>>();
    private final Statistics statistics = new Statistics();
    private final IElementSerializer serializer = new StandardSerializer();
    private final ExecutorService pool;

    public JCSCache(final ClassLoader classLoader, final CacheManager mgr, final JCSConfiguration<K, V> configuration,
                    final CompositeCache<K, JCSElement<V>> cache, final Properties properties) {
        manager = mgr;
        delegate = new CacheAccess<K, JCSElement<V>>(cache);
        config = configuration;
        pool = properties != null && properties.containsKey(POOL_SIZE_PROPERTY)?
                Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty(POOL_SIZE_PROPERTY)), new ThreadPoolManager.MyThreadFactory()) :
                Executors.newCachedThreadPool(new ThreadPoolManager.MyThreadFactory());

        final Factory<CacheLoader<K, V>> cacheLoaderFactory = configuration.getCacheLoaderFactory();
        if (cacheLoaderFactory == null) {
            loader = NoLoader.INSTANCE;
        } else {
            loader = ExceptionWrapperHandler.newProxy(classLoader, cacheLoaderFactory.create(), CacheLoaderException.class, CacheLoader.class);
        }

        final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory = configuration.getCacheWriterFactory();
        if (cacheWriterFactory == null) {
            writer = NoWriter.INSTANCE;
        } else {
            writer = ExceptionWrapperHandler.newProxy(classLoader, cacheWriterFactory.create(), CacheWriterException.class, CacheWriter.class);
        }

        final Factory<ExpiryPolicy> expiryPolicyFactory = configuration.getExpiryPolicyFactory();
        if (expiryPolicyFactory == null) {
            expiryPolicy = new EternalExpiryPolicy();
        } else {
            expiryPolicy = expiryPolicyFactory.create();
        }

        for (final CacheEntryListenerConfiguration<K, V> listener : config.getCacheEntryListenerConfigurations()) {
            listeners.put(listener, new JCSListener<K, V>(listener));
        }

        statistics.setActive(config.isStatisticsEnabled());

        final String mgrStr = manager.getURI().toString().replaceAll(",|:|=|\n", ".");
        try {
            cacheConfigObjectName = new ObjectName(
                    "javax.cache:type=CacheConfiguration," +
                            "CacheManager=" + mgrStr + "," +
                            "Cache=" + cache.getCacheName()
            );
            cacheStatsObjectName = new ObjectName(
                    "javax.cache:type=CacheStatistics," +
                            "CacheManager=" + mgrStr + "," +
                            "Cache=" + cache.getCacheName()
            );
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
        if (config.isManagementEnabled()) {
            JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
        }
        if (config.isStatisticsEnabled()) {
            JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
        }
    }

    private void assertNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cache closed");
        }
    }

    @Override
    public V get(final K key) {
        assertNotClosed();
        assertNotNull(key, "key");
        return doGetControllingExpiry(key, true, false, false, true);
    }

    private V doLoad(final K key, final boolean update, final boolean propagateLoadException) {
        V v = null;
        try {
            v = loader.load(key);
        } catch (final CacheLoaderException e) {
            if (propagateLoadException) {
                throw e;
            }
        }
        if (v != null) {
            try {
                final Duration duration = update ? expiryPolicy.getExpiryForUpdate() : expiryPolicy.getExpiryForCreation();
                if (duration == null || !duration.isZero()) {
                    delegate.put(key, new JCSElement<V>(v, duration));
                }
            } catch (final CacheException e) {
                throw new IllegalStateException(e);
            }
        }
        return v;
    }

    private void touch(final K key, final JCSElement<V> elt) {
        try {
            delegate.put(key, elt);
        } catch (final CacheException e) {
            // no-op
        }
    }

    @Override
    public Map<K, V> getAll(final Set<? extends K> keys) {
        assertNotClosed();
        for (final K k : keys) {
            assertNotNull(k , "key");
        }

        final Set<K> names = (Set<K>) keys;
        final Map<K, V> result = new HashMap<K, V>();
        for (final Map.Entry<K, ICacheElement<K, JCSElement<V>>> k : delegate.getCacheElements(names).entrySet()) {
            final K key = k.getKey();
            assertNotNull(key, "key");

            final JCSElement<V> elt = k.getValue() != null ? k.getValue().getVal() : null;
            V val = elt != null ? elt.getElement() : null;
            if (val == null && config.isReadThrough()) {
                val = doLoad(key, false, false);
                if (val != null) {
                    result.put(key, val);
                }
            } else if (elt != null) {
                elt.update(expiryPolicy.getExpiryForAccess());
                touch(key, elt);
            }
            result.put(key, val);
        }
        if (config.isReadThrough() && result.size() != keys.size()) {
            for (final K k : keys) {
                if (!result.containsKey(k)) {
                    final V v = doLoad(k, false, false);
                    if (v != null) {
                        result.put(k, v);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(final K key) {
        assertNotClosed();
        assertNotNull(key, "key");
        return delegate.get(key) != null;
    }

    @Override
    public void put(final K key, final V rawValue) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(rawValue, "value");

        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long start = statisticsEnabled ? Times.now() : 0;

        try {
            final JCSElement<V> oldElt = delegate.get(key);
            final V old = oldElt != null? oldElt.getElement() : null;

            final boolean storeByValue = config.isStoreByValue();
            final V value = storeByValue ? copy(rawValue) : rawValue;

            final boolean created = old == null;
            final JCSElement<V> element = new JCSElement<V>(value, created ? expiryPolicy.getExpiryForCreation() : expiryPolicy.getExpiryForUpdate());
            if (element.isExpired()) {
                if (!created) {
                    delegate.remove(key);
                }
            } else {
                writer.write(new JCSEntry<K, V>(key, value));
                delegate.put(storeByValue ? copy(key) : key, element);
                for (final JCSListener<K, V> listener : listeners.values()) {
                    if (created) {
                        listener.onCreated(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                                new JCSCacheEntryEvent<K, V>(this, EventType.CREATED, null, key, value)));
                    } else {
                        listener.onUpdated(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                                new JCSCacheEntryEvent<K, V>(this, EventType.UPDATED, old, key, value)));
                    }
                }

                if (statisticsEnabled) {
                    statistics.increasePuts(1);
                    statistics.addPutTime(System.currentTimeMillis() - start);
                }
            }
        } catch (final CacheException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T extends Serializable> T copy(final T value) {
        try {
            return serializer.deSerialize(serializer.serialize(value));
        } catch (final Exception ioe) {
            throw new IllegalStateException(ioe.getMessage(), ioe);
        }
    }

    @Override
    public V getAndPut(final K key, final V value) {
        final V v = doGetControllingExpiry(key, false, false, true, false);
        put(key, value);
        return v;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        assertNotClosed();
        final TempStateCacheView<K, V> view = new TempStateCacheView<K, V>(this);
        for (final Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            view.put(e.getKey(), e.getValue());
        }
        view.merge();
    }

    @Override
    public boolean putIfAbsent(final K key, final V value) {
        if (!containsKey(key)) {
            put(key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(final K key) {
        assertNotClosed();
        assertNotNull(key, "key");

        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long start = statisticsEnabled ? Times.now() : 0;

        final JCSElement<V> v = delegate.get(key);
        final V value = v != null && v.getElement() != null ? v.getElement() : null;
        writer.delete(key);
        boolean remove = delegate.getCacheControl().remove(key);
        if (v != null && v.isExpired()) {
            remove = false;
        }
        for (final JCSListener<K, V> listener : listeners.values()) {
            listener.onRemoved(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                    new JCSCacheEntryEvent<K, V>(this, EventType.REMOVED, null, key, value)));
        }
        if (remove && statisticsEnabled) {
            statistics.increaseRemovals(1);
            statistics.addRemoveTime(Times.now() - start);
        }
        return remove;
    }

    @Override
    public boolean remove(final K key, final V oldValue) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        final V v = doGetControllingExpiry(key, false, false, false, false);
        final boolean found = v != null;
        if (found) {
            if (v.equals(oldValue)) {
                remove(key);
                return true;
            }
            delegate.get(key).update(expiryPolicy.getExpiryForAccess());
        }
        return false;
    }

    @Override
    public V getAndRemove(final K key) {
        final V v = doGetControllingExpiry(key, false, false, true, false);
        remove(key);
        return v;
    }

    private V doGetControllingExpiry(final K key, final boolean updateAcess,
                                     final boolean forceDoLoad, final boolean skipLoad,
                                     final boolean propagateLoadException) {
        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final long getStart = Times.now();
        final JCSElement<V> elt = delegate.get(key);
        V v = elt != null? elt.getElement() : null;
        if (v == null && (config.isReadThrough() || forceDoLoad)) {
            if (!skipLoad) {
                v = doLoad(key, false, propagateLoadException);
            }
        } else if (statisticsEnabled) {
            if (v != null) {
                statistics.increaseHits(1);
            } else {
                statistics.increaseMisses(1);
            }
        }

        if (updateAcess && elt != null) {
            elt.update(expiryPolicy.getExpiryForAccess());
            if (elt.isExpired()) {
                delegate.getCacheControl().remove(key);
            } else {
                touch(key, elt);
            }
        }
        if (v != null) {
            statistics.addGetTime(Times.now() - getStart);
        }
        return v;
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        assertNotNull(newValue, "newValue");
        final boolean statisticsEnabled = config.isStatisticsEnabled();
        final JCSElement<V> elt = delegate.get(key);
        if (elt != null) {
            V value = elt.getElement();
            if (elt.isExpired()) {
                value = null;
            }
            if (value != null && statisticsEnabled) {
                statistics.increaseHits(1);
            }
            if (value == null && config.isReadThrough()) {
                value = doLoad(key, false, false);
            }
            if (value != null && value.equals(oldValue)) {
                put(key, newValue);
                return true;
            } else if (value != null) {
                elt.update(expiryPolicy.getExpiryForAccess());
                touch(key, elt);
            }
        } else if (statisticsEnabled) {
            statistics.increaseMisses(1);
        }
        return false;
    }

    @Override
    public boolean replace(final K key, final V value) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");
        boolean statisticsEnabled = config.isStatisticsEnabled();
        if (containsKey(key)) {
            if (statisticsEnabled) {
                statistics.increaseHits(1);
            }
            put(key, value);
            return true;
        } else if (statisticsEnabled) {
            statistics.increaseMisses(1);
        }
        return false;
    }

    @Override
    public V getAndReplace(final K key, final V value) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");

        final boolean statisticsEnabled = config.isStatisticsEnabled();

        final JCSElement<V> elt = delegate.get(key);
        if (elt != null) {
            V oldValue = elt.getElement();
            if (oldValue == null && config.isReadThrough()) {
                oldValue = doLoad(key, false, false);
            } else if (statisticsEnabled) {
                statistics.increaseHits(1);
            }
            put(key, value);
            return oldValue;
        } else if (statisticsEnabled) {
            statistics.increaseMisses(1);
        }
        return null;
    }

    @Override
    public void removeAll(final Set<? extends K> keys) {
        assertNotClosed();
        assertNotNull(keys, "keys");
        for (final K k : keys) {
            remove(k);
        }
    }

    @Override
    public void removeAll() {
        removeAll(delegate.getCacheControl().getKeySet());
    }

    @Override
    public void clear() {
        assertNotClosed();
        try {
            delegate.clear();
        } catch (final CacheException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <C2 extends Configuration<K, V>> C2 getConfiguration(final Class<C2> clazz) {
        assertNotClosed();
        return clazz.cast(config);
    }

    @Override
    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues,
                        final CompletionListener completionListener) {
        assertNotClosed();
        assertNotNull(keys, "keys");
        for (final K k : keys) {
            assertNotNull(k, "a key");
        }
        pool.submit(new Runnable() {
            @Override
            public void run() {
                doLoadAll(keys, replaceExistingValues, completionListener);
            }
        });
    }

    private void doLoadAll(final Set<? extends K> keys, final boolean replaceExistingValues,
                           final CompletionListener completionListener) {
        try {
            for (final K k : keys) {
                if (replaceExistingValues) {
                    doLoad(k, containsKey(k), completionListener != null);
                    continue;
                } else if (containsKey(k)) {
                    continue;
                }
                doGetControllingExpiry(k, true, true, false, completionListener != null);
            }
        } catch (final RuntimeException e) {
            if (completionListener != null) {
                completionListener.onException(e);
                return;
            }
        }
        if (completionListener != null) {
            completionListener.onCompletion();
        }
    }

    @Override
    public <T> T invoke(final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) throws EntryProcessorException {
        final TempStateCacheView<K, V> view = new TempStateCacheView<K, V>(this);
        final T t = doInvoke(view, key, entryProcessor, arguments);
        view.merge();
        return t;
    }

    private <T> T doInvoke(final TempStateCacheView<K, V> view, final K key, final EntryProcessor<K, V, T> entryProcessor, final Object... arguments) {
        assertNotClosed();
        assertNotNull(entryProcessor, "entryProcessor");
        assertNotNull(key, "key");
        try {
            if (config.isStatisticsEnabled()) {
                if (containsKey(key)) {
                    statistics.increaseHits(1);
                } else {
                    statistics.increaseMisses(1);
                }
            }
            return entryProcessor.process(new JCSMutableEntry<K, V>(view, key), arguments);
        } catch (final Exception ex) {
            return throwEntryProcessorException(ex);
        }
    }

    private static <T> T throwEntryProcessorException(final Exception ex) {
        if (EntryProcessorException.class.isInstance(ex)) {
            throw EntryProcessorException.class.cast(ex);
        }
        throw new EntryProcessorException(ex);
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(final Set<? extends K> keys,
                                                         final EntryProcessor<K, V, T> entryProcessor,
                                                         final Object... arguments) {
        assertNotClosed();
        assertNotNull(entryProcessor, "entryProcessor");
        final Map<K, EntryProcessorResult<T>> results = new HashMap<K, EntryProcessorResult<T>>();
        for (final K k : keys) {
            try {
                final T invoke = invoke(k, entryProcessor, arguments);
                if (invoke != null) {
                    results.put(k, new EntryProcessorResult<T>() {
                        @Override
                        public T get() throws EntryProcessorException {
                            return invoke;
                        }
                    });
                }
            } catch (final Exception e) {
                results.put(k, new EntryProcessorResult<T>() {
                    @Override
                    public T get() throws EntryProcessorException {
                        return throwEntryProcessorException(e);
                    }
                });
            }
        }
        return results;
    }

    @Override
    public void registerCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        assertNotClosed();
        if (listeners.containsKey(cacheEntryListenerConfiguration)) {
            throw new IllegalArgumentException(cacheEntryListenerConfiguration + " already registered");
        }
        listeners.put(cacheEntryListenerConfiguration, new JCSListener<K, V>(cacheEntryListenerConfiguration));
        config.addListener(cacheEntryListenerConfiguration);
    }

    @Override
    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        assertNotClosed();
        listeners.remove(cacheEntryListenerConfiguration);
        config.removeListener(cacheEntryListenerConfiguration);
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        assertNotClosed();
        final Iterator<K> keys = delegate.getCacheControl().getKeySet().iterator();
        return new Iterator<Entry<K, V>>() {
            private K lastKey = null;

            @Override
            public boolean hasNext() {
                return keys.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                lastKey = keys.next();
                return new JCSEntry<K, V>(lastKey, get(lastKey));
            }

            @Override
            public void remove() {
                if (isClosed() || lastKey == null) {
                    throw new IllegalStateException(isClosed() ? "cache closed" : "call next() before remove()");
                }
                JCSCache.this.remove(lastKey);
            }
        };
    }

    @Override
    public String getName() {
        assertNotClosed();
        return delegate.getCacheControl().getCacheName();
    }

    @Override
    public CacheManager getCacheManager() {
        assertNotClosed();
        return manager;
    }

    @Override
    public synchronized void close() {
        if (isClosed()) {
            return;
        }

        delegate.dispose();
        manager.destroyCache(getName());
        closed = true;
        listeners.clear();
        JMXs.unregister(cacheConfigObjectName);
        JMXs.unregister(cacheStatsObjectName);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        assertNotClosed();
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void enableManagement() {
        config.managementEnabled();
        JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
    }

    public void disableManagement() {
        config.managementDisabled();
        JMXs.unregister(cacheConfigObjectName);
    }

    public void enableStatistics() {
        config.statisticsEnabled();
        statistics.setActive(true);
        JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
    }

    public void disableStatistics() {
        config.statisticsDisabled();
        statistics.setActive(false);
        JMXs.unregister(cacheStatsObjectName);
    }
}
