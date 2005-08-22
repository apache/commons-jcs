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
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheAttributes;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheObserver;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheServiceAdmin;
import org.apache.jcs.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;
import org.apache.jcs.engine.CacheEventQueueFactory;
import org.apache.jcs.engine.CacheListeners;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * This class provides remote cache services. The remote cache server propagates
 * events from local caches to other local caches. It can also store cached
 * data, making it available to new clients.
 * 
 * Remote cache servers can be clustered. If the cache used by this remote cache
 * is configured to use a remote cache of type cluster, the two remote caches
 * will communicate with each other. Remote and put requests can be sent from
 * one remote to another. If they are configured to broadcast such event to
 * their client, then remove an puts can be sent to all locals in the cluster.
 * However, get requests are not made between clustered servers. You can setup
 * several clients to use one remote server and several to use another. The get
 * locad will be distributed between the two servers. Since caches are usually
 * high get and low put, this should allow you to scale.
 *  
 */
public class RemoteCacheServer
    extends UnicastRemoteObject
    implements IRemoteCacheService, IRemoteCacheObserver, IRemoteCacheServiceAdmin, Unreferenced
{
    private final static Log log = LogFactory.getLog( RemoteCacheServer.class );

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

    /**
     * Initialize the RMI Cache Server from a proeprties file.
     * 
     * @param prop
     * @throws IOException
     * @throws NotBoundException
     */
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
        }

    }

    /**
     * Subclass can overrdie this method to create the specific cache manager.
     * 
     * @param prop
     *            The anem of the configuration file.
     * @return The cache hub configured with this configuration file.
     */
    protected CompositeCacheManager createCacheManager( String prop )
    {
        CompositeCacheManager hub = CompositeCacheManager.getUnconfiguredInstance();

        if ( prop == null )
        {
            hub.configure( "/remote.cache.ccf" );
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
     * @param cacheName
     * @return The cacheListeners value
     * @throws IOException
     * @throws NotBoundException
     */
    private CacheListeners getCacheListeners( String cacheName )
        throws IOException, NotBoundException
    {
        CacheListeners cacheListeners = (CacheListeners) cacheListenersMap.get( cacheName );
        synchronized ( cacheListenersMap )
        {
            if ( cacheListeners == null )
            {
                cacheListeners = (CacheListeners) cacheListenersMap.get( cacheName );
                if ( cacheListeners == null )
                {
                    // NEED TO CONVERT TO USE THE FACTORY ND GET A FACADE? No it
                    // is the hub
                    cacheListeners = new CacheListeners( cacheManager.getCache( cacheName ) );
                    cacheListenersMap.put( cacheName, cacheListeners );
                }
            }
        }
        return cacheListeners;
    }

    /**
     * Gets the clusterListeners attribute of the RemoteCacheServer object.
     * 
     * @todo may be able to remove this
     * 
     * @param cacheName
     * @return The clusterListeners value
     * @throws IOException
     * @throws NotBoundException
     */
    private CacheListeners getClusterListeners( String cacheName )
        throws IOException, NotBoundException
    {
        CacheListeners cacheListeners = (CacheListeners) clusterListenersMap.get( cacheName );
        synchronized ( clusterListenersMap )
        {
            if ( cacheListeners == null )
            {
                cacheListeners = (CacheListeners) clusterListenersMap.get( cacheName );
                if ( cacheListeners == null )
                {
                    cacheListeners = new CacheListeners( cacheManager.getCache( cacheName ) );
                    clusterListenersMap.put( cacheName, cacheListeners );
                }
            }
        }
        return cacheListeners;
    }

    /////////////////////// Implements the ICacheService interface.
    // //////////////////
    /**
     * Puts a cache bean to the remote cache and notifies all listeners which
     * <br>
     * 
     * <ol>
     * <li>have a different listener id than the originating host;
     * <li>are currently subscribed to the related cache.
     * </ol>
     * 
     * @param item
     * @throws IOException
     *  
     */
    public void put( ICacheElement item )
        throws IOException
    {
        update( item );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheService#update(org.apache.jcs.engine.behavior.ICacheElement)
     */
    public void update( ICacheElement item )
        throws IOException
    {
        update( item, 0 );
    }

    /**
     * An update can come from either a local cache's remote auxiliary, or it
     * can come from a remote server. A remote server is considered a a source
     * of type cluster.
     * 
     * If the update came from a cluster, then we should tell the cache manager
     * that this was a remote put. This way, any lateral and remote auxiliaries
     * configured for the region will not be updated. This is basically how a
     * remote listener works when plugged into a local cache.
     * 
     * If the cluster is configured to keep local cluster consistency, then all
     * listeners will be updated. This allows cluster server A to update cluster
     * server B and then B to update its clients if it is told to keep local
     * cluster consistency. Otherwise, server A will update server B and B will
     * not tell its clients. If you cluster using lateral caches for instance,
     * this is how it will work. Updates to a cluster node, will never get to
     * the leavess. The remote cluster, with local cluster consistency, allows
     * you to update leaves. This basically allows you to have a failover remote
     * server.
     * 
     * Since currently a cluster will not try to get from other cluster servers,
     * you can scale a bit with a cluster configuration. Puts and removes will
     * be broadcasted to all clients, but the get load on a remote server can be
     * reduced.
     * 
     * @param item
     * @param requesterId
     * @throws IOException
     */
    public void update( ICacheElement item, long requesterId )
        throws IOException
    {

        long start = 0;
        if ( timing )
        {
            start = System.currentTimeMillis();
        }

        if ( log.isInfoEnabled() )
        {
            // not thread safe, but it doesn't ahve to be accurate
            puts++;
            if ( puts % 100 == 0 )
            {
                log.info( "puts = " + puts );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "in update, put " + item.getKey() + " in " + item.getCacheName() );
        }

        try
        {
            CacheListeners cacheDesc = getCacheListeners( item.getCacheName() );
            /* Object val = */item.getVal();

            Integer remoteTypeL = (Integer) idTypeMap.get( new Long( requesterId ) );
            if ( log.isDebugEnabled() )
            {
                log.debug( "in update, requesterId = [" + requesterId + "] remoteType = " + remoteTypeL );
            }

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
                    CompositeCache c = (CompositeCache) cacheDesc.cache;

                    // If the source of this request was not from a cluster,
                    // then
                    // consider it a local update. The cache manager will try to
                    // update all
                    // auxiliaries.
                    //
                    // This requires that two local caches not be connected to
                    // two clustered remote caches. The failover runner will
                    // have to make sure of this. ALos, the local cache needs
                    // avoid updating this source. Will need to pass the source
                    // id somehow. The remote cache should udate all local
                    // caches
                    // but not update the cluster source. Cluster remote caches
                    // should only be updated by the server and not the
                    // RemoteCache.
                    if ( fromCluster )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Put FROM cluster, NOT updating other auxiliaries for region." );
                        }

                        c.localUpdate( item );
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Put NOT from cluster, updating other auxiliaries for region." );
                        }

                        c.update( item );
                    }
                }
                catch ( Exception oee )
                {
                }

                // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED

                if ( !fromCluster || ( fromCluster && rcsa.getLocalClusterConsistency() ) )
                {

                    ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                    if ( qlist != null )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "qlist.length = " + qlist.length );
                        }
                        for ( int i = 0; i < qlist.length; i++ )
                        {
                            qlist[i].addPutEvent( item );
                        }
                    }
                    else
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "q list is null" );
                        }
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
            log.error( "Trouble in Update", e );
        }

        // TODO use JAMON for timing
        if ( timing )
        {
            long end = System.currentTimeMillis();
            if ( log.isDebugEnabled() )
            {
                log.debug( "put took " + String.valueOf( end - start ) + " ms." );
            }
        }

        return;
    }

    /**
     * Gets the eventQList attribute of the RemoteCacheServer object. This
     * returns the event queues stored in the cacheListeners object for a
     * particuylar region, if the queue is not for this requester.
     * <p>
     * Basically, this makes sure that a request from a particular local cache,
     * identified by its listener id, does not result in a call to that same
     * listener.
     * 
     * 
     * @param cacheListeners
     * @param requesterId
     * @return The eventQList value
     */
    private ICacheEventQueue[] getEventQList( CacheListeners cacheListeners, long requesterId )
    {
        ICacheEventQueue[] list = null;
        synchronized ( cacheListeners.eventQMap )
        {
            list = (ICacheEventQueue[]) cacheListeners.eventQMap.values().toArray( new ICacheEventQueue[0] );
        }
        int count = 0;
        // Set those not qualified to null; Count those qualified.
        for ( int i = 0; i < list.length; i++ )
        {
            ICacheEventQueue q = list[i];
            if ( q.isWorking() && q.getListenerId() != requesterId )
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
     * 
     * @param cacheName
     * @param key
     * @return
     * @throws IOException
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "get " + key + " from cache " + cacheName );
        }

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
        CompositeCache c = (CompositeCache) cacheDesc.cache;

        return c.localGet( key );
    }

    /**
     * Gets the set of keys of objects currently in the group.
     * 
     * @param cacheName
     * @param group
     * @return A Set of group keys
     */
    public Set getGroupKeys( String cacheName, String group )
    {
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
            return Collections.EMPTY_SET;
        }
        CompositeCache c = (CompositeCache) cacheDesc.cache;
        return c.getGroupKeys( group );
    }

    /**
     * Removes the given key from the specified remote cache. Defaults the
     * listener id to 0.
     * 
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void remove( String cacheName, Serializable key )
        throws IOException
    {
        remove( cacheName, key, 0 );
    }

    /**
     * Remove the key from the cache region and don't tell the source listener
     * about it.
     * 
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    public void remove( String cacheName, Serializable key, long requesterId )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "remove " + key + " from cache " + cacheName );
        }
        CacheListeners cacheDesc = (CacheListeners) cacheListenersMap.get( cacheName );

        Integer remoteTypeL = (Integer) idTypeMap.get( new Long( requesterId ) );
        boolean fromCluster = false;
        if ( remoteTypeL.intValue() == IRemoteCacheAttributes.CLUSTER )
        {
            fromCluster = true;
        }

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and
            // notification.
            synchronized ( cacheDesc )
            {

                boolean removeSuccess = false;

                // No need to notify if it was not cached.
                CompositeCache c = (CompositeCache) cacheDesc.cache;

                if ( fromCluster )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Remove FROM cluster, NOT updating other auxiliaries for region" );
                    }
                    removeSuccess = c.localRemove( key );
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Remove NOT from cluster, updating other auxiliaries for region" );
                    }
                    removeSuccess = c.remove( key );
                }

                if ( log.isDebugEnabled() )
                {
                    log.debug( "remove " + key + " from cache " + cacheName + " success (was it found) = "
                        + removeSuccess );
                }

                // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED

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
        return;
    }

    /**
     * Remove all keys from the sepcified remote cache.
     * 
     * @param cacheName
     * @throws IOException
     */
    public void removeAll( String cacheName )
        throws IOException
    {
        removeAll( cacheName, 0 );
    }

    /**
     * Remove all keys from the sepcified remote cache.
     * 
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void removeAll( String cacheName, long requesterId )
        throws IOException
    {
        CacheListeners cacheDesc = (CacheListeners) cacheListenersMap.get( cacheName );

        Integer remoteTypeL = (Integer) idTypeMap.get( new Long( requesterId ) );
        boolean fromCluster = false;
        if ( remoteTypeL.intValue() == IRemoteCacheAttributes.CLUSTER )
        {
            fromCluster = true;
        }

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and
            // notification.
            synchronized ( cacheDesc )
            {

                // No need to broadcast, or notify if it was not cached.
                CompositeCache c = (CompositeCache) cacheDesc.cache;

                if ( fromCluster )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "RemoveALL FROM cluster, NOT updating other auxiliaries for region" );
                    }
                    c.localRemoveAll();
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "RemoveALL NOT from cluster, updating other auxiliaries for region" );
                    }
                    c.removeAll();
                }

                // update registered listeners
                if ( !fromCluster || ( fromCluster && rcsa.getLocalClusterConsistency() ) )
                {

                    ICacheEventQueue[] qlist = getEventQList( cacheDesc, requesterId );

                    for ( int i = 0; i < qlist.length; i++ )
                    {
                        qlist[i].addRemoveAllEvent();
                    }

                }
            }
        }
        return;
    }

    /**
     * Frees the specified remote cache.
     * 
     * @param cacheName
     * @throws IOException
     */
    public void dispose( String cacheName )
        throws IOException
    {
        dispose( cacheName, 0 );
    }

    /**
     * Frees the specified remote cache.
     * 
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    public void dispose( String cacheName, long requesterId )
        throws IOException
    {
        CacheListeners cacheDesc = (CacheListeners) cacheListenersMap.get( cacheName );

        // this is dangerous
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

    /**
     * Frees all remote caches.
     * 
     * @throws IOException
     */
    public void release()
        throws IOException
    {
        synchronized ( cacheListenersMap )
        {
            for ( Enumeration en = cacheListenersMap.elements(); en.hasMoreElements(); )
            {
                CacheListeners cacheDesc = (CacheListeners) en.nextElement();
                ICacheEventQueue[] qlist = getEventQList( cacheDesc, 0 );

                for ( int i = 0; i < qlist.length; i++ )
                {
                    qlist[i].addDisposeEvent();
                }
            }
            cacheManager.release();
        }
        return;
    }

    /////////////////////// Implements the ICacheObserver interface.
    // //////////////////
    /**
     * Removes dead event queues. SHould clean out deregistered listeners.
     * 
     * @param eventQMap
     */
    private static void cleanupEventQMap( Map eventQMap )
    {
        synchronized ( eventQMap )
        {
            for ( Iterator itr = eventQMap.entrySet().iterator(); itr.hasNext(); )
            {
                Map.Entry e = (Map.Entry) itr.next();
                ICacheEventQueue q = (ICacheEventQueue) e.getValue();

                // this does not care if the q is alive (i.e. if
                // there are active threads; it cares if the queue
                // is working -- if it has not encoutnered errors
                // above the failure threshhold
                if ( !q.isWorking() )
                {
                    itr.remove();
                    p1( "Cache event queue " + q + " is not working and removed from cache server." );
                }
            }
        }
    }

    /**
     * Subscribes to the specified remote cache.
     * 
     * @param cacheName
     *            the specified remote cache.
     * @param listener
     *            object to notify for cache changes. must be synchronized since
     *            there are remote calls involved.
     * @throws IOException
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
            //if ( cacheName.equals("SYSTEM_CLUSTER") || listener instanceof
            // org.apache.jcs.auxiliary.remote.server.RemoteCacheServerListener
            // ) {
            IRemoteCacheListener ircl = (IRemoteCacheListener) listener;
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
                long id = 0;
                try
                {
                    id = listener.getListenerId();
                    // clients problably shouldn't do this.
                    if ( id == 0 )
                    {
                        // must start at one so the next gets recognized
                        long listenerIdB = nextListenerId();
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "listener id=" + ( listenerIdB & 0xff ) + " addded for cache " + cacheName );
                        }
                        listener.setListenerId( listenerIdB );
                        id = listenerIdB;
                        // in case it needs synchronization
                        log.info( "adding vm listener under new id = " + listenerIdB );
                        p1( "adding vm listener under new id = " + listenerIdB );
                    }
                    else
                    {
                        log.info( "adding listener under existing id = " + id );
                        // should confirm the the host is the same as we have on
                        // record, just in case
                        // a client has made a mistake.
                    }

                    // relate the type to an id
                    this.idTypeMap.put( new Long( id ), new Integer( remoteType ) );

                }
                catch ( IOException ioe )
                {
                    log.error( "Problem setting listener id", ioe );
                }

                CacheEventQueueFactory fact = new CacheEventQueueFactory();
                ICacheEventQueue q = fact.createCacheEventQueue( listener, id, cacheName, rcsa.getEventQueuePoolName(),
                                                                 rcsa.getEventQueueTypeFactoryCode() );

                eventQMap.put( new Long( listener.getListenerId() ), q );

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
     * @param listener
     *            The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    public void addCacheListener( ICacheListener listener )
        throws IOException
    {
        for ( Enumeration en = cacheListenersMap.keys(); en.hasMoreElements(); )
        {
            String cacheName = (String) en.nextElement();
            addCacheListener( cacheName, listener );

            if ( log.isDebugEnabled() )
            {
                log.debug( "adding listener for cache " + cacheName );
            }
        }
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheObserver#removeCacheListener(java.lang.String,
     *      org.apache.jcs.engine.behavior.ICacheListener)
     */
    public void removeCacheListener( String cacheName, ICacheListener listener )
        throws IOException
    {
        removeCacheListener( cacheName, listener.getListenerId() );
    }

    /**
     * Unsibscribe this region.
     * 
     * @param cacheName
     * @param listenerId
     * @throws IOException
     */
    public void removeCacheListener( String cacheName, long listenerId )
        throws IOException
    {

        if ( log.isInfoEnabled() )
        {
            log.info( "Removing listener for cache region = [" + cacheName + "] and listenerId = " + listenerId );
        }

        try
        {
            CacheListeners cacheDesc = getCacheListeners( cacheName );
            Map eventQMap = cacheDesc.eventQMap;
            cleanupEventQMap( eventQMap );
            ICacheEventQueue q = (ICacheEventQueue) eventQMap.remove( new Long( listenerId ) );

            if ( q != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Found queue for cache region = [" + cacheName + "] and listenerId = " + listenerId );
                }
                q.destroy();
                cleanupEventQMap( eventQMap );
            }
            else
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Did not find queue for cache region = [" + cacheName + "] and listenerId = "
                        + listenerId );
                }
            }

            if ( log.isInfoEnabled() )
            {
                log.info( "After removing listener " + listenerId + " cache region " + cacheName
                    + "'s listener size = " + cacheDesc.eventQMap.size() );
            }
        }
        catch ( NotBoundException ex )
        {
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * Unsubscribes from all remote caches.
     * 
     * @param listener
     * @throws IOException
     */
    public void removeCacheListener( ICacheListener listener )
        throws IOException
    {
        for ( Enumeration en = cacheListenersMap.keys(); en.hasMoreElements(); )
        {
            String cacheName = (String) en.nextElement();
            removeCacheListener( cacheName, listener );

            if ( log.isDebugEnabled() )
            {
                log.debug( "removing listener for cache " + cacheName );
            }
        }
        return;
    }

    /////////////////////// Implements the ICacheServiceAdmin interface.
    // //////////////////

    /**
     * Shuts down the remote server.
     * 
     * @throws IOException
     */
    public void shutdown()
        throws IOException
    {
        RemoteCacheServerFactory.shutdownImpl( "", Registry.REGISTRY_PORT );
    }

    /**
     * Shuts down a server at a particular host and port.
     * 
     * @param host
     * @param port
     * @throws IOException
     */
    public void shutdown( String host, int port )
        throws IOException
    {
        log.debug( "received shutdown request" );
        RemoteCacheServerFactory.shutdownImpl( host, port );
    }

    /////////////////////// Implements the Unreferenced interface.
    // //////////////////

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

    /**
     * Returns the next generated listener id [0,255].
     * 
     * @return the listener id of a client. This should be unique for this
     *         server.
     */
    private long nextListenerId()
    {
        long id = 0;
        if ( listenerId[0] == Long.MAX_VALUE )
        {
            synchronized ( listenerId )
            {
                id = listenerId[0];
                listenerId[0] = 0;
                // TODO: record & check if the generated id is currently being
                // used by a valid listener. Currently if the id wraps after
                // Long.MAX_VALUE,
                // we just assume it won't collide with an existing listener who
                // is live.
            }
        }
        else
        {
            synchronized ( listenerId )
            {
                id = ++listenerId[0];
            }
        }
        return id; //( long ) ( id & 0xff );
    }

    /**
     * Gets the stats attribute of the RemoteCacheServer object
     * 
     * @return The stats value
     * @throws IOException
     */
    public String getStats()
        throws IOException
    {
        return cacheManager.getStats();
    }

    /**
     * Short cut for writing to system out.
     * 
     * @param s
     */
    private static void p1( String s )
    {
        System.out.println( "RemoteCacheServer:" + s + " >" + Thread.currentThread().getName() );
    }
}
