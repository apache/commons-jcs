package org.apache.commons.jcs3.auxiliary.remote.server;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.junit.jupiter.api.Test;

/**
 * Tests for the remote cache server attributes.
 */
class RemoteCacheServerAttributesUnitTest
{

    /**
     * Verify that the type is set correctly and that the correct name is returned for the type.
     */
    @Test
    void testSetRemoteTypeName_cluster()
    {
        final RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        attributes.setRemoteTypeName( "CLUSTER" );
        assertEquals( RemoteType.CLUSTER, attributes.getRemoteType(), "Wrong type." );
        assertEquals( "CLUSTER", attributes.getRemoteTypeName(), "Wrong name" );
    }

    /**
     * Verify that the type is set correctly and that the correct name is returned for the type.
     */
    @Test
    void testSetRemoteTypeName_local()
    {
        final RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        attributes.setRemoteTypeName( "LOCAL" );
        assertEquals( RemoteType.LOCAL, attributes.getRemoteType(), "Wrong type." );
        assertEquals( "LOCAL", attributes.getRemoteTypeName(), "Wrong name" );
    }

    /**
     * Verify that we get a string, even if not attributes are set.
     */
    @Test
    void testToString()
    {
        final RemoteCacheServerAttributes attributes = new RemoteCacheServerAttributes();
        assertNotNull( attributes.toString(), "Should have a string." );
    }
}
