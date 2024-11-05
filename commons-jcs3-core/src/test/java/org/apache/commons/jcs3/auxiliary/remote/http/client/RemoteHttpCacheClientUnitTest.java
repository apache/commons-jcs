package org.apache.commons.jcs3.auxiliary.remote.http.client;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.junit.Test;

/** Tests for the client. */
public class RemoteHttpCacheClientUnitTest
{
    /**
     * Verify dispose functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testDispose_normal()
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
        assertEquals( "Wrong type.", RemoteRequestType.DISPOSE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify get functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testGet_normal()
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
        assertEquals( "Wrong result.", expected, result );
        assertEquals( "Wrong type.", RemoteRequestType.GET, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify get functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testGet_nullFromDispatcher()
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
        assertNull( "Wrong result.", result );
        assertEquals( "Wrong type.", RemoteRequestType.GET, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify get functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testGetMatching_normal()
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
        assertEquals( "Wrong result.", expected, result.get( "key" ) );
        assertEquals( "Wrong type.", RemoteRequestType.GET_MATCHING,
                      mockDispatcher.lastRemoteCacheRequest.getRequestType() );
    }

    /**
     * Verify get functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testGetMultiple_normal()
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
        assertEquals( "Wrong result.", expected, result.get( "key" ) );
        assertEquals( "Wrong type.", RemoteRequestType.GET_MULTIPLE,
                      mockDispatcher.lastRemoteCacheRequest.getRequestType() );
    }

    /**
     * Verify remove functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testRemove_normal()
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
        assertEquals( "Wrong type.", RemoteRequestType.REMOVE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify removeall functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testRemoveAll_normal()
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
        assertEquals( "Wrong type.", RemoteRequestType.REMOVE_ALL, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify update functionality
     * <p>
     * @throws IOException
     */
    @Test
    public void testUpdate_normal()
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
        assertEquals( "Wrong type.", RemoteRequestType.UPDATE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }
}
