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

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * An event queue is used to propagate ordered cache events to one and only one
 * target listener.
 * 
 * This is a modified version of the experimental version. It uses a
 * PooledExecutor and a BoundedBuffer to queue up events and execute them as
 * threads become available.
 * 
 * The PooledExecutor is static, because presumably these processes will be IO
 * bound, so throwing more than a few threads at them will serve no purpose
 * other than to saturate the IO interface. In light of this, having one thread
 * per region seems unnecessary. This may prove to be false.
 * 
 * @author Aaron Smuts
 * @author Travis Savo <tsavo@ifilm.com>
 *  
 */
public class CacheEventQueue implements ICacheEventQueue {
    private static final Log log = LogFactory.getLog(CacheEventQueue.class);

    // time to wait for an event before snuffing the background thread
    // if the queue is empty.
    // make configurable later
    private static int waitToDieMillis = 10000;

    private ICacheListener listener;

    private long listenerId;

    private String cacheName;

    private int failureCount;

    private int maxFailure;

    // in milliseconds
    private int waitBeforeRetry;

    private boolean destroyed = true;

    private boolean working = true;

    private Thread processorThread;

    //BoundedBuffer to hold queued events.
    //TODO: Make this configurable
    private static final BoundedBuffer queue = new BoundedBuffer(2000);

    //The Thread Pool to execute events with.
    //TODO: Make the size of the pool configurable
    private static PooledExecutor pool = new PooledExecutor(queue, 5);

    static {
        //When our pool is filling up too fast, we should ditch the oldest
        // event in favor of the newer ones.
        //TODO: Make this configurable
        pool.discardOldestWhenBlocked();
        //How long to keep unused threads around
        //TODO: Make this configurable
        pool.setKeepAliveTime(waitToDieMillis);
    }

    /**
     * Constructs with the specified listener and the cache name.
     * 
     * @param listener
     * @param listenerId
     * @param cacheName
     */
    public CacheEventQueue(ICacheListener listener, long listenerId, String cacheName) {
        this(listener, listenerId, cacheName, 10, 500);
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
    public CacheEventQueue(ICacheListener listener, long listenerId, String cacheName, int maxFailure,
            int waitBeforeRetry) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }

        this.listener = listener;
        this.listenerId = listenerId;
        this.cacheName = cacheName;
        this.maxFailure = maxFailure <= 0 ? 3 : maxFailure;
        this.waitBeforeRetry = waitBeforeRetry <= 0 ? 500 : waitBeforeRetry;

