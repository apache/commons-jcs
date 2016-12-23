package org.apache.commons.jcs.utils.discovery;

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
import org.apache.commons.jcs.utils.discovery.UDPDiscoveryMessage.BroadcastType;

import java.util.ArrayList;

/**
 * Tests for the sender.
 */
public class UDPDiscoverySenderUnitTest
    extends TestCase
{
    /** multicast address to send/receive on */
    private static final String ADDRESS = "228.4.5.9";

    /** multicast address to send/receive on */
    private static final int PORT = 5556;

    /** imaginary host address for sending */
    private static final String SENDING_HOST = "imaginary host address";

    /** imaginary port for sending */
    private static final int SENDING_PORT = 1;

    /** receiver instance for tests */
    private UDPDiscoveryReceiver receiver;

    /** sender instance for tests */
    private UDPDiscoverySender sender;

    /**
     * Set up the receiver. Maybe better to just code sockets here? Set up the sender for sending
     * the message.
     * <p>
     * @throws Exception on error
     */
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        receiver = new UDPDiscoveryReceiver( null, ADDRESS, PORT );
        sender = new UDPDiscoverySender( ADDRESS, PORT );
    }

    /**
     * Kill off the sender and receiver.
     * <p>
     * @throws Exception on error
     */
    @Override
    protected void tearDown()
        throws Exception
    {
        receiver.shutdown();
        sender.destroy();
        super.tearDown();
    }

    /**
     * Test sending a live messages.
     * <p>
     * @throws Exception on error
     */
    public void testPassiveBroadcast()
        throws Exception
    {
        // SETUP
        ArrayList<String> cacheNames = new ArrayList<String>();

        // DO WORK
        sender.passiveBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage() ;

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        // disabled test because of JCS-89
        // assertEquals( "wrong host", SENDING_HOST, msg.getHost() );
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", BroadcastType.PASSIVE, msg.getMessageType() );
    }

    /**
     * Test sending a remove broadcast.
     * <p>
     * @throws Exception on error
     */
    public void testRemoveBroadcast()
        throws Exception
    {
        // SETUP
        ArrayList<String> cacheNames = new ArrayList<String>();

        // DO WORK
        sender.removeBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage();

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        // disabled test because of JCS-89
        // assertEquals( "wrong host", SENDING_HOST, msg.getHost() );
        assertEquals( "wrong port", SENDING_PORT, msg.getPort() );
        assertEquals( "wrong message type", BroadcastType.REMOVE, msg.getMessageType() );
    }

    /**
     * Test sending a request broadcast.
     * <p>
     * @throws Exception on error
     */
    public void testRequestBroadcast()
        throws Exception
    {
        // DO WORK
        sender.requestBroadcast();

        // VERIFY
        // grab the sent message
        Object obj = receiver.waitForMessage();

        assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

        UDPDiscoveryMessage msg = (UDPDiscoveryMessage) obj;
        assertEquals( "wrong message type", BroadcastType.REQUEST, msg.getMessageType() );
    }
}
