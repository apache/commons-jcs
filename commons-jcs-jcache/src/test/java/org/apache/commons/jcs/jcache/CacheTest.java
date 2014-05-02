package org.apache.commons.jcs.jcache;

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

import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CacheTest
{
    @Test
    public void getPut()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", null);
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", "bar");
        assertTrue(cache.containsKey("foo"));
        assertEquals("bar", cache.get("foo"));
        cache.remove("foo");
        assertFalse(cache.containsKey("foo"));
        cachingProvider.close();
    }

    @Test
    public void listeners()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", null);
        final Cache<String, String> cache = cacheManager.getCache("default");
        final Set<String> event = new HashSet<String>();
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return new Factory<CacheEntryListener<? super String, ? super String>>()
                {
                    @Override
                    public CacheEntryListener<? super String, ? super String> create()
                    {
                        return new CacheEntryCreatedListener<String, String>()
                        {
                            @Override
                            public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                                    throws CacheEntryListenerException
                            {
                                event.add(cacheEntryEvents.iterator().next().getKey());
                            }
                        };
                    }
                };
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
            }

            @Override
            public boolean isSynchronous()
            {
                return false;
            }
        });
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return new Factory<CacheEntryListener<? super String, ? super String>>()
                {
                    @Override
                    public CacheEntryListener<? super String, ? super String> create()
                    {
                        return new CacheEntryUpdatedListener<String, String>()
                        {
                            @Override
                            public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                                    throws CacheEntryListenerException
                            {
                                event.add(cacheEntryEvents.iterator().next().getKey());
                            }
                        };
                    }
                };
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
            }

            @Override
            public boolean isSynchronous()
            {
                return false;
            }
        });
        cache.registerCacheEntryListener(new CacheEntryListenerConfiguration<String, String>()
        {
            @Override
            public Factory<CacheEntryListener<? super String, ? super String>> getCacheEntryListenerFactory()
            {
                return new Factory<CacheEntryListener<? super String, ? super String>>()
                {
                    @Override
                    public CacheEntryListener<? super String, ? super String> create()
                    {
                        return new CacheEntryRemovedListener<String, String>()
                        {
                            @Override
                            public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                                    throws CacheEntryListenerException
                            {
                                event.add(cacheEntryEvents.iterator().next().getKey());
                            }
                        };
                    }
                };
            }

            @Override
            public boolean isOldValueRequired()
            {
                return false;
            }

            @Override
            public Factory<CacheEntryEventFilter<? super String, ? super String>> getCacheEntryEventFilterFactory()
            {
                return null;
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
    public void loader()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", new CompleteConfiguration<Object, Object>()
        {
            @Override
            public boolean isReadThrough()
            {
                return true;
            }

            @Override
            public boolean isWriteThrough()
            {
                return false;
            }

            @Override
            public boolean isStatisticsEnabled()
            {
                return false;
            }

            @Override
            public boolean isManagementEnabled()
            {
                return false;
            }

            @Override
            public Iterable<CacheEntryListenerConfiguration<Object, Object>> getCacheEntryListenerConfigurations()
            {
                return null;
            }

            @Override
            public Factory<CacheLoader<Object, Object>> getCacheLoaderFactory()
            {
                return new Factory<CacheLoader<Object, Object>>()
                {
                    @Override
                    public CacheLoader<Object, Object> create()
                    {
                        return new CacheLoader<Object, Object>()
                        {
                            @Override
                            public Object load(Object key) throws CacheLoaderException
                            {
                                return "super";
                            }

                            @Override
                            public Map<Object, Object> loadAll(Iterable<?> keys) throws CacheLoaderException
                            {
                                return null;
                            }
                        };
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
                return null;
            }

            @Override
            public Class<Object> getValueType()
            {
                return null;
            }

            @Override
            public boolean isStoreByValue()
            {
                return false;
            }
        });
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertEquals("super", cache.get("lazilyLoaded"));
        cachingProvider.close();
    }
}
