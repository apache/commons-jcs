package org.apache.commons.jcs4.engine;

import java.time.Duration;

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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs4.engine.behavior.ICacheListener;
import org.apache.commons.jcs4.engine.stats.Stats;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;
import org.apache.commons.jcs4.log.Log;
import org.apache.commons.jcs4.utils.threadpool.ThreadPoolManager;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 * <p>
 * This is a modified version of the experimental version. It uses a PooledExecutor and a
 * BoundedBuffer to queue up events and execute them as threads become available.
 * <p>
 * The PooledExecutor is static, because presumably these processes will be IO bound, so throwing
 * more than a few threads at them will serve no purpose other than to saturate the IO interface. In
 * light of this, having one thread per region seems unnecessary. This may prove to be false.
 */
public class PooledCacheEventQueue<K, V>
    extends AbstractCacheEventQueue<K, V>
{
    /** The logger. */
    private static final Log log = Log.getLog( PooledCacheEventQueue.class );

    /** The Thread Pool to execute events with. */
    protected ExecutorService pool;

    /** The Thread Pool queue */
    protected BlockingQueue<Runnable> queue;

    /**
     * Constructor for the CacheEventQueue object
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName
     */
    public PooledCacheEventQueue( final ICacheListener<K, V> listener, final long listenerId, final String cacheName, final int maxFailure,
                                  final int waitBeforeRetry, final String threadPoolName )
    {
        initialize( listener, listenerId, cacheName, maxFailure, waitBeforeRetry, threadPoolName );
    }

    /**
     * Create the thread pool.
     *
     * @param threadPoolName
     * @since 3.1
     */
    protected ExecutorService createPool(final String threadPoolName)
    {
        // this will share the same pool with other event queues by default.
        return ThreadPoolManager.getInstance().getExecutorService(
                threadPoolName == null ? "cache_event_queue" : threadPoolName );
    }

    /**
     * Destroy the queue
     *
     * @param wait time to wait for the queue to drain
     */
    @Override
    public synchronized void destroy(final Duration wait)
    {
        if ( isWorking() )
        {
            setWorking(false);
            pool.shutdown();

            if (wait.toSeconds() > 0)
            {
                try
                {
                    if (!pool.awaitTermination(wait.toSeconds(), TimeUnit.SECONDS))
                    {
                        log.info( "No longer waiting for event queue to finish: {0}",
                                this::getStatistics);
                    }
                }
                catch (final InterruptedException e)
                {
                    // ignore
                }
            }
            log.info( "Cache event queue destroyed: {0}", this );
        }
    }

    /**
     * @return the queue type
     */
    @Override
    public QueueType getQueueType()
    {
        /** The type of queue -- there are pooled and single */
        return QueueType.POOLED;
    }

    /**
     * @return IStats
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats("Pooled Cache Event Queue");

        stats.addStatElement("Working", Boolean.valueOf(isWorking()) );
        stats.addStatElement("Empty", Boolean.valueOf(this.isEmpty()) );

        if ( queue != null )
        {
            stats.addStatElement("Queue Size", Integer.valueOf(queue.size()) );
            stats.addStatElement("Queue Capacity", Integer.valueOf(queue.remainingCapacity()) );
        }

        return stats;
    }

    /**
     * Initializes the queue.
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName
     */
    protected void initialize( final ICacheListener<K, V> listener, final long listenerId, final String cacheName, final int maxFailure,
                            final int waitBeforeRetry, final String threadPoolName )
    {
        super.initialize(listener, listenerId, cacheName, maxFailure, waitBeforeRetry);

        pool = createPool(threadPoolName);

        if (pool instanceof ThreadPoolExecutor tpe)
        {
        	queue = tpe.getQueue();
        }
    }

    /**
     * If the Queue is using a bounded channel we can determine the size. If it is zero or we can't
     * determine the size, we return true.
     *
     * @return whether or not there are items in the queue
     */
    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Adds an event to the queue.
     *
     * @param event
     */
    @Override
    protected void put( final AbstractCacheEvent<?> event )
    {
        pool.execute( event );
    }

    /**
     * Returns the number of elements in the queue. If the queue cannot determine the size
     * accurately it will return 0.
     *
     * @return number of items in the queue.
     */
    @Override
    public int size()
    {
        if ( queue == null )
        {
            return 0;
        }
        return queue.size();
    }
}
