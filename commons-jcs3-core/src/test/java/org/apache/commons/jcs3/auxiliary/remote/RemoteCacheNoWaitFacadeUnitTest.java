package org.apache.commons.jcs3.auxiliary.remote;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.engine.CacheStatus;

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

/**
 * Tests for RemoteCacheNoWaitFacade.
 */
public class RemoteCacheNoWaitFacadeUnitTest
    extends TestCase
{
    /**
     * Verify that we can add an item.
     */
    public void testAddNoWait_InList()
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
        assertEquals( "Should have one entry.", 1, facade.noWaits.size() );
        assertTrue( "Should be in the list.", facade.noWaits.contains( noWait ) );
        assertSame( "Should have same facade.", facade, ((RemoteCache<String, String>)facade.noWaits.get(0).getRemoteCache()).getFacade() );
    }

    /**
     * Verify that failover works
     */
    public void testFailover()
    {
        // SETUP
        final IRemoteCacheAttributes cattr = new RemoteCacheAttributes();
        cattr.setCacheName("testCache1");
        cattr.setFailoverServers("localhost:1101,localhost:1102");
        cattr.setReceive(false);

        final TestRemoteCacheFactory factory = new TestRemoteCacheFactory();
        factory.initialize();

        final AuxiliaryCache<String, String> cache = factory.createCache(cattr, null, null, null);
        final RemoteCacheNoWaitFacade<String, String> facade =
                (RemoteCacheNoWaitFacade<String, String>) cache;
        assertEquals("Should have two failovers.", 2, cattr.getFailovers().size());
        assertEquals("Should have two managers.", 2, factory.managers.size());
        assertEquals("Should have primary server.", 0, cattr.getFailoverIndex());
        RemoteCacheNoWait<String, String> primary = facade.getPrimaryServer();
        assertEquals("Should be ALIVE", CacheStatus.ALIVE, primary.getStatus());

        // Make primary unusable
        facade.getPrimaryServer().getCacheEventQueue().destroy();
        assertEquals("Should be ERROR", CacheStatus.ERROR, primary.getStatus());
        facade.attemptRestorePrimary = false;
        facade.connectAndRestore();

        // VERIFY
        assertEquals("Should have two failovers.", 2, cattr.getFailovers().size());
        assertEquals("Should have two managers.", 2, factory.managers.size());
        assertEquals("Should have switched to secondary server.", 1, cattr.getFailoverIndex());
        assertNotSame("Should have diferent primary now", primary, facade.getPrimaryServer());
        assertEquals("Should be ALIVE", CacheStatus.ALIVE, facade.getPrimaryServer().getStatus());
    }
}
