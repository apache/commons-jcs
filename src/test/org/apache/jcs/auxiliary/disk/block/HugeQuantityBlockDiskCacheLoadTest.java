package org.apache.jcs.auxiliary.disk.block;

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

import org.apache.jcs.JCS;
import org.apache.jcs.utils.timing.ElapsedTimer;
import org.apache.jcs.utils.timing.SleepUtil;

/**
 * Put a few hundred thousand entries in the block disk cache.
 * <p.
 * @author Aaron Smuts
 *
 */
public class HugeQuantityBlockDiskCacheLoadTest
    extends TestCase
{

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestBlockDiskCacheHuge.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     *
     * @param region
     *            Name of the region to access
     *
     * @exception Exception
     *                If an error occurs
     */
    public void testLargeNumberOfItems()
        throws Exception
    {
        int items = 300000;
        String region = "testCache1";

        System.out.println( "--------------------------" );
        long initialMemory = measureMemoryUse();
        System.out.println( "Before getting JCS: " + initialMemory );

        JCS jcs = JCS.getInstance( region );
        jcs.clear();

        try
        {
            ElapsedTimer timer = new ElapsedTimer();
            System.out.println( "Start: " + measureMemoryUse() );

            // Add items to cache
            for ( int i = 0; i <= items; i++ )
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
                System.out.println( "After sleep. " + timer.getElapsedTimeString() + " memory used = " + measureMemoryUse() );
                System.out.println( jcs.getStats() );
            }

            // Test that all items are in cache
            System.out.println( "--------------------------" );
            System.out.println( "Retrieving all." );
            for ( int i = 0; i <= items; i++ )
            {
                //System.out.print(  "\033[s" );
                String value = (String) jcs.get( i + ":key" );
                if( i % 1000 == 0 )
                {
                    //System.out.print(  "\033[r" );
                    System.out.println(  i + " ");
                }
                assertEquals( "Wrong value returned.", region + " data " + i, value );
            }
            long aftetGet = measureMemoryUse();
            System.out.println( "After get: " + aftetGet + " diff = " + (aftetGet - initialMemory));

        }
        finally
        {
            // dump the stats to the report
            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            long endMemory = measureMemoryUse();
            System.out.println( "End: " + endMemory + " diff = " + (endMemory - initialMemory) );
        }
    }

    /**
     * Measure memory used by the VM.
     *
     * @return
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
}
