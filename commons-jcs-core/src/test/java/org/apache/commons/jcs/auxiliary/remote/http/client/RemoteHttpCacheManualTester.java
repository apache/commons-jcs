package org.apache.commons.jcs.auxiliary.remote.http.client;

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
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;

/** Manual tester for a JCS instance configured to use the http client. */
public class RemoteHttpCacheManualTester
    extends TestCase
{
    /** number to use for the test */
    private static int items = 100;

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestRemoteHttpCache.ccf" );
    }

    /**
     * A unit test for JUnit
     * @throws Exception Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        jcs.put( "TestKey", "TestValue" );

//        System.out.println( jcs.getStats() );

        for ( int i = 1; i <= items; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = items; i > 0; i-- )
        {
            String res = jcs.get( i + ":key" );
            if ( res == null )
            {
                //assertNotNull( "[" + i + ":key] should not be null", res );
            }
        }

        // test removal
        jcs.remove( "300:key" );
        assertNull( jcs.get( "TestKey" ) );

//        System.out.println( jcs.getStats() );
    }
}
