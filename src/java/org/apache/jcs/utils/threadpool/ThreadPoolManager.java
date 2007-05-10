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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.utils.props.PropertyLoader;
import org.apache.jcs.utils.threadpool.behavior.IPoolConfiguration;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This manages threadpools for an application using Doug Lea's Util Concurrent
 * package.
 * http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html
 * <p>
 * It is a singleton since threads need to be managed vm wide.
 * <p>
 * This manager forces you to use a bounded queue. By default it uses the
 * current thread for execuion when the buffer is full and no free threads can
 * be created.
 * <p>
 * You can specify the props file to use or pass in a properties object prior to
 * configuration. By default it looks for configuration information in
 * thread_pool.properties.
 * <p>
 * If set, the Properties object will take precedence.
 * <p>
 * If a value is not set for a particular pool, the hard coded defaults will be
 * used.
 *
 * <pre>
 * int boundarySize_DEFAULT = 2000;
 *
 * int maximumPoolSize_DEFAULT = 150;
 *
 * int minimumPoolSize_DEFAULT = 4;
 *
 * int keepAliveTime_DEFAULT = 1000 * 60 * 5;
 *
 * boolean abortWhenBlocked = false;
 *
 * String whenBlockedPolicy_DEFAULT = IPoolConfiguration.POLICY_RUN;
 *
 * int startUpSize_DEFAULT = 4;
 * </pre>
 *
 * You can configure default settings by specifying a default pool in the
 * properties, ie "cache.ccf"
 * <p>
 * @author Aaron Smuts
 */
public class ThreadPoolManager
{
    private static final Log log = LogFactory.getLog( ThreadPoolManager.class );

    // DEFAULT SETTINGS, these are not final since they can be set
    // via the Propeties file or object
    private static boolean useBoundary_DEFAULT = true;

    private static int boundarySize_DEFAULT = 2000;

    private static int maximumPoolSize_DEFAULT = 150;

    private static int minimumPoolSize_DEFAULT = 4;

    private static int keepAliveTime_DEFAULT = 1000 * 60 * 5;

    private static String whenBlockedPolicy_DEFAULT = IPoolConfiguration.POLICY_RUN;

    private static int startUpSize_DEFAULT = 4;

    private static PoolConfiguration defaultConfig;

    // This is the default value. Setting this after
    // inialization will have no effect
    private static String propsFileName = "cache.ccf";

    // the root property name
    private static String PROP_NAME_ROOT = "thread_pool";

    private static String DEFAULT_PROP_NAME_ROOT = "thread_pool.default";

    // You can specify the properties to be used to configure
    // the thread pool. Setting this post initialization will have
    // no effect.
    private static Properties props = null;

    private static HashMap pools = new HashMap();

    // singleton instance
    private static ThreadPoolManager INSTANCE = null;

    /**
     * No instances please. This is a singleton.
     */
    private ThreadPoolManager()
    {
        configure();
    }

