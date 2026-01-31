package org.apache.commons.jcs4.jcache.cdi;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheResult;
import javax.interceptor.InvocationContext;

import org.junit.jupiter.api.Test;

class CDIJCacheHelperTest
{
    @CacheDefaults(cacheName = "child")
    public interface MyChild1 extends MyParent
    {
    }

    @CacheDefaults(cacheName = "child2")
    public interface MyChild2 extends MyParent
    {
    }

    public interface MyParent
    {
        @CacheResult
        String foo();
    }

    private InvocationContext newContext(final MyParent child1) {
        return new InvocationContext()
        {
            @Override
            public Constructor<?> getConstructor()
            {
                return null;
            }

            @Override
            public Map<String, Object> getContextData()
            {
                return null;
            }

            @Override
            public Method getMethod()
            {
                try {
                    return MyParent.class.getMethod("foo");
                } catch (final NoSuchMethodException e) {
                    fail(e.getMessage());
                    return null;
                }
            }

            @Override
            public Object[] getParameters()
            {
                return new Object[0];
            }

            @Override
            public Object getTarget()
            {
                return child1;
            }

            @Override
            public Object getTimer()
            {
                return null;
            }

            @Override
            public Object proceed() throws Exception
            {
                return null;
            }

            @Override
            public void setParameters(final Object[] objects)
            {

            }
        };
    }

    @Test
    void testProxyCacheDefaults()
    {
        final CDIJCacheHelper helper = new CDIJCacheHelper();

        final MyParent child1 = MyParent.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{MyChild1.class}, (proxy, method, args) -> null));
        final CDIJCacheHelper.MethodMeta meta1 = helper.findMeta(newContext(child1));
        assertEquals("child", meta1.getCacheResultCacheName());

        final MyParent child2 = MyParent.class.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{MyChild2.class}, (proxy, method, args) -> null));
        final CDIJCacheHelper.MethodMeta meta2 = helper.findMeta(newContext(child2));
        assertEquals("child2", meta2.getCacheResultCacheName());
    }
}
