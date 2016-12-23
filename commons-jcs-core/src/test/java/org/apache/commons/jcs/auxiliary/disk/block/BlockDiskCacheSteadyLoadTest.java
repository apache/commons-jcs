package org.apache.commons.jcs.auxiliary.disk.block;

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

import java.text.DecimalFormat;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.auxiliary.disk.DiskTestObject;

/**
 * This allows you to put thousands of large objects into the disk cache and to force removes to
 * trigger optimizations along the way.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskCacheSteadyLoadTest
    extends TestCase
{
    /** String for separating log entries. */
    private static final String LOG_DIVIDER = "---------------------------";

    /** the runtime. */
    private static Runtime rt = Runtime.getRuntime();

    /** The decimal format to use int he logs. */
    private static DecimalFormat format = new DecimalFormat( "#,###" );

    /**
     * Insert 2000 wait 1 second, repeat. Average 1000 / sec.
     * <p>
     * @throws Exception
     */
    public void testRunSteadyLoadTest()
        throws Exception
    {
        JCS.setConfigFilename( "/TestBlockDiskCacheSteadyLoad.ccf" );

        logMemoryUsage();

        int numPerRun = 250;
        long pauseBetweenRuns = 1000;
        int runCount = 0;
        int runs = 1000;
        int upperKB = 50;

        CacheAccess<String, DiskTestObject> jcs = JCS.getInstance( ( numPerRun / 2 ) + "aSecond" );

//        ElapsedTimer timer = new ElapsedTimer();
        int numToGet = numPerRun * ( runs / 10 );
        for ( int i = 0; i < numToGet; i++ )
        {
            jcs.get( String.valueOf( i ) );
        }
//        System.out.println( LOG_DIVIDER );
//        System.out.println( "After getting " + numToGet );
//        System.out.println( "Elapsed " + timer.getElapsedTimeString() );
        logMemoryUsage();

        jcs.clear();
        Thread.sleep( 3000 );
//        System.out.println( LOG_DIVIDER );
//        System.out.println( "Start putting" );

//        long totalSize = 0;
        int totalPut = 0;

        Random random = new Random( 89 );
        while ( runCount < runs )
        {
            runCount++;
            for ( int i = 0; i < numPerRun; i++ )
            {
                // 1/2 upper to upperKB-4 KB
                int kiloBytes = Math.max( upperKB / 2, random.nextInt( upperKB ) );
                int bytes = ( kiloBytes ) * 1024;
//                totalSize += bytes;
                totalPut++;
                DiskTestObject object = new DiskTestObject( Integer.valueOf( i ), new byte[bytes] );
                jcs.put( String.valueOf( totalPut ), object );
            }

            // get half of those inserted the previous run
            if ( runCount > 1 )
            {
                for ( int j = ( ( totalPut - numPerRun ) - ( numPerRun / 2 ) ); j < ( totalPut - numPerRun ); j++ )
                {
                    jcs.get( String.valueOf( j ) );
                }
            }

            // remove half of those inserted the previous run
            if ( runCount > 1 )
            {
                for ( int j = ( ( totalPut - numPerRun ) - ( numPerRun / 2 ) ); j < ( totalPut - numPerRun ); j++ )
                {
                    jcs.remove( String.valueOf( j ) );
                }
            }


            Thread.sleep( pauseBetweenRuns );
            if ( runCount % 100 == 0 )
            {
//                System.out.println( LOG_DIVIDER );
//                System.out.println( "Elapsed " + timer.getElapsedTimeString() );
//                System.out.println( "Run count: " + runCount + " Average size: " + ( totalSize / totalPut ) + "\n"
//                    + jcs.getStats() );
                logMemoryUsage();
            }
        }

        Thread.sleep( 3000 );
//        System.out.println( jcs.getStats() );
        logMemoryUsage();

        Thread.sleep( 10000 );
//        System.out.println( jcs.getStats() );
        logMemoryUsage();

        System.gc();
        Thread.sleep( 3000 );
        System.gc();
//        System.out.println( jcs.getStats() );
        logMemoryUsage();
    }

    /**
     * Logs the memory usage.
     */
    private static void logMemoryUsage()
    {
        long byte2MB = 1024 * 1024;
        long total = rt.totalMemory() / byte2MB;
        long free = rt.freeMemory() / byte2MB;
        long used = total - free;
        System.out.println( LOG_DIVIDER );
        System.out.println( "Memory:" + " Used:" + format.format( used ) + "MB" + " Free:" + format.format( free )
            + "MB" + " Total:" + format.format( total ) + "MB" );
    }
}
