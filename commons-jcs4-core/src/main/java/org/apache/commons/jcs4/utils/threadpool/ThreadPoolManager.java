package org.apache.commons.jcs4.utils.threadpool;

import java.time.Duration;

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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jcs4.log.Log;
import org.apache.commons.jcs4.utils.config.ConfigurationBuilder;
import org.apache.commons.jcs4.utils.threadpool.PoolConfiguration.WhenBlockedPolicy;
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

/**
 * This manages threadpools for an application
 * <p>
 * It is a singleton since threads need to be managed JVM wide.
 * </p>
 * <p>
 * This manager forces you to use a bounded queue. By default it uses the current thread for
 * execution when the buffer is full and no free threads can be created.
 * </p>
 * <p>
 * You can specify the props file to use or pass in a properties object prior to configuration.
 * </p>
 * <p>
 * If set, the Properties object will take precedence.
 * </p>
 * <p>
 * If a value is not set for a particular pool, the hard coded defaults in {@code PoolConfiguration} will be used.
 * You can configure default settings by specifying {@code thread_pool.default} in the properties, ie "cache.ccf"
 * </p>
 */
public class ThreadPoolManager
{
    /**
     * The ThreadPoolManager instance (holder pattern)
     */
    private static final class ThreadPoolManagerHolder
    {
        static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    }

    /** The logger */
    private static final Log log = Log.getLog( ThreadPoolManager.class );

    /** The common prefix for all thread names managed by the ThreadPoolManager */
    public static final String JCS_THREAD_POOL_MANAGER_PREFIX = "JCS-ThreadPoolManager-";

    /** The root property name */
    private static final String PROP_NAME_ROOT = "thread_pool";

    /** Default property file name */
    private static final String DEFAULT_PROP_NAME_ROOT = "thread_pool.default";

    /** The scheduler root property name */
    private static final String PROP_NAME_SCHEDULER_ROOT = "scheduler_pool";

    /** Default scheduler property file name */
    private static final String DEFAULT_PROP_NAME_SCHEDULER_ROOT = "scheduler_pool.default";

    /**
     * You can specify the properties to be used to configure the thread pool. Setting this post
     * initialization will have no effect.
     */
    private static volatile Properties props;

    /**
     * Returns a configured instance of the ThreadPoolManger To specify a configuration file or
     * Properties object to use call the appropriate setter prior to calling getInstance.
     *
     * @return The single instance of the ThreadPoolManager
     */
    public static ThreadPoolManager getInstance()
    {
        return ThreadPoolManagerHolder.INSTANCE;
    }

    /**
     * Configures the PoolConfiguration settings.
     *
     * @param root The configuration key prefix
     * @param defaultPoolConfiguration The default configuration
     * @return PoolConfiguration
     */
    private static PoolConfiguration loadConfig(final String root, final PoolConfiguration defaultPoolConfiguration)
    {
        final PoolConfiguration config = ConfigurationBuilder
                .create(PoolConfiguration.class, defaultPoolConfiguration)
                .fromProperties(props, root)
                .build();

        log.debug("{0} PoolConfiguration = {1}", root, config);

        return config;
    }

    /**
     * This will be used if it is not null on initialization. Setting this post initialization will
     * have no effect.
     *
     * @param props The props to set.
     */
    public static void setProps(final Properties props)
    {
        ThreadPoolManager.props = props;
    }

    /** The default config, created using property defaults if present, else those above. */
    private PoolConfiguration defaultConfig;

    /** The default scheduler config, created using property defaults if present, else those above. */
    private PoolConfiguration defaultSchedulerConfig;

    /** Map of names to pools. */
    private final ConcurrentHashMap<String, ExecutorService> pools;

    /** Map of names to scheduler pools. */
    private final ConcurrentHashMap<String, ScheduledExecutorService> schedulerPools;

    /** Map of names to pool use counts. */
    private final ConcurrentHashMap<String, AtomicInteger> poolUseCounts;

    /** Map of names to scheduler pool use counts. */
    private final ConcurrentHashMap<String, AtomicInteger> schedulerPoolUseCounts;

    /**
     * No instances please. This is a singleton.
     */
    private ThreadPoolManager()
    {
        this.pools = new ConcurrentHashMap<>();
        this.schedulerPools = new ConcurrentHashMap<>();
        this.poolUseCounts = new ConcurrentHashMap<>();
        this.schedulerPoolUseCounts = new ConcurrentHashMap<>();
        configure();
    }

