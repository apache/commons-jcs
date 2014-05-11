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

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.engine.control.CompositeCacheManager;

/**
 * Test which exercises the indexed disk cache. Runs three threads against the
 * same region.
 *
 * @version $Id: TestDiskCacheConcurrentForDeadLock.java,v 1.2 2005/02/01
 *          00:01:59 asmuts Exp $
 */
public class IndexedDiskCacheConcurrentNoDeadLockUnitTest
    extends TestCase
{
    /**
     * Constructor for the TestDiskCache object.
     *
     * @param testName
     */
    public IndexedDiskCacheConcurrentNoDeadLockUnitTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     *
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { IndexedDiskCacheConcurrentNoDeadLockUnitTest.class.getName() };
        TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     *
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new IndexedDiskCacheRandomConcurrentTestUtil( "testIndexedDiskCache1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 1, 200, 1 );
            }
        } );

        suite.addTest( new IndexedDiskCacheRandomConcurrentTestUtil( "testIndexedDiskCache2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 10000, 50000, 2 );
            }
        } );

        suite.addTest( new IndexedDiskCacheRandomConcurrentTestUtil( "testIndexedDiskCache3" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 10000, 50000, 3 );
            }
        } );

        suite.addTest( new IndexedDiskCacheRandomConcurrentTestUtil( "testIndexedDiskCache4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 10000, 50000, 4 );
            }
        } );

        suite.addTest( new IndexedDiskCacheRandomConcurrentTestUtil( "testIndexedDiskCache5" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runTestForRegion( "indexedRegion4", 10000, 50000, 5 );
            }
        } );

        return suite;
    }

    /**
     * Test setup
     */
    @Override
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheCon.ccf" );
    }

    /**
     * Test tearDown. Dispose of the cache.
     */
    @Override
    public void tearDown()
    {
        try
        {
            CompositeCacheManager cacheMgr = CompositeCacheManager.getInstance();
            cacheMgr.shutDown();
        }
        catch ( Exception e )
        {
            // log.error(e);
        }
    }

}
