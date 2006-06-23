package org.apache.jcs.auxiliary.disk.jdbc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Calls delete expired on the disk caches. The shrinker is run by a clock
 * daemon. The shrinker calls delete on each region. It pauses between calls.
 * <p>
 * @author Aaron Smuts
 */
public class ShrinkerThread
    implements Runnable
{
    private final static Log log = LogFactory.getLog( ShrinkerThread.class );

    /** A set of JDBCDiskCache objects to call deleteExpired on. */
    private Set shrinkSet = Collections.synchronizedSet( new HashSet() );

    /**
     * How long should we wait between calls to deleteExpired when we are
     * iterating through the list of regions. Delete can lock the table. We want
     * to give clients a chance to get some work done.
     */
    private static final long DEFAULT_PAUSE_BETWEEN_REGION_CALLS_MILLIS = 5000;

    private long pauseBetweenRegionCallsMillis = DEFAULT_PAUSE_BETWEEN_REGION_CALLS_MILLIS;

    /**
     * Does nothing special.
     * <p>
     * @param diskCache
     */
    protected ShrinkerThread()
    {
        super();
    }

    /**
     * Adds a JDBC disk cache to the set of disk cache to shrink.
     * <p>
     * @param diskCache
     */
    public void addDiskCacheToShrinkList( JDBCDiskCache diskCache )
    {
        // the set will prevent dupes.
        // we could also just add these to a hasmap by region name
        // but that might cause a problem if you wanted to use two different
        // jbdc disk caches for the same region.
        shrinkSet.add( diskCache );
    }

    /**
     * Calls deleteExpired on each item in the set. It pauses between each call.
     */
    public void run()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Running JDBC disk cache shrinker.  Number of regions [" + shrinkSet.size() + "]" );
        }

        Object[] caches = null;

        synchronized ( shrinkSet )
        {
            caches = this.shrinkSet.toArray();
        }

        if ( caches != null )
        {
            for ( int i = 0; i < caches.length; i++ )
            {
                JDBCDiskCache cache = (JDBCDiskCache) caches[i];

                long start = System.currentTimeMillis();
                int deleted = cache.deleteExpired();
                long end = System.currentTimeMillis();

                if ( log.isInfoEnabled() )
                {
                    log.info( "Deleted [" + deleted + "] expired for region [" + cache.getCacheName() + "] for table ["
                        + cache.getTableName() + "] in " + ( end - start ) + " ms." );
                }

                // don't pause after the last call to delete expired.
                if ( i < caches.length - 1 )
                {
                    if ( log.isInfoEnabled() )
                    {
                        log.info( "Pausing for [" + this.getPauseBetweenRegionCallsMillis()
                            + "] ms. before shinker the next region." );
                    }

                    try
                    {
                        Thread.sleep( this.getPauseBetweenRegionCallsMillis() );
                    }
                    catch ( InterruptedException e )
                    {
                        log.warn( "Interrupted while waiting to delete expired for the enxt region." );
                    }
                }
            }
        }
    }

    /**
     * How long should we wait between calls to deleteExpired when we are
     * iterating through the list of regions.
     * <p>
     * @param pauseBetweenRegionCallsMillis
     *            The pauseBetweenRegionCallsMillis to set.
     */
    public void setPauseBetweenRegionCallsMillis( long pauseBetweenRegionCallsMillis )
    {
        this.pauseBetweenRegionCallsMillis = pauseBetweenRegionCallsMillis;
    }

    /**
     * How long should we wait between calls to deleteExpired when we are
     * iterating through the list of regions.
     * <p>
     * @return Returns the pauseBetweenRegionCallsMillis.
     */
    public long getPauseBetweenRegionCallsMillis()
    {
        return pauseBetweenRegionCallsMillis;
    }
}
