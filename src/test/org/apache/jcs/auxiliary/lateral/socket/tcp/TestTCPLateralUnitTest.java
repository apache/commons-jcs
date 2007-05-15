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

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCacheManagerMockImpl;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * Basic unit tests for the sending and receiving portions of the lateral cache.
 *
 * @author Aaron Smuts
 *
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
     * Make sure we can send a bunch to the listener. This would be better if we
     * could plugin a Mock CacheManger. The listener will instantiate on on its
     * own. We have to configure one before that.
     * <p>
     * @throws Exception
     */
    public void testSimpleSend()
        throws Exception
    {

        // force initialization
        JCS.getInstance( "test" );

        TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.TCP );
        lac.setTcpServer( "localhost" + ":" + 8111 );
        lac.setTcpListenerPort( 8111 );

        ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        LateralTCPListener listener = (LateralTCPListener) LateralTCPListener.getInstance( lac, cacheMgr );

        // send to the listener
        LateralTCPSender lur = new LateralTCPSender( lac );

        int numMes = 10;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement ce = new CacheElement( "test", "test", message );
            LateralElementDescriptor led = new LateralElementDescriptor( ce );
            led.command = LateralElementDescriptor.UPDATE;
            led.requesterId = 1;
            lur.send( led );
        }

        SleepUtil.sleepAtLeast( numMes * 3 );

        System.out.println( "PutCount = " + listener.getPutCnt() );
        assertEquals( "Should have received " + numMes + " by now.", numMes, listener.getPutCnt() );

    }

    /**
     *
     * @throws Exception
     */
    public void testReceive()
        throws Exception
    {
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1101 );
        lattr.setTransmissionTypeName( "TCP" );
        CompositeCacheManagerMockImpl cacheMgr = new CompositeCacheManagerMockImpl();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // force initialization
        //LateralTCPCacheFactory fact = new LateralTCPCacheFactory();
        //.getInstance( lattr, cacheMgr );
        //LateralCacheNoWait nwait1 = (LateralCacheNoWait)lcMgr1.getCache(
        // "test" );
        //AuxiliaryCache nowait1 = fact.createCache( lattr, cacheMgr );

        //nowait1.update( );

        // start the listener
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1101" );

        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        int cnt = 100;
        for ( int i = 0; i < cnt; i++ )
        {
            ICacheElement element = new CacheElement( "test", "key" + i, "value1" );
            service.update( element );
        }

        SleepUtil.sleepAtLeast( 1000 );

        System.out.println( "cache. getPutCount = " + cacheMgr.getCache().getUpdateCount() );

        assertEquals( "Didn't get the correct number", cnt, cacheMgr.getCache().getUpdateCount() );
    }

    /**
     * Send objects with the same key but different values.
     * @throws Exception
     */
    public void testSameKeyDifferentObject()
        throws Exception
    {
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1103 );
        CompositeCacheManagerMockImpl cacheMgr = new CompositeCacheManagerMockImpl();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1104 );
        lattr2.setTcpServer( "localhost:1103" );

        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        ICacheElement element = new CacheElement( "test", "key", "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        ICacheElement element2 = new CacheElement( "test", "key", "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast(  1000 );

        ICacheElement cacheElement = cacheMgr.getCache().get( "key" );
        System.out.println( "cacheElement = " + cacheElement );
        assertEquals( "Didn't get the correct object", element2.getVal(), cacheElement.getVal() );
    }


    /**
     * Send objects with the same key but different values.
     * @throws Exception
     */
    public void testSameKeyObjectDifferentValueObject()
        throws Exception
    {
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1105 );
        lattr.setTransmissionTypeName( "TCP" );
        CompositeCacheManagerMockImpl cacheMgr = new CompositeCacheManagerMockImpl();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1106);
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1105" );

        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        String key = "key";
        ICacheElement element = new CacheElement( "test", key, "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        ICacheElement element2 = new CacheElement( "test", key, "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        ICacheElement cacheElement = cacheMgr.getCache().get( "key" );
        System.out.println( "cacheElement = " + cacheElement );
        assertEquals( "Didn't get the correct object", element2.getVal(), cacheElement.getVal() );
    }

    /**
     * Create a listener.  Add an element to the listeners cache.  Setup a service.  Try to get from the service.
     * <p>
     * @throws Exception
     */
    public void testSendAndReceived()
        throws Exception
    {
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1107 );
        CompositeCacheManagerMockImpl cacheMgr = new CompositeCacheManagerMockImpl();
        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        ICacheElement element = new CacheElement( "test", "key", "value1" );
        cacheMgr.getCache().update( element );

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1108 );
        lattr2.setTcpServer( "localhost:1107" );

        LateralTCPService service = new LateralTCPService( lattr2 );
        service.setListenerId( 123456 );

        SleepUtil.sleepAtLeast(  300 );

        // DO WORK
        ICacheElement result = service.get( "test", "key" );

        System.out.println( "testSendAndReceived, result = " + result );
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Didn't get the correct object", element.getVal(), result.getVal() );
    }
}
