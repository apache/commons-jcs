package org.apache.commons.jcs3.auxiliary.disk.jdbc.hsql;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the HSQL cache.
 */
class HSQLDiskCacheUnitTest
{
    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestHSQLDiskCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * <p>
     * @throws Exception If an error occurs
     */
    @Test
    void testBasicPutRemove()
        throws Exception
    {
        final int items = 20;

        final String region = "testBasicPutRemove";

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
            assertEquals( region + " data " + i, value, "key = [" + i + ":key] value = [" + value + "]" );
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
            assertNull( jcs.get( i + ":key" ), "Removed key should be null: " + i + ":key" );
        }
    }

    /**
     * Verify that remove all work son a region where it is not prohibited.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    void testRemoveAll()
        throws CacheException, InterruptedException
    {
        final String region = "removeAllAllowed";
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        final int items = 20;

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // a db thread could be updating when we call remove all?
        // there was a race on remove all, an element may be put to disk after it is called even
        // though the put
        // was called before clear.
        // I discovered it and removed it.
        // Thread.sleep( 500 );

//        System.out.println( jcs.getStats() );

        jcs.clear();

        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );
            assertNull( value, "value should be null key = [" + i + ":key] value = [" + value + "]" );
        }
    }

    /**
     * Verify that remove all does not work on a region where it is prohibited.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    void testRemoveAllProhibition()
        throws CacheException, InterruptedException
    {
        final String region = "noRemoveAll";
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        final int items = 20;

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // a db thread could be updating the disk when
        // Thread.sleep( 500 );

        jcs.clear();

        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );
            assertEquals( region + " data " + i, value, "key = [" + i + ":key] value = [" + value + "]" );
        }
    }
}
