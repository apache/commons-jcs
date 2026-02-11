package org.apache.commons.jcs4.engine.memory.lru;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.access.exception.CacheException;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.CompositeCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the test LHMLRU implementation.
 */
class LHMLRUMemoryCacheUnitTest
{
    /** Test setup */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestLHMLRUCache.ccf" );
    }

    /**
     * put the max and clear. verify that no elements remain.
     *
     * @throws CacheException
     */
    @Test
    void testClearThroughHub()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testClearThroughHub" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        cache.clear();

        // Test that first items are not in the cache
        for ( int i = max; i >= 0; i-- )
        {
            final String value = cache.get( i + ":key" );
            assertNull( value, "Should not have value for key [" + i + ":key] in the cache." );
        }
    }

    /**
     * Put half the max and clear. get the key array and verify that it has the correct number of
     * items.
     *
     * @throws Exception
     */
    @Test
    void testGetKeyArray()
        throws Exception
    {
        final CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestLHMLRUCache.ccf" );
        final CompositeCache<String, String> cache = cacheMgr.getCache( "testGetKeyArray" );

        final LHMLRUMemoryCache<String, String> mru = new LHMLRUMemoryCache<>();
        mru.initialize( cache );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max / 2;

        for ( int i = 0; i < items; i++ )
        {
            final ICacheElement<String, String> ice = new CacheElement<>( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            mru.update( ice );
        }

        final Set<String> keys = mru.getKeySet();

        assertEquals( items, keys.size(), "Wrong number of keys." );
    }

    /**
     * Gets stats.
     *
     * @throws CacheException
     */
    @Test
    void testGetStatsThroughHub()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testGetStatsThroughHub" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        final String stats = cache.getStatistics().toString();

        //System.out.println( stats );

        // TODO improve stats check
        assertTrue( stats.indexOf( "200" ) != -1, "Should have 200 puts" + stats );
    }

    /**
     * Verify that the mru gets used by a non-defined region when it is set as the default in the
     * default region.
     *
     * @throws CacheException
     */
    @Test
    void testLoadFromCCF()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testLoadFromCCF" );
        final String memoryCacheName = cache.getCacheAttributes().memoryCacheName();
        assertTrue( memoryCacheName.indexOf( "LHMLRUMemoryCache" ) != -1, "Cache name should have LHMLRU in it." );
    }

    /**
     * put twice as many as the max.  verify that the second half is in the cache.
     *
     * @throws CacheException
     */
    @Test
    void testPutGetThroughHub()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHub" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        // Test that first items are not in the cache
        for ( int i = max -1; i >= 0; i-- )
        {
            final String value = cache.get( i + ":key" );
            assertNull( value, "Should not have value for key [" + i + ":key] in the cache." + cache.getStatistics() );
        }

        // Test that last items are in cache
        // skip 2 for the buffer.
        for ( int i = max + 2; i < items; i++ )
        {
            final String value = cache.get( i + ":key" );
            assertEquals( "myregion data " + i, value );
        }

        // Test that getMultiple returns all the items remaining in cache and none of the missing ones
        final Set<String> keys = new HashSet<>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        final Map<String, ICacheElement<String, String>> elements = cache.getCacheElements( keys );
        for ( int i = max-1; i >= 0; i-- )
        {
            assertNull( elements.get( i + ":key" ),
                        "Should not have value for key [" + i + ":key] in the cache." + cache.getStatistics() );
        }
        for ( int i = max + 2; i < items; i++ )
        {
            final ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( element, "element " + i + ":key is missing" );
            assertEquals( "myregion data " + i, element.getVal(), "value " + i + ":key" );
        }
    }

    /**
     * Put twice as many as the max, twice. verify that the second half is in the cache.
     *
     * @throws CacheException
     */
    @Test
    void testPutGetThroughHubTwice()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHubTwice" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        // Test that first items are not in the cache
        for ( int i = max -1; i >= 0; i-- )
        {
            final String value = cache.get( i + ":key" );
            assertNull( value, "Should not have value for key [" + i + ":key] in the cache." );
        }

        // Test that last items are in cache
        // skip 2 for the buffer.
        for ( int i = max + 2; i < items; i++ )
        {
            final String value = cache.get( i + ":key" );
            assertEquals( "myregion data " + i, value );
        }

    }

    /**
     * put the max and remove each. verify that they are all null.
     *
     * @throws CacheException
     */
    @Test
    void testPutRemoveThroughHub()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testPutRemoveThroughHub" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion data " + i );
        }

        for ( int i = 0; i < items; i++ )
        {
            cache.remove( i + ":key" );
        }

        // Test that first items are not in the cache
        for ( int i = max; i >= 0; i-- )
        {
            final String value = cache.get( i + ":key" );
            assertNull( value, "Should not have value for key [" + i + ":key] in the cache." );
        }
    }

    /**
     * Add a few keys with the delimiter. Remove them.
     *
     * @throws CacheException
     */
    @Test
    void testRemovePartialThroughHub()
        throws CacheException
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testRemovePartialThroughHub" );

        final int max = cache.getCacheAttributes().maxObjects();
        final int items = max / 2;

        cache.put( "test", "data" );

        final String root = "myroot";

        for ( int i = 0; i < items; i++ )
        {
            cache.put( root + ":" + i + ":key", "myregion data " + i );
        }

        // Test that last items are in cache
        for ( int i = 0; i < items; i++ )
        {
            final String value = cache.get( root + ":" + i + ":key" );
            assertEquals( "myregion data " + i, value );
        }

        // remove partial
        cache.remove( root + ":" );

        for ( int i = 0; i < items; i++ )
        {
            assertNull( cache.get( root + ":" + i + ":key" ), "Should have been removed by partial loop." );
        }

        assertNotNull( cache.get( "test" ), "Other item should be in the cache." );
    }
}
