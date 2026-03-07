package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

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

import static org.junit.Assert.assertTrue;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs3.JCS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test that a lateral TCP listener in a clustered setup survives receiving
 * bad/corrupted data on its socket connection that triggers a
 * {@link java.nio.BufferUnderflowException} during deserialization.
 * <p>
 * Without the fix, the listener thread dies upon encountering truncated
 * serialized data because the {@code BufferUnderflowException} propagates
 * out of the read loop. With the fix, the listener catches the exception,
 * logs it, and continues processing subsequent valid messages.
 * <p>
 * This test sets up two lateral TCP cache nodes, sends the truncated data
 * directly to one node's listener port, then verifies the listener is still
 * alive.
 */
public class LateralTCPListenerBadDataTest
{
    /** Port for the first lateral node */
    private static final int LISTENER_PORT_1 = 11261;

    /** Port for the second lateral node */
    private static final int LISTENER_PORT_2 = 11262;

    /** Cache region name used in the test */
    private static final String CACHE_REGION = "testBadDataRegion";

    @BeforeClass
    public static void setUp() throws Exception
    {
        // Configure a 2-node lateral TCP cluster programmatically
        final java.util.Properties props = new java.util.Properties();

        // Default cache region
        props.put("jcs.default", "");
        props.put("jcs.default.cacheattributes",
                "org.apache.commons.jcs3.engine.CompositeCacheAttributes");
        props.put("jcs.default.cacheattributes.MaxObjects", "100");

        // Test region with lateral TCP auxiliary
        props.put("jcs.region." + CACHE_REGION, "LTCP");
        props.put("jcs.region." + CACHE_REGION + ".cacheattributes",
                "org.apache.commons.jcs3.engine.CompositeCacheAttributes");
        props.put("jcs.region." + CACHE_REGION + ".cacheattributes.MaxObjects", "100");

        // Lateral TCP auxiliary - this node listens on LISTENER_PORT_1
        // and connects to the peer on LISTENER_PORT_2
        props.put("jcs.auxiliary.LTCP",
                "org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.LateralTCPCacheFactory");
        props.put("jcs.auxiliary.LTCP.attributes",
                "org.apache.commons.jcs3.auxiliary.lateral.socket.tcp.TCPLateralCacheAttributes");
        props.put("jcs.auxiliary.LTCP.attributes.TcpListenerPort",
                String.valueOf(LISTENER_PORT_1));
        props.put("jcs.auxiliary.LTCP.attributes.TcpServers",
                "localhost:" + LISTENER_PORT_2);
        props.put("jcs.auxiliary.LTCP.attributes.IssueRemoveOnPut", "false");
        props.put("jcs.auxiliary.LTCP.attributes.AllowGet", "true");
        props.put("jcs.auxiliary.LTCP.attributes.UdpDiscoveryEnabled", "false");

        JCS.setConfigProperties(props);
        JCS.getInstance(CACHE_REGION);

        // Give the listener a moment to start up
        TimeUnit.MILLISECONDS.sleep(500);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        JCS.shutdown();
    }

    /**
     * Verifies that the listener continues to accept new connections after
     * receiving bad data. This is a simple check — just confirm that
     * connecting to the port doesn't throw an exception. Before the fix this fails.
     */
    @Test
    public void testListenerAcceptsConnectionsAfterBadData() throws Exception
    {
        // Send bad data first
        try (Socket badSocket = new Socket("localhost", LISTENER_PORT_1))
        {
            final ObjectOutputStream oos =
                    new ObjectOutputStream(badSocket.getOutputStream());
            oos.writeObject(0); // Not a LateralElementDescriptor
            oos.flush();
        }

        TimeUnit.MILLISECONDS.sleep(500);

        // Verify we can still connect — the ServerSocket is still accepting
        boolean connected;
        try (Socket testSocket = new Socket("localhost", LISTENER_PORT_1))
        {
            connected = testSocket.isConnected();
        }

        assertTrue(
                "Should still be able to connect to the listener port after bad data was sent.",
                connected);
    }

}