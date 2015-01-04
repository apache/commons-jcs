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
import java.lang.annotation.Annotation;
import java.util.Set;

public class CacheInvocationParameterImpl implements CacheInvocationParameter
{
    private final Class<?> type;
    private final Object value;
    private final Set<Annotation> annotations;
    private final int position;

    public CacheInvocationParameterImpl(final Class<?> type, final Object value, final Set<Annotation> annotations, final int position)
    {
        this.type = type;
        this.value = value;
        this.annotations = annotations;
        this.position = position;
    }

    @Override
    public Class<?> getRawType()
    {
        return type;
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public Set<Annotation> getAnnotations()
    {
        return annotations;
    }

    @Override
    public int getParameterPosition()
    {
        return position;
    }
}
