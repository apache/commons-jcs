package org.apache.commons.jcs3;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This is based on a test that was posted to the user's list:
 * <p>
 * http://www.opensubscriber.com/message/jcs-users@jakarta.apache.org/2435965.html
 */
class JCSThrashTest
{
    /**
     * A runnable, that can throw an exception.
     */
    protected interface Executable
    {
        /**
         * Executes this object.
         * @throws Exception
         */
        void execute()
            throws Exception;
    }

    /** The logger. */
    private static final Log LOG = Log.getLog( JCSThrashTest.class.getName() );

    /**
     * the cache instance
     */
    protected CacheAccess<String, Serializable> jcs;

    /**
     * @return size
     */
    private int getListSize()
    {
        final String listSize = "List Size";
        final String lruMemoryCache = "LRU Memory Cache";
        String result = "0";
        final List<IStats> istats = jcs.getStatistics().getAuxiliaryCacheStats();
        for ( final IStats istat : istats )
        {
            final List<IStatElement<?>> statElements = istat.getStatElements();
            if ( lruMemoryCache.equals( istat.getTypeName() ) )
            {
                for ( final IStatElement<?> statElement : statElements )
                {
                    if ( listSize.equals( statElement.getName() ) )
                    {
                        result = statElement.getData().toString();
                        break;
                    }
                }
            }
        }
        return Integer.parseInt( result );
    }

    /**
     * Measure memory used by the VM.
     *
     * @return bytes
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
     * Runs a set of threads, for a fixed amount of time.
     *
     * @param executables
     * @throws Exception
     */
    protected void runThreads( final List<Executable> executables )
        throws Exception
    {

        final long endTime = System.currentTimeMillis() + 10000;
        final Throwable[] errors = new Throwable[1];

        // Spin up the threads
        final Thread[] threads = new Thread[executables.size()];
        for ( int i = 0; i < threads.length; i++ )
        {
            final JCSThrashTest.Executable executable = executables.get( i );
            threads[i] = new Thread()
            {
                @Override
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
                    catch ( final Throwable t )
                    {
                        // Hang on to any errors
                        errors[0] = t;
                    }
                }
            };
            threads[i].start();
        }

        // Wait for the threads to finish
        for (final Thread thread : threads) {
            thread.join();
        }

        // Throw any error that happened
        if ( errors[0] != null )
        {
            throw new Exception( "Test thread failed.", errors[0]);
        }
    }

    /**
     * Sets up the test
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestThrash.ccf" );
        jcs = JCS.getInstance( "testcache" );
    }

    /**
     * @throws Exception
     */
    @AfterEach
    void tearDown()
        throws Exception
    {
        jcs.clear();
        jcs.dispose();
    }

    /**
     * This does a bunch of work and then verifies that the memory has not grown by much. Most of
     * the time the amount of memory used after the test is less.
     * @throws Exception
     */
    @Test
    void testForMemoryLeaks()
        throws Exception
    {
        final long differenceMemoryCache = thrashCache();
        LOG.info( "Memory Difference is: " + differenceMemoryCache );
        assertTrue( differenceMemoryCache < 500000 );

        //LOG.info( "Memory Used is: " + measureMemoryUse() );
    }

    /**
     * Tests adding an entry.
     * @throws Exception
     */
    @Test
    void testPut()
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
    @Test
    void testRemove()
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
     * @return time
     * @throws Exception
     */
    protected long thrashCache()
        throws Exception
    {
        final long startingSize = measureMemoryUse();
        LOG.info( "Memory Used is: " + startingSize );

        final String value = "value";
        final String key = "key";

        // Add the entry
        jcs.put( key, value );

        // Create 15 threads that read the keys;
        final List<Executable> executables = new ArrayList<>();
        for ( int i = 0; i < 15; i++ )
        {
            final JCSThrashTest.Executable executable = () ->
            {
                for ( int j = 0; j < 500; j++ )
                {
                    final String keyj = "key" + j;
                    jcs.get( keyj );
                }
                jcs.get( "key" );
            };
            executables.add( executable );
        }

        // Create 15 threads that are insert 500 keys with large byte[] as
        // values
        for ( int i = 0; i < 15; i++ )
        {
            final JCSThrashTest.Executable executable = () ->
            {

                // Add a bunch of entries
                for ( int j = 0; j < 500; j++ )
                {
                    // Use a random length value
                    final String keyj = "key" + j;
                    final byte[] valuej = new byte[10000];
                    jcs.put( keyj, valuej );
                }
            };
            executables.add( executable );
        }

        runThreads( executables );
        jcs.clear();

        final long finishingSize = measureMemoryUse();
        LOG.info( "Memory Used is: " + finishingSize );
        return finishingSize - startingSize;
    }
}
