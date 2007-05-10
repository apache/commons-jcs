package org.apache.jcs;

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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * This is based on a test that was posted to the user's list:
 *
 * http://www.opensubscriber.com/message/jcs-users@jakarta.apache.org/2435965.html
 *
 */
public class JCSThrashTest
    extends TestCase
{

    private static final Log LOG = LogFactory.getLog( JCSThrashTest.class.getName() );

    /**
     * the cache instance
     */
    protected JCS jcs;

    /**
     * @param args
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( JCSThrashTest.class );
    }

    /**
     * @param arg0
     */
    public JCSThrashTest( String arg0 )
    {
        super( arg0 );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        JCS.setConfigFilename( "/TestThrash.ccf" );
        jcs = JCS.getInstance( "testcache" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        jcs.clear();
        jcs.dispose();
    }

    /**
     * Tests adding an entry.
     * @throws Exception
     */
    public void testPut()
        throws Exception
    {
        final String value = "value";
        final String key = "key";

        // Make sure the element is not found
        assertEquals( 0, getListSize() );

        assertNull( jcs.get( key ) );

        jcs.put( key, value );

        // Get the element
        LOG.info( "jcs.getStats(): " + jcs.getStatistics() );
        assertEquals( 1, getListSize() );
        assertNotNull( jcs.get( key ) );
        assertEquals( value, jcs.get( key ) );
    }

    /**
     * Test elements can be removed from the store
     * @throws Exception
     */
    public void testRemove()
        throws Exception
    {
        jcs.put( "key1", "value1" );
        assertEquals( 1, getListSize() );

        jcs.remove( "key1" );
        assertEquals( 0, getListSize() );

        jcs.put( "key2", "value2" );
        jcs.put( "key3", "value3" );
        assertEquals( 2, getListSize() );

        jcs.remove( "key2" );
        assertEquals( 1, getListSize() );

        // Try to remove an object that is not there in the store
        jcs.remove( "key4" );
        assertEquals( 1, getListSize() );
    }

    /**
     * This does a bunch of work and then verifies that the memory has not grown by much.
     * Most of the time the amount of memory used after the test is less.
     *
     * @throws Exception
     */
    public void testForMemoryLeaks()
        throws Exception
    {
        long differenceMemoryCache = thrashCache();
        LOG.info( "Memory Difference is: " + differenceMemoryCache );
        assertTrue( differenceMemoryCache < 500000 );

        //LOG.info( "Memory Used is: " + measureMemoryUse() );
    }

    /**
     * @return
     * @throws Exception
     */
    protected long thrashCache()
        throws Exception
    {

        long startingSize = measureMemoryUse();
        LOG.info( "Memory Used is: " + startingSize );

        final String value = "value";
        final String key = "key";

        // Add the entry
        jcs.put( key, value );

        // Create 15 threads that read the keys;
        final List executables = new ArrayList();
        for ( int i = 0; i < 15; i++ )
        {
            final JCSThrashTest.Executable executable = new JCSThrashTest.Executable()
            {
                public void execute()
                    throws Exception
                {
                    for ( int i = 0; i < 500; i++ )
                    {
                        final String key = "key" + i;
                        jcs.get( key );
                    }
                    jcs.get( "key" );
                }
            };
            executables.add( executable );
        }

        // Create 15 threads that are insert 500 keys with large byte[] as
        // values
        for ( int i = 0; i < 15; i++ )
        {
            final JCSThrashTest.Executable executable = new JCSThrashTest.Executable()
            {
                public void execute()
                    throws Exception
                {

                    // Add a bunch of entries
                    for ( int i = 0; i < 500; i++ )
                    {
                        // Use a random length value
                        final String key = "key" + i;
                        byte[] value = new byte[10000];
                        jcs.put( key, value );
                    }
                }
            };
            executables.add( executable );
        }

        runThreads( executables );
        jcs.clear();

        long finishingSize = measureMemoryUse();
        LOG.info( "Memory Used is: " + finishingSize );
        return finishingSize - startingSize;
    }

    /**
     * Runs a set of threads, for a fixed amount of time.
     * @param executables
     * @throws Exception
     */
    protected void runThreads( final List executables )
        throws Exception
    {

        final long endTime = System.currentTimeMillis() + 10000;
        final Throwable[] errors = new Throwable[1];

        // Spin up the threads
        final Thread[] threads = new Thread[executables.size()];
        for ( int i = 0; i < threads.length; i++ )
        {
            final JCSThrashTest.Executable executable = (JCSThrashTest.Executable) executables.get( i );
            threads[i] = new Thread()
            {
                public void run()
                {
                    try
                    {
                        // Run the thread until the given end time
                        while ( System.currentTimeMillis() < endTime )
                        {
                            executable.execute();
                        }
                    }
                    catch ( Throwable t )
                    {
                        // Hang on to any errors
                        errors[0] = t;
                    }
                }
            };
            threads[i].start();
        }

        // Wait for the threads to finish
        for ( int i = 0; i < threads.length; i++ )
        {
            threads[i].join();
        }

        // Throw any error that happened
        if ( errors[0] != null )
        {
            throw new Exception( "Test thread failed.", errors[0] );
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

    /**
     * A runnable, that can throw an exception.
     */
    protected interface Executable
    {
        /**
         * Executes this object.
         *
         * @throws Exception
         */
        void execute()
            throws Exception;
    }

    /**
     *
     * @return
     */
    private int getListSize()
    {
        final String listSize = "List Size";
        final String lruMemoryCache = "LRU Memory Cache";
        String result = "0";
        IStats istats[] = jcs.getStatistics().getAuxiliaryCacheStats();
        for ( int i = 0; i < istats.length; i++ )
        {
            IStatElement statElements[] = istats[i].getStatElements();
            if ( lruMemoryCache.equals( istats[i].getTypeName() ) )
            {
                for ( int j = 0; j < statElements.length; j++ )
                {
                    if ( listSize.equals( statElements[j].getName() ) )
                    {
                        result = statElements[j].getData();
                    }
                }
            }

        }
        return Integer.parseInt( result );
    }

//    private int getMapSize()
//    {
//        final String listSize = "Map Size";
//        String result = "0";
//        IStatElement statElements[] = jcs.getStatistics().getStatElements();
//        for ( int i = 0; i < statElements.length; i++ )
//        {
//            if ( listSize.equals( statElements[i].getName() ) )
//            {
//                result = statElements[i].getData();
//            }
//        }
//        return Integer.parseInt( result );
//    }
}
