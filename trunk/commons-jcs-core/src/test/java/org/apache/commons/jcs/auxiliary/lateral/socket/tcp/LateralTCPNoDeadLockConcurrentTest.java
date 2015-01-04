package org.apache.commons.jcs.auxiliary.lateral.socket.tcp;

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
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

/**
 * Test which exercises the tcp lateral cache. Runs two threads against the
 * same region and two against other regions.
 */
public class LateralTCPNoDeadLockConcurrentTest
    extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public LateralTCPNoDeadLockConcurrentTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     *
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { LateralTCPNoDeadLockConcurrentTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     */
    public static Test suite()
    {

        System.setProperty( "jcs.auxiliary.LTCP.attributes.PutOnlyMode", "false" );

        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new LateralTCPConcurrentRandomTestUtil( "testLateralTCPCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "region1", 1, 200, 1 );
            }
        } );

        suite.addTest( new LateralTCPConcurrentRandomTestUtil( "testLateralTCPCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "region2", 10000, 12000, 2 );
            }
        } );

        suite.addTest( new LateralTCPConcurrentRandomTestUtil( "testLateralTCPCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "region3", 10000, 12000, 3 );
            }
        } );

        suite.addTest( new LateralTCPConcurrentRandomTestUtil( "testLateralTCPCache4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "region3", 10000, 13000, 4 );
            }
        } );

        suite.addTest( new LateralTCPConcurrentRandomTestUtil( "testLateralTCPCache5" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "region4", 10000, 11000, 5 );
            }
        } );

        return suite;
    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCacheConcurrent.ccf" );
    }

    /**
     * Test tearDown. Dispose of the cache.
     */
    @Override
    public void tearDown()
    {
        try
        {
            CompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();
            cacheMgr.shutDown();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
