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
package org.apache.commons.jcs.jcache.extras.writer;

import org.apache.commons.jcs.jcache.extras.InternalCacheRule;
import org.junit.Rule;
import org.junit.Test;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheWriterException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompositeCacheWriterTest
{
    @Rule
    public final InternalCacheRule rule = new InternalCacheRule(this);


    private final Map<String, String> copy1 = new HashMap<String, String>();
    private final Map<String, String> copy2 = new HashMap<String, String>();

    private final CacheWriterAdapter<String, String> writer1 = new CacheWriterAdapter<String, String>()
    {
        @Override
        public void write(final Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException
        {
            copy1.put(entry.getKey(), entry.getValue());
        }

        @Override
        public void delete(final Object key) throws CacheWriterException
        {
            copy1.remove(key);
        }
    };
    private final CacheWriterAdapter<String, String> writer2 = new CacheWriterAdapter<String, String>()
    {
        @Override
        public void write(final Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException
        {
            copy2.put(entry.getKey(), entry.getValue());
        }

        @Override
        public void delete(final Object key) throws CacheWriterException
        {
            copy2.remove(key);
        }
    };
    private final Configuration<?, ?> config = new MutableConfiguration<String, String>()
            .setStoreByValue(false)
            .setWriteThrough(true)
            .setCacheWriterFactory(new CompositeCacheWriter<String, String>(writer1, writer2));
    private Cache<String, String> cache;

    @Test
    public void checkComposite()
    {
        cache.put("a", "b");
        assertEquals("b", copy1.get("a"));
        assertEquals("b", copy2.get("a"));
        assertEquals(1, copy1.size());
        assertEquals(1, copy2.size());
        cache.remove("a");
        assertTrue(copy1.isEmpty());
        assertTrue(copy2.isEmpty());
    }
}
