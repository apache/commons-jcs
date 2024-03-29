package org.apache.commons.jcs3.auxiliary.disk.jdbc.hsql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.junit.Before;

/**
 * Test which exercises the indexed disk cache. This one uses three different regions for thre
 * threads.
 */
public class HSQLDiskCacheConcurrentUnitTest
{
    /**
     * Number of items to cache, twice the configured maxObjects for the memory cache regions.
     */
    private static final int items = 100;

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     * <p>
     * @param region Name of the region to access
     * @throws Exception If an error occurs
     */
    public static void runTestForRegion( final String region )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( region );

        // Add items to cache
        for ( int i = 0; i < items; i++ )
        {
            jcs.put( i + ":key", region + " data " + i );
        }

        // Test that all items are in cache
        for ( int i = 0; i < items; i++ )
        {
            final String value = jcs.get( i + ":key" );

            assertEquals( "key = [" + i + ":key] value = [" + value + "]", region + " data " + i, value );
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
     * A unit test suite for JUnit. Uses ActiveTestSuite to run multiple tests concurrently.
     * <p>
     * @return The test suite
     */
    public static Test suite()
    {
        final ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest(new TestCase("testHSQLDiskCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestForRegion( "indexedRegion1" );
            }
        });

        suite.addTest(new TestCase("testHSQLDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestForRegion( "indexedRegion2" );
            }
        });

        suite.addTest(new TestCase("testHSQLDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                runTestForRegion( "indexedRegion3" );
            }
        });

        return suite;
    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestHSQLDiskCacheConcurrent.ccf" );
    }
}
