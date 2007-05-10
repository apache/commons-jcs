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
import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheClient;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICompositeCacheManager;
import org.apache.jcs.engine.behavior.IShutdownObserver;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * An instance of RemoteCacheManager corresponds to one remote connection of a specific host and
 * port. All RemoteCacheManager instances are monitored by the singleton RemoteCacheMonitor
 * monitoring daemon for error detection and recovery.
 * <p>
 * Getting an instance of the remote cache has the effect of getting a handle on the remote server.
 * Listeners are not registered with the server until a cache is requested from the manager.
 */
public class RemoteCacheManager
    implements AuxiliaryCacheManager, IShutdownObserver
{
    private static final long serialVersionUID = 798077557166389498L;

    private final static Log log = LogFactory.getLog( RemoteCacheManager.class );

    // Contains mappings of Location instance to RemoteCacheManager instance.
    final static Map instances = new HashMap();

    private static RemoteCacheMonitor monitor;

    private int clients;

    // Contains instances of RemoteCacheNoWait managed by a RemoteCacheManager
    // instance.
    final Map caches = new HashMap();

    final String host;

    final int port;

    final String service;

    private IRemoteCacheAttributes irca;

    /**
     * Handle to the remote cache service; or a zombie handle if failed to connect.
     */
    private IRemoteCacheService remoteService;

    /**
     * Wrapper of the remote cache watch service; or wrapper of a zombie service if failed to
     * connect.
     */
    private RemoteCacheWatchRepairable remoteWatch;

    /**
     * The cache manager listeners will need to use to get a cache.
     */
    private ICompositeCacheManager cacheMgr;

    private String registry;

    /**
     * Constructs an instance to with the given remote connection parameters. If the connection
     * cannot be made, "zombie" services will be temporarily used until a successful re-connection
     * is made by the monitoring daemon.
     * <p>
     * @param host
     * @param port
     * @param service
     * @param cacheMgr
     */
    private RemoteCacheManager( String host, int port, String service, ICompositeCacheManager cacheMgr )
    {
        this.host = host;
        this.port = port;
        this.service = service;
        this.cacheMgr = cacheMgr;

        // register shutdown observer
        // TODO add the shutdown observable methods to the interface
        if ( this.cacheMgr instanceof CompositeCacheManager )
        {
            ( (CompositeCacheManager) this.cacheMgr ).registerShutdownObserver( this );
        }

        this.registry = "//" + host + ":" + port + "/" + service;
        if ( log.isDebugEnabled() )
        {
            log.debug( "looking up server " + registry );
        }
        try
        {
            Object obj = Naming.lookup( registry );
            if ( log.isDebugEnabled() )
            {
                log.debug( "server found" );
            }
            // Successful connection to the remote server.
            remoteService = (IRemoteCacheService) obj;
            if ( log.isDebugEnabled() )
            {
                log.debug( "remoteService = " + remoteService );
            }

            remoteWatch = new RemoteCacheWatchRepairable();
            remoteWatch.setCacheWatch( (IRemoteCacheObserver) obj );
        }
        catch ( Exception ex )
        {
            // Failed to connect to the remote server.
            // Configure this RemoteCacheManager instance to use the "zombie"
            // services.
            log.error( "Problem finding server at [" + registry + "]", ex );
            remoteService = new ZombieRemoteCacheService();
            remoteWatch = new RemoteCacheWatchRepairable();
            remoteWatch.setCacheWatch( new ZombieRemoteCacheWatch() );
            // Notify the cache monitor about the error, and kick off the
            // recovery process.
            RemoteCacheMonitor.getInstance().notifyError();
        }
    }

    /**
     * Gets the defaultCattr attribute of the RemoteCacheManager object.
     * <p>
     * @return The defaultCattr value
     */
    public IRemoteCacheAttributes getDefaultCattr()
    {
        return this.irca;
    }

    /**
     * Adds the remote cache listener to the underlying cache-watch service.
     * <p>
     * @param cattr The feature to be added to the RemoteCacheListener attribute
     * @param listener The feature to be added to the RemoteCacheListener attribute
     * @throws IOException
     */
    public void addRemoteCacheListener( IRemoteCacheAttributes cattr, IRemoteCacheListener listener )
        throws IOException
    {
        if ( cattr.isReceive() )
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "The remote cache is configured to receive events from the remote server.  "
                    + "We will register a listener." );
            }

            synchronized ( caches )
            {
                remoteWatch.addCacheListener( cattr.getCacheName(), listener );
            }
        }
        else
        {
            if ( log.isInfoEnabled() )
            {
                log.info( "The remote cache is configured to NOT receive events from the remote server.  "
                    + "We will NOT register a listener." );
            }
        }
        return;
    }

    /**
     * Removes a listener. When the primary recovers the failover must deregister itself for a
     * region. The failover runner will call this method to de-register. We do not want to dergister
     * all listeners to a remote server, in case a failover is a primary of another region. Having
     * one regions failover act as another servers primary is not currently supported.
     * <p>
     * @param cattr
     * @param listener
     * @throws IOException
     */
    public void removeRemoteCacheListener( IRemoteCacheAttributes cattr, IRemoteCacheListener listener )
        throws IOException
    {
        synchronized ( caches )
        {
            remoteWatch.removeCacheListener( cattr.getCacheName(), listener );
        }
        return;
    }

    /**
     * Stops a listener. This is used to deregister a failover after primary reconnection.
     * <p>
     * @param cattr
     * @throws IOException
     */
    public void removeRemoteCacheListener( IRemoteCacheAttributes cattr )
        throws IOException
    {
        synchronized ( caches )
        {
            RemoteCacheNoWait cache = (RemoteCacheNoWait) caches.get( cattr.getCacheName() );
            if ( cache != null )
            {
                IRemoteCacheClient rc = cache.getRemoteCache();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Found cache for[ " + cattr.getCacheName() + "], deregistering listener." );
                }
                // could also store the listener for a server in the manager.
                IRemoteCacheListener listener = rc.getListener();
                remoteWatch.removeCacheListener( cattr.getCacheName(), listener );
            }
            else
            {
                if ( cattr.isReceive() )
                {
                    log.warn( "Trying to deregister Cache Listener that was never registered." );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Since the remote cache is configured to not receive, "
                            + "there is no listener to deregister." );
                    }
                }
            }
        }
        return;
    }

    /**
     * Stops a listener. This is used to deregister a failover after primary reconnection.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    public void removeRemoteCacheListener( String cacheName )
        throws IOException
    {
        synchronized ( caches )
        {
            RemoteCacheNoWait cache = (RemoteCacheNoWait) caches.get( cacheName );
            if ( cache != null )
            {
                IRemoteCacheClient rc = cache.getRemoteCache();
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Found cache for [" + cacheName + "], deregistering listener." );
                }
                // could also store the listener for a server in the manager.
                IRemoteCacheListener listener = rc.getListener();
                remoteWatch.removeCacheListener( cacheName, listener );
            }
        }
        return;
    }

    /**
     * Returns an instance of RemoteCacheManager for the given connection parameters.
     * <p>
     * Host and Port uniquely identify a manager instance.
     * <p>
     * Also starts up the monitoring daemon, if not already started.
     * <p>
     * If the connection cannot be established, zombie objects will be used for future recovery
     * purposes.
     * <p>
     * @param cattr
     * @param cacheMgr
     * @return The instance value
     * @parma port port of the registry.
     */
    public static RemoteCacheManager getInstance( IRemoteCacheAttributes cattr, ICompositeCacheManager cacheMgr )
    {
        String host = cattr.getRemoteHost();
        int port = cattr.getRemotePort();
        String service = cattr.getRemoteServiceName();
        if ( host == null )
        {
            host = "";
        }
        if ( port < 1024 )
        {
            port = Registry.REGISTRY_PORT;
        }
        Location loc = new Location( host, port );

        RemoteCacheManager ins = (RemoteCacheManager) instances.get( loc );
        synchronized ( instances )
        {
            if ( ins == null )
            {
                ins = (RemoteCacheManager) instances.get( loc );
                if ( ins == null )
                {
                    // cahnge to use cattr and to set defaults
                    ins = new RemoteCacheManager( host, port, service, cacheMgr );
                    ins.irca = cattr;
                    instances.put( loc, ins );
                }
            }
        }

        ins.clients++;
        // Fires up the monitoring daemon.
        if ( monitor == null )
        {
            monitor = RemoteCacheMonitor.getInstance();
            // If the returned monitor is null, it means it's already started
            // elsewhere.
            if ( monitor != null )
            {
                Thread t = new Thread( monitor );
                t.setDaemon( true );
                t.start();
            }
        }
        return ins;
    }

    /**
     * Returns a remote cache for the given cache name.
     * <p>
     * @param cacheName
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        IRemoteCacheAttributes ca = (IRemoteCacheAttributes) irca.copy();
        ca.setCacheName( cacheName );
        return getCache( ca );
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
    public AuxiliaryCache getCache( IRemoteCacheAttributes cattr )
    {
        RemoteCacheNoWait c = null;

        synchronized ( caches )
        {
            c = (RemoteCacheNoWait) caches.get( cattr.getCacheName() );
            if ( c == null )
            {
                // create a listener first and pass it to the remotecache
                // sender.
                RemoteCacheListener listener = null;
                try
                {
                    listener = new RemoteCacheListener( cattr, cacheMgr );
                    addRemoteCacheListener( cattr, listener );
                }
                catch ( IOException ioe )
                {
                    log.error( ioe.getMessage() );
                }
                catch ( Exception e )
                {
                    log.error( e.getMessage() );
                }

                c = new RemoteCacheNoWait( new RemoteCache( cattr, remoteService, listener ) );
                caches.put( cattr.getCacheName(), c );
            }

            // might want to do some listener sanity checking here.
        }

        return c;
    }

    /**
     * Releases.
     * <p>
     * @param name
     * @throws IOException
     */
    public void freeCache( String name )
        throws IOException
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "freeCache [" + name + "]" );
        }
        ICache c = null;
        synchronized ( caches )
        {
            c = (ICache) caches.get( name );
        }
        if ( c != null )
        {
            this.removeRemoteCacheListener( name );
            c.dispose();
        }
    }

    /**
     * Gets the stats attribute of the RemoteCacheManager object
     * <p>
     * @return The stats value
     */
    public String getStats()
    {
        StringBuffer stats = new StringBuffer();
        Iterator allCaches = caches.values().iterator();
        while ( allCaches.hasNext() )
        {
            ICache c = (ICache) allCaches.next();
            if ( c != null )
            {
                stats.append( c.getCacheName() );
            }
        }
        return stats.toString();
    }

    /** Shutdown all. */
    public void release()
    {
        // Wait until called by the last client
        if ( --clients != 0 )
        {
            return;
        }
        synchronized ( caches )
        {
            Iterator allCaches = caches.values().iterator();
            while ( allCaches.hasNext() )
            {
                ICache c = (ICache) allCaches.next();
                if ( c != null )
                {
                    try
                    {
                        // c.dispose();
                        freeCache( c.getCacheName() );
                    }
                    catch ( IOException ex )
                    {
                        log.error( "Problem in release.", ex );
                    }
                }
            }
        }
    }

    /**
     * Fixes up all the caches managed by this cache manager.
     * <p>
     * @param remoteService
     * @param remoteWatch
     */
    public void fixCaches( IRemoteCacheService remoteService, IRemoteCacheObserver remoteWatch )
    {
        synchronized ( caches )
        {
            this.remoteService = remoteService;
            this.remoteWatch.setCacheWatch( remoteWatch );
            for ( Iterator en = caches.values().iterator(); en.hasNext(); )
            {
                RemoteCacheNoWait cache = (RemoteCacheNoWait) en.next();
                cache.fixCache( this.remoteService );
            }
        }
    }

    /**
     * Gets the cacheType attribute of the RemoteCacheManager object
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return REMOTE_CACHE;
    }

    /**
     * Location of the RMI registry.
     */
    private final static class Location
    {
        /** Description of the Field */
        public final String host;

        /** Description of the Field */
        public final int port;

        /**
         * Constructor for the Location object
         * <p>
         * @param host
         * @param port
         */
        public Location( String host, int port )
        {
            this.host = host;
            this.port = port;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            if ( obj == null || !( obj instanceof Location ) )
            {
                return false;
            }
            Location l = (Location) obj;
            if ( this.host == null && l.host != null )
            {
                return false;
            }
            return host.equals( l.host ) && port == l.port;
        }

        /**
         * @return int
         */
        public int hashCode()
        {
            return host == null ? port : host.hashCode() ^ port;
        }
    }

    /**
     * Shutdown callback from composite cache manager.
     * <p>
     * (non-Javadoc)
     * @see org.apache.jcs.engine.behavior.IShutdownObserver#shutdown()
     */
    public void shutdown()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Observed shutdown request." );
        }
        release();
    }

}
