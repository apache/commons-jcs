package org.apache.commons.jcs3.jcache;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;

import org.junit.jupiter.api.Test;

class CacheTest
{
    @Test
    void testAccessExpiry()
        throws InterruptedException
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager(cachingProvider.getDefaultURI(),
                Thread.currentThread().getContextClassLoader(),
                cachingProvider.getDefaultProperties());
        final Cache<Integer, Integer> cache = cacheManager.createCache(
                "test",
                new MutableConfiguration<Integer, Integer>()
                        .setStoreByValue(false)
                        .setStatisticsEnabled(true)
                        .setManagementEnabled(true)
                        .setTypes(Integer.class, Integer.class)
                        .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, 500))));

        try {
            cache.put(1, 2);
            cache.get(1);
            Thread.sleep(650);
            assertFalse(cache.containsKey(1));
            cache.put(1, 2);
            for (int i = 0; i < 3; i++) { // we update the last access to force the idle time and lastaccess to be synced
                Thread.sleep(250);
                assertTrue( cache.containsKey( 1 ), "iteration: " + Integer.toString( i ) );
            }
            assertTrue(cache.containsKey(1));
            Thread.sleep(650);
            assertFalse(cache.containsKey(1));
        } finally {
            cacheManager.close();
            cachingProvider.close();
        }
    }

    @Test
    void testGetPut()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        final Cache<String, String> cache = cacheManager.createCache("default", new MutableConfiguration<>());
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", "bar");
        assertTrue(cache.containsKey("foo"));
        assertEquals("bar", cache.get("foo"));
        cache.remove("foo");
        assertFalse(cache.containsKey("foo"));
        cachingProvider.close();
    }

    @Test
    void testListeners()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", new MutableConfiguration<>());
        final Cache<String, String> cache = cacheManager.getCache("default");
        final Set<String> event = new HashSet<>();
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            /**
             */
            private static final long serialVersionUID = -8253611067837660184L;

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
            }

            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return () -> (CacheEntryCreatedListener<String, String>) cacheEntryEvents -> event.add(cacheEntryEvents.iterator().next().getKey());
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public boolean isSynchronous()
            {
                return false;
            }
        });
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            /**
             */
            private static final long serialVersionUID = 74774789357823553L;

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
            }

            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return () -> (CacheEntryUpdatedListener<String, String>) cacheEntryEvents -> event.add(cacheEntryEvents.iterator().next().getKey());
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public boolean isSynchronous()
            {
                return false;
            }
        });
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            /**
             */
            private static final long serialVersionUID = 2442816458182278519L;

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
            }

            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return () -> (CacheEntryRemovedListener<String, String>) cacheEntryEvents -> event.add(cacheEntryEvents.iterator().next().getKey());
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public boolean isSynchronous()
            {
                return false;
            }
        });

        cache.put("foo", "bar");
        assertEquals(1, event.size());
        assertEquals("foo", event.iterator().next());
        event.clear();
        cache.put("foo", "new");
        assertEquals(1, event.size());
        assertEquals("foo", event.iterator().next());
        event.clear();
        cache.remove("foo");
        assertEquals(1, event.size());
        assertEquals("foo", event.iterator().next());

        cachingProvider.close();
    }

    @Test
    void testLoader()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", new CompleteConfiguration<>()
        {
            /**
             */
            private static final long serialVersionUID = -4598329777808827966L;

            @Override
            public Iterable<CacheEntryListenerConfiguration<Object, Object>> getCacheEntryListenerConfigurations()
            {
                return null;
            }

            @Override
            public Factory<CacheLoader<Object, Object>> getCacheLoaderFactory()
            {
                return () -> new CacheLoader<>()
                {
                    @Override
                    public Object load(final Object key) throws CacheLoaderException
                    {
                        return "super";
                    }

                    @Override
                    public Map<Object, Object> loadAll(final Iterable<?> keys) throws CacheLoaderException
                    {
                        return null;
                    }
                };
            }

            @Override
            public Factory<CacheWriter<? super Object, ? super Object>> getCacheWriterFactory()
            {
                return null;
            }

            @Override
            public Factory<ExpiryPolicy> getExpiryPolicyFactory()
            {
                return null;
            }

            @Override
            public Class<Object> getKeyType()
            {
                return Object.class;
            }

            @Override
            public Class<Object> getValueType()
            {
                return Object.class;
            }

            @Override
            public boolean isManagementEnabled()
            {
                return false;
            }

            @Override
            public boolean isReadThrough()
            {
                return true;
            }

            @Override
            public boolean isStatisticsEnabled()
            {
                return false;
            }

            @Override
            public boolean isStoreByValue()
            {
                return false;
            }

            @Override
            public boolean isWriteThrough()
            {
                return false;
            }
        });
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertEquals("super", cache.get("lazilyLoaded"));
        cachingProvider.close();
    }
}
