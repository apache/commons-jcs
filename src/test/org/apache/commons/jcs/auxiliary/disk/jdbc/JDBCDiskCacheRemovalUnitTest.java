package org.apache.commons.jcs.auxiliary.disk.jdbc;

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

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;

/** Tests for the removal functionality. */
public class JDBCDiskCacheRemovalUnitTest
    extends TestCase
{
    /** db name -- set in system props */
    private final String databaseName = "JCS_STORE_REMOVAL";

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        System.setProperty( "DATABASE_NAME", databaseName );
        JCS.setConfigFilename( "/TestJDBCDiskCacheRemoval.ccf" );
    }

    /**
     * Verify the fix for BUG JCS-20
     * <p>
     * Setup an hsql db. Add an item. Remove using partial key.
     * @throws Exception
     */
    public void testPartialKeyRemoval_Good()
        throws Exception
    {
        // SETUP
        setupDatabase();

        String keyPart1 = "part1";
        String keyPart2 = "part2";
        String region = "testCache1";
        String data = "adfadsfasfddsafasasd";

        CacheAccess<String, String> jcs = JCS.getInstance( region );

        // DO WORK
        jcs.put( keyPart1 + ":" + keyPart2, data );
        Thread.sleep( 1000 );

        // VERIFY
        String resultBeforeRemove = jcs.get( keyPart1 + ":" + keyPart2 );
        assertEquals( "Wrong result", data, resultBeforeRemove );

        jcs.remove( keyPart1 + ":" );
        String resultAfterRemove = jcs.get( keyPart1 + ":" + keyPart2 );
        assertNull( "Should not have a result after removal.", resultAfterRemove );

        System.out.println( jcs.getStats() );
    }

    /**
     * Create the database.
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void setupDatabase()
        throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
    {
        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        Properties p = new Properties();
        String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
        String url = p.getProperty( "url", "jdbc:hsqldb:" );
        String database = p.getProperty( "database", rafroot + "/JDBCDiskCacheRemovalUnitTest" );
        String user = p.getProperty( "user", "sa" );
        String password = p.getProperty( "password", "" );

        new org.hsqldb.jdbcDriver();
        Class.forName( driver ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        setupTABLE( cConn );
    }

    /**
     * SETUP TABLE FOR CACHE
     * <p>
     * @param cConn
     *
     * @throws SQLException if database problems occur
     */
    private void setupTABLE( Connection cConn ) throws SQLException
    {
        boolean newT = true;

        StringBuffer createSql = new StringBuffer();
        createSql.append( "CREATE CACHED TABLE " + databaseName );
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
        }
        catch ( SQLException e )
        {
            // FIXME: This is unreliable
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            sStatement.close();
        }

        String setupData[] = { "create index iKEY on " + databaseName + " (CACHE_KEY, REGION)" };

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
