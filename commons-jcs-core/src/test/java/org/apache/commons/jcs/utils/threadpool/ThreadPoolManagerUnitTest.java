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

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.jcs.utils.props.PropertyLoader;

import junit.framework.TestCase;

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
        Properties props = PropertyLoader.loadProperties( "thread_pool.properties" );
        ThreadPoolManager.setProps( props );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ExecutorService pool = mgr.getExecutorService( "test1" );
        assertNotNull( pool );
    }

    /**
     * Make sure it can load a certain configuration
     */
    public void testSpecialConfig()
    {
        Properties props = PropertyLoader.loadProperties( "thread_pool.properties" );
        ThreadPoolManager.setProps( props );
        ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        ExecutorService pool = mgr.getExecutorService( "aborttest" );
        assertNotNull( pool );
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
        mgr.getExecutorService( poolName1 );

        String poolName2 = "testGetPoolNames2";
        mgr.getExecutorService( poolName2 );

        ArrayList<String> names = mgr.getPoolNames();
        assertTrue( "Should have name in list.", names.contains( poolName1 ) );
        assertTrue( "Should have name in list.", names.contains( poolName2 ) );
    }
}
