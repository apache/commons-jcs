/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.jcs3.jcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

import org.junit.Test;

public class ExpiryListenerTest {

    @Test
    public void listener() throws InterruptedException {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        final CacheEntryExpiredListenerImpl listener = new CacheEntryExpiredListenerImpl();
        cacheManager.createCache("default", new MutableConfiguration<String, String>()
                .setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<ExpiryPolicy>(
                        new CreatedExpiryPolicy(new Duration(TimeUnit.MILLISECONDS, 1))))
                .addCacheEntryListenerConfiguration(new MutableCacheEntryListenerConfiguration<>(
                        FactoryBuilder.factoryOf(listener),
                        null, false, false
                )));
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", "bar");
        Thread.sleep(10);
        assertFalse(cache.containsKey("foo"));
        cachingProvider.close();
        assertEquals(1, listener.events.size());
    }

    private static class CacheEntryExpiredListenerImpl implements CacheEntryExpiredListener<String, String>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -5070377769541346377L;
        private final Collection<CacheEntryEvent<? extends String, ? extends String>> events =
                new ArrayList<>();

        @Override
        public void onExpired(final Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (final CacheEntryEvent<? extends String, ? extends String> cacheEntryEvent : cacheEntryEvents) {
                events.add(cacheEntryEvent);
            }
        }
    }
}
