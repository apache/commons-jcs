package org.apache.jcs.auxiliary.disk.jdbc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Calls delete expired on the disk caches. The shrinker is run by a clock
 * daemon.
 * 
 * @author Aaron Smuts
 * 
 */
public class ShrinkerThread
    implements Runnable
{
    private final static Log log = LogFactory.getLog( ShrinkerThread.class );

    private Set shrinkSet = Collections.synchronizedSet( new HashSet() );

    /**
     * 
     * @param diskCache
     */
    protected ShrinkerThread()
    {
        super();
    }

    /**
     * Adds a JDBC disk cache to the set of disk cache to shrink.
     * 
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
     * Calls deleteExpired on each item in the set.
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
                int deleted = cache.deleteExpired();

                if ( log.isInfoEnabled() )
                {
                    log.info( "Deleted [" + deleted + "] expired for region [" + cache.getCacheName() + "]" );
                }
            }
        }
    }
}
