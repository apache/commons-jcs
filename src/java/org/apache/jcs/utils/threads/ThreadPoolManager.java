package org.apache.jcs.utils.threads;

/**
 * Provides a singleton thread pool.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class ThreadPoolManager
{
    /** The singleton thread pool manager. */
    private final static ThreadPoolManager singleton = new ThreadPoolManager();

    /** The thread pool. */
    private ThreadPool pool = new ThreadPool();
    /** True iff the thread pool has been started. */
    private boolean started;


    /** Can only be constructed by this class. */
    private ThreadPoolManager() { }


    /**
     * Sets the max number of threads that you can open in the pool. Will only
     * be effective if called before the getInstance method is invoked for the
     * first time.
     *
     * @param maxThreads The new maxThreads value
     */
    public static void setMaxThreads( int maxThreads )
    {
        singleton.pool.setMaxThreads( maxThreads );
    }


    /**
     * Sets the min number of idle threads that you can leave in the pool. Will
     * only be effective if called before the getInstance method is invoked for
     * the first time.
     *
     * @param minSpareThreads The new minSpareThreads value
     */
    public static void setMinSpareThreads( int minSpareThreads )
    {
        singleton.pool.setMinSpareThreads( minSpareThreads );
    }


    /**
     * Sets the max number of idle threads that you can leave in the pool. Will
     * only be effective if called before the getInstance method is invoked for
     * the first time.
     *
     * @param maxSpareThreads The new maxSpareThreads value
     */
    public static void setMaxSpareThreads( int maxSpareThreads )
    {
        singleton.pool.setMaxSpareThreads( maxSpareThreads );
    }


    /**
     * Gets the max number of threads that you can open in the pool. Will only
     * be accurate if called after the getInstance method is invoked for the
     * first time.
     *
     * @return The maxThreads value
     */
    public static int getMaxThreads()
    {
        return singleton.pool.getMaxThreads();
    }


    /**
     * Gets the min number of idle threads that you can leave in the pool. Will
     * only be accurate if called after the getInstance method is invoked for
     * the first time.
     *
     * @return The minSpareThreads value
     */
    public static int getMinSpareThreads()
    {
        return singleton.pool.getMinSpareThreads();
    }


    /**
     * Gets the max number of idle threads that you can leave in the pool. Will
     * only be accurate if called after the getInstance method is invoked for
     * the first time.
     *
     * @return The maxSpareThreads value
     */
    public static int getMaxSpareThreads()
    {
        return singleton.pool.getMaxSpareThreads();
    }


    /**
     * Returns the singleton thread pool manager, which can be used to execute a
     * given IThreadPoolRunnable on a thread in the pool. Configuration of the
     * thread pool must be made prior to invoking this method.
     *
     * @return The instance value
     */
    public static ThreadPoolManager getInstance()
    {
        if ( !singleton.started )
        {
            synchronized ( singleton )
            {
                if ( !singleton.started )
                {
                    singleton.pool.start();
                    singleton.started = true;
                }
            }
        }
        return singleton;
    }


    /**
     * Shuts down the thread pool and re-initializes it to the default.
     */
    public static void reset()
    {
        synchronized ( singleton )
        {
            if ( singleton.started )
            {
                singleton.started = false;
                singleton.pool.shutdown();
            }
            singleton.pool = new ThreadPool();
        }
        return;
    }


    /**
     * Executes a given IThreadPoolRunnable on a thread in the pool, block if
     * needed.
     */
    public void runIt( IThreadPoolRunnable r )
    {
        pool.runIt( r );
    }
}
