package org.apache.commons.jcs4.engine.control;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.engine.stats.behavior.ICacheStats;
import org.junit.jupiter.api.Test;

/**
 */
class CacheManagerStatsUnitTest
{

    /**
     * Just get the stats after putting a couple entries in the cache.
     *
     * @throws Exception
     */
    @Test
    void testSimpleGetStats()
        throws Exception
    {
        final CacheAccess<String, String> cache = JCS.getInstance( "testCache1" );

        // 1 miss, 1 hit, 1 put
        cache.get( "testKey" );
        cache.put( "testKey", "testdata" );
        // should have 4 hits
        cache.get( "testKey" );
        cache.get( "testKey" );
        cache.get( "testKey" );
        cache.get( "testKey" );

        final CompositeCacheManager mgr = CompositeCacheManager.getInstance();
        final String statsString = mgr.getStats();

//        System.out.println( statsString );

        assertTrue( statsString.indexOf( "testCache1" ) != -1, "Should have the cacheName in here." );
        assertTrue( statsString.indexOf( "HitCountRam" ) != -1, "Should have the HitCountRam in here." );
        assertTrue( statsString.indexOf( "4" ) != -1, "Should have the 4 in here." );

        final ICacheStats[] stats = mgr.getStatistics();
        final int statsLen = stats.length;
//        System.out.println( "statsLen = " + statsLen );
        for ( int i = 0; i < statsLen; i++ )
        {
            // TODO finish
        }
    }

}
