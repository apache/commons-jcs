package org.apache.commons.jcs.auxiliary.disk.file;

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
import org.apache.commons.jcs.auxiliary.MockCacheEventLogger;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.control.MockCompositeCacheManager;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;

/** Verify that the factory works */
public class FileDiskCacheFactoryUnitTest
    extends TestCase
{
    /** Verify that we can get a cache from the manager via the factory */
    public void testCreateCache_Normal()
    {
        // SETUP
        String cacheName = "testCreateCache_Normal";
        FileDiskCacheAttributes cattr = new FileDiskCacheAttributes();
        cattr.setCacheName( cacheName );
        cattr.setDiskPath( "target/test-sandbox/FileDiskCacheFactoryUnitTest" );

        ICompositeCacheManager cacheMgr = new MockCompositeCacheManager();
        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        IElementSerializer elementSerializer = new MockElementSerializer();

        FileDiskCacheFactory factory = new FileDiskCacheFactory();

        // DO WORK
        FileDiskCache<String, String> result = factory.createCache( cattr, cacheMgr, cacheEventLogger,
                                                                    elementSerializer );

        // VERIFY
        assertNotNull( "Should have a disk cache", result );
        assertEquals( "Should have a disk cache with a serializer", elementSerializer, result.getElementSerializer() );
    }
}
