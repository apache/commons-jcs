package org.apache.commons.jcs3.engine;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs3.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue.QueueType;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.junit.jupiter.api.Test;

/** Tests for the CacheEventQueueFactory */
class CacheEventQueueFactoryUnitTest
{
    /** Test create */
    @Test
    void testCreateCacheEventQueue_Pooled()
    {
        // SETUP
        final QueueType eventQueueType = QueueType.POOLED;
        final ICacheListener<String, String> listener = new MockRemoteCacheListener<>();
        final long listenerId = 1;

        final CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<>();

        // DO WORK
        final ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( result, "Should have a result" );
        assertTrue( result.getQueueType() == QueueType.POOLED, "Wrong type" );
    }

    /** Test create */
    @Test
    void testCreateCacheEventQueue_Single()
    {
        // SETUP
        final QueueType eventQueueType = QueueType.SINGLE;
        final ICacheListener<String, String> listener = new MockRemoteCacheListener<>();
        final long listenerId = 1;

        final CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<>();

        // DO WORK
        final ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( result, "Should have a result" );
        assertEquals( QueueType.SINGLE, result.getQueueType(), "Wrong type" );
    }
}
