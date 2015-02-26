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
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.GeneratedCacheKey;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CacheResult
@Interceptor
@Priority(/*LIBRARY_BEFORE*/1000)
public class CacheResultInterceptor implements Serializable
{
    @Inject
    private CDIJCacheHelper helper;

    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Throwable
    {
        final CacheDefaults defaults = helper.findDefaults(ic);

        final Method method = ic.getMethod();
        final CacheResult cacheResult = method.getAnnotation(CacheResult.class);
        final String cacheName = helper.defaultName(method, defaults, cacheResult.cacheName());

        final CacheKeyInvocationContext<CacheResult> context = new CacheKeyInvocationContextImpl<CacheResult>(
                ic, cacheResult, cacheName, helper.keyParameterIndexes(method));

        final CacheResolverFactory cacheResolverFactory = helper.cacheResolverFactoryFor(defaults, cacheResult.cacheResolverFactory());
        final CacheResolver cacheResolver = cacheResolverFactory.getCacheResolver(context);
        final Cache<Object, Object> cache = cacheResolver.resolveCache(context);

        final GeneratedCacheKey cacheKey = helper.cacheKeyGeneratorFor(defaults, cacheResult.cacheKeyGenerator()).generateCacheKey(context);

        Cache<Object, Object> exceptionCache = null; // lazily created

        Object result;
        if (!cacheResult.skipGet())
        {
            result = cache.get(cacheKey);
            if (result != null)
            {
                return result;
            }


            if (!cacheResult.exceptionCacheName().isEmpty())
            {
                exceptionCache = cacheResolverFactory.getExceptionCacheResolver(context).resolveCache(context);
                final Object exception = exceptionCache.get(cacheKey);
                if (exception != null)
                {
                    throw Throwable.class.cast(exception);
                }
            }
        }

        try
        {
            result = ic.proceed();
            if (result != null)
            {
                cache.put(cacheKey, result);
            }

            return result;
        }
        catch (final Throwable t)
        {
            if (helper.isIncluded(t.getClass(), cacheResult.cachedExceptions(), cacheResult.nonCachedExceptions()))
            {
                if (exceptionCache == null)
                {
                    exceptionCache = cacheResolverFactory.getExceptionCacheResolver(context).resolveCache(context);
                }
                exceptionCache.put(cacheKey, t);
            }
            throw t;
        }
    }
}
