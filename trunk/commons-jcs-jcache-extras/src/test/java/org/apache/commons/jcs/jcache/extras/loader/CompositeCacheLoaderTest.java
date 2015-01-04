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
package org.apache.commons.jcs.jcache.extras.loader;

import org.apache.commons.jcs.jcache.extras.InternalCacheRule;
import org.junit.Rule;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoaderException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CompositeCacheLoaderTest
{
    @Rule
    public final InternalCacheRule rule = new InternalCacheRule(this);

    private final AtomicInteger count = new AtomicInteger(0);

    private final CacheLoaderAdapter<String, String> loader1 = new CacheLoaderAdapter<String, String>()
    {
        @Override
        public String load(String key) throws CacheLoaderException
        {
            count.incrementAndGet();
            return null;
        }
    };
    private final CacheLoaderAdapter<String, String> loader2 = new CacheLoaderAdapter<String, String>()
    {
        @Override
        public String load(String key) throws CacheLoaderException
        {
            count.incrementAndGet();
            return null;
        }
    };
    private final Configuration<?, ?> config = new MutableConfiguration<String, String>()
            .setStoreByValue(false)
            .setReadThrough(true)
            .setCacheLoaderFactory(new CompositeCacheLoader<String, String>(loader1, loader2));
    private Cache<String, String> cache;

    @Test
    public void checkComposite()
    {
        cache.get("foo");
        assertEquals(2, count.get());
    }
}
