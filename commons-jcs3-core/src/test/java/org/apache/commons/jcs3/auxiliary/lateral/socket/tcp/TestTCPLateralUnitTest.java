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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheMonitor;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheAttributes;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCacheNoWait;
import org.apache.commons.jcs3.auxiliary.lateral.LateralCommand;
import org.apache.commons.jcs3.auxiliary.lateral.LateralElementDescriptor;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.control.group.GroupId;
import org.apache.commons.jcs3.utils.serialization.EncryptingSerializer;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for the sending and receiving portions of the lateral cache.
 */
class TestTCPLateralUnitTest
{
    private final MockCompositeCacheManager cacheMgr = new MockCompositeCacheManager();

    private <K,V> CompositeCache<K, V> createCache(final int port)
    {
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setTcpListenerPort(port);
        lattr.setTransmissionType(LateralCacheAttributes.Type.TCP);

        final CompositeCache<K, V> cache = cacheMgr.getCache( "test" );

        // get the listener started
        // give it our mock cache manager
        //LateralTCPListener listener = (LateralTCPListener)
        LateralTCPListener.getInstance( lattr, cacheMgr, new StandardSerializer());

        return cache;
    }

    private <K, V> LateralTCPService<K, V> createService(final int listenerPort, final int serverPort, final long listenerId) throws IOException
    {
        final TCPLateralCacheAttributes lattr2 = new TCPLateralCacheAttributes();
        lattr2.setTcpListenerPort(listenerPort);
        lattr2.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr2.setTcpServer("localhost:" + serverPort);

        final LateralTCPService<K, V> service = new LateralTCPService<>(lattr2,  new StandardSerializer());
        service.setListenerId(listenerId);

        return service;
    }

    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestTCPLateralCache.ccf" );
    }

    private void simpleSend(final IElementSerializer serializer, final int port ) throws IOException
    {
    	// SETUP
        // force initialization
        JCS.getInstance( "test" );

        final TCPLateralCacheAttributes lac = new TCPLateralCacheAttributes();
        lac.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lac.setTcpServer( "localhost:" + port );
        lac.setTcpListenerPort( port );

        final ICompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();

        // start the listener
        final LateralTCPListener<String, String> listener = LateralTCPListener.getInstance( lac, cacheMgr, serializer );

        // send to the listener
        final LateralTCPSender lur = new LateralTCPSender(lac,  serializer);

        // DO WORK
        final int numMes = 10;
        for ( int i = 0; i < numMes; i++ )
        {
            final String message = "adsfasasfasfasdasf";
            final CacheElement<String, String> ce = new CacheElement<>( "test", "test", message );
            final LateralElementDescriptor<String, String> led =
                    new LateralElementDescriptor<>(ce, LateralCommand.UPDATE, 1);
            lur.send( led );
        }

        SleepUtil.sleepAtLeast( numMes * 4 ); // this may need to be adjusted ...

        // VERIFY
        assertEquals( numMes, listener.getPutCnt(), "Should have received " + numMes + " by now." );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get from
     * the service.
     *
     * @throws Exception
     */
    @Test
    void testGet_SendAndReceived()
        throws Exception
    {
        // SETUP
        final CompositeCache<String, String> cache = createCache(1107);

        // add the item to the listeners cache
        final ICacheElement<String, String> element = new CacheElement<>( "test", "key", "value1" );
        cache.update( element );

        // setup a service to talk to the listener started above.
        final LateralTCPService<String, String> service = createService(1108, 1107, 123456);

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        final ICacheElement<String, String> result = service.get( "test", "key" );

        // VERIFY
        assertNotNull( result, "Result should not be null." );
        assertEquals( element.getVal(), result.getVal(), "Didn't get the correct object" );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get keys from
     * the service.
     *
     * @throws Exception
     */
    @Test
    void testGetGroupKeys_SendAndReceived()
        throws Exception
    {
        // SETUP
        final CompositeCache<GroupAttrName<String>, String> cache = createCache(1150);

        // add the item to the listeners cache
        final GroupAttrName<String> groupKey = new GroupAttrName<>(new GroupId("test", "group"), "key");
        final ICacheElement<GroupAttrName<String>, String> element =
            new CacheElement<>( "test", groupKey, "value1" );
        cache.update( element );

        // setup a service to talk to the listener started above.
        final LateralTCPService<GroupAttrName<String>, String>service = createService(1151, 1150, 123459);

        SleepUtil.sleepAtLeast( 500 );

        // DO WORK
        final Set<GroupAttrName<String>> result = service.getKeySet("test");

       // SleepUtil.sleepAtLeast( 5000000 );

        // VERIFY
        assertNotNull( result, "Result should not be null." );
        assertEquals( "key", result.iterator().next().attrName, "Didn't get the correct object" );
    }

    /**
     * Create a listener. Add an element to the listeners cache. Setup a service. Try to get from
     * the service.
     *
     * @throws Exception
     */
    @Test
    void testGetMatching_WithData()
        throws Exception
    {
        // SETUP
        final CompositeCache<String, Integer> cache = createCache(1108);

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
        final LateralTCPService<String, Integer> service = createService(1108, 1108, 123456);

        SleepUtil.sleepAtLeast( 300 );

        // DO WORK
        final Map<String, ICacheElement<String, Integer>> result = service.getMatching( "test", keyprefix1 + ".+" );

        // VERIFY
        assertNotNull( result, "Result should not be null." );
        assertEquals( numToInsertPrefix1, result.size(), "Wrong number returned 1:" );
    }

    /**
     * @throws Exception
     */
    @Test
    void testReceive()
        throws Exception
    {
        // VERIFY
        createCache(1101);

        final LateralTCPService<String, String> service = createService(1102, 1101, 123456);

        // DO WORK
        final int cnt = 100;
        for ( int i = 0; i < cnt; i++ )
        {
            final ICacheElement<String, String> element = new CacheElement<>( "test", "key" + i, "value1" );
            service.update( element );
        }

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        assertEquals( cnt, cacheMgr.getCache().getUpdateCount(), "Didn't get the correct number" );
    }

    /**
     * Send objects with the same key but different values.
     *
     * @throws Exception
     */
    @Test
    void testSameKeyDifferentObject()
        throws Exception
    {
        // SETUP
        final CompositeCache<String, String> cache = createCache(1103);

        // setup a service to talk to the listener started above.
        final LateralTCPService<String, String> service = createService(1104, 1103, 123456);

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( "test", "key", "value1" );
        service.update( element );

        SleepUtil.sleepAtLeast( 300 );

        final ICacheElement<String, String> element2 = new CacheElement<>( "test", "key", "value2" );
        service.update( element2 );

        SleepUtil.sleepAtLeast( 1000 );

        // VERIFY
        final ICacheElement<String, String> cacheElement = cache.get( "key" );
        assertEquals( element2.getVal(), cacheElement.getVal(), "Didn't get the correct object " + cacheElement );
    }

    /**
     * Send objects with the same key but different values.
     *
     * @throws Exception
     */
    @Test
    void testSameKeyObjectDifferentValueObject()
        throws Exception
    {
        final CompositeCache<String, String> cache = createCache(1105);

        final LateralTCPService<String, String> service = createService(1106, 1105, 123456);

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
        assertEquals( element2.getVal(), cacheElement.getVal(), "Didn't get the correct object: " + cacheElement );
    }

    /**
     * Test that the cache has an error status after failing to connect to a TCP server.
     */
    @Test
    void testCacheErrorStatusTcpConnectionFail()
    {
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setCacheName("test");
        lattr.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr.setTcpServer("localhost:1109");
        LateralTCPCacheFactory factory = new LateralTCPCacheFactory();
        factory.initialize();

        LateralCacheNoWait<String, String> lateralNoWait = factory.createCacheNoWait(lattr, null, new StandardSerializer());

        assertEquals(CacheStatus.ERROR, lateralNoWait.getStatus());
    }

    /**
     * Test that the cache monitor can fix a cache that is in error state after failing to connect to a TCP server.
     *
     * @throws Exception
     */
    @Test
    void testCacheRecoveryUsingMonitor() throws Exception
    {
        final TCPLateralCacheAttributes lattr = new TCPLateralCacheAttributes();
        lattr.setCacheName("test");
        lattr.setTransmissionType(LateralCacheAttributes.Type.TCP);
        lattr.setTcpServer("localhost:1110");
        LateralTCPCacheFactory factory = new LateralTCPCacheFactory();
        factory.initialize();

        // reduce the monitor idle period between 'fix' attempts for testing purposes
        MyCacheMonitor.setIdle(500L);
        LateralCacheNoWait<String, String> lateralNoWait = factory.createCacheNoWait(lattr, null, new StandardSerializer());

        // start a TCP server for the cache to connect to
        createCache(1110);
        // adding the cache to the monitor releases the monitor 'fix' thread
        factory.monitorCache(lateralNoWait);

        Thread.sleep(2000L);

        // verify that the monitor has fixed the cache
        assertEquals(CacheStatus.ALIVE, lateralNoWait.getStatus());
    }


    /**
     * @throws Exception
     */
    @Test
    void testSimpleEncryptedSend()
        throws Exception
    {
    	final EncryptingSerializer serializer = new EncryptingSerializer();
    	serializer.setPreSharedKey("my_key");
    	simpleSend(serializer, 8112);
    }

    /**
     * Make sure we can send a bunch to the listener. This would be better if we could plugin a Mock
     * CacheManger. The listener will instantiate it on its own. We have to configure one before
     * that.
     *
     * @throws Exception
     */
    @Test
    void testSimpleSend()
        throws Exception
    {
    	simpleSend(new StandardSerializer(), 8111);
    }

    // used to reduce the monitor idle period between 'fix' attempts for testing purposes
    private static class MyCacheMonitor extends AbstractAuxiliaryCacheMonitor {
        public static void setIdle(long idlePeriod) {
            AbstractAuxiliaryCacheMonitor.idlePeriod = idlePeriod;
        }

        public MyCacheMonitor() {
            super("test");
        }

        @Override
        protected void dispose() {
            // nothing to dispose
        }

        @Override
        protected void doWork() {
            // nothing to do
        }
    }
}
