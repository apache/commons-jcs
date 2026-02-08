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

import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;

/**
 * The CompositeCacheAttributes defines the general cache region settings. If a region is not
 * explicitly defined in the cache.ccf then it inherits the cache default settings.
 * <p>
 * If all the default attributes are not defined in the default region definition in the cache.ccf,
 * the hard coded defaults will be used.
 */
public record CompositeCacheAttributes(
        /** The name of this cache region. */
        String cacheName,

        /** The maximum objects that the memory cache will be allowed to hold. */
        int maxObjects,

        /** Allow lateral caches */
        boolean useLateral,

        /** Whether we should use a disk cache if it is configured. */
        boolean useDisk,

        /** Whether or not we should run the memory shrinker thread. */
        boolean useMemoryShrinker,

        /** ShrinkerIntervalSeconds */
        long shrinkerIntervalSeconds,

        /** The maximum number the shrinker will spool to disk per run. */
        int maxSpoolPerRun,

        /** MaxMemoryIdleTimeSeconds */
        long maxMemoryIdleTimeSeconds,

        /** The name of the memory cache implementation class. */
        String memoryCacheName,

        /** Set via DISK_USAGE_PATTERN_NAME */
        DiskUsagePattern diskUsagePattern,

        /** How many to spool to disk at a time. */
        int spoolChunkSize
) implements ICompositeCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = 6754049978134196787L;

    /** Default max objects value */
    private static final int DEFAULT_MAX_OBJECTS = 100;

    /** Default lateral switch */
    private static final boolean DEFAULT_USE_LATERAL = true;

    /** Default disk switch */
    private static final boolean DEFAULT_USE_DISK = true;

    /** Default shrinker setting */
    private static final boolean DEFAULT_USE_SHRINKER = false;

    /** Default interval to run the shrinker */
    private static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 30;

    /** Default */
    private static final int DEFAULT_MAX_SPOOL_PER_RUN = -1;

    /** Default */
    private static final int DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS = 60 * 120;

    /** Default */
    private static final String DEFAULT_MEMORY_CACHE_NAME = "org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache";

    /** Default number to send to disk at a time when memory fills. */
    private static final int DEFAULT_CHUNK_SIZE = 2;

    /** Record with all defaults set */
    private static final CompositeCacheAttributes DEFAULT = new CompositeCacheAttributes(
            null,
            DEFAULT_MAX_OBJECTS,
            DEFAULT_USE_LATERAL,
            DEFAULT_USE_DISK,
            DEFAULT_USE_SHRINKER,
            DEFAULT_SHRINKER_INTERVAL_SECONDS,
            DEFAULT_MAX_SPOOL_PER_RUN,
            DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS,
            DEFAULT_MEMORY_CACHE_NAME,
            DiskUsagePattern.SWAP,
            DEFAULT_CHUNK_SIZE
          );

    /**
     * @return an object containing the default settings
     */
    public static CompositeCacheAttributes defaults()
    {
        return DEFAULT;
    }

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s
     *            The new cacheName value
     */
    @Override
    public CompositeCacheAttributes withCacheName(String s)
    {
        return new CompositeCacheAttributes(s,
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds(),
                memoryCacheName(),
                diskUsagePattern(),
                spoolChunkSize());
    }

    /**
     * Sets the size of the cache.
     *
     * @param maxObjects the new maxObjects value
     */
    public CompositeCacheAttributes withMaxObjects(int maxObjects)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects,
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds(),
                memoryCacheName(),
                diskUsagePattern(),
                spoolChunkSize());
    }

    /**
     * Sets the maximum number of elements to spool per shrinker run.
     *
     * @param maxSpoolPerRun the new maxSpoolPerRun value
     */
    public CompositeCacheAttributes withMaxSpoolPerRun(int maxSpoolPerRun)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun,
                maxMemoryIdleTimeSeconds(),
                memoryCacheName(),
                diskUsagePattern(),
                spoolChunkSize());
    }

    /**
     * Sets the maximum memory idle-time in seconds of the cache.
     *
     * @param maxMemoryIdleTimeSeconds the new maxMemoryIdleTimeSeconds value
     */
    public CompositeCacheAttributes withMaxMemoryIdleTimeSeconds(long maxMemoryIdleTimeSeconds)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds,
                memoryCacheName(),
                diskUsagePattern(),
                spoolChunkSize());
    }

    /**
     * Sets the memory cache name of the cache.
     *
     * @param memoryCacheName the new memoryCacheName value
     */
    public CompositeCacheAttributes withMemoryCacheName(String memoryCacheName)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds(),
                memoryCacheName,
                diskUsagePattern(),
                spoolChunkSize());
    }

    /**
     * Sets the disk usage pattern of the cache.
     *
     * @param diskUsagePattern the new diskUsagePattern value
     */
    public CompositeCacheAttributes withDiskUsagePattern(DiskUsagePattern diskUsagePattern)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds(),
                memoryCacheName(),
                diskUsagePattern,
                spoolChunkSize());
    }

    /**
     * Sets the spool chunk size of the cache.
     *
     * @param spoolChunkSize the new spoolChunkSize value
     */
    public CompositeCacheAttributes withSpoolChunkSize(int spoolChunkSize)
    {
        return new CompositeCacheAttributes(cacheName(),
                maxObjects(),
                useLateral(),
                useDisk(),
                useMemoryShrinker(),
                shrinkerIntervalSeconds(),
                maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds(),
                memoryCacheName(),
                diskUsagePattern(),
                spoolChunkSize);
    }

    /**
     * Dumps the core attributes.
     *
     * @return For debugging.
     */
    @Override
    public String toString()
    {
        final StringBuilder dump = new StringBuilder();

        dump.append( "[ " );
        dump.append( "useLateral = " ).append( useLateral );
        dump.append( ", useDisk = " ).append( useDisk );
        dump.append( ", maxObjects = " ).append( maxObjects );
        dump.append( ", maxSpoolPerRun = " ).append( maxSpoolPerRun );
        dump.append( ", diskUsagePattern = " ).append( diskUsagePattern );
        dump.append( ", spoolChunkSize = " ).append( spoolChunkSize );
        dump.append( " ]" );

        return dump.toString();
    }
}
