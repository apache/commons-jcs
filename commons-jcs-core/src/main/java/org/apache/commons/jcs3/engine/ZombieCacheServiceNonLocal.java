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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;

/**
 * Zombie adapter for the non local cache services. It just balks if there is no queue configured.
 * <p>
 * If a queue is configured, then events will be added to the queue. The idea is that when proper
 * operation is restored, the non local cache will walk the queue. The queue must be bounded so it
 * does not eat memory.
 * <p>
 * This originated in the remote cache.
 */
public class ZombieCacheServiceNonLocal<K, V>
    extends ZombieCacheService<K, V>
    implements ICacheServiceNonLocal<K, V>
{
    /** The logger */
    private static final Log log = LogManager.getLog( ZombieCacheServiceNonLocal.class );

    /** How big can the queue grow. */
    private int maxQueueSize;

    /** The queue */
    private final ConcurrentLinkedQueue<ZombieEvent> queue;

    /**
     * Default.
     */
    public ZombieCacheServiceNonLocal()
    {
        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Sets the maximum number of items that will be allowed on the queue.
     * <p>
     * @param maxQueueSize
     */
    public ZombieCacheServiceNonLocal( final int maxQueueSize )
    {
        this();
        this.maxQueueSize = maxQueueSize;
    }

    /**
     * Gets the number of items on the queue.
     * <p>
     * @return size of the queue.
     */
    public int getQueueSize()
    {
        return queue.size();
    }

    private void addQueue(final ZombieEvent event)
    {
        queue.add(event);
        if (queue.size() > maxQueueSize)
        {
            queue.poll(); // drop oldest entry
        }
    }

    /**
     * Adds an update event to the queue if the maxSize is greater than 0;
     * <p>
     * @param item ICacheElement
     * @param listenerId - identifies the caller.
     */
    @Override
    public void update( final ICacheElement<K, V> item, final long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            final PutEvent<K, V> event = new PutEvent<>( item, listenerId );
            addQueue( event );
        }
        // Zombies have no inner life
    }

    /**
     * Adds a removeAll event to the queue if the maxSize is greater than 0;
     * <p>
     * @param cacheName - region name
     * @param key - item key
     * @param listenerId - identifies the caller.
     */
    @Override
    public void remove( final String cacheName, final K key, final long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            final RemoveEvent<K> event = new RemoveEvent<>( cacheName, key, listenerId );
            addQueue( event );
        }
        // Zombies have no inner life
    }

    /**
     * Adds a removeAll event to the queue if the maxSize is greater than 0;
     * <p>
     * @param cacheName - name of the region
     * @param listenerId - identifies the caller.
     */
    @Override
    public void removeAll( final String cacheName, final long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            final RemoveAllEvent event = new RemoveAllEvent( cacheName, listenerId );
            addQueue( event );
        }
        // Zombies have no inner life
    }

    /**
     * Does nothing. Gets are synchronous and cannot be added to a queue.
     * <p>
     * @param cacheName - region name
     * @param key - item key
     * @param requesterId - identifies the caller.
     * @return null
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        // Zombies have no inner life
        return null;
    }

    /**
     * Does nothing.
     * <p>
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return empty map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        return Collections.emptyMap();
    }

    /**
     * @param cacheName - region name
     * @param keys - item key
     * @param requesterId - identity of the caller
     * @return an empty map. zombies have no internal data
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
    {
        return new HashMap<>();
    }

    /**
     * Does nothing.
     * <p>
     * @param cacheName - region name
     * @return empty set
     */
    @Override
    public Set<K> getKeySet( final String cacheName )
    {
        return Collections.emptySet();
    }

    /**
     * Walk the queue, calling the service for each queue operation.
     * <p>
     * @param service
     * @throws Exception
     */
    public synchronized void propagateEvents( final ICacheServiceNonLocal<K, V> service )
        throws Exception
    {
        int cnt = 0;
        log.info( "Propagating events to the new ICacheServiceNonLocal." );
        final ElapsedTimer timer = new ElapsedTimer();
        while ( !queue.isEmpty() )
        {
            cnt++;

            // for each item, call the appropriate service method
            final ZombieEvent event = queue.poll();

            if ( event instanceof PutEvent )
            {
                @SuppressWarnings("unchecked") // Type checked by instanceof
                final
                PutEvent<K, V> putEvent = (PutEvent<K, V>) event;
                service.update( putEvent.element, event.requesterId );
            }
            else if ( event instanceof RemoveEvent )
            {
                @SuppressWarnings("unchecked") // Type checked by instanceof
                final
                RemoveEvent<K> removeEvent = (RemoveEvent<K>) event;
                service.remove( event.cacheName, removeEvent.key, event.requesterId );
            }
            else if ( event instanceof RemoveAllEvent )
            {
                service.removeAll( event.cacheName, event.requesterId );
            }
        }
        log.info( "Propagated {0} events to the new ICacheServiceNonLocal in {1}",
                cnt, timer.getElapsedTimeString() );
    }

    /**
     * Base of the other events.
     */
    protected static abstract class ZombieEvent
    {
        /** The name of the region. */
        String cacheName;

        /** The id of the requester */
        long requesterId;
    }

    /**
     * A basic put event.
     */
    private static class PutEvent<K, V>
        extends ZombieEvent
    {
        /** The element to put */
        final ICacheElement<K, V> element;

        /**
         * Set the element
         * @param element
         * @param requesterId
         */
        public PutEvent( final ICacheElement<K, V> element, final long requesterId )
        {
            this.requesterId = requesterId;
            this.element = element;
        }
    }

    /**
     * A basic Remove event.
     */
    private static class RemoveEvent<K>
        extends ZombieEvent
    {
        /** The key to remove */
        final K key;

        /**
         * Set the element
         * @param cacheName
         * @param key
         * @param requesterId
         */
        public RemoveEvent( final String cacheName, final K key, final long requesterId )
        {
            this.cacheName = cacheName;
            this.requesterId = requesterId;
            this.key = key;
        }
    }

    /**
     * A basic RemoveAll event.
     */
    private static class RemoveAllEvent
        extends ZombieEvent
    {
        /**
         * @param cacheName
         * @param requesterId
         */
        public RemoveAllEvent( final String cacheName, final long requesterId )
        {
            this.cacheName = cacheName;
            this.requesterId = requesterId;
        }
    }
}
