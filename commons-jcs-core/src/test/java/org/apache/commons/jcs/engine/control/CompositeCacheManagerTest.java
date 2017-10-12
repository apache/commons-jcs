package org.apache.commons.jcs.engine.control;

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

import junit.framework.TestCase;

import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;

/** Unit tests for the composite cache manager */
public class CompositeCacheManagerTest
    extends TestCase
{

    /**
     * Verify that calling release, when there are active clients, the caches are correctly disposed or not.
     */
    public void testRelease()
    {
        // See JCS-184
        // create the manager
        CompositeCacheManager manager = CompositeCacheManager.getInstance();
        // add a simple cache
        CompositeCacheAttributes cacheAttributes = new CompositeCacheAttributes();
        CompositeCache<String, String> cache = new CompositeCache<>(cacheAttributes, /* attr */ null);
        manager.addCache("simple_cache", cache);
        // add a client to the cache
        CompositeCacheManager.getUnconfiguredInstance();
        // won't release as there are still clients. Only disposed when release() is called by
        // the last client
        manager.release();
        assertEquals("The cache was disposed during release!", CacheStatus.ALIVE, cache.getStatus());
        manager.release();
        assertEquals("The cache was NOT disposed during release!", CacheStatus.DISPOSED, cache.getStatus());
    }

}
