package org.apache.commons.jcs4.engine;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This test case is designed to makes sure there are no deadlocks in the event queue. The time to
 * live should be set to a very short interval to make a deadlock more likely.
 */
class EventQueueConcurrentLoadTest
{

    // Dummy cache listener to use when testing the event queue
    protected static class CacheListenerImpl<K, V>
        implements ICacheListener<K, V>
    {

        protected AtomicInteger putCount = new AtomicInteger();
        protected AtomicInteger removeCount = new AtomicInteger();

        @Override
        public long getListenerId()
            throws IOException
        {
            return 0;
        }

        @Override
        public void handleDispose( final String cacheName )
            throws IOException
        {
            // No implementation needed for this test
        }

        @Override
        public void handlePut( final ICacheElement<K, V> item )
            throws IOException
        {
            putCount.incrementAndGet();
        }

        @Override
        public void handleRemove( final String cacheName, final K key )
            throws IOException
        {
            removeCount.incrementAndGet();
        }

        @Override
        public void handleRemoveAll( final String cacheName )
            throws IOException
        {
            // No implementation needed for this test
        }

        @Override
        public void setListenerId( final long id )
            throws IOException
        {
            // No implementation needed for this test
        }
    }

    private static CacheEventQueue<String, String> queue;
    private static CacheListenerImpl<String, String> listen;

    private static final int maxFailure = 3;
    private static final int waitBeforeRetry = 100;
    private static final int idleTime = 2;

    @BeforeEach
    void setUp()
    {
        listen = new CacheListenerImpl<>();
        queue = new CacheEventQueue<>( listen, 1L, "testCache1", maxFailure, waitBeforeRetry );
        queue.setWaitToDieMillis( idleTime );
    }

    /**
     * Test putting and a delay. Waits until queue is empty to start.
     */
    @Test
    void testRunPutTest1()
        throws Exception
    {
        runPutTest( 200, 200 );
    }

    @Test
    void testRunPutTest2()
        throws Exception
    {
        runPutTest( 1200, 1400 );
    }

    @Test
    void testRunRemoveTest1()
        throws Exception
    {
        runRemoveTest( 2200 );
    }

    @Test
    void testRunPutTest4()
        throws Exception
    {
        runPutTest( 5200, 6600 );
    }

    @Test
    void testRunRemoveTest2()
        throws Exception
    {
        runRemoveTest( 5200 );
    }

    @Test
    void testRunPutDelayTest()
        throws Exception
    {
        runPutDelayTest( 100, 6700 );
    }

    /**
     * Test putting items with a delay.
     */
    public static void runPutDelayTest( final int end, final int expectedPutCount )
        throws Exception
    {
        while ( !queue.isEmpty() )
        {
            synchronized ( queue )
            {
                System.out.println( "queue is busy, waiting 250 millis to begin" );
                queue.wait( 250 );
            }
        }
        System.out.println( "queue is empty, begin" );

        final CacheElement<String, String> elem = new CacheElement<>( "testCache1", "a:key", "adata" );
        queue.addPutEvent( elem );

        for ( int i = 0; i < end; i++ )
        {
            synchronized ( queue )
            {
                if ( i % 2 == 0 )
                {
                    queue.wait( idleTime );
                }
                else
                {
                    queue.wait( idleTime / 2 );
                }
            }
            final CacheElement<String, String> elem2 = new CacheElement<>( "testCache1", i + ":key", i + "data" );
            queue.addPutEvent( elem2 );
        }

        while ( !queue.isEmpty() )
        {
            synchronized ( queue )
            {
                System.out.println( "queue is still busy, waiting 250 millis" );
                queue.wait( 250 );
            }
        }
        System.out.println( "queue is empty, comparing putCount" );

        Thread.sleep( 1000 );

        assertTrue( listen.putCount.get() >= expectedPutCount - 1,
                    "The put count [" + listen.putCount + "] is below the expected minimum threshold [" + expectedPutCount + "]" );
    }

    /**
     * Adds put events to the queue.
     */
    public static void runPutTest( final int end, final int expectedPutCount )
        throws Exception
    {
        for ( int i = 0; i < end; i++ )
        {
            final CacheElement<String, String> elem = new CacheElement<>( "testCache1", i + ":key", i + "data" );
            queue.addPutEvent( elem );
        }

        while ( !queue.isEmpty() )
        {
            synchronized ( queue )
            {
                System.out.println( "queue is still busy, waiting 250 millis" );
                queue.wait( 250 );
            }
        }
        System.out.println( "queue is empty, comparing putCount" );

        assertTrue( listen.putCount.get() >= expectedPutCount - 1,
                    "The put count [" + listen.putCount + "] is below the expected minimum threshold [" + expectedPutCount + "]" );
    }

    /**
     * Add remove events to the event queue.
     */
    public static void runRemoveTest( final int end )
        throws Exception
    {
        for ( int i = 0; i < end; i++ )
        {
            queue.addRemoveEvent( i + ":key" );
        }
    }
}
