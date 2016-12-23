package org.apache.commons.jcs.auxiliary.remote.http.client;

import org.apache.commons.jcs.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs.auxiliary.remote.http.client.behavior.IRemoteHttpCacheClient;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

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

/** Unit tests for the manager. */
public class RemoteHttpCacheFactoryUnitTest
    extends TestCase
{
    /** Verify that we get the default. */
    public void testCreateRemoteHttpCacheClient_Bad()
    {
        // SETUP
        String remoteHttpClientClassName = "junk";
        RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        cattr.setRemoteHttpClientClassName( remoteHttpClientClassName );

        RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();

        // DO WORK
        IRemoteHttpCacheClient<String, String> result = factory.createRemoteHttpCacheClientForAttributes( cattr );

        // VEIFY
        assertNotNull( "Should have a cache.", result );
        assertTrue( "Wrong default.", result instanceof RemoteHttpCacheClient );
        assertTrue( "Should be initialized", ((RemoteHttpCacheClient<String, String>)result).isInitialized() );
    }

    /** Verify that we get the default. */
    public void testCreateRemoteHttpCacheClient_default()
    {
        // SETUP
        RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();

        // DO WORK
        IRemoteHttpCacheClient<String, String> result = factory.createRemoteHttpCacheClientForAttributes( cattr );

        // VEIFY
        assertNotNull( "Should have a cache.", result );
        assertTrue( "Wrong default.", result instanceof RemoteHttpCacheClient );
    }

    /** Verify that we get a cache no wait. */
    public void testGetCache_normal()
    {
        // SETUP
        ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        ICacheEventLogger cacheEventLogger = null;
        IElementSerializer elementSerializer = null;

        RemoteHttpCacheAttributes cattr = new RemoteHttpCacheAttributes();
        RemoteHttpCacheFactory factory = new RemoteHttpCacheFactory();

        // DO WORK
        AuxiliaryCache<String, String> result = factory.createCache(cattr, cacheMgr, cacheEventLogger, elementSerializer);

        // VEIFY
        assertNotNull( "Should have a cache.", result );
    }
}
