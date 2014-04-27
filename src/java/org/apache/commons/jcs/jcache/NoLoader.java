package org.apache.commons.jcs.jcache;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.HashMap;
import java.util.Map;

public class NoLoader<K, V> implements CacheLoader<K, V> {
    public static final NoLoader INSTANCE = new NoLoader();

    private NoLoader() {
        // no-op
    }

    @Override
    public V load(K key) throws CacheLoaderException {
        return null;
    }

    @Override
    public Map<K, V> loadAll(final Iterable<? extends K> keys) throws CacheLoaderException {
        final Map<K, V> entries = new HashMap<K, V>();
        for (final K k : keys) {
            entries.put(k, null);
        }
        return entries;
    }
}
