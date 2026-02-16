package org.apache.commons.jcs4.auxiliary.lateral.socket.tcp;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs4.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs4.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.control.CompositeCacheManager;
import org.apache.commons.jcs4.engine.control.MockKeyMatcher;
import org.apache.commons.jcs4.engine.match.behavior.IKeyMatcher;
import org.apache.commons.jcs4.utils.discovery.DiscoveredService;
import org.apache.commons.jcs4.utils.serialization.StandardSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test for the listener that observers UDP discovery events. */
class LateralTCPDiscoveryListenerUnitTest
{
    /** The listener */
    private LateralTCPDiscoveryListener listener;

    /** The cache factory */
    private LateralTCPCacheFactory factory;

    /** The cache manager. */
    private CompositeCacheManager cacheMgr;

    /** The event logger. */
    protected MockCacheEventLogger cacheEventLogger;

    /** The serializer. */
    protected IElementSerializer elementSerializer;

    /** The key matcher. */
    protected IKeyMatcher<String> keyMatcher;

    /** Create the listener for testing */
    @BeforeEach
    void setUp()
        throws Exception
    {
        factory = new LateralTCPCacheFactory();
        factory.initialize();

        cacheMgr = CompositeCacheManager.getInstance();
        cacheEventLogger = new MockCacheEventLogger();
        elementSerializer = new StandardSerializer();
        keyMatcher = new MockKeyMatcher<>();

        listener = new LateralTCPDiscoveryListener( factory.getName(), cacheMgr,
                cacheEventLogger, elementSerializer, keyMatcher);
    }

    private LateralTCPCacheNoWaitFacade<String, String> setupFacade(final String cacheName)
    {
        final List<LateralTCPCacheNoWait<String, String>> noWaits = new ArrayList<>();
        final LateralTCPCacheAttributes cattr = new LateralTCPCacheAttributes();
        cattr.setCacheName( cacheName );

        return new LateralTCPCacheNoWaitFacade<>( null, noWaits, cattr );
    }

    private LateralTCPCacheNoWait<String, String> setupNoWait(final String cacheName)
    {
        final LateralTCPCacheAttributes cattr = new LateralTCPCacheAttributes();
        cattr.setCacheName( cacheName );

        final LateralTCPCache<String, String> cache = new LateralTCPCache<>(cattr, new ZombieCacheServiceNonLocal<>(), null);
        return new LateralTCPCacheNoWait<>( cache );
    }

    /**
     * Add a no wait to a known facade.
     */
    @Test
    void testAddDiscoveredService_FacadeInList_NoWaitNot()
    {
        // SETUP
        final String cacheName = "testAddDiscoveredService_FacadeInList_NoWaitNot";
        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add( cacheName );

        final DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        final LateralTCPCacheAttributes lca = new LateralTCPCacheAttributes();
        // used as identifying key by factory
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        lca.setCacheName(cacheName);
        final LateralTCPCacheNoWait<String, String> noWait = factory.createCacheNoWait(lca,
                cacheEventLogger, elementSerializer, keyMatcher);
        // this is the normal process, the discovery service expects it there
        cacheMgr.addAuxiliaryCache(factory.getName(), cacheName, noWait);
        cacheMgr.registerAuxiliaryFactory(factory);

        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);
        listener.addNoWaitFacade( cacheName, facade );

        // DO WORK
        listener.addDiscoveredService( service );

