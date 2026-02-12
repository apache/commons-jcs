package org.apache.commons.jcs4.auxiliary.disk.jdbc;

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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.apache.commons.jcs4.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs4.engine.control.MockKeyMatcher;
import org.apache.commons.jcs4.utils.serialization.StandardSerializer;
import org.apache.commons.jcs4.utils.threadpool.DaemonThreadFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs basic tests for the JDBC disk cache.
 */
public class JDBCDiskCacheUnitTest
{
    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     *
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
            assertEquals( region + " data " + i, element.value(), "value " + i + ":key" );
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

    /** Test setup */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestJDBCDiskCache.ccf" );
    }

    /**
     * Verfiy that it uses the pool access manager config.
     *
     * @throws Exception
     */
    @Test
    void testInitializePoolAccess_withPoolName()
        throws Exception
    {
        // SETUP
        System.setProperty( "hsqldb.cache_scale", "8" );
        final String poolName = "testInitializePoolAccess_withPoolName";

        final Properties props = new Properties();
        final String prefix = JDBCDiskCacheFactory.POOL_CONFIGURATION_PREFIX
    		+ poolName
            + JDBCDiskCacheFactory.ATTRIBUTE_PREFIX;
        props.put( prefix + ".url", "jdbc:hsqldb:target/cache_hsql_db" );
        props.put( prefix + ".userName", "sa" );
        props.put( prefix + ".password", "" );
        props.put( prefix + ".maxActive", String.valueOf(10) );
        props.put( prefix + ".driverClassName", "org.hsqldb.jdbcDriver" );

        final JDBCDiskCacheAttributes cattr = new JDBCDiskCacheAttributes();
        cattr.setConnectionPoolName( poolName );
        cattr.setTableName("JCSTESTTABLE_InitializePoolAccess");

        final MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        compositeCacheManager.setConfigurationProperties( props );
        final JDBCDiskCacheFactory dcFactory = new JDBCDiskCacheFactory();
        dcFactory.initialize();
        dcFactory.setScheduledExecutorService(Executors.newScheduledThreadPool(2,
        	new DaemonThreadFactory("JCS-JDBCDiskCacheManager-", Thread.MIN_PRIORITY)));

        final JDBCDiskCache<String, String> diskCache = dcFactory.createCache( cattr,
                compositeCacheManager, null, new StandardSerializer(), new MockKeyMatcher<>());
        assertNotNull( diskCache, "Should have a cache instance" );

        // DO WORK
        final DataSourceFactory result = dcFactory.getDataSourceFactory(cattr, props);

        // VERIFY
        assertNotNull( result, "Should have a data source factory class" );
        assertEquals( poolName, result.getName(), "wrong name" );

        // Disable this test: it's not clear what it is trying to check. Also it causes an Error when re-running tests:
        // JDBCDiskCacheUnitTest.testInitializePoolAccess_withPoolName:157 » SQL Table already exists:
        //     JCSTESTTABLE_INITIALIZEPOOLACCESS in statement [CREATE CACHED TABLE JCSTESTTABLE_InitializePoolAccess]
        // final Connection cConn = result.getDataSource().getConnection();
        // HsqlSetupUtil.setupTable( cConn, cattr.getTableName());

    }

    /**
     * Test the basic JDBC disk cache functionality with a hsql backing.
     * @throws Exception
     */
    @Test
    void testSimpleJDBCPutGetWithHSQL()
        throws Exception
    {
        try (Connection con = HsqlSetupUtil.getTestDatabaseConnection(new Properties(), "cache_hsql_db"))
        {
            HsqlSetupUtil.setupTable(con, "JCS_STORE2");
        }

        runTestForRegion( "testCache1", 200 );
    }
}
