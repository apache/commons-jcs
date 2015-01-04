package org.apache.commons.jcs.auxiliary.lateral.socket.tcp;

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
import org.apache.commons.jcs.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs.auxiliary.lateral.LateralCommand;
import org.apache.commons.jcs.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.control.group.GroupAttrName;
import org.apache.commons.jcs.engine.control.group.GroupId;
import org.apache.commons.jcs.utils.timing.SleepUtil;

import java.util.Map;
import java.util.Set;

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
    @Override
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
        lac.setTransmissionType( LateralCacheAttributes.Type.TCP );
        lac.setTcpServer( "localhost" + ":" + 8111 );
        lac.setTcpListenerPort( 8111 );

        ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        LateralTCPListener<String, String> listener = LateralTCPListener.getInstance( lac, cacheMgr );

        // send to the listener
        LateralTCPSender lur = new LateralTCPSender( lac );

        // DO WORK
        int numMes = 10;
        for ( int i = 0; i < numMes; i++ )
        {
            String message = "adsfasasfasfasdasf";
            CacheElement<String, String> ce = new CacheElement<String, String>( "test", "test", message );
            LateralElementDescriptor<String, String> led = new LateralElementDescriptor<String, String>( ce );
            led.command = LateralCommand.UPDATE;
            led.requesterId = 1;
            lur.send( led );
        }

        SleepUtil.sleepAtLeast( numMes * 3 );

        // VERIFY
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
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
//        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

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
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

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
        ICacheElement<String, String> cacheElement = cache.get( "key" );
        assertEquals( "Didn't get the correct object "+ cacheElement, element2.getVal(), cacheElement.getVal() );
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
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

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
        ICacheElement<String, String> cacheElement = cache.get( "key" );
        assertEquals( "Didn't get the correct object: " + cacheElement , element2.getVal(), cacheElement.getVal() );
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
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        ICacheElement<String, String> element = new CacheElement<String, String>( "test", "key", "value1" );
        cache.update( element );

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
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Didn't get the correct object", element.getVal(), result.getVal() );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get keys from
     * the service.
     * <p>
     * @throws Exception
     */
    public void testGetGroupKeys_SendAndReceived()  throws Exception
    {
        // SETUP
        // setup a listener
        TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1150 );
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        CompositeCache<GroupAttrName<String>, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        GroupAttrName<String> groupKey = new GroupAttrName<String>(new GroupId("test", "group"), "key");
        ICacheElement<GroupAttrName<String>, String> element =
            new CacheElement<GroupAttrName<String>, String>( "test", groupKey, "value1" );
        cache.update( element );

        // setup a service to talk to the listener started above.
        TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1151 );
        lattr2.setTcpServer( "localhost:1150" );

        LateralTCPService<GroupAttrName<String>, String> service =
            new LateralTCPService<GroupAttrName<String>, String>( lattr2 );
        service.setListenerId( 123459 );

        SleepUtil.sleepAtLeast( 500 );

        // DO WORK
        Set<GroupAttrName<String>> result = service.getKeySet("test");

       // SleepUtil.sleepAtLeast( 5000000 );

        // VERIFY
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Didn't get the correct object", "key", result.toArray()[0] );
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
        MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        CompositeCache<String, Integer> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

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
            cache.update( element );
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
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result.size() );
    }
}
