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

import java.io.Serializable;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.junit.Before;
import org.junit.Test;

/**
 */
public class LateralTCPFilterRemoveHashCodeUnitTest
{
    /** Does the test print to system out. */
    private static final boolean isSysOut = false;

    /** The port the server will listen to. */
    private static final int serverPort = 2001;

    /**
     * @param s String to print
     */
    public static void p( final String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
    }

    /**
     * This tests issues tons of puts. It also check to see that a key that was
     * put in was removed by the clients remove command.
     *
     * @param region
     *            Name of the region to access
     * @param numOps
     * @param testNum
     *
     * @throws Exception
     *                If an error occurs
     */
    public static void runTestForRegion( final String region, final int numOps, final int testNum )
        throws Exception
    {
        final CacheAccess<String, Serializable> cache = JCS.getInstance( region );

        Thread.sleep( 100 );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr2.setTcpServer( "localhost:" + serverPort );
        lattr2.setIssueRemoveOnPut( true );
        // should still try to remove
        lattr2.setAllowPut( false );

        // this service will put and remove using the lateral to
        // the cache instance above
        // the cache thinks it is different since the listenerid is different
        final LateralTCPService<String, Serializable> service =
                new LateralTCPService<>(lattr2,  new StandardSerializer());
        service.setListenerId( 123456 );

        final String keyToBeRemovedOnPut = "test1";

        final String keyToNotBeRemovedOnPut = "test2";

        final Serializable dataToPassHashCodeCompare = new Serializable()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public int hashCode()
            {
                return 1;
            }
        };
        //String dataToPassHashCodeCompare = "this should be the same and not
        // get removed.";
        //p( "dataToPassHashCodeCompare hashCode = " + +
        // dataToPassHashCodeCompare.hashCode() );

        cache.put( keyToBeRemovedOnPut, "this should get removed." );
        final ICacheElement<String, Serializable> element1 = new CacheElement<>( region, keyToBeRemovedOnPut, region
            + ":data-this shouldn't get there" );
        service.update( element1 );

        cache.put( keyToNotBeRemovedOnPut, dataToPassHashCodeCompare );
        final ICacheElement<String, Serializable> element2 = new CacheElement<>( region, keyToNotBeRemovedOnPut, dataToPassHashCodeCompare );
        service.update( element2 );

        /*
         * try { for ( int i = 1; i < numOps; i++ ) { Random ran = new Random( i );
         * int n = ran.nextInt( 4 ); int kn = ran.nextInt( range ); String key =
         * "key" + kn;
         *
         * ICacheElement<String, String> element = new CacheElement( region, key, region +
         * ":data" + i + " junk asdfffffffadfasdfasf " + kn + ":" + n );
         * service.update( element ); if ( show ) { p( "put " + key ); }
         *
         * if ( i % 100 == 0 ) { System.out.println( cache.getStats() ); }
         *  } p( "Finished cycle of " + numOps ); } catch ( Exception e ) { p(
         * e.toString() ); e.printStackTrace( System.out ); throw e; }
         */

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

        final Object testObj1 = cache.get( keyToBeRemovedOnPut );
        p( "test object1 = " + testObj1 );
        assertNull( "The test object should have been remvoed by a put.", testObj1 );

        final Object testObj2 = cache.get( keyToNotBeRemovedOnPut );
        p( "test object2 = " + testObj2 + " hashCode = " );
        if ( testObj2 != null )
        {
            p( "test2 hashCode = " + +testObj2.hashCode() );
        }
        assertNotNull( "This should not have been removed, since the hascode were the same.", testObj2 );

    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        System.setProperty( "jcs.auxiliary.LTCP.attributes.TcpServers", "localhost:" + serverPort );
        JCS.setConfigFilename( "/TestTCPLateralRemoveFilter.ccf" );
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void test()
        throws Exception
    {
        runTestForRegion( "region1", 200, 1 );
    }
}
