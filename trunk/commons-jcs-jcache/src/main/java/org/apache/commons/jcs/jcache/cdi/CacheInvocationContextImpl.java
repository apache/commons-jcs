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

import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheInvocationParameter;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.HashSet;

import static java.util.Arrays.asList;

public class CacheInvocationContextImpl<A extends Annotation> extends CacheMethodDetailsImpl<A> implements CacheInvocationContext<A>
{
    private CacheInvocationParameter[] parameters = null;

    public CacheInvocationContextImpl(final InvocationContext delegate, final A cacheAnnotation, final String cacheName)
    {
        super(delegate, cacheAnnotation, cacheName);
    }

    @Override
    public Object getTarget()
    {
        return delegate.getTarget();
    }

    @Override
    public CacheInvocationParameter[] getAllParameters()
    {
        if (parameters == null)
        {
            final Object[] args = delegate.getParameters();
            final Class<?>[] parameterTypes = getMethod().getParameterTypes();
            final Annotation[][] parameterAnnotations = getMethod().getParameterAnnotations();
            parameters = new CacheInvocationParameter[args.length];
            for (int i = 0; i < args.length; i++)
            {
                parameters[i] = new CacheInvocationParameterImpl(parameterTypes[i], args[i], new HashSet<Annotation>(asList(parameterAnnotations[i])), i);
            }
        }
        return parameters;
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
