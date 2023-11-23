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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.remote.MockRemoteCacheService;
import org.apache.commons.jcs3.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs3.engine.CacheElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Unit tests for the servlet. */
public class RemoteHttpCacheServletUnitTest
{
    private RemoteHttpCacheServlet servlet;
    private MockRemoteCacheService<Serializable, Serializable> remoteHttpCacheService;

    @Before
    public void setUp() throws Exception
    {
        servlet = new RemoteHttpCacheServlet();
        servlet.init(null);

        remoteHttpCacheService = new MockRemoteCacheService<>();
        servlet.setRemoteCacheService( remoteHttpCacheService );
    }

    @After
    public void tearDown() throws Exception
    {
        servlet.destroy();
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_Get()
    {
        final String cacheName = "test";
        final Serializable key = "key";
        final long requesterId = 2;
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastGetKey );
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_GetMatching()
    {
        final String cacheName = "test";
        final String pattern = "pattern";
        final long requesterId = 2;
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                  requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong pattern.", pattern, remoteHttpCacheService.lastGetMatchingPattern );
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_GetMultiple()
    {
        final String cacheName = "test";
        final Set<Serializable> keys = Collections.emptySet();
        final long requesterId = 2;
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                  requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong keys.", keys, remoteHttpCacheService.lastGetMultipleKeys );

    }

    /** Verify that we balk and return an error. */
    @Test
    public void testProcessRequest_null()
    {
        final RemoteCacheRequest<Serializable, Serializable> request = null;

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertTrue( "Should have 'The request is null' in the errorMessage", result.getErrorMessage().indexOf( "The request is null" ) != -1 );
        assertTrue( "Should have 'The request is null' in the toString", result.toString().indexOf( "The request is null" ) != -1 );
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_Remove()
    {
        final String cacheName = "test";
        final Serializable key = "key";
        final long requesterId = 2;
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastRemoveKey );
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_RemoveAll()
    {
        final String cacheName = "testRemoveALl";
        final long requesterId = 2;
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong cacheName.", cacheName, remoteHttpCacheService.lastRemoveAllCacheName );
    }

    /** Verify that the service is called. */
    @Test
    public void testProcessRequest_Update()
    {
        final String cacheName = "test";
        final String key = "key";
        final long requesterId = 2;
        final CacheElement<Serializable, Serializable> element = new CacheElement<>( cacheName, key, null );
        final RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // DO WORK
        final RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong object.", element, remoteHttpCacheService.lastUpdate );
    }
}
