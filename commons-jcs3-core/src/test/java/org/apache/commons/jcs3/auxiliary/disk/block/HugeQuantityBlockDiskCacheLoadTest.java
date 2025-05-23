package org.apache.commons.jcs3.auxiliary.disk.block;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;
import org.apache.commons.jcs3.utils.timing.SleepUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Put a few hundred thousand entries in the block disk cache.
 */
class HugeQuantityBlockDiskCacheLoadTest
{

    /**
     * Measure memory used by the VM.
     * @return long
     * @throws InterruptedException
     */
    protected long measureMemoryUse()
        throws InterruptedException
    {
        System.gc();
        Thread.sleep( 3000 );
        System.gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Test setup
     */
    @BeforeEach
    void setUp()
    {
        JCS.setConfigFilename( "/TestBlockDiskCacheHuge.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more than the size of the
     * memory cache, so items should spool to disk.
     *
     * @throws Exception If an error occurs
     */
    @Test
    void testLargeNumberOfItems()
        throws Exception
    {
        final int items = 300000;
        final String region = "testCache1";

        System.out.println( "--------------------------" );
        final long initialMemory = measureMemoryUse();
        System.out.println( "Before getting JCS: " + initialMemory );

        final CacheAccess<String, String> jcs = JCS.getInstance( region );
        jcs.clear();

        try
        {
            final ElapsedTimer timer = new ElapsedTimer();
            System.out.println( "Start: " + measureMemoryUse() );

            // Add items to cache
            for ( int i = 0; i < items; i++ )
            {
                jcs.put( i + ":key", region + " data " + i );
            }

            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            System.out.println( "After put: " + measureMemoryUse() );

            Thread.sleep( 5000 );

            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            System.out.println( "After wait: " + measureMemoryUse() );

            for ( int i = 0; i < 10; i++ )
            {
                SleepUtil.sleepAtLeast( 3000 );
                System.out.println( "--------------------------" );
                System.out.println( "After sleep. " + timer.getElapsedTimeString() + " memory used = "
                    + measureMemoryUse() );
                System.out.println( jcs.getStats() );
            }

            // Test that all items are in cache
            System.out.println( "--------------------------" );
            System.out.println( "Retrieving all." );
            for ( int i = 0; i < items; i++ )
            {
                //System.out.print(  "\033[s" );
                final String value = jcs.get( i + ":key" );
                if ( i % 1000 == 0 )
                {
                    //System.out.print(  "\033[r" );
                    System.out.println( i + " " );
                }
                assertEquals( region + " data " + i, value, "Wrong value returned." );
            }
            final long aftetGet = measureMemoryUse();
            System.out.println( "After get: " + aftetGet + " diff = " + ( aftetGet - initialMemory ) );

        }
        finally
        {
            // dump the stats to the report
            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            final long endMemory = measureMemoryUse();
            System.out.println( "End: " + endMemory + " diff = " + ( endMemory - initialMemory ) );
        }
    }
}
