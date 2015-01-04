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
package org.apache.commons.jcs.jcache.extras;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.lang.reflect.Field;

// TODO: *if needed* define @CacheDeifnition instead of relying on field types
public class InternalCacheRule implements TestRule
{
    private final Object test;

    public InternalCacheRule(final Object test)
    {
        this.test = test;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                final CachingProvider provider = Caching.getCachingProvider();
                final CacheManager manager = provider.getCacheManager();
                try
                {
                    Field cache = null;
                    CompleteConfiguration<?, ?> config = null;
                    for (final Field f : test.getClass().getDeclaredFields())
                    {
                        if (Cache.class.isAssignableFrom(f.getType()))
                        {
                            f.setAccessible(true);
                            cache = f;
                        }
                        else if (Configuration.class.isAssignableFrom(f.getType()))
                        {
                            f.setAccessible(true);
                            config = (CompleteConfiguration<?, ?>) f.get(test);
                        }
                    }
                    if (cache != null)
                    {
                        if (config == null)
                        {
                            throw new IllegalStateException("Define a Configuration field");
                        }
                        cache.set(test, manager.createCache(cache.getName(), config));
                    }
                    base.evaluate();
                }
                finally
                {
                    manager.close();
                    provider.close();
                }
            }
        };
    }
}
