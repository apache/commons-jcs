package org.apache.commons.jcs3.engine.control.event;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs3.engine.control.event.behavior.IElementEvent;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventHandler;
import org.apache.commons.jcs3.engine.control.event.behavior.IElementEventQueue;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration;
import org.apache.commons.jcs3.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;
import org.apache.commons.jcs3.utils.threadpool.ThreadPoolManager;

/**
 * An event queue is used to propagate ordered cache events to one and only one target listener.
 */
public class ElementEventQueue
    implements IElementEventQueue
{
    private static final String THREAD_PREFIX = "JCS-ElementEventQueue-";

    /** The logger */
    private static final Log log = LogManager.getLog( ElementEventQueue.class );

    /** shutdown or not */
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    /** The worker thread pool. */
    private final ExecutorService queueProcessor;

    /**
     * Constructor for the ElementEventQueue object
     */
    public ElementEventQueue()
    {
        queueProcessor = ThreadPoolManager.getInstance().createPool(
        		new PoolConfiguration(false, 0, 1, 1, 0, WhenBlockedPolicy.RUN, 1), THREAD_PREFIX);

        log.debug( "Constructed: {0}", this );
    }

    /**
     * Dispose queue
     */
    @Override
    public void dispose()
    {
        if (destroyed.compareAndSet(false, true))
        {
            // Pool will be shut down by the ThreadPoolManager
            // queueProcessor.shutdownNow();
            log.info( "Element event queue destroyed: {0}", this );
        }
    }

    /**
     * Adds an ElementEvent to be handled
     * @param hand The IElementEventHandler
     * @param event The IElementEventHandler IElementEvent event
     * @throws IOException
     */
    @Override
    public <T> void addElementEvent( final IElementEventHandler hand, final IElementEvent<T> event )
        throws IOException
    {

        log.debug("Adding Event Handler to QUEUE, !destroyed = {0}", !destroyed.get());

        if (destroyed.get())
        {
            log.warn("Event submitted to disposed element event queue {0}", event);
        }
        else
        {
            queueProcessor.execute(() -> hand.handleElementEvent(event));
        }
    }

    // /////////////////////////// Inner classes /////////////////////////////

    /**
     * Retries before declaring failure.
     * @deprecated No longer used
     */
    @Deprecated
    protected abstract class AbstractElementEventRunner
        implements Runnable
    {
        /**
         * Main processing method for the AbstractElementEvent object
         */
        @Override
        public void run()
        {
            try
            {
                doRun();
                // happy and done.
            }
            catch ( final IOException e )
            {
                // Too bad. The handler has problems.
                log.warn( "Giving up element event handling {0}", ElementEventQueue.this, e );
            }
        }

        /**
         * This will do the work or trigger the work to be done.
         * <p>
         * @throws IOException
         */
        protected abstract void doRun()
            throws IOException;
    }
}
