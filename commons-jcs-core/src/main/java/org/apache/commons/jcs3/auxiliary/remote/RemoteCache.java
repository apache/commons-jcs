package org.apache.commons.jcs3.auxiliary.remote;

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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs3.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Client proxy for an RMI remote cache.
 * <p>
 * This handles gets, updates, and removes. It also initiates failover recovery when an error is
 * encountered.
 */
public class RemoteCache<K, V>
    extends AbstractRemoteAuxiliaryCache<K, V>
{
    /** The logger. */
    private static final Log log = LogManager.getLog( RemoteCache.class );

    /** for error notifications */
    private final RemoteCacheMonitor monitor;

    /** back link for failover initiation */
    private AbstractRemoteCacheNoWaitFacade<K, V> facade;

    /**
     * Constructor for the RemoteCache object. This object communicates with a remote cache server.
     * One of these exists for each region. This also holds a reference to a listener. The same
     * listener is used for all regions for one remote server. Holding a reference to the listener
     * allows this object to know the listener id assigned by the remote cache.
     * <p>
     * @param cattr the cache configuration
     * @param remote the remote cache server handle
     * @param listener a listener
     * @param monitor the cache monitor
     */
    public RemoteCache( final IRemoteCacheAttributes cattr,
        final ICacheServiceNonLocal<K, V> remote,
        final IRemoteCacheListener<K, V> listener,
        final RemoteCacheMonitor monitor )
    {
        super( cattr, remote, listener );
        this.monitor = monitor;

        RemoteUtils.configureGlobalCustomSocketFactory( getRemoteCacheAttributes().getRmiSocketFactoryTimeoutMillis() );
    }

    /**
     * @return IStats object
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Remote Cache" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        elems.add(new StatElement<>( "Remote Host:Port", getIPAddressForService() ) );
        elems.add(new StatElement<>( "Remote Type", this.getRemoteCacheAttributes().getRemoteTypeName() ) );

//      if ( this.getRemoteCacheAttributes().getRemoteType() == RemoteType.CLUSTER )
//      {
//          // something cluster specific
//      }

        // get the stats from the super too
        final IStats sStats = super.getStatistics();
        elems.addAll(sStats.getStatElements());

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * Set facade
     *
     * @param facade the facade to set
     */
    protected void setFacade(final AbstractRemoteCacheNoWaitFacade<K, V> facade)
    {
        this.facade = facade;
    }

    /**
     * Get facade
     *
     * @return the facade
     */
    protected AbstractRemoteCacheNoWaitFacade<K, V> getFacade()
    {
        return facade;
    }

    /**
     * Handles exception by disabling the remote cache service before re-throwing the exception in
     * the form of an IOException.
     * <p>
     * @param ex
     * @param msg
     * @param eventName
     * @throws IOException
     */
    @Override
    protected void handleException( final Exception ex, final String msg, final String eventName )
        throws IOException
    {
        final String message = "Disabling remote cache due to error: " + msg;

        logError( cacheName, "", message );
        log.error( message, ex );

        // we should not switch if the existing is a zombie.
        if ( getRemoteCacheService() == null || !( getRemoteCacheService() instanceof ZombieCacheServiceNonLocal ) )
        {
            // TODO make configurable
            setRemoteCacheService( new ZombieCacheServiceNonLocal<>( getRemoteCacheAttributes().getZombieQueueMaxSize() ) );
        }
        // may want to flush if region specifies
        // Notify the cache monitor about the error, and kick off the recovery
        // process.
        monitor.notifyError();

        log.debug( "Initiating failover, rcnwf = {0}", facade );

        if ( facade != null && facade.getAuxiliaryCacheAttributes().getRemoteType() == RemoteType.LOCAL )
        {
            log.debug( "Found facade, calling failover" );
            // may need to remove the noWait index here. It will be 0 if it is
            // local since there is only 1 possible listener.
            facade.failover( facade.getPrimaryServer() );
        }

        if ( ex instanceof IOException )
        {
            throw (IOException) ex;
        }
        throw new IOException( ex );
    }

    /**
     * Debugging info.
     * <p>
     * @return basic info about the RemoteCache
     */
    @Override
    public String toString()
    {
        return "RemoteCache: " + cacheName + " attributes = " + getRemoteCacheAttributes();
    }

    /**
     * Gets the extra info for the event log.
     * <p>
     * @return disk location
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return getIPAddressForService();
    }

    /**
     * IP address for the service, if one is stored.
     * <p>
     * Protected for testing.
     * <p>
     * @return String
     */
    protected String getIPAddressForService()
    {
        String ipAddress = "(null)";
        if (this.getRemoteCacheAttributes().getRemoteLocation() != null)
        {
            ipAddress = this.getRemoteCacheAttributes().getRemoteLocation().toString();
        }
        return ipAddress;
    }
}
