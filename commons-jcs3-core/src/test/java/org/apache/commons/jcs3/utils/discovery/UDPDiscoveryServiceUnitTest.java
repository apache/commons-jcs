package org.apache.commons.jcs3.utils.discovery;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the service. */
class UDPDiscoveryServiceUnitTest
{
    private final static String host = "228.5.6.7";
    private final static int port = 6789;

    private UDPDiscoveryService service;
    private MockDiscoveryListener discoveryListener;

    @BeforeEach
    void setUp()
        throws Exception
    {
        // SETUP
        final UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
        attributes.setUdpDiscoveryAddr( host );
        attributes.setUdpDiscoveryPort( port );
        attributes.setServicePort( 1000 );

        // create the service
        service = new UDPDiscoveryService(attributes, new StandardSerializer());
        service.startup();
        service.addParticipatingCacheName( "testCache1" );

        discoveryListener = new MockDiscoveryListener();
        service.addDiscoveryListener( discoveryListener );
    }

    /** Verify that the list is updated. */
    @Test
    void testAddOrUpdateService_InList_NamesChange()
    {
        final DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList<>() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        final ArrayList<String> differentCacheNames = new ArrayList<>();
        differentCacheNames.add( "name1" );
        final DiscoveredService discoveredService2 = new DiscoveredService();
        discoveredService2.setServiceAddress( host );
        discoveredService2.setCacheNames( differentCacheNames );
        discoveredService2.setServicePort( 1000 );
        discoveredService2.setLastHearFromTime( 500 );

        // DO WORK
        service.addOrUpdateService( discoveredService );
        // again
        service.addOrUpdateService( discoveredService2 );

        // VERIFY
        assertEquals( 1, service.getDiscoveredServices().size(), "Should only be one in the set." );
        assertTrue( service.getDiscoveredServices()
                        .contains( discoveredService ), "Service should be in the service list." );
        assertTrue( discoveryListener.discoveredServices
                        .contains( discoveredService ), "Service should be in the listener list." );

        // need to update the time this sucks. add has no effect convert to a map
        for (final DiscoveredService service1 : service.getDiscoveredServices())
        {
            if ( discoveredService.equals( service1 ) )
            {
                assertEquals( service1.getLastHearFromTime(),
                              discoveredService2.getLastHearFromTime(),
                              "The match should have the new last heard from time." );
                assertEquals( service1.getCacheNames() + "", differentCacheNames + "", "The names should be updated." );
            }
        }
        // the mock has a list from all add calls.
        // it should have been called when the list changed.
        assertEquals( 2, discoveryListener.discoveredServices.size(), "Mock should have been called twice." );
        assertEquals( discoveredService2,
                      discoveryListener.discoveredServices.get( 1 ),
                      "The second mock listener add should be discoveredService2" );
    }

    /** Verify that the list is updated. */
    @Test
    void testAddOrUpdateService_InList_NamesDoNotChange()
    {
        final ArrayList<String> sameCacheNames = new ArrayList<>();
        sameCacheNames.add( "name1" );

        final DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( sameCacheNames );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        final DiscoveredService discoveredService2 = new DiscoveredService();
        discoveredService2.setServiceAddress( host );
        discoveredService2.setCacheNames( sameCacheNames );
        discoveredService2.setServicePort( 1000 );
        discoveredService2.setLastHearFromTime( 500 );

        // DO WORK
        service.addOrUpdateService( discoveredService );
        // again
        service.addOrUpdateService( discoveredService2 );

        // VERIFY
        assertEquals( 1, service.getDiscoveredServices().size(), "Should only be one in the set." );
        assertTrue( service.getDiscoveredServices()
                        .contains( discoveredService ), "Service should be in the service list." );
        assertTrue( discoveryListener.discoveredServices
                        .contains( discoveredService ), "Service should be in the listener list." );

        // need to update the time this sucks. add has no effect convert to a map
        for (final DiscoveredService service1 : service.getDiscoveredServices())
        {
            if ( discoveredService.equals( service1 ) )
            {
                assertEquals( service1.getLastHearFromTime(),
                              discoveredService2.getLastHearFromTime(),
                              "The match should have the new last heard from time." );
            }
        }
        // the mock has a list from all add calls.
        // it should have been called when the list changed.
        //assertEquals( "Mock should have been called once.", 1, discoveryListener.discoveredServices.size() );
        // logic changed.  it's called every time.
        assertEquals( 2, discoveryListener.discoveredServices.size(), "Mock should have been called twice." );
    }

    /** Verify that the list is updated. */
    @Test
    void testAddOrUpdateService_NotInList()
    {
        final DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList<>() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        // DO WORK
        service.addOrUpdateService( discoveredService );

        // VERIFY
        assertTrue( service.getDiscoveredServices()
                        .contains( discoveredService ), "Service should be in the service list." );
        assertTrue( discoveryListener.discoveredServices
                        .contains( discoveredService ), "Service should be in the listener list." );
    }

    /** Verify that the list is updated. */
    @Test
    void testRemoveDiscoveredService()
    {
        final DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList<>() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        service.addOrUpdateService( discoveredService );

        // DO WORK
        service.removeDiscoveredService( discoveredService );

        // VERIFY
        assertFalse( service.getDiscoveredServices()
                         .contains( discoveredService ), "Service should not be in the service list." );
        assertFalse( discoveryListener.discoveredServices
                         .contains( discoveredService ), "Service should not be in the listener list." );
    }
}
