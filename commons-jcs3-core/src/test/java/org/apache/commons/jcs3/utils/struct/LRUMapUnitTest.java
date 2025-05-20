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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for the LRUMap
 */
class LRUMapUnitTest
{

    /**
     * Add items to the map and then test to see that they come back in the entry set.
     */
    @Test
    void testGetEntrySet()
    {
        final int size = 10;
        final Map<String, String> cache = new LRUMap<>( size );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        final Set<Entry<String, String>> entries = cache.entrySet();
        assertEquals( size, entries.size(), "Set contains the wrong number of items." );

        // check minimal correctness
        for (final Entry<String, String> data : entries)
        {
            assertTrue( data.getValue().indexOf( "data:" ) != -1, "Data is wrong." );
        }
    }

    /**
     * Put and then remove.  Make sure the element is returned.
     */
    @Test
    void testPutAndRemove()
    {
        final int size = 10;
        final Map<String, String> cache = new LRUMap<>( size );

        cache.put( "key:" + 1, "data:" + 1 );
        final String data = cache.remove( "key:" + 1 );
        assertEquals( "data:" + 1, data, "Data is wrong." );
    }

    /**
     * Put into the lru with no limit and then make sure they are all there.
     */
    @Test
    void testPutWithNoSizeLimit()
    {
        final int size = 10;
        final Map<String, String> cache = new LRUMap<>( );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        for ( int i = 0; i < size; i++ )
        {
            final String data = cache.get( "key:" + i );
            assertEquals( "data:" + i, data, "Data is wrong." );
        }
    }

    /**
     * Put up to the size limit and then make sure they are all there.
     */
    @Test
    void testPutWithSizeLimit()
    {
        final int size = 10;
        final Map<String, String> cache = new LRUMap<>( size );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        for ( int i = 0; i < size; i++ )
        {
            final String data = cache.get( "key:" + i );
            assertEquals( "data:" + i, data, "Data is wrong." );
        }
    }

    /**
     * Call remove on an empty map
     */
    @Test
    void testRemoveEmpty()
    {
        final int size = 10;
        final Map<String, String> cache = new LRUMap<>( size );

        final Object returned = cache.remove( "key:" + 1 );
        assertNull( returned, "Shouldn't hvae anything." );
    }

}
