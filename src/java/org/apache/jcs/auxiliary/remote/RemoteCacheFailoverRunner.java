package org.apache.jcs.auxiliary.remote;


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


import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.CacheConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
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
                Thread.sleep( idlePeriod );
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
