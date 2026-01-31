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
package org.apache.commons.jcs4.jcache;

import static org.apache.commons.jcs4.jcache.Asserts.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.CompositeCacheConfigurator;
import org.apache.commons.jcs4.engine.control.CompositeCacheManager;
import org.apache.commons.jcs4.jcache.lang.Subsitutor;
import org.apache.commons.jcs4.jcache.proxy.ClassLoaderAwareCache;

public class JCSCachingManager implements CacheManager
{
    private static final class InternalManager extends CompositeCacheManager
    {
        protected static InternalManager create()
        {
            return new InternalManager();
        }

        @Override // needed to call it from JCSCachingManager
        protected void initialize() {
            super.initialize();
        }

        @Override
        protected CompositeCacheConfigurator newConfigurator()
        {
            return new CompositeCacheConfigurator()
            {
                @Override
                protected <K, V> CompositeCache<K, V> newCache(
                        final ICompositeCacheAttributes cca, final IElementAttributes ea)
                {
                    return new ExpiryAwareCache<>( cca, ea );
                }
            };
        }
    }
    private static final Subsitutor SUBSTITUTOR = Subsitutor.Helper.INSTANCE;

    private static final String DEFAULT_CONFIG =
        """
    	jcs.default=DC
    	jcs.default.cacheattributes=org.apache.commons.jcs4.engine.CompositeCacheAttributes
    	jcs.default.cacheattributes.MaxObjects=200001
    	jcs.default.cacheattributes.MemoryCacheName=org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache
    	jcs.default.cacheattributes.UseMemoryShrinker=true
    	jcs.default.cacheattributes.MaxMemoryIdleTimeSeconds=3600
    	jcs.default.cacheattributes.ShrinkerIntervalSeconds=60
    	jcs.default.elementattributes=org.apache.commons.jcs4.engine.ElementAttributes
    	jcs.default.elementattributes.IsEternal=false
    	jcs.default.elementattributes.MaxLife=700
    	jcs.default.elementattributes.IdleTime=1800
    	jcs.default.elementattributes.IsSpool=true
    	jcs.default.elementattributes.IsRemote=true
    	jcs.default.elementattributes.IsLateral=true
    	""";

