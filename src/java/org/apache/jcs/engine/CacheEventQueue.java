package org.apache.jcs.engine;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * An event queue is used to propagate ordered cache events to one and only one
 * target listener.
 *
 * <pre>
 * Changes:<br>
 * 17 April 2004  Hanson Char
 * <ol><li>Bug fix: add missing synchronization to method addRemoveEvent();</li>
 * <li>Use the light weight new int[0] for creating the object monitor queueLock,
 * instead of new Object();</li>
 * <li>Explicitely qualify member variables of CacheEventQueue in inner classes.
 * Hopefully this will help identify any potential concurrency issue.</li>
 * </ol>
 * </pre>
 */
public class CacheEventQueue implements ICacheEventQueue
{
    private final static Log log = LogFactory.getLog( CacheEventQueue.class );

    private static int processorInstanceCount = 0;

    // private LinkedQueue queue = new LinkedQueue();

    private ICacheListener listener;
    private byte listenerId;
    private String cacheName;

    private int failureCount;
    private int maxFailure;

    // in milliseconds
    private int waitBeforeRetry;

    private boolean destroyed;
    private Thread t;

    // Internal queue implementation

    private Object queueLock = new int[0];

    // Dummy node

    private Node head = new Node();
    private Node tail = head;

    /**
     * Constructs with the specified listener and the cache name.
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     */
    public CacheEventQueue( ICacheListener listener,
                            byte listenerId,
                            String cacheName )
    {
        this( listener, listenerId, cacheName, 10, 500 );
    }