    /**
     * Initialize the ThreadPoolManager and create all the pools defined in the configuration.
     */
    private void configure()
    {
        log.debug( "Initializing ThreadPoolManager" );

        if ( props == null )
        {
            log.warn( "No configuration settings found. Using hardcoded default values for all pools." );
            props = new Properties();
        }

        // set initial default and then override if new settings are available
        defaultConfig = loadConfig(DEFAULT_PROP_NAME_ROOT, PoolConfiguration.defaults());
        defaultSchedulerConfig = loadConfig(DEFAULT_PROP_NAME_SCHEDULER_ROOT,
                new PoolConfiguration(false, 0, 4, 4, Duration.ZERO, WhenBlockedPolicy.DISCARDOLDEST, 4, Thread.MIN_PRIORITY));
    }

    /**
     * Creates a pool based on the configuration info.
     *
     * @param config The pool configuration
     * @param threadNamePrefix prefix for the thread names of the pool
     * @return A ThreadPool wrapper
     */
    private ExecutorService createPool(final PoolConfiguration config, final String threadNamePrefix)
    {
        BlockingQueue<Runnable> queue = null;
        if ( config.useBoundary() )
        {
            log.debug( "Creating a Bounded Buffer to use for the pool" );
            queue = new LinkedBlockingQueue<>(config.boundarySize());
        }
        else
        {
            log.debug( "Creating a non bounded Linked Queue to use for the pool" );
            queue = new LinkedBlockingQueue<>();
        }

        final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            config.startUpSize(),
            config.maximumPoolSize(),
            config.keepAliveTime().toMillis(),
            TimeUnit.MILLISECONDS,
            queue,
            new DaemonThreadFactory(threadNamePrefix, config.threadPriority()));

        // when blocked policy
        switch (config.whenBlockedPolicy())
        {
            case ABORT:
                pool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
                break;

            case RUN:
                pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                break;

            case DISCARDOLDEST:
                pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                break;

            case DISCARD:
                pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                break;
        }

        pool.prestartAllCoreThreads();

