package org.apache.commons.jcs;

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
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;

/**
 * This test ensures that basic memory operations are with a speficified order of magnitude of the
 * java.util.Hashtable.
 * <p>
 * Currenlty JCS is un 2x a hashtable for gets, and under 1.2x for puts.
 */
public class JCSvsHashtablePerformanceTest
    extends TestCase
{
    /** jcs / hashtable */
    float ratioPut = 0;

    /** jcs / hashtable */
    float ratioGet = 0;

    /** ration goal */
    float target = 3.50f;

    /** Times to run the test */
    int loops = 20;

    /** how many puts and gets to run */
    int tries = 50000;

    /**
     * @param testName
     */
    public JCSvsHashtablePerformanceTest( String testName )
    {
        super( testName );
    }

    /**
     * A unit test suite for JUnit
     * @return The test suite
     */
    public static Test suite()
    {
        return new TestSuite( JCSvsHashtablePerformanceTest.class );
    }

    /**
     * A unit test for JUnit
     * @throws Exception Description of the Exception
     */
    public void testSimpleLoad()
        throws Exception
    {
        Log log1 = LogFactory.getLog( LRUMemoryCache.class );
        if ( log1.isDebugEnabled() )
        {
            System.out.println( "The log level must be at info or above for the a performance test." );
            return;
        }
        Log log2 = LogFactory.getLog( JCS.class );
        if ( log2.isDebugEnabled() )
        {
            System.out.println( "The log level must be at info or above for the a performance test." );
            return;
        }
        doWork();
        assertTrue( this.ratioPut < target );
        assertTrue( this.ratioGet < target );
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

        try
        {

            JCS.setConfigFilename( "/TestJCSvHashtablePerf.ccf" );
            CacheAccess<String, String> cache = JCS.getInstance( "testCache1" );

            for ( int j = 0; j < loops; j++ )
            {

                String name = "JCS      ";
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

                // /////////////////////////////////////////////////////////////
                name = "Hashtable";
                Hashtable<String, String> cache2 = new Hashtable<String, String>();
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache2.put( "key:" + i, "data" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cache2.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalHashtable += time;
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( name + " get time for " + tries + " = " + time + "; millis per = " + tPer );

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
        System.out.println( "Put average for JCS       = " + putAvJCS );
        System.out.println( "Put average for Hashtable = " + putAvHashtable );
        ratioPut = Float.intBitsToFloat( (int) putAvJCS ) / Float.intBitsToFloat( (int) putAvHashtable );
        System.out.println( "JCS puts took " + ratioPut + " times the Hashtable, the goal is <" + target + "x" );

        System.out.println( "\n" );
        System.out.println( "Get average for JCS       = " + getAvJCS );
        System.out.println( "Get average for Hashtable = " + getAvHashtable );
        ratioGet = Float.intBitsToFloat( (int) getAvJCS ) / Float.intBitsToFloat( (int) getAvHashtable );
        System.out.println( "JCS gets took " + ratioGet + " times the Hashtable, the goal is <" + target + "x" );

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
