package org.apache.commons.jcs3.utils.struct;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 * Tests the LRUMap
 */
class LRUMapConcurrentUnitTest
{

    // Number to test with
    private static final int items = 20000;

    /**
     * Just make sure that we can put and get concurrently
     *
     * @param map LRUMap instance
     * @param items Number of items to test with
     * @throws Exception If an error occurs
     */
    public static void runConcurrentPutGetTests( final LRUMap<String, String> map, final int items )
        throws Exception
    {
        for ( int i = 0; i < items; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = items - 1; i >= 0; i-- )
        {
            final String res = map.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }
    }

    /**
     * Put, get, and remove from a range. This should occur at a range that is
     * not touched by other tests.
     *
     * @param map LRUMap instance
     * @param start Start index of the range
     * @param end End index of the range
     * @throws Exception If an error occurs
     */
    public static void runConcurrentRangeTests( final LRUMap<String, String> map, final int start, final int end )
        throws Exception
    {
        for ( int i = start; i < end; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = end - 1; i >= start; i-- )
        {
            final String res = map.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        // Test removal
        map.remove( start + ":key" );
        assertNull( map.get( start + ":key" ) );
    }

    @Test
    void testLRURemoval()
        throws Exception
    {
        final int total = 10;
        final LRUMap<String, String> map = new LRUMap<>( total );

        // Put the max in
        for ( int i = 0; i < total; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        final Iterator<?> it = map.entrySet().iterator();
        while ( it.hasNext() )
        {
            assertNotNull( it.next() );
        }

        // Get the max out backwards
        for ( int i = total - 1; i >= 0; i-- )
        {
            final String res = map.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        // Since we got them backwards the total should be at the end.
        // Add one and confirm that total is gone.
        map.put( total + ":key", "data" + total );
        assertNull( map.get( total - 1 + ":key" ) );
    }

    @Test
    void testLRURemovalAgain()
        throws Exception
    {
        final int total = 10000;
        final LRUMap<String, String> map = new LRUMap<>( total );

        // Put the max in
        for ( int i = 0; i < total * 2; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        // Get the total number, these should be null
        for ( int i = total - 1; i >= 0; i-- )
        {
            assertNull( map.get( i + ":key" ) );
        }

        // Get the total to total *2 items out, these should be found.
        for ( int i = total * 2 - 1; i >= total; i-- )
        {
            final String res = map.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }
    }

    @Test
    void testSimpleLoad()
        throws Exception
    {
        final LRUMap<String, String> map = new LRUMap<>( items );

        for ( int i = 0; i < items; i++ )
        {
            map.put( i + ":key", "data" + i );
        }

        for ( int i = items - 1; i >= 0; i-- )
        {
            final String res = map.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        // Test removal
        map.remove( "300:key" );
        assertNull( map.get( "300:key" ) );
    }

    @Test
    void testConcurrentPutGet()
        throws Exception
    {
        final LRUMap<String, String> map = new LRUMap<>( 2000 );

        // Run concurrent put/get tests
        runConcurrentPutGetTests( map, 2000 );
    }

    @Test
    void testConcurrentRange()
        throws Exception
    {
        final LRUMap<String, String> map = new LRUMap<>( 20000 );

        // Run concurrent range tests
        runConcurrentRangeTests( map, 10000, 20000 );
        runConcurrentRangeTests( map, 0, 10000 );
    }
}
