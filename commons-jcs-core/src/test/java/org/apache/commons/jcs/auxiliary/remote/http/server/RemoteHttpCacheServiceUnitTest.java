package org.apache.commons.jcs.auxiliary.remote.http.server;

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
import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;

import java.util.HashSet;

/** Unit tests for the service. */
public class RemoteHttpCacheServiceUnitTest
    extends TestCase
{
    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testUpdate_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        String cacheName = "test";
        String key = "key";
        long requesterId = 2;
        CacheElement<String, String> element = new CacheElement<String, String>( cacheName, key, null );

        // DO WORK
        server.update( element, requesterId );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGet_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.get( "region", "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGetMatching_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMatching( "region", "pattern", 0 );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGetMultiple_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMultiple( "region", new HashSet<String>() );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testRemove_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.remove( "region", "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testRemoveAll_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();

        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService<String, String> server =
            new RemoteHttpCacheService<String, String>( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.removeAll( "region" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }
}
