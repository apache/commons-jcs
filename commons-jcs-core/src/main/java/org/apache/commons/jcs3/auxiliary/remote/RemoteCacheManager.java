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
import java.rmi.Naming;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheClient;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.CacheWatchRepairable;
import org.apache.commons.jcs3.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs3.engine.ZombieCacheWatch;
import org.apache.commons.jcs3.engine.behavior.ICacheObserver;
import org.apache.commons.jcs3.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * An instance of RemoteCacheManager corresponds to one remote connection of a specific host and
 * port. All RemoteCacheManager instances are monitored by the singleton RemoteCacheMonitor
 * monitoring daemon for error detection and recovery.
 * <p>
 * Getting an instance of the remote cache has the effect of getting a handle on the remote server.
 * Listeners are not registered with the server until a cache is requested from the manager.
 */
public class RemoteCacheManager
{
    /** The logger */
    private static final Log log = LogManager.getLog( RemoteCacheManager.class );

    /** Contains instances of RemoteCacheNoWait managed by a RemoteCacheManager instance. */
    private final ConcurrentMap<String, RemoteCacheNoWait<?, ?>> caches =
            new ConcurrentHashMap<>();

    /** The event logger. */
    private final ICacheEventLogger cacheEventLogger;

    /** The serializer. */
    private final IElementSerializer elementSerializer;

    /** Handle to the remote cache service; or a zombie handle if failed to connect. */
    private ICacheServiceNonLocal<?, ?> remoteService;

    /**
     * Wrapper of the remote cache watch service; or wrapper of a zombie service if failed to
     * connect.
     */
    private final CacheWatchRepairable remoteWatch;

    /** The cache manager listeners will need to use to get a cache. */
    private final ICompositeCacheManager cacheMgr;

    /** For error notification */
    private final RemoteCacheMonitor monitor;

    /** The service found through lookup */
    private final String registry;

    /** can it be restored */
    private boolean canFix = true;

    /**
     * Constructs an instance to with the given remote connection parameters. If the connection
     * cannot be made, "zombie" services will be temporarily used until a successful re-connection
     * is made by the monitoring daemon.
     * <p>
     * @param cattr cache attributes
     * @param cacheMgr the cache hub
     * @param monitor the cache monitor thread for error notifications
     * @param cacheEventLogger
     * @param elementSerializer
     */
    protected RemoteCacheManager( final IRemoteCacheAttributes cattr, final ICompositeCacheManager cacheMgr,
                                final RemoteCacheMonitor monitor,
                                final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer)
    {
        this.cacheMgr = cacheMgr;
        this.monitor = monitor;
        this.cacheEventLogger = cacheEventLogger;
        this.elementSerializer = elementSerializer;
        this.remoteWatch = new CacheWatchRepairable();

        this.registry = RemoteUtils.getNamingURL(cattr.getRemoteLocation(), cattr.getRemoteServiceName());

        try
        {
            lookupRemoteService();
        }
        catch (final IOException e)
        {
            log.error("Could not find server", e);
            // Notify the cache monitor about the error, and kick off the
            // recovery process.
            monitor.notifyError();
        }
    }

    /**
     * Lookup remote service from registry
     * @throws IOException if the remote service could not be found
     *
     */
    protected void lookupRemoteService() throws IOException
    {
        log.info( "Looking up server [{0}]", registry );
        try
        {
            final Object obj = Naming.lookup( registry );
            log.info( "Server found: {0}", obj );

            // Successful connection to the remote server.
            this.remoteService = (ICacheServiceNonLocal<?, ?>) obj;
            log.debug( "Remote Service = {0}", remoteService );
            remoteWatch.setCacheWatch( (ICacheObserver) remoteService );
        }
        catch ( final Exception ex )
        {
            // Failed to connect to the remote server.
            // Configure this RemoteCacheManager instance to use the "zombie"
            // services.
            this.remoteService = new ZombieCacheServiceNonLocal<>();
            remoteWatch.setCacheWatch( new ZombieCacheWatch() );
            throw new IOException( "Problem finding server at [" + registry + "]", ex );
        }
    }

    /**
     * Adds the remote cache listener to the underlying cache-watch service.
     * <p>
     * @param cattr The feature to be added to the RemoteCacheListener attribute
     * @param listener The feature to be added to the RemoteCacheListener attribute
     * @throws IOException
     */
    public <K, V> void addRemoteCacheListener( final IRemoteCacheAttributes cattr, final IRemoteCacheListener<K, V> listener )
        throws IOException
    {
        if ( cattr.isReceive() )
        {
            log.info( "The remote cache is configured to receive events from the remote server. "
                + "We will register a listener. remoteWatch = {0} | IRemoteCacheListener = {1}"
                + " | cacheName ", remoteWatch, listener, cattr.getCacheName() );

            remoteWatch.addCacheListener( cattr.getCacheName(), listener );
        }
        else
        {
            log.info( "The remote cache is configured to NOT receive events from the remote server. "
                    + "We will NOT register a listener." );
        }
    }

