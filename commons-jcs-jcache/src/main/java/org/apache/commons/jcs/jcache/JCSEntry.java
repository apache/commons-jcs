package org.apache.commons.jcs.jcache;

import javax.cache.Cache;

public class JCSEntry<K, V> implements Cache.Entry<K, V>
{
    private final K key;
    private final V value;

    public JCSEntry(final K key, final V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey()
    {
        return key;
    }

    @Override
    public V getValue()
    {
        return value;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz)
    {
        if (clazz.isInstance(this))
        {
            return clazz.cast(this);
        }
        throw new UnsupportedOperationException();
    }
}
