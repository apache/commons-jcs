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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * An abstract base class to the different implementations
 */
public abstract class AbstractCacheEventQueue<K, V>
    implements ICacheEventQueue<K, V>
{
    /**
     * Retries before declaring failure.
     */
    protected class AbstractCacheEvent<T> implements Runnable
    {
        protected final T eventData;
        private final IOExConsumer<T> eventRun;

        /**
         * Constructor
         *
         * @param eventData data of the event
         * @param eventRun operation to apply on the event
         */
        protected AbstractCacheEvent(final T eventData, final IOExConsumer<T> eventRun)
        {
            this.eventData = eventData;
            this.eventRun = eventRun;
        }

        /**
         * Main processing method for the AbstractCacheEvent object
         */
        @Override
        public void run()
        {
            for (int failures = 0; failures < maxFailure; failures++)
            {
                try
                {
                    eventRun.accept(eventData);
                    return;
                }
                catch (final Throwable e)
                {
                    log.warn("Error while running event from Queue: {0}. "
                            + "Retrying...", this, e);
                }

                try
                {
                    Thread.sleep( waitBeforeRetry );
                }
                catch ( final InterruptedException ie )
                {
                    log.warn("Interrupted while sleeping for retry on event "
                            + "{0}.", this, ie);
                    break;
                }
            }

            log.warn( "Dropping Event and marking Event Queue {0} as "
                    + "non-functional.", this );
            destroy();
        }
    }

    /**
     * The cache should be disposed when this event is processed.
     */
    protected class DisposeEvent extends AbstractCacheEvent<String>
    {
        /**
         * Constructor for the DisposeEvent object.
         */
        protected DisposeEvent()
        {
            super(cacheName, listener::handleDispose);
        }

        /**
         * For debugging.
         *
         * @return The name of the event.
         */
        @Override
        public String toString()
        {
            return "DisposeEvent";
        }
    }

    // /////////////////////////// Inner classes /////////////////////////////
    protected interface IOExConsumer<T>
    {
        static <T> Consumer<T> unchecked(IOExConsumer<T> consumer)
        {
            return t -> {
                try
                {
                    consumer.accept(t);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            };
        }

        void accept(T t) throws IOException;
    }

    /**
     * An element should be put in the cache.
     */
    protected class PutEvent extends AbstractCacheEvent<ICacheElement<K, V>>
    {
        /**
         * Constructor for the PutEvent object.
         * <p>
         * @param ice a cache element
         */
        PutEvent( final ICacheElement<K, V> ice )
        {
            super(ice, listener::handlePut);
        }

        /**
         * For debugging.
         * <p>
         * @return Info on the key and value.
         */
        @Override
        public String toString()
        {
            return "PutEvent for key: " + eventData.getKey() + " value: " + eventData.getVal();
        }
    }

    /**
     * All elements should be removed from the cache when this event is processed.
     */
    protected class RemoveAllEvent extends AbstractCacheEvent<String>
    {
        /**
         * Constructor for the RemoveAllEvent object.
         */
        protected RemoveAllEvent()
        {
            super(cacheName, listener::handleRemoveAll);
        }

        /**
         * For debugging.
         * <p>
         * @return The name of the event.
         */
        @Override
        public String toString()
        {
            return "RemoveAllEvent";
        }
    }

    /**
     * An element should be removed from the cache.
     */
    protected class RemoveEvent extends AbstractCacheEvent<K>
    {
        /**
         * Constructor for the RemoveEvent object
         * <p>
         * @param key
         */
        RemoveEvent( final K key )
        {
            super(key, AbstractCacheEventQueue.this::remove);
        }

        /**
         * For debugging.
         * <p>
         * @return Info on the key to remove.
         */
        @Override
        public String toString()
        {
            return "RemoveEvent for " + eventData;
        }
    }

    /** The logger. */
    private static final Log log = LogManager.getLog( AbstractCacheEventQueue.class );

    /** default */
    protected static final int DEFAULT_WAIT_TO_DIE_MILLIS = 10000;

    /**
     * time to wait for an event before snuffing the background thread if the queue is empty. make
     * configurable later
     */
    private int waitToDieMillis = DEFAULT_WAIT_TO_DIE_MILLIS;

    /**
     * When the events are pulled off the queue, then tell the listener to handle the specific event
     * type. The work is done by the listener.
     */
    private ICacheListener<K, V> listener;

    /** Id of the listener registered with this queue */
    private long listenerId;

    /** The cache region name, if applicable. */
    private String cacheName;

    /** Maximum number of failures before we buy the farm. */
    private int maxFailure;

    /** in milliseconds */
    private int waitBeforeRetry;

    /**
     * This means that the queue is functional. If we reached the max number of failures, the queue
     * is marked as non functional and will never work again.
     */
    private final AtomicBoolean working = new AtomicBoolean(true);

    /**
     * This adds a dispose event to the queue. When it is processed, the cache is shut down
     */
    @Override
    public void addDisposeEvent()
    {
        put( new DisposeEvent() );
    }

    /**
     * This adds a put event to the queue. When it is processed, the element will be put to the
     * listener.
     * <p>
     * @param ce The feature to be added to the PutEvent attribute
     * @throws IOException
     */
    @Override
    public void addPutEvent( final ICacheElement<K, V> ce )
    {
        put( new PutEvent( ce ) );
    }

    /**
     * This adds a remove all event to the queue. When it is processed, all elements will be removed
     * from the cache.
     */
    @Override
    public void addRemoveAllEvent()
    {
        put( new RemoveAllEvent() );
    }

    /**
     * This adds a remove event to the queue. When processed the listener's remove method will be
     * called for the key.
     * <p>
     * @param key The feature to be added to the RemoveEvent attribute
     * @throws IOException
     */
    @Override
    public void addRemoveEvent( final K key )
    {
        put( new RemoveEvent( key ) );
    }

    /**
     * @return the cacheName
     */
    protected String getCacheName()
    {
        return cacheName;
    }

    /**
     * @return The listenerId value
     */
    @Override
    public long getListenerId()
    {
        return listenerId;
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
     * Initializes the queue.
     * <p>
     * @param listener
     * @param listenerId
     * @param cacheName
     * @param maxFailure
     * @param waitBeforeRetry
     */
    protected void initialize( final ICacheListener<K, V> listener, final long listenerId, final String cacheName, final int maxFailure,
                            final int waitBeforeRetry)
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

        log.debug( "Constructed: {0}", this );
    }

    /**
     * @return whether the queue is functional.
     */
    @Override
    public boolean isWorking()
    {
        return working.get();
    }

    /**
     * Adds an event to the queue.
     * <p>
     * @param event
     */
    protected abstract void put( AbstractCacheEvent<?> event );

    /**
     * Call remove on the listener.
     * Helper method to allow method reference in RemoveEvent
     * <p>
     * @throws IOException
     */
    private void remove(K key) throws IOException
    {
        listener.handleRemove( cacheName, key );
    }

    /**
     * Sets the time to wait for events before killing the background thread.
     * <p>
     * @param wtdm the ms for the q to sit idle.
     */
    public void setWaitToDieMillis( final int wtdm )
    {
        waitToDieMillis = wtdm;
    }

    /**
     * This means that the queue is functional. If we reached the max number of failures, the queue
     * is marked as non functional and will never work again.
     * <p>
     * @param b
     */
    public void setWorking( final boolean b )
    {
        working.set(b);
    }

    /**
     * Creates a brief string identifying the listener and the region.
     * <p>
     * @return String debugging info.
     */
    @Override
    public String toString()
    {
        return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" + cacheName + "]";
    }
}
