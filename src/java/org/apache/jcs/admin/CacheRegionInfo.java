package org.apache.jcs.admin;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.control.CompositeCache;

/** Stores info on a cache region for the template */
public class CacheRegionInfo
{
    CompositeCache cache = null;

    long byteCount = 0;

    /**
     * @return
     */
    public CompositeCache getCache()
    {
        return this.cache;
    }

    /**
     * @return
     */
    public long getByteCount()
    {
        return this.byteCount;
    }

    /**
     * @return
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
     * 
     * @return String
     */
    public String getStats()
    {
        return this.cache.getStats();
    }

}
