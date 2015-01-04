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

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheResolverFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

@ApplicationScoped
public class CDIJCacheHelper
{
    private static final Logger LOGGER = Logger.getLogger(CDIJCacheHelper.class.getName());

    private final CacheResolverFactory defaultCacheResolverFactory = new CacheResolverFactoryImpl();
    private final CacheKeyGeneratorImpl defaultCacheKeyGenerator = new CacheKeyGeneratorImpl();
    private final ConcurrentMap<Method, String> generatedNames = new ConcurrentHashMap<Method, String>();

    @Inject
    private BeanManager beanManager;

    public String defaultName(final Method method, final CacheDefaults defaults, final String cacheName)
    {
        if (!cacheName.isEmpty())
        {
            return cacheName;
        }
        if (defaults != null)
        {
            final String name = defaults.cacheName();
            if (!name.isEmpty())
            {
                return name;
            }
        }

        String computedName = generatedNames.get(method);
        if (computedName == null)
        {
            final StringBuilder name = new StringBuilder(method.getDeclaringClass().getName());
            name.append(".");
            name.append(method.getName());
            name.append("(");
            final Class<?>[] parameterTypes = method.getParameterTypes();
            for (int pIdx = 0; pIdx < parameterTypes.length; pIdx++)
            {
                name.append(parameterTypes[pIdx].getName());
                if ((pIdx + 1) < parameterTypes.length)
                {
                    name.append(",");
                }
            }
            name.append(")");
            computedName = name.toString();
            generatedNames.putIfAbsent(method, computedName);
        }
        return computedName;
    }

    public CacheDefaults findDefaults(final InvocationContext ic)
    {
        Class<?> clazz = ic.getTarget().getClass();
        CacheDefaults annotation = null;
        while (clazz != null && clazz != Object.class)
        {
            annotation = clazz.getAnnotation(CacheDefaults.class);
            if (annotation != null)
            {
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return annotation;
    }

    public boolean isIncluded(final Class<?> aClass, final Class<?>[] in, final Class<?>[] out)
    {
        if (in.length == 0 && out.length == 0)
        {
            return false;
        }
        for (final Class<?> potentialIn : in)
        {
            if (potentialIn.isAssignableFrom(aClass))
            {
                for (final Class<?> potentialOut : out)
                {
                    if (potentialOut.isAssignableFrom(aClass))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public CacheKeyGenerator cacheKeyGeneratorFor(final CacheDefaults defaults, final Class<? extends CacheKeyGenerator> cacheKeyGenerator)
    {
        if (!CacheKeyGenerator.class.equals(cacheKeyGenerator))
        {
            return instance(cacheKeyGenerator);
        }
        if (defaults != null)
        {
            final Class<? extends CacheKeyGenerator> defaultCacheKeyGenerator = defaults.cacheKeyGenerator();
            if (!CacheResolverFactory.class.equals(defaultCacheKeyGenerator))
            {
                return instance(defaultCacheKeyGenerator);
            }
        }
        return defaultCacheKeyGenerator;
    }

    public CacheResolverFactory cacheResolverFactoryFor(final CacheDefaults defaults, final Class<? extends CacheResolverFactory> cacheResolverFactory)
    {
        if (!CacheResolverFactory.class.equals(cacheResolverFactory))
        {
            return instance(cacheResolverFactory);
        }
        if (defaults != null)
        {
            final Class<? extends CacheResolverFactory> defaultCacheResolverFactory = defaults.cacheResolverFactory();
            if (!CacheResolverFactory.class.equals(defaultCacheResolverFactory))
            {
                return instance(defaultCacheResolverFactory);
            }
        }
        return defaultCacheResolverFactory;
    }

    public <T> T instance(final Class<T> type)
    {
        final Set<Bean<?>> beans = beanManager.getBeans(type);
        if (beans.isEmpty())
        {
            return null;
        }
        final Bean<?> bean = beanManager.resolve(beans);
        final CreationalContext<?> context = beanManager.createCreationalContext(bean);
        final Class<? extends Annotation> scope = bean.getScope();
        final boolean dependent = Dependent.class.equals(scope);
        if (!dependent && !beanManager.isNormalScope(scope))
        {
            LOGGER.warning("Not normal scope beans (" + type.getName() + ") can leak");
        }
        try
        {
            return (T) beanManager.getReference(bean, bean.getBeanClass(), context);
        }
        finally
        {
            if (dependent)
            { // TODO: depent or pseudo scope?
                context.release();
            }
        }
    }
}
