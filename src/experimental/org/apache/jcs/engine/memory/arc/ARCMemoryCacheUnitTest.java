package org.apache.jcs.engine.memory.arc;

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

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Initial tests for the ARCMemoryCache
 * <p>
 * @author Aaron Smuts
 */
public class ARCMemoryCacheUnitTest
    extends TestCase
{
    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestARCCache.ccf" );
    }

    /**
     * Put a few items on, get them, and then remove them.
     * @throws CacheException
     */
    public void testPutGetRemoveThroughHub()
        throws CacheException
    {
        JCS cache = JCS.getInstance( "testPutGetThroughHub" );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max * 2;

        for ( int i = 0; i < items; i++ )
        {
            cache.put( i + ":key", "myregion" + " data " + i );
        }

        // Test that first items are not in the cache
        for ( int i = max; i >= 0; i-- )
        {
            String value = (String) cache.get( i + ":key" );
            assertNull( "Should not have value for key [" + i + ":key" + "] in the cache.", value );
        }

        // Test that last items are in cache
        // skip 2 for the buffer.
        for ( int i = max + 2; i < items; i++ )
        {
            String value = (String) cache.get( i + ":key" );
            assertEquals( "myregion" + " data " + i, value );
        }

        System.out.println( cache.getStats() );
    }

    /**
     * Put half the max, get the key array, and verify that it has the correct
     * number of items.
     * <p>
     * @throws Exception
     */
    public void testGetKeyArray()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max / 2;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        System.out.println( "testGetKeyArray " + arc.getStats() );

        Object[] keys = arc.getKeyArray();
        assertEquals( "Wrong number of keys.", items, keys.length );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );
    }

    /**
     * Put half the max and then get the first element.  It should now be in t2.
     * <p>
     * @throws Exception
     */
    public void testHitInT1BelowMax()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max / 2;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        ICacheElement element = arc.get( 0 + ":key" );

        System.out.println( "testHitInT1BelowMax " + arc.getStats() );

        assertNotNull( "Should have the element.", element );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );

        assertEquals( "T2 should have one item.", 1, arc.getListSize( ARCMemoryCache._T2_ ) );
    }

    /**
     * Put half the max and then get the first element. then get it again.  ti shoudl be in t2
     * <p>
     * @throws Exception
     */
    public void testHitInT1ThenT2BelowMax()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max / 2;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        ICacheElement element = arc.get( 0 + ":key" );

        System.out.println( "testHitInT1ThenT2BelowMax " + arc.getStats() );

        assertNotNull( "Should have the element.", element );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );

        assertEquals( "T2 should have one item.", 1, arc.getListSize( ARCMemoryCache._T2_ ) );
    }

    /**
     * Put half the max and then get the first element.  It should now be in t2.
     * <p>
     * @throws Exception
     */
    public void testHitInT1AtMax()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        ICacheElement element = arc.get( 0 + ":key" );

        System.out.println( "testHitInT1AtMax " + arc.getStats() );

        assertNotNull( "Should have the element.", element );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );

        assertEquals( "T2 should have one item.", 1, arc.getListSize( ARCMemoryCache._T2_ ) );
    }

    /**
     * Put half the max and then get the first element.  It should now be in t2.
     * <p>
     * @throws Exception
     */
    public void SKIPtestHitInT1OverMax()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max + 1;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        ICacheElement element = arc.get( 0 + ":key" );

        System.out.println( "testHitInT1OverMax " + arc.getStats() );

        assertNull( "Should not have the element since it was the first.", element );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );

        assertEquals( "T2 should have one item.", 1, arc.getListSize( ARCMemoryCache._T2_ ) );
    }

    /**
     * Put half the max and then get the first element.  It should now be in t2.
     * <p>
     * @throws Exception
     */
    public void testPutInT1ToMax()
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestARCCache.ccf" );
        CompositeCache cache = cacheMgr.getCache( "testGetKeyArray" );

        ARCMemoryCache arc = new ARCMemoryCache();
        arc.initialize( cache );

        int max = cache.getCacheAttributes().getMaxObjects();
        int items = max;

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement ice = new CacheElement( cache.getCacheName(), i + ":key", cache.getCacheName() + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            arc.update( ice );
        }

        //ICacheElement element = arc.get( 0 + ":key" );

        System.out.println( "testPutInT1ToMax " + arc.getStats() );

        //assertNotNull( "Should have the element.", element );
        assertEquals( "Target t1 should be 1/2 until dupe gets or puts.", max / 2, arc.getTarget_T1() );

        assertEquals( "Wrong number of items in T1.", max, arc.getListSize( ARCMemoryCache._T1_ ) );
        assertEquals( "Wrong number of items in T2.", 0, arc.getListSize( ARCMemoryCache._T2_ ) );
    }
}
