package org.apache.commons.jcs4.auxiliary.disk.block;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the block disk cache. This one uses three different
 * regions for three threads.
 */
public class BlockDiskCacheConcurrentUnitTest
{

    /**
     * Number of items to cache, twice the configured maxObjects for the memory
     * cache regions.
     */
    private static final int items = 200;

    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestBlockDiskCache.ccf" );
        JCS.getInstance( "indexedRegion1" ).clear();
        JCS.getInstance( "indexedRegion2" ).clear();
        JCS.getInstance( "indexedRegion3" ).clear();
    }

    /**
     * Common method to test cache operations.
     */
    public static void runTestForRegion( final String region )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );
            assertEquals( region + " data " + i, value );
        }

        // Test that getElements returns all the expected values
        final Set<String> keys = new HashSet<>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        final Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = 0; i < items; i++ )
        {
            final ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( element, "element " + i + ":key is missing" );
            assertEquals( region + " data " + i, element.getVal(), "value " + i + ":key" );
        }

        // Remove all the items
        for ( int i = 0; i < items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal
        for ( int i = 0; i < items; i++ )
        {
            assertNull( jcs.get( i + ":key" ), "Removed key should be null: " + i + ":key\n stats " + jcs.getStats() );
        }
    }

    /**
     * Overloaded method to test cache operations in a range.
     */
    public static void runTestForRegionInRange( final String region, final int start, final int end )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = start; i < end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = start; i < end; i++ )
        {
            final String value = jcs.get( i + ":key" );
            assertEquals( region + " data " + i, value );
        }

        // Test that getElements returns all the expected values
        final Set<String> keys = new HashSet<>();
        for ( int i = start; i < end; i++ )
        {
            keys.add( i + ":key" );
        }

        final Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = start; i < end; i++ )
        {
            final ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( element, "element " + i + ":key is missing" );
            assertEquals( region + " data " + i, element.getVal(), "value " + i + ":key" );
        }

        // Remove all the items
        for ( int i = start; i < end; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal
        for ( int i = start; i < end; i++ )
        {
            assertNull( jcs.get( i + ":key" ), "Removed key should be null: " + i + ":key\n stats " + jcs.getStats() );
        }
    }

    /**
     * Test methods for different regions.
     */
    @Test
    void testBlockDiskCache1()
        throws Exception
    {
        runTestForRegion( "indexedRegion1" );
    }

    @Test
    void testBlockDiskCache2()
        throws Exception
    {
        runTestForRegion( "indexedRegion2" );
    }

    @Test
    void testBlockDiskCache3()
        throws Exception
    {
        runTestForRegion( "indexedRegion3" );
    }

    @Test
    void testBlockDiskCache4()
        throws Exception
    {
        runTestForRegionInRange( "indexedRegion3", 300, 600 );
    }
}
