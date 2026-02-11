package org.apache.commons.jcs4;

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

import org.apache.commons.jcs4.access.CacheAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verify that basic removal functionality works.
 */
class JCSRemovalSimpleConcurrentTest
{
    private CacheAccess<String, String> jcs;

    /**
     * Test setup
     *
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestRemoval.ccf" );
        jcs = JCS.getInstance( "testCache1" );
    }

    /**
     * Verify that clear removes everyting as it should.
     *
     * @throws Exception
     */
    @Test
    void testClear()
        throws Exception
    {

        final int count = 500;

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }
        jcs.clear();

        for ( int i = count; i >= 0; i-- )
        {
            final String res = jcs.get( i + ":key" );
            if ( res != null )
            {
                assertNull( res, "[" + i + ":key] should be null after remvoeall" + jcs.getStatistics() );
            }
        }
    }

    /**
     * Verify that we can clear repeatedly without error.
     *
     * @throws Exception
     */
    @Test
    void testClearRepeatedlyWithoutError()
        throws Exception
    {
        final int count = 500;

        jcs.clear();

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        for ( int i = count; i >= 0; i-- )
        {
            jcs.put( i + ":key", "data" + i );
            jcs.clear();
            final String res = jcs.get( i + ":key" );
            if ( res != null )
            {
                assertNull( res, "[" + i + ":key] should be null after remvoeall" + jcs.getStatistics() );
            }
        }
    }

    /**
     * Verify that 1 level deep hierchical removal works.
     *
     * @throws Exception
     */
    @Test
    void testSingleDepthRemoval()
        throws Exception
    {

        final int count = 500;

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( res, "[" + i + ":key] should not be null" );
        }

        for ( int i = 0; i < count; i++ )
        {
            jcs.remove( i + ":" );
            assertNull( jcs.get( i + ":key" ) );
        }
    }

    /**
     * Verify that 2 level deep hierchical removal works.
     *
     * @throws Exception
     */
    @Test
    void testTwoDeepRemoval()
        throws Exception
    {
        final int count = 500;

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( "key:" + i + ":anotherpart", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            final String res = jcs.get( "key:" + i + ":anotherpart" );
            assertNotNull( res, "[key:" + i + ":anotherpart] should not be null, " + jcs.getStatistics() );
        }

        for ( int i = 0; i < count; i++ )
        {
            jcs.remove( "key:" + i + ":" );
            assertNull( jcs.get( "key:" + i + ":anotherpart" ), jcs.getStatistics().toString() );
        }

    }
}
