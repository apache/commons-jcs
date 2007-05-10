package org.apache.jcs.auxiliary.disk.jdbc;

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
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.JCS;

/**
 * Runs basic tests for the JDBC disk cache.
 *
 * @author Aaron Smuts
 *
 */
public class JDBCDiskCacheUnitTest
    extends TestCase
{

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestJDBCDiskCache.ccf" );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     *
     * @throws Exception
     */
    public void testSimpleJDBCPutGetWithHSQL()
        throws Exception
    {
        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        Properties p = new Properties();
        String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
        String url = p.getProperty( "url", "jdbc:hsqldb:" );
        String database = p.getProperty( "database", rafroot + "/cache_hsql_db" );
        String user = p.getProperty( "user", "sa" );
        String password = p.getProperty( "password", "" );

        new org.hsqldb.jdbcDriver();
        Class.forName( driver ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        setupTABLE( cConn );

        runTestForRegion( "testCache1", 200 );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     * @param items
     *
     * @exception Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region, int items )
        throws Exception
    {
        JCS jcs = JCS.getInstance( region );

        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        System.out.println( jcs.getStats() );

        Thread.sleep( 1000 );

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
     * SETUP TABLE FOR CACHE
     *
     * @param cConn
     */
    void setupTABLE( Connection cConn )
    {
        boolean newT = true;

        StringBuffer createSql = new StringBuffer();
        createSql.append( "CREATE CACHED TABLE JCS_STORE2 " );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           DATE, " );
        createSql.append( "CREATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
        createSql.append( "SYSTEM_EXPIRE_TIME_SECONDS      BIGINT, " );
        createSql.append( "IS_ETERNAL            CHAR(1), " );
        createSql.append( "PRIMARY KEY (CACHE_KEY, REGION) " );
        createSql.append( ");" );

        Statement sStatement = null;
        try
        {
            sStatement = cConn.createStatement();
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

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
                e.printStackTrace();
            }
        }

        String setupData[] = { "create index iKEY on JCS_STORE2 (CACHE_KEY, REGION)" };

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
