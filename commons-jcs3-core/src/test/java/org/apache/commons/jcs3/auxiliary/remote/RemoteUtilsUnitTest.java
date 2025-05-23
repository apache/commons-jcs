package org.apache.commons.jcs3.auxiliary.remote;

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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.rmi.registry.Registry;

import org.junit.jupiter.api.Test;

/**
 * Simple tests for remote utils. It is difficult to verify most of the things is does.
 */
class RemoteUtilsUnitTest
{
    /**
     * Call create registry.
     * <p>
     * The exception is in the security manager setting.
     * </p>
     */
    @Test
    void testCreateRegistry()
    {
        final Registry registry = RemoteUtils.createRegistry( 1102 );
        assertNotNull( registry, "Registry should not be null" );
    }

    @Test
    void testGetNamingURL()
    {
        assertEquals("//host:1/servicename", RemoteUtils.getNamingURL("host",1,"servicename"));
        assertEquals("//127.0.0.1:2/servicename", RemoteUtils.getNamingURL("127.0.0.1",2,"servicename"));
        assertEquals("//[0:0:0:0:0:0:0:1%251]:3/servicename", RemoteUtils.getNamingURL("0:0:0:0:0:0:0:1%1",3,"servicename"));
    }

    @Test
    void testParseServerAndPort()
    {
        RemoteLocation loc = RemoteLocation.parseServerAndPort("server1:1234");
        assertEquals("server1", loc.getHost());
        assertEquals(1234, loc.getPort());

        loc = RemoteLocation.parseServerAndPort("  server2  :  4567  ");
        assertEquals("server2", loc.getHost());
        assertEquals(4567, loc.getPort());

        loc = RemoteLocation.parseServerAndPort("server2  :  port");
        assertNull(loc);
    }
}
