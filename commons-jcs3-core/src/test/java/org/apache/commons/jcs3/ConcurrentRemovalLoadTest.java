package org.apache.commons.jcs3;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Test which exercises the hierarchical removal when the cache is active.
 */
@Execution(ExecutionMode.CONCURRENT)
class ConcurrentRemovalLoadTest
{

    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestRemoval.ccf" );
    }

    @Test
    void testRemoveCache1_FirstRange()
        throws Exception
    {
        RemovalTestUtil.runTestPutThenRemoveCategorical( 0, 200 );
    }

    @Test
    void testPutCache1()
        throws Exception
    {
        RemovalTestUtil.runPutInRange( 300, 400 );
    }

    @Test
    void testPutCache2_FirstRange()
        throws Exception
    {
        RemovalTestUtil.runPutInRange( 401, 600 );
    }

    @Test
    void testPutCache3_StompPreviousPut()
        throws Exception
    {
        RemovalTestUtil.runPutInRange( 401, 600 );
    }

    @Test
    void testRemoveCache1_SecondRange()
        throws Exception
    {
        RemovalTestUtil.runTestPutThenRemoveCategorical( 601, 700 );
    }

    @Test
    void testRemoveCache1_ThirdRange()
        throws Exception
    {
        RemovalTestUtil.runTestPutThenRemoveCategorical( 701, 800 );
    }

    @Test
    void testRemoveCache1_FourthRange()
        throws Exception
    {
        RemovalTestUtil.runTestPutThenRemoveCategorical( 901, 1000 );
    }

    @Test
    void testPutCache2_WithConcurrentGets()
        throws Exception
    {
        // verify that there are no errors with concurrent gets
        RemovalTestUtil.runGetInRange( 0, 1000, false );
    }
}