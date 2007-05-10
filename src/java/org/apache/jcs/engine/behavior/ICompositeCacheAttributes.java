package org.apache.jcs.engine.behavior;

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

import java.io.Serializable;

/**
 * This defines the minimla behavior for the Cache Configuration settings.
 */
public interface ICompositeCacheAttributes
    extends Serializable
{
    /** Items will only go to disk when the memory limit is reached. This is the default. */
    public static final short DISK_USAGE_PATTERN_SWAP = 0;

    /**
     * Items will go to disk on a normal put. If The disk usage pattern is UPDATE, the swap will be
     * disabled.
     */
    public static final short DISK_USAGE_PATTERN_UPDATE = 1;

    /**
     * SetMaxObjects is used to set the attribute to determine the maximum
     * number of objects allowed in the memory cache. If the max number of
     * objects or the cache size is set, the default for the one not set is
     * ignored. If both are set, both are used to determine the capacity of the
     * cache, i.e., object will be removed from the cache if either limit is
     * reached. TODO: move to MemoryCache config file.
     * <p>
     * @param size
     *            The new maxObjects value
     */
    public void setMaxObjects( int size );

    /**
     * Gets the maxObjects attribute of the ICompositeCacheAttributes object
     * <p>
     * @return The maxObjects value
     */
    public int getMaxObjects();

    /**
     * Sets the useDisk attribute of the ICompositeCacheAttributes object
     * <p>
     * @param useDisk
     *            The new useDisk value
     */
    public void setUseDisk( boolean useDisk );

    /**
     * Gets the useDisk attribute of the ICompositeCacheAttributes object
     * <p>
     * @return The useDisk value
     */
    public boolean getUseDisk();

    /**
     * set whether the cache should use a lateral cache
     * <p>
     * @param d
     *            The new useLateral value
     */
    public void setUseLateral( boolean d );

    /**
     * Gets the useLateral attribute of the ICompositeCacheAttributes object
     * <p>
     * @return The useLateral value
     */
    public boolean getUseLateral();

    /**
     * Sets whether the cache is remote enabled
     * <p>
     * @param isRemote
     *            The new useRemote value
     */
    public void setUseRemote( boolean isRemote );

    /**
     * returns whether the cache is remote enabled
     * <p>
     * @return The useRemote value
     */
    public boolean getUseRemote();

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     * <p>
     * @param s
     *            The new cacheName value
     */
    public void setCacheName( String s );

    /**
     * Gets the cacheName attribute of the ICompositeCacheAttributes object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Sets the name of the MemoryCache, referenced by the appropriate manager.
     * TODO: create a separate memory cache attribute class.
     * <p>
     * @param s
     *            The new memoryCacheName value
     */
    public void setMemoryCacheName( String s );

    /**
     * Gets the memoryCacheName attribute of the ICompositeCacheAttributes
     * object
     * <p>
     * @return The memoryCacheName value
     */
    public String getMemoryCacheName();

    /**
     * Whether the memory cache should perform background memory shrinkage.
     * <p>
     * @param useShrinker
     *            The new UseMemoryShrinker value
     */
    public void setUseMemoryShrinker( boolean useShrinker );

    /**
     * Whether the memory cache should perform background memory shrinkage.
     * <p>
     * @return The UseMemoryShrinker value
     */
    public boolean getUseMemoryShrinker();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     * <p>
     * @param seconds
     *            The new MaxMemoryIdleTimeSeconds value
     */
    public void setMaxMemoryIdleTimeSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     * <p>
     * @return The MaxMemoryIdleTimeSeconds value
     */
    public long getMaxMemoryIdleTimeSeconds();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the shrinker interval.
     * <p>
     * @param seconds
     *            The new ShrinkerIntervalSeconds value
     */
    public void setShrinkerIntervalSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the shrinker interval.
     * <p>
     * @return The ShrinkerIntervalSeconds value
     */
    public long getShrinkerIntervalSeconds();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the maximum number of items to spool per run.
     * <p>
     * @param maxSpoolPerRun
     *            The new maxSpoolPerRun value
     */
    public void setMaxSpoolPerRun( int maxSpoolPerRun );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the maximum number of items to spool per run.
     * <p>
     * @return The maxSpoolPerRun value
     */
    public int getMaxSpoolPerRun();

    /**
     * Clones the attributes.
     * <p>
     * @return a new object with the same settings.
     */
    public ICompositeCacheAttributes copy();

    /**
     * By default this is SWAP_ONLY.
     * <p>
     * @param diskUsagePattern The diskUsagePattern to set.
     */
    public void setDiskUsagePattern( short diskUsagePattern );

    /**
     * Translates the name to the disk usage pattern short value.
     * <p>
     * The allowed values are SWAP and UPDATE.
     * <p>
     * @param diskUsagePatternName The diskUsagePattern to set.
     */
    public void setDiskUsagePatternName( String diskUsagePatternName );

    /**
     * @return Returns the diskUsagePattern.
     */
    public short getDiskUsagePattern();
}
