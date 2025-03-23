package org.apache.commons.jcs3.auxiliary.remote;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for the remote cache no wait. The no wait manages a queue on top of the client.
 */
class RemoteCacheNoWaitUnitTest
{
    /**
     * Simply verify that the serviced supplied to fix is passed onto the client. Verify that the
     * original event queue is destroyed. A new event queue willbe plugged in on fix.
     *
     * @throws Exception
     */
    @Test
    void testFixCache()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        client.status = CacheStatus.ALIVE;
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        final MockRemoteCacheService<String, String> service = new MockRemoteCacheService<>();

        final ICacheElement<String, String> element = new CacheElement<>( "testUpdate", "key", "value" );

        // DO WORK
        noWait.update( element );
        SleepUtil.sleepAtLeast( 10 );
        // ICacheEventQueue<String, String> originalQueue = noWait.getCacheEventQueue();

        noWait.fixCache( service );

        noWait.update( element );
        SleepUtil.sleepAtLeast( 10 );

        // VERIFY
        assertEquals( service, client.fixed, "Wrong status" );
    }

    /**
     * Simply verify that the client get is called from the no wait.
     *
     * @throws Exception
     */
    @Test
    void testGet()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        final ICacheElement<String, String> input = new CacheElement<>( "testUpdate", "key", "value" );
        client.getSetupMap.put( "key", input );

        // DO WORK
        final ICacheElement<String, String> result = noWait.get( "key" );

        // VERIFY
        assertEquals( input, result, "Wrong element" );
    }

    /**
     * Simply verify that the client getMultiple is called from the no wait.
     *
     * @throws Exception
     */
    @Test
    void testGetMultiple()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        final ICacheElement<String, String> inputElement = new CacheElement<>( "testUpdate", "key", "value" );
        final Map<String, ICacheElement<String, String>> inputMap = new HashMap<>();
        inputMap.put( "key", inputElement );

        final Set<String> keys = new HashSet<>();
        keys.add( "key" );

        client.getMultipleSetupMap.put( keys, inputMap );

        // DO WORK
        final Map<String, ICacheElement<String, String>> result = noWait.getMultiple( keys );

        // VERIFY
        assertEquals( inputMap, result, "elements map" );
    }

    /**
     * Simply verify that the client status is returned in the stats.
     *
     * @throws Exception
     */
    @Test
    void testGetStats()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        client.status = CacheStatus.ALIVE;
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        // DO WORK
        final String result = noWait.getStats();

        // VERIFY
        assertTrue( result.indexOf( "ALIVE" ) != -1, "Status should contain 'ALIVE'" );
    }

    /**
     * Simply verify that we get a status of error if the cache is in error..
     *
     * @throws Exception
     */
    @Test
    void testGetStatus_error()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        client.status = CacheStatus.ERROR;
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        // DO WORK
        final CacheStatus result = noWait.getStatus();

        // VERIFY
        assertEquals( CacheStatus.ERROR, result, "Wrong status" );
    }

    /**
     * Simply verify that the client gets updated via the no wait.
     *
     * @throws Exception
     */
    @Test
    void testRemove()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        final String input = "MyKey";

        // DO WORK
        noWait.remove( input );

        SleepUtil.sleepAtLeast( 10 );

        // VERIFY
        assertEquals( 1, client.removeList.size(), "Wrong number updated." );
        assertEquals( input, client.removeList.get( 0 ), "Wrong key" );
    }

    /**
     * Simply verify that the client gets updated via the no wait.
     *
     * @throws Exception
     */
    @Test
    void testUpdate()
        throws Exception
    {
        // SETUP
        final MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<>();
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );

        final ICacheElement<String, String> element = new CacheElement<>( "testUpdate", "key", "value" );

        // DO WORK
        noWait.update( element );

        // VERIFY
        SleepUtil.sleepAtLeast( 10 );

        assertEquals( 1, client.updateList.size(), "Wrong number updated." );
        assertEquals( element, client.updateList.get( 0 ), "Wrong element" );
    }
}
