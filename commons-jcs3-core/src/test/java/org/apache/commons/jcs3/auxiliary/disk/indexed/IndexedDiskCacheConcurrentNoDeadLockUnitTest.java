package org.apache.commons.jcs3.auxiliary.disk.indexed;

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

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.jcs3.JCS;
import org.junit.After;
import org.junit.Before;

/**
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 */
public class IndexedDiskCacheConcurrentNoDeadLockUnitTest
{
    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     */
    public static Test suite()
    {
        final ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest(new TestCase("testIndexedDiskCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 1, 200, 1 );
            }
        });

        suite.addTest(new TestCase("testIndexedDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 2 );
            }
        });

        suite.addTest(new TestCase("testIndexedDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 3 );
            }
        });

        suite.addTest(new TestCase("testIndexedDiskCache4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 4 );
            }
        });

        suite.addTest(new TestCase("testIndexedDiskCache5" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                IndexedDiskCacheRandomConcurrentTestUtil.runTestForRegion( "indexedRegion4", 10000, 50000, 5 );
            }
        });

        return suite;
    }

    /**
     * Test setup
     */
    @Before
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

    /**
     * Test tearDown. Dispose of the cache.
     */
    @After
    public void tearDown()
    {
        JCS.shutdown();
    }
}
