package org.apache.commons.jcs4.auxiliary.remote.server;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs4.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs4.auxiliary.remote.RemoteCacheAttributes;
import org.apache.commons.jcs4.auxiliary.remote.RemoteCacheFactory;
import org.apache.commons.jcs4.auxiliary.remote.RemoteCacheManager;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.CacheStatus;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs4.engine.control.MockElementSerializer;
import org.apache.commons.jcs4.utils.net.HostNameUtil;
import org.apache.commons.jcs4.utils.timing.SleepUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
/**
 * These tests startup the remote server and make requests to it.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class BasicRemoteCacheClientServerUnitTest
{

    private static final int LOCAL_PORT = 12020;

    /**
     * Server instance to use in the tests.
     */
    private static RemoteCacheServer<String, String> server;

    /**
     * Factory instance to use in the tests.
     */
    private static RemoteCacheFactory factory;

    /**
     * The remote server port
     */
    private static int remotePort;

    /**
     * Starts the server. This is not in a setup, since the server is slow to kill right now.
     */
    @BeforeAll
    static void setup()
    {
        // Add some debug to try and find out why test fails on Jenkins/Continuum
        try {
            final InetAddress lh = InetAddress.getByName("localhost");
            System.out.println( "localhost=" + lh );
            final InetAddress ina = InetAddress.getLocalHost();
            System.out.println( "InetAddress.getLocalHost()=" + ina );

            // Iterate all NICs (network interface cards)...
            final Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if ( ifaces != null )
            {
                while ( ifaces.hasMoreElements() )
                {
                    final NetworkInterface iface = ifaces.nextElement();
                    // Iterate all IP addresses assigned to each card...
                    for ( final Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); )
                    {
                        final InetAddress inetAddr = inetAddrs.nextElement();
                        final boolean loopbackAddress = inetAddr.isLoopbackAddress();
                        final boolean siteLocalAddress = inetAddr.isSiteLocalAddress();
                        System.out.println( "Found: " + inetAddr +
                                                " isLoopback: " + loopbackAddress +
                                                " isSiteLocal: " + siteLocalAddress +
                                                ( !loopbackAddress && siteLocalAddress ? " *" : "" ) );
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        // End of debug
        final String configFile = "TestRemoteCacheClientServer.ccf";
        server = RemoteCacheServerStartupUtil.startServerUsingProperties(configFile);
        factory = new RemoteCacheFactory();
        factory.initialize();
        remotePort = server.remoteCacheServerAttributes.getRemoteLocation().getPort();
    }

    @AfterAll
    static void stop()
        throws IOException
    {
        if (server != null) { // in case setup failed, no point throwing NPE as well
            server.shutdown("localhost", remotePort);
        }
    }

    /**
     * Verify that we can start the remote cache server. Send an item to the remote. Verify that the
     * remote put count goes up. If we go through JCS, the manager will be shared and we will get
     * into an endless loop. We will use a mock cache manager instead.
     */
    @Test
    void test1SinglePut()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        final RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteLocation("localhost", remotePort);
        attributes.setLocalPort(LOCAL_PORT);
        attributes.setCacheName("testSinglePut");

        final RemoteCacheManager remoteCacheManager = factory.getManager(attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer());
        final AuxiliaryCache<String, String> cache = remoteCacheManager.getCache(attributes);

        // DO WORK
        final int numPutsPrior = server.getPutCount();
        final ICacheElement<String, String> element = new CacheElement<>(cache.getCacheName(), "key", "value");
        cache.update(element);
        SleepUtil.sleepAtLeast(200);

        // VERIFY
        assertEquals( CacheStatus.ALIVE, cache.getStatus(), "Cache is alive" );
        assertEquals( 1, server.getPutCount() - numPutsPrior, "Wrong number of puts" );

        // DO WORK
        final ICacheElement<String, String> result = cache.get("key");

        // VERIFY
        assertEquals( element.getVal(), result.getVal(), "Wrong element." );
    }

    /**
     * Verify that we can remove an item via the remote server.
     */
    @Test
    void test2PutRemove()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        final RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteLocation("localhost", remotePort);
        attributes.setLocalPort(LOCAL_PORT);
        attributes.setCacheName("testPutRemove");

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        final RemoteCacheManager remoteCacheManager = factory.getManager(attributes, compositeCacheManager, cacheEventLogger, null);
        final AuxiliaryCache<String, String> cache = remoteCacheManager.getCache(attributes);

        // DO WORK
        final int numPutsPrior = server.getPutCount();
        final ICacheElement<String, String> element = new CacheElement<>(cache.getCacheName(), "key", "value");
        cache.update(element);
        SleepUtil.sleepAtLeast(50);

        // VERIFY
        assertEquals( CacheStatus.ALIVE, cache.getStatus(), "Cache is alive" );
        assertEquals( 1, server.getPutCount() - numPutsPrior, "Wrong number of puts" );

        // DO WORK
        final ICacheElement<String, String> result = cache.get("key");

        // VERIFY
        assertEquals( element.getVal(), result.getVal(), "Wrong element." );

        // DO WORK
        cache.remove("key");
        SleepUtil.sleepAtLeast(200);
        final ICacheElement<String, String> resultAfterRemote = cache.get("key");

        // VERIFY
        assertNull( resultAfterRemote, "Element resultAfterRemote should be null." );
    }

    /**
     * Register a listener with the server. Send an update. Verify that the listener received it.
     */
    @Test
    void test3PutAndListen()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        final RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteLocation("localhost", remotePort);
        attributes.setLocalPort(LOCAL_PORT);
        attributes.setCacheName("testPutAndListen");

        final RemoteCacheManager remoteCacheManager = factory.getManager(attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer());
        final AuxiliaryCache<String, String> cache = remoteCacheManager.getCache(attributes);

        final MockRemoteCacheListener<String, String> listener = new MockRemoteCacheListener<>();
        server.addCacheListener(cache.getCacheName(), listener);

        // DO WORK
        final int numPutsPrior = server.getPutCount();
        final ICacheElement<String, String> element = new CacheElement<>(cache.getCacheName(), "key", "value");
        cache.update(element);
        SleepUtil.sleepAtLeast(50);

        // VERIFY
        assertEquals( CacheStatus.ALIVE, cache.getStatus(), "Cache is alive" );
        assertEquals( 1, server.getPutCount() - numPutsPrior, "Wrong number of puts" );
        assertEquals( 1, listener.putCount, "Wrong number of puts to listener." );

        // Clean up
        server.removeCacheListener( listener );
    }

    /**
     * Register a listener with the server. Send multiple updates. Verify that the listener received them.
     */
    @Test
    void test4PutaMultipleAndListen()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();
        final RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteLocation("localhost", remotePort);
        attributes.setLocalPort(LOCAL_PORT);
        attributes.setCacheName("testPutaMultipleAndListen");

        final RemoteCacheManager remoteCacheManager = factory.getManager(attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer());
        final AuxiliaryCache<String, String> cache = remoteCacheManager.getCache(attributes);

        final MockRemoteCacheListener<String, String> listener = new MockRemoteCacheListener<>();
        server.addCacheListener(cache.getCacheName(), listener);

        // DO WORK
        final int numPutsPrior = server.getPutCount();
        final int numToPut = 100;
        for ( int i = 0; i < numToPut; i++ )
        {
            final ICacheElement<String, String> element = new CacheElement<>( cache.getCacheName(), "key" + i,
                                                                              "value" + i );
            cache.update(element);
        }
        SleepUtil.sleepAtLeast(500);

        // VERIFY
        assertEquals( CacheStatus.ALIVE, cache.getStatus(), "Cache is alive" );
        assertEquals( numToPut, server.getPutCount() - numPutsPrior, "Wrong number of puts" );
        assertEquals( numToPut, listener.putCount, "Wrong number of puts to listener." );
    }

    @Test
    void testLocalHost()
        throws Exception
    {
        final InetAddress byName = InetAddress.getByName("localhost");
        assertTrue( byName.isLoopbackAddress(),
                    "Expected localhost (" + byName.getHostAddress() + ") to be a loopback address" );
        final InetAddress localHost = HostNameUtil.getLocalHostLANAddress();
        assertTrue( localHost.isSiteLocalAddress(),
                    "Expected getLocalHostLANAddress() (" + localHost + ") to return a site local address" );
    }
}
