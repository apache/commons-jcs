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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryMessage.BroadcastType;
import org.apache.commons.jcs3.utils.net.HostNameUtil;
import org.apache.commons.jcs3.utils.serialization.EncryptingSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the sender with EncryptingSerializer.
 */
public class UDPDiscoverySenderEncryptedUnitTest
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
     * <p>
     * @throws Exception on error
     */
    @Before
    public void setUp()
        throws Exception
    {
        assumeNotNull("This machine does not support multicast", HostNameUtil.getMulticastNetworkInterface());

        final EncryptingSerializer serializer = new EncryptingSerializer();
        serializer.setPreSharedKey("my_key");

        futureMsg = new CompletableFuture<>();
        receiver = new UDPDiscoveryReceiver( msg -> {
            futureMsg.complete(msg);
        }, null, ADDRESS, PORT );
        receiver.setSerializer(serializer);
        final Thread t = new Thread( receiver );
        t.start();

        sender = new UDPDiscoverySender(null, ADDRESS, PORT, 1, serializer);
    }

    /**
     * Kill off the sender and receiver.
     * <p>
     * @throws Exception on error
     */
    @After
    public void tearDown()
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
     * <p>
     * @throws Exception on error
     */
    @Test
    public void testPassiveBroadcast()
        throws Exception
    {
        // SETUP
        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add("testCache");

        // DO WORK
        sender.passiveBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull("message not received", msg);
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", BroadcastType.PASSIVE, msg.getMessageType() );
    }

    /**
     * Test sending a remove broadcast.
     * <p>
     * @throws Exception on error
     */
    @Test
    public void testRemoveBroadcast()
        throws Exception
    {
        // SETUP
        final ArrayList<String> cacheNames = new ArrayList<>();
        cacheNames.add("testCache");

        // DO WORK
        sender.removeBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull("message not received", msg);
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", BroadcastType.REMOVE, msg.getMessageType() );
    }

    /**
     * Test sending a request broadcast.
     * <p>
     * @throws Exception on error
     */
    @Test
    public void testRequestBroadcast()
        throws Exception
    {
        // DO WORK
        sender.requestBroadcast(1L);

        // VERIFY
        final UDPDiscoveryMessage msg = futureMsg.get(3, TimeUnit.SECONDS);
        assertNotNull("message not received", msg);
        assertEquals( "wrong message type", BroadcastType.REQUEST, msg.getMessageType() );
    }
}
