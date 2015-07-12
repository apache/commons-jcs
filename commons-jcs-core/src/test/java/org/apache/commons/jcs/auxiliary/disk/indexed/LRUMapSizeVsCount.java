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

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This ensures that the jcs version of the LRU map is as fast as the commons
 * version. It has been testing at .6 to .7 times the commons LRU.
 * <p>
 * @author aaronsm
 *
 */
public class LRUMapSizeVsCount
    extends TestCase
{
    /** The put put ration after the test */
    double ratioPut = 0;

    /** The ratio after the test */
    double ratioGet = 0;

    /** put size / count  ratio */
    float targetPut = 1.2f;

    /** get size / count ratio */
    float targetGet = 1.2f;

    /** Time to loop */
    int loops = 20;

    /** items to put and get per loop */
    int tries = 100000;

    /**
     * @param testName
     */
    public LRUMapSizeVsCount( String testName )
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
        return new TestSuite( LRUMapSizeVsCount.class );
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

        long putTotalCount = 0;
        long getTotalCount = 0;
        long putTotalSize = 0;
        long getTotalSize = 0;

        long minTimeSizePut = Long.MAX_VALUE;
        long minTimeSizeGet = Long.MAX_VALUE;
        long minTimeCountPut = Long.MAX_VALUE;
        long minTimeCountGet = Long.MAX_VALUE;

        String cacheName = "LRUMap";
        String cache2Name = "";

        try
        {
        	IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
        	cattr.setName("junit");
        	cattr.setCacheName("junit");
        	cattr.setDiskPath(".");
        	IndexedDiskCache<String, String> idc = new IndexedDiskCache<String, String>(cattr);

			Map<String, IndexedDiskElementDescriptor> cacheCount = idc.new LRUMapCountLimited( tries );
			Map<String, IndexedDiskElementDescriptor> cacheSize = idc.new LRUMapSizeLimited( tries/1024/2 );

            for ( int j = 0; j < loops; j++ )
            {
                cacheName = "LRU Count           ";
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cacheCount.put( "key:" + i,  new IndexedDiskElementDescriptor(i, i) );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalCount += time;
                minTimeCountPut = Math.min(time, minTimeCountPut);
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( cacheName + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cacheCount.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalCount += time;
                minTimeCountGet = Math.min(minTimeCountGet, time);
                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( cacheName + " get time for " + tries + " = " + time + "; millis per = " + tPer );

                ///////////////////////////////////////////////////////////////
                cache2Name = "LRU Size            ";
                //or LRUMapJCS
                //cache2Name = "Hashtable";
                //Hashtable cache2 = new Hashtable();
                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cacheSize.put( "key:" + i, new IndexedDiskElementDescriptor(i, i) );
                }
                end = System.currentTimeMillis();
                time = end - start;
                putTotalSize += time;
                minTimeSizePut = Math.min(minTimeSizePut, time);

                tPer = Float.intBitsToFloat( (int) time ) / Float.intBitsToFloat( tries );
                System.out.println( cache2Name + " put time for " + tries + " = " + time + "; millis per = " + tPer );

                start = System.currentTimeMillis();
                for ( int i = 0; i < tries; i++ )
                {
                    cacheSize.get( "key:" + i );
                }
                end = System.currentTimeMillis();
                time = end - start;
                getTotalSize += time;
                minTimeSizeGet = Math.min(minTimeSizeGet, time);

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

        long putAvCount = putTotalCount / loops;
        long getAvCount = getTotalCount / loops;
        long putAvSize = putTotalSize / loops;
        long getAvSize = getTotalSize / loops;

        System.out.println( "Finished " + loops + " loops of " + tries + " gets and puts" );

        System.out.println( "\n" );
        System.out.println( "Put average for " + cacheName +  " = " + putAvCount );
        System.out.println( "Put average for " + cache2Name + " = " + putAvSize );
        ratioPut = (putAvSize *1.0) / putAvCount;
        System.out.println( cache2Name.trim() + " puts took " + ratioPut + " times the " + cacheName.trim() + ", the goal is <" + targetPut
            + "x" );

        System.out.println( "\n" );
        System.out.println( "Put minimum for " + cacheName +  " = " + minTimeCountPut );
        System.out.println( "Put minimum for " + cache2Name + " = " + minTimeSizePut );
        ratioPut = (minTimeSizePut * 1.0) / minTimeCountPut;
        System.out.println( cache2Name.trim() + " puts took " + ratioPut + " times the " + cacheName.trim() + ", the goal is <" + targetPut
            + "x" );

        System.out.println( "\n" );
        System.out.println( "Get average for " + cacheName + " = " + getAvCount );
        System.out.println( "Get average for " + cache2Name + " = " + getAvSize );
        ratioGet = Float.intBitsToFloat( (int) getAvCount ) / Float.intBitsToFloat( (int) getAvSize );
        ratioGet = (getAvSize * 1.0) / getAvCount;
        System.out.println( cache2Name.trim() + " gets took " + ratioGet + " times the " + cacheName.trim() + ", the goal is <" + targetGet
            + "x" );

        System.out.println( "\n" );
        System.out.println( "Get minimum for " + cacheName +  " = " + minTimeCountGet );
        System.out.println( "Get minimum for " + cache2Name + " = " + minTimeSizeGet );
        ratioPut = (minTimeSizeGet * 1.0) / minTimeCountGet;
        System.out.println( cache2Name.trim() + " puts took " + ratioPut + " times the " + cacheName.trim() + ", the goal is <" + targetGet
            + "x" );

    }

    /**
     * @param args
     */
    public static void main( String args[] )
    {
    	LRUMapSizeVsCount test = new LRUMapSizeVsCount( "command" );
        test.doWork();
    }

}
