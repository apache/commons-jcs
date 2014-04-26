package org.apache.commons.jcs.jcache;

import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.jcache.jmx.JCSCacheMXBean;
import org.apache.commons.jcs.jcache.jmx.JCSCacheStatisticsMXBean;
import org.apache.commons.jcs.jcache.jmx.JMXs;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;
import javax.cache.expiry.EternalExpiryPolicy;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;

// TODO: get statistics distributed?
// TODO:: get configuration and CompleteConfiguration to get CacheLoader needed for get()
// TODO:: optimize touch()
public class JCSCache<K extends Serializable, V extends Serializable, C extends CompleteConfiguration<K, V>> implements Cache<K, V> {
    private final CacheAccess<K, JCSElement<V>> delegate;
    private final CacheManager manager;
    private final C config;
    private final CacheLoader<K, V> loader;
    private final CacheWriter<? super K, ? super V> writer;
    private final ExpiryPolicy expiryPolicy;
    private final ObjectName cacheConfigObjectName;
    private final ObjectName cacheStatsObjectName;
    private volatile boolean closed = false;
    private final Map<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>> listeners = new ConcurrentHashMap<CacheEntryListenerConfiguration<K, V>, JCSListener<K, V>>();
    private final Statistics statistics = new Statistics();

    public JCSCache(final CacheManager mgr, final C configuration, final CompositeCache<K, JCSElement<V>> cache) {
        manager = mgr;
        delegate = new CacheAccess<K, JCSElement<V>>(cache);
        config = configuration;

        final Factory<CacheLoader<K, V>> cacheLoaderFactory = configuration.getCacheLoaderFactory();
        if (cacheLoaderFactory == null) {
            loader = NoLoader.INSTANCE;
        } else {
            loader = cacheLoaderFactory.create();
        }

        final Factory<CacheWriter<? super K, ? super V>> cacheWriterFactory = configuration.getCacheWriterFactory();
        if (cacheWriterFactory == null) {
            writer = NoWriter.INSTANCE;
        } else {
            writer = cacheWriterFactory.create();
        }

        final Factory<ExpiryPolicy> expiryPolicyFactory = configuration.getExpiryPolicyFactory();
        if (expiryPolicyFactory == null) {
            expiryPolicy = new EternalExpiryPolicy();
        } else {
            expiryPolicy = expiryPolicyFactory.create();
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
        JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
        JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
    }

    private void registerJMXBeans() {
        try {
            JMXs.register(cacheStatsObjectName, new JCSCacheStatisticsMXBean(statistics));
            JMXs.register(cacheConfigObjectName, new JCSCacheMXBean<K, V>(this));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
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

        final JCSElement<V> elt = delegate.get(key);
        V v = elt != null? elt.getElement() : null;
        if (v == null && config.isReadThrough()) {
            v = doLoad(key);
        } else if (elt != null) {
            elt.update(expiryPolicy.getExpiryForAccess());
            touch(key, elt);
        }

        return v;
    }

    private V doLoad(final K key) {
        final V v = loader.load(key);
        if (v != null) {
            try {
                if (!expiryPolicy.getExpiryForCreation().isZero()) {
                    delegate.put(key, new JCSElement<V>(v, expiryPolicy.getExpiryForCreation()));
                }
            } catch (final CacheException e) {
                throw new IllegalStateException(e);
            }
            writer.write(new JCSEntry<K, V>(key, v));
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

        final Set<K> names = (Set<K>) keys;
        final Map<K, V> result = new HashMap<K, V>();
        for (final Map.Entry<K, ICacheElement<K, JCSElement<V>>> k : delegate.getCacheElements(names).entrySet()) {
            final K key = k.getKey();
            assertNotNull(key, "key");

            final JCSElement<V> elt = k.getValue() != null ? k.getValue().getVal() : null;
            V val = elt != null ? elt.getElement() : null;
            if (val == null && config.isReadThrough()) {
                val = doLoad(key);
                if (val != null) {
                    result.put(key, val);
                }
            } else if (elt != null) {
                elt.update(expiryPolicy.getExpiryForAccess());
                touch(key, elt);
            }
            result.put(key, val);
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
    public void put(final K key, final V value) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");

        try {
            final JCSElement<V> oldElt = delegate.get(key);
            final V old = oldElt != null? oldElt.getElement() : null;

            final boolean created = old == null;
            final JCSElement<V> element = new JCSElement<V>(value, created ? expiryPolicy.getExpiryForCreation() : expiryPolicy.getExpiryForUpdate());
            if (element.isExpired()) {
                delegate.remove(key);
            } else {
                delegate.put(key, element);
                writer.write(new JCSEntry<K, V>(key, value));
                for (final JCSListener<K, V> listener : listeners.values()) {
                    if (created) {
                        listener.onCreated(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                                new JCSCacheEntryEvent<K, V>(this, EventType.CREATED, null, key, value)));
                    } else {
                        listener.onUpdated(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                                new JCSCacheEntryEvent<K, V>(this, EventType.UPDATED, old, key, value)));
                    }
                }
            }

            if (config.isStatisticsEnabled()) {
                statistics.increasePuts(1);
            }
        } catch (final CacheException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public V getAndPut(final K key, final V value) {
        final V v = get(key);
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

        final boolean remove = delegate.getCacheControl().remove(key);
        for (final JCSListener<K, V> listener : listeners.values()) {
            listener.onRemoved(Arrays.<CacheEntryEvent<? extends K, ? extends V>>asList(
                    new JCSCacheEntryEvent<K, V>(this, EventType.REMOVED, null, key, null)));
        }
        return remove;
    }

    @Override
    public boolean remove(final K key, final V oldValue) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        if (containsKey(key) && get(key).equals(oldValue)) {
            remove(key);
            return true;
        }
        return false;
    }

    @Override
    public V getAndRemove(final K key) {
        final V v = get(key);
        remove(key);
        return v;
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(oldValue, "oldValue");
        assertNotNull(newValue, "newValue");
        final JCSElement<V> elt = delegate.get(key);
        if (elt != null) {
            V value = elt.getElement();
            if (value == null && config.isReadThrough()) {
                value = doLoad(key);
            }
            if (value != null && value.equals(oldValue)) {
                put(key, newValue);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean replace(final K key, final V value) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");
        if (containsKey(key)) {
            put(key, value);
            return true;
        }
        return false;
    }

    @Override
    public V getAndReplace(final K key, final V value) {
        assertNotClosed();
        assertNotNull(key, "key");
        assertNotNull(value, "value");

        final JCSElement<V> elt = delegate.get(key);
        if (elt != null) {
            V oldValue = elt.getElement();
            if (oldValue == null && config.isReadThrough()) {
                oldValue = doLoad(key);
            }
            put(key, value);
            return oldValue;
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
        if (config.isStatisticsEnabled()) {
            statistics.increaseRemovals(delegate.getCacheControl().getSize());
        }
        clear();
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
        // TODO: async
        try {
            for (final K k : keys) {
                if (!containsKey(k) || replaceExistingValues) {
                    get(k); // will trigger cacheloader and init
                }
            }
        } catch (final Exception e) {
            if (completionListener != null) {
                completionListener.onException(e);
            }
            return;
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
        final Map<K, EntryProcessorResult<T>> results = new HashMap<K, EntryProcessorResult<T>>();
        for (final K k : keys) {
            try {
                results.put(k, new EntryProcessorResult<T>() {
                    @Override
                    public T get() throws EntryProcessorException {
                        return invoke(k, entryProcessor, arguments);
                    }
                });
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
        listeners.put(cacheEntryListenerConfiguration, new JCSListener<K, V>(cacheEntryListenerConfiguration));
    }

    @Override
    public void deregisterCacheEntryListener(final CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        assertNotClosed();
        listeners.remove(cacheEntryListenerConfiguration);
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
}
