package org.apache.commons.jcs3.auxiliary.remote.http.server;

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

import java.util.HashSet;

import org.apache.commons.jcs3.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.control.MockCompositeCacheManager;
import org.junit.jupiter.api.Test;

/** Tests for the service. */
class RemoteHttpCacheServiceUnitTest
{
    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testGet_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.get( "region", "key" );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testGetMatching_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMatching( "region", "pattern", 0 );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testGetMultiple_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMultiple( "region", new HashSet<>() );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testRemove_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.remove( "region", "key" );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testRemoveAll_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.removeAll( "region" );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }

    /**
     * Verify event log calls.
     *
     * @throws Exception
     */
    @Test
    void testUpdate_simple()
        throws Exception
    {
        // SETUP
        final MockCompositeCacheManager manager = new MockCompositeCacheManager();
        final MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        final RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        final RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<>( manager, rcsa, cacheEventLogger );

        final String cacheName = "test";
        final String key = "key";
        final long requesterId = 2;
        final CacheElement<String, String> element = new CacheElement<>( cacheName, key, null );

        // DO WORK
        server.update( element, requesterId );

        // VERIFY
        assertEquals( 1, cacheEventLogger.startICacheEventCalls, "Start should have been called." );
        assertEquals( 1, cacheEventLogger.endICacheEventCalls, "End should have been called." );
    }
}
