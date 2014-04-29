package org.apache.commons.jcs.jcache;

import javax.cache.Cache;
import javax.cache.processor.MutableEntry;

public class JCSMutableEntry<K, V> implements MutableEntry<K, V>
{
    private final Cache<K, V> cache;
    private final K key;

    public JCSMutableEntry(final Cache<K, V> cache, final K key)
    {
        this.cache = cache;
        this.key = key;
    }

    @Override
    public boolean exists()
    {
        return cache.containsKey(key);
    }

    @Override
    public void remove()
    {
        cache.remove(key);
    }

    @Override
    public void setValue(final V value)
    {
        cache.put(key, value);
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return cache.get(key);
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
