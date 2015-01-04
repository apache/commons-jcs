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
 * Test which exercises the block disk cache. This one uses three different
 * regions for three threads.
 */
public class BlockDiskCacheConcurrentUnitTest
    extends TestCase
{
    /**
     * Number of items to cache, twice the configured maxObjects for the memory
     * cache regions.
     */
    private static int items = 200;

    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     * @throws Exception
     */
    public BlockDiskCacheConcurrentUnitTest( String testName )
        throws Exception
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
        String[] testCaseName = { BlockDiskCacheConcurrentUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     * @throws Exception
     */
    public static Test suite()
        throws Exception
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        JCS.setConfigFilename( "/TestBlockDiskCache.ccf" );
        JCS.getInstance( "indexedRegion1" ).clear();
        JCS.getInstance( "indexedRegion2" ).clear();
        JCS.getInstance( "indexedRegion3" ).clear();

        suite.addTest( new BlockDiskCacheConcurrentUnitTest( "testBlockDiskCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion1" );
            }
        } );

        suite.addTest( new BlockDiskCacheConcurrentUnitTest( "testBlockDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion2" );
            }
        } );

        suite.addTest( new BlockDiskCacheConcurrentUnitTest( "testBlockDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion3" );
            }
        } );

        suite.addTest( new BlockDiskCacheConcurrentUnitTest( "testBlockDiskCache4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegionInRange( "indexedRegion3", 300, 600 );
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
        JCS.setConfigFilename( "/TestBlockDiskCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     *
     * @throws Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region )
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = 0; i <= items; i++ )
        {
            String value = jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value );
        }

        // Test that getElements returns all the expected values
        Set<String> keys = new HashSet<String>();
        for ( int i = 0; i <= items; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = 0; i <= items; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", region + " data " + i, element.getVal() );
        }

        // Remove all the items
        for ( int i = 0; i <= items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal
        // another thread may have inserted since
        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key" + "\n stats " + jcs.getStats(), jcs
                .get( i + ":key" ) );
        }
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     * @param start
     * @param end
     *
     * @throws Exception
     *                If an error occurs
     */
    public void runTestForRegionInRange( String region, int start, int end )
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = start; i <= end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = start; i <= end; i++ )
        {
            String value = jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value );
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
            assertEquals( "value " + i + ":key", region + " data " + i, element.getVal() );
        }

        // Remove all the items
        for ( int i = start; i <= end; i++ )
        {
            jcs.remove( i + ":key" );
        }

//        System.out.println( jcs.getStats() );

        // Verify removal
        // another thread may have inserted since
        for ( int i = start; i <= end; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key " + "\n stats " + jcs.getStats(), jcs.get( i
                + ":key" ) );
        }
    }
}
