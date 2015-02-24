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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

import static java.util.Arrays.asList;

// TODO: observe annotated type (or maybe sthg else) to cache data and inject this extension (used as metadata cache)
// to get class model and this way allow to add cache annotation on the fly - == avoid java pure reflection to get metadata
public class MakeJCacheCDIInterceptorFriendly implements Extension
{
    private static final AtomicInteger id = new AtomicInteger();
    private static final boolean USE_ID = !Boolean.getBoolean("org.apache.commons.jcs.cdi.skip-id");

    private boolean needHelper = true;

    protected void discoverInterceptorBindings(final @Observes BeforeBeanDiscovery beforeBeanDiscoveryEvent,
                                               final BeanManager bm)
    {
        // CDI 1.1 will just pick createAnnotatedType(X) as beans so we'll skip our HelperBean
        // but CDI 1.0 needs our HelperBean + interceptors in beans.xml like:
        /*
        <beans xmlns="http://java.sun.com/xml/ns/javaee"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
              http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
          <interceptors>
            <class>org.apache.commons.jcs.jcache.cdi.CacheResultInterceptor</class>
            <class>org.apache.commons.jcs.jcache.cdi.CacheRemoveAllInterceptor</class>
            <class>org.apache.commons.jcs.jcache.cdi.CacheRemoveInterceptor</class>
            <class>org.apache.commons.jcs.jcache.cdi.CachePutInterceptor</class>
          </interceptors>
        </beans>
         */
        bm.createAnnotatedType(CDIJCacheHelper.class);
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
        /* CDI >= 1.1 only. Actually we shouldn't go here with CDI 1.1 since we defined the annotated type for the helper
        final AnnotatedType<CDIJCacheHelper> annotatedType = bm.createAnnotatedType(CDIJCacheHelper.class);
        final BeanAttributes<CDIJCacheHelper> beanAttributes = bm.createBeanAttributes(annotatedType);
        final InjectionTarget<CDIJCacheHelper> injectionTarget = bm.createInjectionTarget(annotatedType);
        final Bean<CDIJCacheHelper> bean = bm.createBean(beanAttributes, CDIJCacheHelper.class, new InjectionTargetFactory<CDIJCacheHelper>() {
            @Override
            public InjectionTarget<CDIJCacheHelper> createInjectionTarget(Bean<CDIJCacheHelper> bean) {
                return injectionTarget;
            }
        });
        */
        final AnnotatedType<CDIJCacheHelper> annotatedType = bm.createAnnotatedType(CDIJCacheHelper.class);
        final InjectionTarget<CDIJCacheHelper> injectionTarget = bm.createInjectionTarget(annotatedType);
        final HelperBean bean = new HelperBean(annotatedType, injectionTarget, findIdSuffix());
        afterBeanDiscovery.addBean(bean);
    }

    protected void vetoScannedCDIJCacheHelperQualifiers(final @Observes ProcessAnnotatedType<CDIJCacheHelper> pat) {
        if (!needHelper) { // already seen, shouldn't really happen,just a protection
            pat.veto();
        }
        needHelper = false;
    }

    // TODO: make it better for ear+cluster case with CDI 1.0
    private String findIdSuffix() {
        // big disadvantage is all deployments of a cluster needs to be in the exact same order but it works with ears
        if (USE_ID) {
            return "lib" + id.incrementAndGet();
        }
        return "default";
    }

    public static class HelperBean implements Bean<CDIJCacheHelper>, PassivationCapable {
        private final AnnotatedType<CDIJCacheHelper> at;
        private final InjectionTarget<CDIJCacheHelper> it;
        private final HashSet<Annotation> qualifiers;
        private final String id;

        public HelperBean(final AnnotatedType<CDIJCacheHelper> annotatedType,
                          final InjectionTarget<CDIJCacheHelper> injectionTarget,
                          final String id) {
            this.at = annotatedType;
            this.it = injectionTarget;
            this.id =  "JCS#CDIHelper#" + id;

            this.qualifiers = new HashSet<Annotation>();
            this.qualifiers.add(new AnnotationLiteral<Default>() {});
            this.qualifiers.add(new AnnotationLiteral<Any>() {});
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return it.getInjectionPoints();
        }

        @Override
        public Class<?> getBeanClass() {
            return at.getJavaClass();
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        public Set<Type> getTypes() {
            return at.getTypeClosure();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public CDIJCacheHelper create(final CreationalContext<CDIJCacheHelper> context) {
            final CDIJCacheHelper produce = it.produce(context);
            it.inject(produce, context);
            it.postConstruct(produce);
            return produce;
        }

        @Override
        public void destroy(final CDIJCacheHelper instance, final CreationalContext<CDIJCacheHelper> context) {
            it.preDestroy(instance);
            it.dispose(instance);
            context.release();
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
