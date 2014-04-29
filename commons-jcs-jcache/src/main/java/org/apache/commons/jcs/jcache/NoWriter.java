package org.apache.commons.jcs.jcache;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.util.Collection;

public class NoWriter<K, V> implements CacheWriter<K, V>
{
    public static final NoWriter INSTANCE = new NoWriter();

    @Override
    public void write(final Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException
    {
        // no-op
    }

    @Override
    public void delete(final Object key) throws CacheWriterException
    {
        // no-op
    }

    @Override
    public void writeAll(final Collection<Cache.Entry<? extends K, ? extends V>> entries) throws CacheWriterException
    {
        for (final Cache.Entry<? extends K, ? extends V> entry : entries)
        {
            write(entry);
        }
    }

    @Override
    public void deleteAll(final Collection<?> keys) throws CacheWriterException
    {
        for (final Object k : keys)
        {
            delete(k);
        }
    }
}
