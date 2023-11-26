package org.apache.commons.jcs3.auxiliary.lateral;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.TCPLateralCacheAttributes;
import org.apache.commons.jcs3.engine.ZombieCacheServiceNonLocal;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LateralCacheNoWaitFacade.
 */
public class LateralCacheNoWaitFacadeUnitTest
{
    private LateralCacheNoWaitFacade<String, String> facade;
    private LateralCache<String, String> cache;

    @Before
    public void setUp() throws Exception
    {
        // SETUP
        List<LateralCacheNoWait<String, String>> noWaits = new ArrayList<>();
        TCPLateralCacheAttributes cattr = new TCPLateralCacheAttributes();
        cattr.setCacheName( "testCache1" );
        cattr.setTcpServer("localhost:7890");

        facade = new LateralCacheNoWaitFacade<>( null, noWaits, cattr );
        cache = new LateralCache<>(cattr, new ZombieCacheServiceNonLocal<>(), null);
    }

    /**
     * Verify that we can remove an item.
     */
    @Test
    public void testAdd_InList()
    {
        final LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<>( cache );

        // DO WORK
        facade.addNoWait( noWait );
        facade.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );
        assertEquals( "Should only have 1", 1, facade.getNoWaitSize() );
    }

    /**
     * Verify that we can remove an item.
     */
    @Test
    public void testAddThenRemoveNoWait_InList()
    {
        LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<>( cache );

        // DO WORK
        facade.addNoWait( noWait );

        // VERIFY
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );

        // DO WORK
        facade.removeNoWait( noWait );

        // VERIFY
        assertEquals( "Should have 0", 0, facade.getNoWaitSize() );
        assertFalse( "Should not be in the list. ", facade.containsNoWait( noWait ) );
    }

    /**
     * Verify that we can remove an item.
     */
    @Test
    public void testAddThenRemoveNoWait_InListSize2()
    {
        final LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<>( cache );
        noWait.setIdentityKey("1234");
        final LateralCacheNoWait<String, String> noWait2 = new LateralCacheNoWait<>( cache );
        noWait2.setIdentityKey("2345");

        // DO WORK
        facade.addNoWait( noWait );
        facade.addNoWait( noWait2 );

        // VERIFY
        assertEquals( "Should have 2", 2, facade.getNoWaitSize() );
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait ) );
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait2 ) );

        // DO WORK
        facade.removeNoWait( noWait );

        // VERIFY
        assertEquals( "Should only have 1", 1, facade.getNoWaitSize() );
        assertFalse( "Should not be in the list. ", facade.containsNoWait( noWait ) );
        assertTrue( "Should be in the list.", facade.containsNoWait( noWait2 ) );
    }

    /**
     * Verify that we can remove an item.
     */
    @Test
    public void testAddThenRemoveNoWait_NotInList()
    {
        final LateralCacheNoWait<String, String> noWait = new LateralCacheNoWait<>( cache );

        // DO WORK
        facade.removeNoWait( noWait );

        // VERIFY
        assertFalse( "Should not be in the list.", facade.containsNoWait( noWait ) );
    }
}
