package org.apache.commons.jcs4;

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

import org.apache.commons.jcs4.access.CacheAccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the indexed disk cache. This one uses three different
 * regions for three threads.
 */
public class TestTCPLateralCache
{

    /**
     * Number of items to cache, twice the configured maxObjects for the memory
     * cache regions.
     */
    private static final int items = 200;

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     *
     * @throws Exception
     *                If an error occurs
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
            Assertions.assertEquals( region + " data " + i, value,
                                     "Cache did not return expected value for key: " + i + ":key" );
        }

        // Remove all the items
        for ( int i = 0; i < items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal
        for ( int i = 0; i < items; i++ )
        {
            Assertions.assertNull( jcs.get( i + ":key" ), "Removed key should be null: " + i + ":key" );
        }
    }

    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCache.ccf" );
    }

    /**
     * Test for TCP region without a receiver.
     */
    @Test
    void testTcpRegion1_noReceiver()
        throws Exception
    {
        runTestForRegion( "testTcpRegion1" );
    }

    // Uncomment the following tests if more regions are needed.
    /*
    @Test
    void testIndexedDiskCache2() throws Exception {
        runTestForRegion("indexedRegion2");
    }

    @Test
    void testIndexedDiskCache3() throws Exception {
        runTestForRegion("indexedRegion3");
    }
    */
}
