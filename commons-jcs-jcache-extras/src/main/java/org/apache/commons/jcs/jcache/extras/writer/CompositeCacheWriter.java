/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs.jcache.extras.writer;

import org.apache.commons.jcs.jcache.extras.closeable.Closeables;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public class CompositeCacheWriter<K, V> implements CacheWriter<K, V>, Closeable, Factory<CacheWriter<K, V>>
{
    private final CacheWriter<K, V>[] writers;

    public CompositeCacheWriter(final CacheWriter<K, V>... writers)
    {
        this.writers = writers;
    }

    @Override
    public void write(final Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException
    {
        CacheWriterException e = null;
        for (final CacheWriter<K, V> writer : writers)
        {
            try
            {
                writer.write(entry);
            }
            catch (final CacheWriterException ex)
            {
                if (e == null)
                {
                    e = ex;
                }
            }
        }
        if (e != null)
        {
            throw e;
        }
    }

    @Override
    public void writeAll(final Collection<Cache.Entry<? extends K, ? extends V>> entries) throws CacheWriterException
    {
        CacheWriterException e = null;
        for (final CacheWriter<K, V> writer : writers)
        {
            try
            {
                writer.writeAll(entries);
            }
            catch (final CacheWriterException ex)
            {
                if (e == null)
                {
                    e = ex;
                }
            }
        }
        if (e != null)
        {
            throw e;
        }
    }

    @Override
    public void delete(final Object key) throws CacheWriterException
    {
        CacheWriterException e = null;
        for (final CacheWriter<K, V> writer : writers)
        {
            try
            {
                writer.delete(key);
            }
            catch (final CacheWriterException ex)
            {
                if (e == null)
                {
                    e = ex;
                }
            }
        }
        if (e != null)
        {
            throw e;
        }
    }

    @Override
    public void deleteAll(final Collection<?> keys) throws CacheWriterException
    {
        CacheWriterException e = null;
        for (final CacheWriter<K, V> writer : writers)
        {
            try
            {
                writer.deleteAll(keys);
            }
            catch (final CacheWriterException ex)
            {
                if (e == null)
                {
                    e = ex;
                }
            }
        }
        if (e != null)
        {
            throw e;
        }
    }

    @Override
    public CacheWriter<K, V> create()
    {
        return this;
    }

    @Override
    public void close() throws IOException
    {
        Closeables.close(writers);
    }
}
