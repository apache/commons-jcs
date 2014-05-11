package org.apache.commons.jcs.engine.memory.lru;

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

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test which exercises the LRUMemory cache. This one uses three different
 * regions for three threads.
 */
public class LHMLRUMemoryCacheConcurrentUnitTest
    extends TestCase
{
    /**
     * Number of items to cache, twice the configured maxObjects for the memory
     * cache regions.
     */
    private static int items = 200;

    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public LHMLRUMemoryCacheConcurrentUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     *
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { LHMLRUMemoryCacheConcurrentUnitTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     * <p>
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new LHMLRUMemoryCacheConcurrentUnitTest( "testLHMLRUMemoryCache" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion1" );
            }
        } );

        return suite;
    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        //JCS.setConfigFilename( "/TestLHMLRUCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should be dumped.
     * <p>
     * @param region
     *            Name of the region to access
     *
     * @throws Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region )
        throws Exception
    {
        CompositeCacheManager cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestLHMLRUCache.ccf" );
        CompositeCache<String, String> cache = cacheMgr.getCache( region );

        LRUMemoryCache<String, String> lru = new LRUMemoryCache<String, String>();
        lru.initialize( cache );

        // Add items to cache

        for ( int i = 0; i < items; i++ )
        {
            ICacheElement<String, String> ice = new CacheElement<String, String>( cache.getCacheName(), i + ":key", region + " data " + i );
            ice.setElementAttributes( cache.getElementAttributes() );
            lru.update( ice );
        }

        // Test that initial items have been purged
        for ( int i = 0; i < 100; i++ )
        {
            assertNull( "Should not have " + i + ":key", lru.get( i + ":key" ) );
        }

        // Test that last items are in cache
        for ( int i = 100; i < items; i++ )
        {
            String value = lru.get( i + ":key" ).getVal();
            assertEquals( region + " data " + i, value );
        }

        // Test that getMultiple returns all the items remaining in cache and none of the missing ones
        Set<String> keys = new HashSet<String>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = lru.getMultiple( keys );
        for ( int i = 0; i < 100; i++ )
        {
            assertNull( "Should not have " + i + ":key", elements.get( i + ":key" ) );
        }
        for ( int i = 100; i < items; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", region + " data " + i, element.getVal() );
        }

        // Remove all the items

        for ( int i = 0; i < items; i++ )
        {
            lru.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i < items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", lru.get( i + ":key" ) );
        }
    }

}
