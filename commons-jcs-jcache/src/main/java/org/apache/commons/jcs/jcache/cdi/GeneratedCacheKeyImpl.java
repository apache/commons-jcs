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

import javax.cache.annotation.GeneratedCacheKey;
import java.util.Arrays;

public class GeneratedCacheKeyImpl implements GeneratedCacheKey
{
    private final Object[] params;
    private final int hash;

    public GeneratedCacheKeyImpl(final Object[] parameters)
    {
        this.params = parameters;
        this.hash = Arrays.deepHashCode(parameters);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final GeneratedCacheKeyImpl that = GeneratedCacheKeyImpl.class.cast(o);
        return Arrays.deepEquals(params, that.params);

    }

    @Override
    public int hashCode()
    {
        return hash;
    }
}
