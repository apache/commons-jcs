package org.apache.commons.jcs.auxiliary.disk.indexed;

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
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.MockElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs.utils.timing.SleepUtil;

import java.io.IOException;

/** Unit tests for the manager */
public class IndexedDiskCacheManagerUnitTest
    extends TestCase
{
    /**
     * Verify that the disk cache has the event logger
     * @throws IOException
     */
    public void testGetCache_normal()
        throws IOException
    {
        // SETUP
        String cacheName = "testGetCache_normal";
        IndexedDiskCacheAttributes defaultCacheAttributes = new IndexedDiskCacheAttributes();
        defaultCacheAttributes.setDiskPath( "target/IndexedDiskCacheManagerUnitTest" );

        ICacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        MockElementSerializer elementSerializer = new MockElementSerializer();

        String key = "myKey";
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( "test", key, "MyValue" );

        IndexedDiskCacheManager manager = IndexedDiskCacheManager.getInstance( defaultCacheAttributes,
                                                                               cacheEventLogger, elementSerializer );

        // DO WORK
        IndexedDiskCache<String, String> cache = manager.getCache( cacheName );

        cache.update( cacheElement );
        SleepUtil.sleepAtLeast( 100 );
        cache.get( key );

        // VERIFY
        assertEquals( "wrong cacheEventLogger", cacheEventLogger, cache.getCacheEventLogger() );
        assertEquals( "wrong elementSerializer", elementSerializer, cache.getElementSerializer() );
        assertEquals( "Wrong serialize count", elementSerializer.serializeCount, 1 );
        assertEquals( "Wrong deSerialize count", elementSerializer.deSerializeCount, 1 );
    }
}
