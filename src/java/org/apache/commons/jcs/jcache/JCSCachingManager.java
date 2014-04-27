package org.apache.commons.jcs.jcache;

import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.jcache.proxy.ClassLoaderAwareHandler;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.jcs.jcache.Asserts.assertNotNull;

public class JCSCachingManager implements CacheManager {
    private final CachingProvider provider;
    private final URI uri;
    private final ClassLoader loader;
    private final Properties properties;
    private final ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<String, Cache<?, ?>>();
    private final CompositeCacheManager instance;
    private volatile boolean closed = false;

    public JCSCachingManager(final CachingProvider provider, final URI uri,
                             final ClassLoader loader, final Properties properties) {
        this.provider = provider;
        this.uri = uri;
        this.loader = loader;
        this.properties = properties;

        if (uri == JCSCachingProvider.DEFAULT_URI && (properties == null || properties.isEmpty())) {
            try {
                instance = CompositeCacheManager.getInstance();
            } catch (final CacheException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            instance = CompositeCacheManager.getUnconfiguredInstance();
            final Properties props = new Properties();
            if (uri != JCSCachingProvider.DEFAULT_URI) {
                InputStream inStream = null;
                try {
                    inStream = uri.toURL().openStream();
                    props.load(inStream);
                } catch (final IOException e) {
                    throw new IllegalArgumentException(e);
                } finally {
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (final IOException e) {
                            // no-op
                        }
                    }
                }
            }
            props.putAll(properties);
            instance.configure(props);
        }
    }

    private void assertNotClosed() {
        if (isClosed()) {
            throw new IllegalStateException("cache manager closed");
        }
    }

    @Override // TODO: use configuration + handle not serializable key/values
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(final String cacheName, final C configuration) throws IllegalArgumentException {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        if (!caches.containsKey(cacheName)) {
            final Cache<K, V> cache = ClassLoaderAwareHandler.newProxy(
                    loader,
                    new JCSCache/*<K, V, C>*/(
                            loader,
                            this,
                            new JCSConfiguration(configuration, configuration.getKeyType(), configuration.getValueType()),
                            instance.getCache(cacheName)),
                    Cache.class
            );
            caches.putIfAbsent(cacheName, cache);
        } else {
            throw new javax.cache.CacheException("cache " + cacheName + " already exists");
        }
        return getCache(cacheName, configuration.getKeyType(), configuration.getValueType());
    }

    @Override
    public void destroyCache(final String cacheName) {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final Cache<?, ?> cache = caches.remove(cacheName);
        instance.freeCache(cacheName, true);
        if (cache != null && !cache.isClosed()) {
            cache.clear();
            cache.close();
            instance.freeCache(cacheName, true);
        }
    }

    @Override
    public void enableManagement(final String cacheName, final boolean enabled) {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final JCSCache<?, ?, ?> cache = getJCSCache(cacheName);
        if (cache != null) {
            if (enabled) {
                cache.enableManagement();
            } else {
                cache.disableManagement();
            }
        }
    }

    private JCSCache<?, ?, ?> getJCSCache(final String cacheName) {
        final Cache<?, ?> cache = caches.get(cacheName);
        return JCSCache.class.cast(ClassLoaderAwareHandler.class.cast(Proxy.getInvocationHandler(cache)).getDelegate());
    }

    @Override
    public void enableStatistics(final String cacheName, final boolean enabled) {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final JCSCache<?, ?, ?> cache = getJCSCache(cacheName);
        if (cache != null) {
            if (enabled) {
                cache.enableStatistics();
            } else {
                cache.disableStatistics();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (isClosed()) {
            return;
        }

        assertNotClosed();
        for (final Cache<?, ?> c : caches.values()) {
            c.close();
        }
        caches.clear();
        closed = true;
        if (JCSCachingProvider.class.isInstance(provider)) {
            JCSCachingProvider.class.cast(provider).remove(this);
        }
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName) {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        return (Cache<K, V>) doGetCache(cacheName, Object.class, Object.class);
    }

    @Override
    public Iterable<String> getCacheNames() {
        return new ImmutableIterable<String>(caches.keySet());
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        try {
            return doGetCache(cacheName, keyType, valueType);
        } catch (final IllegalArgumentException iae) {
            throw new ClassCastException(iae.getMessage());
        }
    }

    private <K, V> Cache<K, V> doGetCache(final String cacheName, final Class<K> keyType, final Class<V> valueType) {
        final Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
        if (cache == null) {
            return null;
        }

        final Configuration<K, V> config = cache.getConfiguration(Configuration.class);
        if (!config.getKeyType().isAssignableFrom(keyType) || !config.getValueType().isAssignableFrom(valueType)) {
            throw new IllegalArgumentException("this cache is <" + config.getKeyType().getName() + ", " + config.getValueType().getName() + "> "
                                            + " and not <" + keyType.getName() + ", " + valueType.getName() + ">");
        }
        return cache;
    }

    @Override
    public CachingProvider getCachingProvider() {
        return provider;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader() {
        return loader;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
