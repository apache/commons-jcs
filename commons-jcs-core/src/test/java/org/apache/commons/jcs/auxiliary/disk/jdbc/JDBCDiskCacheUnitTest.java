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
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.utils.serialization.StandardSerializer;
import org.apache.commons.jcs.utils.threadpool.DaemonThreadFactory;

/**
 * Runs basic tests for the JDBC disk cache.
 *<p>
 * @author Aaron Smuts
 */
public class JDBCDiskCacheUnitTest
    extends TestCase
{
    /** Test setup */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestJDBCDiskCache.ccf" );
    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
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

        HsqlSetupTableUtil.setupTABLE( cConn, "JCS_STORE2" );

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

//        System.out.println( "BEFORE PUT \n" + jcs.getStats() );

        // Add items to cache

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

//        System.out.println( jcs.getStats() );

        Thread.sleep( 1000 );

//        System.out.println( jcs.getStats() );

        // Test that all items are in cache

        for ( int i = 0; i <= items; i++ )
        {
            String value = jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
        }

        // Test that getElements returns all the expected values
        Set<String> keys = new HashSet<String>();
        for ( int i = 0; i <= items; i++ )
        {
            keys.add( i + ":key" );
        }

        Map<String, ICacheElement<String, String>> elements = jcs.getCacheElements( keys );
        for ( int i = 0; i <= items; i++ )
        {
            ICacheElement<String, String> element = elements.get( i + ":key" );
            assertNotNull( "element " + i + ":key is missing", element );
            assertEquals( "value " + i + ":key", region + " data " + i, element.getVal() );
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
     * Verfiy that it uses the pool access manager config.
     * <p>
     * @throws Exception
     */
    public void testInitializePoolAccess_withPoolName()
        throws Exception
    {
        // SETUP
        String poolName = "testInitializePoolAccess_withPoolName";

        String url = "jdbc:hsqldb:";
        String userName = "sa";
        String password = "";
        int maxActive = 10;
        String driverClassName = "org.hsqldb.jdbcDriver";

        Properties props = new Properties();
        String prefix = JDBCDiskCacheFactory.POOL_CONFIGURATION_PREFIX
    		+ poolName
            + JDBCDiskCacheFactory.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", url );
        props.put( prefix + ".userName", userName );
        props.put( prefix + ".password", password );
        props.put( prefix + ".maxActive", String.valueOf( maxActive ) );
        props.put( prefix + ".driverClassName", driverClassName );

        JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setConnectionPoolName( poolName );
        cattr.setTableName("JCSTESTTABLE_InitializePoolAccess");

        MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        compositeCacheManager.setConfigurationProperties( props );
        JDBCDiskCacheFactory dcFactory = new JDBCDiskCacheFactory();
        dcFactory.initialize();
        dcFactory.setScheduledExecutorService(Executors.newScheduledThreadPool(2,
        	new DaemonThreadFactory("JCS-JDBCDiskCacheManager-", Thread.MIN_PRIORITY)));

        JDBCDiskCache<String, String> diskCache = dcFactory.createCache( cattr, compositeCacheManager, null, new StandardSerializer() );
        assertNotNull( "Should have a cache instance", diskCache );

        // DO WORK
        DataSourceFactory result = dcFactory.getDataSourceFactory(cattr, props);

        // VERIFY
        assertNotNull( "Should have a data source factory class", result );
        assertEquals( "wrong name", poolName, result.getName() );

        System.setProperty( "hsqldb.cache_scale", "8" );

        String rafroot = "target";
        String database = rafroot + "/cache_hsql_db";

        new org.hsqldb.jdbcDriver();
        Class.forName( driverClassName ).newInstance();
        Connection cConn = DriverManager.getConnection( url + database, userName, password );

        HsqlSetupTableUtil.setupTABLE( cConn, "JCSTESTTABLE_InitializePoolAccess" );

    }
}
