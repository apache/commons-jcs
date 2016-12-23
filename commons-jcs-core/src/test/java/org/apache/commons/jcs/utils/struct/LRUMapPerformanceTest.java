package org.apache.commons.jcs.utils.struct;

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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.jcs.JCSvsHashtablePerformanceTest;

import java.util.Map;

/**
 * This ensures that the jcs version of the LRU map is as fast as the commons
 * version. It has been testing at .6 to .7 times the commons LRU.
 * <p>
 * @author aaronsm
 *
 */
public class LRUMapPerformanceTest
    extends TestCase
{
    /** The put put ration after the test */
    float ratioPut = 0;

    /** The ratio after the test */
    float ratioGet = 0;

    /** put jcs / commons ratio */
    float targetPut = 1.2f;

    /** get jcs / commons ratio */
    float targetGet = .5f;

    /** Time to loop */
    int loops = 20;

    /** items to put and get per loop */
    int tries = 100000;

    /**
     * @param testName
     */
    public LRUMapPerformanceTest( String testName )
    {
        super( testName );
    }

    /**
     * A unit test suite for JUnit
     * <p>
     * @return The test suite
     */
    public static Test suite()
    {
        return new TestSuite( LRUMapPerformanceTest.class );
    }

    /**
     * A unit test for JUnit
     *
     * @throws Exception
     *                Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        doWork();
        assertTrue( this.ratioPut < targetPut );
        assertTrue( this.ratioGet < targetGet );
    }

    /**
     *
     */
    public void doWork()
    {
        long start = 0;
        long end = 0;
        long time = 0;
        float tPer = 0;

        long putTotalJCS = 0;
        long getTotalJCS = 0;
        long putTotalHashtable = 0;
        long getTotalHashtable = 0;

        String name = "LRUMap";
        String cache2Name = "";

        try
        {
            Map<String, String> cache = new LRUMap<String, String>( tries );

            for ( int j = 0; j < loops; j++ )
            {
                name = "JCS      ";
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalJCS += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalJCS += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = " + time + "; millis per = " + tPer );

                ///////////////////////////////////////////////////////////////
                cache2Name = "LRUMapJCS (commons)";
                //or LRUMapJCS
                Map<String, String> cache2 = new org.apache.commons.collections4.map.LRUMap<String, String>( tries );
                //cache2Name = "Hashtable";
                //Hashtable cache2 = new Hashtable();
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache2.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( cache2Name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache2.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( cache2Name + " get time for " + tries + " = " + time + "; millis per = " + tPer );

                System.out.println( "\n" );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            System.out.println( e );
        }

        long putAvJCS = putTotalJCS / loops;
        long getAvJCS = getTotalJCS / loops;
        long putAvHashtable = putTotalHashtable / loops;
        long getAvHashtable = getTotalHashtable / loops;

        System.out.println( "Finished " + loops + " loops of " + tries + " gets and puts" );

        System.out.println( "\n" );
        System.out.println( "Put average for LRUMap       = " + putAvJCS );
        System.out.println( "Put average for " + cache2Name + " = " + putAvHashtable );
        ratioPut = Float.intBitsToFloat( (int) putAvJCS ) / Float.intBitsToFloat( (int) putAvHashtable );
        System.out.println( name + " puts took " + ratioPut + " times the " + cache2Name + ", the goal is <" + targetPut
            + "x" );

        System.out.println( "\n" );
        System.out.println( "Get average for LRUMap       = " + getAvJCS );
        System.out.println( "Get average for " + cache2Name + " = " + getAvHashtable );
        ratioGet = Float.intBitsToFloat( (int) getAvJCS ) / Float.intBitsToFloat( (int) getAvHashtable );
        System.out.println( name + " gets took " + ratioGet + " times the " + cache2Name + ", the goal is <" + targetGet
            + "x" );
    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
        JCSvsHashtablePerformanceTest test = new JCSvsHashtablePerformanceTest( "command" );
        test.doWork();
    }

}
