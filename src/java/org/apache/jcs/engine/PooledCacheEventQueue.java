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
import org.apache.jcs.utils.threadpool.ThreadPool;
import org.apache.jcs.utils.threadpool.ThreadPoolManager;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 * <p>
 * This is a modified version of the experimental version. It uses a PooledExecutor and a
 * BoundedBuffer to queue up events and execute them as threads become available.
 * <p>
 * The PooledExecutor is static, because presumably these processes will be IO bound, so throwing
 * more than a few threads at them will serve no purpose other than to saturate the IO interface. In
 * light of this, having one thread per region seems unnecessary. This may prove to be false.
 * <p>
 * @author Aaron Smuts
 * @author Travis Savo <tsavo@ifilm.com>
 */
public class PooledCacheEventQueue
    implements ICacheEventQueue
{
    private static final int queueType = POOLED_QUEUE_TYPE;

    private static final Log log = LogFactory.getLog( PooledCacheEventQueue.class );

    // time to wait for an event before snuffing the background thread
    // if the queue is empty.
    // make configurable later
    private int waitToDieMillis = 10000;

    private ICacheListener listener;

    private long listenerId;

    private String cacheName;

    private int maxFailure;

    // in milliseconds
    private int waitBeforeRetry;

    private boolean destroyed = true;

    private boolean working = true;

    // The Thread Pool to execute events with.
    private ThreadPool pool = null;

    /**
     * Constructor for the CacheEventQueue object
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName
     */
    public PooledCacheEventQueue( ICacheListener listener, long listenerId, String cacheName, int maxFailure,
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

        // this will share the same pool with other event queues by default.
        if ( threadPoolName == null )
        {
            threadPoolName = "cache_event_queue";
        }
        pool = ThreadPoolManager.getInstance().getPool( threadPoolName );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructed: " + this );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getQueueType()
     */
    public int getQueueType()
    {
        return queueType;
    }

    /**
     * Event Q is emtpy.
     */
    public synchronized void stopProcessing()
    {
        destroyed = true;
    }

    /**
     * Returns the time to wait for events before killing the background thread.
     * <p>
     * @return the time to wait before shutting down in ms.
     */
    public int getWaitToDieMillis()
    {
        return waitToDieMillis;
    }

    /**
     * Sets the time to wait for events before killing the background thread.
     * <p>
     * @param wtdm
     */
    public void setWaitToDieMillis( int wtdm )
    {
        waitToDieMillis = wtdm;
    }

    /**
     * @return String info.
     */
    public String toString()
    {
        return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" + cacheName + "]";
    }

    /**
     * @return true if not destroyed.
     */
    public boolean isAlive()
    {
        return ( !destroyed );
    }

    /**
     * @param aState
     */
    public void setAlive( boolean aState )
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
     * Destroy the queue. Interrupt all threads.
     */
    public synchronized void destroy()
    {
        if ( !destroyed )
        {
            destroyed = true;
            // TODO decide whether to shutdown or interrupt
            // pool.getPool().shutdownNow();
            pool.getPool().interruptAll();
            if ( log.isInfoEnabled() )
            {
                log.info( "Cache event queue destroyed: " + this );
            }
        }
    }

    /**
     * Constructs a PutEvent for the object and passes it to the event queue.
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
        try
        {
            pool.execute( event );
        }
        catch ( InterruptedException e )
        {
            log.error( e );
        }
    }

    /**
     * @return Statistics info
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.ICacheEventQueue#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Pooled Cache Event Queue" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Working" );
        se.setData( "" + this.working );
        elems.add( se );

        se = new StatElement();
        se.setName( "Destroyed" );
        se.setData( "" + this.isAlive() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Empty" );
        se.setData( "" + this.isEmpty() );
        elems.add( se );

        if ( pool.getQueue() != null )
        {
            if ( pool.getQueue() instanceof BoundedBuffer )
            {
                BoundedBuffer bb = (BoundedBuffer) pool.getQueue();
                se = new StatElement();
                se.setName( "Queue Size" );
                se.setData( "" + bb.size() );
                elems.add( se );

                se = new StatElement();
                se.setName( "Queue Capacity" );
                se.setData( "" + bb.capacity() );
                elems.add( se );
            }
        }

        se = new StatElement();
        se.setName( "Pool Size" );
        se.setData( "" + pool.getPool().getPoolSize() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Maximum Pool Size" );
        se.setData( "" + pool.getPool().getMaximumPoolSize() );
        elems.add( se );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[elems.size()] );
        stats.setStatElements( ses );

        return stats;
    }

    // /////////////////////////// Inner classes /////////////////////////////

    /**
     * Retries before declaring failure.
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private abstract class AbstractCacheEvent
        implements Runnable
    {
        int failures = 0;

        boolean done = false;

        /**
         * Main processing method for the AbstractCacheEvent object. It calls the abstract doRun
         * method that all concrete instances must implement.
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
     * An event that puts an item to a ICacheListener
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class PutEvent
        extends AbstractCacheEvent
    {
        private ICacheElement ice;

        /**
         * Constructor for the PutEvent object
         * @param ice
         * @exception IOException
         */
        PutEvent( ICacheElement ice )
            throws IOException
        {
            this.ice = ice;
        }

        /**
         * Tells the ICacheListener to handle the put.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handlePut( ice );
        }

        public String toString()
        {
            return new StringBuffer( "PutEvent for key: " ).append( ice.getKey() ).append( " value: " )
                .append( ice.getVal() ).toString();
        }

    }

    /**
     * An event that knows how to call remove on an ICacheListener
     * <p>
     * @author asmuts
     * @created January 15, 2002
     */
    private class RemoveEvent
        extends AbstractCacheEvent
    {
        private Serializable key;

        /**
         * Constructor for the RemoveEvent object
         * @param key
         * @exception IOException
         */
        RemoveEvent( Serializable key )
            throws IOException
        {
            this.key = key;
        }

        /**
         * Calls remove on the listner.
         * <p>
         * @exception IOException
         */
        protected void doRun()
            throws IOException
        {
            listener.handleRemove( cacheName, key );
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return new StringBuffer( "RemoveEvent for " ).append( key ).toString();
        }

    }

    /**
     * An event that knows how to call remove all on an ICacheListener
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

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return "RemoveAllEvent";
        }

    }

    /**
     * The Event put into the queue for dispose requests.
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

        public String toString()
        {
            return "DisposeEvent";
        }
    }

    /**
     * @return whether or not the queue is functional
     */
    public boolean isWorking()
    {
        return working;
    }

    /**
     * @param isWorkingArg whether the queue is functional
     */
    public void setWorking( boolean isWorkingArg )
    {
        working = isWorkingArg;
    }

    /**
     * If the Queue is using a bounded channel we can determine the size. If it is zero or we can't
     * determine the size, we return true.
     * <p>
     * @return whether or not there are items in the queue
     */
    public boolean isEmpty()
    {
        if ( pool.getQueue() == null )
        {
            return pool.getQueue().peek() == null;
        }
        else
        {
            if ( pool.getQueue() instanceof BoundedBuffer )
            {
                BoundedBuffer bb = (BoundedBuffer) pool.getQueue();
                return bb.size() == 0;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Returns the number of elements in the queue. If the queue cannot determine the size
     * accurately it will return 1.
     * <p>
     * @return number of items in the queue.
     */
    public int size()
    {
        if ( pool.getQueue() == null )
        {
            return pool.getQueue().peek() == null ? 0 : 1;
        }
        else
        {
            if ( pool.getQueue() instanceof BoundedBuffer )
            {
                BoundedBuffer bb = (BoundedBuffer) pool.getQueue();
                return bb.size();
            }
            else
            {
                return 1;
            }
        }
    }
}
