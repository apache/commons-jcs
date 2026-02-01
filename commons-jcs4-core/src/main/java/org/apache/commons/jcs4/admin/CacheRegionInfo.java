package org.apache.commons.jcs4.admin;

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

/**
 * Stores info on a cache region for the template
 */
public record CacheRegionInfo(
    /** The name of the cache region */
    String cacheName,

    /** The size of the cache region */
    int cacheSize,

    /** The status of the cache region */
    String cacheStatus,

    /** The statistics of the cache region */
    String cacheStatistics,

    /** The number of memory hits in the cache region */
    long hitCountRam,

    /** The number of auxiliary hits in the cache region */
    long hitCountAux,

    /** The number of misses in the cache region because the items were not found */
    long missCountNotFound,

    /** The number of misses in the cache region because the items were expired */
    long missCountExpired,

    /** The number of bytes counted so far, will be a total of all items */
    long byteCount
)
{
    /**
     * @return string info on the region
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("\nCacheRegionInfo ");
        if (cacheName() != null)
        {
            buf.append("\n CacheName [" + cacheName() + "]");
            buf.append("\n Status [" + cacheStatus() + "]");
        }
        buf.append("\n ByteCount [" + byteCount() + "]");

        return buf.toString();
    }
}
