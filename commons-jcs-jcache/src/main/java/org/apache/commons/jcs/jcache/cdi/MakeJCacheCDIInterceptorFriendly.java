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
package org.apache.commons.jcs.jcache.cdi;

import java.lang.annotation.Annotation;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import static java.util.Arrays.asList;

// TODO: observe annotated type (or maybe sthg else) to cache data and inecjt this extension (used as metadata cache)
// to get class model and this way allow to add cache annotation on the fly - == avoid java pure reflection to get metadata
public class MakeJCacheCDIInterceptorFriendly implements Extension
{
    private boolean needHelper = true;

    protected void discoverInterceptorBindings(final @Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent,
                                               final BeanManager bm)
    {
        for (final Class<?> interceptor : asList(
                CachePutInterceptor.class, CacheRemoveInterceptor.class,
                CacheRemoveAllInterceptor.class, CacheResultInterceptor.class)) {
            beforeBeanDiscoveryEvent.addAnnotatedType(bm.createAnnotatedType(interceptor));
        }
        for (final Class<? extends Annotation> interceptor : asList(
                CachePut.class, CacheRemove.class,
                CacheRemoveAll.class, CacheResult.class)) {
            beforeBeanDiscoveryEvent.addInterceptorBinding(interceptor);
        }
    }

    protected void addHelper(final @Observes AfterBeanDiscovery afterBeanDiscovery,
                             final BeanManager bm)
    {
        if (!needHelper) {
            return;
        }
        final AnnotatedType<CDIJCacheHelper> annotatedType = bm.createAnnotatedType(CDIJCacheHelper.class);
        final BeanAttributes<CDIJCacheHelper> beanAttributes = bm.createBeanAttributes(annotatedType);
        final InjectionTarget<CDIJCacheHelper> injectionTarget = bm.createInjectionTarget(annotatedType);
        final Bean<CDIJCacheHelper> bean = bm.createBean(beanAttributes, CDIJCacheHelper.class, new InjectionTargetFactory<CDIJCacheHelper>() {
            @Override
            public InjectionTarget<CDIJCacheHelper> createInjectionTarget(Bean<CDIJCacheHelper> bean) {
                return injectionTarget;
            }
        });
        afterBeanDiscovery.addBean(bean);
    }

    protected void vetoScannedCDIJCacheHelperQualifiers(final @Observes ProcessAnnotatedType<CDIJCacheHelper> pat) {
        if (!needHelper) { // already seen, shouldn't really happen,just a protection
            pat.veto();
        }
        needHelper = false;
    }
}
