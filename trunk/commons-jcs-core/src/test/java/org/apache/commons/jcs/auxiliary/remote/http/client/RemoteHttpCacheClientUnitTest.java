package org.apache.commons.jcs.auxiliary.remote.http.client;

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
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Unit tests for the client. */
public class RemoteHttpCacheClientUnitTest
    extends TestCase
{
    /**
     * Verify get functionality
     * <p>
     * @throws IOException
     */
    public void testGet_nullFromDispatcher()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

        mockDispatcher.setupRemoteCacheResponse = null;

        // DO WORK
        ICacheElement<String, String> result = client.get( cacheName, key );

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
    public void testGet_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

        ICacheElement<String, String> expected = new CacheElement<String, String>( cacheName, key, "value" );
        RemoteCacheResponse<ICacheElement<String, String>> remoteHttpCacheResponse =
            new RemoteCacheResponse<ICacheElement<String,String>>();
        remoteHttpCacheResponse.setPayload( expected );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        ICacheElement<String, String> result = client.get( cacheName, key );

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
    public void testGetMatching_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String pattern = "key";

        ICacheElement<String, String> expected = new CacheElement<String, String>( cacheName, "key", "value" );
        Map<String, ICacheElement<String, String>> expectedMap = new HashMap<String, ICacheElement<String,String>>();
        expectedMap.put( "key", expected );
        RemoteCacheResponse<Map<String, ICacheElement<String, String>>> remoteHttpCacheResponse =
            new RemoteCacheResponse<Map<String,ICacheElement<String,String>>>();
        remoteHttpCacheResponse.setPayload( expectedMap );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        Map<String, ICacheElement<String, String>> result = client.getMatching( cacheName, pattern );

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
    public void testGetMultiple_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        Set<String> keys = Collections.emptySet();

        ICacheElement<String, String> expected = new CacheElement<String, String>( cacheName, "key", "value" );
        Map<String, ICacheElement<String, String>> expectedMap = new HashMap<String, ICacheElement<String,String>>();
        expectedMap.put( "key", expected );
        RemoteCacheResponse<Map<String, ICacheElement<String, String>>> remoteHttpCacheResponse =
            new RemoteCacheResponse<Map<String,ICacheElement<String,String>>>();
        remoteHttpCacheResponse.setPayload( expectedMap );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        Map<String, ICacheElement<String, String>> result = client.getMultiple( cacheName, keys );

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
    public void testRemove_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

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
    public void testRemoveAll_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

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
    public void testUpdate_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

        ICacheElement<String, String> element = new CacheElement<String, String>( cacheName, "key", "value" );

        // DO WORK
        client.update( element );

        // VERIFY
        assertEquals( "Wrong type.", RemoteRequestType.UPDATE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }

    /**
     * Verify dispose functionality
     * <p>
     * @throws IOException
     */
    public void testDispose_normal()
        throws IOException
    {
        // SETUP
        RemoteHttpCacheAttributes attributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheClient<String, String> client = new RemoteHttpCacheClient<String, String>( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

        // DO WORK
        client.dispose( cacheName );

        // VERIFY
        assertEquals( "Wrong type.", RemoteRequestType.DISPOSE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }
}
