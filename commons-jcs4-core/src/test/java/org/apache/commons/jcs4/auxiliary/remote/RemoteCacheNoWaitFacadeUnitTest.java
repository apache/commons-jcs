package org.apache.commons.jcs4.auxiliary.remote;

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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs4.engine.CacheStatus;
import org.junit.jupiter.api.Test;

/**
 * Tests for RemoteCacheNoWaitFacade.
 */
class RemoteCacheNoWaitFacadeUnitTest
{
    /**
     * Verify that we can add an item.
     */
    @Test
    void testAddNoWait_InList()
    {
        // SETUP
        final List<RemoteCacheNoWait<String, String>> noWaits = new ArrayList<>();
        final IRemoteCacheAttributes cattr = new RemoteCacheAttributes();
        cattr.setCacheName( "testCache1" );

        final RemoteCache<String, String> client = new RemoteCache<>(cattr, null, null, null);
        final RemoteCacheNoWait<String, String> noWait = new RemoteCacheNoWait<>( client );
        noWaits.add( noWait );

        final RemoteCacheNoWaitFacade<String, String> facade = new RemoteCacheNoWaitFacade<>(noWaits, cattr, null, null, null );

        // VERIFY
        assertEquals( 1, facade.noWaits.size(), "Should have one entry." );
        assertTrue( facade.noWaits.contains( noWait ), "Should be in the list." );
        assertSame( facade, ( (RemoteCache<String, String>) facade.noWaits.get( 0 ).getRemoteCache() ).getFacade(),
                    "Should have same facade." );
    }

    /**
     * Verify that failover works
     */
    @Test
    void testFailover()
    {
        // SETUP
        final RemoteCacheAttributes cattr = new RemoteCacheAttributes();
        cattr.setCacheName("testCache1");
        cattr.setFailoverServers("localhost:1101,localhost:1102");
        cattr.setReceive(false);

        final TestRemoteCacheFactory factory = new TestRemoteCacheFactory();
        factory.initialize();

        final AuxiliaryCache<String, String> cache = factory.createCache(cattr, null, null, null, null);
        final RemoteCacheNoWaitFacade<String, String> facade =
                (RemoteCacheNoWaitFacade<String, String>) cache;
        assertEquals( 2, cattr.getFailovers().size(), "Should have two failovers." );
        assertEquals( 2, factory.managers.size(), "Should have two managers." );
        assertEquals( 0, cattr.getFailoverIndex(), "Should have primary server." );
        final RemoteCacheNoWait<String, String> primary = facade.getPrimaryServer();
        assertEquals( CacheStatus.ALIVE, primary.getStatus(), "Should be ALIVE" );

        // Make primary unusable
        facade.getPrimaryServer().getCacheEventQueue().destroy();
        assertEquals( CacheStatus.ERROR, primary.getStatus(), "Should be ERROR" );
        facade.attemptRestorePrimary = false;
        facade.connectAndRestore();

        // VERIFY
        assertEquals( 2, cattr.getFailovers().size(), "Should have two failovers." );
        assertEquals( 2, factory.managers.size(), "Should have two managers." );
        assertEquals( 1, cattr.getFailoverIndex(), "Should have switched to secondary server." );
        assertNotSame( primary, facade.getPrimaryServer(), "Should have diferent primary now" );
        assertEquals( CacheStatus.ALIVE, facade.getPrimaryServer().getStatus(), "Should be ALIVE" );
    }
}
