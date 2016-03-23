package org.apache.commons.jcs.engine;

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

import java.io.IOException;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheListener;

/**
 * This test case is designed to makes sure there are no deadlocks in the event queue. The time to
 * live should be set to a very short interval to make a deadlock more likely.
 * <p>
 * @author Aaron Smuts
 */
public class EventQueueConcurrentLoadTest
    extends TestCase
{
    /** The queue implementation */
    private static CacheEventQueue<String, String> queue = null;

    /** The mock listener */
    private static CacheListenerImpl<String, String> listen = null;

    /** max failure setting */
    private final int maxFailure = 3;

    /** time to wait before retrying on failure. */
    private final int waitBeforeRetry = 100;

    /** very small idle time */
    private final int idleTime = 2;

    /**
     * Constructor for the TestDiskCache object.
     * @param testName
     */
    public EventQueueConcurrentLoadTest( String testName )
    {
        super( testName );
    }

    /**
     * Main method passes this test to the text test runner.
     * @param args
     */
    public static void main( String args[] )
    {
        String[] testCaseName = { EventQueueConcurrentLoadTest.class.getName() };
        junit.textui.TestRunner.main( testCaseName );
    }

    /**
     * A unit test suite for JUnit
     * @return The test suite
     */
    public static Test suite()
    {
        ActiveTestSuite suite = new ActiveTestSuite();

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunPutTest1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runPutTest( 200, 200 );
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunPutTest2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runPutTest( 1200, 1400 );
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunRemoveTest1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runRemoveTest( 2200 );
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testStopProcessing1" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runStopProcessingTest();
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunPutTest4" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runPutTest( 5200, 6600 );
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunRemoveTest2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runRemoveTest( 5200 );
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testStopProcessing2" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runStopProcessingTest();
            }
        } );

        suite.addTest( new EventQueueConcurrentLoadTest( "testRunPutDelayTest" )
        {
            @Override
            public void runTest()
                throws Exception
            {
                this.runPutDelayTest( 100, 6700 );
            }
        } );

        return suite;
    }

    /**
     * Test setup. Create the static queue to be used by all tests
     */
    @Override
    public void setUp()
    {
        listen = new CacheListenerImpl<String, String>();
        queue = new CacheEventQueue<String, String>( listen, 1L, "testCache1", maxFailure, waitBeforeRetry );

        queue.setWaitToDieMillis( idleTime );
    }

    /**
     * Adds put events to the queue.
     * @param end
     * @param expectedPutCount
     * @throws Exception
     */
    public void runPutTest( int end, int expectedPutCount )
        throws Exception
    {
        for ( int i = 0; i <= end; i++ )
        {
            CacheElement<String, String> elem = new CacheElement<String, String>( "testCache1", i + ":key", i + "data" );
            queue.addPutEvent( elem );
        }

        while ( !queue.isEmpty() )
        {
            synchronized ( this )
            {
                System.out.println( "queue is still busy, waiting 250 millis" );
                this.wait( 250 );
            }
        }
        System.out.println( "queue is empty, comparing putCount" );

        // this becomes less accurate with each test. It should never fail. If
        // it does things are very off.
        assertTrue( "The put count [" + listen.putCount + "] is below the expected minimum threshold ["
            + expectedPutCount + "]", listen.putCount >= ( expectedPutCount - 1 ) );

    }

    /**
     * Add remove events to the event queue.
     * @param end
     * @throws Exception
     */
    public void runRemoveTest( int end )
        throws Exception
    {
        for ( int i = 0; i <= end; i++ )
        {
            queue.addRemoveEvent( i + ":key" );
        }

    }

    /**
     * Add remove events to the event queue.
     * @throws Exception
     */
    public void runStopProcessingTest()
        throws Exception
    {
        queue.stopProcessing();
    }

    /**
     * Test putting and a delay. Waits until queue is empty to start.
     * @param end
     * @param expectedPutCount
     * @throws Exception
     */
    public void runPutDelayTest( int end, int expectedPutCount )
        throws Exception
    {
        while ( !queue.isEmpty() )
        {
            synchronized ( this )
            {
                System.out.println( "queue is busy, waiting 250 millis to begin" );
                this.wait( 250 );
            }
        }
        System.out.println( "queue is empty, begin" );

        // get it going
        CacheElement<String, String> elem = new CacheElement<String, String>( "testCache1", "a:key", "adata" );
        queue.addPutEvent( elem );

        for ( int i = 0; i <= end; i++ )
        {
            synchronized ( this )
            {
                if ( i % 2 == 0 )
                {
                    this.wait( idleTime );
                }
                else
                {
                    this.wait( idleTime / 2 );
                }
            }
            CacheElement<String, String> elem2 = new CacheElement<String, String>( "testCache1", i + ":key", i + "data" );
            queue.addPutEvent( elem2 );
        }

        while ( !queue.isEmpty() )
        {
            synchronized ( this )
            {
                System.out.println( "queue is still busy, waiting 250 millis" );
                this.wait( 250 );
            }
        }
        System.out.println( "queue is empty, comparing putCount" );

        Thread.sleep( 1000 );

        // this becomes less accurate with each test. It should never fail. If
        // it does things are very off.
        assertTrue( "The put count [" + listen.putCount + "] is below the expected minimum threshold ["
            + expectedPutCount + "]", listen.putCount >= ( expectedPutCount - 1 ) );

    }

    /**
     * This is a dummy cache listener to use when testing the event queue.
     */
    protected static class CacheListenerImpl<K, V>
        implements ICacheListener<K, V>
    {
        /**
         * <code>putCount</code>
         */
        protected int putCount = 0;

        /**
         * <code>removeCount</code>
         */
        protected int removeCount = 0;

        /**
         * @param item
         * @throws IOException
         */
        @Override
        public void handlePut( ICacheElement<K, V> item )
            throws IOException
        {
            synchronized ( this )
            {
                putCount++;
            }
        }

        /**
         * @param cacheName
         * @param key
         * @throws IOException
         */
        @Override
        public void handleRemove( String cacheName, K key )
            throws IOException
        {
            synchronized ( this )
            {
                removeCount++;
            }

        }

        /**
         * @param cacheName
         * @throws IOException
         */
        @Override
        public void handleRemoveAll( String cacheName )
            throws IOException
        {
            // TODO Auto-generated method stub

        }

        /**
         * @param cacheName
         * @throws IOException
         */
        @Override
        public void handleDispose( String cacheName )
            throws IOException
        {
            // TODO Auto-generated method stub

        }

        /**
         * @param id
         * @throws IOException
         */
        @Override
        public void setListenerId( long id )
            throws IOException
        {
            // TODO Auto-generated method stub

        }

        /**
         * @return 0
         * @throws IOException
         */
        @Override
        public long getListenerId()
            throws IOException
        {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
