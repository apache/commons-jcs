package org.apache.commons.jcs4.access;

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

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.engine.control.CompositeCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * This test is for the system property usage in configuration values.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SystemPropertyUnitTest
{

    /**
     * Set up configuration file before each test.
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestSystemProperties.ccf" );
    }

    /**
     * Clear system properties after each test to prevent interference between tests.
     */
    @AfterEach
    void tearDown()
    {
        System.clearProperty( "MY_SYSTEM_PROPERTY_DISK_DIR" );
        System.clearProperty( "MY_SYSTEM_PROPERTY_MAX_SIZE" );
    }

    /**
     * Verify that we use a system property for a ${FOO} string in a value.
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    void testSystemPropertyInValueDelimiter()
        throws Exception
    {
        final int maxMemory = 1234;
        System.setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );
        System.setProperty( "MY_SYSTEM_PROPERTY_MAX_SIZE", String.valueOf( maxMemory ) );

        final CacheAccess<String, String> cache = JCS.getInstance( "test1" );
        assertEquals( maxMemory, cache.getCacheAttributes().getMaxObjects(),
                      "We should have used the system property for the memory size" );
    }

    /**
     * Verify that we use a default value when the system property is missing.
     *
     * @throws Exception
     */
    @Test
    @Order(2)
    void testSystemPropertyMissingInValueDelimiter()
        throws Exception
    {
        System.setProperty( "MY_SYSTEM_PROPERTY_DISK_DIR", "system_set" );

        final CompositeCacheManager mgr = CompositeCacheManager.getUnconfiguredInstance();
        mgr.configure( "/TestSystemProperties.ccf" );

        final CacheAccess<String, String> cache = JCS.getInstance( "missing" );
        assertEquals( 100, cache.getCacheAttributes().getMaxObjects(),
                      "We should have used the default property for the memory size" );
    }
}
