package org.apache.jcs.engine;

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

    private Object queueLock = new Object();

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

        t = new QProcessor();
        t.start();

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
        if ( !destroyed )
        {
            destroyed = true;

            // sychronize on queue so the thread will not wait forever,
            // and then interrupt the QueueProcessor

            synchronized ( queueLock )
            {
                t.interrupt();
            }

            t = null;

            log.info( "Cache event queue destroyed: " + this );
        }
    }

    /**
     * @return
     */
    public String toString()
    {
        return "listenerId=" + listenerId + ", cacheName=" + cacheName;
    }

    /**
     * @return The {3} value
     */
    public boolean isAlive()
    {
        return ( !destroyed );
    }

    /**
     * @return The {3} value
     */
    public byte getListenerId()
    {
        return listenerId;
    }

    /**
     * @param ce The feature to be added to the PutEvent attribute
     * @exception IOException
     */
    public synchronized void addPutEvent( ICacheElement ce )
        throws IOException
    {
        if ( !destroyed )
        {
            put( new PutEvent( ce ) );
        }
    }

    /**
     * @param key The feature to be added to the RemoveEvent attribute
     * @exception IOException
     */
    public void addRemoveEvent( Serializable key )
        throws IOException
    {
        if ( !destroyed )
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
        if ( !destroyed )
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
        if ( !destroyed )
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

        synchronized ( queueLock )
        {
            tail.next = newNode;
            tail = newNode;

            queueLock.notify();
        }
    }

    private AbstractCacheEvent take() throws InterruptedException
    {
        synchronized ( queueLock )
        {
            // wait until there is something to read

            while ( head == tail )
            {
                queueLock.wait();
            }

            // we have the lock, and the list is not empty

            Node node = head.next;

            // This is an awful bug.  This will always return null.
            // This make the event Q and event destroyer.
            //AbstractCacheEvent value = head.event;

            // corrected
            AbstractCacheEvent value = node.event;

            if ( log.isDebugEnabled() )
            {
              log.debug( "head.event = " + head.event );
              log.debug( "node.event = " + node.event );
            }

            // Node becomes the new head (head is always empty)

            node.event = null;
            head = node;

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
     * @author asmuts
     * @created January 15, 2002
     */
    private class QProcessor extends Thread
    {
        /**
         * Constructor for the QProcessor object
         */
        QProcessor()
        {
            super( "CacheEventQueue.QProcessor-" + ( ++processorInstanceCount ) );

            setDaemon( true );
        }

        /**
         * Main processing method for the QProcessor object
         */
        public void run()
        {
            AbstractCacheEvent r = null;

            while ( !destroyed )
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

                if ( !destroyed && r != null )
                {
                    r.run();
                }
            }
            // declare failure as listener is permanently unreachable.
            // queue = null;
            listener = null;
            // The listener failure logging more the problem of the user
            // of the q.
            log.info( "QProcessor exiting for " + CacheEventQueue.this );
        }
    }

    /**
     * Retries before declaring failure.
     *
     * @author asmuts
     * @created January 15, 2002
     */
    private abstract class AbstractCacheEvent implements Runnable
    {
        /**
         * Main processing method for the AbstractCacheEvent object
         */
        public void run()
        {
            IOException ex = null;

            while ( !destroyed && failureCount <= maxFailure )
            {
                try
                {
                    ex = null;
                    doRun();
                    failureCount = 0;
                    return;
                    // happy and done.
                }
                catch ( IOException e )
                {
                    failureCount++;
                    ex = e;
                }
                // Let's get idle for a while before retry.
                if ( !destroyed && failureCount <= maxFailure )
                {
                    try
                    {
                        log.warn( "...retrying propagation " + CacheEventQueue.this + "..." + failureCount );
                        Thread.currentThread().sleep( waitBeforeRetry );
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
     * @author asmuts
     * @created January 15, 2002
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
            listener.handlePut( ice );
        }
    }

    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
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
            listener.handleRemove( cacheName, key );
        }
    }

    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
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
            listener.handleRemoveAll( cacheName );
        }
    }

    /**
     * Description of the Class
     *
     * @author asmuts
     * @created January 15, 2002
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
            listener.handleDispose( cacheName );
        }
    }
}

