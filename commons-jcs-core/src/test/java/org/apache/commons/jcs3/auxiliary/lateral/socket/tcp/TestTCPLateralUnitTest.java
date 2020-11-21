package org.apache.commons.jcs3.auxiliary.lateral.socket.tcp;

import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCommand;
import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.control.group.GroupId;

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

        final TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType( LateralCacheAttributes.Type.TCP );
        lac.setTcpServer( "localhost" + ":" + 8111 );
        lac.setTcpListenerPort( 8111 );

        final ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        final LateralTCPListener<String, String> listener = LateralTCPListener.getInstance( lac, cacheMgr );

        // send to the listener
        final LateralTCPSender lur = new LateralTCPSender( lac );

        // DO WORK
        final int numMes = 10;
        for ( int i = 0; i < numMes; i++ )
        {
            final String message = "adsfasasfasfasdasf";
            final CacheElement<String, String> ce = new CacheElement<>( "test", "test", message );
            final LateralElementDescriptor<String, String> led = new LateralElementDescriptor<>( ce );
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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1101 );
        lattr.setTransmissionTypeName( "TCP" );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
//        System.out.println( "mock cache = " + cacheMgr.getCache( "test" ) );

        LateralTCPListener.getInstance( lattr, cacheMgr );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1102 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1101" );

        final LateralTCPService<String, String> service = new LateralTCPService<>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        final int cnt = 100;
        for ( int i = 0; i < cnt; i++ )
        {
            final ICacheElement<String, String> element = new CacheElement<>( "test", "key" + i, "value1" );
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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1103 );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        final CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // setup a service to talk to the listener started above.
        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1104 );
        lattr2.setTcpServer( "localhost:1103" );

        final LateralTCPService<String, String> service = new LateralTCPService<>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( "test", "key", "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        final ICacheElement<String, String> element2 = new CacheElement<>( "test", "key", "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        final ICacheElement<String, String> cacheElement = cache.get( "key" );
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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1105 );
        lattr.setTransmissionTypeName( "TCP" );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        final CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr );

        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1106 );
        lattr2.setTransmissionTypeName( "TCP" );
        lattr2.setTcpServer( "localhost:1105" );

        final LateralTCPService<String, String> service = new LateralTCPService<>( lattr2 );
        service.setListenerId( 123456 );

        // DO WORK
        final String key = "key";
        final ICacheElement<String, String> element = new CacheElement<>( "test", key, "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        final ICacheElement<String, String> element2 = new CacheElement<>( "test", key, "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        final ICacheElement<String, String> cacheElement = cache.get( "key" );
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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1107 );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        final CompositeCache<String, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        final ICacheElement<String, String> element = new CacheElement<>( "test", "key", "value1" );
        cache.update( element );

        // setup a service to talk to the listener started above.
        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1108 );
        lattr2.setTcpServer( "localhost:1107" );

        final LateralTCPService<String, String> service = new LateralTCPService<>( lattr2 );
        service.setListenerId( 123456 );

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        final ICacheElement<String, String> result = service.get( "test", "key" );

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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1150 );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        final CompositeCache<GroupAttrName<String>, String> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        // add the item to the listeners cache
        final GroupAttrName<String> groupKey = new GroupAttrName<>(new GroupId("test", "group"), "key");
        final ICacheElement<GroupAttrName<String>, String> element =
            new CacheElement<>( "test", groupKey, "value1" );
        cache.update( element );

        // setup a service to talk to the listener started above.
        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1151 );
        lattr2.setTcpServer( "localhost:1150" );

        final LateralTCPService<GroupAttrName<String>, String> service =
            new LateralTCPService<>( lattr2 );
        service.setListenerId( 123459 );

        SleepUtil.sleepAtLeast( 500 );

        // DO WORK
        final Set<GroupAttrName<String>> result = service.getKeySet("test");

       // SleepUtil.sleepAtLeast( 5000000 );

        // VERIFY
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Didn't get the correct object", "key", result.iterator().next().attrName );
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
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort( 1108 );
        final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        final CompositeCache<String, Integer> cache = cacheMgr.getCache( "test" );
//        System.out.println( "mock cache = " + cache );

        // get the listener started
        // give it our mock cache manager
        LateralTCPListener.getInstance( lattr, cacheMgr );

        final String keyprefix1 = "MyPrefix1";
        final int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            // add the item to the listeners cache
            final ICacheElement<String, Integer> element = new CacheElement<>( "test", keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
            cache.update( element );
        }

        // setup a service to talk to the listener started above.
        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort( 1108 );
        lattr2.setTcpServer( "localhost:1108" );

        final LateralTCPService<String, Integer> service = new LateralTCPService<>( lattr2 );
        service.setListenerId( 123456 );

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        final Map<String, ICacheElement<String, Integer>> result = service.getMatching( "test", keyprefix1 + ".+" );

        // VERIFY
        assertNotNull( "Result should not be null.", result );
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result.size() );
    }
}
