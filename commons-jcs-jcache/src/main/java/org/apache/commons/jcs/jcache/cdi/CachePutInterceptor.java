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
import javax.cache.annotation.CachePut;
import javax.cache.annotation.GeneratedCacheKey;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CachePut
@Interceptor
@Priority(/*LIBRARY_BEFORE*/1000)
public class CachePutInterceptor implements Serializable
{
    @Inject
    private CDIJCacheHelper helper;

    @AroundInvoke
    public Object cache(final InvocationContext ic) throws Throwable
    {
        final CacheDefaults defaults = helper.findDefaults(ic);

        final Method method = ic.getMethod();
        final CachePut cachePut = method.getAnnotation(CachePut.class);
        final String cacheName = helper.defaultName(method, defaults, cachePut.cacheName());
        final boolean afterInvocation = cachePut.afterInvocation();

        final CacheKeyInvocationContext<CachePut> context = new CacheKeyInvocationContextImpl<CachePut>(
                ic, cachePut, cacheName, helper.keyParameterIndexes(method));
        if (!afterInvocation)
        {
            doCache(context, defaults, cachePut);
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
                if (helper.isIncluded(t.getClass(), cachePut.cacheFor(), cachePut.noCacheFor()))
                {
                    doCache(context, defaults, cachePut);
                }
            }

            throw t;
        }

        if (afterInvocation)
        {
            doCache(context, defaults, cachePut);
        }

        return result;
    }

    private void doCache(final CacheKeyInvocationContext<CachePut> context, final CacheDefaults defaults, final CachePut cachePut)
    {
        final Cache<Object, Object> cache = helper.cacheResolverFactoryFor(defaults, cachePut.cacheResolverFactory()).getCacheResolver(context).resolveCache(context);
        final GeneratedCacheKey key = helper.cacheKeyGeneratorFor(defaults, cachePut.cacheKeyGenerator()).generateCacheKey(context);
        cache.put(key, context.getValueParameter().getValue());
    }
}