    /**
     * Removes a listener. When the primary recovers the failover must deregister itself for a
     * region. The failover runner will call this method to de-register. We do not want to deregister
     * all listeners to a remote server, in case a failover is a primary of another region. Having
     * one regions failover act as another servers primary is not currently supported.
     * <p>
     * @param cattr
     * @throws IOException
     */
    public void removeRemoteCacheListener( final IRemoteCacheAttributes cattr )
        throws IOException
    {
        final RemoteCacheNoWait<?, ?> cache = caches.get( cattr.getCacheName() );
        if ( cache != null )
        {
        	removeListenerFromCache(cache);
        }
        else
        {
            if ( cattr.isReceive() )
            {
                log.warn( "Trying to deregister Cache Listener that was never registered." );
            }
            else
            {
                log.debug( "Since the remote cache is configured to not receive, "
                    + "there is no listener to deregister." );
            }
        }
    }

    // common helper method
	private void removeListenerFromCache(final RemoteCacheNoWait<?, ?> cache) throws IOException
	{
		final IRemoteCacheClient<?, ?> rc = cache.getRemoteCache();
	    log.debug( "Found cache for [{0}], deregistering listener.", cache::getCacheName);
		// could also store the listener for a server in the manager.
        remoteWatch.removeCacheListener(cache.getCacheName(), rc.getListener());
	}

    /**
     * Gets a RemoteCacheNoWait from the RemoteCacheManager. The RemoteCacheNoWait objects are
     * identified by the cache name value of the RemoteCacheAttributes object.
     * <p>
     * If the client is configured to register a listener, this call results on a listener being
     * created if one isn't already registered with the remote cache for this region.
     * <p>
     * @param cattr
     * @return The cache value
     */
    @SuppressWarnings("unchecked") // Need to cast because of common map for all caches
    public <K, V> RemoteCacheNoWait<K, V> getCache( final IRemoteCacheAttributes cattr )
    {
        // might want to do some listener sanity checking here.
        return (RemoteCacheNoWait<K, V>) caches.computeIfAbsent(cattr.getCacheName(),
                key -> newRemoteCacheNoWait(cattr));
    }

    /**
     * Create new RemoteCacheNoWait instance
     *
     * @param cattr the cache configuration
     * @return the instance
     */
    protected <K, V> RemoteCacheNoWait<K, V> newRemoteCacheNoWait(final IRemoteCacheAttributes cattr)
    {
        final RemoteCacheNoWait<K, V> remoteCacheNoWait;
        // create a listener first and pass it to the remotecache
        // sender.
        RemoteCacheListener<K, V> listener = null;
        try
        {
            listener = new RemoteCacheListener<>( cattr, cacheMgr, elementSerializer );
            addRemoteCacheListener( cattr, listener );
        }
        catch ( final IOException e )
        {
            log.error( "Problem adding listener. RemoteCacheListener = {0}",
                    listener, e );
        }

        @SuppressWarnings("unchecked")
        final IRemoteCacheClient<K, V> remoteCacheClient =
            new RemoteCache<>(cattr, (ICacheServiceNonLocal<K, V>) remoteService, listener, monitor);
        remoteCacheClient.setCacheEventLogger( cacheEventLogger );
        remoteCacheClient.setElementSerializer( elementSerializer );

        remoteCacheNoWait = new RemoteCacheNoWait<>( remoteCacheClient );
        remoteCacheNoWait.setCacheEventLogger( cacheEventLogger );
        remoteCacheNoWait.setElementSerializer( elementSerializer );

        return remoteCacheNoWait;
    }

    /** Shutdown all. */
    public void release()
    {
        caches.forEach((name, cache) -> {
            try
            {
                log.info("freeCache [{0}]", name);

                removeListenerFromCache(cache);
                cache.dispose();
            }
            catch ( final IOException ex )
            {
                log.error("Problem releasing {0}", name, ex);
            }
        });
        caches.clear();
    }

    /**
     * Fixes up all the caches managed by this cache manager.
     */
    public void fixCaches()
    {
        if ( !canFix )
        {
            return;
        }

        log.info( "Fixing caches. ICacheServiceNonLocal {0} | IRemoteCacheObserver {1}",
                remoteService, remoteWatch );

        caches.values().stream()
            .filter(cache -> cache.getStatus() == CacheStatus.ERROR)
            .forEach(cache -> cache.fixCache(remoteService));

        if ( log.isInfoEnabled() )
        {
            final String msg = "Remote connection to " + registry + " resumed.";
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logApplicationEvent( "RemoteCacheManager", "fix", msg );
            }
            log.info( msg );
        }
    }

    /**
     * Returns true if the connection to the remote host can be
     * successfully re-established.
     * <p>
     * @return true if we found a failover server
     */
    public boolean canFixCaches()
    {
        try
        {
            lookupRemoteService();
        }
        catch (final IOException e)
        {
            log.error("Could not find server", e);
            canFix = false;
        }

        return canFix;
    }
}
