package org.apache.jcs.auxiliary.lateral;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.Iterator;

import org.apache.jcs.engine.CacheConstants;

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
 */
public class LateralCacheMonitor implements Runnable
{
    private final static Log log =
        LogFactory.getLog( LateralCacheMonitor.class );

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

                Thread.sleep( idlePeriod );
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
            log.info( "LateralCacheManager.instances.size() = "  + LateralCacheManager.instances.size() );
            //for
            int cnt = 0;
            Iterator itr = LateralCacheManager.instances.values().iterator();
            while ( itr.hasNext() ) {
                cnt++;
                LateralCacheManager mgr = ( LateralCacheManager ) itr.next();
                try
                {
                    // If any cache is in error, it strongly suggests all caches managed by the
                    // same LateralCacheManager instance are in error.  So we fix them once and for all.
                    //for
                    log.info( "\n " + cnt + "- mgr.lca.getTcpServer() = "  + mgr.lca.getTcpServer() + " mgr = " + mgr);
                    log.info( "\n " + cnt + "- mgr.caches.size() = "  + mgr.caches.size() );
                    Iterator itr2 = mgr.caches.values().iterator();
                    //{
                        while( itr2.hasNext() )
                        {
                            LateralCacheNoWait c = ( LateralCacheNoWait ) itr2.next();
                            if ( c.getStatus() == CacheConstants.STATUS_ERROR )
                            {
                                log.info( "found LateralCacheNoWait in error, " + c.toString() );

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
                                //break;
                            } else {
                              log.info("lcnw not in error");
                            }
                        }
                    //}
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

