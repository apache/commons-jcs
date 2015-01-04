package org.apache.commons.jcs.admin;

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

/**
 * Test the admin bean that is used by the JCSAdmin.jsp
 *
 * @author Aaron Smuts
 *
 */
public class AdminBeanUnitTest
    extends TestCase
{

    /**
     * Create a test region and then verify that we get it from the list.
     *
     * @throws Exception
     *
     */
    public void testGetRegionInfo()
        throws Exception
    {
        String regionName = "myRegion";
        CacheAccess<String, String> cache = JCS.getInstance( regionName );

        cache.put( "key", "value" );

        JCSAdminBean admin = new JCSAdminBean();

        CacheRegionInfo[] regions = admin.buildCacheInfo();

        boolean foundRegion = false;

        for (CacheRegionInfo info : regions)
        {

            if ( info.getCacheName().equals( regionName ) )
            {
                foundRegion = true;

                assertTrue( "Byte count should be greater than 5.", info.getByteCount() > 5 );

                assertNotNull( "Should have stats.", info.getCacheStatistics() );
            }
        }

        assertTrue( "Should have found the region we just created.", foundRegion );
    }

    /**
     * Put a value in a region and verify that it shows up.
     *
     * @throws Exception
     */
    public void testGetElementForRegionInfo()
        throws Exception
    {
        String regionName = "myRegion";
        CacheAccess<String, String> cache = JCS.getInstance( regionName );

        // clear the region
        cache.clear();

        String key = "myKey";
        cache.put( key, "value" );

        JCSAdminBean admin = new JCSAdminBean();

        CacheElementInfo[] elements = admin.buildElementInfo( regionName );

        assertEquals( "Wrong number of elements in the region.", 1, elements.length );

        CacheElementInfo elementInfo = elements[0];
        assertEquals( "Wrong key." + elementInfo, key, elementInfo.getKey() );
    }

    /**
     * Remove an item via the remove method.
     *
     * @throws Exception
     */
    public void testRemove()
        throws Exception
    {
        JCSAdminBean admin = new JCSAdminBean();

        String regionName = "myRegion";
        CacheAccess<String, String> cache = JCS.getInstance( regionName );

        // clear the region
        cache.clear();
        admin.clearRegion( regionName );

        String key = "myKey";
        cache.put( key, "value" );

        CacheElementInfo[] elements = admin.buildElementInfo( regionName );

        assertEquals( "Wrong number of elements in the region.", 1, elements.length );

        CacheElementInfo elementInfo = elements[0];

        assertEquals( "Wrong key.", key, elementInfo.getKey() );

        admin.removeItem( regionName, key );

        CacheElementInfo[] elements2 = admin.buildElementInfo( regionName );
        assertEquals( "Wrong number of elements in the region after remove.", 0, elements2.length );
    }

    /**
     * Add an item to a region. Call clear all and verify that it doesn't exist.
     *
     * @throws Exception
     */
    public void testClearAll()
        throws Exception
    {
        JCSAdminBean admin = new JCSAdminBean();

        String regionName = "myRegion";
        CacheAccess<String, String> cache = JCS.getInstance( regionName );

        String key = "myKey";
        cache.put( key, "value" );

        admin.clearAllRegions();

        CacheElementInfo[] elements2 = admin.buildElementInfo( regionName );
        assertEquals( "Wrong number of elements in the region after remove.", 0, elements2.length );
    }
}
