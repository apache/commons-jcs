package org.apache.jcs.utils.discovery;

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

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.MockCacheEventLogger;

/** Unit tests for the service. */
public class UDPDiscoveryServiceUnitTest
    extends TestCase
{
    /** Verify that the list is updated. */
    public void testAddOrUpdateService_NotInList()
    {
        // SETUP
        String host = "228.5.6.7";
        int port = 6789;
        UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
        attributes.setUdpDiscoveryAddr( host );
        attributes.setUdpDiscoveryPort( port );
        attributes.setServicePort( 1000 );

        // create the service
        UDPDiscoveryService service = new UDPDiscoveryService( attributes, new MockCacheEventLogger() );
        service.addParticipatingCacheName( "testCache1" );

        MockDiscoveryListener discoveryListener = new MockDiscoveryListener();
        service.setDiscoveryListener( discoveryListener );

        DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        // DO WORK
        service.addOrUpdateService( discoveredService );

        // VERIFY
        assertTrue( "Service should be in the service list.", service.getDiscoveredServices()
            .contains( discoveredService ) );
        assertTrue( "Service should be in the listener list.", discoveryListener.discoveredServices
            .contains( discoveredService ) );
    }

    /** Verify that the list is updated. */
    public void testAddOrUpdateService_InList()
    {
        // SETUP
        String host = "228.5.6.7";
        int port = 6789;
        UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
        attributes.setUdpDiscoveryAddr( host );
        attributes.setUdpDiscoveryPort( port );
        attributes.setServicePort( 1000 );

        // create the service
        UDPDiscoveryService service = new UDPDiscoveryService( attributes, new MockCacheEventLogger() );
        service.addParticipatingCacheName( "testCache1" );

        MockDiscoveryListener discoveryListener = new MockDiscoveryListener();
        service.setDiscoveryListener( discoveryListener );

        DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        DiscoveredService discoveredService2 = new DiscoveredService();
        discoveredService2.setServiceAddress( host );
        discoveredService2.setCacheNames( new ArrayList() );
        discoveredService2.setServicePort( 1000 );
        discoveredService2.setLastHearFromTime( 500 );

        // DO WORK
        service.addOrUpdateService( discoveredService );
        // again
        service.addOrUpdateService( discoveredService2 );

        // VERIFY
        assertEquals( "Should only be one in the set.", 1, service.getDiscoveredServices().size() );
        assertTrue( "Service should be in the service list.", service.getDiscoveredServices()
            .contains( discoveredService ) );
        assertTrue( "Service should be in the listener list.", discoveryListener.discoveredServices
            .contains( discoveredService ) );
        
        Iterator it = service.getDiscoveredServices().iterator();
        // need to update the time this sucks. add has no effect convert to a map
        while ( it.hasNext() )
        {
            DiscoveredService service1 = (DiscoveredService) it.next();
            if ( discoveredService.equals( service1 ) )
            {
                assertEquals( "The match should have the new last heard from time.", service1.getLastHearFromTime(),
                              discoveredService2.getLastHearFromTime() );
            }
        }        
    }
    
    /** Verify that the list is updated. */
    public void testRemoveDiscoveredService()
    {
        // SETUP
        String host = "228.5.6.7";
        int port = 6789;
        UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes();
        attributes.setUdpDiscoveryAddr( host );
        attributes.setUdpDiscoveryPort( port );
        attributes.setServicePort( 1000 );

        // create the service
        UDPDiscoveryService service = new UDPDiscoveryService( attributes, new MockCacheEventLogger() );
        service.addParticipatingCacheName( "testCache1" );

        MockDiscoveryListener discoveryListener = new MockDiscoveryListener();
        service.setDiscoveryListener( discoveryListener );

        DiscoveredService discoveredService = new DiscoveredService();
        discoveredService.setServiceAddress( host );
        discoveredService.setCacheNames( new ArrayList() );
        discoveredService.setServicePort( 1000 );
        discoveredService.setLastHearFromTime( 100 );

        service.addOrUpdateService( discoveredService );

        // DO WORK
        service.removeDiscoveredService( discoveredService );

        // VERIFY
        assertFalse( "Service should not be in the service list.", service.getDiscoveredServices()
            .contains( discoveredService ) );
        assertFalse( "Service should not be in the listener list.", discoveryListener.discoveredServices
            .contains( discoveredService ) );
    }    
}
