package org.apache.jcs.engine;

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

import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

/**
 * The CompositeCacheAttributes defines the general cache region settings. If a region is not
 * explicitly defined in the cache.ccf then it inherits the cache default settings.
 * <p>
 * If all the default attributes are not defined in the default region definition in the cache.ccf,
 * the hard coded defaults will be used.
 */
public class CompositeCacheAttributes
    implements ICompositeCacheAttributes, Cloneable
{
    private static final long serialVersionUID = 6754049978134196787L;

    private static final boolean DEFAULT_USE_LATERAL = true;

    private static final boolean DEFAULT_USE_REMOTE = true;

    private static final boolean DEFAULT_USE_DISK = true;

    private static final boolean DEFAULT_USE_SHRINKER = false;

    private static final int DEFAULT_MAX_OBJECTS = 100;

    private static final int DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS = 60 * 120;

    private static final int DEFAULT_SHRINKER_INTERVAL_SECONDS = 30;

    private static final int DEFAULT_MAX_SPOOL_PER_RUN = -1;

    private static final String DEFAULT_MEMORY_CACHE_NAME = "org.apache.jcs.engine.memory.lru.LRUMemoryCache";

    private boolean useLateral = DEFAULT_USE_LATERAL;

    private boolean useRemote = DEFAULT_USE_REMOTE;

    /** Whether we should use a disk cache if it is configured. */
    private boolean useDisk = DEFAULT_USE_DISK;

    /** Whether or not we should run the memory shrinker thread. */
    private boolean useMemoryShrinker = DEFAULT_USE_SHRINKER;

    /** The maximum objects that the memory cache will be allowed to hold. */
    private int maxObjs = DEFAULT_MAX_OBJECTS;

    /** maxMemoryIdleTimeSeconds */
    private long maxMemoryIdleTimeSeconds = DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS;

    /** shrinkerIntervalSeconds */
    private long shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;

    /** The maximum number the shrinker will spool to disk per run. */
    private int maxSpoolPerRun = DEFAULT_MAX_SPOOL_PER_RUN;

    /** The name of this cache region. */
    private String cacheName;

    /** The name of the memory cache implementation class. */
    private String memoryCacheName;

    private short diskUsagePattern = DISK_USAGE_PATTERN_SWAP;

    /**
     * Constructor for the CompositeCacheAttributes object
     */
    public CompositeCacheAttributes()
    {
        super();
        // set this as the default so the configuration is a bit simpler
        memoryCacheName = DEFAULT_MEMORY_CACHE_NAME;
    }

    /**
     * Sets the maxObjects attribute of the CompositeCacheAttributes object
     * <p>
     * @param maxObjs The new maxObjects value
     */
    public void setMaxObjects( int maxObjs )
    {
        this.maxObjs = maxObjs;
    }

    /**
     * Gets the maxObjects attribute of the CompositeCacheAttributes object
     * <p>
     * @return The maxObjects value
     */
    public int getMaxObjects()
    {
        return this.maxObjs;
    }

    /**
     * Sets the useDisk attribute of the CompositeCacheAttributes object
     * <p>
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk )
    {
        this.useDisk = useDisk;
    }

    /**
     * Gets the useDisk attribute of the CompositeCacheAttributes object
     * <p>
     * @return The useDisk value
     */
    public boolean getUseDisk()
    {
        return useDisk;
    }

    /**
     * Sets the useLateral attribute of the CompositeCacheAttributes object
     * <p>
     * @param b The new useLateral value
     */
    public void setUseLateral( boolean b )
    {
        this.useLateral = b;
    }

    /**
     * Gets the useLateral attribute of the CompositeCacheAttributes object
     * <p>
     * @return The useLateral value
     */
    public boolean getUseLateral()
    {
        return this.useLateral;
    }

    /**
     * Sets the useRemote attribute of the CompositeCacheAttributes object
     * <p>
     * @param useRemote The new useRemote value
     */
    public void setUseRemote( boolean useRemote )
    {
        this.useRemote = useRemote;
    }

    /**
     * Gets the useRemote attribute of the CompositeCacheAttributes object
     * <p>
     * @return The useRemote value
     */
    public boolean getUseRemote()
    {
        return this.useRemote;
    }

    /**
     * Sets the cacheName attribute of the CompositeCacheAttributes object
     * <p>
     * @param s The new cacheName value
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }

    /**
     * Gets the cacheName attribute of the CompositeCacheAttributes object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Sets the memoryCacheName attribute of the CompositeCacheAttributes object
     * <p>
     * @param s The new memoryCacheName value
     */
    public void setMemoryCacheName( String s )
    {
        this.memoryCacheName = s;
    }

    /**
     * Gets the memoryCacheName attribute of the CompositeCacheAttributes object
     * <p>
     * @return The memoryCacheName value
     */
    public String getMemoryCacheName()
    {
        return this.memoryCacheName;
    }

    /**
     * Whether the memory cache should perform background memory shrinkage.
     * <p>
     * @param useShrinker The new UseMemoryShrinker value
     */
    public void setUseMemoryShrinker( boolean useShrinker )
    {
        this.useMemoryShrinker = useShrinker;
    }

    /**
     * Whether the memory cache should perform background memory shrinkage.
     * <p>
     * @return The UseMemoryShrinker value
     */
    public boolean getUseMemoryShrinker()
    {
        return this.useMemoryShrinker;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * <p>
     * @param seconds The new MaxMemoryIdleTimeSeconds value
     */
    public void setMaxMemoryIdleTimeSeconds( long seconds )
    {
        this.maxMemoryIdleTimeSeconds = seconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * <p>
     * @return The MaxMemoryIdleTimeSeconds value
     */
    public long getMaxMemoryIdleTimeSeconds()
    {
        return this.maxMemoryIdleTimeSeconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This sets the shrinker interval.
     * <p>
     * @param seconds The new ShrinkerIntervalSeconds value
     */
    public void setShrinkerIntervalSeconds( long seconds )
    {
        this.shrinkerIntervalSeconds = seconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This gets the shrinker interval.
     * <p>
     * @return The ShrinkerIntervalSeconds value
     */
    public long getShrinkerIntervalSeconds()
    {
        return this.shrinkerIntervalSeconds;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This sets the maximum number of items to spool per run.
     * <p>
     * If the value is -1, then there is no limit to the number of items to be spooled.
     * <p>
     * @param maxSpoolPerRun The new maxSpoolPerRun value
     */
    public void setMaxSpoolPerRun( int maxSpoolPerRun )
    {
        this.maxSpoolPerRun = maxSpoolPerRun;
    }

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements to reclaim space.
     * This gets the maximum number of items to spool per run.
     * <p>
     * @return The maxSpoolPerRun value
     */
    public int getMaxSpoolPerRun()
    {
        return this.maxSpoolPerRun;
    }

    /**
     * By default this is SWAP_ONLY.
     * <p>
     * @param diskUsagePattern The diskUsagePattern to set.
     */
    public void setDiskUsagePattern( short diskUsagePattern )
    {
        this.diskUsagePattern = diskUsagePattern;
    }

    /**
     * Translates the name to the disk usage pattern short value.
     * <p>
     * The allowed values are SWAP and UPDATE.
     * <p>
     * @param diskUsagePatternName The diskUsagePattern to set.
     */
    public void setDiskUsagePatternName( String diskUsagePatternName )
    {
        if ( diskUsagePatternName != null )
        {
            diskUsagePatternName = diskUsagePatternName.toUpperCase().trim();
            if ( diskUsagePatternName.startsWith( "SWAP" ) )
            {
                this.setDiskUsagePattern( DISK_USAGE_PATTERN_SWAP );
            }
            else if ( diskUsagePatternName.startsWith( "UPDATE" ) )
            {
                this.setDiskUsagePattern( DISK_USAGE_PATTERN_UPDATE );
            }
        }
    }

    /**
     * @return Returns the diskUsagePattern.
     */
    public short getDiskUsagePattern()
    {
        return diskUsagePattern;
    }

    /**
     * Description of the Method
     * <p>
     * @return
     */
    public ICompositeCacheAttributes copy()
    {
        try
        {
            ICompositeCacheAttributes cattr = (CompositeCacheAttributes) this.clone();
            return cattr;
        }
        catch ( Exception e )
        {
            System.err.println( e.toString() );
            return new CompositeCacheAttributes();
        }
    }

    /**
     * Dumps the core attributes.
     * <p>
     * @return For debugging.
     */
    public String toString()
    {
        StringBuffer dump = new StringBuffer();

        dump.append( "[ " );
        dump.append( "useLateral = " ).append( useLateral );
        dump.append( ", useRemote = " ).append( useRemote );
        dump.append( ", useDisk = " ).append( useDisk );
        dump.append( ", maxObjs = " ).append( maxObjs );
        dump.append( ", maxSpoolPerRun = " ).append( maxSpoolPerRun );
        dump.append( ", diskUsagePattern = " ).append( diskUsagePattern );
        dump.append( " ]" );

        return dump.toString();
    }
}
