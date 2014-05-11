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
package org.apache.commons.jcs.jcache;

import org.junit.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotSerializableTest
{
    @Test
    public void run()
    {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();
        cacheManager.createCache("default", new MutableConfiguration<String, NotSerializableAndImHappyWithIt>().setStoreByValue(false));
        final Cache<String, NotSerializableAndImHappyWithIt> cache = cacheManager.getCache("default");
        assertFalse(cache.containsKey("foo"));
        cache.put("foo", new NotSerializableAndImHappyWithIt("bar"));
        assertTrue(cache.containsKey("foo"));
        assertEquals("bar", cache.get("foo").name);
        cache.remove("foo");
        assertFalse(cache.containsKey("foo"));
        cache.close();
        cacheManager.close();
        cachingProvider.close();
    }

    public static class NotSerializableAndImHappyWithIt {
        private final String name;

        public NotSerializableAndImHappyWithIt(final String name)
        {
            this.name = name;
        }
    }
}
