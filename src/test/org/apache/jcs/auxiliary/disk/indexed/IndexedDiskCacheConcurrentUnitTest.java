package org.apache.jcs.auxiliary.disk.indexed;

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
 * Test which exercises the indexed disk cache. This one uses three different
 * regions for thre threads.
 *
 * @version $Id: TestDiskCache.java 224346 2005-06-04 02:01:59Z asmuts $
 */
public class IndexedDiskCacheConcurrentUnitTest
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
     */
    public IndexedDiskCacheConcurrentUnitTest( String testName )
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
        String[] testCaseName = { IndexedDiskCacheConcurrentUnitTest.class.getName() };
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

        suite.addTest( new IndexedDiskCacheConcurrentUnitTest( "testIndexedDiskCache1" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion1" );
            }
        } );

        suite.addTest( new IndexedDiskCacheConcurrentUnitTest( "testIndexedDiskCache2" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion2" );
            }
        } );

        suite.addTest( new IndexedDiskCacheConcurrentUnitTest( "testIndexedDiskCache3" )
        {
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion3" );
            }
        } );

        suite.addTest( new IndexedDiskCacheConcurrentUnitTest( "testIndexedDiskCache4" )
        {
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
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     *
     * @exception Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region )
        throws Exception
    {
        JCS jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value );
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
     * @exception Exception
     *                If an error occurs
     */
    public void runTestForRegionInRange( String region, int start, int end )
        throws Exception
    {
        JCS jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = start; i <= end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = start; i <= end; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value );
        }

        // Remove all the items
        for ( int i = start; i <= end; i++ )
        {
            jcs.remove( i + ":key" );
        }

        System.out.println( jcs.getStats() );

        // Verify removal
        // another thread may have inserted since
        for ( int i = start; i <= end; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key " + "\n stats " + jcs.getStats(), jcs.get( i
                + ":key" ) );
        }
    }
}
