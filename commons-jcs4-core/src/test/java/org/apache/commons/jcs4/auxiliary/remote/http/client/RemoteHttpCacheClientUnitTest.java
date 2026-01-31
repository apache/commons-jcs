package org.apache.commons.jcs4.auxiliary.remote.http.client;

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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs4.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.junit.jupiter.api.Test;

/** Tests for the client. */
class RemoteHttpCacheClientUnitTest
{
    /**
     * Verify dispose functionality
     *
     * @throws IOException
     */
    @Test
    void testDispose_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";

        // DO WORK
        client.dispose( cacheName );

        // VERIFY
        assertEquals( RemoteRequestType.DISPOSE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }

    /**
     * Verify get functionality
     *
     * @throws IOException
     */
    @Test
    void testGet_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";
        final String key = "key";

        final ICacheElement<String, String> expected = new CacheElement<>( cacheName, key, "value" );
        final RemoteCacheResponse<ICacheElement<String, String>> remoteHttpCacheResponse =
            new RemoteCacheResponse<>();
        remoteHttpCacheResponse.setPayload( expected );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        final ICacheElement<String, String> result = client.get( cacheName, key );

        // VERIFY
        assertEquals( expected, result, "Wrong result." );
        assertEquals( RemoteRequestType.GET, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }

    /**
     * Verify get functionality
     *
     * @throws IOException
     */
    @Test
    void testGet_nullFromDispatcher()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";
        final String key = "key";

        mockDispatcher.setupRemoteCacheResponse = null;

        // DO WORK
        final ICacheElement<String, String> result = client.get( cacheName, key );

        // VERIFY
        assertNull( result, "Wrong result." );
        assertEquals( RemoteRequestType.GET, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }

    /**
     * Verify get functionality
     *
     * @throws IOException
     */
    @Test
    void testGetMatching_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";
        final String pattern = "key";

        final ICacheElement<String, String> expected = new CacheElement<>( cacheName, "key", "value" );
        final Map<String, ICacheElement<String, String>> expectedMap = new HashMap<>();
        expectedMap.put( "key", expected );
        final RemoteCacheResponse<Map<String, ICacheElement<String, String>>> remoteHttpCacheResponse =
            new RemoteCacheResponse<>();
        remoteHttpCacheResponse.setPayload( expectedMap );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        final Map<String, ICacheElement<String, String>> result = client.getMatching( cacheName, pattern );

        // VERIFY
        assertEquals( expected, result.get( "key" ), "Wrong result." );
        assertEquals( RemoteRequestType.GET_MATCHING,
                      mockDispatcher.lastRemoteCacheRequest.getRequestType(),
                      "Wrong type." );
    }

    /**
     * Verify get functionality
     *
     * @throws IOException
     */
    @Test
    void testGetMultiple_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";
        final Set<String> keys = Collections.emptySet();

        final ICacheElement<String, String> expected = new CacheElement<>( cacheName, "key", "value" );
        final Map<String, ICacheElement<String, String>> expectedMap = new HashMap<>();
        expectedMap.put( "key", expected );
        final RemoteCacheResponse<Map<String, ICacheElement<String, String>>> remoteHttpCacheResponse =
            new RemoteCacheResponse<>();
        remoteHttpCacheResponse.setPayload( expectedMap );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        final Map<String, ICacheElement<String, String>> result = client.getMultiple( cacheName, keys );

        // VERIFY
        assertEquals( expected, result.get( "key" ), "Wrong result." );
        assertEquals( RemoteRequestType.GET_MULTIPLE,
                      mockDispatcher.lastRemoteCacheRequest.getRequestType(),
                      "Wrong type." );
    }

    /**
     * Verify remove functionality
     *
     * @throws IOException
     */
    @Test
    void testRemove_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";
        final String key = "key";

        // DO WORK
        client.remove( cacheName, key );

        // VERIFY
        assertEquals( RemoteRequestType.REMOVE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }

    /**
     * Verify removeall functionality
     *
     * @throws IOException
     */
    @Test
    void testRemoveAll_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";

        // DO WORK
        client.removeAll( cacheName );

        // VERIFY
        assertEquals( RemoteRequestType.REMOVE_ALL, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }

    /**
     * Verify update functionality
     *
     * @throws IOException
     */
    @Test
    void testUpdate_normal()
        throws IOException
    {
        // SETUP
        final RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<>( attributes );

        final MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        final String cacheName = "test";

        final ICacheElement<String, String> element = new CacheElement<>( cacheName, "key", "value" );

        // DO WORK
        client.update( element );

        // VERIFY
        assertEquals( RemoteRequestType.UPDATE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType(), "Wrong type." );
    }
}
