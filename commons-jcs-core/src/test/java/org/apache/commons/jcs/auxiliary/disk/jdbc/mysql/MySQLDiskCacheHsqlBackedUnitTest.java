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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

/**
 * Runs basic tests for the JDBC disk cache.
 * @author Aaron Smuts
 */
public class MySQLDiskCacheHsqlBackedUnitTest
    extends TestCase
{
    /**
     * Creates the DB
     * <p>
     * @throws Exception
     */
    public MySQLDiskCacheHsqlBackedUnitTest()
        throws Exception
    {
        super();
        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        String url = "jdbc:hsqldb:";
        String database = rafroot + "/MySQLDiskCacheHsqlBackedUnitTest";
        String user = "sa";
        String password = "";

        new org.hsqldb.jdbcDriver();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        setupTABLE( cConn );
    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestMySQLDiskCache.ccf" );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * @throws Exception
     */
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

    /**
     * SETUP TABLE FOR CACHE
     * @param cConn
     */
    void setupTABLE( Connection cConn ) throws SQLException
    {
        boolean newT = true;

        StringBuilder createSql = new StringBuilder();
        createSql.append( "CREATE CACHED TABLE JCS_STORE_MYSQL " );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           TIMESTAMP, " );
        createSql.append( "UPDATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
        createSql.append( "SYSTEM_EXPIRE_TIME_SECONDS      BIGINT, " );
        createSql.append( "IS_ETERNAL            CHAR(1), " );
        createSql.append( "PRIMARY KEY (CACHE_KEY, REGION) " );
        createSql.append( ");" );

        Statement sStatement = cConn.createStatement();

        try
        {
            sStatement.executeQuery( createSql.toString() );
            sStatement.close();
        }
        catch ( SQLException e )
        {
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            else
            {
                // TODO figure out if it exists prior to trying to create it.
                // log.error( "Problem creating table.", e );
                throw e;
            }
        }

        String setupData[] = { "create index iKEY on JCS_STORE_MYSQL (CACHE_KEY, REGION)" };

        if ( newT )
        {
            for ( int i = 1; i < setupData.length; i++ )
            {
                try
                {
                    sStatement.executeQuery( setupData[i] );
                }
                catch ( SQLException e )
                {
                    System.out.println( "Exception: " + e );
                }
            }
        } // end ifnew
    }
}
