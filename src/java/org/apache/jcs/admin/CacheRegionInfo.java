package org.apache.jcs.admin;

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

import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.control.CompositeCache;

/**
 * Stores info on a cache region for the template
 */
public class CacheRegionInfo
{
    /** The cache region we are getting info on. */
    CompositeCache cache = null;

    /** number of bytes counted so far, will be a total of all items. */
    long byteCount = 0;

    /**
     * @return the underlying region
     */
    public CompositeCache getCache()
    {
        return this.cache;
    }

    /**
     * @return total byte count
     */
    public long getByteCount()
    {
        return this.byteCount;
    }

    /**
     * @return a status string
     */
    public String getStatus()
    {
        int status = this.cache.getStatus();

        return ( status == CacheConstants.STATUS_ALIVE
                                                      ? "ALIVE"
                                                      : status == CacheConstants.STATUS_DISPOSED
                                                                                                ? "DISPOSED"
                                                                                                : status == CacheConstants.STATUS_ERROR
                                                                                                                                       ? "ERROR"
                                                                                                                                       : "UNKNOWN" );
    }

    /**
     * Return the stats for the region.
     * <p>
     * @return String
     */
    public String getStats()
    {
        return this.cache.getStats();
    }

    /**
     * @return string info on the region
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nCacheRegionInfo " );
        if ( getCache() != null )
        {
            buf.append( "\n CacheName [" + getCache().getCacheName() + "]" );
            buf.append( "\n Status [" + getStatus() + "]" );
        }
        buf.append( "\n ByteCount [" + getByteCount() + "]" );

        return buf.toString();
    }
}
