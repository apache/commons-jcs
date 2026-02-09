package org.apache.commons.jcs4.engine;

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
     * @param memoryCacheName the new memoryCacheName value
     * @param maxObjects the new maxObjects value
     */
    public static CompositeCacheAttributes withMemoryCacheNameAndMaxObjects(String memoryCacheName, int maxObjects)
    {
        return new CompositeCacheAttributes(CompositeCacheAttributes.defaults().cacheName(),
                maxObjects,
                CompositeCacheAttributes.defaults().useLateral(),
                CompositeCacheAttributes.defaults().useDisk(),
                CompositeCacheAttributes.defaults().useMemoryShrinker(),
                CompositeCacheAttributes.defaults().shrinkerIntervalSeconds(),
                CompositeCacheAttributes.defaults().maxSpoolPerRun(),
                CompositeCacheAttributes.defaults().maxMemoryIdleTimeSeconds(),
                memoryCacheName,
                CompositeCacheAttributes.defaults().diskUsagePattern(),
                CompositeCacheAttributes.defaults().spoolChunkSize());
    }

    /**
     * Get a CompositeCacheAttributes object suitable for tests
     *
     * @param memoryCacheName the new memoryCacheName value
     * @param maxMemoryIdleTimeSeconds the new maxMemoryIdleTimeSeconds value
     * @param maxSpoolPerRun the new maxSpoolPerRun value
     */
    public static CompositeCacheAttributes withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
            String memoryCacheName, long maxMemoryIdleTimeSeconds, int maxSpoolPerRun)
    {
        return new CompositeCacheAttributes(CompositeCacheAttributes.defaults().cacheName(),
                CompositeCacheAttributes.defaults().maxObjects(),
                CompositeCacheAttributes.defaults().useLateral(),
                CompositeCacheAttributes.defaults().useDisk(),
                CompositeCacheAttributes.defaults().useMemoryShrinker(),
                CompositeCacheAttributes.defaults().shrinkerIntervalSeconds(),
                maxSpoolPerRun,
                maxMemoryIdleTimeSeconds,
                memoryCacheName,
                CompositeCacheAttributes.defaults().diskUsagePattern(),
                CompositeCacheAttributes.defaults().spoolChunkSize());
    }

    /**
     * Get a CompositeCacheAttributes object suitable for tests
     *
     * @param maxObjects the new maxObjects value
     * @param spoolChunkSize the new spoolChunkSize value
     */
    public static CompositeCacheAttributes withMaxObjectsAndSpoolChunkSize(int maxObjects, int spoolChunkSize)
    {
        return new CompositeCacheAttributes(CompositeCacheAttributes.defaults().cacheName(),
                maxObjects,
                CompositeCacheAttributes.defaults().useLateral(),
                CompositeCacheAttributes.defaults().useDisk(),
                CompositeCacheAttributes.defaults().useMemoryShrinker(),
                CompositeCacheAttributes.defaults().shrinkerIntervalSeconds(),
                CompositeCacheAttributes.defaults().maxSpoolPerRun(),
                CompositeCacheAttributes.defaults().maxMemoryIdleTimeSeconds(),
                CompositeCacheAttributes.defaults().memoryCacheName(),
                CompositeCacheAttributes.defaults().diskUsagePattern(),
                spoolChunkSize);
    }

    /**
     * Sets the maximum memory idle-time in seconds of the cache.
     *
     * @param maxMemoryIdleTimeSeconds the new maxMemoryIdleTimeSeconds value
     */
    public static CompositeCacheAttributes withMaxMemoryIdleTimeSeconds(long maxMemoryIdleTimeSeconds)
    {
        return new CompositeCacheAttributes(CompositeCacheAttributes.defaults().cacheName(),
                CompositeCacheAttributes.defaults().maxObjects(),
                CompositeCacheAttributes.defaults().useLateral(),
                CompositeCacheAttributes.defaults().useDisk(),
                CompositeCacheAttributes.defaults().useMemoryShrinker(),
                CompositeCacheAttributes.defaults().shrinkerIntervalSeconds(),
                CompositeCacheAttributes.defaults().maxSpoolPerRun(),
                maxMemoryIdleTimeSeconds,
                CompositeCacheAttributes.defaults().memoryCacheName(),
                CompositeCacheAttributes.defaults().diskUsagePattern(),
                CompositeCacheAttributes.defaults().spoolChunkSize());
    }

    /**
     * Sets the disk usage pattern of the cache.
     *
     * @param diskUsagePattern the new diskUsagePattern value
     */
    public static CompositeCacheAttributes withDiskUsagePattern(ICompositeCacheAttributes.DiskUsagePattern diskUsagePattern)
    {
        return new CompositeCacheAttributes(CompositeCacheAttributes.defaults().cacheName(),
                CompositeCacheAttributes.defaults().maxObjects(),
                CompositeCacheAttributes.defaults().useLateral(),
                CompositeCacheAttributes.defaults().useDisk(),
                CompositeCacheAttributes.defaults().useMemoryShrinker(),
                CompositeCacheAttributes.defaults().shrinkerIntervalSeconds(),
                CompositeCacheAttributes.defaults().maxSpoolPerRun(),
                CompositeCacheAttributes.defaults().maxMemoryIdleTimeSeconds(),
                CompositeCacheAttributes.defaults().memoryCacheName(),
                diskUsagePattern,
                CompositeCacheAttributes.defaults().spoolChunkSize());
    }
}
