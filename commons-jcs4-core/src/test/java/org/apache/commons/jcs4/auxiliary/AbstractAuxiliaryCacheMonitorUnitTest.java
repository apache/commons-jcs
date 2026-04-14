package org.apache.commons.jcs4.auxiliary;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Basic unit tests for the cache monitor.
 */
class AbstractAuxiliaryCacheMonitorUnitTest
{
    /**
     * Test that the cache monitor survives runtime exceptions in doWork()
     *
     * @throws Exception
     */
    @Test
    void testCacheMonitorResilience() throws Exception
    {
        // reduce the monitor idle period between 'fix' attempts for testing purposes
        MockCacheMonitor.setIdle(100L);

        final MockCacheMonitor monitor = new MockCacheMonitor();
        monitor.start();
        monitor.notifyError();
        Thread.sleep(200L);
        monitor.notifyError();
        Thread.sleep(200L);

        // verify that the monitor has been called multiple times
        assertTrue(1 < monitor.doWorkCalls, "Should have multiple calls " + monitor.doWorkCalls);

        monitor.notifyShutdown();
        Thread.sleep(200L);
        // verify that the monitor shutdown has been called once
        assertEquals(1, monitor.disposeCalls, "Should have one call");
    }

    // used to reduce the monitor idle period between 'fix' attempts for testing purposes
    private static class MockCacheMonitor extends AbstractAuxiliaryCacheMonitor
    {
        protected int doWorkCalls = 0;
        protected int disposeCalls = 0;

        public static void setIdle(long idlePeriod)
        {
            AbstractAuxiliaryCacheMonitor.idlePeriod = idlePeriod;
        }

        public MockCacheMonitor()
        {
            super("test");
        }

        @Override
        protected void dispose()
        {
            disposeCalls++;
        }

        @Override
        protected void doWork() throws Exception
        {
            doWorkCalls++;
            throw new NullPointerException("test");
        }
    }
}
