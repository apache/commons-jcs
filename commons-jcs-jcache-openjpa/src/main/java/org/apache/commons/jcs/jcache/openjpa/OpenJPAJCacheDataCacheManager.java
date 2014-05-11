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
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.util.Map;
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

    Cache<Object, Object> getOrCreateCache(final String prefix, final String entity)
    {
        final String internalName = prefix + entity;
        Cache<Object, Object> cache = cacheManager.getCache(internalName);
        if (cache == null)
        {
            final Properties properties = cacheManager.getProperties();
            final MutableConfiguration<Object, Object> configuration = new MutableConfiguration<Object, Object>()
                    .setStoreByValue("true".equalsIgnoreCase(properties.getProperty("jcache.store-by-value", "false")));

            configuration.setReadThrough("true".equals(properties.getProperty("jcache.read-through", "false")));
            configuration.setWriteThrough("true".equals(properties.getProperty("jcache.write-through", "false")));
            if (configuration.isReadThrough())
            {
                configuration.setCacheLoaderFactory(new FactoryBuilder.ClassFactory<CacheLoader<Object, Object>>(properties.getProperty("jcache.cache-loader-factory")));
            }
            if (configuration.isWriteThrough())
            {
                configuration.setCacheWriterFactory(new FactoryBuilder.ClassFactory<CacheWriter<Object, Object>>(properties.getProperty("jcache.cache-writer-factory")));
            }
            final String expirtyPolicy = properties.getProperty("jcache.expiry-policy-factory");
            if (expirtyPolicy != null)
            {
                configuration.setExpiryPolicyFactory(new FactoryBuilder.ClassFactory<ExpiryPolicy>(expirtyPolicy));
            }
            else
            {
                configuration.setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<ExpiryPolicy>(new CreatedExpiryPolicy(Duration.FIVE_MINUTES)));
            }
            configuration.setManagementEnabled("true".equals(properties.getProperty("jcache.management-enabled", "false")));
            configuration.setStatisticsEnabled("true".equals(properties.getProperty("jcache.statistics-enabled", "false")));

            cache = cacheManager.createCache(internalName, configuration);
        }
        return cache;
    }

    CacheManager getCacheManager()
    {
        return cacheManager;
    }
}
