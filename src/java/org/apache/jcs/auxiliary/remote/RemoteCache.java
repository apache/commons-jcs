package org.apache.jcs.auxiliary.remote;

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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Client proxy for an RMI remote cache.
 * <p>
 * This handles gets, updates, and removes. It also initiates failover recovery when an error is
 * encountered.
 */
public class RemoteCache
    extends AbstractRemoteAuxiliaryCache
{
    /** Don't change. */
    private static final long serialVersionUID = -5329231850422826460L;

    /** The logger. */
    private final static Log log = LogFactory.getLog( RemoteCache.class );

    /**
     * Constructor for the RemoteCache object. This object communicates with a remote cache server.
     * One of these exists for each region. This also holds a reference to a listener. The same
     * listener is used for all regions for one remote server. Holding a reference to the listener
     * allows this object to know the listener id assigned by the remote cache.
     * <p>
     * @param cattr
     * @param remote
     * @param listener
     */
    public RemoteCache( IRemoteCacheAttributes cattr, IRemoteCacheService remote, IRemoteCacheListener listener )
    {
        super( cattr, remote, listener );

        RemoteUtils.configureGlobalCustomSocketFactory( getRemoteCacheAttributes().getRmiSocketFactoryTimeoutMillis() );
    }

    /**
     * @return IStats object
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Remote Cache No Wait" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Remote Host:Port" );
        se.setData( getIPAddressForService() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Remote Type" );
        se.setData( this.getRemoteCacheAttributes().getRemoteTypeName() + "" );
        elems.add( se );

        if ( this.getRemoteCacheAttributes().getRemoteType() == IRemoteCacheAttributes.CLUSTER )
        {
            // something cluster specific
        }

        // get the stats from the super too
        // get as array, convert to list, add list to our outer list
        IStats sStats = super.getStatistics();
        IStatElement[] sSEs = sStats.getStatElements();
        List sL = Arrays.asList( sSEs );
        elems.addAll( sL );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
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
    protected void handleException( Exception ex, String msg, String eventName )
        throws IOException
    {
        String message = "Disabling remote cache due to error: " + msg;

        logError( cacheName, "", message );
        log.error( message, ex );

        // we should not switch if the existing is a zombie.
        if ( getRemoteCacheService() == null || !( getRemoteCacheService() instanceof ZombieRemoteCacheService ) )
        {
            // TODO make configurable
            setRemoteCacheService( new ZombieRemoteCacheService( getRemoteCacheAttributes().getZombieQueueMaxSize() ) );
        }
        // may want to flush if region specifies
        // Notify the cache monitor about the error, and kick off the recovery
        // process.
        RemoteCacheMonitor.getInstance().notifyError();

        // initiate failover if local
        RemoteCacheNoWaitFacade rcnwf = (RemoteCacheNoWaitFacade) RemoteCacheFactory.getFacades()
            .get( getRemoteCacheAttributes().getCacheName() );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Initiating failover, rcnf = " + rcnwf );
        }

        if ( rcnwf != null && rcnwf.remoteCacheAttributes.getRemoteType() == RemoteCacheAttributes.LOCAL )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Found facade, calling failover" );
            }
            // may need to remove the noWait index here. It will be 0 if it is
            // local since there is only 1 possible listener.
            rcnwf.failover( 0 );
        }

        if ( ex instanceof IOException )
        {
            throw (IOException) ex;
        }
        throw new IOException( ex.getMessage() );
    }

    /**
     * Debugging info.
     * <p>
     * @return basic info about the RemoteCache
     */
    public String toString()
    {
        return "RemoteCache: " + cacheName + " attributes = " + getRemoteCacheAttributes();
    }

    /**
     * Gets the extra info for the event log.
     * <p>
     * @return disk location
     */
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
        String ipAddress = this.getRemoteCacheAttributes().getRemoteHost() + ":"
            + this.getRemoteCacheAttributes().getRemotePort();
        return ipAddress;
    }
}
