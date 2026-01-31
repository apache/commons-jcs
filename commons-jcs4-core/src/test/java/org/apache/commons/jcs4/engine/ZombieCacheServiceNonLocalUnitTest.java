package org.apache.commons.jcs4.engine;

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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.junit.jupiter.api.Test;

/**
 * Tests for the zombie remote cache service.
 */
class ZombieCacheServiceNonLocalUnitTest
{
    /**
     * Verify that a removeAll event gets added and then is sent to the service passed to propagate.
     *
     * @throws Exception
     */
    @Test
    void testRemoveAllThenWalk()
        throws Exception
    {
        // SETUP
        final MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<>();

        final ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<>( 10 );

        final String cacheName = "testRemoveThenWalk";

        // DO WORK
        zombie.removeAll( cacheName, 123L );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( cacheName, service.lastRemoveAllCacheName, "Updated element is not as expected." );
    }

    /**
     * Verify that a remove event gets added and then is sent to the service passed to propagate.
     *
     * @throws Exception
     */
    @Test
    void testRemoveThenWalk()
        throws Exception
    {
        // SETUP
        final MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<>();

        final ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<>( 10 );

        final String cacheName = "testRemoveThenWalk";
        final String key = "myKey";

        // DO WORK
        zombie.remove( cacheName, key, 123L );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( key, service.lastRemoveKey, "Updated element is not as expected." );
    }

    /**
     * Verify that an update event gets added and then is sent to the service passed to propagate.
     *
     * @throws Exception
     */
    @Test
    void testUpdateThenWalk()
        throws Exception
    {
        // SETUP
        final MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<>();

        final ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<>( 10 );

        final String cacheName = "testUpdate";

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( cacheName, "key", "value" );
        zombie.update( element, 123L );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( element, service.lastUpdate, "Updated element is not as expected." );
    }

    /**
     * Verify that nothing is added if the max is set to 0.
     *
     * @throws Exception
     */
    @Test
    void testUpdateThenWalk_zeroSize()
        throws Exception
    {
        // SETUP
        final MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<>();

        final ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<>( 0 );

        final String cacheName = "testUpdate";

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( cacheName, "key", "value" );
        zombie.update( element, 123L );
        zombie.propagateEvents( service );

        // VERIFY
        assertNull( service.lastUpdate, "Nothing should have been put to the service." );
    }
}
