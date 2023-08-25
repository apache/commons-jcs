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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.jcs3.access.CacheAccess;

/**
 * Simple methods to be run by active test suites that test removal.
 */
public class RemovalTestUtil
{
    /**
     * Adds elements in the range specified and then removes them using the
     * categorical or substring removal method.
     *
     * @param start
     * @param end
     *
     * @throws Exception
     *                Description of the Exception
     */
    public static void runTestPutThenRemoveCategorical( final int start, final int end )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = start; i < end; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = end; i >= start; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

        for ( int i = start; i < end; i++ )
        {
            jcs.remove( i + ":" );
            assertNull( jcs.get( i + ":key" ) );
        }
    }

    /**
     * Put items in the cache in this key range. Can be used to verify that
     * concurrent operations are not effected by things like hierchical removal.
     *
     * @param start
     *            int
     * @param end
     *            int
     * @throws Exception
     */
    public static void runPutInRange( final int start, final int end )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = start; i < end; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = end; i >= start; i-- )
        {
            final String res = jcs.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }
    }

    /**
     * Just get from start to end.
     *
     * @param start
     *            int
     * @param end
     *            int
     * @param check
     *            boolean -- check to see if the items are in the cache.
     * @throws Exception
     */
    public static void runGetInRange( final int start, final int end, final boolean check )
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        // don't care if they are found
        for ( int i = end; i >= start; i-- )
        {
            final String res = jcs.get( i + ":key" );
            if ( check )
            {
                assertNotNull( "[" + i + ":key] should not be null", res );
            }
        }
    }
}
