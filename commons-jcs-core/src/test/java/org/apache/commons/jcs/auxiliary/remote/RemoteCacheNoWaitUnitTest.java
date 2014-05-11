package org.apache.commons.jcs.auxiliary.remote;

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
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs.utils.timing.SleepUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for the remote cache no wait. The no wait manages a queue on top of the client.
 * <p>
 * @author Aaron Smuts
 */
public class RemoteCacheNoWaitUnitTest
    extends TestCase
{
    /**
     * Simply verify that the client gets updated via the no wait.
     * <p>
     * @throws Exception
     */
    public void testUpdate()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        ICacheElement<String, String> element = new CacheElement<String, String>( "testUpdate", "key", "value" );

        // DO WORK
        noWait.update( element );

        // VERIFY
        SleepUtil.sleepAtLeast( 10 );

        assertEquals( "Wrong number updated.", 1, client.updateList.size() );
        assertEquals( "Wrong element", element, client.updateList.get( 0 ) );
    }

    /**
     * Simply verify that the client get is called from the no wait.
     * <p>
     * @throws Exception
     */
    public void testGet()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        ICacheElement<String, String> input = new CacheElement<String, String>( "testUpdate", "key", "value" );
        client.getSetupMap.put( "key", input );

        // DO WORK
        ICacheElement<String, String> result = noWait.get( "key" );

        // VERIFY
        assertEquals( "Wrong element", input, result );
    }

    /**
     * Simply verify that the client getMultiple is called from the no wait.
     * <p>
     * @throws Exception
     */
    public void testGetMultiple()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        ICacheElement<String, String> inputElement = new CacheElement<String, String>( "testUpdate", "key", "value" );
        Map<String, ICacheElement<String, String>> inputMap = new HashMap<String, ICacheElement<String,String>>();
        inputMap.put( "key", inputElement );

        Set<String> keys = new HashSet<String>();
        keys.add( "key" );

        client.getMultipleSetupMap.put( keys, inputMap );

        // DO WORK
        Map<String, ICacheElement<String, String>> result = noWait.getMultiple( keys );

        // VERIFY
        assertEquals( "elements map", inputMap, result );
    }

    /**
     * Simply verify that the client gets updated via the no wait.
     * <p>
     * @throws Exception
     */
    public void testRemove()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        String input = "MyKey";

        // DO WORK
        noWait.remove( input );

        SleepUtil.sleepAtLeast( 10 );

        // VERIFY
        assertEquals( "Wrong number updated.", 1, client.removeList.size() );
        assertEquals( "Wrong key", input, client.removeList.get( 0 ) );
    }

    /**
     * Simply verify that the client status is returned in the stats.
     * <p>
     * @throws Exception
     */
    public void testGetStats()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        client.status = CacheStatus.ALIVE;
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        // DO WORK
        String result = noWait.getStats();

        // VERIFY
        assertTrue( "Status should contain 'ALIVE'", result.indexOf( "ALIVE" ) != -1 );
    }

    /**
     * Simply verify that we get a status of error if the cache is in error..
     * <p>
     * @throws Exception
     */
    public void testGetStatus_error()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        client.status = CacheStatus.ERROR;
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        // DO WORK
        CacheStatus result = noWait.getStatus();

        // VERIFY
        assertEquals( "Wrong status", CacheStatus.ERROR, result );
    }

    /**
     * Simply verify that the serviced supplied to fix is passed onto the client. Verify that the
     * original event queue is destroyed. A new event queue willbe plugged in on fix.
     * <p>
     * @throws Exception
     */
    public void testFixCache()
        throws Exception
    {
        // SETUP
        MockRemoteCacheClient<String, String> client = new MockRemoteCacheClient<String, String>();
        client.status = CacheStatus.ALIVE;
        RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<String, String>( client );

        MockRemoteCacheService<String, String> service = new MockRemoteCacheService<String, String>();

        ICacheElement<String, String> element = new CacheElement<String, String>( "testUpdate", "key", "value" );

        // DO WORK
        noWait.update( element );
        SleepUtil.sleepAtLeast( 10 );
        ICacheEventQueue<String, String> originalQueue = noWait.getCacheEventQueue();

        noWait.fixCache( service );

        noWait.update( element );
        SleepUtil.sleepAtLeast( 10 );
        ICacheEventQueue<String, String> newQueue = noWait.getCacheEventQueue();

        // VERIFY
        assertEquals( "Wrong status", service, client.fixed );
        assertFalse( "Original queue should not alive", originalQueue.isAlive() );
        assertTrue( "New queue should be alive." + newQueue, newQueue.isAlive() );
    }
}