        if (log.isDebugEnabled()) {
            log.debug("Constructed: " + this);
        }
    }

    /**
     * Event Q is emtpy.
     */
    public synchronized void stopProcessing() {

        destroyed = true;
        processorThread = null;

    }

    /**
     * Returns the time to wait for events before killing the background thread.
     */
    public int getWaitToDieMillis() {
        return waitToDieMillis;
    }

    /**
     * Sets the time to wait for events before killing the background thread.
     */
    public void setWaitToDieMillis(int wtdm) {
        waitToDieMillis = wtdm;
    }

    /**
     * @return
     */
    public String toString() {
        return "CacheEventQueue [listenerId=" + listenerId + ", cacheName=" + cacheName + "]";
    }

    /**
     * @return The {3} value
     */
    public boolean isAlive() {
        return (!destroyed);
    }

    public void setAlive(boolean aState) {
        destroyed = !aState;
    }

    /**
     * @return The {3} value
     */
    public long getListenerId() {
        return listenerId;
    }

    /**
     * Event Q is emtpy.
     */
    public synchronized void destroy() {
        if (!destroyed) {
            destroyed = true;
            log.info("Cache event queue destroyed: " + this);
        }
    }

    /**
     * @param ce
     *            The feature to be added to the PutEvent attribute
     * @exception IOException
     */
    public synchronized void addPutEvent(ICacheElement ce) throws IOException {
        if (isWorking()) {
            put(new PutEvent(ce));
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Not enqueuing Put Event for [" + this + "] because it's non-functional.");
            }
        }
    }

    /**
     * @param key
     *            The feature to be added to the RemoveEvent attribute
     * @exception IOException
     */
    public synchronized void addRemoveEvent(Serializable key) throws IOException {
        if (isWorking()) {
            put(new RemoveEvent(key));
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Not enqueuing Remove Event for [" + this + "] because it's non-functional.");
            }
        }
    }

    /**
     * @exception IOException
     */
    public synchronized void addRemoveAllEvent() throws IOException {
        if (isWorking()) {
            put(new RemoveAllEvent());
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Not enqueuing RemoveAll Event for [" + this + "] because it's non-functional.");
            }
        }
    }

    /**
     * @exception IOException
     */
    public synchronized void addDisposeEvent() throws IOException {
        if (isWorking()) {
            put(new DisposeEvent());
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Not enqueuing Dispose Event for [" + this + "] because it's non-functional.");
            }
        }
    }

    /**
     * Adds an event to the queue.
     * 
     * @param event
     */
    private void put(AbstractCacheEvent event) {
        try {
            pool.execute(event);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    public String getStats() {
        StringBuffer buf = new StringBuffer();
        buf.append("\n -------------------------");
        buf.append("\n Cache Event Queue:");
        buf.append("\n working = " + this.working);
        buf.append("\n isAlive() = " + this.isAlive());
        buf.append("\n isEmpty() = " + this.isEmpty());
        buf.append("\n pool size = " + pool.getPoolSize() + "/" + pool.getMaximumPoolSize());
        buf.append("\n queue size = " + queue.size() + "/" + queue.capacity());
        return buf.toString();
    }

    ///////////////////////////// Inner classes /////////////////////////////

    /**
     * Retries before declaring failure.
     * 
     * @author asmuts
     * @created January 15, 2002
     */
    private abstract class AbstractCacheEvent implements Runnable {
        int failures = 0;

        boolean done = false;

        /**
         * Main processing method for the AbstractCacheEvent object
         */
        public void run() {
            try {
                doRun();
            } catch (IOException e) {
                if (log.isWarnEnabled()) {
                    log.warn(e);
                }
                if (++failures >= maxFailure) {
                    if (log.isWarnEnabled()) {
                        log.warn("Error while running event from Queue: " + this
                                + ". Dropping Event and marking Event Queue as non-functional.");
                    }
                    setWorking(false);
                    setAlive(false);
                    return;
                }
                if (log.isInfoEnabled()) {
                    log.info("Error while running event from Queue: " + this + ". Retrying...");
                }
                try {
                    Thread.sleep(waitBeforeRetry);
                    run();
                } catch (InterruptedException ie) {
                    if (log.isErrorEnabled()) {
                        log.warn("Interrupted while sleeping for retry on event " + this + ".");
                    }
                    setWorking(false);
                    setAlive(false);
                }
            }
        }

        /**
         * @exception IOException
         */
        protected abstract void doRun() throws IOException;
    }

    /**
     * @author asmuts
     * @created January 15, 2002
     */
    private class PutEvent extends AbstractCacheEvent {

        private ICacheElement ice;

        /**
         * Constructor for the PutEvent object
         * 
         * @param ice
         * @exception IOException
         */
        PutEvent(ICacheElement ice) throws IOException {
            this.ice = ice;
            /*
             * this.key = key; this.obj = CacheUtils.dup(obj); this.attr = attr;
             * this.groupName = groupName;
             */
        }

        /**
         * Description of the Method
         * 
         * @exception IOException
         */
        protected void doRun() throws IOException {
            /*
             * CacheElement ce = new CacheElement(cacheName, key, obj);
             * ce.setElementAttributes( attr ); ce.setGroupName( groupName );
             */
            listener.handlePut(ice);
        }

        public String toString() {
            return new StringBuffer("PutEvent for key: ").append(ice.getKey()).append(" value: ").append(ice.getVal())
                    .toString();
        }

    }

    /**
     * Description of the Class
     * 
     * @author asmuts
     * @created January 15, 2002
     */
    private class RemoveEvent extends AbstractCacheEvent {
        private Serializable key;

        /**
         * Constructor for the RemoveEvent object
         * 
         * @param key
         * @exception IOException
         */
        RemoveEvent(Serializable key) throws IOException {
            this.key = key;
        }

        /**
         * Description of the Method
         * 
         * @exception IOException
         */
        protected void doRun() throws IOException {
            listener.handleRemove(cacheName, key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return new StringBuffer("RemoveEvent for ").append(key).toString();
        }

    }

    /**
     * Description of the Class
     * 
     * @author asmuts
     * @created January 15, 2002
     */
    private class RemoveAllEvent extends AbstractCacheEvent {

        /**
         * Description of the Method
         * 
         * @exception IOException
         */
        protected void doRun() throws IOException {
            listener.handleRemoveAll(cacheName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "RemoveAllEvent";
        }

    }

    /**
     * Description of the Class
     * 
     * @author asmuts
     * @created January 15, 2002
     */
    private class DisposeEvent extends AbstractCacheEvent {

        /**
         * Called when gets to the end of the queue
         * 
         * @exception IOException
         */
        protected void doRun() throws IOException {
            listener.handleDispose(cacheName);
        }

        public String toString() {
            return "DisposeEvent";
        }
    }

    /**
     * @return
     */
    public boolean isWorking() {
        return working;
    }

    /**
     * @param b
     */
    public void setWorking(boolean b) {
        working = b;
    }

    public boolean isEmpty() {
        return queue.size() == 0;
    }

}