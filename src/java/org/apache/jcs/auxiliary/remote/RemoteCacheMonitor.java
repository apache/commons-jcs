package org.apache.jcs.auxiliary.remote;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Iterator;

import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to monitor and repair any failed connection for the remote cache
 * service. By default the monitor operates in a failure driven mode. That is,
 * it goes into a wait state until there is an error. TODO consider moving this
 * into an active monitoring mode. Upon the notification of a connection error,
 * the monitor changes to operate in a time driven mode. That is, it attempts to
 * recover the connections on a periodic basis. When all failed connections are
 * restored, it changes back to the failure driven mode.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheMonitor implements Runnable
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheMonitor.class );

    private static RemoteCacheMonitor instance;
    private static long idlePeriod = 30 * 1000;
    // minimum 30 seconds.
    //private static long idlePeriod = 3*1000; // for debugging.

    // Must make sure RemoteCacheMonitor is started before any remote error can be detected!
    private boolean alright = true;

    final static int TIME = 0;
    final static int ERROR = 1;
    static int mode = ERROR;


    /**
     * Configures the idle period between repairs.
     *
     * @param idlePeriod The new idlePeriod value
     */
    public static void setIdlePeriod( long idlePeriod )
    {
        if ( idlePeriod > RemoteCacheMonitor.idlePeriod )
        {
            RemoteCacheMonitor.idlePeriod = idlePeriod;
        }
    }


    /** Constructor for the RemoteCacheMonitor object */
    private RemoteCacheMonitor() { }


    /**
     * Returns the singleton instance;
     *
     * @return The instance value
     */
    static RemoteCacheMonitor getInstance()
    {
        if ( instance == null )
        {
            synchronized ( RemoteCacheMonitor.class )
            {
                if ( instance == null )
                {
                    return instance = new RemoteCacheMonitor();
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
        log.debug( "Notified of an error." );
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
    /** Main processing method for the RemoteCacheMonitor object */
    public void run()
    {
        log.debug( "Monitoring daemon started" );
        do
        {

            if ( mode == ERROR )
            {
                if ( alright )
                {
                    synchronized ( this )
                    {
                        if ( alright )
                        {
                            // make this configurable, comment out wait to enter time driven mode
                            // Failure driven mode.
                            try
                            {
                                log.debug( "FAILURE DRIVEN MODE: cache monitor waiting for error" );
                                wait();
                                // wake up only if there is an error.
                            }
                            catch ( InterruptedException ignore )
                            {
                            }
                        }
                    }
                }
            }
            else
            {
                log.debug( "TIME DRIVEN MODE: cache monitor sleeping for " + idlePeriod );
                // Time driven mode: sleep between each round of recovery attempt.
                // will need to test not just check status
            }

            try
            {
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
            //p("cache monitor running.");
            // Monitor each RemoteCacheManager instance one after the other.
            // Each RemoteCacheManager corresponds to one remote connection.
            for ( Iterator itr = RemoteCacheManager.instances.values().iterator(); itr.hasNext();  )
            {
                RemoteCacheManager mgr = ( RemoteCacheManager ) itr.next();
                try
                {
                    // If any cache is in error, it strongly suggests all caches managed by the
                    // same RmicCacheManager instance are in error.  So we fix them once and for all.
                    for ( Iterator itr2 = mgr.caches.values().iterator(); itr2.hasNext();  )
                    {
                        if ( itr2.hasNext() )
                        {
                            RemoteCacheNoWait c = ( RemoteCacheNoWait ) itr2.next();
                            if ( c.getStatus() == CacheConstants.STATUS_ERROR )
                            {
                                RemoteCacheRestore repairer = new RemoteCacheRestore( mgr );
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
                        }
                    }
                }
                catch ( Exception ex )
                {
                    bad();
                    // Problem encountered in fixing the caches managed by a RemoteCacheManager instance.
                    // Soldier on to the next RemoteCacheManager instance.
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

