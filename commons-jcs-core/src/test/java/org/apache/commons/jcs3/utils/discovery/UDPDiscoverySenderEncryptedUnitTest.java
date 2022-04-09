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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.jcs3.utils.discovery.UDPDiscoveryMessage.BroadcastType;
import org.apache.commons.jcs3.utils.serialization.EncryptingSerializer;

import junit.framework.TestCase;

/**
 * Tests for the sender with EncryptingSerializer.
 */
public class UDPDiscoverySenderEncriptedUnitTest
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

        EncryptingSerializer serializer = new EncryptingSerializer();
        serializer.setPreSharedKey("my_key");
        
        receiver = new UDPDiscoveryReceiver( null, null, ADDRESS, PORT );
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
    @Override
    protected void tearDown()
        throws Exception
    {
        receiver.shutdown();
        sender.close();
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
        final ArrayList<String> cacheNames = new ArrayList<>();

        // DO WORK
        sender.passiveBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        final UDPDiscoveryMessage msg = getMessage();
        assertNotNull("message not received", msg);
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
        final ArrayList<String> cacheNames = new ArrayList<>();

        // DO WORK
        sender.removeBroadcast( SENDING_HOST, SENDING_PORT, cacheNames, 1L );

        // VERIFY
        // grab the sent message
        final UDPDiscoveryMessage msg = getMessage();
        assertNotNull("message not received", msg);
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
        final UDPDiscoveryMessage msg = getMessage();
        assertNotNull("message not received", msg);
        assertEquals( "wrong message type", BroadcastType.REQUEST, msg.getMessageType() );

        
    }
    
    /**
     * Wait for multicast message for 3 seconds
     * 
     * @return the object message or null if nothing received within 3 seconds
     */
    private UDPDiscoveryMessage getMessage() {
    	ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
           public Object call() throws IOException {
              return receiver.waitForMessage();
           }
        };
        Future<Object> future = executor.submit(task);
        try {
        	Object obj = future.get(3, TimeUnit.SECONDS);
 
        	assertTrue( "unexpected crap received", obj instanceof UDPDiscoveryMessage );

            return (UDPDiscoveryMessage) obj;
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
        	return null;
        }
    }
}
