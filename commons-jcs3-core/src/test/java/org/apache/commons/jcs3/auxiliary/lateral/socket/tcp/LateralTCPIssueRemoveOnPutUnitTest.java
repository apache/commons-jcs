package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the issue remove on put fuctionality.
 */
public class LateralTCPIssueRemoveOnPutUnitTest
{
    /** Should log data go to system out. */
    private static final boolean isSysOut = false;

    /** The port the server will listen to. */
    private static final int serverPort = 1118;

    /**
     * @param s String to be printed
     */
    public static void p( final String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
    }

    /**
     * This tests issues tons of puts. It also check to see that a key that was put in was removed
     * by the clients remove command.
     * <p>
     * @param region Name of the region to access
     * @param range
     * @param numOps
     * @param testNum
     * @throws Exception If an error occurs
     */
    public static void runTestForRegion( final String region, final int range, final int numOps, final int testNum )
        throws Exception
    {
        final CacheAccess<String, String> cache = JCS.getInstance( region );

        Thread.sleep( 100 );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr2.setTcpServer( "localhost:" + serverPort );
        lattr2.setIssueRemoveOnPut( true );
        // should still try to remove
        lattr2.setAllowPut( false );

        // Using the lateral, this service will put to and remove from
        // the cache instance above.
        // The cache thinks it is different since the listenerid is different
        final LateralTCPService<String, String> service =
                new LateralTCPService<>( lattr2,  new StandardSerializer());
        service.setListenerId( 123456 );

        final String keyToBeRemovedOnPut = "test1";
        cache.put( keyToBeRemovedOnPut, "this should get removed." );

        final ICacheElement<String, String> element1 = new CacheElement<>( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get there" );
        service.update( element1 );

        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                final Random ran = new Random( i );
                final int n = ran.nextInt( 4 );
                final int kn = ran.nextInt( range );
                final String key = "key" + kn;

                final ICacheElement<String, String> element = new CacheElement<>( region, key, region + ":data" + i
                    + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                service.update( element );
                p("put " + key);

                if (i % 100 == 0)
                {
                    p(cache.getStats());
                }

            }
            p("Finished cycle of " + numOps);
        }
        catch ( final Exception e )
        {
            p( e.toString() );
            throw e;
        }

        final CacheAccess<String, String> jcs = JCS.getInstance( region );
        final String key = "testKey" + testNum;
        final String data = "testData" + testNum;
        jcs.put( key, data );
        final String value = jcs.get( key );
        assertEquals( "Couldn't put normally.", data, value );

        // make sure the items we can find are in the correct region.
        for ( int i = 1; i < numOps; i++ )
        {
            final String keyL = "key" + i;
            final String dataL = jcs.get( keyL );
            if ( dataL != null )
            {
                assertTrue( "Incorrect region detected.", dataL.startsWith( region ) );
            }

        }

        Thread.sleep( 200 );

        final Object testObj = cache.get( keyToBeRemovedOnPut );
        p( "runTestForRegion, test object = " + testObj );
        assertNull( "The test object should have been removed by a put.", testObj );

    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        System.setProperty( "jcs.auxiliary.LTCP.attributes.TcpServers", "localhost:" + serverPort );
        JCS.setConfigFilename( "/TestTCPLateralIssueRemoveCache.ccf" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPutLocalPutRemoteGetBusyVerifyRemoved()
        throws Exception
    {
        runTestForRegion( "region1", 1, 200, 1 );
    }

    /**
     * Verify that a standard put works. Get the cache configured from a file. Create a tcp service
     * to talk to that cache. Put via the service. Verify that the cache got the data.
     * <p>
     * @throws Exception
     */
    @Test
    public void testStandardPut()
        throws Exception
    {
        final String region = "region1";

        final CacheAccess<String, String> cache = JCS.getInstance( region );

        Thread.sleep( 100 );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr2.setTcpServer( "localhost:" + serverPort );
        lattr2.setIssueRemoveOnPut( false );
        // should still try to remove
        // lattr2.setAllowPut( false );

        // Using the lateral, this service will put to and remove from
        // the cache instance above.
        // The cache thinks it is different since the listenerid is different
        final LateralTCPService<String, String> service =
                new LateralTCPService<>(lattr2,  new StandardSerializer());
        service.setListenerId( 123456 );

        final String keyToBeRemovedOnPut = "test1_notremoved";

        final ICacheElement<String, String> element1 = new CacheElement<>( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get removed, it should get to the cache." );
        service.update( element1 );

        Thread.sleep( 1000 );

        final Object testObj = cache.get( keyToBeRemovedOnPut );
        p( "testStandardPut, test object = " + testObj );
        assertNotNull( "The test object should not have been removed by a put.", testObj );
    }
}
