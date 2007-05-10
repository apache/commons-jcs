package org.apache.jcs.utils.threadpool;

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

import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * This is simply a wrapper around the Pooled Excutor that allows clients to
 * access the queue.
 * <p>
 * @author aaronsm
 */
public class ThreadPool
{
    private PooledExecutor pool = null;

    private Channel queue = null;

    /**
     * Create the wrapper.
     * <p>
     * @param pool
     * @param queue
     */
    public ThreadPool( PooledExecutor pool, Channel queue )
    {
        this.pool = pool;
        this.queue = queue;
    }

    /**
     * This is intended to give the client access to the PooledExecutor itself.
     * <p>
     * @return Returns the pool.
     */
    public PooledExecutor getPool()
    {
        return pool;
    }

    /**
     * @return Returns the queue.
     */
    public Channel getQueue()
    {
        return queue;
    }

    /**
     * Delegates execution to the pooled executor.
     * <p>
     * @param run
     * @throws InterruptedException
     */
    public void execute( Runnable run )
        throws InterruptedException
    {
        pool.execute( run );
    }
}
