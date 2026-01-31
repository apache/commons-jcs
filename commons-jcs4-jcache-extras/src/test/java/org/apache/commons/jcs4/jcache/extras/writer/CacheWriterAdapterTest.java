/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.jcs4.jcache.extras.writer;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheWriterException;

import org.apache.commons.jcs4.jcache.extras.InternalCacheExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InternalCacheExtension.class)
public class CacheWriterAdapterTest
{
    private final Map<String, String> copy = new HashMap<>();
    private final Configuration<?, ?> config = new MutableConfiguration<String, String>()
            .setStoreByValue(false).setReadThrough(true)
            .setCacheWriterFactory(new CacheWriterAdapter<String, String>()
            {
                private static final long serialVersionUID = 124351798952737984L;

                @Override
                public void delete(final Object key) throws CacheWriterException
                {
                    copy.remove(key);
                }

                @Override
                public void write(final Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException
                {
                    copy.put(entry.getKey(), entry.getValue());
                }
            });
    private Cache<String, String> cache;

    @Test
    void testCheckWriteAllAndDeleteAll()
    {
        assertTrue(copy.isEmpty());
        assertFalse(cache.iterator().hasNext());
        cache.put("foo", "bar");
        assertEquals(1, copy.size());
        cache.remove("foo");
        assertTrue(copy.isEmpty());

        cache.putAll(new HashMap<String, String>() {private static final long serialVersionUID = -5341092848989593322L;

        {
            put("a", "b");
            put("b", "c");
        }});
        assertEquals(2, copy.size());
        cache.removeAll(new HashSet<>(asList("a", "b")));
        assertTrue(copy.isEmpty());
    }
}
