package org.apache.commons.jcs3;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

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

import org.junit.Before;

/**
 * Test which exercises the hierarchical removal when the cache is active.
 */
public class ConcurrentRemovalLoadTest
{
    /**
     * A unit test suite for JUnit. This verifies that we can remove hierarchically while the region
     * is active.
     * @return The test suite
     */
    public static Test suite()
    {
        final ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest(new TestCase("testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runTestPutThenRemoveCategorical( 0, 200 );
            }
        });

        suite.addTest(new TestCase("testPutCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runPutInRange( 300, 400 );
            }
        });

        suite.addTest(new TestCase("testPutCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runPutInRange( 401, 600 );
            }
        });

        // stomp on previous put
        suite.addTest(new TestCase("testPutCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runPutInRange( 401, 600 );
            }
        });

        suite.addTest(new TestCase("testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runTestPutThenRemoveCategorical( 601, 700 );
            }
        });

        suite.addTest(new TestCase("testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runTestPutThenRemoveCategorical( 701, 800 );
            }
        });

        suite.addTest(new TestCase("testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runTestPutThenRemoveCategorical( 901, 1000 );
            }
        });

        suite.addTest(new TestCase("testPutCache2" )
        {
            // verify that there are no errors with concurrent gets.
            @Override
            public void runTest()
                throws Exception
            {
                RemovalTestUtil.runGetInRange( 0, 1000, false );
            }
        });
        return suite;
    }

    /**
     * Test setup
     * <p>
     * @throws Exception
     */
    @Before
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestRemoval.ccf" );
    }
}
