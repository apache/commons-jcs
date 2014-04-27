package org.apache.commons.jcs.jcache;

import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JCSCachingProvider implements CachingProvider {
    public static final URI DEFAULT_URI = URI.create("jcs://default");

    private final ConcurrentMap<ClassLoader, ConcurrentMap<URI, CacheManager>> cacheManagersByLoader =
                            new ConcurrentHashMap<ClassLoader, ConcurrentMap<URI, CacheManager>>();

    @Override
    public CacheManager getCacheManager(final URI inUri, final ClassLoader inClassLoader, final Properties properties) {
        final URI uri = inUri != null ? inUri : getDefaultURI();
        final ClassLoader classLoader = inClassLoader != null ? inClassLoader : getDefaultClassLoader();

        ConcurrentMap<URI, CacheManager> managers = cacheManagersByLoader.get(classLoader);
        if (managers == null) {
            managers = new ConcurrentHashMap<URI, CacheManager>();
            final ConcurrentMap<URI, CacheManager> existingManagers = cacheManagersByLoader.putIfAbsent(classLoader, managers);
            if (existingManagers != null) {
                managers = existingManagers;
            }
        }

        CacheManager mgr = managers.get(uri);
        if (mgr == null) {
            mgr = new JCSCachingManager(this, uri, classLoader, properties);
            final CacheManager existing = managers.putIfAbsent(uri, mgr);
            if (existing != null) {
                mgr = existing;
            }
        }

        return mgr;
    }

    @Override
    public URI getDefaultURI() {
        return DEFAULT_URI;
    }

    @Override
    public void close() {
        for (final Map<URI, CacheManager> v : cacheManagersByLoader.values()) {
            for (final CacheManager m : v.values()) {
                m.close();
            }
            v.clear();
        }
        cacheManagersByLoader.clear();
    }

    @Override
    public void close(final ClassLoader classLoader) {
        final Map<URI, CacheManager> cacheManagers = cacheManagersByLoader.remove(classLoader);
        if (cacheManagers != null) {
            for (final CacheManager mgr : cacheManagers.values()) {
                mgr.close();
            }
            cacheManagers.clear();
        }
    }

    @Override
    public void close(final URI uri, final ClassLoader classLoader) {
        final Map<URI, CacheManager> cacheManagers = cacheManagersByLoader.remove(classLoader);
        if (cacheManagers != null) {
            final CacheManager mgr = cacheManagers.remove(uri);
            if (mgr != null) {
                mgr.close();
            }
        }
    }

    @Override
    public CacheManager getCacheManager(final URI uri, final ClassLoader classLoader) {
        return getCacheManager(uri, classLoader, getDefaultProperties());
    }

    @Override
    public CacheManager getCacheManager() {
        return getCacheManager(getDefaultURI(), getDefaultClassLoader());
    }

    @Override
    public boolean isSupported(final OptionalFeature optionalFeature) {
        return false;
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return JCSCachingProvider.class.getClassLoader();
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    void remove(final CacheManager mgr) {
        final ClassLoader classLoader = mgr.getClassLoader();
        final Map<URI, CacheManager> mgrs = cacheManagersByLoader.get(classLoader);
        if (mgrs != null) {
            mgrs.remove(mgr.getURI());
            if (mgrs.isEmpty()) {
                cacheManagersByLoader.remove(classLoader);
            }
        }
    }
}
