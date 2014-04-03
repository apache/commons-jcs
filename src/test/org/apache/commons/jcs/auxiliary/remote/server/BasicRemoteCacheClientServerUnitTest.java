package org.apache.commons.jcs.auxiliary.remote.server;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs.auxiliary.remote.RemoteCacheAttributes;
import org.apache.commons.jcs.auxiliary.remote.RemoteCacheManager;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.utils.timing.SleepUtil;

/**
 * These tests startup the remote server and make requests to it.
 * <p>
 * @author Aaron Smuts
 */
public class BasicRemoteCacheClientServerUnitTest
    extends TestCase
{
    /** Server instance to use in the tests. */
    private RemoteCacheServer<String, String> server = null;

    /** the remote server port */
    private final int remotePort;

    /**
     * Starts the server. This is not in a setup, since the server is slow to kill right now.
     */
    public BasicRemoteCacheClientServerUnitTest()
    {
        String configFile = "TestRemoteCacheClientServer.ccf";
        server = RemoteCacheServerStartupUtil.startServerUsingProperties( configFile );
        remotePort = server.remoteCacheServerAttributes.getRemotePort();
    }

    /**
     * Verify that we can start the remote cache server. Send an item to the remote. Verify that the
     * remote put count goes up. If we go through JCS, the manager will be shared and we will get
     * into an endless loop. We will use a mock cache manager instead.
     * <p>
     * The remote server uses the real JCS. We can verify that items are added to JCS behind the
     * server by calling get. We cannot access it directly via JCS since it is serialized.
     * <p>
     * This test uses a mock injected client to test a normal server.
     * <p>
     * @throws Exception
     */
    public void testSinglePut()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();

        RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteHost( "localhost" );
        attributes.setLocalPort( 1202 );
        attributes.setRemotePort( remotePort );

        RemoteCacheManager remoteCacheManager = RemoteCacheManager.getInstance( attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer() );
        String regionName = "testSinglePut";
        AuxiliaryCache<String, String> cache = remoteCacheManager.getCache( regionName );

        // DO WORK
        int numPutsPrior = server.getPutCount();
        ICacheElement<String, String> element = new CacheElement<String, String>( regionName, "key", "value" );
        cache.update( element );
        SleepUtil.sleepAtLeast( 200 );

        // VERIFY
//        System.out.println( server.getStats() );
        assertEquals( "Wrong number of puts", 1, server.getPutCount() - numPutsPrior );

        // DO WORK
        ICacheElement<String, String> result = cache.get( "key" );

        // VERIFY
        assertEquals( "Wrong element.", element.getVal(), result.getVal() );
    }

    /**
     * Verify that we can remove an item via the remote server.
     * <p>
     * @throws Exception
     */
    public void testPutRemove()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();

        RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteHost( "localhost" );
        attributes.setLocalPort( 1202 );
        attributes.setRemotePort( remotePort );

        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteCacheManager remoteCacheManager = RemoteCacheManager.getInstance( attributes, compositeCacheManager, cacheEventLogger, null );
        String regionName = "testPutRemove";
        AuxiliaryCache<String, String> cache = remoteCacheManager.getCache( regionName );

        // DO WORK
        int numPutsPrior = server.getPutCount();
        ICacheElement<String, String> element = new CacheElement<String, String>( regionName, "key", "value" );
        cache.update( element );
        SleepUtil.sleepAtLeast( 50 );

        // VERIFY
//        System.out.println( server.getStats() );
        assertEquals( "Wrong number of puts", 1, server.getPutCount() - numPutsPrior );

        // DO WORK
        ICacheElement<String, String> result = cache.get( "key" );

        // VERIFY
        assertEquals( "Wrong element.", element.getVal(), result.getVal() );

        // DO WORK
        cache.remove( "key" );
        SleepUtil.sleepAtLeast( 200 );
        ICacheElement<String, String> resultAfterRemote = cache.get( "key" );

        // VERIFY
        assertNull( "Element resultAfterRemote should be null.", resultAfterRemote );
    }

    /**
     * Register a listener with the server. Send an update. Verify that the listener received it.
     * @throws Exception
     */
    public void testPutAndListen()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();

        RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteHost( "localhost" );
        attributes.setLocalPort( 1202 );
        attributes.setRemotePort( remotePort );

        RemoteCacheManager remoteCacheManager = RemoteCacheManager.getInstance( attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer() );
        String regionName = "testPutAndListen";
        AuxiliaryCache<String, String> cache = remoteCacheManager.getCache( regionName );

        MockRemoteCacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        server.addCacheListener( regionName, listener );

        // DO WORK
        int numPutsPrior = server.getPutCount();
        ICacheElement<String, String> element = new CacheElement<String, String>( regionName, "key", "value" );
        cache.update( element );
        SleepUtil.sleepAtLeast( 50 );

        // VERIFY
        try
        {
//            System.out.println( server.getStats() );
            assertEquals( "Wrong number of puts", 1, server.getPutCount() - numPutsPrior );
            assertEquals( "Wrong number of puts to listener.", 1, listener.putCount );
        }
        finally
        {
            // remove from all regions.
            server.removeCacheListener( listener );
        }
    }

    /**
     * Register a listener with the server. Send an update. Verify that the listener received it.
     * @throws Exception
     */
    public void testPutaMultipleAndListen()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager compositeCacheManager = new MockCompositeCacheManager();

        RemoteCacheAttributes attributes = new RemoteCacheAttributes();
        attributes.setRemoteHost( "localhost" );
        attributes.setLocalPort( 1202 );
        attributes.setRemotePort( remotePort );

        RemoteCacheManager remoteCacheManager = RemoteCacheManager.getInstance( attributes, compositeCacheManager, new MockCacheEventLogger(), new MockElementSerializer() );
        String regionName = "testPutAndListen";
        AuxiliaryCache<String, String> cache = remoteCacheManager.getCache( regionName );

        MockRemoteCacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        server.addCacheListener( regionName, listener );

        // DO WORK
        int numPutsPrior = server.getPutCount();
        int numToPut = 100;
        for ( int i = 0; i < numToPut; i++ )
        {
            ICacheElement<String, String> element = new CacheElement<String, String>( regionName, "key" + 1, "value" + i );
            cache.update( element );
        }
        SleepUtil.sleepAtLeast( 500 );

        // VERIFY
//        System.out.println( server.getStats() );
        assertEquals( "Wrong number of puts", numToPut, server.getPutCount() - numPutsPrior );
        assertEquals( "Wrong number of puts to listener.", numToPut, listener.putCount );
    }
}
