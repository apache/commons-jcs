package org.apache.commons.jcs;

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
import org.apache.commons.jcs.access.CacheAccess;

/**
 * Verify that basic removal functionality works.
 */
public class JCSRemovalSimpleConcurrentTest
    extends TestCase
{
    /**
     * @param testName
     */
    public JCSRemovalSimpleConcurrentTest( String testName )
    {
        super( testName );
    }

    /**
     * Test setup
     * <p>
     * @throws Exception
     */
    @Override
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestRemoval.ccf" );
        JCS.getInstance( "testCache1" );
    }

    /**
     * Main method passes this test to the text test runner.
     * <p>
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { JCSRemovalSimpleConcurrentTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * Verify that 2 level deep hierchical removal works.
     * <p>
     * @throws Exception
     */
    public void testTwoDeepRemoval()
        throws Exception
    {
        int count = 500;
        CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = 0; i <= count; i++ )
        {
            jcs.put( "key:" + i + ":anotherpart", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            String res = jcs.get( "key:" + i + ":anotherpart" );
            assertNotNull( "[key:" + i + ":anotherpart] should not be null, " + jcs.getStats(), res );
        }

        for ( int i = 0; i <= count; i++ )
        {
            jcs.remove( "key:" + i + ":" );
            assertNull( jcs.getStats(), jcs.get( "key:" + i + ":anotherpart" ) );
        }

    }

    /**
     * Verify that 1 level deep hierchical removal works.
     *
     * @throws Exception
     */
    public void testSingleDepthRemoval()
        throws Exception
    {

        int count = 500;
        CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = 0; i <= count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            String res = jcs.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

        for ( int i = 0; i <= count; i++ )
        {
            jcs.remove( i + ":" );
            assertNull( jcs.get( i + ":key" ) );
        }
    }

    /**
     * Verify that clear removes everyting as it should.
     * <p>
     * @throws Exception
     */
    public void testClear()
        throws Exception
    {

        int count = 500;
        CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        for ( int i = 0; i <= count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            String res = jcs.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }
        jcs.clear();

        for ( int i = count; i >= 0; i-- )
        {
            String res = jcs.get( i + ":key" );
            if ( res != null )
            {
                assertNull( "[" + i + ":key] should be null after remvoeall" + jcs.getStats(), res );
            }
        }
    }

    /**
     * Verify that we can clear repeatedly without error.
     *
     * @throws Exception
     */
    public void testClearRepeatedlyWithoutError()
        throws Exception
    {
        int count = 500;
        CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        jcs.clear();

        for ( int i = 0; i <= count; i++ )
        {
            jcs.put( i + ":key", "data" + i );
        }

        for ( int i = count; i >= 0; i-- )
        {
            String res = jcs.get( i + ":key" );
            assertNotNull( "[" + i + ":key] should not be null", res );
        }

        for ( int i = count; i >= 0; i-- )
        {
            jcs.put( i + ":key", "data" + i );
            jcs.clear();
            String res = jcs.get( i + ":key" );
            if ( res != null )
            {
                assertNull( "[" + i + ":key] should be null after remvoeall" + jcs.getStats(), res );
            }
        }
    }
}
