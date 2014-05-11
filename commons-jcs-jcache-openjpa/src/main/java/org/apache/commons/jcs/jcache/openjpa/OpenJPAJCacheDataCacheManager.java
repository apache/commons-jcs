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
package org.apache.commons.jcs.jcache.openjpa;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.DataCacheManagerImpl;
import org.apache.openjpa.lib.conf.ObjectValue;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;
import java.util.Properties;

public class OpenJPAJCacheDataCacheManager extends DataCacheManagerImpl
{
    private CachingProvider provider;
    private CacheManager cacheManager;

    @Override
    public void initialize(final OpenJPAConfiguration conf, final ObjectValue dataCache, final ObjectValue queryCache)
    {
        super.initialize(conf, dataCache, queryCache);
        provider = Caching.getCachingProvider();
        cacheManager = provider.getCacheManager(
                provider.getDefaultURI(), provider.getDefaultClassLoader(), new Properties()); // todo get props
    }

    @Override
    public void close()
    {
        super.close();
        cacheManager.close();
        provider.close();
    }

    Cache<Object, Object> getOrCreateCache(final String prefix, final String entity)
    {
        final String name = entity;
        final String internalName = prefix + name;
        Cache<Object, Object> cache = cacheManager.getCache(internalName);
        if (cache == null)
        {
            cache = cacheManager.createCache(internalName,
                    new MutableConfiguration<Object, Object>().setStoreByValue(false)
                            .setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new CreatedExpiryPolicy(Duration.FIVE_MINUTES))));
        }
        return cache;
    }

    CacheManager getCacheManager()
    {
        return cacheManager;
    }
}
