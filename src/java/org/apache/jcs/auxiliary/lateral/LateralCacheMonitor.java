package org.apache.jcs.auxiliary.lateral;

import java.util.Iterator;

import org.apache.jcs.auxiliary.lateral.LateralCacheManager;
import org.apache.jcs.auxiliary.lateral.LateralCacheNoWait;
import org.apache.jcs.auxiliary.lateral.LateralCacheRestore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to monitor and repair any failed connection for the lateral cache
 * service. By default the monitor operates in a failure driven mode. That is,
 * it goes into a wait state until there is an error. Upon the notification of a
 * connection error, the monitor changes to operate in a time driven mode. That
 * is, it attempts to recover the connections on a periodic basis. When all
 * failed connections are restored, it changes back to the failure driven mode.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class LateralCacheMonitor implements Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralCacheManager.class );

    private static LateralCacheMonitor instance;
    private static long idlePeriod = 20 * 1000;
    // minimum 20 seconds.
    //private static long idlePeriod = 3*1000; // for debugging.

    // Must make sure LateralCacheMonitor is started before any lateral error can be detected!
    private boolean alright = true;


    /**
     * Configures the idle period between repairs.
     *
     * @param idlePeriod The new idlePeriod value
     */
    public static void setIdlePeriod( long idlePeriod )
    {
        if ( idlePeriod > LateralCacheMonitor.idlePeriod )
        {
            LateralCacheMonitor.idlePeriod = idlePeriod;
        }
    }


    /** Constructor for the LateralCacheMonitor object */
    private LateralCacheMonitor() { }


    /**
     * Returns the singleton instance;
     *
     * @return The instance value
     */
    static LateralCacheMonitor getInstance()
    {
        if ( instance == null )
        {
            synchronized ( LateralCacheMonitor.class )
            {
                if ( instance == null )
                {
                    return instance = new LateralCacheMonitor();
                }
            }
        }
        return instance;
    }


    /**
     * Notifies the cache monitor that an error occurred, and kicks off the
     * error recovery process.
     */
    public void notifyError()
    {
        bad();
        synchronized ( this )
        {
            notify();
        }
    }

    // Run forever.

    // Avoid the use of any synchronization in the process of monitoring for performance reason.
    // If exception is thrown owing to synchronization,
    // just skip the monitoring until the next round.
    /** Main processing method for the LateralCacheMonitor object */
    public void run()
    {
        do
        {
            if ( alright )
            {
                synchronized ( this )
                {
                    if ( alright )
                    {
                        // Failure driven mode.
                        try
                        {
                            wait();
                            // wake up only if there is an error.
                        }
                        catch ( InterruptedException ignore )
                        {
                        }
                    }
                }
            }
            // Time driven mode: sleep between each round of recovery attempt.
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "cache monitor sleeping for " + idlePeriod );
                }

                Thread.currentThread().sleep( idlePeriod );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }
            // The "alright" flag must be false here.
            // Simply presume we can fix all the errors until proven otherwise.
            synchronized ( this )
            {
                alright = true;
            }

            log.debug( "cache monitor running." );

            // Monitor each LateralCacheManager instance one after the other.
            // Each LateralCacheManager corresponds to one lateral connection.
            for ( Iterator itr = LateralCacheManager.instances.values().iterator(); itr.hasNext();  )
            {
                LateralCacheManager mgr = ( LateralCacheManager ) itr.next();
                try
                {
                    // If any cache is in error, it strongly suggests all caches managed by the
                    // same LateralCacheManager instance are in error.  So we fix them once and for all.
                    for ( Iterator itr2 = mgr.caches.values().iterator(); itr2.hasNext();  )
                    {
                        if ( itr2.hasNext() )
                        {
                            LateralCacheNoWait c = ( LateralCacheNoWait ) itr2.next();
                            if ( c.getStatus() == c.STATUS_ERROR )
                            {
                                log.debug( "found LateralCacheNoWait in error" );

                                LateralCacheRestore repairer = new LateralCacheRestore( mgr );
                                // If we can't fix them, just skip and re-try in the next round.
                                if ( repairer.canFix() )
                                {
                                    repairer.fix();
                                }
                                else
                                {
                                    bad();
                                }
                                break;
                            }
                            else
                            {
                                log.debug( "lcnw not in error" );
                            }
                        }
                    }
                }
                catch ( Exception ex )
                {
                    bad();
                    // Problem encountered in fixing the caches managed by a LateralCacheManager instance.
                    // Soldier on to the next LateralCacheManager instance.
                    log.error( ex );
                }
            }
        } while ( true );
    }


    /** Sets the "alright" flag to false in a critial section. */
    private void bad()
    {
        if ( alright )
        {
            synchronized ( this )
            {
                alright = false;
            }
        }
    }
}