    private static void addProperties(final URL url, final Properties aggregator)
    {
        try (InputStream inStream = url.openStream()) {
            aggregator.load(inStream);
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    private static Properties readConfig(final URI uri, final ClassLoader loader, final Properties properties) {
        final Properties props = new Properties();
        try {
            if (JCSCachingProvider.DEFAULT_URI.toString().equals(uri.toString()) || uri.toURL().getProtocol().equals("jcs"))
            {

                final Enumeration<URL> resources = loader.getResources(uri.getPath());
                if (!resources.hasMoreElements()) // default
                {
                    props.load(new ByteArrayInputStream(DEFAULT_CONFIG.getBytes(StandardCharsets.UTF_8)));
                }
                else
                {
                    do
                    {
                        addProperties(resources.nextElement(), props);
                    }
                    while (resources.hasMoreElements());
                }
            }
            else
            {
                props.load(uri.toURL().openStream());
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        if (properties != null)
        {
            props.putAll(properties);
        }

        for (final Map.Entry<Object, Object> entry : props.entrySet()) {
            if (entry.getValue() == null)
            {
                continue;
            }
            final String substitute = SUBSTITUTOR.substitute(entry.getValue().toString());
            if (!substitute.equals(entry.getValue()))
            {
                entry.setValue(substitute);
            }
        }
        return props;
    }
    private final CachingProvider provider;
    private final URI uri;
    private final ClassLoader loader;
    private final Properties properties;
    private final ConcurrentMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();
    private final Properties configProperties;

    private volatile boolean closed;

    private final InternalManager delegate = InternalManager.create();

    public JCSCachingManager(final CachingProvider provider, final URI uri, final ClassLoader loader, final Properties properties)
    {
        this.provider = provider;
        this.uri = uri;
        this.loader = loader;
        this.properties = readConfig(uri, loader, properties);
        this.configProperties = properties;

        delegate.setJmxName(CompositeCacheManager.JMX_OBJECT_NAME
                + ",provider=" + provider.hashCode()
                + ",uri=" + uri.toString().replaceAll(",|:|=|\n", ".")
                + ",classloader=" + loader.hashCode()
                + ",properties=" + this.properties.hashCode());
        delegate.initialize();
        delegate.configure(this.properties);
    }

    private void assertNotClosed()
    {
        if (isClosed())
        {
            throw new IllegalStateException("cache manager closed");
        }
    }

    @Override
    public synchronized void close()
    {
        if (isClosed())
        {
            return;
        }

        assertNotClosed();
        for (final Cache<?, ?> c : caches.values())
        {
            c.close();
        }
        caches.clear();
        closed = true;
        if (JCSCachingProvider.class.isInstance(provider))
        {
            JCSCachingProvider.class.cast(provider).remove(this);
        }
        delegate.shutDown();
    }

    @Override
    // TODO: use configuration + handle not serializable key/values
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(final String cacheName, final C configuration)
            throws IllegalArgumentException
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        assertNotNull(configuration, "configuration");
        final Class<K> keyType = configuration.getKeyType();
        final Class<V> valueType = configuration.getValueType();
        if (caches.containsKey(cacheName)) {
            throw new javax.cache.CacheException("cache " + cacheName + " already exists");
        }
        @SuppressWarnings("unchecked")
        final Cache<K, V> cache = ClassLoaderAwareCache.wrap(loader,
                new JCSCache<>(
                        loader, this, cacheName,
                        new JCSConfiguration<>(configuration, keyType, valueType),
                        properties,
                        ExpiryAwareCache.class.cast(delegate.getCache(cacheName))));
        caches.putIfAbsent(cacheName, cache);
        return getCache(cacheName, keyType, valueType);
    }

    @Override
    public void destroyCache(final String cacheName)
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final Cache<?, ?> cache = caches.remove(cacheName);
        if (cache != null && !cache.isClosed())
        {
            cache.clear();
            cache.close();
        }
    }

    private <K, V> Cache<K, V> doGetCache(final String cacheName, final Class<K> keyType, final Class<V> valueType)
    {
        @SuppressWarnings("unchecked") // common map for all caches
        final Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
        if (cache == null)
        {
            return null;
        }

        @SuppressWarnings("unchecked") // don't know how to solve this
        final Configuration<K, V> config = cache.getConfiguration(Configuration.class);
        if (keyType != null && !config.getKeyType().isAssignableFrom(keyType) ||
            valueType != null && !config.getValueType().isAssignableFrom(valueType))
        {
            throw new IllegalArgumentException("this cache is <" + config.getKeyType().getName() + ", " + config.getValueType().getName()
                    + ">  and not <" + keyType.getName() + ", " + valueType.getName() + ">");
        }
        return cache;
    }

    @Override
    public void enableManagement(final String cacheName, final boolean enabled)
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final JCSCache<?, ?> cache = getJCSCache(cacheName);
        if (cache != null)
        {
            if (enabled)
            {
                cache.enableManagement();
            }
            else
            {
                cache.disableManagement();
            }
        }
    }

    @Override
    public void enableStatistics(final String cacheName, final boolean enabled)
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        final JCSCache<?, ?> cache = getJCSCache(cacheName);
        if (cache != null)
        {
            if (enabled)
            {
                cache.enableStatistics();
            }
            else
            {
                cache.disableStatistics();
            }
        }
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName)
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        return (Cache<K, V>) doGetCache(cacheName, Object.class, Object.class);
    }

    @Override
    public <K, V> Cache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType)
    {
        assertNotClosed();
        assertNotNull(cacheName, "cacheName");
        assertNotNull(keyType, "keyType");
        assertNotNull(valueType, "valueType");
        try
        {
            return doGetCache(cacheName, keyType, valueType);
        }
        catch (final IllegalArgumentException iae)
        {
            throw new ClassCastException(iae.getMessage());
        }
    }

    @Override
    public Iterable<String> getCacheNames()
    {
        return new ImmutableIterable<>(caches.keySet());
    }

    @Override
    public CachingProvider getCachingProvider()
    {
        return provider;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return loader;
    }

    private JCSCache<?, ?> getJCSCache(final String cacheName)
    {
        final Cache<?, ?> cache = caches.get(cacheName);
        return JCSCache.class.cast(ClassLoaderAwareCache.getDelegate(cache));
    }

    @Override
    public Properties getProperties()
    {
        return configProperties;
    }

    @Override
    public URI getURI()
    {
        return uri;
    }

    @Override
    public boolean isClosed()
    {
        return closed;
    }

    public void release(final String name) {
        caches.remove(name);
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        if (clazz.isInstance(this))
        {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }
}
