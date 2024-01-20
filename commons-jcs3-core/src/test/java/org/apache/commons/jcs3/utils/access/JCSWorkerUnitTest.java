package org.apache.commons.jcs3.utils.access;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test cases for the JCS worker.
 */
public class JCSWorkerUnitTest
{

    /**
     * Test basic worker functionality.  This is a serial not a concurrent test.
     * <p>
     * Just verify that the worker will go to the cache before asking the helper.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleGet()
        throws Exception
    {
        final JCSWorker<String, Long> cachingWorker = new JCSWorker<>( "example region" );

        // This is the helper.
        final JCSWorkerHelper<Long> helper = new AbstractJCSWorkerHelper<>()
        {
            int timesCalled;

            @Override
            public Long doWork()
            {
                return Long.valueOf( ++timesCalled );
            }
        };

        final String key = "abc";

        final Long result = cachingWorker.getResult( key, helper );
        assertEquals( "Called the wrong number of times", Long.valueOf( 1 ), result );

        // should get it from the cache.
        final Long result2 = cachingWorker.getResult( key, helper );
        assertEquals( "Called the wrong number of times", Long.valueOf( 1 ), result2 );
    }

}
