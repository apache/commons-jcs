package org.apache.jcs.engine;

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

import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 * <p>
 * This is a modified version of the experimental version. It should lazy initialize the processor
 * thread, and kill the thread if the queue goes empty for a specified period, now set to 1 minute.
 * If something comes in after that a new processor thread should be created.
 */
public class CacheEventQueue
    extends AbstractCacheEventQueue
{
    /** The type of queue -- there are pooled and single */
    private static final String queueType = SINGLE_QUEUE_TYPE;

    /** the thread that works the queue. */
    private Thread processorThread;

    /** sync */
    protected final Object queueLock = new Object();

    /** the head of the queue */
    private Node head = new Node();

    /** the end of the queue */
    private Node tail = head;

    /** Number of items in the queue */
    private int size = 0;

    /**
     * Constructs with the specified listener and the cache name.
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     */
    public CacheEventQueue( ICacheListener listener, long listenerId, String cacheName )
    {
        this( listener, listenerId, cacheName, 10, 500 );
    }

    /**
     * Constructor for the CacheEventQueue object
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     */
    public CacheEventQueue( ICacheListener listener, long listenerId, String cacheName, int maxFailure,
                            int waitBeforeRetry )
    {
        initialize( listener, listenerId, cacheName, maxFailure, waitBeforeRetry, null );
    }

    /**
     * Initializes the queue.
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName
     */
    public void initialize( ICacheListener listener, long listenerId, String cacheName, int maxFailure,
                            int waitBeforeRetry, String threadPoolName )
    {
        if ( listener == null )
        {
            throw new IllegalArgumentException( "listener must not be null" );
        }

        this.listener = listener;
        this.listenerId = listenerId;
        this.cacheName = cacheName;
        this.maxFailure = maxFailure <= 0 ? 3 : maxFailure;
        this.waitBeforeRetry = waitBeforeRetry <= 0 ? 500 : waitBeforeRetry;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructed: " + this );
        }
    }

    /**
     * What type of queue is this.
     * <p>
     * @return queueType
     */
    public String getQueueType()
    {
        return queueType;
    }

    /**
     * Kill the processor thread and indicate that the queue is detroyed and no longer alive, but it
     * can still be working.
     */
    public synchronized void stopProcessing()
    {
        destroyed = true;
        processorThread = null;
    }

    /**
     * Event Q is empty.
     * <p>
     * Calling destroy interrupts the processor thread.
     */
    public synchronized void destroy()
    {
        if ( !destroyed )
        {
            destroyed = true;

            if ( log.isInfoEnabled() )
            {
                log.info( "Destroying queue, stats =  " + getStatistics() );
            }

            // Synchronize on queue so the thread will not wait forever,
            // and then interrupt the QueueProcessor

            if ( processorThread != null )
            {
                synchronized ( queueLock )
                {
                    processorThread.interrupt();
                }
            }
            processorThread = null;

            if ( log.isInfoEnabled() )
            {
                log.info( "Cache event queue destroyed: " + this );
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "Destroy was called after queue was destroyed.  Doing nothing.  Stats =  " + getStatistics() );
            }
        }
    }

    /**
     * Adds an event to the queue.
     * <p>
     * @param event
     */
    @Override
    protected void put( AbstractCacheEvent event )
    {
        Node newNode = new Node();
        if ( log.isDebugEnabled() )
        {
            log.debug( "Event entering Queue for " + cacheName + ": " + event );
        }

        newNode.event = event;

        synchronized ( queueLock )
        {
            size++;
            tail.next = newNode;
            tail = newNode;
            if ( isWorking() )
            {
                if ( !isAlive() )
                {
                    destroyed = false;
                    processorThread = new QProcessor( this );
                    processorThread.start();
                    if ( log.isInfoEnabled() )
                    {
                        log.info( "Cache event queue created: " + this );
                    }
                }
                else
                {
                    queueLock.notify();
                }
            }
        }
    }

    // /////////////////////////// Inner classes /////////////////////////////

    /**
     * This is the thread that works the queue.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class QProcessor
        extends Thread
    {
        /** The queue to work */
        CacheEventQueue queue;

        /**
         * Constructor for the QProcessor object
         * <p>
         * @param aQueue the event queue to take items from.
         */
        QProcessor( CacheEventQueue aQueue )
        {
            super( "CacheEventQueue.QProcessor-" + aQueue.cacheName );

            setDaemon( true );
            queue = aQueue;
        }

        /**
         * Main processing method for the QProcessor object.
         * <p>
         * Waits for a specified time (waitToDieMillis) for something to come in and if no new
         * events come in during that period the run method can exit and the thread is dereferenced.
         */
        @Override
        public void run()
        {
            AbstractCacheEvent event = null;

            while ( queue.isAlive() )
            {
                event = queue.take();

                if ( log.isDebugEnabled() )
                {
                    log.debug( "Event from queue = " + event );
                }

                if ( event == null )
                {
                    synchronized ( queueLock )
                    {
                        try
                        {
                            queueLock.wait( queue.getWaitToDieMillis() );
                        }
                        catch ( InterruptedException e )
                        {
                            log.warn( "Interrupted while waiting for another event to come in before we die." );
                            return;
                        }
                        event = queue.take();
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Event from queue after sleep = " + event );
                        }
                    }
                    if ( event == null )
                    {
                        queue.stopProcessing();
                    }
                }

                if ( queue.isWorking() && queue.isAlive() && event != null )
                {
                    event.run();
                }
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "QProcessor exiting for " + queue );
            }
        }
    }

    /**
     * Returns the next cache event from the queue or null if there are no events in the queue.
     * <p>
     * We have an empty node at the head and the tail. When we take an item from the queue we move
     * the next node to the head and then clear the value from that node. This value is returned.
     * <p>
     * When the queue is empty the head node is the same as the tail node.
     * <p>
     * @return An event to process.
     */
    protected AbstractCacheEvent take()
    {
        synchronized ( queueLock )
        {
            // wait until there is something to read
            if ( head == tail )
            {
                return null;
            }

            Node node = head.next;

            AbstractCacheEvent value = node.event;

            if ( log.isDebugEnabled() )
            {
                log.debug( "head.event = " + head.event );
                log.debug( "node.event = " + node.event );
            }

            // Node becomes the new head (head is always empty)

            node.event = null;
            head = node;

            size--;
            return value;
        }
    }

    /**
     * This method returns semi-structured data on this queue.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getStatistics()
     * @return information on the status and history of the queue
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Cache Event Queue" );

        ArrayList<IStatElement> elems = new ArrayList<IStatElement>();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Working" );
        se.setData( "" + this.working );
        elems.add( se );

        se = new StatElement();
        se.setName( "Alive" );
        se.setData( "" + this.isAlive() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Empty" );
        se.setData( "" + this.isEmpty() );
        elems.add( se );

        int size = 0;
        synchronized ( queueLock )
        {
            // wait until there is something to read
            if ( head == tail )
            {
                size = 0;
            }
            else
            {
                Node n = head;
                while ( n != null )
                {
                    n = n.next;
                    size++;
                }
            }

            se = new StatElement();
            se.setName( "Size" );
            se.setData( "" + size );
            elems.add( se );
        }

        // get an array and put them in the Stats object
        IStatElement[] ses = elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

    /**
     * @return whether there are any items in the queue.
     */
    public boolean isEmpty()
    {
        return tail == head;
    }

    /**
     * Returns the number of elements in the queue.
     * <p>
     * @return number of items in the queue.
     */
    public int size()
    {
        return size;
    }
}
