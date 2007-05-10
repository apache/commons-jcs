package org.apache.jcs.auxiliary.lateral.socket.tcp;

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

import java.util.Random;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Tests the issue remove on put fuctionality.
 *
 * @author asmuts
 */
public class LateralTCPIssueRemoveOnPutUnitTest
    extends TestCase
{
    private static boolean isSysOut = true;

    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public LateralTCPIssueRemoveOnPutUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralIssueRemoveCache.ccf" );
    }

    /**
     *
     * @throws Exception
     */
    public void testPutLocalPutRemoteGetBusyVerifyRemoved()
        throws Exception
    {
        this.runTestForRegion( "region1", 1, 200, 1 );
    }

    /**
     * Verify that a standard put works.
     *
     * Get the cache configured from a file. Create a tcp service to talk to
     * that cache. Put via the servive. Verify that the cache got the data.
     *
     * @throws Exception
     *
     */
    public void testStandardPut()
        throws Exception
    {
        String region = "region1";

        JCS cache = JCS.getInstance( region );

        Thread.sleep( 100 );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1110" );
        lattr2.setIssueRemoveOnPut( false );
        // should still try to remove
        // lattr2.setAllowPut( false );

        // Using the lateral, this service will put to and remove from
        // the cache instance above.
        // The cache thinks it is different since the listenerid is different
        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        String keyToBeRemovedOnPut = "test1_notremoved";

        ICacheElement element1 = new CacheElement( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get removed, it should get to the cache." );
        service.update( element1 );

        Thread.sleep( 1000 );

        Object testObj = cache.get( keyToBeRemovedOnPut );
        p( "testStandardPut, test object = " + testObj );
        assertNotNull( "The test object should not have been removed by a put.", testObj );
    }

    /**
     * This tests issues tons of puts. It also check to see that a key that was
     * put in was removed by the clients remove command.
     *
     * @param region
     *            Name of the region to access
     * @param range
     * @param numOps
     * @param testNum
     *
     * @exception Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region, int range, int numOps, int testNum )
        throws Exception
    {

        boolean show = true;// false;

        JCS cache = JCS.getInstance( region );

        Thread.sleep( 100 );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1110" );
        lattr2.setIssueRemoveOnPut( true );
        // should still try to remove
        lattr2.setAllowPut( false );

        // Using the lateral, this service will put to and remove from
        // the cache instance above.
        // The cache thinks it is different since the listenerid is different
        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        String keyToBeRemovedOnPut = "test1";
        cache.put( keyToBeRemovedOnPut, "this should get removed." );

        ICacheElement element1 = new CacheElement( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get there" );
        service.update( element1 );

        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                Random ran = new Random( i );
                int n = ran.nextInt( 4 );
                int kn = ran.nextInt( range );
                String key = "key" + kn;

                ICacheElement element = new CacheElement( region, key, region + ":data" + i
                    + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                service.update( element );
                if ( show )
                {
                    p( "put " + key );
                }

                if ( i % 100 == 0 )
                {
                    System.out.println( cache.getStats() );
                }

            }
            p( "Finished cycle of " + numOps );
        }
        catch ( Exception e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
            throw e;
        }

        JCS jcs = JCS.getInstance( region );
        String key = "testKey" + testNum;
        String data = "testData" + testNum;
        jcs.put( key, data );
        String value = (String) jcs.get( key );
        assertEquals( "Couldn't put normally.", data, value );

        // make sure the items we can find are in the correct region.
        for ( int i = 1; i < numOps; i++ )
        {
            String keyL = "key" + i;
            String dataL = (String) jcs.get( keyL );
            if ( dataL != null )
            {
                assertTrue( "Incorrect region detected.", dataL.startsWith( region ) );
            }

        }

        Thread.sleep( 200 );

        Object testObj = cache.get( keyToBeRemovedOnPut );
        p( "runTestForRegion, test object = " + testObj );
        assertNull( "The test object should have been removed by a put.", testObj );

    }

    /**
     * @param s
     *            String to be printed
     */
    public static void p( String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
    }
}
