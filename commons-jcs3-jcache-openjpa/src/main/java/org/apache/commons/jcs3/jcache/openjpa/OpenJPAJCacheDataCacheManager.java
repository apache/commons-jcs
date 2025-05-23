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
package org.apache.commons.jcs3.jcache.openjpa;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.DataCacheManagerImpl;
import org.apache.openjpa.lib.conf.ObjectValue;

public class OpenJPAJCacheDataCacheManager extends DataCacheManagerImpl
{
    private CachingProvider provider;
    private CacheManager cacheManager;

    @Override
    public void close()
    {
        super.close();
        if (!cacheManager.isClosed())
        {
            cacheManager.close();
        }
        provider.close();
    }

    CacheManager getCacheManager()
    {
        return cacheManager;
    }

    Cache<Object, Object> getOrCreateCache(final String prefix, final String entity)
    {
        final String internalName = prefix + entity;
        Cache<Object, Object> cache = cacheManager.getCache(internalName);
        if (cache == null)
        {
            final Properties properties = cacheManager.getProperties();
            final MutableConfiguration<Object, Object> configuration = new MutableConfiguration<>()
                    .setStoreByValue(Boolean.parseBoolean(properties.getProperty("jcache.store-by-value", "false")));

            configuration.setReadThrough(Boolean.parseBoolean(properties.getProperty("jcache.read-through", "false")));
            configuration.setWriteThrough(Boolean.parseBoolean(properties.getProperty("jcache.write-through", "false")));
            if (configuration.isReadThrough())
            {
                configuration.setCacheLoaderFactory(new FactoryBuilder.ClassFactory<>(properties.getProperty("jcache.cache-loader-factory")));
            }
            if (configuration.isWriteThrough())
            {
                configuration.setCacheWriterFactory(new FactoryBuilder.ClassFactory<>(properties.getProperty("jcache.cache-writer-factory")));
            }
            final String expirtyPolicy = properties.getProperty("jcache.expiry-policy-factory");
            if (expirtyPolicy != null)
            {
                configuration.setExpiryPolicyFactory(new FactoryBuilder.ClassFactory<>(expirtyPolicy));
            }
            else
            {
                configuration.setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<>(new CreatedExpiryPolicy(Duration.FIVE_MINUTES)));
            }
            configuration.setManagementEnabled(Boolean.parseBoolean(properties.getProperty("jcache.management-enabled", "false")));
            configuration.setStatisticsEnabled(Boolean.parseBoolean(properties.getProperty("jcache.statistics-enabled", "false")));

            cache = cacheManager.createCache(internalName, configuration);
        }
        return cache;
    }

    @Override
    public void initialize(final OpenJPAConfiguration conf, final ObjectValue dataCache, final ObjectValue queryCache)
    {
        super.initialize(conf, dataCache, queryCache);
        provider = Caching.getCachingProvider();

        final Properties properties = new Properties();
        final Map<String, Object> props = conf.toProperties(false);
        if (props != null)
        {
            for (final Map.Entry<?, ?> entry : props.entrySet())
            {
                if (entry.getKey() != null && entry.getValue() != null)
                {
                    properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }

        final String uri = properties.getProperty("jcache.uri", provider.getDefaultURI().toString());
        cacheManager = provider.getCacheManager(URI.create(uri), provider.getDefaultClassLoader(), properties);
    }
}
