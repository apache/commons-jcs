package org.apache.jcs.utils.threads;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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
