package org.apache.jcs;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 *
 * @version $Id$
 */
public class TestBDBJEDiskCacheConcurrent extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     */
    public TestBDBJEDiskCacheConcurrent( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     */
    public static void main( String args[] )
    {
        String[] testCaseName = {TestDiskCacheConcurrent.class.getName()};
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return    The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new TestBDBJEDiskCacheConcurrent( "testIndexedDiskCache1" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 0, 200 );
            }
        } );

        suite.addTest( new TestBDBJEDiskCacheConcurrent( "testIndexedDiskCache2" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 1000, 1200 );
            }
        } );

        suite.addTest( new TestBDBJEDiskCacheConcurrent( "testIndexedDiskCache3" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 2000, 3200 );
            }
        });

        suite.addTest( new TestBDBJEDiskCacheConcurrent( "testIndexedDiskCache4" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 2200, 5200 );
            }
        });

        suite.addTest( new TestBDBJEDiskCacheConcurrent( "testIndexedDiskCache5" )
        {
            public void runTest() throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 0, 10000 );
            }
        });


        return suite;
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestBDBJEDiskCacheCon.ccf" );
    }



//    /**
//     * Tests the region which uses the indexed disk cache
//     */
//    public void testIndexedDiskCache()
//        throws Exception
//    {
//        runTestForRegion( "indexedRegion" );
//    }
//
//    /**
//     * Tests the region which uses the indexed disk cache
//     */
//    public void testIndexedDiskCache2()
//        throws Exception
//    {
//        runTestForRegion( "indexedRegion2" );
//    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region Name of the region to access
     *
     * @exception Exception If an error occurs
     */
    public void runTestForRegion( String region, int start, int end )
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
            String value = ( String ) jcs.get( i + ":key" );

            this.assertEquals( region + " data " + i, value );
        }

/*// you can't remove in one thread and expect them to be in another
        // Remove all the items

        for ( int i = start; i <= end; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = start; i <= end; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key",
                        jcs.get( i + ":key" ) );
        }
*/

    }
}
