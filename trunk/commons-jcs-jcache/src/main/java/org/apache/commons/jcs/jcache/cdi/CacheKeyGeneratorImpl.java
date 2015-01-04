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
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import java.lang.annotation.Annotation;

public class CacheKeyGeneratorImpl implements CacheKeyGenerator
{
    @Override
    public GeneratedCacheKey generateCacheKey(final CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext)
    {
        final CacheInvocationParameter[] keyParameters = cacheKeyInvocationContext.getKeyParameters();
        final Object[] parameters = new Object[keyParameters.length];
        for (int index = 0; index < keyParameters.length; index++)
        {
            parameters[index] = keyParameters[index].getValue();
        }
        return new GeneratedCacheKeyImpl(parameters);
    }
}
