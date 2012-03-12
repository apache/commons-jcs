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

import java.util.Map;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.MockCompositeCacheManager;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * Basic unit tests for the sending and receiving portions of the lateral cache.
 * <p>
 * @author Aaron Smuts
 */
public class TestTCPLateralUnitTest
    extends TestCase
{
    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCache.ccf" );
    }

    /**
     * Make sure we can send a bunch to the listener. This would be better if we could plugin a Mock
     * CacheManger. The listener will instantiate it on its own. We have to configure one before
     * that.
     * <p>
     * @throws Exception
     */
    public void testSimpleSend()
        throws Exception
    {
        // SETUP
        // force initialization
        JCS.getInstance( "test" );

        TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.TCP );
        lac.setTcpServer( "localhost" + ":" + 8111 );
        lac.setTcpListenerPort( 8111 );

        ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        LateralTCPListener<String, String> listener = (LateralTCPListener) LateralTCPListener.getInstance( lac, cacheMgr );

        // send to the listener
        LateralTCPSender lur = new LateralTCPSender( lac );

        // DO WORK
        int numMes = 10;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement<String, String> ce = new CacheElement<String, String>( "test", "test", message );
            LateralElementDescriptor<String, String> led = new LateralElementDescriptor<String, String>( ce );
            led.command = LateralElementDescriptor.UPDATE;
            led.requesterId = 1;
            lur.send( led );
        }

        SleepUtil.sleepAtLeast( numMes * 3 );

        // VERIFY
        System.out.println( "PutCount = " + listener.getPutCnt() );
        assertEquals( "Should have received " + numMes + " by now.", numMes, listener.getPutCnt() );
    }

    /**
     * @throws Exception
     */
    public void testReceive()
        throws Exception
    {
        // VERIFY
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1101 );
        lattr.setTransmissionTypeName( "TCP" );
        MockCompositeCacheManager<String, String> cacheMgr = new MockCompositeCacheManager<String, String>();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        LateralTCPListener.getInstance( lattr, cacheMgr );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1101" );

        LateralTCPService<String, String> service = new LateralTCPService<String, String>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        int cnt = 100;
        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement<String, String> element = new CacheElement<String, String>( "test", "key" + i, "value1" );
            service.update( element );
        }

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        System.out.println( "cache. getPutCount = " + cacheMgr.getCache().getUpdateCount() );
        assertEquals( "Didn't get the correct number", cnt, cacheMgr.getCache().getUpdateCount() );
    }

    /**
     * Send objects with the same key but different values.
     * <p>
     * @throws Exception
     */
    public void testSameKeyDifferentObject()
        throws Exception
    {
        // SETUP
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1103 );
        MockCompositeCacheManager<String, String> cacheMgr = new MockCompositeCacheManager<String, String>();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1104 );
        lattr2.setTcpServer( "localhost:1103" );

        LateralTCPService<String, String> service = new LateralTCPService<String, String>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        ICacheElement<String, String> element = new CacheElement<String, String>( "test", "key", "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        ICacheElement<String, String> element2 = new CacheElement<String, String>( "test", "key", "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        ICacheElement<String, String> cacheElement = cacheMgr.getCache().get( "key" );
        System.out.println( "cacheElement = " + cacheElement );
        assertEquals( "Didn't get the correct object", element2.getVal(), cacheElement.getVal() );
    }

    /**
     * Send objects with the same key but different values.
     * <p>
     * @throws Exception
     */
    public void testSameKeyObjectDifferentValueObject()
        throws Exception
    {
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1105 );
        lattr.setTransmissionTypeName( "TCP" );
        MockCompositeCacheManager<String, String> cacheMgr = new MockCompositeCacheManager<String, String>();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1106 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1105" );

        LateralTCPService<String, String> service = new LateralTCPService<String, String>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        String key = "key";
        ICacheElement<String, String> element = new CacheElement<String, String>( "test", key, "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        ICacheElement<String, String> element2 = new CacheElement<String, String>( "test", key, "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        ICacheElement<String, String> cacheElement = cacheMgr.getCache().get( "key" );
        System.out.println( "cacheElement = " + cacheElement );
        assertEquals( "Didn't get the correct object", element2.getVal(), cacheElement.getVal() );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get from
     * the service.
     * <p>
     * @throws Exception
     */
    public void testGet_SendAndReceived()
        throws Exception
    {
        // SETUP
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1107 );
        MockCompositeCacheManager<String, String> cacheMgr = new MockCompositeCacheManager<String, String>();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        ICacheElement<String, String> element = new CacheElement<String, String>( "test", "key", "value1" );
        cacheMgr.getCache().update( element );

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1108 );
        lattr2.setTcpServer( "localhost:1107" );

        LateralTCPService<String, String> service = new LateralTCPService<String, String>( lattr2 );
        service.setListenerId( 123456 );

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        ICacheElement<String, String> result = service.get( "test", "key" );

        // VERIFY
        System.out.println( "testSendAndReceived, result = " + result );
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Didn't get the correct object", element.getVal(), result.getVal() );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get from
     * the service.
     * <p>
     * @throws Exception
     */
    public void testGetMatching_WithData()
        throws Exception
    {
        // SETUP
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1108 );
        MockCompositeCacheManager<String, Integer> cacheMgr = new MockCompositeCacheManager<String, Integer>();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        String keyprefix1 = "MyPrefix1";
        int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            // add the item to the listeners cache
            ICacheElement<String, Integer> element = new CacheElement<String, Integer>( "test", keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
            cacheMgr.getCache().update( element );
        }

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1108 );
        lattr2.setTcpServer( "localhost:1108" );

        LateralTCPService<String, Integer> service = new LateralTCPService<String, Integer>( lattr2 );
        service.setListenerId( 123456 );

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        Map<String, ICacheElement<String, Integer>> result = service.getMatching( "test", keyprefix1 + ".+" );

        // VERIFY
        System.out.println( "testSendAndReceived, result = " + result );
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result.size() );
    }
}
