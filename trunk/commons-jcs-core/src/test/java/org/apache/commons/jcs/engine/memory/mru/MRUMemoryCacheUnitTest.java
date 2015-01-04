package org.apache.commons.jcs.engine.memory.mru;

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

import junit.framework.TestCase;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for the test MRU implementation.
 * <p>
 * @author Aaron Smuts
 */
public class MRUMemoryCacheUnitTest
    extends TestCase
{
    /** Test setup */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestMRUCache.ccf" );
    }

    /**
     * Verify that the mru gets used by a non-defined region when it is set as the default in the
     * default region.
     * <p>
     * @throws CacheException
     */
    public void testLoadFromCCF()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testPutGet" );
        String memoryCacheName = cache.getCacheAttributes().getMemoryCacheName();
        assertTrue( "Cache name should have MRU in it.", memoryCacheName.indexOf( "MRUMemoryCache" ) != -1 );
    }

    /**
     * put twice as many as the max.  verify that the second half is in the cache.
     * <p>
     * @throws CacheException
     */
    public void testPutGetThroughHub()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        // Test that first items are not in the cache
        for ( int i = max -1; i >= 0; i-- )
        {
            String value = cache.get( i + ":key" );
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache." + cache.getStats(), value );
        }

        // Test that last items are in cache
        // skip 2 for the buffer.
        for ( int i = max + 2; i < items; i++ )
        {
            String value = cache.get( i + ":key" );
            assertEquals( "myregion" + " data " + i, value );
        }

        // Test that getMultiple returns all the items remaining in cache and none of the missing ones
        Set<String> keys = new HashSet<String>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = cache.getCacheElements( keys );
        for ( int i = max-1; i >= 0; i-- )
        {
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache." + cache.getStats(), elements.get( i + ":key" ) );
        }
        for ( int i = max + 2; i < items; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", "myregion" + " data " + i, element.getVal() );
        }
    }

    /**
     * Put twice as many as the max, twice. verify that the second half is in the cache.
     * <p>
     * @throws CacheException
     */
    public void testPutGetThroughHubTwice()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        // Test that first items are not in the cache
        for ( int i = max-1; i >= 0; i-- )
        {
            String value = cache.get( i + ":key" );
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache.", value );
        }

        // Test that last items are in cache
        // skip 2 for the buffer.
        for ( int i = max + 2; i < items; i++ )
        {
            String value = cache.get( i + ":key" );
            assertEquals( "myregion" + " data " + i, value );
        }

    }

    /**
     * put the max and remove each. verify that they are all null.
     * <p>
     * @throws CacheException
     */
    public void testPutRemoveThroughHub()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        for ( int i = 0; i < items; i++ )
        {
            cache.remove( i + ":key" );
        }

        // Test that first items are not in the cache
        for ( int i = max; i >= 0; i-- )
        {
            String value = cache.get( i + ":key" );
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache.", value );
        }
    }

    /**
     * put the max and clear. verify that no elements remain.
     * <p>
     * @throws CacheException
     */
    public void testClearThroughHub()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testPutGetThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        cache.clear();

        // Test that first items are not in the cache
        for ( int i = max; i >= 0; i-- )
        {
            String value = cache.get( i + ":key" );
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache.", value );
        }
    }

    /**
     * Get stats.
     * <p>
     * @throws CacheException
     */
    public void testGetStatsThroughHub()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testGetStatsThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        String stats = cache.getStats();

//        System.out.println( stats );

        // TODO improve stats check
        assertTrue( "Should have 200 puts", stats.indexOf( "2000" ) != -1 );
    }

    /**
     * Put half the max and clear. get the key array and verify that it has the correct number of
     * items.
     * <p>
     * @throws Exception
     */
    public void testGetKeyArray()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestMRUCache.ccf" );
        CompositeCache<String, String> cache = cacheMgr.getCache( "testGetKeyArray" );

        MRUMemoryCache<String, String> mru = new MRUMemoryCache<String, String>();
        mru.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max / 2;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement<String, String> ice = new CacheElement<String, String>( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            mru.update( ice );
        }

        Set<String> keys = mru.getKeySet();

        assertEquals( "Wrong number of keys.", items, keys.size() );
    }

    /**
     * Add a few keys with the delimiter. Remove them.
     * <p>
     * @throws CacheException
     */
    public void testRemovePartialThroughHub()
        throws CacheException
    {
        CacheAccess<String, String> cache = JCS.getInstance( "testGetStatsThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max / 2;

        cache.put( "test", "data" );

        String root = "myroot";

        for ( int i = 0; i < items; i++ )
        {
            cache.put( root + ":" + i + ":key", "myregion" + " data " + i );
        }

        // Test that last items are in cache
        for ( int i = 0; i < items; i++ )
        {
            String value = cache.get( root + ":" + i + ":key" );
            assertEquals( "myregion" + " data " + i, value );
        }

        // remove partial
        cache.remove( root + ":" );

        for ( int i = 0; i < items; i++ )
        {
            assertNull( "Should have been removed by partial loop.", cache.get( root + ":" + i + ":key" ) );
        }

        assertNotNull( "Other item should be in the cache.", cache.get( "test" ) );
    }
}
