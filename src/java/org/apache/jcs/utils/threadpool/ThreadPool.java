package org.apache.jcs.utils.threadpool;

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
