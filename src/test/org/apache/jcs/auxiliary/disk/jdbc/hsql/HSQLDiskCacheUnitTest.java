package org.apache.jcs.auxiliary.disk.jdbc.hsql;

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

/**
 * Test which exercises the indexed disk cache. This one uses three different regions for thre
 * threads.
 */
public class HSQLDiskCacheUnitTest
    extends TestCase
{
    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestHSQLDiskCache.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * <p>
     * @param region Name of the region to access
     * @exception Exception If an error occurs
     */
    public void testBasicPutRemove()
        throws Exception
    {
        int items = 20;

        String region = "testBasicPutRemove";

        JCS jcs = JCS.getInstance( region );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        //SleepUtil.sleepAtLeast( 1000 );

        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }

        // Remove all the items

        for ( int i = 0; i <= items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcs.get( i + ":key" ) );
        }
    }

    /**
     * Verify that remove all work son a region where it is not prohibited.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    public void testRemoveAll()
        throws CacheException, InterruptedException
    {
        String region = "removeAllAllowed";
        JCS jcs = JCS.getInstance( region );

        int items = 20;

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // a db thread could be updating when we call remove all?
        // there was a race on remove all, an element may be put to disk after it is called even
        // though the put
        // was called before clear.
        // I discovered it and removed it.
        // Thread.sleep( 500 );

        System.out.println( jcs.getStats() );

        jcs.clear();

        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertNull( "value should be null key = [" + i + ":key] value = [" + value + "]", value );
        }
    }

    /**
     * Verify that remove all does not work on a region where it is prohibited.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    public void testRemoveAllProhibition()
        throws CacheException, InterruptedException
    {
        String region = "noRemoveAll";
        JCS jcs = JCS.getInstance( region );

        int items = 20;

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // a db thread could be updating the disk when
        // Thread.sleep( 500 );

        System.out.println( jcs.getStats() );

        jcs.clear();

        for ( int i = 0; i <= items; i++ )
        {
            String value = (String) jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }
    }
}
