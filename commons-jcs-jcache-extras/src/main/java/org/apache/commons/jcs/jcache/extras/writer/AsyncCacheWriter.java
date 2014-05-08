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

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncCacheWriter<K, V> implements CacheWriter<K, V>, Closeable, Factory<CacheWriter<K, V>>
{
    private static final Logger LOGGER = Logger.getLogger(AsyncCacheWriter.class.getName());

    private final CacheWriter<K, V> writer;
    private final ExecutorService pool;

    public AsyncCacheWriter(final CacheWriter<K, V> delegate, final int poolSize)
    {
        writer = delegate;
        pool = Executors.newFixedThreadPool(
                poolSize, new DaemonThreadFactory(delegate.getClass().getName() + "-" + delegate.hashCode() + "-"));
    }

    @Override
    public void write(final Cache.Entry<? extends K, ? extends V> entry) throws CacheWriterException
    {
        pool.submit(new ExceptionProtectionRunnable()
        {
            @Override
            public void doRun()
            {
                writer.write(entry);
            }
        });
    }

    @Override
    public void writeAll(final Collection<Cache.Entry<? extends K, ? extends V>> entries) throws CacheWriterException
    {
        pool.submit(new ExceptionProtectionRunnable()
        {
            @Override
            public void doRun()
            {
                writer.writeAll(entries);
            }
        });
    }

    @Override
    public void delete(final Object key) throws CacheWriterException
    {
        pool.submit(new ExceptionProtectionRunnable()
        {
            @Override
            public void doRun()
            {
                writer.delete(key);
            }
        });
    }

    @Override
    public void deleteAll(final Collection<?> keys) throws CacheWriterException
    {
        pool.submit(new ExceptionProtectionRunnable()
        {
            @Override
            public void doRun()
            {
                writer.deleteAll(keys);
            }
        });
    }

    @Override
    public void close() throws IOException
    {
        final List<Runnable> runnables = pool.shutdownNow();
        for (final Runnable r : runnables)
        {
            r.run();
        }
    }

    @Override
    public CacheWriter<K, V> create()
    {
        return this;
    }

    // avoid dep on impl
    private static class DaemonThreadFactory implements ThreadFactory
    {
        private final AtomicInteger index = new AtomicInteger(1);
        private final String prefix;

        public DaemonThreadFactory(final String prefix)
        {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread( final Runnable runner )
        {
            final Thread t = new Thread( runner );
            t.setName(prefix + index.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    private static abstract class ExceptionProtectionRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                doRun();
            }
            catch (final Exception e)
            {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        protected abstract void doRun();
    }
}
