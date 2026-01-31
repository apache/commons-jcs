package org.apache.commons.jcs4.auxiliary.disk.indexed;

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

import org.apache.commons.jcs4.JCS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 */
public class IndexedDiskCacheConcurrentNoDeadLockUnitTest
{

    @BeforeEach
    void setUp()
    {
        // Set the configuration file for the cache
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

    @AfterEach
    void tearDown()
    {
        // Dispose of the cache after each test
        JCS.shutdown();
    }

    @Test
    void testIndexedDiskCache1()
        throws Exception
    {
        IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 1, 200, 1 );
    }

    @Test
    void testIndexedDiskCache2()
        throws Exception
    {
        IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 2 );
    }

    @Test
    void testIndexedDiskCache3()
        throws Exception
    {
        IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 3 );
    }

    @Test
    void testIndexedDiskCache4()
        throws Exception
    {
        IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 4 );
    }

    @Test
    void testIndexedDiskCache5()
        throws Exception
    {
        IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 5 );
    }
}
