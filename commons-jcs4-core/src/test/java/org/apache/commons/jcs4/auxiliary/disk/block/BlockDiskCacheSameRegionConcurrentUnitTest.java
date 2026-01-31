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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the block disk cache. Runs four threads against the same region.
 */
public class BlockDiskCacheSameRegionConcurrentUnitTest
{

    @BeforeEach
    void setUp()
    {
        // Set the configuration file
        JCS.setConfigFilename( "/TestBlockDiskCacheCon.ccf" );
    }

    /**
     * Helper method to test cache operations within a specific range in the region.
     */
    public static void runTestForRegion( final String region, final int start, final int end )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = start; i < end; i++ )
        {
            jcs.put( i + ":key", region + " data " + i + "-" + region );
        }

        // Test that all items are in cache
        for ( int i = start; i < end; i++ )
        {
            final String key = i + ":key";
            final String value = jcs.get( key );

            assertEquals( region + " data " + i + "-" + region, value, "Wrong value for key [" + key + "]" );
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
            assertEquals( region + " data " + i + "-" + region, element.getVal(), "value " + i + ":key" );
        }
    }

    @Test
    void testBlockDiskCache1()
        throws Exception
    {
        runTestForRegion( "blockRegion4", 0, 200 );
    }

    @Test
    void testBlockDiskCache2()
        throws Exception
    {
        runTestForRegion( "blockRegion4", 1000, 1200 );
    }

    @Test
    void testBlockDiskCache3()
        throws Exception
    {
        runTestForRegion( "blockRegion4", 2000, 2200 );
    }

    @Test
    void testBlockDiskCache4()
        throws Exception
    {
        runTestForRegion( "blockRegion4", 2200, 5200 );
    }
}
