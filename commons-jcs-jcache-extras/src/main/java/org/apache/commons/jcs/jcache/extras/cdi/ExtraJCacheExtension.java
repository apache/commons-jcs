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
package org.apache.commons.jcs.jcache.extras.cdi;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.util.Properties;

// add default CacheProvider and CacheManager
public class ExtraJCacheExtension implements Extension
{
    private static final boolean ACTIVATED = "true".equals(System.getProperty("org.apache.jcs.extra.cdi", "true"));

    private boolean cacheManagerFound = false;
    private boolean cacheProviderFound = false;
    private CacheManager cacheManager;
    private CachingProvider cachingProvider;

    public <A> void processBean(final @Observes ProcessBean<A> processBeanEvent)
    {
        if (!ACTIVATED)
        {
            return;
        }

        if (cacheManagerFound && cacheProviderFound)
        {
            return;
        }

        final Bean<A> bean = processBeanEvent.getBean();
        if (CacheManagerBean.class.isInstance(bean) || CacheProviderBean.class.isInstance(bean))
        {
            return;
        }

        if (!cacheManagerFound)
        {
            cacheManagerFound = bean.getTypes().contains(CacheManager.class);
        }
        if (!cacheProviderFound)
        {
            cacheProviderFound = bean.getTypes().contains(CachingProvider.class);
        }
    }

    public void addJCacheBeans(final @Observes AfterBeanDiscovery afterBeanDiscovery)
    {
        if (!ACTIVATED)
        {
            return;
        }

        if (cacheManagerFound && cacheProviderFound) {
            return;
        }

        cachingProvider = Caching.getCachingProvider();
        if (!cacheManagerFound)
        {
            cacheManager = cachingProvider.getCacheManager(
                    cachingProvider.getDefaultURI(),
                    cachingProvider.getDefaultClassLoader(),
                    new Properties());
            afterBeanDiscovery.addBean(new CacheManagerBean(cacheManager));
        }
        if (!cacheProviderFound)
        {
            afterBeanDiscovery.addBean(new CacheProviderBean(cachingProvider));
        }
    }

    public void destroyIfCreated(final @Observes BeforeShutdown beforeShutdown)
    {
        if (cacheManager != null)
        {
            cacheManager.close();
        }
        if (cachingProvider != null)
        {
            cachingProvider.close();
        }
    }
}
