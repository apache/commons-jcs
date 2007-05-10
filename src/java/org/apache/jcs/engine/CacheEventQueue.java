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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 * <p>
 * This is a modified version of the experimental version. It should lazy initilaize the processor
 * thread, and kill the thread if the queue goes emtpy for a specified period, now set to 1 minute.
 * If something comes in after that a new processor thread should be created.
 */
public class CacheEventQueue
    implements ICacheEventQueue
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( CacheEventQueue.class );

    /** The type of queue -- there are pooled and single */
    private static final int queueType = SINGLE_QUEUE_TYPE;

    /** default */
    private static final int DEFAULT_WAIT_TO_DIE_MILLIS = 10000;

    /**
     * time to wait for an event before snuffing the background thread if the queue is empty. make
     * configurable later
     */
    private int waitToDieMillis = DEFAULT_WAIT_TO_DIE_MILLIS;

    /**
     * When the events are pulled off the queue, the tell the listener to handle the specific event
     * type. The work is done by the listener.
     */
    private ICacheListener listener;

    /** Id of the listener registed with this queue */
    private long listenerId;

    /** The cache region name, if applicable. */
    private String cacheName;

    /** Maximum number of failures before we buy the farm. */
    private int maxFailure;

    /** in milliseconds */
    private int waitBeforeRetry;

    /** this is true if there is no worker thread. */
    private boolean destroyed = true;

    /**
     * This means that the queue is functional. If we reached the max number of failures, the queue
     * is marked as non functional and will never work again.
     */
    private boolean working = true;

    /** the thread that works the queue. */
    private Thread processorThread;

    /** sync */
    private Object queueLock = new Object();

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
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getQueueType()
     */
    public int getQueueType()
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
     * Returns the time to wait for events before killing the background thread.
     * @return int
     */
    public int getWaitToDieMillis()
    {
        return waitToDieMillis;
    }

    /**
     * Sets the time to wait for events before killing the background thread.
     * <p>
     * @param wtdm the ms for the q to sit idle.
     */
    public void setWaitToDieMillis( int wtdm )
    {
        waitToDieMillis = wtdm;
    }

    /**
     * Creates a brief string identifying the listener and the region.
     * <p>
     * @return String debugging info.
     */
    public String toString()
    {
        return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" + cacheName + "]";
    }

    /**
     * If they queue has an active thread it is considered alive.
     * <p>
     * @return The alive value
     */
    public synchronized boolean isAlive()
    {
        return ( !destroyed );
    }

    /**
     * Sets whether the queue is actively processing -- if there are working threads.
     * <p>
     * @param aState
     */
    public synchronized void setAlive( boolean aState )
    {
        destroyed = !aState;
    }

    /**
     * @return The listenerId value
     */
    public long getListenerId()
    {
        return listenerId;
    }

    /**
     * Event Q is emtpy.
     * <p>
     * Calling destroy interupts the processor thread.
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

            // sychronize on queue so the thread will not wait forever,
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
     * This adds a put event ot the queue. When it is processed, the element will be put to the
     * listener.
     * <p>
     * @param ce The feature to be added to the PutEvent attribute
     * @exception IOException
     */
    public synchronized void addPutEvent( ICacheElement ce )
        throws IOException
    {
        if ( isWorking() )
        {
            put( new PutEvent( ce ) );
        }
        else
        {
            if ( log.isWarnEnabled() )
            {
                log.warn( "Not enqueuing Put Event for [" + this + "] because it's non-functional." );
            }
        }
    }

    /**
     * This adds a remove event to the queue. When processed the listener's remove method will be
     * called for the key.
     * <p>
     * @param key The feature to be added to the RemoveEvent attribute
     * @exception IOException
     */
    public synchronized void addRemoveEvent( Serializable key )
        throws IOException
    {
        if ( isWorking() )
        {
            put( new RemoveEvent( key ) );
        }
        else
        {
            if ( log.isWarnEnabled() )
            {
                log.warn( "Not enqueuing Remove Event for [" + this + "] because it's non-functional." );
            }
        }
    }

    /**
     * This adds a remove all event to the queue. When it is processed, all elements will be removed
     * from the cache.
     * <p>
     * @exception IOException
     */
    public synchronized void addRemoveAllEvent()
        throws IOException
    {
        if ( isWorking() )
        {
            put( new RemoveAllEvent() );
        }
        else
        {
            if ( log.isWarnEnabled() )
            {
                log.warn( "Not enqueuing RemoveAll Event for [" + this + "] because it's non-functional." );
            }
        }
    }

    /**
     * @exception IOException
     */
    public synchronized void addDisposeEvent()
        throws IOException
    {
        if ( isWorking() )
        {
            put( new DisposeEvent() );
        }
        else
        {
            if ( log.isWarnEnabled() )
            {
                log.warn( "Not enqueuing Dispose Event for [" + this + "] because it's non-functional." );
            }
        }
    }

    /**
     * Adds an event to the queue.
     * <p>
     * @param event
     */
    private void put( AbstractCacheEvent event )
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
    private AbstractCacheEvent take()
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
     * This method returns semi structured data on this queue.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Cache Event Queue" );

        ArrayList elems = new ArrayList();

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
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }

    // /////////////////////////// Inner classes /////////////////////////////

    /** The queue is composed of nodes. */
    private static class Node
    {
        /** Next node in the singly linked list. */
        Node next = null;

        /** The payload. */
        CacheEventQueue.AbstractCacheEvent event = null;
    }

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
     * Retries before declaring failure.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private abstract class AbstractCacheEvent
        implements Runnable
    {
        /** Number of failures encountered processing this event. */
        int failures = 0;

        /** Have we finished the job */
        boolean done = false;

        /**
         * Main processing method for the AbstractCacheEvent object
         */
        public void run()
        {
            try
            {
                doRun();
            }
            catch ( IOException e )
            {
                if ( log.isWarnEnabled() )
                {
                    log.warn( e );
                }
                if ( ++failures >= maxFailure )
                {
                    if ( log.isWarnEnabled() )
                    {
                        log.warn( "Error while running event from Queue: " + this
                            + ". Dropping Event and marking Event Queue as non-functional." );
                    }
                    setWorking( false );
                    setAlive( false );
                    return;
                }
                if ( log.isInfoEnabled() )
                {
                    log.info( "Error while running event from Queue: " + this + ". Retrying..." );
                }
                try
                {
                    Thread.sleep( waitBeforeRetry );
                    run();
                }
                catch ( InterruptedException ie )
                {
                    if ( log.isErrorEnabled() )
                    {
                        log.warn( "Interrupted while sleeping for retry on event " + this + "." );
                    }
                    // TODO consider if this is best. maybe we shoudl just
                    // destroy
                    setWorking( false );
                    setAlive( false );
                }
            }
        }

        /**
         * @exception IOException
         */
        protected abstract void doRun()
            throws IOException;
    }

    /**
     * An element should be put in the cache.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class PutEvent
        extends AbstractCacheEvent
    {
        /** The element to put to the listener */
        private ICacheElement ice;

        /**
         * Constructor for the PutEvent object.
         * <p>
         * @param ice
         * @exception IOException
         */
        PutEvent( ICacheElement ice )
            throws IOException
        {
            this.ice = ice;
        }

        /**
         * Call put on the listener.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handlePut( ice );
        }

        /**
         * For debugging.
         * <p>
         * @return Info on the key and value.
         */
        public String toString()
        {
            return new StringBuffer( "PutEvent for key: " ).append( ice.getKey() ).append( " value: " )
                .append( ice.getVal() ).toString();
        }

    }

    /**
     * An element should be removed from the cache.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class RemoveEvent
        extends AbstractCacheEvent
    {
        /** The key to remove from the listener */
        private Serializable key;

        /**
         * Constructor for the RemoveEvent object
         * <p>
         * @param key
         * @exception IOException
         */
        RemoveEvent( Serializable key )
            throws IOException
        {
            this.key = key;
        }

        /**
         * Call remove on the listener.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handleRemove( cacheName, key );
        }

        /**
         * For debugging.
         * <p>
         * @return Info on the key to remove.
         */
        public String toString()
        {
            return new StringBuffer( "RemoveEvent for " ).append( key ).toString();
        }

    }

    /**
     * All elements should be removed from the cache when this event is processed.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class RemoveAllEvent
        extends AbstractCacheEvent
    {
        /**
         * Call removeAll on the listener.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handleRemoveAll( cacheName );
        }

        /**
         * For debugging.
         * <p>
         * @return The name of the event.
         */
        public String toString()
        {
            return "RemoveAllEvent";
        }

    }

    /**
     * The cache should be disposed when this event is processed.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class DisposeEvent
        extends AbstractCacheEvent
    {
        /**
         * Called when gets to the end of the queue
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handleDispose( cacheName );
        }

        /**
         * For debugging.
         * <p>
         * @return The name of the event.
         */
        public String toString()
        {
            return "DisposeEvent";
        }
    }

    /**
     * @return whether the queue is functional.
     */
    public boolean isWorking()
    {
        return working;
    }

    /**
     * This means that the queue is functional. If we reached the max number of failures, the queue
     * is marked as non functional and will never work again.
     * <p>
     * @param b
     */
    public void setWorking( boolean b )
    {
        working = b;
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
