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
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CacheLoaderAdapterTest
{
    @Rule
    public final InternalCacheRule rule = new InternalCacheRule(this);

    private final AtomicInteger count = new AtomicInteger(0);
    private final Configuration<?, ?> config = new MutableConfiguration<String, String>().setStoreByValue(false).setReadThrough(true).setCacheLoaderFactory(new CacheLoaderAdapter<String, String>()
    {
        @Override
        public String load(final String key) throws CacheLoaderException
        {
            count.incrementAndGet();
            return key;
        }
    });
    private Cache<String, String> cache;

    @Test
    public void checkLoadAll()
    {
        assertFalse(cache.iterator().hasNext());
        assertEquals("foo", cache.get("foo"));

        count.decrementAndGet();
        cache.loadAll(new HashSet<String>(asList("a", "b")), true, null);
        int retries = 100;
        while (retries-- > 0 && count.get() != 2)
        {
            try
            {
                Thread.sleep(20);
            }
            catch (final InterruptedException e)
            {
                Thread.interrupted();
            }
        }
        assertEquals(2, count.get());
        assertEquals("a", cache.get("a"));
        assertEquals("b", cache.get("b"));
        assertEquals(2, count.get());
    }
}
