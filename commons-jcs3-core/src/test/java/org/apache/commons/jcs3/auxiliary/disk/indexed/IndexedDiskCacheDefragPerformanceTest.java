package org.apache.commons.jcs3.auxiliary.disk.indexed;

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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.junit.Test;

/**
 * This is for manually testing the defrag process.
 */
public class IndexedDiskCacheDefragPerformanceTest
{
    /**
     * Resembles a cached image.
     */
    private static final class Tile
        implements Serializable
    {
        /** Don't change */
        private static final long serialVersionUID = 1L;

        /**
         * Key
         */
        public Integer id;

        /**
         * Byte size
         */
        public byte[] imageBytes;

        /**
         * @param id
         * @param imageBytes
         */
        public Tile( final Integer id, final byte[] imageBytes )
        {
            this.id = id;
            this.imageBytes = imageBytes;
        }
    }

    /** For readability */
    private static final String LOG_DIVIDER = "---------------------------";

    /** Total to test with */
    private static final int TOTAL_ELEMENTS = 30000;

    /** Time to wait */
    private static final long SLEEP_TIME_DISK = 8000;

    /** How often to log */
    private static final int LOG_INCREMENT = 5000;

    /** For getting memory usage */
    private static final Runtime rt = Runtime.getRuntime();

    /** For displaying memory usage */
    private static final DecimalFormat format = new DecimalFormat( "#,###" );

    /**
     * Logs the memory usage.
     */
    private static void logMemoryUsage()
    {
        final long byte2MB = 1024 * 1024;
        final long total = rt.totalMemory() / byte2MB;
        final long free = rt.freeMemory() / byte2MB;
        final long used = total - free;
        System.out.println( LOG_DIVIDER );
        System.out.println( "Memory:" + " Used:" + format.format( used ) + "MB" + " Free:" + format.format( free )
            + "MB" + " Total:" + format.format( total ) + "MB" );
    }

    /**
     * @throws Exception
     */
    private static void runRealTimeOptimizationTest()
        throws Exception
    {
        JCS.setConfigFilename( "/TestDiskCacheDefragPerformance.ccf" );
        final CacheAccess<Integer, Tile> jcs = JCS.getInstance( "defrag" );

        Tile tile;
        System.out.println( "Cache Defrag Test" );

        final Random random = new Random( 89 );
        for ( int i = 0; i < TOTAL_ELEMENTS; i++ )
        {
            final int bytes = random.nextInt( 20 );
            // 4-24 KB
            tile = new Tile( Integer.valueOf( i ), new byte[( bytes + 4 ) * 1024]);
            // images

            jcs.put( tile.id, tile );

            if ( ( i != 0 ) && ( 0 == ( i % 100 ) ) )
            {
                jcs.get( Integer.valueOf( random.nextInt( i ) ) );
            }

            if ( 0 == ( i % LOG_INCREMENT ) )
            {
                System.out.print( i + ", " );
                Thread.sleep( SLEEP_TIME_DISK );
            }
        }

        System.out.println( LOG_DIVIDER );
        System.out.println( "Total elements = " + TOTAL_ELEMENTS );
        System.out.println( "Stats prior to sleeping " + jcs.getStats() );

        // Allow system to settle down
        System.out.println( "Sleeping for a minute." );
        Thread.sleep( 60000 );

        System.out.println( LOG_DIVIDER );
        System.out.println( "Stats prior to dispose " + jcs.getStats() );

        jcs.dispose();
        System.out.println( LOG_DIVIDER );
        System.out.println( "Stats after dispose " + jcs.getStats() );
        System.out.println( "Done testing." );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testRealTimeOptimization()
        throws Exception
    {
        System.out.println( LOG_DIVIDER );
        System.out.println( "JCS DEFRAG PERFORMANCE TESTS" );
        System.out.println( LOG_DIVIDER );
        logMemoryUsage();
        IndexedDiskCacheDefragPerformanceTest.runRealTimeOptimizationTest();
        logMemoryUsage();

        System.out.println( LOG_DIVIDER );
    }
}
