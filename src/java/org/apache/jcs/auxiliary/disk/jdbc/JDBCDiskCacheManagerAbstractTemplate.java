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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * This class serves as an abstract template for JDBCDiskCache Manager. The MySQL JDBC Disk Cache
 * needs many of the same features as the generic maanger.
 * <p>
 * @author Aaron Smuts
 */
public abstract class JDBCDiskCacheManagerAbstractTemplate
    implements AuxiliaryCacheManager
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( JDBCDiskCacheManagerAbstractTemplate.class );

    /**
     * Incremented on getIntance, decremented on release.
     */
    protected static int clients;

    /**
     * A map of JDBCDiskCache objects to region names.
     */
    protected static Hashtable caches = new Hashtable();

    /**
     * A map of TableState objects to table names. Each cache has a table state object, which is
     * used to determin if any long processes such as deletes or optimizations are running.
     */
    protected static Hashtable tableStates = new Hashtable();

    /**
     * The background disk shrinker, one for all regions.
     */
    private ClockDaemon shrinkerDaemon;

    /**
     * A map of table name to shrinker threads. This allows each table to have a different setting.
     * It assumes that there is only one jdbc disk cache auxiliary defined per table.
     */
    private Map shrinkerThreadMap = new Hashtable();

    /**
     * Children must implement this method.
     * <p>
     * @param cattr
     * @param tableState An object used by multiple processes to indicate state.
     * @return AuxiliaryCache -- a JDBCDiskCache
     */
    protected abstract AuxiliaryCache createJDBCDiskCache( JDBCDiskCacheAttributes cattr, TableState tableState );

    /**
     * Creates a JDBCDiskCache for the region if one doesn't exist, else it returns the precreated
     * instance. It also adds the region to the shrinker thread if needed.
     * <p>
     * @param cattr
     * @return The cache value
     */
    public AuxiliaryCache getCache( JDBCDiskCacheAttributes cattr )
    {
        AuxiliaryCache diskCache = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            diskCache = (AuxiliaryCache) caches.get( cattr.getCacheName() );

            if ( diskCache == null )
            {
                TableState tableState = (TableState) tableStates.get( cattr.getTableName() );

                if ( tableState == null )
                {
                    tableState = new TableState( cattr.getTableName() );
                }

                diskCache = createJDBCDiskCache( cattr, tableState );

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
    protected void createShrinkerWhenNeeded( JDBCDiskCacheAttributes cattr, AuxiliaryCache raf )
    {
        // add cache to shrinker.
        if ( cattr.isUseDiskShrinker() )
        {
            if ( shrinkerDaemon == null )
            {
                shrinkerDaemon = new ClockDaemon();
                shrinkerDaemon.setThreadFactory( new MyThreadFactory() );
            }

            ShrinkerThread shrinkerThread = (ShrinkerThread) shrinkerThreadMap.get( cattr.getTableName() );
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
                shrinkerDaemon.executePeriodically( intervalMillis, shrinkerThread, false );
            }
            shrinkerThread.addDiskCacheToShrinkList( (JDBCDiskCache) raf );
        }
    }

    /**
     * @param name
     */
    public void freeCache( String name )
    {
        JDBCDiskCache raf = (JDBCDiskCache) caches.get( name );
        if ( raf != null )
        {
            raf.dispose();
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
            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                JDBCDiskCache raf = (JDBCDiskCache) allCaches.nextElement();
                if ( raf != null )
                {
                    raf.dispose();
                }
            }
        }
    }

    /**
     * Allows us to set the daemon status on the clockdaemon
     * <p>
     * @author aaronsm
     */
    class MyThreadFactory
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
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }
}
