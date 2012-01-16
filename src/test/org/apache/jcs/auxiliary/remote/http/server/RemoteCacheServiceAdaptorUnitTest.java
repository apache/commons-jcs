package org.apache.jcs.auxiliary.remote.http.server;

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

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.MockRemoteCacheService;
import org.apache.jcs.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.jcs.engine.CacheElement;

/** Unit tests for the adaptor. */
public class RemoteCacheServiceAdaptorUnitTest
    extends TestCase
{
    /** Verify that we balk and return an error. */
    public void testProcessRequest_null()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        RemoteCacheRequest<String, String> request = null;

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertTrue( "Should have 'The request is null' in the errorMessage", result.getErrorMessage().indexOf( "The request is null" ) != -1 );
        assertTrue( "Should have 'The request is null' in the toString", result.toString().indexOf( "The request is null" ) != -1 );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_Get()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String key = "key";
        long requesterId = 2;
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastGetKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMatching()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong pattern.", pattern, remoteHttpCacheService.lastGetMatchingPattern );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMultiple()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Set<String> keys = Collections.emptySet();
        long requesterId = 2;
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong keys.", keys, remoteHttpCacheService.lastGetMultipleKeys );

    }

    /** Verify that the service is called. */
    public void testProcessRequest_Update()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String key = "key";
        long requesterId = 2;
        CacheElement<String, String> element = new CacheElement<String, String>( cacheName, key, null );
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong object.", element, remoteHttpCacheService.lastUpdate );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_Remove()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String key = "key";
        long requesterId = 2;
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastRemoveKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_RemoveAll()
    {
        // SETUP
        RemoteCacheServiceAdaptor<String, String> adaptor = new RemoteCacheServiceAdaptor<String, String>();

        MockRemoteCacheService<String, String> remoteHttpCacheService = new MockRemoteCacheService<String, String>();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "testRemoveALl";
        long requesterId = 2;
        RemoteCacheRequest<String, String> request = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // DO WORK
        RemoteCacheResponse<String, String> result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong cacheName.", cacheName, remoteHttpCacheService.lastRemoveAllCacheName );
    }
}
