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
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.access.TestCacheAccess;

/**
 * This is used by other tests to generate a random load on the disk cache.
 */
public class IndexedDiskCacheRandomConcurrentTestUtil
    extends TestCase
{

    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public IndexedDiskCacheRandomConcurrentTestUtil( String testName )
    {
        super( testName );
    }

    /**
     * Randomly adds items to cache, gets them, and removes them. The range
     * count is more than the size of the memory cache, so items should spool to
     * disk.
     *
     * @param region
     *            Name of the region to access
     * @param range
     * @param numOps
     * @param testNum
     *
     * @throws Exception
     *                If an error occurs
     */
    public void runTestForRegion( String region, int range, int numOps, int testNum )
        throws Exception
    {
        // run a rondom operation test to detect deadlocks
        TestCacheAccess tca = new TestCacheAccess( "/TestDiskCacheCon.ccf" );
        tca.setRegion( region );
        tca.random( range, numOps );

        // make sure a simple put then get works
        // this may fail if the other tests are flooding the disk cache
        CacheAccess<String, String> jcs = JCS.getInstance( region );
        String key = "testKey" + testNum;
        String data = "testData" + testNum;
        jcs.put( key, data );
        String value = jcs.get( key );
        assertEquals( data, value );

    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

}
