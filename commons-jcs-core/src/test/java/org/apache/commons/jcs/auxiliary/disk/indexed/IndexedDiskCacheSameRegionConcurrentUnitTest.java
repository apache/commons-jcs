package org.apache.commons.jcs.auxiliary.disk.indexed;

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
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 */
public class IndexedDiskCacheSameRegionConcurrentUnitTest
    extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public IndexedDiskCacheSameRegionConcurrentUnitTest( String testName )
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
        String[] testCaseName = { IndexedDiskCacheSameRegionConcurrentUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new IndexedDiskCacheSameRegionConcurrentUnitTest( "testIndexedDiskCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 0, 200 );
            }
        } );

        suite.addTest( new IndexedDiskCacheSameRegionConcurrentUnitTest( "testIndexedDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 1000, 1200 );
            }
        } );

        suite.addTest( new IndexedDiskCacheSameRegionConcurrentUnitTest( "testIndexedDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 2000, 2200 );
            }
        } );

        suite.addTest( new IndexedDiskCacheSameRegionConcurrentUnitTest( "testIndexedDiskCache4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 2200, 5200 );
            }
        } );

        suite.addTest( new IndexedDiskCacheSameRegionConcurrentUnitTest( "testIndexedDiskCache5" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 0, 5100 );
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
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

    // /**
    // * Tests the region which uses the indexed disk cache
    // */
    // public void testIndexedDiskCache()
    // throws Exception
    // {
    // runTestForRegion( "indexedRegion" );
    // }
    //
    // /**
    // * Tests the region which uses the indexed disk cache
    // */
    // public void testIndexedDiskCache2()
    // throws Exception
    // {
    // runTestForRegion( "indexedRegion2" );
    // }

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
    public void runTestForRegion( String region, int start, int end )
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
            String key = i + ":key";
            String value = jcs.get( key );

            assertEquals( "Wrong value for key [" + key + "]", region + " data " + i, value );
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

        // you can't remove in one thread and expect them to be in another //
        //          Remove all the items
        //
        //          for ( int i = start; i <= end; i++ ) { jcs.remove( i + ":key" ); } //
        //          Verify removal
        //
        //          for ( int i = start; i <= end; i++ ) { assertNull( "Removed key
        //          should be null: " + i + ":key", jcs.get( i + ":key" ) ); }
    }
}
