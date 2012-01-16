package org.apache.jcs.auxiliary.disk.jdbc;

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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.disk.AbstractDiskCacheManager;

/**
 * This class serves as an abstract template for JDBCDiskCache Manager. The MySQL JDBC Disk Cache
 * needs many of the same features as the generic manager.
 * <p>
 * @author Aaron Smuts
 */
public abstract class JDBCDiskCacheManagerAbstractTemplate
    extends AbstractDiskCacheManager
{
    /** Don't change. */
    private static final long serialVersionUID = 218557927622128905L;

    /** The logger. */
    private static final Log log = LogFactory.getLog( JDBCDiskCacheManagerAbstractTemplate.class );

    /** Incremented on getIntance, decremented on release. */
    protected static int clients;

    /** A map of JDBCDiskCache objects to region names. */
    protected static Hashtable<String, AuxiliaryCache<? extends Serializable, ? extends Serializable>> caches =
        new Hashtable<String, AuxiliaryCache<? extends Serializable, ? extends Serializable>>();

    /**
     * A map of TableState objects to table names. Each cache has a table state object, which is
     * used to determine if any long processes such as deletes or optimizations are running.
     */
    protected static Hashtable<String, TableState> tableStates = new Hashtable<String, TableState>();

    /** The background disk shrinker, one for all regions. */
    private ScheduledExecutorService shrinkerDaemon;

    /**
     * A map of table name to shrinker threads. This allows each table to have a different setting.
     * It assumes that there is only one jdbc disk cache auxiliary defined per table.
     */
    private final Map<String, ShrinkerThread> shrinkerThreadMap = new Hashtable<String, ShrinkerThread>();

    /**
     * Children must implement this method.
     * <p>
     * @param cattr
     * @param tableState An object used by multiple processes to indicate state.
     * @return AuxiliaryCache -- a JDBCDiskCache
     */
    protected abstract <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V> createJDBCDiskCache( JDBCDiskCacheAttributes cattr, TableState tableState );

    /**
     * Creates a JDBCDiskCache for the region if one doesn't exist, else it returns the pre-created
     * instance. It also adds the region to the shrinker thread if needed.
     * <p>
     * @param cattr
     * @return The cache value
     */
    public <K extends Serializable, V extends Serializable> AuxiliaryCache<K, V> getCache( JDBCDiskCacheAttributes cattr )
    {
        AuxiliaryCache<K, V> diskCache = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            diskCache = (AuxiliaryCache<K, V>) caches.get( cattr.getCacheName() );

            if ( diskCache == null )
            {
                TableState tableState = tableStates.get( cattr.getTableName() );

                if ( tableState == null )
                {
                    tableState = new TableState( cattr.getTableName() );
                }

                diskCache = createJDBCDiskCache( cattr, tableState );
                diskCache.setCacheEventLogger( getCacheEventLogger() );
                diskCache.setElementSerializer( getElementSerializer() );
                caches.put( cattr.getCacheName(), diskCache );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "JDBC cache = " + diskCache );
        }

        // create a shrinker if we need it.
        createShrinkerWhenNeeded( cattr, diskCache );

        return diskCache;
    }

    /**
     * If UseDiskShrinker is true then we will create a shrinker daemon if necessary.
     * <p>
     * @param cattr
     * @param raf
     */
    protected void createShrinkerWhenNeeded( JDBCDiskCacheAttributes cattr, AuxiliaryCache<?, ?> raf )
    {
        // add cache to shrinker.
        if ( cattr.isUseDiskShrinker() )
        {
            if ( shrinkerDaemon == null )
            {
                shrinkerDaemon = Executors.newScheduledThreadPool(2, new MyThreadFactory());
            }

            ShrinkerThread shrinkerThread = shrinkerThreadMap.get( cattr.getTableName() );
            if ( shrinkerThread == null )
            {
                shrinkerThread = new ShrinkerThread();
                shrinkerThreadMap.put( cattr.getTableName(), shrinkerThread );

                long intervalMillis = Math.max( 999, cattr.getShrinkerIntervalSeconds() * 1000 );
                if ( log.isInfoEnabled() )
                {
                    log.info( "Setting the shrinker to run every [" + intervalMillis + "] ms. for table ["
                        + cattr.getTableName() + "]" );
                }
                shrinkerDaemon.scheduleAtFixedRate(shrinkerThread, 0, intervalMillis, TimeUnit.MILLISECONDS);
            }
            shrinkerThread.addDiskCacheToShrinkList( (JDBCDiskCache<?, ?>) raf );
        }
    }

    /**
     * @param name
     */
    public void freeCache( String name )
    {
        JDBCDiskCache<?, ?> raf = (JDBCDiskCache<?, ?>) caches.get( name );
        if ( raf != null )
        {
            try
            {
                raf.dispose();
            }
            catch ( IOException e )
            {
                log.error( "Problem disposing of disk.", e );
            }
        }
    }

    /**
     * Gets the cacheType attribute of the HSQLCacheManager object
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }

    /** Disposes of all regions. */
    public void release()
    {
        // Wait until called by the last client
        if ( --clients != 0 )
        {
            return;
        }
        synchronized ( caches )
        {
            Enumeration<AuxiliaryCache<?, ?>> allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                JDBCDiskCache<?, ?> raf = (JDBCDiskCache<?, ?>) allCaches.nextElement();
                if ( raf != null )
                {
                    try
                    {
                        raf.dispose();
                    }
                    catch ( IOException e )
                    {
                        log.error( "Problem disposing of disk.", e );
                    }
                }
            }
        }
    }

    /**
     * Allows us to set the daemon status on the clock-daemon
     */
    protected static class MyThreadFactory
        implements ThreadFactory
    {
        /**
         * Set the priority to min and daemon to true.
         * <p>
         * @param runner
         * @return the daemon thread.
         */
        public Thread newThread( Runnable runner )
        {
            Thread t = new Thread( runner );
            String oldName = t.getName();
            t.setName( "JCS-JDBCDiskCacheManager-" + oldName );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }
}
