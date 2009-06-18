package org.apache.jcs.engine.control.event;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEventQueue;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 */
public class ElementEventQueue
    implements IElementEventQueue
{
    /** The logger */
    private final static Log log = LogFactory.getLog( ElementEventQueue.class );

    /** The cache (region) name. */
    private String cacheName;

    /** default */
    private static final int DEFAULT_WAIT_TO_DIE_MILLIS = 10000;

    /**
     * time to wait for an event before snuffing the background thread if the queue is empty. make
     * configurable later
     */
    private int waitToDieMillis = DEFAULT_WAIT_TO_DIE_MILLIS;

    /** shutdown or not */
    private boolean destroyed = false;

    /** The worker thread. */
    private Thread processorThread;

    /** Internal queue implementation */
    private Object queueLock = new Object();

    /** Dummy node */
    private Node head = new Node();

    /** tail of the doubly linked list */
    private Node tail = head;

    /** Number of items in the queue */
    private int size = 0;

    /**
     * Constructor for the ElementEventQueue object
     * <p>
     * @param cacheName
     */
    public ElementEventQueue( String cacheName )
    {
        this.cacheName = cacheName;

        processorThread = new QProcessor( this );
        processorThread.start();

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructed: " + this );
        }
    }

    /**
     * Event Q is empty.
     */
    public synchronized void destroy()
    {
        if ( !destroyed )
        {
            destroyed = true;

            // synchronize on queue so the thread will not wait forever,
            // and then interrupt the QueueProcessor
            synchronized ( queueLock )
            {
                processorThread.interrupt();
            }

            processorThread = null;

            if ( log.isInfoEnabled() )
            {
                log.info( "Element event queue destroyed: " + this );
            }
        }
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
     * <p>
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
     * @return the region name for the event queue
     */
    public String toString()
    {
        return "cacheName=" + cacheName;
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

    /**
     * @return The destroyed value
     */
    public boolean isAlive()
    {
        return ( !destroyed );
    }

    /**
     * Adds an ElementEvent to be handled
     * @param hand The IElementEventHandler
     * @param event The IElementEventHandler IElementEvent event
     * @throws IOException
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "Adding Event Handler to QUEUE, !destroyed = " + !destroyed );
        }

        if ( !destroyed )
        {
            ElementEventRunner runner = new ElementEventRunner( hand, event );

            if ( log.isDebugEnabled() )
            {
                log.debug( "runner = " + runner );
            }

            put( runner );
        }
    }

    /**
     * Adds an event to the queue.
     * @param event
     */
    private void put( AbstractElementEventRunner event )
    {
        Node newNode = new Node();

        newNode.event = event;

        synchronized ( queueLock )
        {
            size++;
            tail.next = newNode;
            tail = newNode;
            if ( !isAlive() )
            {
                destroyed = false;
                processorThread = new QProcessor( this );
                processorThread.start();
            }
            else
            {
                queueLock.notify();
            }
            queueLock.notify();
        }
    }

    /**
     * Returns the next item on the queue, or waits if empty.
     * <p>
     * @return AbstractElementEventRunner
     */
    private AbstractElementEventRunner take()
    {
        synchronized ( queueLock )
        {
            // wait until there is something to read
            if ( head == tail )
            {
                return null;
            }

            Node node = head.next;

            AbstractElementEventRunner value = node.event;

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

    // /////////////////////////// Inner classes /////////////////////////////

    /** A node in the queue. These are chained forming a singly linked list */
    private static class Node
    {
        /** The next node. */
        Node next = null;

        /** The event to run */
        ElementEventQueue.AbstractElementEventRunner event = null;
    }

    /**
     */
    private class QProcessor
        extends Thread
    {
        /** The event queue */
        ElementEventQueue queue;

        /**
         * Constructor for the QProcessor object
         * <p>
         * @param aQueue 
         */
        QProcessor( ElementEventQueue aQueue )
        {
            super( "ElementEventQueue.QProcessor-" + aQueue.cacheName );

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
            AbstractElementEventRunner event = null;

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

                if ( queue.isAlive() && event != null )
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
     */
    private abstract class AbstractElementEventRunner
        implements Runnable
    {
        /**
         * Main processing method for the AbstractElementEvent object
         */
        public void run()
        {
            IOException ex = null;

            try
            {
                ex = null;
                doRun();
                return;
                // happy and done.
            }
            catch ( IOException e )
            {
                ex = e;
            }

            // Too bad. The handler has problems.
            if ( ex != null )
            {
                log.warn( "Giving up element event handling " + ElementEventQueue.this, ex );

            }
            return;
        }

        /**
         * This will do the work or trigger the work to be done.
         * <p>
         * @exception IOException
         */
        protected abstract void doRun()
            throws IOException;
    }

    /**
     * ElementEventRunner.
     */
    private class ElementEventRunner
        extends AbstractElementEventRunner
    {
        /** the handler */
        private IElementEventHandler hand;

        /** event */
        private IElementEvent event;

        /**
         * Constructor for the PutEvent object.
         * <p>
         * @param hand
         * @param event
         * @exception IOException
         */
        ElementEventRunner( IElementEventHandler hand, IElementEvent event )
            throws IOException
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Constructing " + this );
            }
            this.hand = hand;
            this.event = event;
        }

        /**
         * Tells the handler to handle the event.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            hand.handleElementEvent( event );
        }
    }
}
