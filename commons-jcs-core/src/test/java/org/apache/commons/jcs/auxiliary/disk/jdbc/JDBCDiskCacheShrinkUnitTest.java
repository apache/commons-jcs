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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.utils.timing.SleepUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs basic tests for the JDBC disk cache.
 * <p>
 * @author Aaron Smuts
 */
public class JDBCDiskCacheShrinkUnitTest
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
        Properties p = new Properties();
        String driver = p.getProperty( "driver", "org.hsqldb.jdbcDriver" );
        String url = p.getProperty( "url", "jdbc:hsqldb:" );
        String database = p.getProperty( "database", rafroot + "/JDBCDiskCacheShrinkUnitTest" );
        String user = p.getProperty( "user", "sa" );
        String password = p.getProperty( "password", "" );

        new org.hsqldb.jdbcDriver();
        Class.forName( driver ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, user, password );

        HsqlSetupTableUtil.setupTABLE( cConn, "JCS_STORE_SHRINK" );
    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestJDBCDiskCacheShrink.ccf" );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing. Verify that items
     * configured to expire after 1 second actually expire.
     * <p>
     * @throws Exception
     */
    @Test
    public void testExpireInBackground()
        throws Exception
    {
        String regionExpire = "expire1Second";
        int items = 200;

        CacheAccess<String, String> jcsExpire = JCS.getInstance( regionExpire );

//        System.out.println( "BEFORE PUT \n" + jcsExpire.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcsExpire.put( i + ":key", regionExpire + " data " + i );
        }

//        System.out.println( jcsExpire.getStats() );

        // the shrinker is supposed to run every second
        SleepUtil.sleepAtLeast( 3000 );

//        System.out.println( jcsExpire.getStats() );

        // Test that all items have been removed from the cache
        for ( int i = 0; i <= items; i++ )
        {
            assertNull( "Removed key should be null: " + i + ":key", jcsExpire.get( i + ":key" ) );
        }
    }

    /**
     * Verify that those not scheduled to expire do not expire.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    public void testDidNotExpire()
        throws CacheException, InterruptedException
    {
        String region = "expire100Second";
        int items = 200;

        CacheAccess<String, String> jcs = JCS.getInstance( region );

//        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

//        System.out.println( jcs.getStats() );

        SleepUtil.sleepAtLeast( 1000 );

//        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = jcs.get( i + ":key" );

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
     * Verify that eternal trumps max life.
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    public void testDidNotExpireEternal()
        throws CacheException, InterruptedException
    {
        String region = "eternal";
        int items = 200;

        CacheAccess<String, String> jcs = JCS.getInstance( region );

//        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

//        System.out.println( jcs.getStats() );

        SleepUtil.sleepAtLeast( 1000 );

//        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = jcs.get( i + ":key" );

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
}
