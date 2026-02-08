package org.apache.commons.jcs4.engine.control;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.jcs4.engine.CacheStatus;
import org.apache.commons.jcs4.engine.CompositeCacheAttributes;
import org.junit.jupiter.api.Test;

/** Tests for the composite cache manager */
class CompositeCacheManagerTest
{

    /**
     * Verify that calling release, when there are active clients, the caches are correctly disposed or not.
     */
    @Test
    void testRelease()
    {
        // See JCS-184
        // create the manager
        final CompositeCacheManager manager = CompositeCacheManager.getInstance();
        // add a simple cache
        final CompositeCacheAttributes cacheAttributes = CompositeCacheAttributes.defaults();
        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttributes, /* attr */ null);
        manager.addCache("simple_cache", cache);
        // add a client to the cache
        CompositeCacheManager.getUnconfiguredInstance();
        // won't release as there are still clients. Only disposed when release() is called by
        // the last client
        manager.release();
        assertEquals( CacheStatus.ALIVE, cache.getStatus(), "The cache was disposed during release!" );
        manager.release();
        assertEquals( CacheStatus.DISPOSED, cache.getStatus(), "The cache was NOT disposed during release!" );
    }

}
