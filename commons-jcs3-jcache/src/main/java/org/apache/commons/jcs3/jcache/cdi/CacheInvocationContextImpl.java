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
package org.apache.commons.jcs3.jcache.cdi;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheInvocationParameter;
import javax.interceptor.InvocationContext;

public class CacheInvocationContextImpl<A extends Annotation> extends CacheMethodDetailsImpl<A> implements CacheInvocationContext<A>
{
    private static final Object[] EMPTY_ARGS = {};

    private static CacheInvocationParameterImpl newCacheInvocationParameterImpl(final Class<?> type, final Object arg,
                                                                         final Set<Annotation> annotations, final int i) {
        return new CacheInvocationParameterImpl(type, arg, annotations, i);
    }

    private CacheInvocationParameter[] parameters;

    public CacheInvocationContextImpl(final InvocationContext delegate, final A cacheAnnotation, final String cacheName,
                                      final CDIJCacheHelper.MethodMeta meta)
    {
        super(delegate, cacheAnnotation, cacheName, meta);
    }

    protected CacheInvocationParameter[] doGetAllParameters(final Integer[] indexes)
    {
        final Object[] parameters = delegate.getParameters();
        final Object[] args = parameters == null ? EMPTY_ARGS : parameters;
        final Class<?>[] parameterTypes = meta.getParameterTypes();
        final List<Set<Annotation>> parameterAnnotations = meta.getParameterAnnotations();

        final CacheInvocationParameter[] parametersAsArray = new CacheInvocationParameter[indexes == null ? args.length : indexes.length];
        if (indexes == null)
        {
            for (int i = 0; i < args.length; i++)
            {
                parametersAsArray[i] = newCacheInvocationParameterImpl(parameterTypes[i], args[i], parameterAnnotations.get(i), i);
            }
        }
        else
        {
            int outIdx = 0;
            for (final Integer i : indexes) {
                parametersAsArray[outIdx] = newCacheInvocationParameterImpl(parameterTypes[i], args[i], parameterAnnotations.get(i), i);
                outIdx++;
            }
        }
        return parametersAsArray;
    }

    @Override
    public CacheInvocationParameter[] getAllParameters()
    {
        if (parameters == null)
        {
            parameters = doGetAllParameters(null);
        }
        return parameters;
    }

    @Override
    public Object getTarget()
    {
        return delegate.getTarget();
    }

    @Override
    public <T> T unwrap(final Class<T> cls)
    {
        if (cls.isAssignableFrom(getClass()))
        {
            return cls.cast(this);
        }
        throw new IllegalArgumentException(cls.getName());
    }
}
