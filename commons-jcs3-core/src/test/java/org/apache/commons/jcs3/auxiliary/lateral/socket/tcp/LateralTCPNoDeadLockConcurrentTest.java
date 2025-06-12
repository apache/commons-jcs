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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs3.JCS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the tcp lateral cache. Runs two threads against the
 * same region and two against other regions.
 */
public class LateralTCPNoDeadLockConcurrentTest
{

    /**
     * Setup method for JUnit 5, executed before each test.
     */
    @BeforeEach
    void setUp()
    {
        System.setProperty( "jcs.auxiliary.LTCP.attributes.PutOnlyMode", "false" );
        JCS.setConfigFilename( "/TestTCPLateralCacheConcurrent.ccf" );
    }

    /**
     * Test tearDown. Dispose of the cache after each test.
     */
    @AfterEach
    void tearDown()
    {
        JCS.shutdown();
    }

    @Test
    void testLateralTCPCache1()
        throws Exception
    {
        LateralTCPConcurrentRandomTestUtil.runTestForRegion( "region1", 1, 200, 1 );
    }

    @Test
    void testLateralTCPCache2()
        throws Exception
    {
        LateralTCPConcurrentRandomTestUtil.runTestForRegion( "region2", 10000, 12000, 2 );
    }

    @Test
    void testLateralTCPCache3()
        throws Exception
    {
        LateralTCPConcurrentRandomTestUtil.runTestForRegion( "region3", 10000, 12000, 3 );
    }

    @Test
    void testLateralTCPCache4()
        throws Exception
    {
        LateralTCPConcurrentRandomTestUtil.runTestForRegion( "region3", 10000, 13000, 4 );
    }

    @Test
    void testLateralTCPCache5()
        throws Exception
    {
        LateralTCPConcurrentRandomTestUtil.runTestForRegion( "region4", 10000, 11000, 5 );
    }
}
