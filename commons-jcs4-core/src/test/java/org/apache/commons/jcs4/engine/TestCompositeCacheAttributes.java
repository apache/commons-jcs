package org.apache.commons.jcs4.engine;

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

import static org.apache.commons.jcs4.engine.CompositeCacheAttributes.defaults;

import java.time.Duration;

import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;

/**
 * The CompositeCacheAttributes defines the general cache region settings. If a region is not
 * explicitly defined in the cache.ccf then it inherits the cache default settings.
 * <p>
 * If all the default attributes are not defined in the default region definition in the cache.ccf,
 * the hard coded defaults will be used.
 */
public class TestCompositeCacheAttributes
{
    /**
     * Get a CompositeCacheAttributes object suitable for tests
     *
     * @param memoryCacheName The new memoryCacheName value
     * @param maxObjects The new maxObjects value
     */
    public static CompositeCacheAttributes withMemoryCacheNameAndMaxObjects(String memoryCacheName, int maxObjects)
    {
        return new CompositeCacheAttributes(defaults().cacheName(),
                maxObjects,
                defaults().UseMemoryShrinker(),
                defaults().ShrinkerInterval(),
                defaults().MaxSpoolPerRun(),
                defaults().MaxMemoryIdleTime(),
                memoryCacheName,
                defaults().DiskUsagePattern(),
                defaults().SpoolChunkSize(),
                defaults().Shards());
    }

    /**
     * Get a CompositeCacheAttributes object suitable for tests
     *
     * @param memoryCacheName The new memoryCacheName value
     * @param maxMemoryIdleTime The new maxMemoryIdleTime value
     * @param maxSpoolPerRun The new maxSpoolPerRun value
     */
    public static CompositeCacheAttributes withMemoryCacheNameMaxMemoryIdleTimeAndMaxSpoolPerRun(
            String memoryCacheName, Duration maxMemoryIdleTime, int maxSpoolPerRun)
    {
        return new CompositeCacheAttributes(defaults().cacheName(),
                defaults().MaxObjects(),
                defaults().UseMemoryShrinker(),
                defaults().ShrinkerInterval(),
                maxSpoolPerRun,
                maxMemoryIdleTime,
                memoryCacheName,
                defaults().DiskUsagePattern(),
                defaults().SpoolChunkSize(),
                defaults().Shards());
    }

    /**
     * Get a CompositeCacheAttributes object suitable for tests
     *
     * @param maxObjects The new maxObjects value
     * @param spoolChunkSize The new spoolChunkSize value
     */
    public static CompositeCacheAttributes withMaxObjectsAndSpoolChunkSize(int maxObjects, int spoolChunkSize)
    {
        return new CompositeCacheAttributes(defaults().cacheName(),
                maxObjects,
                defaults().UseMemoryShrinker(),
                defaults().ShrinkerInterval(),
                defaults().MaxSpoolPerRun(),
                defaults().MaxMemoryIdleTime(),
                defaults().MemoryCacheName(),
                defaults().DiskUsagePattern(),
                spoolChunkSize,
                defaults().Shards());
    }

    /**
     * Sets the maximum memory idle-time of the cache.
     *
     * @param maxMemoryIdleTime The new maxMemoryIdleTime value
     */
    public static CompositeCacheAttributes withMaxMemoryIdleTime(Duration maxMemoryIdleTime)
    {
        return new CompositeCacheAttributes(defaults().cacheName(),
                defaults().MaxObjects(),
                defaults().UseMemoryShrinker(),
                defaults().ShrinkerInterval(),
                defaults().MaxSpoolPerRun(),
                maxMemoryIdleTime,
                defaults().MemoryCacheName(),
                defaults().DiskUsagePattern(),
                defaults().SpoolChunkSize(),
                defaults().Shards());
    }

    /**
     * Sets the disk usage pattern of the cache.
     *
     * @param diskUsagePattern The new diskUsagePattern value
     */
    public static CompositeCacheAttributes withDiskUsagePattern(ICompositeCacheAttributes.DiskUsagePatternEnum diskUsagePattern)
    {
        return new CompositeCacheAttributes(defaults().cacheName(),
                defaults().MaxObjects(),
                defaults().UseMemoryShrinker(),
                defaults().ShrinkerInterval(),
                defaults().MaxSpoolPerRun(),
                defaults().MaxMemoryIdleTime(),
                defaults().MemoryCacheName(),
                diskUsagePattern,
                defaults().SpoolChunkSize(),
                defaults().Shards());
    }
}
