package org.apache.commons.jcs3.engine;

import java.util.concurrent.ExecutorService;

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

import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;
import org.apache.commons.jcs3.utils.threadpool.ThreadPoolManager;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 */
public class CacheEventQueue<K, V>
    extends PooledCacheEventQueue<K, V>
{
    /**
     * Constructs with the specified listener and the cache name.
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     */
    public CacheEventQueue( final ICacheListener<K, V> listener, final long listenerId, final String cacheName )
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
    public CacheEventQueue( final ICacheListener<K, V> listener, final long listenerId, final String cacheName, final int maxFailure,
                            final int waitBeforeRetry )
    {
        super( listener, listenerId, cacheName, maxFailure, waitBeforeRetry, null );
    }

    /**
     * Create the thread pool.
     * <p>
     * @param threadPoolName
     * @since 3.1
     */
    @Override
    protected ExecutorService createPool(final String threadPoolName)
    {
        // create a default pool with one worker thread to mimic the SINGLE queue behavior
        return ThreadPoolManager.getInstance().createPool(
                new PoolConfiguration(false, 0, 1, 1, getWaitToDieMillis(), WhenBlockedPolicy.BLOCK, 1),
                "CacheEventQueue.QProcessor-" + getCacheName());
    }

    /**
     * What type of queue is this.
     * <p>
     * @return queueType
     */
    @Override
    public QueueType getQueueType()
    {
        /** The type of queue -- there are pooled and single */
        return QueueType.SINGLE;
    }
}
