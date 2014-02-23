package org.apache.commons.jcs;

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

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Test which exercises the hierarchical removal when the cache is active.
 */
public class ConcurrentRemovalLoadTest
    extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     * @param testName
     */
    public ConcurrentRemovalLoadTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { RemovalTestUtil.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit. This verfies that we can remove hierarchically while the region
     * is active.
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new RemovalTestUtil( "testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestPutThenRemoveCategorical( 0, 200 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testPutCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runPutInRange( 300, 400 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testPutCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runPutInRange( 401, 600 );
            }
        } );

        // stomp on previous put
        suite.addTest( new RemovalTestUtil( "testPutCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runPutInRange( 401, 600 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestPutThenRemoveCategorical( 601, 700 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestPutThenRemoveCategorical( 701, 800 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testRemoveCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestPutThenRemoveCategorical( 901, 1000 );
            }
        } );

        suite.addTest( new RemovalTestUtil( "testPutCache2" )
        {
            // verify that there are no errors with concurrent gets.
            @Override
            public void runTest()
                throws Exception
            {
                runGetInRange( 0, 1000, false );
            }
        } );
        return suite;
    }

    /**
     * Test setup
     * <p>
     * @throws Exception
     */
    @Override
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestRemoval.ccf" );
        JCS.getInstance( "testCache1" );
    }
}
