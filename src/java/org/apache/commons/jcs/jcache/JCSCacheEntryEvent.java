package org.apache.commons.jcs.jcache;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

public class JCSCacheEntryEvent<K, V> extends CacheEntryEvent<K, V> {
    private final V old;
    private final K key;
    private final V value;

    public JCSCacheEntryEvent(final Cache source, final EventType eventType, final V old, final K key, final V value) {
        super(source, eventType);
        this.old = old;
        this.key = key;
        this.value = value;
    }

    @Override
    public V getOldValue() {
        return old;
    }

    @Override
    public boolean isOldValueAvailable() {
        return old != null;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        throw new IllegalArgumentException(clazz.getName() + " not supported in unwrap");
    }
}
