package org.apache.commons.jcs.auxiliary.disk.block;

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
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * @throws InterruptedException
     */
    public static void main( String args[] ) throws InterruptedException
    {
        String[] testCaseName = { BlockDiskCacheSameRegionConcurrentUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );

        // Give test threads some time to finish
        Thread.sleep(2000);
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
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 0, 200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 1000, 1200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "blockRegion4", 2000, 2200 );
            }
        } );

        suite.addTest( new BlockDiskCacheSameRegionConcurrentUnitTest( "testBlockDiskCache4" )
        {
            @Override
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
    @Override
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestBlockDiskCacheCon.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * @param region Name of the region to access
     * @param start
     * @param end
     * @throws Exception If an error occurs
     */
    public void runTestForRegion( String region, int start, int end )
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache

        for ( int i = start; i <= end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i + "-" + region );
        }

        // Test that all items are in cache

        for ( int i = start; i <= end; i++ )
        {
            String key = i + ":key";
            String value = jcs.get( key );

            assertEquals( "Wrong value for key [" + key + "]", region + " data " + i + "-" + region, value );
        }

        // Test that getElements returns all the expected values
        Set<String> keys = new HashSet<String>();
        for ( int i = start; i <= end; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = start; i <= end; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", region + " data " + i + "-" + region, element.getVal() );
        }
    }
}
