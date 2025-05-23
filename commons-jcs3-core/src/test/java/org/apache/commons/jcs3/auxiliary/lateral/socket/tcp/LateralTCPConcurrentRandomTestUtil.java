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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 */
public class LateralTCPConcurrentRandomTestUtil
{
    /** Should we write out. */
    private static final boolean isSysOut = false;
    //private static boolean isSysOut = true;

    /**
     * @param s string to print
     */
    public static void p( final String s )
    {
        if ( isSysOut )
        {
            System.out.println( s );
        }
    }

    /**
     * Randomly adds items to cache, gets them, and removes them. The range
     * count is more than the size of the memory cache, so items should spool to
     * disk.
     *
     * @param region
     *            Name of the region to access
     * @param range
     * @param numOps
     * @param testNum
     * @throws Exception
     *                If an error occurs
     */
    public static void runTestForRegion( final String region, final int range, final int numOps, final int testNum )
        throws Exception
    {
        final boolean show = true; //false;

        final CacheAccess<String, String> cache = JCS.getInstance( region );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1103 );
        lattr2.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr2.setTcpServer( "localhost:1102" );

        // this service will put and remove using the lateral to
        // the cache instance above
        // the cache thinks it is different since the listenerid is different
        final LateralTCPService<String, String> service =
                new LateralTCPService<>(lattr2,  new StandardSerializer());
        service.setListenerId( 123456 );

        try
        {
            for ( int i = 1; i < numOps; i++ )
            {
                final Random ran = new Random( i );
                final int n = ran.nextInt( 4 );
                final int kn = ran.nextInt( range );
                final String key = "key" + kn;
                if ( n == 1 )
                {
                    final ICacheElement<String, String> element = new CacheElement<>( region, key, region + ":data" + i
                        + " junk asdfffffffadfasdfasf " + kn + ":" + n );
                    service.update( element );
                    if ( show )
                    {
                        p( "put " + key );
                    }
                }
                /**/
                else if ( n == 2 )
                {
                    service.remove( region, key );
                    if ( show )
                    {
                        p( "removed " + key );
                    }
                }
                /**/
                else
                {
                    // slightly greater chance of get
                    try
                    {
                        final Object obj = service.get( region, key );
                        if ( show && obj != null )
                        {
                            p( obj.toString() );
                        }
                    }
                    catch ( final Exception e )
                    {
                        // consider failing, some timeouts are expected
                        e.printStackTrace();
                    }
                }

                if ( i % 100 == 0 )
                {
                    p( cache.getStats() );
                }

            }
            p( "Finished random cycle of " + numOps );
        }
        catch ( final Exception e )
        {
            p( e.toString() );
            e.printStackTrace( System.out );
            throw e;
        }

        final CacheAccess<String, String> jcs = JCS.getInstance( region );
        final String key = "testKey" + testNum;
        final String data = "testData" + testNum;
        jcs.put( key, data );
        final String value = jcs.get( key );
        assertEquals( data, value, "Couldn't put normally." );

        // make sure the items we can find are in the correct region.
        for ( int i = 1; i < numOps; i++ )
        {
            final String keyL = "key" + i;
            final String dataL = jcs.get( keyL );
            if ( dataL != null )
            {
                assertTrue( dataL.startsWith( region ), "Incorrect region detected." );
            }

        }

        //Thread.sleep( 1000 );

        //ICacheElement<String, String> element = new CacheElement( region, "abc", "testdata");
        //service.update( element );

        //Thread.sleep( 2500 );
        // could be too mcuh going on right now to get ti through, sot he test
        // might fail.
        //String value2 = (String) jcs.get( "abc" );
        //assertEquals( "Couldn't put laterally, could be too much traffic in
        // queue.", "testdata", value2 );

    }
}
