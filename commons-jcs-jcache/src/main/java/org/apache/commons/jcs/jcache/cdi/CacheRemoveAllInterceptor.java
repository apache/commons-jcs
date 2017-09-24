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

import javax.annotation.Priority;
import javax.cache.Cache;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
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
        final CDIJCacheHelper.MethodMeta methodMeta = helper.findMeta(ic);

        final String cacheName = methodMeta.getCacheRemoveAllCacheName();

        final CacheResolverFactory cacheResolverFactory = methodMeta.getCacheRemoveAllResolverFactory();
        final CacheKeyInvocationContext<CacheRemoveAll> context = new CacheKeyInvocationContextImpl<CacheRemoveAll>(
                ic, methodMeta.getCacheRemoveAll(), cacheName, methodMeta);
        final CacheResolver cacheResolver = cacheResolverFactory.getCacheResolver(context);
        final Cache<Object, Object> cache = cacheResolver.resolveCache(context);

        final boolean afterInvocation = methodMeta.isCachePutAfter();
        if (!afterInvocation)
        {
            cache.removeAll();
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
                if (helper.isIncluded(t.getClass(), methodMeta.getCacheRemoveAll().evictFor(), methodMeta.getCacheRemoveAll().noEvictFor()))
                {
                    cache.removeAll();
                }
            }
            throw t;
        }

        if (afterInvocation)
        {
            cache.removeAll();
        }

        return result;
    }
}