        return pool;
    }

    /**
     * Creates a scheduler pool based on the configuration info.
     *
     * @param config The pool configuration
     * @param threadNamePrefix prefix for the thread names of the pool
     * @return A ScheduledExecutorService
     */
    private ScheduledExecutorService createSchedulerPool(final PoolConfiguration config, final String threadNamePrefix)
    {
        return Executors.newScheduledThreadPool(
                config.maximumPoolSize(),
                new DaemonThreadFactory(threadNamePrefix, config.threadPriority()));
    }

    /**
     * Returns an executor service by name. If a service by this name does not exist in the configuration file or
     * properties, one will be created using the default values.
     * <p>
     * Services are lazily created.
     *
     * @param name
     * @return The executor service configured for the name.
     */
    public ExecutorService getExecutorService(final String name)
    {
    	return getExecutorService(name, loadConfig(PROP_NAME_ROOT + "." + name, defaultConfig));
    }

    /**
     * Returns an executor service by name. If a service by this name does not exist in the configuration file or
     * properties, one will be created using the default values.
     * <p>
     * Services are lazily created.
     *
     * @param name
     * @param config The pool configuration
     * @return The executor service configured for the name.
     */
    public ExecutorService getExecutorService(final String name, final PoolConfiguration config)
    {
        synchronized (pools)
        {
            ExecutorService pool = pools.computeIfAbsent(name, key -> {
                log.debug("Creating pool for name [{0}]", key);
                return createPool(config, JCS_THREAD_POOL_MANAGER_PREFIX + key + "-");
            });

            AtomicInteger useCount = poolUseCounts.computeIfAbsent(name, k -> new AtomicInteger());
            useCount.getAndIncrement();

            return pool;
        }
    }

    /**
     * Returns the names of all configured pools.
     *
     * @return ArrayList of string names
     */
    protected Set<String> getPoolNames()
    {
        return pools.keySet();
    }

    /**
     * Returns a scheduler pool by name. If a pool by this name does not exist in the configuration file or
     * properties, one will be created using the default values.
     * <p>
     * Pools are lazily created.
     *
     * @param name
     * @return The scheduler pool configured for the name.
     */
    public ScheduledExecutorService getSchedulerPool(final String name)
    {
        return getSchedulerPool(name, loadConfig(PROP_NAME_SCHEDULER_ROOT + "." + name, defaultSchedulerConfig));
    }

    /**
     * Returns a scheduler pool by name. If a pool by this name does not exist in the configuration file or
     * properties, one will be created using the named configuration.
     * <p>
     * Pools are lazily created.
     *
     * @param name
     * @param config The pool configuration
     * @return The scheduler pool configured for the name.
     */
    public ScheduledExecutorService getSchedulerPool(final String name, PoolConfiguration config)
    {
        synchronized (schedulerPools)
        {
            ScheduledExecutorService pool = schedulerPools.computeIfAbsent(name, key -> {
                log.debug( "Creating scheduler pool for name [{0}]", key );
                return createSchedulerPool(config, JCS_THREAD_POOL_MANAGER_PREFIX + key + "-");
        	});

            AtomicInteger useCount = schedulerPoolUseCounts.computeIfAbsent(name, k -> new AtomicInteger());
            useCount.getAndIncrement();

            return pool;
        }
    }

    /**
     * Dispose of the instance of the ThreadPoolManger and shut down all thread pools
     */
    public void dispose()
    {
        synchronized (pools)
        {
            for (final Iterator<Map.Entry<String, ExecutorService>> i =
                    pools.entrySet().iterator(); i.hasNext();)
            {
                final Map.Entry<String, ExecutorService> entry = i.next();
                try
                {
                    entry.getValue().shutdownNow();
                }
                catch (final Throwable t)
                {
                    log.warn("Failed to close pool {0}", entry.getKey(), t);
                }
                i.remove();
                poolUseCounts.remove(entry.getKey());
            }
        }

        synchronized (schedulerPools)
        {
            for (final Iterator<Map.Entry<String, ScheduledExecutorService>> i =
                    schedulerPools.entrySet().iterator(); i.hasNext();)
            {
                final Map.Entry<String, ScheduledExecutorService> entry = i.next();
                try
                {
                    entry.getValue().shutdownNow();
                }
                catch (final Throwable t)
                {
                    log.warn("Failed to close pool {0}", entry.getKey(), t);
                }
                i.remove();
                schedulerPoolUseCounts.remove(entry.getKey());
            }
        }
    }

    /**
     * Dispose of a thread pool
     *
     * @param poolName the name of the pool
     */
    public void disposeExecutorService(String poolName)
    {
        disposeExecutorService(poolName, Duration.ZERO);
    }

    /**
     * Dispose of a thread pool
     *
     * @param poolName the name of the pool
     * @param wait Duration to wait for termination
     */
    public void disposeExecutorService(String poolName, Duration wait)
    {
        ExecutorService pool = null;

        synchronized (pools)
        {
            AtomicInteger useCount = poolUseCounts.get(poolName);
            if (useCount == null)
            {
                log.warn("No useCount exists for pool {0}", poolName);
            }
            else if (useCount.decrementAndGet() <= 0)
            {
                poolUseCounts.remove(poolName, useCount);
                pool = pools.remove(poolName);
                if (pool == null)
                {
                    log.warn("Failed to close non-existing pool {0}", poolName);
                }
                else
                {
                    try
                    {
                        if (wait == null || wait.isZero())
                        {
                            pool.shutdownNow();
                        }
                        else
                        {
                            pool.shutdown();
                        }
                    }
                    catch (final Throwable t)
                    {
                        log.warn("Failed to close pool {0}", poolName, t);
                    }
                }
            }
        }

        if (pool != null && wait != null && !wait.isZero())
        {
            try
            {
                if (!pool.awaitTermination(wait.toMillis(), TimeUnit.MILLISECONDS))
                {
                    log.info( "No longer waiting for pool {0} to terminate", poolName);
                }
            }
            catch (final InterruptedException e)
            {
                // ignore
            }
        }
    }

    /**
     * Dispose of a scheduler thread pool
     *
     * @param poolName the name of the pool
     */
    public void disposeSchedulerPool(String poolName)
    {
        disposeSchedulerPool(poolName, Duration.ZERO);
    }

    /**
     * Dispose of a scheduler thread pool
     *
     * @param poolName the name of the pool
     * @param wait Duration to wait for termination
     */
    public void disposeSchedulerPool(String poolName, Duration wait)
    {
        ExecutorService pool = null;

        synchronized (schedulerPools)
        {
            AtomicInteger useCount = schedulerPoolUseCounts.get(poolName);
            if (useCount == null)
            {
                log.warn("No useCount exists for pool {0}", poolName);
            }
            else if (useCount.decrementAndGet() == 0)
            {
                schedulerPoolUseCounts.remove(poolName, useCount);
                pool = schedulerPools.remove(poolName);
                if (pool == null)
                {
                    log.warn("Failed to close non-existing pool {0}", poolName);
                }
                else
                {
                    try
                    {
                        if (wait == null || wait.isZero())
                        {
                            pool.shutdownNow();
                        }
                        else
                        {
                            pool.shutdown();
                        }
                    }
                    catch (final Throwable t)
                    {
                        log.warn("Failed to close pool {0}", poolName, t);
                    }
                }
            }
        }

        if (pool != null && wait != null && !wait.isZero())
        {
            try
            {
                if (!pool.awaitTermination(wait.toMillis(), TimeUnit.MILLISECONDS))
                {
                    log.info( "No longer waiting for pool {0} to terminate", poolName);
                }
            }
            catch (final InterruptedException e)
            {
                // ignore
            }
        }
    }
}
