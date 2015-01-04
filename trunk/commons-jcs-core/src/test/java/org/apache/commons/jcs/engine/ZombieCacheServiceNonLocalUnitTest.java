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
import org.apache.commons.jcs.engine.behavior.ICacheElement;

/**
 * Tests for the zombie remote cache service.
 */
public class ZombieCacheServiceNonLocalUnitTest
    extends TestCase
{
    /**
     * Verify that an update event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testUpdateThenWalk()
        throws Exception
    {
        // SETUP
        MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<String, String>();

        ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<String, String>( 10 );

        String cacheName = "testUpdate";

        // DO WORK
        ICacheElement<String, String> element = new CacheElement<String, String>( cacheName, "key", "value" );
        zombie.update( element, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", element, service.lastUpdate );
    }

    /**
     * Verify that nothing is added if the max is set to 0.
     * <p>
     * @throws Exception
     */
    public void testUpdateThenWalk_zeroSize()
        throws Exception
    {
        // SETUP
        MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<String, String>();

        ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<String, String>( 0 );

        String cacheName = "testUpdate";

        // DO WORK
        ICacheElement<String, String> element = new CacheElement<String, String>( cacheName, "key", "value" );
        zombie.update( element, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertNull( "Nothing should have been put to the service.", service.lastUpdate );
    }

    /**
     * Verify that a remove event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testRemoveThenWalk()
        throws Exception
    {
        // SETUP
        MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<String, String>();

        ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<String, String>( 10 );

        String cacheName = "testRemoveThenWalk";
        String key = "myKey";

        // DO WORK
        zombie.remove( cacheName, key, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", key, service.lastRemoveKey );
    }

    /**
     * Verify that a removeAll event gets added and then is sent to the service passed to propagate.
     * <p>
     * @throws Exception
     */
    public void testRemoveAllThenWalk()
        throws Exception
    {
        // SETUP
        MockCacheServiceNonLocal<String, String> service = new MockCacheServiceNonLocal<String, String>();

        ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<String, String>( 10 );

        String cacheName = "testRemoveThenWalk";

        // DO WORK
        zombie.removeAll( cacheName, 123l );
        zombie.propagateEvents( service );

        // VERIFY
        assertEquals( "Updated element is not as expected.", cacheName, service.lastRemoveAllCacheName );
    }
}
