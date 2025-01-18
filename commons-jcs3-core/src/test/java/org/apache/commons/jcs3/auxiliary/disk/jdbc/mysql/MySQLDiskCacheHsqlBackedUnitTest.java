package org.apache.commons.jcs3.auxiliary.disk.jdbc.mysql;

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

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.HsqlSetupUtil;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs basic tests for the JDBC disk cache.
 */
public class MySQLDiskCacheHsqlBackedUnitTest
{
    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * <p>
     * @param region Name of the region to access
     * @param items
     * @throws Exception If an error occurs
     */
    public static void runTestForRegion( final String region, final int items )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        Thread.sleep( 1000 );

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
     * Test setup
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestMySQLDiskCache.ccf" );
        try (Connection con = HsqlSetupUtil.getTestDatabaseConnection(new Properties(), getClass().getSimpleName()))
        {
            HsqlSetupUtil.setupTable(con, "JCS_STORE_MYSQL");
        }
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * <p>
     * @throws Exception
     */
    @Test
    void testPutGetMatchingWithHSQL()
        throws Exception
    {
        // SETUP
        final int items = 200;
        final String region = "testCache2";
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // DO WORK
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }
        Thread.sleep( 1000 );

        final Map<String, ICacheElement<String, String>> matchingResults = jcs.getMatchingCacheElements( "1.8.+" );

        // VERIFY
        assertEquals( 10, matchingResults.size(), "Wrong number returned" );
//        System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * @throws Exception
     */
    @Test
    void testSimpleJDBCPutGetWithHSQL()
        throws Exception
    {
        runTestForRegion( "testCache1", 200 );
    }
}
