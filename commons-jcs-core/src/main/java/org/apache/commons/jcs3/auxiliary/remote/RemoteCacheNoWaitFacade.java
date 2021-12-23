package org.apache.commons.jcs3.auxiliary.remote;

import java.io.IOException;

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

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICache;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Used to provide access to multiple services under nowait protection. Factory should construct
 * NoWaitFacade to give to the composite cache out of caches it constructs from the varies manager
 * to lateral services.
 * <p>
 * Typically, we only connect to one remote server per facade. We use a list of one
 * RemoteCacheNoWait.
 */
public class RemoteCacheNoWaitFacade<K, V>
    extends AbstractRemoteCacheNoWaitFacade<K, V>
{
    /** log instance */
    private static final Log log = LogManager.getLog( RemoteCacheNoWaitFacade.class );

    /** Provide factory instance to RemoteCacheFailoverRunner */
    private final RemoteCacheFactory cacheFactory;

    /** Attempt to restore primary connection (switched off for testing) */
    protected boolean attemptRestorePrimary = true;

    /** Time in ms to sleep between failover attempts */
    private static final long idlePeriod = 20000L;

    /**
     * Constructs with the given remote cache, and fires events to any listeners.
     * <p>
     * @param noWaits
     * @param rca
     * @param cacheEventLogger
     * @param elementSerializer
     * @param cacheFactory
     */
    public RemoteCacheNoWaitFacade( final List<RemoteCacheNoWait<K,V>> noWaits,
                                    final IRemoteCacheAttributes rca,
                                    final ICacheEventLogger cacheEventLogger,
                                    final IElementSerializer elementSerializer,
                                    final RemoteCacheFactory cacheFactory)
    {
        super( noWaits, rca, cacheEventLogger, elementSerializer );
        this.cacheFactory = cacheFactory;
    }

    /**
     * Begin the failover process if this is a local cache. Clustered remote caches do not failover.
     * <p>
     * @param rcnw The no wait in error.
     */
    @Override
    protected void failover( final RemoteCacheNoWait<K, V> rcnw )
    {
        log.debug( "in failover for {0}", rcnw );

        if ( getAuxiliaryCacheAttributes().getRemoteType() == RemoteType.LOCAL )
        {
            if ( rcnw.getStatus() == CacheStatus.ERROR )
            {
                // start failover, primary recovery process
                final Thread runner = new Thread(this::connectAndRestore);
                runner.setDaemon( true );
                runner.start();

                if ( getCacheEventLogger() != null )
                {
                    getCacheEventLogger().logApplicationEvent( "RemoteCacheNoWaitFacade", "InitiatedFailover",
                                                               rcnw + " was in error." );
                }
            }
            else
            {
                log.info( "The noWait is not in error" );
            }
        }
    }

    /**
     * The thread tries to establish a connection with a failover
     * server, if any are defined. Once a failover connection is made, it will
     * attempt to replace the failover with the primary remote server.
     * <p>
     * It works by switching out the RemoteCacheNoWait inside the Facade.
     * <p>
     * Client (i.e.) the CompositeCache has reference to a RemoteCacheNoWaitFacade.
     * This facade is created by the RemoteCacheFactory. The factory maintains a set
     * of managers, one for each remote server. Typically, there will only be one
     * manager.
     * <p>
     * If you use multiple remote servers, you may want to set one or more as
     * failovers. If a local cache cannot connect to the primary server, or looses
     * its connection to the primary server, it will attempt to restore that
     * Connection in the background. If failovers are defined, the Failover runner
     * will try to connect to a failover until the primary is restored.
     * If no failovers are defined, this will exit automatically.
     *
     * @since 3.1
     */
    protected void connectAndRestore()
    {
        final IRemoteCacheAttributes rca0 = getAuxiliaryCacheAttributes();
        // Each RemoteCacheManager corresponds to one remote connection.
        final List<RemoteLocation> failovers = rca0.getFailovers();
        // we should probably check to see if there are any failovers,
        // even though the caller should have already.

        if ( failovers == null )
        {
            log.warn( "Remote is misconfigured, failovers was null." );
            return;
        }
        if ( failovers.size() == 1 )
        {
            // if there is only the primary, return out of this
            log.info( "No failovers defined, exiting failover runner." );
            return;
        }

        final AtomicBoolean allright = new AtomicBoolean(false);

        do
        {
            log.info( "Remote cache FAILOVER RUNNING." );

            // there is no active listener
            if ( !allright.get() )
            {
                // Monitor each RemoteCacheManager instance one after the other.
                final int fidx = rca0.getFailoverIndex();
                log.debug( "fidx = {0} failovers.size = {1}", rca0::getFailoverIndex, failovers::size);

                // If we don't check the primary, if it gets connected in the
                // background,
                // we will disconnect it only to put it right back
                final ListIterator<RemoteLocation> i = failovers.listIterator(fidx); // + 1; // +1 skips the primary
                log.debug( "starting at failover i = {0}", i );

                // try them one at a time until successful
                while (i.hasNext() && !allright.get())
                {
                    final int failoverIndex = i.nextIndex();
                    final RemoteLocation server = i.next();
                    log.debug("Trying server [{0}] at failover index i = {1}", server, failoverIndex);

                    final RemoteCacheAttributes rca = (RemoteCacheAttributes) rca0.clone();
                    rca.setRemoteLocation(server);
                    final RemoteCacheManager rcm = cacheFactory.getManager( rca );

                    log.debug( "RemoteCacheAttributes for failover = {0}", rca );

                    if (rcm != null)
                    {
                        // add a listener if there are none, need to tell rca
                        // what number it is at
                        final ICache<K, V> ic = rcm.getCache( rca );
                        if ( ic.getStatus() == CacheStatus.ALIVE )
                        {
                            // may need to do this more gracefully
                            log.debug( "resetting no wait" );
                            restorePrimaryServer((RemoteCacheNoWait<K, V>) ic);
                            rca0.setFailoverIndex(failoverIndex);

                            log.debug("setting ALLRIGHT to true");
                            if (i.hasPrevious())
                            {
                                log.debug("Moving to Primary Recovery Mode, failover index = {0}", failoverIndex);
                            }
                            else
                            {
                                log.debug("No need to connect to failover, the primary server is back up.");
                            }

                            allright.set(true);

                            log.info( "CONNECTED to host = [{0}]", rca::getRemoteLocation);
                        }
                    }
                }
            }
            // end if !allright
            // get here if while index >0 and allright, meaning that we are
            // connected to some backup server.
            else
            {
                log.debug( "ALLRIGHT is true " );
                log.info( "Failover runner is in primary recovery mode. "
                        + "Failover index = {0} Will now try to reconnect to "
                        + "primary server.", rca0::getFailoverIndex);
            }

            // Exit loop if in test mode
            if (allright.get() && !attemptRestorePrimary)
            {
                break;
            }

            boolean primaryRestoredSuccessfully = false;
            // if we are not connected to the primary, try.
            if (rca0.getFailoverIndex() > 0)
            {
                primaryRestoredSuccessfully = restorePrimary();
                log.debug( "Primary recovery success state = {0}",
                        primaryRestoredSuccessfully );
            }

            if (!primaryRestoredSuccessfully)
            {
                // Time driven mode: sleep between each round of recovery attempt.
                try
                {
                    log.warn( "Failed to reconnect to primary server. "
                            + "Cache failover runner is going to sleep for "
                            + "{0} milliseconds.", idlePeriod );
                    Thread.sleep( idlePeriod );
                }
                catch ( final InterruptedException ex )
                {
                    // ignore;
                }
            }

            // try to bring the listener back to the primary
        }
        while (rca0.getFailoverIndex() > 0 || !allright.get());
        // continue if the primary is not restored or if things are not allright.

        if ( log.isInfoEnabled() )
        {
            final int failoverIndex = rca0.getFailoverIndex();
            log.info( "Exiting failover runner. Failover index = {0}", failoverIndex);

            if ( failoverIndex <= 0 )
            {
                log.info( "Failover index is <= 0, meaning we are not connected to a failover server." );
            }
            else
            {
                log.info( "Failover index is > 0, meaning we are connected to a failover server." );
            }
        }
    }

    /**
     * Try to restore the primary server.
     * <p>
     * Once primary is restored the failover listener must be deregistered.
     * <p>
     * The primary server is the first server defines in the FailoverServers
     * list.
     *
     * @return boolean value indicating whether the restoration was successful
     */
    private boolean restorePrimary()
    {
        final IRemoteCacheAttributes rca0 = getAuxiliaryCacheAttributes();
        // try to move back to the primary
        final RemoteLocation server = rca0.getFailovers().get(0);

        log.info( "Trying to restore connection to primary remote server "
                + "[{0}]", server );

        final RemoteCacheAttributes rca = (RemoteCacheAttributes) rca0.clone();
        rca.setRemoteLocation(server);
        final RemoteCacheManager rcm = cacheFactory.getManager( rca );

        if (rcm != null)
        {
            // add a listener if there are none, need to tell rca what number it
            // is at
            final ICache<K, V> ic = rcm.getCache( rca );
            // by default the listener id should be 0, else it will be the
            // listener
            // Originally associated with the remote cache. either way is fine.
            // We just don't want the listener id from a failover being used.
            // If the remote server was rebooted this could be a problem if new
            // locals were also added.

            if ( ic.getStatus() == CacheStatus.ALIVE )
            {
                try
                {
                    // we could have more than one listener registered right
                    // now.
                    // this will not result in a loop, only duplication
                    // stop duplicate listening.
                    if (getPrimaryServer() != null && getPrimaryServer().getStatus() == CacheStatus.ALIVE )
                    {
                        final int fidx = rca0.getFailoverIndex();

                        if ( fidx > 0 )
                        {
                            final RemoteLocation serverOld = rca0.getFailovers().get(fidx);

                            log.debug( "Failover Index = {0} the server at that "
                                    + "index is [{1}]", fidx, serverOld );

                            if ( serverOld != null )
                            {
                                // create attributes that reflect the
                                // previous failed over configuration.
                                final RemoteCacheAttributes rcaOld = (RemoteCacheAttributes) rca0.clone();
                                rcaOld.setRemoteLocation(serverOld);
                                final RemoteCacheManager rcmOld = cacheFactory.getManager( rcaOld );

                                if ( rcmOld != null )
                                {
                                    // manager can remove by name if
                                    // necessary
                                    rcmOld.removeRemoteCacheListener( rcaOld );
                                }
                                log.info( "Successfully deregistered from "
                                        + "FAILOVER remote server = {0}", serverOld );
                            }
                        }
                        else if ( fidx == 0 )
                        {
                            // this should never happen. If there are no
                            // failovers this shouldn't get called.
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "No need to restore primary, it is already restored." );
                                return true;
                            }
                        }
                        else {
                            // this should never happen
                            log.warn( "Failover index is less than 0, this shouldn't happen" );
                        }
                    }
                }
                catch ( final IOException e )
                {
                    // TODO, should try again, or somehow stop the listener
                    log.error("Trouble trying to deregister old failover "
                            + "listener prior to restoring the primary = {0}",
                            server, e );
                }

                // Restore primary
                // may need to do this more gracefully, letting the failover finish in the background
                final RemoteCacheNoWait<K, V> failoverNoWait = getPrimaryServer();

                // swap in a new one
                restorePrimaryServer((RemoteCacheNoWait<K, V>) ic);
                rca0.setFailoverIndex( 0 );

                final String message = "Successfully reconnected to PRIMARY "
                        + "remote server. Substituted primary for "
                        + "failoverNoWait [" + failoverNoWait + "]";
                log.info( message );

                if (getCacheEventLogger() != null)
                {
                    getCacheEventLogger().logApplicationEvent(
                            "RemoteCacheFailoverRunner", "RestoredPrimary",
                            message );
                }
                return true;
            }
        }

        // else all right
        // if the failover index was at 0 here, we would be in a bad
        // situation, unless there were just
        // no failovers configured.
        log.debug( "Primary server status in error, not connected." );

        return false;
    }
}
