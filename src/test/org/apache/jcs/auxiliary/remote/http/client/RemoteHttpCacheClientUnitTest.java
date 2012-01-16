package org.apache.jcs.auxiliary.remote.http.client;

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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

        mockDispatcher.setupRemoteCacheResponse = null;

        // DO WORK
        ICacheElement<String, String> result = client.get( cacheName, key );

        // VERIFY
        assertNull( "Wrong result.", result );
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_GET, mockDispatcher.lastRemoteCacheRequest
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

        ICacheElement<String, String> expected = new CacheElement( cacheName, key, "value" );
        RemoteCacheResponse remoteHttpCacheResponse = new RemoteCacheResponse();
        remoteHttpCacheResponse.getPayload().put( key, expected );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        ICacheElement<String, String> result = client.get( cacheName, key );

        // VERIFY
        assertEquals( "Wrong result.", expected, result );
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_GET, mockDispatcher.lastRemoteCacheRequest
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String pattern = "key";

        ICacheElement<String, String> expected = new CacheElement( cacheName, "key", "value" );
        RemoteCacheResponse remoteHttpCacheResponse = new RemoteCacheResponse();
        remoteHttpCacheResponse.getPayload().put( "key", expected );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        Map result = client.getMatching( cacheName, pattern );

        // VERIFY
        assertEquals( "Wrong result.", expected, result.get( "key" ) );
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_GET_MATCHING,
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        Set keys = Collections.EMPTY_SET;

        ICacheElement<String, String> expected = new CacheElement( cacheName, "key", "value" );
        RemoteCacheResponse remoteHttpCacheResponse = new RemoteCacheResponse();
        remoteHttpCacheResponse.getPayload().put( "key", expected );

        mockDispatcher.setupRemoteCacheResponse = remoteHttpCacheResponse;

        // DO WORK
        Map result = client.getMultiple( cacheName, keys );

        // VERIFY
        assertEquals( "Wrong result.", expected, result.get( "key" ) );
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_GET_MULTIPLE,
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";
        String key = "key";

        // DO WORK
        client.remove( cacheName, key );

        // VERIFY
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_REMOVE, mockDispatcher.lastRemoteCacheRequest
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

        // DO WORK
        client.removeAll( cacheName );

        // VERIFY
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_REMOVE_ALL, mockDispatcher.lastRemoteCacheRequest
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

        ICacheElement<String, String> element = new CacheElement( cacheName, "key", "value" );

        // DO WORK
        client.update( element );

        // VERIFY
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_UPDATE, mockDispatcher.lastRemoteCacheRequest
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
        RemoteHttpCacheClient client = new RemoteHttpCacheClient( attributes );

        MockRemoteCacheDispatcher mockDispatcher = new MockRemoteCacheDispatcher();
        client.setRemoteDispatcher( mockDispatcher );

        String cacheName = "test";

        // DO WORK
        client.dispose( cacheName );

        // VERIFY
        assertEquals( "Wrong type.", RemoteCacheRequest.REQUEST_TYPE_DISPOSE, mockDispatcher.lastRemoteCacheRequest
            .getRequestType() );
    }
}
