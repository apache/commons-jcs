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
 *   http://www.apache.org/licenses/LICENSE-2.0
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
public class CompositeCacheAttributes
    implements ICompositeCacheAttributes
{
    /** Don't change */
    private static final long serialVersionUID = 6754049978134196787L;

    /** Default lateral switch */
    private static final boolean DEFAULT_USE_LATERAL = true;

    /** Default remote switch */
    private static final boolean DEFAULT_USE_REMOTE = true;

    /** Default disk switch */
    private static final boolean DEFAULT_USE_DISK = true;

    /** Default shrinker setting */
    private static final boolean DEFAULT_USE_SHRINKER = false;

    /** Default max objects value */
    private static final int DEFAULT_MAX_OBJECTS = 100;

    /** Default */
    private static final int DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS = 60 * 120;

    /** Default interval to run the shrinker */
    private static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 30;

    /** Default */
    private static final int DEFAULT_MAX_SPOOL_PER_RUN = -1;

    /** Default */
    private static final String DEFAULT_MEMORY_CACHE_NAME = "org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache";

    /** Default number to send to disk at a time when memory fills. */
    private static final int DEFAULT_CHUNK_SIZE = 2;

    /** Allow lateral caches */
    private boolean useLateral = DEFAULT_USE_LATERAL;

    /** Allow remote caches */
    private boolean useRemote = DEFAULT_USE_REMOTE;

    /** Whether we should use a disk cache if it is configured. */
    private boolean useDisk = DEFAULT_USE_DISK;

    /** Whether or not we should run the memory shrinker thread. */
    private boolean useMemoryShrinker = DEFAULT_USE_SHRINKER;

    /** The maximum objects that the memory cache will be allowed to hold. */
    private int maxObjs = DEFAULT_MAX_OBJECTS;

    /** MaxMemoryIdleTimeSeconds */
    private long maxMemoryIdleTimeSeconds = DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS;

    /** ShrinkerIntervalSeconds */
    private long shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;

    /** The maximum number the shrinker will spool to disk per run. */
    private int maxSpoolPerRun = DEFAULT_MAX_SPOOL_PER_RUN;

    /** The name of this cache region. */
    private String cacheName;

    /** The name of the memory cache implementation class. */
    private String memoryCacheName;

    /** Set via DISK_USAGE_PATTERN_NAME */
    private DiskUsagePattern diskUsagePattern = DiskUsagePattern.SWAP;

    /** How many to spool to disk at a time. */
    private int spoolChunkSize = DEFAULT_CHUNK_SIZE;

    /**
     * Constructor for the CompositeCacheAttributes object
     */
    public CompositeCacheAttributes()
    {
        // set this as the default so the configuration is a bit simpler
        memoryCacheName = DEFAULT_MEMORY_CACHE_NAME;
    }

    /**
     * @see Object#clone()
     */
    @Override
    public ICompositeCacheAttributes clone()
    {
        try
        {
            return (ICompositeCacheAttributes)super.clone();
        }
        catch (final CloneNotSupportedException e)
        {
            throw new IllegalStateException("Clone not supported. This should never happen.", e);
        }
    }

    /**
     * Gets the cacheName attribute of the CompositeCacheAttributes object
     *
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * @return the diskUsagePattern.
     */
    @Override
    public DiskUsagePattern getDiskUsagePattern()
    {
        return diskUsagePattern;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     *
     * @return The MaxMemoryIdleTimeSeconds value
     */
    @Override
    public long getMaxMemoryIdleTimeSeconds()
    {
        return this.maxMemoryIdleTimeSeconds;
    }

    /**
     * Gets the maxObjects attribute of the CompositeCacheAttributes object
     *
     * @return The maxObjects value
     */
    @Override
    public int getMaxObjects()
    {
        return this.maxObjs;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This gets the maximum number of items to spool per run.
     *
     * @return The maxSpoolPerRun value
     */
    @Override
    public int getMaxSpoolPerRun()
    {
        return this.maxSpoolPerRun;
    }

    /**
     * Gets the memoryCacheName attribute of the CompositeCacheAttributes object
     *
     * @return The memoryCacheName value
     */
    @Override
    public String getMemoryCacheName()
    {
        return this.memoryCacheName;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This gets the shrinker interval.
     *
     * @return The ShrinkerIntervalSeconds value
     */
    @Override
    public long getShrinkerIntervalSeconds()
    {
        return this.shrinkerIntervalSeconds;
    }

    /**
     * Number to send to disk at the time when memory is full.
     *
     * @return int
     */
    @Override
    public int getSpoolChunkSize()
    {
        return spoolChunkSize;
    }

    /**
     * Gets the useDisk attribute of the CompositeCacheAttributes object
     *
     * @return The useDisk value
     */
    @Override
    public boolean isUseDisk()
    {
        return useDisk;
    }

    /**
     * Gets the useLateral attribute of the CompositeCacheAttributes object
     *
     * @return The useLateral value
     */
    @Override
    public boolean isUseLateral()
    {
        return this.useLateral;
    }

    /**
     * Tests whether the memory cache should perform background memory shrinkage.
     *
     * @return The UseMemoryShrinker value
     */
    @Override
    public boolean isUseMemoryShrinker()
    {
        return this.useMemoryShrinker;
    }

    /**
     * Gets the useRemote attribute of the CompositeCacheAttributes object
     *
     * @return The useRemote value
     */
    @Override
    public boolean isUseRemote()
    {
        return this.useRemote;
    }

    /**
     * Sets the cacheName attribute of the CompositeCacheAttributes object
     *
     * @param s The new cacheName value
     */
    @Override
    public void setCacheName( final String s )
    {
        this.cacheName = s;
    }

    /**
     * By default this is SWAP_ONLY.
     *
     * @param diskUsagePattern The diskUsagePattern to set.
     */
    @Override
    public void setDiskUsagePattern( final DiskUsagePattern diskUsagePattern )
    {
        this.diskUsagePattern = diskUsagePattern;
    }

    /**
     * Translates the name to the disk usage pattern short value.
     * <p>
     * The allowed values are SWAP and UPDATE.
     *
     * @param diskUsagePatternName The diskUsagePattern to set.
     */
    @Override
    public void setDiskUsagePatternName( final String diskUsagePatternName )
    {
        if ( diskUsagePatternName != null )
        {
            final String name = diskUsagePatternName.toUpperCase().trim();
            if ( name.startsWith( "SWAP" ) )
            {
                setDiskUsagePattern( DiskUsagePattern.SWAP );
            }
            else if ( name.startsWith( "UPDATE" ) )
            {
                setDiskUsagePattern( DiskUsagePattern.UPDATE );
            }
        }
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     *
     * @param seconds The new MaxMemoryIdleTimeSeconds value
     */
    @Override
    public void setMaxMemoryIdleTimeSeconds( final long seconds )
    {
        this.maxMemoryIdleTimeSeconds = seconds;
    }

    /**
     * Sets the maxObjects attribute of the CompositeCacheAttributes object
     *
     * @param maxObjs The new maxObjects value
     */
    @Override
    public void setMaxObjects( final int maxObjs )
    {
        this.maxObjs = maxObjs;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This sets the maximum number of items to spool per run.
     * <p>
     * If the value is -1, then there is no limit to the number of items to be spooled.
     *
     * @param maxSpoolPerRun The new maxSpoolPerRun value
     */
    @Override
    public void setMaxSpoolPerRun( final int maxSpoolPerRun )
    {
        this.maxSpoolPerRun = maxSpoolPerRun;
    }

    /**
     * Sets the memoryCacheName attribute of the CompositeCacheAttributes object
     *
     * @param s The new memoryCacheName value
     */
    @Override
    public void setMemoryCacheName( final String s )
    {
        this.memoryCacheName = s;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This sets the shrinker interval.
     *
     * @param seconds The new ShrinkerIntervalSeconds value
     */
    @Override
    public void setShrinkerIntervalSeconds( final long seconds )
    {
        this.shrinkerIntervalSeconds = seconds;
    }

    /**
     * Number to send to disk at a time.
     *
     * @param spoolChunkSize
     */
    @Override
    public void setSpoolChunkSize( final int spoolChunkSize )
    {
        this.spoolChunkSize = spoolChunkSize;
    }

    /**
     * Sets the useDisk attribute of the CompositeCacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    @Override
    public void setUseDisk( final boolean useDisk )
    {
        this.useDisk = useDisk;
    }

    /**
     * Sets the useLateral attribute of the CompositeCacheAttributes object
     *
     * @param b The new useLateral value
     */
    @Override
    public void setUseLateral( final boolean b )
    {
        this.useLateral = b;
    }

    /**
     * Sets whether the memory cache should perform background memory shrinkage.
     *
     * @param useShrinker The new UseMemoryShrinker value
     */
    @Override
    public void setUseMemoryShrinker( final boolean useShrinker )
    {
        this.useMemoryShrinker = useShrinker;
    }

    /**
     * Sets the useRemote attribute of the CompositeCacheAttributes object
     *
     * @param useRemote The new useRemote value
     */
    @Override
    public void setUseRemote( final boolean useRemote )
    {
        this.useRemote = useRemote;
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
        dump.append( ", useRemote = " ).append( useRemote );
        dump.append( ", useDisk = " ).append( useDisk );
        dump.append( ", maxObjs = " ).append( maxObjs );
        dump.append( ", maxSpoolPerRun = " ).append( maxSpoolPerRun );
        dump.append( ", diskUsagePattern = " ).append( diskUsagePattern );
        dump.append( ", spoolChunkSize = " ).append( spoolChunkSize );
        dump.append( " ]" );

        return dump.toString();
    }
}