    /**
     * Constructor for the CacheEventQueue object
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     */
    public CacheEventQueue( ICacheListener listener,
                            byte listenerId,
                            String cacheName,
                            int maxFailure,
                            int waitBeforeRetry )
    {
        if ( listener == null )
        {
            throw new IllegalArgumentException( "listener must not be null" );
        }

        this.listener = listener;
        this.listenerId = listenerId;
        this.cacheName = cacheName;
        this.maxFailure = maxFailure <= 0 ? 10 : maxFailure;
        this.waitBeforeRetry = waitBeforeRetry <= 0 ? 500 : waitBeforeRetry;

        this.t = new QProcessor();
        this.t.start();

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructed: " + this );
        }
    }

    /**
     * Event Q is emtpy.
     */
    public synchronized void destroy()
    {
        if ( !this.destroyed )
        {
            this.destroyed = true;

            // sychronize on queue so the thread will not wait forever,
            // and then interrupt the QueueProcessor

            synchronized ( this.queueLock )
            {
                this.t.interrupt();
            }

            this.t = null;

            log.info( "Cache event queue destroyed: " + this );
        }
    }

    /**
     * @return
     */
    public String toString()
    {
        return "listenerId=" + this.listenerId + ", cacheName=" + this.cacheName;
    }

    /**
     * @return The {3} value
     */
    public boolean isAlive()
    {
        return ( !this.destroyed );
    }

    /**
     * @return The {3} value
     */
    public byte getListenerId()
    {
        return this.listenerId;
    }

    /**
     * @param ce The feature to be added to the PutEvent attribute
     * @exception IOException
     */
    public synchronized void addPutEvent( ICacheElement ce )
        throws IOException
    {
        if ( !this.destroyed )
        {
            put( new PutEvent( ce ) );
        }
    }

    /**
     * @param key The feature to be added to the RemoveEvent attribute
     * @exception IOException
     */
    public synchronized void addRemoveEvent( Serializable key )
        throws IOException
    {
        if ( !this.destroyed )
        {
            put( new RemoveEvent( key ) );
        }
    }

    /**
     * @exception IOException
     */
    public synchronized void addRemoveAllEvent()
        throws IOException
    {
        if ( !this.destroyed )
        {
            put( new RemoveAllEvent() );
        }
    }

    /**
     * @exception IOException
     */
    public synchronized void addDisposeEvent()
        throws IOException
    {
        if ( !this.destroyed )
        {
            put( new DisposeEvent() );
        }
    }

    /**
     * Adds an event to the queue.
     *
     * @param event
     */
    private void put( AbstractCacheEvent event )
    {
        Node newNode = new Node();

        newNode.event = event;

        synchronized ( this.queueLock )
        {
            this.tail.next = newNode;
            this.tail = newNode;

            this.queueLock.notify();
        }
    }

    private AbstractCacheEvent take() throws InterruptedException
    {
        synchronized ( this.queueLock )
        {
            // wait until there is something to read

            while ( this.head == this.tail )
            {
                this.queueLock.wait();
            }

            // we have the lock, and the list is not empty

            Node node = this.head.next;

            // This is an awful bug.  This will always return null.
            // This make the event Q and event destroyer.
            //AbstractCacheEvent value = head.event;

            // corrected
            AbstractCacheEvent value = node.event;

            if ( log.isDebugEnabled() )
            {
              log.debug( "head.event = " + this.head.event );
              log.debug( "node.event = " + node.event );
            }

            // Node becomes the new head (head is always empty)

            node.event = null;
            this.head = node;

            return value;
        }
    }

    ///////////////////////////// Inner classes /////////////////////////////

    private static class Node
    {
        Node next = null;
        AbstractCacheEvent event = null;
    }

    /**
     */
    private class QProcessor extends Thread
    {
        /**
         * Constructor for the QProcessor object
         */
        QProcessor()
        {
            super( "CacheEventQueue.QProcessor-" + ( ++CacheEventQueue.this.processorInstanceCount ) );

            setDaemon( true );
        }

        /**
         * Main processing method for the QProcessor object
         */
        public void run()
        {
            AbstractCacheEvent r = null;

            while ( !CacheEventQueue.this.destroyed )
            {
                try
                {
                    r = take();

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "r from take() = " + r );
                    }

                }
                catch ( InterruptedException e )
                {
                    // We were interrupted, just continue -- the while loop
                    // will exit if we have been properly destroyed.
                }

                if ( !CacheEventQueue.this.destroyed && r != null )
                {
                    r.run();
                }
            }
            // declare failure as listener is permanently unreachable.
            // queue = null;
            CacheEventQueue.this.listener = null;
            // The listener failure logging more the problem of the user
            // of the q.
            log.info( "QProcessor exiting for " + CacheEventQueue.this );
        }
    }

    /**
     * Retries before declaring failure.
     *
     */
    private abstract class AbstractCacheEvent implements Runnable
    {
        /**
         * Main processing method for the AbstractCacheEvent object
         */
        public void run()
        {
            IOException ex = null;

            while ( !CacheEventQueue.this.destroyed
                    && CacheEventQueue.this.failureCount <= CacheEventQueue.this.maxFailure )
            {
                try
                {
                    ex = null;
                    doRun();
                    CacheEventQueue.this.failureCount = 0;
                    return;
                    // happy and done.
                }
                catch ( IOException e )
                {
                    CacheEventQueue.this.failureCount++;
                    ex = e;
                }
                // Let's get idle for a while before retry.
                if ( !CacheEventQueue.this.destroyed
                     && CacheEventQueue.this.failureCount <= CacheEventQueue.this.maxFailure )
                {
                    try
                    {
                        log.warn( "...retrying propagation " + CacheEventQueue.this + "..." + CacheEventQueue.this.failureCount );
                        Thread.currentThread().sleep( CacheEventQueue.this.waitBeforeRetry );
                    }
                    catch ( InterruptedException ie )
                    {
                        // ignore;
                    }
                }
            }
            // Too bad.  The remote host is unreachable, so we give up.
            if ( ex != null )
            {
                log.warn( "Giving up propagation " + CacheEventQueue.this, ex );

                destroy();
            }
            return;
        }

        /**
         * Description of the Method
         *
         * @exception IOException
         */
        protected abstract void doRun()
            throws IOException;
    }

    /**
     */
    private class PutEvent extends AbstractCacheEvent
    {

        private ICacheElement ice;

        /**
         * Constructor for the PutEvent object
         *
         * @param ice
         * @exception IOException
         */
        PutEvent( ICacheElement ice )
            throws IOException
        {
            this.ice = ice;
            /*
             * this.key = key;
             * this.obj = CacheUtils.dup(obj);
             * this.attr = attr;
             * this.groupName = groupName;
             */
        }

        /**
         * Description of the Method
         *
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            /*
             * CacheElement ce = new CacheElement(cacheName, key, obj);
             * ce.setElementAttributes( attr );
             * ce.setGroupName( groupName );
             */
            CacheEventQueue.this.listener.handlePut( ice );
        }
    }

    /**
     * Description of the Class
     *
     */
    private class RemoveEvent extends AbstractCacheEvent
    {
        private Serializable key;

        /**
         * Constructor for the RemoveEvent object
         *
         * @param key
         * @exception IOException
         */
        RemoveEvent( Serializable key )
            throws IOException
        {
            this.key = key;
        }

        /**
         * Description of the Method
         *
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            CacheEventQueue.this.listener.handleRemove( CacheEventQueue.this.cacheName, key );
        }
    }

    /**
     * Description of the Class
     *
     */
    private class RemoveAllEvent extends AbstractCacheEvent
    {
        /**
         * Description of the Method
         *
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            CacheEventQueue.this.listener.handleRemoveAll( CacheEventQueue.this.cacheName );
        }
    }

    /**
     * Description of the Class
     *
     */
    private class DisposeEvent extends AbstractCacheEvent
    {
        /**
         * Description of the Method
         *
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            CacheEventQueue.this.listener.handleDispose( CacheEventQueue.this.cacheName );
        }
    }
}

