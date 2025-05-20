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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryMessage.BroadcastType;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the sender.
 */
class UDPDiscoverySenderUnitTest
{
    /** Multicast address to send/receive on */
    private static final String ADDRESS = "228.4.5.9";

    /** Multicast address to send/receive on */
    private static final int PORT = 5556;

    /** Imaginary host address for sending */
    private static final String SENDING_HOST = "imaginary host address";

    /** Imaginary port for sending */
    private static final int SENDING_PORT = 1;

    /** Receiver instance for tests */
    private UDPDiscoveryReceiver receiver;

    /** Sender instance for tests */
    private UDPDiscoverySender sender;

    /** Delayed message */
    private CompletableFuture<UDPDiscoveryMessage> futureMsg;

    /**
     * Sets up the receiver. Maybe better to just code sockets here? Set up the sender for sending
     * the message.
     *
     * @throws Exception on error
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        assumeTrue( HostNameUtil.getMulticastNetworkInterface() != null, "This machine does not support multicast" );

        futureMsg = new CompletableFuture<>();
        receiver = new UDPDiscoveryReceiver( msg ->
                                                 futureMsg.complete( msg ), null, ADDRESS, PORT );
        receiver.setSerializer(new StandardSerializer());
        final Thread t = new Thread( receiver );
        t.start();

        sender = new UDPDiscoverySender(null, ADDRESS, PORT, 1, new StandardSerializer());
    }

    /**
     * Kill off the sender and receiver.
     *
     * @throws Exception on error
     */
    @AfterEach
    void tearDown()
        throws Exception
    {
        if (receiver != null)
        {
            receiver.shutdown();
        }
        if (sender != null)
        {
            sender.close();
        }
    }

    /**
     * Test sending a live messages.
     *
     * @throws Exception on error
     */
    @Test
    void testPassiveBroadcast()
        throws Exception
    {
        // SETUP
        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add("testCache");

        // DO WORK
        sender.passiveBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull( msg, "message not received" );
        assertEquals( SENDING_PORT, msg.getPort(), "wrong port" );
        assertEquals( BroadcastType.PASSIVE, msg.getMessageType(), "wrong message type" );
    }

    /**
     * Test sending a remove broadcast.
     *
     * @throws Exception on error
     */
    @Test
    void testRemoveBroadcast()
        throws Exception
    {
        // SETUP
        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add("testCache");

        // DO WORK
        sender.removeBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull( msg, "message not received" );
        assertEquals( SENDING_PORT, msg.getPort(), "wrong port" );
        assertEquals( BroadcastType.REMOVE, msg.getMessageType(), "wrong message type" );
    }

    /**
     * Test sending a request broadcast.
     *
     * @throws Exception on error
     */
    @Test
    void testRequestBroadcast()
        throws Exception
    {
        // DO WORK
        sender.requestBroadcast(1L);

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull( msg, "message not received" );
        assertEquals( BroadcastType.REQUEST, msg.getMessageType(), "wrong message type" );
    }
}
