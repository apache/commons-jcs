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

import junit.framework.TestCase;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * This is for manually testing the defrag process.
 */
public class IndexedDiskCacheDefragPerformanceTest
    extends TestCase
{
    /** For readability */
    private static final String LOG_DIVIDER = "---------------------------";

    /** total to test with */
    private static final int TOTAL_ELEMENTS = 30000;

    /** time to wait */
    private static final long SLEEP_TIME_DISK = 8000;

    /** how often to log */
    private static final int LOG_INCREMENT = 5000;

    /** for getting memory usage */
    private static Runtime rt = Runtime.getRuntime();

    /** for displaying memory usage */
    private static DecimalFormat format = new DecimalFormat( "#,###" );

    /**
     * @throws Exception
     */
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

    /**
     * @throws Exception
     */
    private static void runRealTimeOptimizationTest()
        throws Exception
    {
        JCS.setConfigFilename( "/TestDiskCacheDefragPerformance.ccf" );
        CacheAccess<Integer, Tile> jcs = JCS.getInstance( "defrag" );

        Tile tile;
        System.out.println( "Cache Defrag Test" );

        Random random = new Random( 89 );
        for ( int i = 0; i < TOTAL_ELEMENTS; i++ )
        {
            int bytes = random.nextInt( 20 );
            // 4-24 KB
            tile = new Tile( Integer.valueOf( i ), new byte[( bytes + 4 ) * 1024] );
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
        System.out.println( "Sleeping for a a minute." );
        Thread.sleep( 60000 );

        System.out.println( LOG_DIVIDER );
        System.out.println( "Stats prior to dispose " + jcs.getStats() );

        jcs.dispose();
        System.out.println( LOG_DIVIDER );
        System.out.println( "Stats after dispose " + jcs.getStats() );
        System.out.println( "Done testing." );
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

    /**
     * Resembles a cached image.
     */
    private static class Tile
        implements Serializable
    {
        /** Don't change */
        private static final long serialVersionUID = 1L;

        /**
         * Key
         */
        public Integer id;

        /**Byte size
         *
         */
        public byte[] imageBytes;

        /**
         * @param id
         * @param imageBytes
         */
        public Tile( Integer id, byte[] imageBytes )
        {
            this.id = id;
            this.imageBytes = imageBytes;
        }
    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        try
        {
            IndexedDiskCacheDefragPerformanceTest tester = new IndexedDiskCacheDefragPerformanceTest();
            tester.testRealTimeOptimization();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
