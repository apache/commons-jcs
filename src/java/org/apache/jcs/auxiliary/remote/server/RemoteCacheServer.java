package org.apache.jcs.auxiliary.remote.server;

import java.io.IOException;
import java.io.Serializable;

import java.rmi.NotBoundException;
import java.rmi.registry.Registry;

import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheServiceAdmin;
import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;

import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.CacheListeners;
import org.apache.jcs.engine.CacheConstants;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.behavior.ICompositeCache;

import org.apache.jcs.engine.control.CompositeCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides remote cache services.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class RemoteCacheServer
     extends UnicastRemoteObject
     implements IRemoteCacheService, IRemoteCacheObserver,
    IRemoteCacheServiceAdmin, Unreferenced
{
    private final static Log log =
        LogFactory.getLog( RemoteCacheServer.class );

    /** Description of the Field */
    protected final static boolean timing = true;

    //true;
    /** Description of the Field */
    public String className;

    private int puts = 0;

    // Maps cache name to CacheListeners object.
    // association of listeners (regions).
    private final Hashtable cacheListenersMap = new Hashtable();
    private final Hashtable clusterListenersMap = new Hashtable();
    private CompositeCacheManager cacheManager;

    // relates listener id with a type
    private final Hashtable idTypeMap = new Hashtable();

    //private transient int listenerId = 0;
    private int[] listenerId = new int[1];

    /** Description of the Field */
    protected IRemoteCacheServerAttributes rcsa;


    /**
     * Constructor for the RemoteCacheServer object
     *
     * @param rcsa
     * @exception IOException
     * @exception NotBoundException
     */
    protected RemoteCacheServer( IRemoteCacheServerAttributes rcsa )
        throws IOException, NotBoundException
    {
        super( rcsa.getServicePort() );
        this.rcsa = rcsa;
        init( rcsa.getConfigFileName() );
    }


    /** RMI Cache Server. */
    protected void init( String prop )
        throws IOException, NotBoundException
    {

        String s = this.getClass().getName();
        int idx = s.lastIndexOf( "." );
        this.className = s.substring( idx + 1 );

        cacheManager = createCacheManager( prop );

        // cacheManager would have created a number of ICache objects.
        // Use these objects to set up the cacheListenersMap.
        String[] list = cacheManager.getCacheNames();
        for ( int i = 0; i < list.length; i++ )
        {
            String name = list[i];
            cacheListenersMap.put( name, new CacheListeners( cacheManager.getCache( name ) ) );
            //cacheListenersMap.put(name, new CacheListeners(cacheManager.getCache(name)));
        }

    }


    /**
     * Subclass can overrdie this method to create the specific cache manager.
     */
    protected CompositeCacheManager createCacheManager( String prop )
    {
        CompositeCacheManager hub = CompositeCacheManager.getUnconfiguredInstance();

        if ( prop == null )
        {
            hub.configure( "/remote.cache.properties" );
        }
        else
        {
            hub.configure( prop );
        }

        return hub;
    }


    /**
     * Returns the cache lsitener for the specified cache. Creates the cache and
     * the cache descriptor if they do not already exist.
     *
     * @return The cacheListeners value
     */
    private CacheListeners getCacheListeners( String cacheName )
        throws IOException,
        NotBoundException
    {
        CacheListeners cacheListeners = ( CacheListeners ) cacheListenersMap.get( cacheName );
        if ( cacheListeners == null )
        {
            synchronized ( cacheListenersMap )
            {
                cacheListeners = ( CacheListeners ) cacheListenersMap.get( cacheName );
                if ( cacheListeners == null )
                {
                    // NEED TO CONVERT TO USE THE FACTORY ND GET A FACADE?  No it is the hub
                    cacheListeners = new CacheListeners( cacheManager.getCache( cacheName ) );
                    cacheListenersMap.put( cacheName, cacheListeners );
                }
            }
        }
        return cacheListeners;
    }


    // may be able to remove this
    /**
     * Gets the clusterListeners attribute of the RemoteCacheServer object
     *
     * @return The clusterListeners value
     */
    private CacheListeners getClusterListeners( String cacheName )
        throws IOException,
        NotBoundException
    {
        CacheListeners cacheListeners = ( CacheListeners ) clusterListenersMap.get( cacheName );
        if ( cacheListeners == null )
        {
            synchronized ( clusterListenersMap )
            {
                cacheListeners = ( CacheListeners ) clusterListenersMap.get( cacheName );
                if ( cacheListeners == null )
                {
                    cacheListeners = new CacheListeners( cacheManager.getCache( cacheName ) );
                    clusterListenersMap.put( cacheName, cacheListeners );
                }
            }
        }
        return cacheListeners;
    }


    /////////////////////// Implements the ICacheService interface. //////////////////
    /**
     * Puts a cache bean to the remote cache and notifies all listeners which
     * <br>
     *
     * <ol>
     *   <li> have a different host than the originating host;
     *   <li> are currently subscribed to the related cache.
     * </ol>
     *
     */
    public void put( ICacheElement item )
        throws IOException
    {
        update( item );
    }


    /** Description of the Method */
    public void update( ICacheElement item )
        throws IOException
    {
        update( item, ( byte ) 0 );
    }


    /** Description of the Method */
    public void update( ICacheElement item, byte requesterId )
        throws IOException
    {

        long start = 0;
        if ( timing )
        {
            start = System.currentTimeMillis();
        }

        if ( log.isDebugEnabled() )
        {
            puts++;
            if ( puts % 100 == 0 )
            {
                p1( "puts = " + puts );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "in update, put " + item.getKey() + " in " + item.getCacheName() );
        }

        try
        {
            CacheListeners cacheDesc = getCacheListeners( item.getCacheName() );
            Object val = item.getVal();

            Integer remoteTypeL = ( Integer ) idTypeMap.get( new Byte( requesterId ) );
            boolean fromCluster = false;
            if ( remoteTypeL.intValue() == IRemoteCacheAttributes.CLUSTER )
            {
                fromCluster = true;
            }
            // ordered cache item update and notification.
            synchronized ( cacheDesc )
            {
                try
                {
                    ICompositeCache c = ( ICompositeCache ) cacheDesc.cache;

                    //TODO; make this a bit less of a hack
                    // If the source of this request was from a cluster, then
                    // update the remote caches.  COnsider it a local update.
                    // This requires that two local caches not be connected to
                    // two clustered remote caches. The failover runner will
                    // have to make sure of this.  ALos, the local cache needs
                    // avoid updating this source.  Will need to pass the source
                    // id somehow.  The remote cache should udate all local caches
                    // but not update the cluster source.  Cluster remote caches
                    // should only be updated by the server and not the RemoteCache.
                    // PUT LOGIC IN REMOTE CACHE
                    // WILL TRY UPDATING REMOTES
                    if ( fromCluster )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "not updating clusters **************************************" );
                        }

                        c.localUpdate( item );
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "updating clusters **************************************" );
                        }

                        c.update( item );
                    }
                }
                catch ( Exception oee )
                {
                }

                // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED
                ICompositeCache cache = ( ICompositeCache ) cacheDesc.cache;
                if ( !fromCluster || ( fromCluster && rcsa.getLocalClusterConsistency() ) )
                {

                    ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                    for ( int i = 0; i < qlist.length; i++ )
                    {
                        qlist[i].addPutEvent( item );
                    }

                }

            }
        }
        catch ( NotBoundException ex )
        {
            ex.printStackTrace( System.out );
            throw new IllegalStateException( ex.getMessage() );
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( timing )
        {
            long end = System.currentTimeMillis();
            this.p1( "put took " + String.valueOf( end - start ) + " ms." );
        }

        return;
    }


    /**
     * Gets the eventQList attribute of the RemoteCacheServer object
     *
     * @return The eventQList value
     */
    private ICacheEventQueue[] getEventQList( CacheListeners cacheListeners, byte requesterId )
    {
        ICacheEventQueue[] list = null;
        synchronized ( cacheListeners.eventQMap )
        {
            list = ( ICacheEventQueue[] ) cacheListeners.eventQMap.values().toArray( new ICacheEventQueue[0] );
        }
        int count = 0;
        // Set those not qualified to null;  Count those qualified.
        for ( int i = 0; i < list.length; i++ )
        {
            ICacheEventQueue q = list[i];
            if ( q.isAlive() && q.getListenerId() != requesterId )
            {
                count++;
            }
            else
            {
                list[i] = null;
            }
        }
        if ( count == list.length )
        {
            // All qualified.
            return list;
        }
        // Returns only the qualified.
        ICacheEventQueue[] qq = new ICacheEventQueue[count];
        count = 0;
        for ( int i = 0; i < list.length; i++ )
        {
            if ( list[i] != null )
            {
                qq[count++] = list[i];
            }
        }
        return qq;
    }


    /**
     * Returns a cache value from the specified remote cache; or null if the
     * cache or key does not exist.
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "get " + key + " from cache " + cacheName );
        }

//        Integer remoteTypeL = ( Integer ) idTypeMap.get( new Byte( requesterId ) );
        boolean fromCluster = false;
//        if ( remoteTypeL.intValue() == IRemoteCacheAttributes.CLUSTER )
//        {
//            fromCluster = true;
//        }

        CacheListeners cacheDesc = null;
        try
        {
            cacheDesc = getCacheListeners( cacheName );
        }
        catch ( Exception e )
        {
            log.error( e );
        }

        if ( cacheDesc == null )
        {
            return null;
        }
        else
        {
            ICompositeCache c = ( ICompositeCache ) cacheDesc.cache;

            return c.localGet( key );
        }
    }


    /** Removes the given key from the specified remote cache. */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, ( byte ) 0 );
    }


    /** Description of the Method */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "remove " + key + " from cache " + cacheName );
        }
        CacheListeners cacheDesc = ( CacheListeners ) cacheListenersMap.get( cacheName );

        Integer remoteTypeL = ( Integer ) idTypeMap.get( new Byte( requesterId ) );
        boolean fromCluster = false;
        if ( remoteTypeL.intValue() == IRemoteCacheAttributes.CLUSTER )
        {
            fromCluster = true;
        }

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and notification.
            synchronized ( cacheDesc )
            {

                boolean removeSuccess = false;

                // No need to notify if it was not cached.
                ICompositeCache c = ( ICompositeCache ) cacheDesc.cache;

                if ( fromCluster )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "not updating clusters **************************************" );
                    }
                    removeSuccess = c.localRemove( key );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "updating clusters **************************************" );
                    }
                    removeSuccess = c.remove( key );
                }

                if ( removeSuccess )
                {

                    // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                    // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED
                    ICompositeCache cache = ( ICompositeCache ) cacheDesc.cache;
                    if ( !fromCluster || ( fromCluster && rcsa.getLocalClusterConsistency() ) )
                    {

                        ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                        for ( int i = 0; i < qlist.length; i++ )
                        {
                            qlist[i].addRemoveEvent( key );
                        }
                    }

                }
            }
        }
        return;
    }


    /** Remove all keys from the sepcified remote cache. */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, ( byte ) 0 );
    }


    /** Description of the Method */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException
    {
        CacheListeners cacheDesc = ( CacheListeners ) cacheListenersMap.get( cacheName );

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and notification.
            synchronized ( cacheDesc )
            {
                ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                for ( int i = 0; i < qlist.length; i++ )
                {
                    qlist[i].addRemoveAllEvent();
                }
                cacheDesc.cache.removeAll();
            }
        }
        return;
    }


    /** Frees the specified remote cache. */
    public void dispose( String cacheName )
        throws IOException
    {
        dispose( cacheName, ( byte ) 0 );
    }


    /** Description of the Method */
    public void dispose( String cacheName, byte requesterId )
        throws IOException
    {
        CacheListeners cacheDesc = ( CacheListeners ) cacheListenersMap.get( cacheName );

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered free-cache-op and notification.
            synchronized ( cacheDesc )
            {
                ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                for ( int i = 0; i < qlist.length; i++ )
                {
                    qlist[i].addDisposeEvent();
                }
                cacheManager.freeCache( cacheName );
            }
        }
        return;
    }


    /** Frees all remote caches. */
    public void release()
        throws IOException
    {
        synchronized ( cacheListenersMap )
        {
            for ( Enumeration en = cacheListenersMap.elements(); en.hasMoreElements();  )
            {
                CacheListeners cacheDesc = ( CacheListeners ) en.nextElement();
                ICacheEventQueue[] qlist = getEventQList( cacheDesc, ( byte ) 0 );

                for ( int i = 0; i < qlist.length; i++ )
                {
                    qlist[i].addDisposeEvent();
                }
            }
            cacheManager.release();
        }
        return;
    }


    // modify to use unique name
    /**
     * Gets the requester attribute of the RemoteCacheServer object
     *
     * @return The requester value
     */
    private String getRequester()
    {
        try
        {
            return getClientHost();
        }
        catch ( ServerNotActiveException ex )
        {
            // impossible case.
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
    }


    /////////////////////// Implements the ICacheObserver interface. //////////////////
    /** Description of the Method */
    private static void cleanupEventQMap( Map eventQMap )
    {
        synchronized ( eventQMap )
        {
            for ( Iterator itr = eventQMap.entrySet().iterator(); itr.hasNext();  )
            {
                Map.Entry e = ( Map.Entry ) itr.next();
                ICacheEventQueue q = ( ICacheEventQueue ) e.getValue();

                if ( !q.isAlive() )
                {
                    itr.remove();
                    p1( "Cache event queue " + q + " dead and removed from cache server." );
                }
            }
        }
    }


    /**
     * Subscribes to the specified remote cache.
     *
     * @param cacheName the specified remote cache.
     * @param listener object to notify for cache changes. must be synchronized
     *      since there are remote calls involved.
     */
    public void addCacheListener( String cacheName, ICacheListener listener )
        throws IOException
    {
        if ( cacheName == null || listener == null )
        {
            throw new IllegalArgumentException( "cacheName and listener must not be null" );
        }
        try
        {
            CacheListeners cacheDesc;
            //if ( cacheName.equals("SYSTEM_CLUSTER") || listener instanceof org.apache.jcs.auxiliary.remote.server.RemoteCacheServerListener ) {
            IRemoteCacheListener ircl = ( IRemoteCacheListener ) listener;
            int remoteType = ircl.getRemoteType();
            if ( remoteType == IRemoteCacheAttributes.CLUSTER )
            {
                log.debug( "adding cluster listener" );
                cacheDesc = getClusterListeners( cacheName );
            }
            else
            {
                log.debug( "adding normal listener" );
                cacheDesc = getCacheListeners( cacheName );
            }
            Map eventQMap = cacheDesc.eventQMap;
            cleanupEventQMap( eventQMap );

            synchronized ( ICacheListener.class )
            {
                byte id = 0;
                try
                {
                    id = listener.getListenerId();
                    if ( id == 0 )
                    {
                        // must start at one so the next gets recognized
                        byte listenerIdB = nextListenerId();
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "listener id=" + ( listenerIdB & 0xff ) + " addded for cache " + cacheName );
                        }
                        listener.setListenerId( listenerIdB );
                        id = listenerIdB;
                        // in case it needs synchronization
                        p1( "added new vm listener " + listenerIdB );

                        // relate the type to an id
                        this.idTypeMap.put( new Byte( listenerIdB ), new Integer( remoteType ) );

                    }
                    else
                    {
                        p1( "added existing vm listener " + id );
                    }
                }
                catch ( IOException ioe )
                {
                }
                //eventQMap.put(listener, new CacheEventQueue(listener, getRequester(), cacheName));
                eventQMap.put( listener, new CacheEventQueue( listener, id, cacheName ) );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "****** Cache " + cacheName + "'s listener size=" + cacheDesc.eventQMap.size() );
                }
            }
            // end sync
        }
        catch ( NotBoundException ex )
        {
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
        return;
    }


    /**
     * Subscribes to all remote caches.
     *
     * @param listener The feature to be added to the CacheListener attribute
     */
    public void addCacheListener( ICacheListener listener )
        throws IOException
    {
        for ( Enumeration en = cacheListenersMap.keys(); en.hasMoreElements();  )
        {
            String cacheName = ( String ) en.nextElement();
            addCacheListener( cacheName, listener );

            if ( log.isDebugEnabled() )
            {
                log.debug( "adding listener for cache " + cacheName );
            }
        }
        return;
    }


    /** Unsubscribes from the specified remote cache. */
    public void removeCacheListener( String cacheName, ICacheListener listener )
        throws IOException
    {
        try
        {
            CacheListeners cacheDesc = getCacheListeners( cacheName );
            Map eventQMap = cacheDesc.eventQMap;
            cleanupEventQMap( eventQMap );
            ICacheEventQueue q = ( ICacheEventQueue ) eventQMap.remove( listener );

            if ( q != null )
            {
                q.destroy();
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "****** Cache " + cacheName + "'s listener size=" + cacheDesc.eventQMap.size() );
            }
        }
        catch ( NotBoundException ex )
        {
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
    }


    /** Unsubscribes from all remote caches. */
    public void removeCacheListener( ICacheListener listener )
        throws IOException
    {
        for ( Enumeration en = cacheListenersMap.keys(); en.hasMoreElements();  )
        {
            String cacheName = ( String ) en.nextElement();
            removeCacheListener( cacheName, listener );

            if ( log.isDebugEnabled() )
            {
                log.debug( "removing listener for cache " + cacheName );
            }
        }
        return;
    }

    /////////////////////// Implements the ICacheServiceAdmin interface. //////////////////

    /** Description of the Method */
    public void shutdown()
        throws IOException
    {
        RemoteCacheServerFactory.shutdownImpl( "", Registry.REGISTRY_PORT );
    }


    /** Description of the Method */
    public void shutdown( String host, int port )
        throws IOException
    {
        log.debug( "received shutdown request" );
        RemoteCacheServerFactory.shutdownImpl( host, port );
    }

    /////////////////////// Implements the Unreferenced interface. //////////////////

    /**
     * Called by the RMI runtime sometime after the runtime determines that the
     * reference list, the list of clients referencing the remote object,
     * becomes empty.
     */
    // TODO: test out the DGC.
    public void unreferenced()
    {
        log.debug( "*** Warning: Server now unreferenced and subject to GC. ***" );
    }

    /** Returns the next generated listener id [0,255]. */
    private byte nextListenerId()
    {
        int id = 0;
        if ( listenerId[0] == 255 )
        {
            synchronized ( listenerId )
            {
                id = listenerId[0];
                listenerId[0] = 0;
                // TODO: record & check if the generated id is currently being
                // used by a valid listener.  Currently if the id wraps after 255,
                // we just assume it won't collide with an existing listener who is live.
            }
        }
        else
        {
            synchronized ( listenerId )
            {
                id = ++listenerId[0];
            }
        }
        return ( byte ) ( id & 0xff );
    }


    /** Description of the Method */
    private static void p1( String s )
    {
        System.out.println( "RemoteCacheServer:" + s + " >" + Thread.currentThread().getName() );
    }
}
