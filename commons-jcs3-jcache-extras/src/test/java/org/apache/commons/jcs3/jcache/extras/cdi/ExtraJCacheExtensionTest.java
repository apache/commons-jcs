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
package org.apache.commons.jcs3.jcache.extras.cdi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExtraJCacheExtensionTest
{
    public static class BeanWithInjections {
        @Inject
        private CacheManager mgr;

        @Inject
        private CachingProvider provider;

        public CacheManager getMgr()
        {
            return mgr;
        }

        public CachingProvider getProvider()
        {
            return provider;
        }
    }
    private static BeanManagerImpl bm;

    private static ContainerLifecycle lifecycle;

    @BeforeAll
    static void startContainer()
    {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        lifecycle = webBeansContext.getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);
        bm = webBeansContext.getBeanManagerImpl();
    }

    @AfterAll
    static void stopContainer()
    {
        lifecycle.stopApplication(null);
    }

    @Inject
    private BeanWithInjections bean;

    @BeforeEach
    void inject()
        throws Exception
    {
        OWBInjector.inject(bm, this, bm.createCreationalContext(null));
    }

    @Test
    void testDefaultCacheManager()
    {
        assertNotNull(bean.getMgr());
    }

    @Test
    void testDefaultCacheProvider()
    {
        assertNotNull(bean.getProvider());
    }
}
