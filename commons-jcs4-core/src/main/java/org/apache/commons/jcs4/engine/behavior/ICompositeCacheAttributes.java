package org.apache.commons.jcs4.engine.behavior;

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

import java.io.Serializable;

/**
 * This defines the minimal behavior for the Cache Configuration settings.
 */
public interface ICompositeCacheAttributes
    extends Serializable, Cloneable
{
    enum DiskUsagePattern
    {
        /** Items will only go to disk when the memory limit is reached. This is the default. */
        SWAP,

        /**
         * Items will go to disk on a normal put. If The disk usage pattern is UPDATE, the swap will be
         * disabled.
         */
        UPDATE
    }

    /**
     * Clone object
     */
    ICompositeCacheAttributes clone();

    /**
     * Gets the cacheName attribute of the ICompositeCacheAttributes object
     *
     * @return The cacheName value
     */
    String getCacheName();

    /**
     * @return the diskUsagePattern.
     */
    DiskUsagePattern getDiskUsagePattern();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @return The MaxMemoryIdleTimeSeconds value
     */
    long getMaxMemoryIdleTimeSeconds();

    /**
     * Gets the maxObjects attribute of the ICompositeCacheAttributes object
     *
     * @return The maxObjects value
     */
    int getMaxObjects();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the maximum number of items to spool per run.
     *
     * @return The maxSpoolPerRun value
     */
    int getMaxSpoolPerRun();

    /**
     * Gets the memoryCacheName attribute of the ICompositeCacheAttributes
     * object
     *
     * @return The memoryCacheName value
     */
    String getMemoryCacheName();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the shrinker interval.
     *
     * @return The ShrinkerIntervalSeconds value
     */
    long getShrinkerIntervalSeconds();

    /**
     * Number to send to disk at time when memory is full.
     *
     * @return int
     */
    int getSpoolChunkSize();

    /**
     * Gets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @return The useDisk value
     */
    boolean isUseDisk();

    /**
     * Gets the useLateral attribute of the ICompositeCacheAttributes object
     *
     * @return The useLateral value
     */
    boolean isUseLateral();

    /**
     * Tests whether the memory cache should perform background memory shrinkage.
     *
     * @return The UseMemoryShrinker value
     */
    boolean isUseMemoryShrinker();

    /**
     * returns whether the cache is remote enabled
     *
     * @return The useRemote value
     */
    boolean isUseRemote();

    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s
     *            The new cacheName value
     */
    void setCacheName( String s );

    /**
     * By default this is SWAP_ONLY.
     *
     * @param diskUsagePattern The diskUsagePattern to set.
     */
    void setDiskUsagePattern( DiskUsagePattern diskUsagePattern );

    /**
     * Translates the name to the disk usage pattern short value.
     * <p>
     * The allowed values are SWAP and UPDATE.
     *
     * @param diskUsagePatternName The diskUsagePattern to set.
     */
    void setDiskUsagePatternName( String diskUsagePatternName );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @param seconds
     *            The new MaxMemoryIdleTimeSeconds value
     */
    void setMaxMemoryIdleTimeSeconds( long seconds );

    /**
     * SetMaxObjects is used to set the attribute to determine the maximum
     * number of objects allowed in the memory cache. If the max number of
     * objects or the cache size is set, the default for the one not set is
     * ignored. If both are set, both are used to determine the capacity of the
     * cache, i.e., object will be removed from the cache if either limit is
     * reached. TODO: move to MemoryCache config file.
     *
     * @param size
     *            The new maxObjects value
     */
    void setMaxObjects( int size );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the maximum number of items to spool per run.
     *
     * @param maxSpoolPerRun
     *            The new maxSpoolPerRun value
     */
    void setMaxSpoolPerRun( int maxSpoolPerRun );

    /**
     * Sets the name of the MemoryCache, referenced by the appropriate manager.
     * TODO: create a separate memory cache attribute class.
     *
     * @param s
     *            The new memoryCacheName value
     */
    void setMemoryCacheName( String s );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the shrinker interval.
     *
     * @param seconds
     *            The new ShrinkerIntervalSeconds value
     */
    void setShrinkerIntervalSeconds( long seconds );

    /**
     * Number to send to disk at a time.
     *
     * @param spoolChunkSize
     */
    void setSpoolChunkSize( int spoolChunkSize );

    /**
     * Sets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @param useDisk
     *            The new useDisk value
     */
    void setUseDisk( boolean useDisk );

    /**
     * Sets whether the cache should use a lateral cache
     *
     * @param d
     *            The new useLateral value
     */
    void setUseLateral( boolean d );

    /**
     * Sets whether the memory cache should perform background memory shrinkage.
     *
     * @param useShrinker
     *            The new UseMemoryShrinker value
     */
    void setUseMemoryShrinker( boolean useShrinker );

    /**
     * Sets whether the cache is remote enabled
     *
     * @param isRemote
     *            The new useRemote value
     */
    void setUseRemote( boolean isRemote );
}
