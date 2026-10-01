package org.apache.commons.jcs4.engine.memory.lru;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.CompositeCacheAttributes;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes.DiskUsagePatternEnum;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.event.ElementEventQueue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for the LRU memory cache. */
class LRUMemoryCacheUnitTest
{
    /** Distinct immutable keys that all map to the same shard. */
    private record CollidingKey(int id)
    {
        @Override
        public int hashCode()
        {
            return 7;
        }
    }

    /**
     * An empty shard must not stop eviction while another shard still has entries.
     * The single-shard case provides a control for the same workload.
     *
     * @param shards number of memory cache shards
     * @throws Exception if cache setup or an operation fails
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 16})
    void testCapacityWithCollidingKeys(final int shards) throws Exception
    {
        final int maxObjects = 128;
        final int items = 1024;
        final CompositeCacheAttributes attributes = new CompositeCacheAttributes(
                "testCapacityWithCollidingKeys", maxObjects, false, Duration.ofSeconds(30),
                -1, Duration.ofHours(2), LRUMemoryCache.class.getName(), DiskUsagePatternEnum.SWAP, 1, shards);
        // Disable expiry, auxiliary spooling and background shrinking to isolate memory eviction.
        final ElementAttributes elementAttributes = new ElementAttributes(false, false, false, true,
                Duration.ofMinutes(5), Duration.ofMillis(-1));
        final CompositeCache<CollidingKey, String> cache = new CompositeCache<>(attributes, elementAttributes);
        ElementEventQueue queue = null;
        try
        {
            queue = new ElementEventQueue();
            cache.setElementEventQueue(queue);
            final CacheAccess<CollidingKey, String> access = new CacheAccess<>(cache);

            for (int i = 0; i < items; i++)
            {
                access.put(new CollidingKey(i), "value" + i);
                assertEquals(Math.min(i + 1, maxObjects), cache.getMemoryCache().getSize(),
                        "Unexpected memory size after insertion " + (i + 1) + " with " + shards + " shards");
            }

            // Read only after all insertions so verification does not affect eviction order.
            assertEquals(maxObjects, cache.getMemoryCache().getKeySet().size());
            int residentValues = 0;
            for (int i = 0; i < items; i++)
            {
                final String value = access.get(new CollidingKey(i));
                if (value != null)
                {
                    assertEquals("value" + i, value);
                    residentValues++;
                }
            }
            assertEquals(maxObjects, residentValues, "Actual residents must agree with the memory size");
        }
        finally
        {
            try
            {
                cache.dispose();
            }
            finally
            {
                if (queue != null)
                {
                    queue.dispose();
                }
            }
        }
    }
}
