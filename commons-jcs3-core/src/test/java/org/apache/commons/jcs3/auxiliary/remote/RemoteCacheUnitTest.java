package org.apache.commons.jcs3.auxiliary.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import java.util.HashSet;
import java.util.Map;

import org.apache.commons.jcs3.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs3.utils.serialization.SerializationConversionUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Tests for the Remote Cache.
 */
public class RemoteCacheUnitTest
{
    private IRemoteCacheAttributes cattr;
    private MockRemoteCacheService<String, String> service;
    private MockRemoteCacheListener<String, String> listener;
    private RemoteCacheMonitor monitor;

    @Before
    public void setUp() throws Exception
    {
        cattr = new RemoteCacheAttributes();
        service = new MockRemoteCacheService<>();
        listener = new MockRemoteCacheListener<>();
        monitor = new RemoteCacheMonitor();
    }

    /**
     * Verify that there is no problem if there is no listener.
     * <p>
     * @throws Exception
     */
    @Test
    public void testDispose_nullListener()
        throws Exception
    {
        // SETUP
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, null, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.dispose( );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testDispose_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.dispose( );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testGet_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.get( "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testGetMatching_simple()
        throws Exception
    {
        // SETUP
        final String pattern = "adsfasdfasd.?";

        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        final Map<String, ICacheElement<String, String>> result = remoteCache.getMatching( pattern );

        // VERIFY
        assertNotNull( "Should have a map", result );
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testGetMultiple_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.getMultiple( new HashSet<>() );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testRemove_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.remove( "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testRemoveAll_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        // DO WORK
        remoteCache.remove( "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify that the remote service update method is called. The remote cache serializes the object
     * first.
     * <p>
     * @throws Exception
     */
    @Test
    public void testUpdate()
        throws Exception
    {
        // SETUP
        final long listenerId = 123;
        listener.setListenerId( listenerId );

        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final String cacheName = "testUpdate";

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( cacheName, "key", "value" );
        remoteCache.update( element );

        // VERIFY
        assertTrue( "The element should be in the serialized wrapper.",
                    service.lastUpdate instanceof ICacheElementSerialized );
        final ICacheElement<String, String> result = SerializationConversionUtil
            .getDeSerializedCacheElement( (ICacheElementSerialized<String, String>) service.lastUpdate, remoteCache
                .getElementSerializer() );
        assertEquals( "Wrong element updated.", element.getVal(), result.getVal() );
        assertEquals( "Wrong listener id.", Long.valueOf( listenerId ), service.updateRequestIdList.get( 0 ) );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    @Test
    public void testUpdate_simple()
        throws Exception
    {
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, service, listener, monitor );

        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        remoteCache.setCacheEventLogger( cacheEventLogger );

        final ICacheElement<String, String> item = new CacheElement<>( "region", "key", "value" );

        // DO WORK
        remoteCache.update( item );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify that when we call fix events queued in the zombie are propagated to the new service.
     * <p>
     * @throws Exception
     */
    @Test
    public void testUpdateZombieThenFix()
        throws Exception
    {
        // SETUP
        final ZombieCacheServiceNonLocal<String, String> zombie = new ZombieCacheServiceNonLocal<>( 10 );

        // set the zombie
        final RemoteCache<String, String> remoteCache = new RemoteCache<>( cattr, zombie, listener, monitor );

        final String cacheName = "testUpdate";

        // DO WORK
        final ICacheElement<String, String> element = new CacheElement<>( cacheName, "key", "value" );
        remoteCache.update( element );
        // set the new service, this should call propagate
        remoteCache.fixCache( service );

        // VERIFY
        assertTrue( "The element should be in the serialized warapper.",
                    service.lastUpdate instanceof ICacheElementSerialized );
        final ICacheElement<String, String> result = SerializationConversionUtil
            .getDeSerializedCacheElement( (ICacheElementSerialized<String, String>) service.lastUpdate, remoteCache
                .getElementSerializer() );
        assertEquals( "Wrong element updated.", element.getVal(), result.getVal() );
    }
}
