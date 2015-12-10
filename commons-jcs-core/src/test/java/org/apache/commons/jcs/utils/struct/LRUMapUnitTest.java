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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Basic unit tests for the LRUMap
 *
 * @author Aaron Smuts
 *
 */
public class LRUMapUnitTest
    extends TestCase
{

    /**
     * Put up to the size limit and then make sure they are all there.
     *
     */
    public void testPutWithSizeLimit()
    {
        int size = 10;
        Map<String, String> cache = new LRUMap<String, String>( size );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        for ( int i = 0; i < size; i++ )
        {
            String data = cache.get( "key:" + i );
            assertEquals( "Data is wrong.", "data:" + i, data );
        }
    }

    /**
     * Put into the lru with no limit and then make sure they are all there.
     *
     */
    public void testPutWithNoSizeLimit()
    {
        int size = 10;
        Map<String, String> cache = new LRUMap<String, String>( );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        for ( int i = 0; i < size; i++ )
        {
            String data = cache.get( "key:" + i );
            assertEquals( "Data is wrong.", "data:" + i, data );
        }
    }

    /**
     * Put and then remove.  Make sure the element is returned.
     *
     */
    public void testPutAndRemove()
    {
        int size = 10;
        Map<String, String> cache = new LRUMap<String, String>( size );

        cache.put( "key:" + 1, "data:" + 1 );
        String data = cache.remove( "key:" + 1 );
        assertEquals( "Data is wrong.", "data:" + 1, data );
    }

    /**
     * Call remove on an empty map
     *
     */
    public void testRemoveEmpty()
    {
        int size = 10;
        Map<String, String> cache = new LRUMap<String, String>( size );

        Object returned = cache.remove( "key:" + 1 );
        assertNull( "Shouldn't hvae anything.", returned );
    }


    /**
     * Add items to the map and then test to see that they come back in the entry set.
     *
     */
    public void testGetEntrySet()
    {
        int size = 10;
        Map<String, String> cache = new LRUMap<String, String>( size );

        for ( int i = 0; i < size; i++ )
        {
            cache.put( "key:" + i, "data:" + i );
        }

        Set<Entry<String, String>> entries = cache.entrySet();
        assertEquals( "Set contains the wrong number of items.", size, entries.size() );

        // check minimal correctness
        for (Entry<String, String> data : entries)
        {
            assertTrue( "Data is wrong.", data.getValue().indexOf( "data:") != -1  );
        }
    }


}
