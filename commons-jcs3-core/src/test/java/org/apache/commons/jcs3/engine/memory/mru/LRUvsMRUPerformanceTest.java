package org.apache.commons.jcs3.engine.memory.mru;

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

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.memory.lru.LRUMemoryCache;
import org.apache.commons.jcs3.log.Log;
import org.junit.jupiter.api.Test;

/**
 * Tests the performance difference between the LRU and the MRU. There should be very little.
 */
public class LRUvsMRUPerformanceTest
{
    /** Ration we want */
    float ratioPut;

    /** Ration we want */
    float ratioGet;

    /** Ration we want */
    float target = 1.20f;

    /** Times to run */
    int loops = 20;

    /** Item per run */
    int tries = 10000;

    /**
     * Runs the test
     */
    public void doWork()
    {

        long start = 0;
        long end = 0;
        long time = 0;
        float tPer = 0;

        long putTotalLRU = 0;
        long getTotalLRU = 0;
        long putTotalMRU = 0;
        long getTotalMRU = 0;

        try
        {

            JCS.setConfigFilename( "/TestMRUCache.ccf" );
            final CacheAccess<String, String> cache = JCS.getInstance( "lruDefined" );
            final CacheAccess<String, String> mru = JCS.getInstance( "mruDefined" );

            System.out.println( "LRU = " + cache );

            for ( int j = 0; j < loops; j++ )
            {

                System.out.println( "Beginning loop " + j );

                String name = "LRU      ";
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalLRU += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalLRU += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = " + time + "; millis per = " + tPer );

                // /////////////////////////////////////////////////////////////
                name = "MRU";
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    mru.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalMRU += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    mru.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalMRU += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = " + time + "; millis per = " + tPer );

                System.out.println( "\n" );
            }

        }
        catch ( final Exception e )
        {
            e.printStackTrace( System.out );
            System.out.println( e );
        }

        final long putAvJCS = putTotalLRU / loops;
        final long getAvJCS = getTotalLRU / loops;
        final long putAvHashtable = putTotalMRU / loops;
        final long getAvHashtable = getTotalMRU / loops;

        System.out.println( "Finished " + loops + " loops of " + tries + " gets and puts" );

        System.out.println( "\n" );
        System.out.println( "Put average for JCS       = " + putAvJCS );
        System.out.println( "Put average for MRU = " + putAvHashtable );
        ratioPut = Float.intBitsToFloat( (int) putAvJCS ) / Float.intBitsToFloat( (int) putAvHashtable );
        System.out.println( "JCS puts took " + ratioPut + " times the Hashtable, the goal is <" + target + "x" );

        System.out.println( "\n" );
        System.out.println( "Get average for JCS       = " + getAvJCS );
        System.out.println( "Get average for MRU = " + getAvHashtable );
        ratioGet = Float.intBitsToFloat( (int) getAvJCS ) / Float.intBitsToFloat( (int) getAvHashtable );
        System.out.println( "JCS gets took " + ratioGet + " times the Hashtable, the goal is <" + target + "x" );
    }

    /**
     * A unit test for JUnit
     * @throws Exception Description of the Exception
     */
    @Test
    void testSimpleLoad()
        throws Exception
    {
        final Log log1 = Log.getLog( LRUMemoryCache.class );
        if ( log1.isDebugEnabled() )
        {
            System.out.println( "The log level must be at info or above for the a performance test." );
            return;
        }
        final Log log2 = Log.getLog( MRUMemoryCache.class );
        if ( log2.isDebugEnabled() )
        {
            System.out.println( "The log level must be at info or above for the a performance test." );
            return;
        }
        doWork();

        // these were when the mru was implemented with the jdk linked list
        //assertTrue( "Ratio is unacceptible.", this.ratioPut < target );
        ///assertTrue( "Ratio is unacceptible.", this.ratioGet < target );
    }

}
