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

import java.io.Serializable;
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
    /** Verify that the service is called. */
    public void testProcessRequest_Get()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteCacheRequest request = RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastGetKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMatching()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;
        RemoteCacheRequest request = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong pattern.", pattern, remoteHttpCacheService.lastGetMatchingPattern );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMultiple()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Set keys = Collections.EMPTY_SET;
        long requesterId = 2;
        RemoteCacheRequest request = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                  requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong keys.", keys, remoteHttpCacheService.lastGetMultipleKeys );

    }

    /** Verify that the service is called. */
    public void testProcessRequest_Update()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        CacheElement element = new CacheElement( cacheName, key, null );
        RemoteCacheRequest request = RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong object.", element, remoteHttpCacheService.lastUpdate );
    }
    
    /** Verify that the service is called. */
    public void testProcessRequest_Remove()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteCacheRequest request = RemoteCacheRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastRemoveKey );
    }
    
    /** Verify that the service is called. */
    public void testProcessRequest_RemoveAll()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "testRemoveALl";
        long requesterId = 2;
        RemoteCacheRequest request = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // DO WORK
        RemoteCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong cacheName.", cacheName, remoteHttpCacheService.lastRemoveAllCacheName );
    }
}
