package org.apache.commons.jcs4.auxiliary.disk.behavior;

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

import java.io.File;
import java.time.Duration;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheAttributes;

/**
 * Common disk cache attributes.
 */
public interface IDiskCacheAttributes
    extends AuxiliaryCacheAttributes
{
    enum DiskLimitType {
        /** Limit elements by count (default) */
        COUNT,
        /** Limit elements by their size */
        SIZE
    }
    /**
     * This is the default purgatory size limit. Purgatory is the area where
     * items to be spooled are temporarily stored. It basically provides access
     * to items on the to-be-spooled queue.
     */
    int MAX_PURGATORY_SIZE_DEFAULT = 5000;

    /**
     *
     * @return active DiskLimitType
     */
    DiskLimitType getDiskLimitType();

    /**
     * Gets the diskPath attribute of the attributes object
     *
     * @return The diskPath value
     */
    File getDiskPath();

    /**
     * Gets the maxKeySize attribute of the DiskCacheAttributes object
     *
     * @return The maxPurgatorySize value
     */
    int getMaxPurgatorySize();

    /**
     * Gets the amount of time we will wait for elements to move to
     * disk during shutdown for a particular region.
     *
     * @return the time.
     */
    Duration getShutdownSpoolTimeLimit();

    /**
     * If this is true then remove all is not prohibited.
     *
     * @return boolean
     */
    boolean isAllowRemoveAll();
}
