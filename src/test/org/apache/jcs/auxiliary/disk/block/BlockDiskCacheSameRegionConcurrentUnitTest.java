package org.apache.jcs.auxiliary.disk.block;

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

import org.apache.jcs.JCS;

/**
 * Test which exercises the block disk cache. Runs three threads against the same region.
 */
public class BlockDiskCacheSameRegionConcurrentUnitTest
    extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     * <p>
     * @param testName
     */
    public BlockDiskCacheSameRegionConcurrentUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     * <p>
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { BlockDiskCacheSameRegionConcurrentUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache1" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 0, 200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache2" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 1000, 1200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache3" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 2000, 2200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache4" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 2200, 5200 );
            }
        } );

        return suite;
    }

    /**
     * Test setup.  Sets the config name and clears the region.
     * <p>
     * @throws Exception
     */
    public void setUp() throws Exception
    {
        JCS.setConfigFilename( "/TestBlockDiskCacheCon.ccf" );
        JCS.getInstance( "blockRegion4" ).clear();
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * @param region Name of the region to access
     * @param start
     * @param end
     * @exception Exception If an error occurs
     */
    public void runTestForRegion( String region, int start, int end )
        throws Exception
    {
        JCS jcs = JCS.getInstance( region );

        // Add items to cache

        for ( int i = start; i <= end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i + "-" + region );
        }

        // Test that all items are in cache

        for ( int i = start; i <= end; i++ )
        {
            String key = i + ":key";
            String value = (String) jcs.get( key );

            assertEquals( "Wrong value for key [" + key + "]", region + " data " + i + "-" + region, value );
        }
    }
}
