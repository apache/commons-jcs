package org.apache.commons.jcs.engine;

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
import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs.engine.behavior.ICacheEventQueue.QueueType;
import org.apache.commons.jcs.engine.behavior.ICacheListener;

/** Unit tests for the CacheEventQueueFactory */
public class CacheEventQueueFactoryUnitTest
    extends TestCase
{
    /** Test create */
    public void testCreateCacheEventQueue_Single()
    {
        // SETUP
        QueueType eventQueueType = QueueType.SINGLE;
        ICacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        long listenerId = 1;

        CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<String, String>();

        // DO WORK
        ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof CacheEventQueue );
    }

    /** Test create */
    public void testCreateCacheEventQueue_Pooled()
    {
        // SETUP
        QueueType eventQueueType = QueueType.POOLED;
        ICacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        long listenerId = 1;

        CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<String, String>();

        // DO WORK
        ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof PooledCacheEventQueue );
    }
}
