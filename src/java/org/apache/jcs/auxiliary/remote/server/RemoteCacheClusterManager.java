package org.apache.jcs.auxiliary.remote.server;


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


import java.io.IOException;

import java.rmi.Naming;
import java.rmi.registry.Registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jcs.auxiliary.remote.RemoteCache;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWait;
import org.apache.jcs.auxiliary.remote.RemoteCacheWatchRepairable;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheService;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheWatch;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class RemoteCacheClusterManager implements ICacheManager
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheClusterManager.class );

    // Contains mappings of Location instance to RemoteCacheManager instance.
    final static Map instances = new HashMap();
    private static RemoteCacheClusterMonitor monitor;

    private int clients;

    // Contains instances of RemoteCacheNoWait managed by an RemoteCacheManager instance.
    final Map caches = new HashMap();

    final String host;
    final int port;
    final String service;

    private IRemoteCacheAttributes irca;

    /**
     * Handle to the remote cache service; or a zombie handle if failed to
     * connect.
     */
    private IRemoteCacheService remoteService;

    /**
     * Wrapper of the remote cache watch service; or wrapper of a zombie service
     * if failed to connect.
     */
    private RemoteCacheWatchRepairable remoteWatch;


    /**
     * Constructs an instance to with the given remote connection parameters. If
     * the connection cannot be made, "zombie" services will be temporarily used
     * until a successful re-connection is made by the monitoring daemon.
     *
     * @param host
     * @param port
     * @param service
     */
    private RemoteCacheClusterManager( String host, int port, String service )
    {

        this.host = host;
        this.port = port;
        this.service = service;

        String registry = "//" + host + ":" + port + "/" + service;
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
            remoteService = ( IRemoteCacheService ) obj;
            remoteWatch = new RemoteCacheWatchRepairable();
            remoteWatch.setCacheWatch( ( IRemoteCacheObserver ) obj );
        }
        catch ( Exception ex )
        {
            // Failed to connect to the remote server.
            // Configure this RemoteCacheManager instance to use the "zombie" services.
            log.error( ex.getMessage() );
            remoteService = new ZombieRemoteCacheService();
            remoteWatch = new RemoteCacheWatchRepairable();
            remoteWatch.setCacheWatch( new ZombieRemoteCacheWatch() );
            // Notify the cache monitor about the error, and kick off the recovery process.
            RemoteCacheClusterMonitor.getInstance().notifyError();
        }

    }


    /**
     * Gets the defaultCattr attribute of the RemoteCacheClusterManager object
     *
     * @return The defaultCattr value
     */
    public IRemoteCacheAttributes getDefaultCattr()
    {
        return this.irca;
    }


    /**
     * Adds the remote cache listener to the underlying cache-watch service.
     *
     * @param cattr The feature to be added to the RemoteCacheListener attribute
     * @param listener The feature to be added to the RemoteCacheListener
     *      attribute
     */
    public void addRemoteCacheListener( IRemoteCacheAttributes cattr, IRemoteCacheListener listener )
        throws IOException
    {
        synchronized ( caches )
        {
            remoteWatch.addCacheListener( cattr.getCacheName(), listener );
        }
        return;
    }


    /**
     * Returns an instance of RemoteCacheManager for the given connection
     * parameters. Also starts up the monitoring daemon, if not already started.
     * If the connection cannot be established, zombie objects will be used for
     * future recovery purposes.
     *
     * @return The instance value
     * @parma port port of the registry.
     */
    public static RemoteCacheClusterManager getInstance( IRemoteCacheAttributes cattr )
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

        RemoteCacheClusterManager ins = ( RemoteCacheClusterManager ) instances.get( loc );
        if ( ins == null )
        {
            synchronized ( instances )
            {
                ins = ( RemoteCacheClusterManager ) instances.get( loc );
                if ( ins == null )
                {
                    // cahnge to use cattr and to set defaults
                    ins = new RemoteCacheClusterManager( host, port, service );
                    ins.irca = cattr;
                    instances.put( loc, ins );
                }
            }

            // create a listener int he absence of specific caches to listen, so the
            // cluster will be contacted from a general source, not on a per cache basis.
            try
            {
                cattr.setCacheName( "server" );
                ins.addRemoteCacheListener( cattr, RemoteCacheServerListener.getInstance( cattr ) );
            }
            catch ( IOException ioe )
            {
                log.error( ioe );
            }
            catch ( Exception e )
            {
                log.error( e );
            }

        }

        ins.clients++;
        // Fires up the monitoring daemon.
        if ( monitor == null )
        {
            monitor = RemoteCacheClusterMonitor.getInstance();
            // If the returned monitor is null, it means it's already started elsewhere.
            if ( monitor != null )
            {
                Thread t = new Thread( monitor );
                t.setDaemon( true );
                t.start();
            }
        }

        /*
         * moved to factory
         * try {
         * / create one cache to make imported framework function
         * cattr.setCacheName( "SYSTEM_CLUSTER");
         * ins.addRemoteCacheListener( cattr, RemoteCacheServerListener.getInstance( cattr ) );
         * } catch( IOException ioe ) {
         * ins.log.error( ioe );
         * } catch( Exception e ) {
         * ins.log.error( e );
         * }
         */
        return ins;
    }


    /**
     * Returns a remote cache for the given cache name.
     *
     * @return The cache value
     */
    /**
     * Returns a remote cache for the given cache name.
     *
     * @return The cache value
     */
    public ICache getCache( String cacheName )
    {
        IRemoteCacheAttributes ca = ( IRemoteCacheAttributes ) irca.copy();
        ca.setCacheName( cacheName );
        return getCache( ca );
    }


    /**
     * Gets the cache attribute of the RemoteCacheClusterManager object
     *
     * @return The cache value
     */
    public ICache getCache( IRemoteCacheAttributes cattr )
    {
        RemoteCacheNoWait c = null;
        synchronized ( caches )
        {
            c = ( RemoteCacheNoWait ) caches.get( cattr.getCacheName() );
            if ( c == null )
            {
                c = new RemoteCacheNoWait( new RemoteCache( cattr, remoteService ) );
                caches.put( cattr.getCacheName(), c );
            }
        }

        //if ( irca.getUseRemote() ) {
        try
        {
            addRemoteCacheListener( cattr, RemoteCacheServerListener.getInstance( cattr ) );
        }
        catch ( IOException ioe )
        {
            log.error( ioe );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        //}
        return c;
    }


    /** Description of the Method */
    public void freeCache( String name )
        throws IOException
    {
        ICache c = null;

        synchronized ( caches )
        {
            c = ( ICache ) caches.get( name );
        }
        if ( c != null )
        {
            c.dispose();
        }
    }

    /** Description of the Method */
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
                ICache c = ( ICache ) allCaches.next();
                if ( c != null )
                {
                    try
                    {
                        c.dispose();
                    }
                    catch ( IOException ex )
                    {
                        log.error( ex );
                    }
                }
            }
        }
    }
    //end release()

    /** Fixes up all the caches managed by this cache manager. */
    public void fixCaches( IRemoteCacheService remoteService, IRemoteCacheObserver remoteWatch )
    {
        synchronized ( caches )
        {
            this.remoteService = remoteService;
            this.remoteWatch.setCacheWatch( remoteWatch );
            for ( Iterator en = caches.values().iterator(); en.hasNext();  )
            {
                RemoteCacheNoWait cache = ( RemoteCacheNoWait ) en.next();
                cache.fixCache( this.remoteService );
            }
        }
    }

    /**
     * Gets the cacheType attribute of the RemoteCacheClusterManager object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return REMOTE_CACHE;
    }


    /**
     * Location of the RMI registry.
     *
     */
    private final static class Location
    {
        /** Description of the Field */
        public final String host;
        /** Description of the Field */
        public final int port;


        /**
         * Constructor for the Location object
         *
         * @param host
         * @param port
         */
        public Location( String host, int port )
        {
            this.host = host;
            this.port = port;
        }


        /** Description of the Method */
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
            Location l = ( Location ) obj;
            if ( this.host == null && l.host != null )
            {
                return false;
            }
            return host.equals( l.host ) && port == l.port;
        }


        /** Description of the Method */
        public int hashCode()
        {
            return host == null ? port : host.hashCode() ^ port;
        }

    }

}

