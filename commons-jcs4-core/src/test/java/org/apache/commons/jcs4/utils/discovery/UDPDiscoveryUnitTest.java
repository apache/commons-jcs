package org.apache.commons.jcs4.utils.discovery;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;

import org.apache.commons.jcs4.utils.net.HostNameUtil;
import org.apache.commons.jcs4.utils.serialization.StandardSerializer;
import org.apache.commons.jcs4.utils.timing.SleepUtil;
import org.junit.jupiter.api.Test;

/**
 * Tests for discovery
 */
class UDPDiscoveryUnitTest
{
    /**
     *
     * @throws Exception
     */
    private void simpleUDPDiscovery(final String discoveryAddress)
        throws Exception
    {
        final UDPDiscoveryAttributes attributes = new UDPDiscoveryAttributes(
                1000,
                discoveryAddress,
                6789,
                4 /* datagram TTL */
                );

        // create the service
        final UDPDiscoveryService service = new UDPDiscoveryService(attributes, new StandardSerializer());
        service.startup();
        service.addParticipatingCacheName( "testCache1" );

        final MockDiscoveryListener discoveryListener = new MockDiscoveryListener();
        service.addDiscoveryListener( discoveryListener );

        // create a receiver with the service
        final UDPDiscoveryReceiver receiver = new UDPDiscoveryReceiver( service::processMessage,
                null,
                attributes.udpDiscoveryAddr(),
                attributes.udpDiscoveryPort() );
        receiver.setSerializer(service.getSerializer());
        final Thread t = new Thread( receiver );
        t.start();

        // create a sender
        try (final UDPDiscoverySender sender = new UDPDiscoverySender(
                attributes, service.getSerializer()))
        {
            // create more names than we have no wait facades for
            // the only one that gets added should be testCache1
            final ArrayList<String> cacheNames = new ArrayList<>();
            final int numJunk = 10;
            for ( int i = 0; i < numJunk; i++ )
            {
                cacheNames.add( "junkCacheName" + i );
            }
            cacheNames.add( "testCache1" );

            // send max messages
            final int max = 10;
            int cnt = 0;
            for ( ; cnt < max; cnt++ )
            {
                sender.passiveBroadcast( "localhost", 1111, cacheNames, 1 );
                SleepUtil.sleepAtLeast( 20 );
            }

            SleepUtil.sleepAtLeast( 200 );

            // check to see that we got 10 messages
            //System.out.println( "Receiver count = " + receiver.getCnt() );

            // request braodcasts change things.
            assertTrue( cnt <= receiver.getCnt(),
                        "Receiver count [" + receiver.getCnt() + "] should be the at least the number sent [" + cnt + "]." );
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    void testSimpleUDPDiscoveryIPv4()
        throws Exception
    {
        assumeTrue( HostNameUtil.getMulticastNetworkInterface() != null, "This machine does not support multicast" );

        simpleUDPDiscovery("228.5.6.7");
    }

    /**
     *
     * @throws Exception
     */
    @Test
    void testSimpleUDPDiscoveryIPv6()
        throws Exception
    {
        assumeTrue( HostNameUtil.getMulticastNetworkInterface() != null, "This machine does not support multicast" );

        simpleUDPDiscovery("FF02::5678");
    }
}
