package org.apache.commons.jcs.utils.threadpool;

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
import org.apache.commons.jcs.utils.props.PropertyLoader;
import org.apache.commons.jcs.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Verify that the manager can create pools as intended by the default and
 * specified file names.
 *
 * @author asmuts
 */
public class ThreadPoolManagerUnitTest
    extends TestCase
{

    /**
     * Make sure it can load a default cache.ccf file
     */
    public void testDefaultConfig()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPoolExecutor pool = mgr.getPool( "test1" );
        assertNotNull( pool );

        int poolSize = pool.getPoolSize();
        int expectedPoolSize = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.test1.startUpSize" ) );
        assertEquals( poolSize, expectedPoolSize );

        // int qs = ((BoundedBuffer)pool.getQueue()).size();

        int max = pool.getMaximumPoolSize();

        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.test1.maximumPoolSize" ) );
        assertEquals(expected, max );
    }

    /**
     * Try to get an undefined pool from an existing default file.
     */
    public void testDefaultConfigUndefinedPool()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPoolExecutor pool = mgr.getPool( "doesnotexist" );
        assertNotNull( pool );

        int max = pool.getMaximumPoolSize();

        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.default.maximumPoolSize" ) );
        assertEquals( expected, max );
    }

    /**
     * Makes ure we can get a non existent pool from the non exitent config
     * file.
     */
    public void testNonExistentConfigFile()
    {
        ThreadPoolManager.setPropsFileName( "somefilethatdoesntexist" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ThreadPoolExecutor pool = mgr.getPool( "doesntexist" );
        assertNotNull( "Should have gotten back a pool configured like the default", pool );

        int max = pool.getMaximumPoolSize();

        // it will load from the default file
        int expected = Integer.parseInt( PropertyLoader.loadProperties( "cache.ccf" )
            .getProperty( "thread_pool.default.maximumPoolSize" ) );

        assertEquals( expected, max );
    }

    /**
     * Get a couple pools by name and then see if they are in the list.
     *
     */
    public void testGetPoolNames()
    {
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        String poolName1 = "testGetPoolNames1";
        mgr.getPool( poolName1 );

        String poolName2 = "testGetPoolNames2";
        mgr.getPool( poolName2 );

        ArrayList<String> names = mgr.getPoolNames();
        assertTrue( "Should have name in list.", names.contains( poolName1 ) );
        assertTrue( "Should have name in list.", names.contains( poolName2 ) );
    }

    /**
     * Verify that the wait policy gets set correctly.
     *
     * Switched off as the POLICY_WAIT is not supported by the javax.concurrent package
     */
    public void OFFtestWaitPolicyConfig()
    {
        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        // force config from new props file
        mgr.configure();
        assertNotNull( mgr );

        ThreadPoolExecutor pool = mgr.getPool( "waittest" );
        assertNotNull( "Should have gotten back a pool.", pool );

        int max = pool.getMaximumPoolSize();

        // it will load from the default file
        int expected = Integer.parseInt( PropertyLoader.loadProperties( "thread_pool.properties" )
            .getProperty( "thread_pool.waittest.maximumPoolSize" ) );

        assertEquals( "Max is wrong", expected, max );

        PoolConfiguration config = mgr.loadConfig( "thread_pool.waittest" );

        assertEquals( "Policy is wrong.", WhenBlockedPolicy.WAIT, config.getWhenBlockedPolicy() );
    }

    /**
     * Verify that if we specify not to use a buffer boundary that we get a
     * linked queue.
     *
     */
//    public void testNoBoundary()
//    {
//        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
//        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
//        // force config from new props file
//        mgr.configure();
//        assertNotNull( mgr );
//
//        ThreadPoolExecutor pool = mgr.getPool( "nobound" );
//        assertNotNull( "Should have gotten back a pool.", pool );
//
//        assertTrue( "Should have a linked queue and not a bounded buffer.", pool.getQueue() instanceof LinkedQueue );
//    }

    /**
     * Verify that if we specify useBoundary=true that we get a BoundedBuffer.
     *
     */
//    public void testWithBoundary()
//    {
//        // SETUP
//        ThreadPoolManager.setPropsFileName( "thread_pool.properties" );
//        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
//        // force config from new props file
//        mgr.configure();
//        assertNotNull( mgr );
//
//        // DO WORK
//        ThreadPoolExecutor pool = mgr.getPool( "withbound" );
//
//        // VERIFY
//        assertNotNull( "Should have gotten back a pool.", pool );
//        assertTrue( "Should have a BoundedBuffer and not a linked queue.", pool.getQueue() instanceof BoundedBuffer );
//    }
}