    /**
     * Creates a pool based on the configuration info.
     * <p>
     * @param config
     * @return A ThreadPoll wrapper
     */
    private ThreadPool createPool( PoolConfiguration config )
    {
        PooledExecutor pool = null;
        Channel queue = null;
        if ( config.isUseBoundary() )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Creating a Bounded Buffer to use for the pool" );
            }
            queue = new BoundedBuffer( config.getBoundarySize() );
            pool = new PooledExecutor( queue, config.getMaximumPoolSize() );
            pool.setThreadFactory( new MyThreadFactory() );
        }
        else
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Creating a non bounded Linked Queue to use for the pool" );
            }
            queue = new LinkedQueue();
            pool = new PooledExecutor( queue, config.getMaximumPoolSize() );
        }

        pool.setMinimumPoolSize( config.getMinimumPoolSize() );
        pool.setKeepAliveTime( config.getKeepAliveTime() );

        // when blocked policy
        if ( config.getWhenBlockedPolicy().equals( IPoolConfiguration.POLICY_ABORT ) )
        {
            pool.abortWhenBlocked();
        }
        else if ( config.getWhenBlockedPolicy().equals( IPoolConfiguration.POLICY_RUN ) )
        {
            pool.runWhenBlocked();
        }
        else if ( config.getWhenBlockedPolicy().equals( IPoolConfiguration.POLICY_WAIT ) )
        {
            pool.waitWhenBlocked();
        }
        else if ( config.getWhenBlockedPolicy().equals( IPoolConfiguration.POLICY_ABORT ) )
        {
            pool.abortWhenBlocked();
        }
        else if ( config.getWhenBlockedPolicy().equals( IPoolConfiguration.POLICY_DISCARDOLDEST ) )
        {
            pool.discardOldestWhenBlocked();
        }

        pool.createThreads( config.getStartUpSize() );

        return new ThreadPool( pool, queue );
    }

    /**
     * Returns a configured instance of the ThreadPoolManger To specify a
     * configuation file or Properties object to use call the appropriate setter
     * prior to calling getInstance.
     * <p>
     * @return The single instance of the ThreadPoolManager
     */
    public static synchronized ThreadPoolManager getInstance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new ThreadPoolManager();
        }
        return INSTANCE;
    }

    /**
     * Returns a pool by name. If a pool by this name does not exist in the
     * configuration file or properties, one will be created using the default
     * values.
     * <p>
     * Pools are lazily created.
     * <p>
     * @param name
     * @return The thread pool configured for the name.
     */
    public ThreadPool getPool( String name )
    {
        ThreadPool pool = null;

        synchronized ( pools )
        {
            pool = (ThreadPool) pools.get( name );
            if ( pool == null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Creating pool for name [" + name + "]" );
                }
                PoolConfiguration config = this.loadConfig( PROP_NAME_ROOT + "." + name );
                pool = createPool( config );

                if ( pool != null )
                {
                    pools.put( name, pool );
                }

                if ( log.isDebugEnabled() )
                {
                    log.debug( "PoolName = " + getPoolNames() );
                }
            }
        }

        return pool;
    }

    /**
     * Returns the names of all configured pools.
     * <p>
     * @return ArrayList of string names
     */
    public ArrayList getPoolNames()
    {
        ArrayList poolNames = new ArrayList();
        synchronized ( pools )
        {
            Set names = pools.keySet();
            Iterator it = names.iterator();
            while ( it.hasNext() )
            {
                poolNames.add( it.next() );
            }
        }
        return poolNames;
    }

    /**
     * Setting this post initialization will have no effect.
     * <p>
     * @param propsFileName
     *            The propsFileName to set.
     */
    public static void setPropsFileName( String propsFileName )
    {
        ThreadPoolManager.propsFileName = propsFileName;
    }

    /**
     * Returns the name of the properties file that we used to initialize the
     * pools. If the value was set post-initialization, then it may not be the
     * file used.
     * <p>
     * @return Returns the propsFileName.
     */
    public static String getPropsFileName()
    {
        return propsFileName;
    }

    /**
     * This will be used if it is not null on initialzation. Setting this post
     * initialization will have no effect.
     * <p>
     * @param props
     *            The props to set.
     */
    public static void setProps( Properties props )
    {
        ThreadPoolManager.props = props;
    }

    /**
     * @return Returns the props.
     */
    public static Properties getProps()
    {
        return props;
    }

    /**
     * Intialize the ThreadPoolManager and create all the pools defined in the
     * configuration.
     */
    protected void configure()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Initializing ThreadPoolManager" );
        }

        if ( props == null )
        {
            try
            {
                props = PropertyLoader.loadProperties( propsFileName );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "File contained " + props.size() + " properties" );
                }
            }
            catch ( Exception e )
            {
                log.error( "Problem loading properties. propsFileName [" + propsFileName + "]", e );
            }
        }

        if ( props == null )
        {
            log.warn( "No configuration settings found.  Using hardcoded default values for all pools." );
            props = new Properties();
        }

        // set intial default and then override if new
        // settings are available
        defaultConfig = new PoolConfiguration( useBoundary_DEFAULT, boundarySize_DEFAULT, maximumPoolSize_DEFAULT,
                                               minimumPoolSize_DEFAULT, keepAliveTime_DEFAULT,
                                               whenBlockedPolicy_DEFAULT, startUpSize_DEFAULT );

        defaultConfig = loadConfig( DEFAULT_PROP_NAME_ROOT );
    }

    /**
     * Configures the default PoolConfiguration settings.
     * <p>
     * @param root
     * @return PoolConfiguration
     */
    protected PoolConfiguration loadConfig( String root )
    {
        PoolConfiguration config = (PoolConfiguration) defaultConfig.clone();

        if ( props.containsKey( root + ".useBoundary" ) )
        {
            try
            {
                config.setUseBoundary( Boolean.valueOf( (String) props.get( root + ".useBoundary" ) ).booleanValue() );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "useBoundary not a boolean.", nfe );
            }
        }

        // load default if they exist
        if ( props.containsKey( root + ".boundarySize" ) )
        {
            try
            {
                config.setBoundarySize( Integer.parseInt( (String) props.get( root + ".boundarySize" ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "boundarySize not a number.", nfe );
            }
        }

        // maximum pool size
        if ( props.containsKey( root + ".maximumPoolSize" ) )
        {
            try
            {
                config.setMaximumPoolSize( Integer.parseInt( (String) props.get( root + ".maximumPoolSize" ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "maximumPoolSize not a number.", nfe );
            }
        }

        // minimum pool size
        if ( props.containsKey( root + ".minimumPoolSize" ) )
        {
            try
            {
                config.setMinimumPoolSize( Integer.parseInt( (String) props.get( root + ".minimumPoolSize" ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "minimumPoolSize not a number.", nfe );
            }
        }

        // keep alive
        if ( props.containsKey( root + ".keepAliveTime" ) )
        {
            try
            {
                config.setKeepAliveTime( Integer.parseInt( (String) props.get( root + ".keepAliveTime" ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "keepAliveTime not a number.", nfe );
            }
        }

        // when blocked
        if ( props.containsKey( root + ".whenBlockedPolicy" ) )
        {
            config.setWhenBlockedPolicy( (String) props.get( root + ".whenBlockedPolicy" ) );
        }

        // startupsize
        if ( props.containsKey( root + ".startUpSize" ) )
        {
            try
            {
                config.setStartUpSize( Integer.parseInt( (String) props.get( root + ".startUpSize" ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                log.error( "startUpSize not a number.", nfe );
            }
        }

        if ( log.isInfoEnabled() )
        {
            log.info( root + " PoolConfiguration = " + config );
        }

        return config;
    }

    /**
     * Allows us to set the daemon status on the threads.
     * <p>
     * @author aaronsm
     */
    class MyThreadFactory
        implements ThreadFactory
    {
        /*
         * (non-Javadoc)
         *
         * @see EDU.oswego.cs.dl.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            t.setDaemon( true );
            return t;
        }
    }
}
