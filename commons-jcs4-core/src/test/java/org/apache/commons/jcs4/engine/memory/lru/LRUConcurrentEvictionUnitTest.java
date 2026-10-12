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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.CompositeCacheAttributes;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes.DiskUsagePatternEnum;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.event.ElementEventQueue;
import org.apache.commons.jcs4.engine.memory.util.MemoryElementDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Regression tests for concurrent eviction and successful-removal accounting. */
public class LRUConcurrentEvictionUnitTest
{
    private record Key(int id)
    {
        @Override
        public int hashCode()
        {
            return 7;
        }
    }

    /** Adds scheduling hooks without changing victim selection or removal. */
    public static class ObservedLRU extends LRUMemoryCache<Key, String>
    {
        private volatile CyclicBarrier selected;
        private boolean orphanNextUpdate;
        private boolean failSpooling;

        @Override
        protected void adjustUpdateElement(final MemoryElementDescriptor<Key, String> node) throws IOException
        {
            if (orphanNextUpdate)
            {
                orphanNextUpdate = false;
                // Model a remove between map publication and list publication.
                remove(node.getCacheElement().key());
            }
            super.adjustUpdateElement(node);
        }

        @Override
        public void waterfall(final ICacheElement<Key, String> element)
        {
            if (failSpooling)
            {
                throw new IllegalStateException("Injected spool failure");
            }
            final CyclicBarrier barrier = selected;
            if (barrier != null && element.key().id() == 0)
            {
                try
                {
                    barrier.await(5, TimeUnit.SECONDS);
                }
                catch (final Exception e)
                {
                    throw new AssertionError("Both evictors must select the original victim", e);
                }
            }
            super.waterfall(element);
        }
    }

    private static final class Fixture implements AutoCloseable
    {
        private final CompositeCache<Key, String> core;
        private final CacheAccess<Key, String> access;
        private final ObservedLRU memory;
        private final ElementEventQueue queue = new ElementEventQueue();

        private Fixture(final int capacity, final int shards)
        {
            final CompositeCacheAttributes attributes = new CompositeCacheAttributes(
                    "concurrent-eviction", capacity, false, Duration.ofSeconds(30), -1,
                    Duration.ofHours(2), ObservedLRU.class.getName(), DiskUsagePatternEnum.SWAP, 1, shards);
            core = new CompositeCache<>(attributes, new ElementAttributes(false, false, false, true,
                    Duration.ofMinutes(5), Duration.ofMillis(-1)));
            core.setElementEventQueue(queue);
            access = new CacheAccess<>(core);
            memory = (ObservedLRU) core.getMemoryCache();
        }

        @Override
        public void close()
        {
            try
            {
                core.dispose();
            }
            finally
            {
                queue.dispose();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 16})
    void testConcurrentPutsRespectCapacity(final int shards) throws Exception
    {
        try (Fixture fixture = new Fixture(1, shards))
        {
            fixture.access.put(new Key(0), "zero");
            fixture.memory.selected = new CyclicBarrier(2);
            final ExecutorService workers = Executors.newFixedThreadPool(2);
            try
            {
                final Future<?> first = workers.submit(() -> fixture.access.put(new Key(1), "one"));
                final Future<?> second = workers.submit(() -> fixture.access.put(new Key(2), "two"));
                first.get(10, TimeUnit.SECONDS);
                second.get(10, TimeUnit.SECONDS);
                assertEquals(1, fixture.memory.getSize(), "Both puts have completed; capacity must hold");
                assertEquals(1, fixture.memory.getKeySet().size());
                assertEquals(3, fixture.core.getUpdateCount());
            }
            finally
            {
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 16})
    void testConcurrentFreeElementsCountsSuccessfulRemovals(final int shards) throws Exception
    {
        try (Fixture fixture = new Fixture(16, shards))
        {
            fixture.access.put(new Key(0), "zero");
            fixture.access.put(new Key(1), "one");
            fixture.memory.selected = new CyclicBarrier(2);
            final ExecutorService workers = Executors.newFixedThreadPool(2);
            try
            {
                final Future<Integer> first = workers.submit(() -> fixture.memory.freeElements(1));
                final Future<Integer> second = workers.submit(() -> fixture.memory.freeElements(1));
                assertEquals(2, first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS));
                assertEquals(0, fixture.memory.getSize(), "Two reported evictions must remove two entries");
            }
            finally
            {
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void testDetachedVictimDoesNotPreventProgress() throws Exception
    {
        try (Fixture fixture = new Fixture(16, 1))
        {
            fixture.memory.orphanNextUpdate = true;
            fixture.access.put(new Key(0), "removed-before-list-publication");
            fixture.access.put(new Key(1), "live");
            assertEquals(1, fixture.memory.getSize());
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                assertEquals(1, fixture.memory.freeElements(2));
                assertEquals(0, fixture.memory.getSize());
            });
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 16})
    void testConcurrentChurn(final int shards) throws Exception
    {
        try (Fixture fixture = new Fixture(128, shards))
        {
            final int threads = 8;
            final int writes = 2048;
            final CyclicBarrier start = new CyclicBarrier(threads);
            final ExecutorService workers = Executors.newFixedThreadPool(threads);
            try
            {
                final List<Future<?>> results = new ArrayList<>();
                for (int writer = 0; writer < threads; writer++)
                {
                    final int first = writer * writes;
                    results.add(workers.submit(() -> {
                        start.await(5, TimeUnit.SECONDS);
                        for (int i = 0; i < writes; i++)
                        {
                            fixture.access.put(new Key(first + i), "value" + (first + i));
                        }
                        return null;
                    }));
                }
                for (final Future<?> result : results)
                {
                    result.get(30, TimeUnit.SECONDS);
                }
                assertTrue(fixture.memory.getSize() > 0 && fixture.memory.getSize() <= 128);
                assertEquals(fixture.memory.getSize(), fixture.memory.getKeySet().size());
                assertEquals(threads * writes, fixture.core.getUpdateCount());
                for (final Key key : fixture.memory.getKeySet())
                {
                    assertEquals("value" + key.id(), fixture.access.get(key));
                }
            }
            finally
            {
                workers.shutdownNow();
                assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void testSpoolingFailureDoesNotRemoveEntry() throws Exception
    {
        try (Fixture fixture = new Fixture(16, 1))
        {
            fixture.access.put(new Key(0), "retained");
            fixture.memory.failSpooling = true;
            assertThrows(IllegalStateException.class, () -> fixture.memory.freeElements(1));
            assertEquals(1, fixture.memory.getSize());
            assertEquals("retained", fixture.access.get(new Key(0)));
            fixture.memory.failSpooling = false;
        }
    }
}
