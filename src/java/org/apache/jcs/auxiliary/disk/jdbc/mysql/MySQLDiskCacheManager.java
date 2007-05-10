package org.apache.jcs.auxiliary.disk.jdbc.mysql;

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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCacheAttributes;
import org.apache.jcs.auxiliary.disk.jdbc.JDBCDiskCacheManagerAbstractTemplate;
import org.apache.jcs.auxiliary.disk.jdbc.TableState;
import org.apache.jcs.auxiliary.disk.jdbc.mysql.util.ScheduleFormatException;
import org.apache.jcs.auxiliary.disk.jdbc.mysql.util.ScheduleParser;

/**
 * This manages instances of the MySQL jdbc disk cache. It maintains one for
 * each region. One for all regions would work, but this gives us more detailed
 * stats by region.
 * <p>
 * Although the generic JDBC Disk Cache Manager can be used for MySQL, the MySQL
 * JDBC Disk Cache has additional features, such as table optimization that are
 * particular to MySQL.
 */
public class MySQLDiskCacheManager
    extends JDBCDiskCacheManagerAbstractTemplate
{
    private static final long serialVersionUID = -8258856770927857896L;

    private static final Log log = LogFactory.getLog( MySQLDiskCacheManager.class );

    private static MySQLDiskCacheManager instance;

    private MySQLDiskCacheAttributes defaultJDBCDiskCacheAttributes;

    // ms in a day
    private static final int DAILY_INTERVAL = 60 * 60 * 24 * 1000;

    // for schedule optimizations
    private Timer daemon = null;

    /**
     * Constructor for the HSQLCacheManager object
     * <p>
     * @param cattr
     */
    private MySQLDiskCacheManager( MySQLDiskCacheAttributes cattr )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Creating MySQLDiskCacheManager with " + cattr );
        }
        defaultJDBCDiskCacheAttributes = cattr;
    }

    /**
     * Gets the defaultCattr attribute of the HSQLCacheManager object
     * <p>
     * @return The defaultCattr value
     */
    public MySQLDiskCacheAttributes getDefaultJDBCDiskCacheAttributes()
    {
        return defaultJDBCDiskCacheAttributes;
    }

    /**
     * Gets the instance attribute of the HSQLCacheManager class
     * <p>
     * @param cattr
     * @return The instance value
     */
    public static MySQLDiskCacheManager getInstance( MySQLDiskCacheAttributes cattr )
    {
        synchronized ( MySQLDiskCacheManager.class )
        {
            if ( instance == null )
            {
                instance = new MySQLDiskCacheManager( cattr );
            }
        }
        clients++;
        return instance;
    }

    /**
     * Gets the cache attribute of the HSQLCacheManager object
     * <p>
     * @param cacheName
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        MySQLDiskCacheAttributes cattr = (MySQLDiskCacheAttributes) defaultJDBCDiskCacheAttributes.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }

    /**
     * Creates a JDBCDiskCache using the supplied attributes.
     * <p>
     * @param cattr
     * @return
     */
    protected AuxiliaryCache createJDBCDiskCache( JDBCDiskCacheAttributes cattr, TableState tableState )
    {
        AuxiliaryCache raf = new MySQLDiskCache( (MySQLDiskCacheAttributes) cattr, tableState );

        scheduleOptimizations( (MySQLDiskCacheAttributes) cattr, tableState );

        return raf;
    }

    /**
     * For each time in the optimization schedule, this calls schedule
     * Optimizaiton.
     * <p>
     * @param attributes
     * @param tableState
     */
    protected void scheduleOptimizations( MySQLDiskCacheAttributes attributes, TableState tableState )
    {
        if ( attributes != null )
        {
            if ( attributes.getOptimizationSchedule() != null )
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Will try to configure optimization for table [" + attributes.getTableName()
                        + "] on schdule [" + attributes.getOptimizationSchedule() + "]" );
                }

                MySQLTableOptimizer optimizer = new MySQLTableOptimizer( attributes, tableState );

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
                catch ( ScheduleFormatException e )
                {
                    log.warn( "Problem creating optimization schedule for table [" + attributes.getTableName() + "]" );
                }
            }
            else
            {
                if ( log.isInfoEnabled() )
                {
                    log.info( "Optimization is not configured for table [" + attributes.getTableName() + "]" );
                }
            }
        }
    }

    /**
     * This takes in a single time and schedules the optimizer to be called at
     * that time every day.
     * <p>
     * @param startTime --
     *            HH:MM:SS format
     * @param optimizer
     */
    protected void scheduleOptimization( Date startTime, MySQLTableOptimizer optimizer )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "startTime [" + startTime + "] for optimizer " + optimizer );
        }

        // create clock daemon if necessary
        if ( daemon == null )
        {
            // true for daemon status
            daemon = new Timer( true );
        }

        // get the runnable from the factory
        TimerTask runnable = new OptimizerTask( optimizer );

        // have the daemon execut our runnable
        // false to not execute immediately.
        daemon.scheduleAtFixedRate( runnable, startTime, DAILY_INTERVAL );

        if ( log.isInfoEnabled() )
        {
            log.info( "Scheduled optimization to begin at [" + startTime + "]" );
        }
    }

    /**
     * This calls the optimizers' optimize table method. This is used by the
     * timer.
     * <p>
     * @author Aaron Smuts
     */
    private class OptimizerTask
        extends TimerTask
    {
        private MySQLTableOptimizer optimizer = null;

        /**
         * Get a handle on the optimizer.
         * <p>
         * @param optimizer
         */
        public OptimizerTask( MySQLTableOptimizer optimizer )
        {
            this.optimizer = optimizer;
        }

        /**
         * This calls optimize on the optimizer.
         * <p>
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            if ( optimizer != null )
            {
                boolean success = optimizer.optimizeTable();
                if ( log.isInfoEnabled() )
                {
                    log.info( "Optimization success status [" + success + "]" );
                }
            }
            else
            {
                log.warn( "OptimizerRunner: The optimizer is null.  Could not optimize table." );
            }
        }
    }
}
