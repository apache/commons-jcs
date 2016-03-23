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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheService;
import org.apache.commons.jcs.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheResponse;
import org.apache.commons.jcs.engine.CacheElement;

/** Unit tests for the servlet. */
public class RemoteHttpCacheServletUnitTest
    extends TestCase
{
    private RemoteHttpCacheServlet servlet;
    private MockRemoteCacheService<Serializable, Serializable> remoteHttpCacheService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        servlet = new RemoteHttpCacheServlet();
        servlet.init(null);

        remoteHttpCacheService = new MockRemoteCacheService<Serializable, Serializable>();
        servlet.setRemoteCacheService( remoteHttpCacheService );
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        servlet.destroy();
        super.tearDown();
    }

    /** Verify that we balk and return an error. */
    public void testProcessRequest_null()
    {
        RemoteCacheRequest<Serializable, Serializable> request = null;

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertTrue( "Should have 'The request is null' in the errorMessage", result.getErrorMessage().indexOf( "The request is null" ) != -1 );
        assertTrue( "Should have 'The request is null' in the toString", result.toString().indexOf( "The request is null" ) != -1 );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_Get()
    {
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastGetKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMatching()
    {
        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong pattern.", pattern, remoteHttpCacheService.lastGetMatchingPattern );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMultiple()
    {
        String cacheName = "test";
        Set<Serializable> keys = Collections.emptySet();
        long requesterId = 2;
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong keys.", keys, remoteHttpCacheService.lastGetMultipleKeys );

    }

    /** Verify that the service is called. */
    public void testProcessRequest_Update()
    {
        String cacheName = "test";
        String key = "key";
        long requesterId = 2;
        CacheElement<Serializable, Serializable> element = new CacheElement<Serializable, Serializable>( cacheName, key, null );
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong object.", element, remoteHttpCacheService.lastUpdate );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_Remove()
    {
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastRemoveKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_RemoveAll()
    {
        String cacheName = "testRemoveALl";
        long requesterId = 2;
        RemoteCacheRequest<Serializable, Serializable> request = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // DO WORK
        RemoteCacheResponse<Object> result = servlet.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong cacheName.", cacheName, remoteHttpCacheService.lastRemoveAllCacheName );
    }
}
