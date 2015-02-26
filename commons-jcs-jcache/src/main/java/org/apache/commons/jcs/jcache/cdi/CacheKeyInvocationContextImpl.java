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

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.CacheValue;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;

public class CacheKeyInvocationContextImpl<A extends Annotation> extends CacheInvocationContextImpl<A> implements CacheKeyInvocationContext<A>
{
    private final Integer[] keyIndexes;
    private CacheInvocationParameter[] keyParams = null;
    private CacheInvocationParameter valueParam = null;

    public CacheKeyInvocationContextImpl(final InvocationContext delegate, final A annotation, final String name,
                                         final Integer[] keyIndexes)
    {
        super(delegate, annotation, name);
        this.keyIndexes = keyIndexes;
    }

    @Override
    public CacheInvocationParameter[] getKeyParameters()
    {
        if (keyParams == null)
        {
            final Collection<CacheInvocationParameter> keys = new LinkedList<CacheInvocationParameter>();
            for (final CacheInvocationParameter param : getAllParameters())
            {
                for (final Annotation a : param.getAnnotations())
                {
                    if (a.annotationType().equals(CacheKey.class))
                    {
                        keys.add(param);
                    }
                }
            }
            if (keys.isEmpty())
            {
                keyParams = doGetAllParameters(keyIndexes);
            }
            else
            {
                keyParams = keys.toArray(new CacheInvocationParameter[keys.size()]);
            }
        }
        return keyParams;
    }

    @Override
    public CacheInvocationParameter getValueParameter()
    {
        if (valueParam == null)
        {
            for (final CacheInvocationParameter param : getAllParameters())
            {
                for (final Annotation a : param.getAnnotations())
                {
                    if (a.annotationType().equals(CacheValue.class))
                    {
                        valueParam = param;
                        return valueParam;
                    }
                }
            }
        }
        return valueParam;
    }
}
