package org.apache.jcs.auxiliary.lateral.socket.tcp.discovery;

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

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.auxiliary.lateral.LateralCache;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWaitFacade;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.socket.tcp.TCPLateralCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 *
 * @author Aaron Smuts
 *
 */
public class UDPDiscoveryUnitTest
    extends TestCase
{

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestUDPDiscovery.ccf" );
    }

    /**
     * 1. create the attributes for the service
     * <p>
     * 2. create the service
     * <p>
     * 3. create a no wait facade for the service
     * <p>
     * 4. add the facade to the service under the name testCache1
     * <p>
     * 5. create a receiver with the service
     * <p>
     * 6. create a sender
     * <p>
     * 7.create more names than we have no wait facades for the only one that
     * gets added should be testCache1
     * <p>
     * 8. send 10 messages
     * <p>
     * 9. check to see that we got 10 messages
     * <p>
     * 10. check to see if the testCache1 facade got a nowait.
     *
     * @throws Exception
     */
    public void testSimpleUDPDiscovery()
        throws Exception
    {
        // create the attributes for the service
        TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.TCP );
        lac.setTcpServer( "localhost" + ":" + 1111 );

        ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // create the service
        UDPDiscoveryService service = new UDPDiscoveryService( lac.getUdpDiscoveryAddr(), lac.getUdpDiscoveryPort(), lac.getTcpListenerPort(), cacheMgr );
        service.setTcpLateralCacheAttributes( lac );

        // create a no wait facade for the service
        ArrayList noWaits = new ArrayList();
        ILateralCacheAttributes attr = new LateralCacheAttributes();
        attr.setCacheName( "testCache1" );

        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), attr );

        // add the facade to the service under the name testCache1
        service.addNoWaitFacade( lcnwf, "testCache1" );

        // create a receiver with the service
        UDPDiscoveryReceiver receiver = new UDPDiscoveryReceiver( service, "228.5.6.7", 6789, cacheMgr );
        Thread t = new Thread( receiver );
        t.start();

        // create a sender
        UDPDiscoverySender sender = new UDPDiscoverySender( "228.5.6.7", 6789 );

        // create more names than we have no wait facades for
        // the only one that gets added should be testCache1
        ArrayList cacheNames = new ArrayList();
        int numJunk = 10;
        for ( int i = 0; i < numJunk; i++ )
        {
            cacheNames.add( "junkCacheName" + i );
        }
        cacheNames.add( "testCache1" );

        // send max messages
        int max = 10;
        int cnt = 0;
        for ( ; cnt < max; cnt++ )
        {
            sender.passiveBroadcast( "localhost", 1111, cacheNames, 1 );
            Thread.sleep( 3 );
        }

        // check to see that we got 10 messages
        System.out.println( "Receiver count = " + receiver.getCnt() );
        //assertEquals( "Receiver count should be the same as the number
        // sent.", cnt, receiver.getCnt() );

        // request braodcasts change things.
        assertTrue( "Receiver count should be the at least the number sent.", cnt <= receiver.getCnt() );

        Thread.sleep( 2000 );

        // check to see if the testCache1 facade got a nowait.
        assertEquals( "Should have 1", 1, lcnwf.noWaits.length );

        //ArrayList cacheNames2 = new ArrayList();
        //cacheNames2.add( "testCache1" );
        // add another
        //sender.passiveBroadcast( "localhost", 11112, cacheNames2, 1 );
        //Thread.sleep( 30 );
        //assertEquals( "Should have 2", 2, lcnwf.noWaits.length );

    }

    /**
     * Verify that the config does not throw any errors.
     *
     * @throws Exception
     */
    public void testUDPDiscoveryConfig()
        throws Exception
    {
        JCS jcs = JCS.getInstance( "testCache1" );

        System.out.println( jcs.getStats() );

        JCS jcs2 = JCS.getInstance( "testCache2" );

        System.out.println( jcs2.getStats() );

    }

    /**
     * Make sure the no wait facade doesn't add dupes.
     * <p>
     * @throws Exception
     */
    public void testNoWaitFacadeAdd()
        throws Exception
    {
        ArrayList noWaits = new ArrayList();
        ILateralCacheAttributes attr = new LateralCacheAttributes();
        attr.setCacheName( "testCache1" );
        LateralCacheNoWaitFacade lcnwf = new LateralCacheNoWaitFacade( (LateralCacheNoWait[]) noWaits
            .toArray( new LateralCacheNoWait[0] ), attr );

        TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.TCP );
        lac.setTcpServer( "localhost" + ":" + 1111 );

        LateralCache cache = new MockLateralCache( lac );

        // add one
        LateralCacheNoWait noWait = new LateralCacheNoWait( cache );
        lcnwf.addNoWait( noWait );
        assertEquals( "Facade should have 1 no wait", 1, lcnwf.noWaits.length );

        // add another
        LateralCacheNoWait noWait2 = new LateralCacheNoWait( cache );
        lcnwf.addNoWait( noWait2 );
        assertEquals( "Facade should have 2 no waits", 2, lcnwf.noWaits.length );

        // try adding the same one again
        lcnwf.addNoWait( noWait2 );
        assertEquals( "Facade should still have 2 no waits", 2, lcnwf.noWaits.length );

    }
}
