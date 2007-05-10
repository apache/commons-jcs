package org.apache.jcs.auxiliary.remote;

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
import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.ZombieCacheService;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.utils.struct.BoundedQueue;
import org.apache.jcs.utils.timing.ElapsedTimer;

/**
 * Zombie adapter for the remote cache service. It just balks if there is no queue configured. If a
 * queue is configured, then events will be added to the queue. The idea is that when proper
 * operation is restored, the remote cache will walk the queue. The queue must be bounded so it does
 * not eat memory.
 * <p>
 * Much of this is potentially reusable.
 * <p>
 * TODO figure out a way to get the propagate method into an interface for Zombies.
 */
public class ZombieRemoteCacheService
    extends ZombieCacheService
    implements IRemoteCacheService
{
    private final static Log log = LogFactory.getLog( ZombieRemoteCacheService.class );

    private int maxQueueSize = 0;

    private BoundedQueue queue;

    /**
     * Default.
     */
    public ZombieRemoteCacheService()
    {
        queue = new BoundedQueue( 0 );
    }

    /**
     * Sets the maximum number of items that will be allowed on the queue.
     * <p>
     * @param maxQueueSize
     */
    public ZombieRemoteCacheService( int maxQueueSize )
    {
        this.maxQueueSize = maxQueueSize;
        queue = new BoundedQueue( maxQueueSize );
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

    /**
     * Adds an update event to the queue if the maxSize is greater than 0;
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#update(org.apache.jcs.engine.behavior.ICacheElement,
     *      long)
     */
    public void update( ICacheElement item, long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            PutEvent event = new PutEvent( item, listenerId );
            queue.add( event );
        }
        // Zombies have no inner life
        return;
    }

    /**
     * Adds a removeAll event to the queue if the maxSize is greater than 0;
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#remove(java.lang.String,
     *      java.io.Serializable, long)
     */
    public void remove( String cacheName, Serializable key, long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            RemoveEvent event = new RemoveEvent( cacheName, key, listenerId );
            queue.add( event );
        }
        // Zombies have no inner life
        return;
    }

    /**
     * Adds a removeAll event to the queue if the maxSize is greater than 0;
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#removeAll(java.lang.String,
     *      long)
     */
    public void removeAll( String cacheName, long listenerId )
    {
        if ( maxQueueSize > 0 )
        {
            RemoveAllEvent event = new RemoveAllEvent( cacheName, listenerId );
            queue.add( event );
        }
        // Zombies have no inner life
        return;
    }

    /**
     * Does nothing. Gets are synchronous and cannot be added to a queue.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#get(java.lang.String,
     *      java.io.Serializable, long)
     */
    public ICacheElement get( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        // Zombies have no inner life
        return null;
    }

    /**
     * Does nothing.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService#getGroupKeys(java.lang.String,
     *      java.lang.String)
     */
    public Set getGroupKeys( String cacheName, String groupName )
    {
        return Collections.EMPTY_SET;
    }

    /**
     * Walk the queue, calling the service for each queue operation.
     * <p>
     * @param service
     * @throws Exception
     */
    protected void propagateEvents( IRemoteCacheService service )
        throws Exception
    {
        int cnt = 0;
        if ( log.isInfoEnabled() )
        {
            log.info( "Propagating events to the new RemoteService." );
        }
        ElapsedTimer timer = new ElapsedTimer();
        while ( !queue.isEmpty() )
        {
            cnt++;

            // for each item, call the appropriate service method
            ZombieEvent event = (ZombieEvent) queue.take();

            if ( event instanceof PutEvent )
            {
                PutEvent putEvent = (PutEvent) event;
                service.update( putEvent.element, event.requesterId );
            }
            else if ( event instanceof RemoveEvent )
            {
                RemoveEvent removeEvent = (RemoveEvent) event;
                service.remove( event.cacheName, removeEvent.key, event.requesterId );
            }
            else if ( event instanceof RemoveAllEvent )
            {
                service.removeAll( event.cacheName, event.requesterId );
            }
        }
        if ( log.isInfoEnabled() )
        {
            log.info( "Propagated " + cnt + " events to the new RemoteService in " + timer.getElapsedTimeString() );
        }
    }

    /**
     * Base of the other events.
     */
    private abstract class ZombieEvent
    {
        String cacheName;

        long requesterId;
    }

    /**
     * A basic put event.
     */
    private class PutEvent
        extends ZombieEvent
    {
        ICacheElement element;

        /**
         * Set the element
         * @param element
         * @param requesterId
         */
        public PutEvent( ICacheElement element, long requesterId )
        {
            this.requesterId = requesterId;
            this.element = element;
        }
    }

    /**
     * A basic Remove event.
     */
    private class RemoveEvent
        extends ZombieEvent
    {
        Serializable key;

        /**
         * Set the element
         * @param cacheName
         * @param key
         * @param requesterId
         */
        public RemoveEvent( String cacheName, Serializable key, long requesterId )
        {
            this.cacheName = cacheName;
            this.requesterId = requesterId;
            this.key = key;
        }
    }

    /**
     * A basic RemoveAll event.
     */
    private class RemoveAllEvent
        extends ZombieEvent
    {
        /**
         * @param cacheName
         * @param requesterId
         */
        public RemoveAllEvent( String cacheName, long requesterId )
        {
            this.cacheName = cacheName;
            this.requesterId = requesterId;
        }
    }
}
