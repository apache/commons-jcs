package org.apache.commons.jcs4.utils.threadpool;

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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.jcs4.utils.props.PropertyLoader;
import org.junit.jupiter.api.Test;

/**
 * Verify that the manager can create pools as intended by the default and
 * specified file names.
 */
class ThreadPoolManagerUnitTest
{

    /**
     * Make sure it can load a default cache.ccf file
     */
    @Test
    void testDefaultConfig()
    {
        final Properties props = PropertyLoader.loadProperties( "thread_pool.properties" );
        ThreadPoolManager.setProps( props );
        final ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        final ExecutorService pool = mgr.getExecutorService( "test1" );
        assertNotNull( pool );
    }

    /**
     * Gets a couple pools by name and then see if they are in the list.
     */
    @Test
    void testGetPoolNames()
    {
        final ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        final String poolName1 = "testGetPoolNames1";
        mgr.getExecutorService( poolName1 );

        final String poolName2 = "testGetPoolNames2";
        mgr.getExecutorService( poolName2 );

        final Set<String> names = mgr.getPoolNames();
        assertTrue( names.contains( poolName1 ), "Should have name in list." );
        assertTrue( names.contains( poolName2 ), "Should have name in list." );
    }

    /**
     * Make sure it can load a certain configuration
     */
    @Test
    void testSpecialConfig()
    {
        final Properties props = PropertyLoader.loadProperties( "thread_pool.properties" );
        ThreadPoolManager.setProps( props );
        final ThreadPoolManager mgr = ThreadPoolManager.getInstance();
        assertNotNull( mgr );

        final ExecutorService pool = mgr.getExecutorService( "aborttest" );
        assertNotNull( pool );
    }
}
