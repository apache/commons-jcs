package org.apache.commons.jcs3.auxiliary.remote.util;

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

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.jcs3.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs3.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs3.engine.CacheElement;
import org.junit.Test;

/** Unit tests for the request creator. */
public class RemoteCacheRequestFactoryUnitTest
{
    /** Simple test */
    @Test
    public void testCreateGetMatchingRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final String pattern = "pattern";
        final long requesterId = 2;

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET_MATCHING, result.getRequestType() );
    }

    /** Simple test */
    @Test
    public void testCreateGetMultipleRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final Set<Serializable> keys = Collections.emptySet();
        final long requesterId = 2;

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET_MULTIPLE, result.getRequestType() );
    }

    /** Simple test */
    @Test
    public void testCreateGetRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final Serializable key = "key";
        final long requesterId = 2;

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET, result.getRequestType() );
    }

    /** Simple test */
    @Test
    public void testCreateRemoveAllRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final long requesterId = 2;

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.REMOVE_ALL, result.getRequestType() );
    }

    /** Simple test */
    @Test
    public void testCreateRemoveRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final Serializable key = "key";
        final long requesterId = 2;

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result = RemoteCacheRequestFactory
            .createRemoveRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.REMOVE, result.getRequestType() );
    }

    /** Simple test */
    @Test
    public void testCreateUpdateRequest_Normal()
    {
        // SETUP
        final String cacheName = "test";
        final Serializable key = "key";
        final long requesterId = 2;

        final CacheElement<Serializable, Serializable> element =
            new CacheElement<>( cacheName, key, null );

        // DO WORK
        final RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.UPDATE, result.getRequestType() );
    }
}
