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

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheFailoverRunner implements Runnable
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheFailoverRunner.class );

    private RemoteCacheNoWaitFacade facade;

    private static long idlePeriod = 20 * 1000;
    private boolean alright = true;

    /**
     * Constructor for the RemoteCacheFailoverRunner object
     *
     * @param facade
     */
    public RemoteCacheFailoverRunner( RemoteCacheNoWaitFacade facade )
    {
        this.facade = facade;
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
     * Main processing method for the RemoteCacheFailoverRunner object
     */
    public void run()
    {
        do
        {

            // will only be run if there is an error
            /*
             * if (alright) {
             * synchronized(this) {
             * if (alright) {
             * / Failure driven mode.
             * try {
             * wait(); // wake up only if there is an error.
             * } catch(InterruptedException ignore) {
             * }
             * }
             * }
             * }
             * / The "alright" flag must be false here.
             * / Simply presume we can fix all the errors until proven otherwise.
             * synchronized(this) {
             * alright = true;
             * }
             */
            log.debug( "cache failover running." );

            // there is no active listener
            if ( !alright )
            {

                // reset listener id for reidentification by new remote
                // RemoteCacheInfo.listenerId = 0;
                // this may not work, we may need to have unique listener ids per failover

                // Monitor each RemoteCacheManager instance one after the other.
                // Each RemoteCacheManager corresponds to one remote connection.
                String[] failovers = facade.rca.getFailovers();
                int fidx = facade.rca.getFailoverIndex();
                log.debug( "fidx = " + fidx + " failovers.length = " + failovers.length );
                int i = fidx + 1;
                log.debug( "i = " + i );
                for ( ; i < failovers.length; i++ )
                {
                    log.debug( "i = " + i );
                    String server = failovers[i];

                    RemoteCacheAttributes rca = null;
                    try
                    {

                        rca = ( RemoteCacheAttributes ) facade.rca.copy();
                        rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                        rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                        RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca );
                        log.debug( "RemoteCacheAttributes for failover = " + rca.toString() );
                        // add a listener if there are none, need to tell rca what number it is at
                        ICache ic = rcm.getCache( rca.getCacheName() );
                        if ( ic != null )
                        {
                            if ( ic.getStatus() == CacheConstants.STATUS_ALIVE )
                            {
                                // may need to do this more gracefully
                                log.debug( "reseting no wait" );
                                facade.noWaits = new RemoteCacheNoWait[1];
                                facade.noWaits[0] = ( RemoteCacheNoWait ) ic;
                                facade.rca.setFailoverIndex( i );

                                synchronized ( this )
                                {
                                    log.debug( "setting ALRIGHT to true, moving to Primary Recovery Mode" );
                                    alright = true;
                                    log.debug( "CONNECTED to " + rca.getRemoteHost() + ":" + rca.getRemotePort() + "\n\n" );
                                }

                            }
                        }
                        else
                        {
                            //p( "noWait is null" );
                        }

                    }
                    catch ( Exception ex )
                    {
                        bad();
                        log.debug( "FAILED to connect to " + rca.getRemoteHost() + ":" + rca.getRemotePort() );
                        // Problem encountered in fixing the caches managed by a RemoteCacheManager instance.
                        // Soldier on to the next RemoteCacheManager instance.
                        log.error( ex.toString() );
                    }
                }

            }
            // end if !alright
            else
            {
                log.warn( "ALRIGHT is true --  failover runner is in primary recovery mode" );
            }

            //try to move back to the primary
            String[] failovers = facade.rca.getFailovers();
            String server = failovers[0];
            try
            {

                RemoteCacheAttributes rca = ( RemoteCacheAttributes ) facade.rca.copy();
                rca.setRemoteHost( server.substring( 0, server.indexOf( ":" ) ) );
                rca.setRemotePort( Integer.parseInt( server.substring( server.indexOf( ":" ) + 1 ) ) );
                RemoteCacheManager rcm = RemoteCacheManager.getInstance( rca );
                // add a listener if there are none, need to tell rca what number it is at
                ICache ic = rcm.getCache( rca.getCacheName() );
                if ( ic != null )
                {
                    if ( ic.getStatus() == CacheConstants.STATUS_ALIVE )
                    {
                        // may need to do this more gracefully
                        log.debug( "reseting no wait to PRIMARY" );
                        facade.noWaits = new RemoteCacheNoWait[1];
                        facade.noWaits[0] = ( RemoteCacheNoWait ) ic;
                        facade.rca.setFailoverIndex( 0 );
                        //return;
                    }
                }
                else
                {
                    //p( "noWait is null" );
                }

            }
            catch ( Exception ex )
            {
                log.error( ex );
            }

            // Time driven mode: sleep between each round of recovery attempt.
            try
            {
                log.debug( "cache failover runner sleeping for " + idlePeriod );
                Thread.currentThread().sleep( idlePeriod );
            }
            catch ( InterruptedException ex )
            {
                // ignore;
            }

            // try to bring the listener back to the primary
        } while ( facade.rca.getFailoverIndex() > 0 );

        log.debug( "exiting failover runner" );
        return;
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
