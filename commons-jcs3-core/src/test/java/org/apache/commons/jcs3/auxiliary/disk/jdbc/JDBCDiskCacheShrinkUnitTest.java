package org.apache.commons.jcs3.auxiliary.disk.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

/**
 * Runs basic tests for the JDBC disk cache.
 */
class JDBCDiskCacheShrinkUnitTest
{
    /**
     * Test setup
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestJDBCDiskCacheShrink.ccf" );
        try (Connection con = HsqlSetupUtil.getTestDatabaseConnection(new Properties(), getClass().getSimpleName()))
        {
            HsqlSetupUtil.setupTable(con, "JCS_STORE_SHRINK");
        }
    }

    /**
     * Verify that those not scheduled to expire do not expire.
     * <p>
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    void testDidNotExpire()
        throws CacheException, InterruptedException
    {
        final String region = "expire100Second";
        final int items = 200;

        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        SleepUtil.sleepAtLeast( 1000 );

        // Test that all items are in cache
        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value, "key = [" + i + ":key] value = [" + value + "]" );
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
     * Verify that eternal trumps max life.
     * @throws CacheException
     * @throws InterruptedException
     */
    @Test
    void testDidNotExpireEternal()
        throws CacheException, InterruptedException
    {
        final String region = "eternal";
        final int items = 200;

        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        SleepUtil.sleepAtLeast( 1000 );

        // Test that all items are in cache
        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );

            assertEquals( region + " data " + i, value, "key = [" + i + ":key] value = [" + value + "]" );
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
     * Test the basic JDBC disk cache functionality with a hsql backing. Verify that items
     * configured to expire after 1 second actually expire.
     * <p>
     * @throws Exception
     */
    @Test
    void testExpireInBackground()
        throws Exception
    {
        final String regionExpire = "expire1Second";
        final int items = 200;

        final CacheAccess<String, String> jcsExpire = JCS.getInstance( regionExpire );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcsExpire.put( i + ":key", regionExpire + " data " + i );
        }

        // the shrinker is supposed to run every second
        SleepUtil.sleepAtLeast( 3000 );

        // Test that all items have been removed from the cache
        for ( int i = 0; i < items; i++ )
        {
            assertNull( jcsExpire.get( i + ":key" ), "Removed key should be null: " + i + ":key" );
        }
    }
}
