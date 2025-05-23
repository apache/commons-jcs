package org.apache.commons.jcs3.engine;

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

import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.log.Log;

/**
 * This class hands out event Queues. This allows us to change the implementation more easily. You
 * can configure the cache to use a custom type.
 */
public class CacheEventQueueFactory<K, V>
{
    /** The logger. */
    private static final Log log = Log.getLog( CacheEventQueueFactory.class );

    /**
     * Fully configured event queue.
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     * @param threadPoolName null is OK, if not a pooled event queue this is ignored
     * @param poolType single or pooled
     * @return ICacheEventQueue
     */
    public ICacheEventQueue<K, V> createCacheEventQueue( final ICacheListener<K, V> listener, final long listenerId, final String cacheName,
                                                   final int maxFailure, final int waitBeforeRetry, final String threadPoolName,
                                                   final ICacheEventQueue.QueueType poolType )
    {
        log.debug( "threadPoolName = [{0}] poolType = {1}", threadPoolName, poolType );

        ICacheEventQueue<K, V> eventQueue = null;
        if ( poolType == null || ICacheEventQueue.QueueType.SINGLE == poolType )
        {
            eventQueue = new CacheEventQueue<>( listener, listenerId, cacheName, maxFailure, waitBeforeRetry );
        }
        else if ( ICacheEventQueue.QueueType.POOLED == poolType )
        {
            eventQueue = new PooledCacheEventQueue<>( listener, listenerId, cacheName, maxFailure, waitBeforeRetry,
                                                    threadPoolName );
        }

        return eventQueue;
    }

    /**
     * The most commonly used factory method.
     *
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param threadPoolName
     * @param poolType   SINGLE, POOLED
     * @return ICacheEventQueue
     */
    public ICacheEventQueue<K, V> createCacheEventQueue( final ICacheListener<K, V> listener, final long listenerId, final String cacheName,
                                                   final String threadPoolName, final ICacheEventQueue.QueueType poolType )
    {
        return createCacheEventQueue( listener, listenerId, cacheName, 10, 500, threadPoolName, poolType );
    }
}
