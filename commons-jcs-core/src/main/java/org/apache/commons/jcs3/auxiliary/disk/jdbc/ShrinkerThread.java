package org.apache.commons.jcs3.auxiliary.disk.jdbc;

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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;

/**
 * Calls delete expired on the disk caches. The shrinker is run by a clock daemon. The shrinker
 * calls delete on each region. It pauses between calls.
 * <p>
 * @author Aaron Smuts
 */
public class ShrinkerThread
    implements Runnable
{
    /** The logger. */
    private static final Log log = LogManager.getLog( ShrinkerThread.class );

    /** A set of JDBCDiskCache objects to call deleteExpired on. */
    private final CopyOnWriteArraySet<JDBCDiskCache<?, ?>> shrinkSet =
            new CopyOnWriteArraySet<>();

    /** Default time period to use. */
    private static final long DEFAULT_PAUSE_BETWEEN_REGION_CALLS_MILLIS = 5000;

    /**
     * How long should we wait between calls to deleteExpired when we are iterating through the list
     * of regions. Delete can lock the table. We want to give clients a chance to get some work
     * done.
     */
    private long pauseBetweenRegionCallsMillis = DEFAULT_PAUSE_BETWEEN_REGION_CALLS_MILLIS;

    /**
     * Does nothing special.
     */
    protected ShrinkerThread()
    {
    }

    /**
     * Adds a JDBC disk cache to the set of disk cache to shrink.
     * <p>
     * @param diskCache
     */
    public void addDiskCacheToShrinkList( final JDBCDiskCache<?, ?> diskCache )
    {
        // the set will prevent dupes.
        // we could also just add these to a hashmap by region name
        // but that might cause a problem if you wanted to use two different
        // jbdc disk caches for the same region.
        shrinkSet.add( diskCache );
    }

    /**
     * Calls deleteExpired on each item in the set. It pauses between each call.
     */
    @Override
    public void run()
    {
        try
        {
            deleteExpiredFromAllRegisteredRegions();
        }
        catch ( final Throwable e )
        {
            log.error( "Caught an exception while trying to delete expired items.", e );
        }
    }

    /**
     * Deletes the expired items from all the registered regions.
     */
    private void deleteExpiredFromAllRegisteredRegions()
    {
        log.info( "Running JDBC disk cache shrinker. Number of regions [{0}]",
                shrinkSet::size);

        for (final Iterator<JDBCDiskCache<?, ?>> i = shrinkSet.iterator(); i.hasNext();)
        {
            final JDBCDiskCache<?, ?> cache = i.next();
            final ElapsedTimer timer = new ElapsedTimer();
            final int deleted = cache.deleteExpired();

            log.info( "Deleted [{0}] expired for region [{1}] for table [{2}] in {3} ms.",
                    deleted, cache.getCacheName(), cache.getTableName(), timer.getElapsedTime() );

            // don't pause after the last call to delete expired.
            if ( i.hasNext() )
            {
                log.info( "Pausing for [{0}] ms before shrinking the next region.",
                        this.getPauseBetweenRegionCallsMillis() );

                try
                {
                    Thread.sleep( this.getPauseBetweenRegionCallsMillis() );
                }
                catch ( final InterruptedException e )
                {
                    log.warn( "Interrupted while waiting to delete expired for the next region." );
                }
            }
        }
    }

    /**
     * How long should we wait between calls to deleteExpired when we are iterating through the list
     * of regions.
     * <p>
     * @param pauseBetweenRegionCallsMillis The pauseBetweenRegionCallsMillis to set.
     */
    public void setPauseBetweenRegionCallsMillis( final long pauseBetweenRegionCallsMillis )
    {
        this.pauseBetweenRegionCallsMillis = pauseBetweenRegionCallsMillis;
    }

    /**
     * How long should we wait between calls to deleteExpired when we are iterating through the list
     * of regions.
     * <p>
     * @return Returns the pauseBetweenRegionCallsMillis.
     */
    public long getPauseBetweenRegionCallsMillis()
    {
        return pauseBetweenRegionCallsMillis;
    }
}
