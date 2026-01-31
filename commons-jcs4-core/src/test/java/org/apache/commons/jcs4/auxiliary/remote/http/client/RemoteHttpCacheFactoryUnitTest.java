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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs4.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.remote.http.client.behavior.IRemoteHttpCacheClient;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests for the manager. */
class RemoteHttpCacheFactoryUnitTest
{
    /** Verify that we get the default. */
    @Test
    void testCreateRemoteHttpCacheClient_Bad()
    {
        // SETUP
        final String remoteHttpClientClassName = "junk";
        final RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        cattr.setRemoteHttpClientClassName( remoteHttpClientClassName );

        final RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();

        // DO WORK
        final IRemoteHttpCacheClient<String, String> result = factory.createRemoteHttpCacheClientForAttributes( cattr );

        // VEIFY
        Assertions.assertNotNull( result, "Should have a cache." );
        assertInstanceOf( RemoteHttpCacheClient.class, result, "Wrong default." );
        assertTrue( ( (RemoteHttpCacheClient<String, String>) result ).isInitialized(), "Should be initialized" );
    }

    /** Verify that we get the default. */
    @Test
    void testCreateRemoteHttpCacheClient_default()
    {
        // SETUP
        final RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        final RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();

        // DO WORK
        final IRemoteHttpCacheClient<String, String> result = factory.createRemoteHttpCacheClientForAttributes( cattr );

        // VEIFY
        assertNotNull( result, "Should have a cache." );
        assertInstanceOf( RemoteHttpCacheClient.class, result, "Wrong default." );
    }

    /** Verify that we get a cache no wait. */
    @Test
    void testGetCache_normal()
    {
        // SETUP
        final ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        assertNotNull( cacheMgr, "Should have a manager." );
        final ICacheEventLogger cacheEventLogger = null;
        final IElementSerializer elementSerializer = null;

        final RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        assertNotNull( cattr, "Should have attributes." );
        final RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();
        assertNotNull( factory, "Should have a factory." );

        // DO WORK
        final AuxiliaryCache<String, String> result = factory.createCache(cattr, cacheMgr, cacheEventLogger, elementSerializer);

        // VERIFY
        assertNotNull( result, "Should have a cache." );
    }
}