        // VERIFY
        assertTrue( listener.containsNoWait( cacheName, noWait ), "Should have no wait." );
    }

    /**
     * Add a no wait to a known facade.
     */
    @Test
    void testAddNoWait_FacadeInList()
    {
        // SETUP
        final String cacheName = "testAddNoWaitFacade_FacadeInList";
        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);
        listener.addNoWaitFacade( cacheName, facade );

        final LateralTCPCacheNoWait<String, String> noWait = setupNoWait(cacheName);

        // DO WORK
        final boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertTrue( result, "Should have added the no wait." );
    }

    /**
     * Add a no wait from an unknown facade.
     */
    @Test
    void testAddNoWait_FacadeNotInList()
    {
        // SETUP
        final String cacheName = "testAddNoWaitFacade_FacadeInList";
        final LateralTCPCacheNoWait<String, String> noWait = setupNoWait(cacheName);

        // DO WORK
        final boolean result = listener.addNoWait( noWait );

        // VERIFY
        assertFalse( result, "Should not have added the no wait." );
    }

    /**
     * Add a no wait facade.
     */
    @Test
    void testAddNoWaitFacade_NotInList()
    {
        // SETUP
        final String cacheName = "testAddNoWaitFacade_NotInList";
        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);

        // DO WORK
        listener.addNoWaitFacade( cacheName, facade );

        // VERIFY
        assertTrue( listener.containsNoWaitFacade( cacheName ), "Should have the facade." );
    }

    /**
     * Test cache creation with empty noWaits.
     */
    @Test
    void testEmptyNoWaits()
    {
        // SETUP
        final String cacheName = "testEmptyNoWaits";

        final LateralTCPCacheAttributes lca = new LateralTCPCacheAttributes();
        lca.setTcpServers(""); // default
        lca.setTcpListenerPort(1120);
        lca.setCacheName(cacheName);
        lca.setUdpDiscoveryEnabled(false);
        final LateralTCPCacheNoWaitFacade<String, String> noWait = factory.createCache(lca,
                cacheMgr, cacheEventLogger, elementSerializer, keyMatcher);

        // VERIFY
        assertFalse( noWait.containsNoWait( "" ), "No waits should be empty." );
    }

    /**
     * Remove a no wait from a known facade.
     */
    @Test
    void testRemoveDiscoveredService_FacadeInList_NoWaitIs()
    {
        // SETUP
        final String cacheName = "testRemoveDiscoveredService_FacadeInList_NoWaitIs";

        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add( cacheName );

        final DiscoveredService service = new DiscoveredService();
        service.setCacheNames( cacheNames );
        service.setServiceAddress( "localhost" );
        service.setServicePort( 9999 );

        final LateralTCPCacheAttributes lca = new LateralTCPCacheAttributes();
        // used as identifying key by factory
        lca.setTcpServer( service.getServiceAddress() + ":" + service.getServicePort() );
        lca.setCacheName(cacheName);
        final LateralTCPCacheNoWait<String, String> noWait = factory.createCacheNoWait(lca,
                cacheEventLogger, elementSerializer, keyMatcher);
        // this is the normal process, the discovery service expects it there
        cacheMgr.addAuxiliaryCache(factory.getName(), cacheName, noWait);
        cacheMgr.registerAuxiliaryFactory(factory);

        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);
        listener.addNoWaitFacade( cacheName, facade );
        listener.addDiscoveredService( service );

        // DO WORK
        listener.removeDiscoveredService( service );

        // VERIFY
        assertFalse( listener.containsNoWait( cacheName, noWait ), "Should not have no wait." );
    }

    /**
     * Remove a no wait from a known facade.
     */
    @Test
    void testRemoveNoWait_FacadeInList_NoWaitIs()
    {
        // SETUP
        final String cacheName = "testRemoveNoWaitFacade_FacadeInListNoWaitIs";
        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);
        listener.addNoWaitFacade( cacheName, facade );

        final LateralTCPCacheNoWait<String, String> noWait = setupNoWait(cacheName);
        listener.addNoWait( noWait );

        // DO WORK
        final boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertTrue( result, "Should have removed the no wait." );
    }

    /**
     * Remove a no wait from a known facade.
     */
    @Test
    void testRemoveNoWait_FacadeInList_NoWaitNot()
    {
        // SETUP
        final String cacheName = "testAddNoWaitFacade_FacadeInList";
        final LateralTCPCacheNoWaitFacade<String, String> facade = setupFacade(cacheName);
        listener.addNoWaitFacade( cacheName, facade );

        final LateralTCPCacheNoWait<String, String> noWait = setupNoWait(cacheName);

        // DO WORK
        final boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( result, "Should not have removed the no wait." );
    }

    /**
     * Remove a no wait from an unknown facade.
     */
    @Test
    void testRemoveNoWait_FacadeNotInList()
    {
        // SETUP
        final String cacheName = "testRemoveNoWaitFacade_FacadeNotInList";
        final LateralTCPCacheNoWait<String, String> noWait = setupNoWait(cacheName);

        // DO WORK
        final boolean result = listener.removeNoWait( noWait );

        // VERIFY
        assertFalse( result, "Should not have removed the no wait." );
    }
}
