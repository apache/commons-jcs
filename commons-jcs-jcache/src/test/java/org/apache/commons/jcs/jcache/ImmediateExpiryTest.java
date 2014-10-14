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

package org.apache.commons.jcs.jcache;

import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertFalse;

public class ImmediateExpiryTest
{
    @Test
    public void immediate()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default",
                new MutableConfiguration<Object, Object>()
                        .setExpiryPolicyFactory(
                                new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new CreatedExpiryPolicy(Duration.ZERO))));
        final Cache<String, String> cache = cacheManager.getCache("default");
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", "bar");
        assertFalse(cache.containsKey("foo"));
        cachingProvider.close();
    }
}
