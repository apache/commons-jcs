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

import java.io.Serializable;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.cache.Cache;
import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.CacheRemoveAll;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CacheRemoveAll
@Interceptor
@Priority(/*LIBRARY_BEFORE*/1000)
public class CacheRemoveAllInterceptor implements Serializable
{
    @Inject
    private CDIJCacheHelper helper;

    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Throwable
    {
        final CacheDefaults defaults = helper.findDefaults(ic);

        final Method method = ic.getMethod();
        final CacheRemoveAll cacheRemoveAll = method.getAnnotation(CacheRemoveAll.class);
        final String cacheName = helper.defaultName(method, defaults, cacheRemoveAll.cacheName());
        final boolean afterInvocation = cacheRemoveAll.afterInvocation();

        final CacheKeyInvocationContext<CacheRemoveAll> context = new CacheKeyInvocationContextImpl<CacheRemoveAll>(
                ic, cacheRemoveAll, cacheName, helper.keyParameterIndexes(method));
        if (!afterInvocation)
        {
            removeAll(context, defaults, cacheRemoveAll);
        }

        final Object result;
        try
        {
            result = ic.proceed();
        }
        catch (final Throwable t)
        {
            if (afterInvocation)
            {
                if (helper.isIncluded(t.getClass(), cacheRemoveAll.evictFor(), cacheRemoveAll.noEvictFor()))
                {
                    removeAll(context, defaults, cacheRemoveAll);
                }
            }
            throw t;
        }

        if (afterInvocation)
        {
            removeAll(context, defaults, cacheRemoveAll);
        }

        return result;
    }

    private void removeAll(final CacheKeyInvocationContext<CacheRemoveAll> context, final CacheDefaults defaults, final CacheRemoveAll cacheRemoveAll)
    {
        final Cache<Object, Object> cache = helper.cacheResolverFactoryFor(defaults, cacheRemoveAll.cacheResolverFactory()).getCacheResolver(context).resolveCache(context);
        cache.removeAll();
    }
}
