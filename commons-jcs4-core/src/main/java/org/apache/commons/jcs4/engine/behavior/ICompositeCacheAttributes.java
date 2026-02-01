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
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s
     *            The new cacheName value
     */
    void setCacheName( String s );
}
