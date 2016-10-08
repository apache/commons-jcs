package org.apache.commons.jcs.auxiliary.disk.jdbc.mysql;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.auxiliary.disk.jdbc.HsqlSetupTableUtil;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs basic tests for the JDBC disk cache.
 * @author Aaron Smuts
 */
public class MySQLDiskCacheHsqlBackedUnitTest
{
    /**
     * Creates the DB
     * <p>
     * @throws Exception
     */
    @BeforeClass
    public static void setupDatabase() throws Exception
    {
        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        String url = "jdbc:hsqldb:";
        String database = rafroot + "/MySQLDiskCacheHsqlBackedUnitTest";
        String user = "sa";
        String password = "";

        new org.hsqldb.jdbcDriver();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        HsqlSetupTableUtil.setupTABLE( cConn, "JCS_STORE_MYSQL" );
    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestMySQLDiskCache.ccf" );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * @throws Exception
     */
    @Test
    public void testSimpleJDBCPutGetWithHSQL()
        throws Exception
    {
        runTestForRegion( "testCache1", 200 );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * <p>
     * @param region Name of the region to access
     * @param items
     * @throws Exception If an error occurs
     */
    public void runTestForRegion( String region, int items )
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( region );
        //System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        //System.out.println( jcs.getStats() );
        Thread.sleep( 1000 );
        //System.out.println( jcs.getStats() );

        // Test that all items are in cache
        for ( int i = 0; i < items; i++ )
        {
            String value = jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }

        // Test that getElements returns all the expected values
        Set<String> keys = new HashSet<String>();
        for ( int i = 0; i < items; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = 0; i < items; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", region + " data " + i, element.getVal() );
        }

        // Remove all the items
        for ( int i = 0; i < items; i++ )
        {
            jcs.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i < items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcs.get( i + ":key" ) );
        }
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * <p>
     * @throws Exception
     */
    @Test
    public void testPutGetMatchingWithHSQL()
        throws Exception
    {
        // SETUP
        int items = 200;
        String region = "testCache2";
        CacheAccess<String, String> jcs = JCS.getInstance( region );
//        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // DO WORK
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }
        Thread.sleep( 1000 );

        Map<String, ICacheElement<String, String>> matchingResults = jcs.getMatchingCacheElements( "1.8.+" );

        // VERIFY
        assertEquals( "Wrong number returned", 10, matchingResults.size() );
//        System.out.println( "matchingResults.keySet() " + matchingResults.keySet() );
//        System.out.println( "\nAFTER TEST \n" + jcs.getStats() );
    }
}
