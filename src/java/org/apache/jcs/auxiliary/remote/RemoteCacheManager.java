package org.apache.jcs.auxiliary.remote;

import java.io.IOException;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.Registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jcs.engine.behavior.ICache;

import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheManager;

import org.apache.jcs.auxiliary.remote.RemoteCache;
import org.apache.jcs.auxiliary.remote.RemoteCacheMonitor;
import org.apache.jcs.auxiliary.remote.RemoteCacheNoWait;
import org.apache.jcs.auxiliary.remote.RemoteCacheWatchRepairable;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheService;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheWatch;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.auxiliary.remote.group.RemoteGroupCacheListener;

import org.apache.jcs.engine.CacheWatchRepairable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An instance of RemoteCacheManager corresponds to one remote connection of a
 * specific host and port. All RemoteCacheManager instances are monitored by the
 * singleton RemoteCacheMonitor monitoring daemon for error detection and
 * recovery.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheManager implements IAuxiliaryCacheManager
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheManager.class );

    // Contains mappings of Location instance to RemoteCacheManager instance.
    final static Map instances = new HashMap();
    private static RemoteCacheMonitor monitor;

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
    private RemoteCacheManager( String host, int port, String service )
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
            RemoteCacheMonitor.getInstance().notifyError();
        }
    }


    /**
     * Gets the defaultCattr attribute of the RemoteCacheManager object
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
    public static RemoteCacheManager getInstance( IRemoteCacheAttributes cattr )
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

        RemoteCacheManager ins = ( RemoteCacheManager ) instances.get( loc );
        if ( ins == null )
        {
            synchronized ( instances )
            {
                ins = ( RemoteCacheManager ) instances.get( loc );
                if ( ins == null )
                {
                    // cahnge to use cattr and to set defaults
                    ins = new RemoteCacheManager( host, port, service );
                    ins.irca = cattr;
                    instances.put( loc, ins );
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            ins.log.debug( "Manager stats : " + ins.getStats() + "-- in getInstance()" );
        }
        ins.clients++;
        // Fires up the monitoring daemon.
        if ( monitor == null )
        {
            monitor = RemoteCacheMonitor.getInstance();
            // If the returned monitor is null, it means it's already started elsewhere.
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
     * Gets the cache attribute of the RemoteCacheManager object
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
        if ( log.isDebugEnabled() )
        {
            log.debug( "Manager stats : " + getStats() );
        }
        //if ( irca.getUseRemote() ) {
        try
        {
            // Remote cache manager can handle this by gettign the type formt he listener
            //if ( cattr.getRemoteType() == cattr.CLUSTER ) {
            //  addRemoteCacheListener( cattr, RemoteGroupCacheListener.getInstance( cattr ) );
            //} else
            //if ( cattr.getRemoteType() == cattr.LOCAL ) {
            addRemoteCacheListener( cattr, RemoteGroupCacheListener.getInstance( cattr ) );
            //}
        }
        catch ( IOException ioe )
        {
            log.error( ioe.getMessage() );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
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


    // Don't care if there is a concurrency failure ?
    /**
     * Gets the stats attribute of the RemoteCacheManager object
     *
     * @return The stats value
     */
    public String getStats()
    {
        StringBuffer stats = new StringBuffer();
        Iterator allCaches = caches.values().iterator();
        while ( allCaches.hasNext() )
        {
            ICache c = ( ICache ) allCaches.next();
            if ( c != null )
            {
                stats.append( "<br>&nbsp;&nbsp;&nbsp;" + c.getStats() );
            }
        }
        return stats.toString();
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
     * Gets the cacheType attribute of the RemoteCacheManager object
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
     * @author asmuts
     * @created January 15, 2002
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

