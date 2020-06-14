package org.apache.commons.jcs3.auxiliary.disk.jdbc.mysql;

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

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.JDBCDiskCacheFactory;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.TableState;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.dsfactory.DataSourceFactory;
import org.apache.commons.jcs3.auxiliary.disk.jdbc.mysql.util.ScheduleParser;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * This factory should create mysql disk caches.
 * <p>
 * @author Aaron Smuts
 */
public class MySQLDiskCacheFactory
    extends JDBCDiskCacheFactory
{
    /** The logger */
    private static final Log log = LogManager.getLog( MySQLDiskCacheFactory.class );

    /**
     * This factory method should create an instance of the mysqlcache.
     * <p>
     * @param rawAttr specific cache configuration attributes
     * @param compositeCacheManager the global cache manager
     * @param cacheEventLogger a specific logger for cache events
     * @param elementSerializer a serializer for cache elements
     * @return MySQLDiskCache the cache instance
     * @throws SQLException if the cache instance could not be created
     */
    @Override
    public <K, V> MySQLDiskCache<K, V> createCache( AuxiliaryCacheAttributes rawAttr,
            ICompositeCacheManager compositeCacheManager,
            ICacheEventLogger cacheEventLogger, IElementSerializer elementSerializer )
            throws SQLException
    {
        MySQLDiskCacheAttributes cattr = (MySQLDiskCacheAttributes) rawAttr;
        TableState tableState = getTableState( cattr.getTableName() );
        DataSourceFactory dsFactory = getDataSourceFactory(cattr, compositeCacheManager.getConfigurationProperties());

        MySQLDiskCache<K, V> cache = new MySQLDiskCache<>( cattr, dsFactory, tableState);
        cache.setCacheEventLogger( cacheEventLogger );
        cache.setElementSerializer( elementSerializer );

        // create a shrinker if we need it.
        createShrinkerWhenNeeded( cattr, cache );
        scheduleOptimizations( cattr, tableState, cache.getDataSource() );

        return cache;

    }

    /**
     * For each time in the optimization schedule, this calls schedule Optimization.
     * <p>
     * @param attributes configuration properties.
     * @param tableState for noting optimization in progress, etc.
     * @param ds the DataSource
     */
    protected void scheduleOptimizations( MySQLDiskCacheAttributes attributes, TableState tableState, DataSource ds  )
    {
        if ( attributes != null )
        {
            if ( attributes.getOptimizationSchedule() != null )
            {
                log.info( "Will try to configure optimization for table [{0}] on schedule [{1}]",
                        () -> attributes.getTableName(),  () -> attributes.getOptimizationSchedule());

                MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState, ds );

                // loop through the dates.
                try
                {
                    Date[] dates = ScheduleParser.createDatesForSchedule( attributes.getOptimizationSchedule() );
                    if ( dates != null )
                    {
                        for ( int i = 0; i < dates.length; i++ )
                        {
                            this.scheduleOptimization( dates[i], optimizer );
                        }
                    }
                }
                catch ( ParseException e )
                {
                    log.warn( "Problem creating optimization schedule for table [{0}]",
                            attributes.getTableName(), e );
                }
            }
            else
            {
                log.info( "Optimization is not configured for table [{0}]",
                        attributes.getTableName());
            }
        }
    }

    /**
     * This takes in a single time and schedules the optimizer to be called at that time every day.
     * <p>
     * @param startTime -- HH:MM:SS format
     * @param optimizer
     */
    protected void scheduleOptimization( Date startTime, MySQLTableOptimizer optimizer )
    {
        log.info( "startTime [{0}] for optimizer {1}", startTime, optimizer );

        Date now = new Date();
        long initialDelay = startTime.getTime() - now.getTime();

        // have the daemon execute the optimization
        getScheduledExecutorService().scheduleAtFixedRate(() -> optimizeTable(optimizer),
                initialDelay, 86400L, TimeUnit.SECONDS );
    }

    /**
     * This calls the optimizers' optimize table method. This is used by the timer.
     * <p>
     * @author Aaron Smuts
     */
    private void optimizeTable(MySQLTableOptimizer optimizer)
    {
        if ( optimizer != null )
        {
            boolean success = optimizer.optimizeTable();
            log.info( "Optimization success status [{0}]", success );
        }
        else
        {
            log.warn( "OptimizerRunner: The optimizer is null. Could not optimize table." );
        }
    }
}
