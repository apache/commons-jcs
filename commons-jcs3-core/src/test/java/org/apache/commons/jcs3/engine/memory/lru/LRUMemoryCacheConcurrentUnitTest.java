package org.apache.commons.jcs3.engine.memory.lru;

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

import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the LRUMemory cache. This one uses three different
 * regions for three threads.
 */
class LRUMemoryCacheConcurrentUnitTest
{

    // Number of items to cache, twice the configured maxObjects for the memory cache regions.
    private static final int items = 200;

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should be dumped.
     * @param region Name of the region to access
     * @throws Exception If an error occurs
     */
    public static void runTestForRegion( final String region )
        throws Exception
    {
        final CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestDiskCache.ccf" );
        final CompositeCache<String, String> cache = cacheMgr.getCache( region );

        final LRUMemoryCache<String, String> lru = new LRUMemoryCache<>();
        lru.initialize( cache );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            final ICacheElement<String, String> ice = new CacheElement<>( cache.getCacheName(), i + ":key",
                                                                          region + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            lru.update( ice );
        }

        // Test that initial items have been purged
        for ( int i = 0; i < 100; i++ )
        {
            assertNull( lru.get( i + ":key" ), "Should not have " + i + ":key" );
        }

        // Test that last items are in cache
        for ( int i = 100; i < items; i++ )
        {
            final String value = lru.get( i + ":key" ).getVal();
            assertEquals( region + " data " + i, value );
        }

        // Test that getMultiple returns all the items remaining in cache and none of the missing ones
        final Set<String> keys = new HashSet<>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        final Map<String, ICacheElement<String, String>> elements = lru.getMultiple( keys );
        for ( int i = 0; i < 100; i++ )
        {
            assertNull( elements.get( i + ":key" ), "Should not have " + i + ":key" );
        }
        for ( int i = 100; i < items; i++ )
        {
            final ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( element, "element " + i + ":key is missing" );
            assertEquals( region + " data " + i, element.getVal(), "value " + i + ":key" );
        }

        // Remove all the items
        for ( int i = 0; i < items; i++ )
        {
            lru.remove( i + ":key" );
        }

        // Verify removal
        for ( int i = 0; i < items; i++ )
        {
            assertNull( lru.get( i + ":key" ), "Removed key should be null: " + i + ":key" );
        }
    }

    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        // Any setup logic can go here, e.g., configuring caches
        // JCS.setConfigFilename("/TestDiskCache.ccf");
    }

    /**
     * Run test for region 'testRegion1'
     */
    @Test
    void testLRUMemoryCache()
        throws Exception
    {
        runTestForRegion( "testRegion1" );
    }
}
