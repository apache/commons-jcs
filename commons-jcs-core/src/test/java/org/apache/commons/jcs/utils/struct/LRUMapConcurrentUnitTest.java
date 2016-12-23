package org.apache.commons.jcs.utils.struct;

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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Iterator;

/**
 * Tests the LRUMap
 *
 */
public class LRUMapConcurrentUnitTest
    extends TestCase
{
    /** number to test with */
    private static int items = 20000;

    /**
     * Constructor for the TestSimpleLoad object
     * <p>
     * @param testName
     *            Description of the Parameter
     */
    public LRUMapConcurrentUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * A unit test suite for JUnit
     * <p>
     * @return The test suite
     */
    public static Test suite()
    {
        // run the basic tests
        TestSuite suite = new TestSuite( LRUMapConcurrentUnitTest.class );

        // run concurrent tests
        final LRUMap<String, String> map = new LRUMap<String, String>( 2000 );
        suite.addTest( new LRUMapConcurrentUnitTest( "conc1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runConcurrentPutGetTests( map, 2000 );
            }
        } );
        suite.addTest( new LRUMapConcurrentUnitTest( "conc2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runConcurrentPutGetTests( map, 2000 );
            }
        } );
        suite.addTest( new LRUMapConcurrentUnitTest( "conc3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runConcurrentPutGetTests( map, 2000 );
            }
        } );

        // run more concurrent tests
        final int max2 = 20000;
        final LRUMap<String, String> map2 = new LRUMap<String, String>( max2 );
        suite.addTest( new LRUMapConcurrentUnitTest( "concB1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runConcurrentRangeTests( map2, 10000, max2 );
            }
        } );
        suite.addTest( new LRUMapConcurrentUnitTest( "concB1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runConcurrentRangeTests( map2, 0, 9999 );
            }
        } );

        return suite;
    }

    /**
     * Just test that we can put, get and remove as expected.
     * <p>
     * @throws Exception
     *                Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        LRUMap<String, String> map = new LRUMap<String, String>( items );

        for ( int i = 0; i < items; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = items - 1; i >= 0; i-- )
        {
            String res = map.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

        // test removal
        map.remove( "300:key" );
        assertNull( map.get( "300:key" ) );

    }

    /**
     * Just make sure that the LRU functions in he most simple case.
     *
     * @throws Exception
     *                Description of the Exception
     */
    public void testLRURemoval()
        throws Exception
    {
        int total = 10;
        LRUMap<String, String> map = new LRUMap<String, String>( total );
        map.setChunkSize( 1 );

        // put the max in
        for ( int i = 0; i < total; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        Iterator<?> it = map.entrySet().iterator();
        while ( it.hasNext() )
        {
            assertNotNull( it.next() );
        }
//        System.out.println( map.getStatistics() );

        // get the max out backwards
        for ( int i = total - 1; i >= 0; i-- )
        {
            String res = map.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

//        System.out.println( map.getStatistics() );

        //since we got them backwards the total should be at the end.
        // add one confirm that total is gone.
        map.put( ( total ) + ":key", "data" + ( total ) );
        assertNull( map.get( ( total - 1 ) + ":key" ) );

    }

    /**
     * @throws Exception
     */
    public void testLRURemovalAgain()
        throws Exception
    {
        int total = 10000;
        LRUMap<String, String> map = new LRUMap<String, String>( total );
        map.setChunkSize( 1 );

        // put the max in
        for ( int i = 0; i < total * 2; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        // get the total number, these should be null
        for ( int i = total - 1; i >= 0; i-- )
        {
            assertNull( map.get( i + ":key" ) );

        }

        // get the total to total *2 items out, these should be found.
        for ( int i = ( total * 2 ) - 1; i >= total; i-- )
        {
            String res = map.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

//        System.out.println( map.getStatistics() );

    }

    /**
     * Just make sure that we can put and get concurrently
     *
     * @param map
     * @param items
     * @throws Exception
     */
    public void runConcurrentPutGetTests( LRUMap<String, String> map, int items )
        throws Exception
    {
        for ( int i = 0; i < items; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = items - 1; i >= 0; i-- )
        {
            String res = map.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }
    }

    /**
     * Put, get, and remove from a range. This should occur at a range that is
     * not touched by other tests.
     * <p>
     * @param map
     * @param start
     * @param end
     * @throws Exception
     */
    public void runConcurrentRangeTests( LRUMap<String, String> map, int start, int end )
        throws Exception
    {
        for ( int i = start; i < end; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = end - 1; i >= start; i-- )
        {
            String res = map.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

        // test removal
        map.remove( start + ":key" );
        assertNull( map.get( start + ":key" ) );
    }
}
