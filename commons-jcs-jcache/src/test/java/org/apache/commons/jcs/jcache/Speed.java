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

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/* not a test, just a helper to run when micro-benchmarking

 @BenchmarkMode(Mode.AverageTime)
 @OutputTimeUnit(TimeUnit.NANOSECONDS)
 */
public class Speed
{
    private static final Random RDM = new Random(System.currentTimeMillis());

    @State(Scope.Benchmark)
    public static class MapState
    {
        private ConcurrentHashMap<String, Integer> map;
        private final AtomicInteger count = new AtomicInteger(0);

        @Setup
        public void setup()
        {
            map = new ConcurrentHashMap<String, Integer>();
        }

        @TearDown
        public void end()
        {
            map.clear();
        }
    }

    @State(Scope.Benchmark)
    public static class JCacheState
    {
        private CachingProvider cachingProvider;
        private CacheManager manager;
        private Cache<Object, Object> jcache;
        private final AtomicInteger count = new AtomicInteger(0);

        @Setup
        public void setup()
        {
            cachingProvider = Caching.getCachingProvider();
            manager = cachingProvider.getCacheManager();
            manager.createCache("speed-test", new MutableConfiguration<String, Integer>().setStoreByValue(false));
            jcache = manager.getCache("speed-test");
        }

        @TearDown
        public void end()
        {
            cachingProvider.close();
        }
    }

    @State(Scope.Benchmark)
    public static class LRUMemoryCacheState
    {
        private LRUMemoryCache<String, Integer> map;
        private final AtomicInteger count = new AtomicInteger(0);

        @Setup
        public void setup()
        {
            map = new LRUMemoryCache<String, Integer>();
            map.initialize(new CompositeCache<String, Integer>(new CompositeCacheAttributes(), new ElementAttributes()));
        }

        @TearDown
        public void end()
        {
            try
            {
                map.removeAll();
            }
            catch (final IOException e)
            {
                // no-op
            }
        }
    }

    @GenerateMicroBenchmark
    public void memCachePut(final LRUMemoryCacheState state)
    {
        final int i = state.count.incrementAndGet();
        try
        {
            state.map.update(new CacheElement<String, Integer>("speed-test", Integer.toString(i), i));
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GenerateMicroBenchmark
    public void memCacheGet(final LRUMemoryCacheState state)
    {
        try
        {
            state.map.get(RDM.nextInt(1000) + "");
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GenerateMicroBenchmark
    public void normalMapPut(final MapState state)
    {
        final int i = state.count.incrementAndGet();
        state.map.put(Integer.toString(i), i);
    }

    @GenerateMicroBenchmark
    public void normalMapPutGet(final MapState state)
    {
        final int i = state.count.incrementAndGet();
        final String key = Integer.toString(i);
        state.map.get(key);
        state.map.put(key, i);
    }

    @GenerateMicroBenchmark
    public void normalMapGet(final MapState state)
    {
        state.map.get(RDM.nextInt(1000) + "");
    }

    @GenerateMicroBenchmark
    public void jcsJCachePut(final JCacheState state)
    {
        final int i = state.count.incrementAndGet();
        state.jcache.put(Integer.toString(i), i);
    }

    @GenerateMicroBenchmark
    public void jcsJCacheGet(final JCacheState state)
    {
        state.jcache.get(RDM.nextInt(1000) + "");
    }

    public static void main(final String[] args) throws IOException, RunnerException
    {
        new Runner(new OptionsBuilder().include(".*" + Speed.class.getName() + ".*").warmupIterations(5).measurementIterations(5).forks(2)
                .build()).run();
    }
}
