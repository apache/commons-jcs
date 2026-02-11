package org.apache.commons.jcs4.admin;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.junit.jupiter.api.Test;

/**
 * Test the admin bean that is used by the JCSAdmin.jsp
 */
class AdminBeanUnitTest
{

    /**
     * Add an item to a region. Call clear all and verify that it doesn't exist.
     *
     * @throws Exception
     */
    @Test
    void testClearAll()
        throws Exception
    {
        final JCSAdminBean admin = new JCSAdminBean();

        final String regionName = "myRegion";
        final CacheAccess<String, String> cache = JCS.getInstance( regionName );

        final String key = "myKey";
        cache.put( key, "value" );

        admin.clearAllRegions();

        final List<CacheElementInfo> elements2 = admin.buildElementInfo( regionName );
        assertEquals( 0, elements2.size(), "Wrong number of elements in the region after remove." );
    }

    /**
     * Put a value in a region and verify that it shows up.
     *
     * @throws Exception
     */
    @Test
    void testGetElementForRegionInfo()
        throws Exception
    {
        final String regionName = "myRegion";
        final CacheAccess<String, String> cache = JCS.getInstance( regionName );

        // clear the region
        cache.clear();

        final String key = "myKey";
        cache.put( key, "value" );

        final JCSAdminBean admin = new JCSAdminBean();

        final List<CacheElementInfo> elements = admin.buildElementInfo( regionName );
        assertEquals( 1, elements.size(), "Wrong number of elements in the region." );

        final CacheElementInfo elementInfo = elements.get(0);
        assertEquals( key, elementInfo.key(), "Wrong key." + elementInfo );
    }

    /**
     * Create a test region and then verify that we get it from the list.
     *
     * @throws Exception
     */
    @Test
    void testGetRegionInfo()
        throws Exception
    {
        final String regionName = "myRegion";
        final CacheAccess<String, String> cache = JCS.getInstance( regionName );

        cache.put( "key", "value" );

        final JCSAdminBean admin = new JCSAdminBean();

        final List<CacheRegionInfo> regions = admin.buildCacheInfo();

        boolean foundRegion = false;

        for (final CacheRegionInfo info : regions)
        {
            if ( info.cacheName().equals( regionName ) )
            {
                foundRegion = true;

                assertTrue( info.byteCount() > 5, "Byte count should be greater than 5." );
                assertNotNull( info.cacheStatistics(), "Should have stats." );
            }
        }

        assertTrue( foundRegion, "Should have found the region we just created." );
    }

    /**
     * Remove an item via the remove method.
     *
     * @throws Exception
     */
    @Test
    void testRemove()
        throws Exception
    {
        final JCSAdminBean admin = new JCSAdminBean();

        final String regionName = "myRegion";
        final CacheAccess<String, String> cache = JCS.getInstance( regionName );

        // clear the region
        cache.clear();
        admin.clearRegion( regionName );

        final String key = "myKey";
        cache.put( key, "value" );

        final List<CacheElementInfo> elements = admin.buildElementInfo( regionName );
        assertEquals( 1, elements.size(), "Wrong number of elements in the region." );

        final CacheElementInfo elementInfo = elements.get(0);
        assertEquals( key, elementInfo.key(), "Wrong key." );

        admin.removeItem( regionName, key );

        final List<CacheElementInfo> elements2 = admin.buildElementInfo( regionName );
        assertEquals( 0, elements2.size(), "Wrong number of elements in the region after remove." );
    }
}
