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

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

public class ExtraJCacheExtensionTest
{
    private static BeanManagerImpl bm;
    private static ContainerLifecycle lifecycle;

    @BeforeClass
    public static void startContainer()
    {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        lifecycle = webBeansContext.getService(ContainerLifecycle.class);
        lifecycle.startApplication(null);
        bm = webBeansContext.getBeanManagerImpl();
    }

    @AfterClass
    public static void stopContainer()
    {
        lifecycle.stopApplication(null);
    }

    @Before
    public void inject() throws Exception
    {
        OWBInjector.inject(bm, this, bm.createCreationalContext(null));
    }

    @Inject
    private BeanWithInjections bean;

    @Test
    public void defaultCacheManager()
    {
        assertNotNull(bean.getMgr());
    }

    @Test
    public void defaultCacheProvider()
    {
        assertNotNull(bean.getProvider());
    }

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
}
