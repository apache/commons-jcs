package org.apache.jcs.auxiliary.lateral;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.lateral.behavior.ILateralCacheManager;
import org.apache.jcs.engine.CacheConstants;

/**
 * Used to monitor and repair any failed connection for the lateral cache
 * service. By default the monitor operates in a failure driven mode. That is,
 * it goes into a wait state until there is an error. Upon the notification of a
 * connection error, the monitor changes to operate in a time driven mode. That
 * is, it attempts to recover the connections on a periodic basis. When all
 * failed connections are restored, it changes back to the failure driven mode.
 *
 */
public class LateralCacheMonitor
    implements Runnable
{
    private final static Log log = LogFactory.getLog( LateralCacheMonitor.class );

    private static long idlePeriod = 20 * 1000;

    // minimum 20 seconds.
    //private static long idlePeriod = 3*1000; // for debugging.

    // Must make sure LateralCacheMonitor is started before any lateral error
    // can be detected!
    private boolean alright = true;

    private final static int ERROR = 1;

    private static int mode = ERROR;

    private ILateralCacheManager manager;

    /**
     * Configures the idle period between repairs.
     *
     * @param idlePeriod
     *            The new idlePeriod value
     */
    public static void setIdlePeriod( long idlePeriod )
    {
        if ( idlePeriod > LateralCacheMonitor.idlePeriod )
        {
            LateralCacheMonitor.idlePeriod = idlePeriod;
        }
    }

    /**
     * Allows close classes, ie testers to set the idle period to something
     * testable.
     *
     * @param idlePeriod
     */
    protected static void forceShortIdlePeriod( long idlePeriod )
    {
        LateralCacheMonitor.idlePeriod = idlePeriod;
    }

    /** Constructor for the LateralCacheMonitor object
     * <p>
     * It's the clients responsibility to decide how many
     * of these there will be.
     *
     * @param manager
     */
    public LateralCacheMonitor( ILateralCacheManager manager )
    {
        this.manager = manager;
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

    /**
     * Main processing method for the LateralCacheMonitor object
     */
    public void run()
    {
        do
        {
            if ( mode == ERROR )
            {
                if ( log.isDebugEnabled() )
                {
                    if ( alright )
                    {
                        log.debug( "ERROR DRIVEN MODE: alright = " + alright
                            + ", connection monitor will wait for an error." );
                    }
                    else
                    {
                        log.debug( "ERROR DRIVEN MODE: alright = " + alright + " connection monitor running." );
                    }
                }

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
                                //no op, this is expected
                            }
                        }
                    }
                }
            }
            else
            {
                log.debug( "TIME DRIVEN MODE: connection monitor will sleep for " + idlePeriod + " after this run." );
                // Time driven mode: sleep between each round of recovery
                // attempt.
                // will need to test not just check status
            }

            // The "alright" flag must be false here.
            // Simply presume we can fix all the errors until proven otherwise.
            synchronized ( this )
            {
                alright = true;
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "Cache monitor running." );
            }

            // Monitor each LateralCacheManager instance one after the other.
            // Each LateralCacheManager corresponds to one lateral connection.
            log.info( "LateralCacheManager.instances.size() = " + manager.getInstances().size() );
            //for
            int cnt = 0;
            Iterator itr = manager.getInstances().values().iterator();
            while ( itr.hasNext() )
            {
                cnt++;
                ILateralCacheManager mgr = (ILateralCacheManager) itr.next();
                try
                {
                    // If any cache is in error, it strongly suggests all caches
                    // managed by the
                    // same LateralCacheManager instance are in error. So we fix
                    // them once and for all.
                    //for
                    //log.info( "\n " + cnt + "- mgr.lca.getTcpServer() = " + mgr.lca.getTcpServer() + " mgr = " + mgr );
                    log.info( "\n " + cnt + "- mgr.getCaches().size() = " + mgr.getCaches().size() );

                    if ( mgr.getCaches().size() == 0 )
                    {
                        // there is probably a problem.
                        // monitor may be running when we just started up and
                        // there
                        // is not a cache yet.
                        // if this is error driven mode, mark as bad,
                        // otherwise we will come back around argain.
                        if ( mode == ERROR )
                        {
                            bad();
                        }
                    }

                    Iterator itr2 = mgr.getCaches().values().iterator();

                    while ( itr2.hasNext() )
                    {
                        LateralCacheNoWait c = (LateralCacheNoWait) itr2.next();
                        if ( c.getStatus() == CacheConstants.STATUS_ERROR )
                        {
                            log.info( "found LateralCacheNoWait in error, " + c.toString() );

                            LateralCacheRestore repairer = new LateralCacheRestore( mgr );
                            // If we can't fix them, just skip and re-try in the
                            // next round.
                            if ( repairer.canFix() )
                            {
                                repairer.fix();
                            }
                            else
                            {
                                bad();
                            }
                            //break;
                        }
                        else
                        {
                            log.info( "Lateral Cache No Wait not in error" );
                        }
                    }
                }
                catch ( Exception ex )
                {
                    bad();
                    // Problem encountered in fixing the caches managed by a
                    // LateralCacheManager instance.
                    // Soldier on to the next LateralCacheManager instance.
                    log.error( "Problem encountered in fixing the caches", ex );
                }
            }

            try
            {
                // don't want to sleep after waking from an error
                // run immediately and sleep here.
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Lateral cache monitor sleeping for " + idlePeriod + " between runs." );
                }

                Thread.sleep( idlePeriod );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }
        }
        while ( true );
    }

    /**
     * Sets the "alright" flag to false in a critial section.
     */
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
