package org.apache.commons.jcs3.admin;

import java.io.IOException;
import java.util.List;

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

import javax.management.MXBean;

/**
 * A MXBean to expose the JCS statistics to JMX
 */
@MXBean
public interface JCSJMXBean
{
    /**
     * Builds up data on every region.
     * <p>
     * TODO we need a most lightweight method that does not count bytes. The byte counting can
     *       really swamp a server.
     * @return List of CacheRegionInfo objects
     */
    List<CacheRegionInfo> buildCacheInfo();

    /**
     * Builds up info about each element in a region.
     *
     * @param cacheName
     * @return List of CacheElementInfo objects
     * @throws IOException
     */
    List<CacheElementInfo> buildElementInfo( String cacheName ) throws IOException;

    /**
     * Clears all regions in the cache.
     * <p>
     * If this class is running within a remote cache server, clears all regions via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears all regions in the cache directly via
     * the usual cache API.
     */
    void clearAllRegions() throws IOException;

    /**
     * Clears a particular cache region.
     * <p>
     * If this class is running within a remote cache server, clears the region via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears the region directly via the usual
     * cache API.
     */
    void clearRegion(String cacheName) throws IOException;

    /**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non serializable objects in
     * the region or an error occurs, suppresses exceptions and returns 0.
     * <p>
     *
     * @return long The size of the region in bytes.
     */
    long getByteCount(String cacheName);

    /**
     * Removes a particular item from a particular region.
     * <p>
     * If this class is running within a remote cache server, removes the item via the <code>RemoteCacheServer</code>
     * API, so that removes will be broadcast to client machines. Otherwise clears the region directly via the usual
     * cache API.
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    void removeItem(String cacheName, String key) throws IOException;
}
