package org.apache.commons.jcs3;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.jcs3.access.CacheAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs a few thousand queries.
 */
class JCSLightLoadUnitTest
{
    /** Number to use for the test */
    private static final int items = 20000;

    /**
     * Test setup
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestSimpleLoad.ccf" );
    }

    /**
     * A unit test for JUnit
     * @throws Exception Description of the Exception
     */
    @Test
    void testSimpleLoad()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = 1; i < items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = items-1; i > 0; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        // test removal
        jcs.remove( "300:key" );
        assertNull( jcs.get( "300:key" ) );
    }
}
