package org.apache.jcs;

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

import junit.framework.TestCase;

/**
 *
 * @author Aaron Smuts
 *
 */
public class ZeroSizeCacheUnitTest
    extends TestCase
{
    /** number to get each loop */
    private static int items = 20000;

    /**
     * Test setup
     * <p>
     * @throws Exception 
     */
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestZeroSizeCache.ccf" );
        JCS.getInstance( "testCache1" );
    }

    /**
     * Verify that a 0 size cache does not result in errors. You should be able
     * to disable a region this way.
     * @throws Exception
     *
     */
    public void testPutGetRemove()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "testCache1" );

        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        // all the gets should be null
        for ( int i = items; i >= 0; i-- )
        {
            String res = (String) jcs.get( i + ":key" );
            if ( res == null )
            {
                assertNull( "[" + i + ":key] should be null", res );
            }
        }

        // test removal, should be no exceptions
        jcs.remove( "300:key" );

        // allow the shrinker to run
        Thread.sleep( 500 );

        // do it again.
        for ( int i = 0; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = items; i >= 0; i-- )
        {
            String res = (String) jcs.get( i + ":key" );
            if ( res == null )
            {
                assertNull( "[" + i + ":key] should be null", res );
            }
        }

        System.out.println( jcs.getStats() );
    }
}
