package org.apache.commons.jcs3.auxiliary.remote.server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.jcs3.access.exception.CacheException;
import org.apache.commons.jcs3.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.IRemoteCacheServer;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.IRemoteCacheServerAttributes;
import org.apache.commons.jcs3.auxiliary.remote.server.behavior.RemoteType;
import org.apache.commons.jcs3.engine.CacheEventQueueFactory;
import org.apache.commons.jcs3.engine.CacheListeners;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.engine.control.CompositeCache;
import org.apache.commons.jcs3.engine.control.CompositeCacheManager;
import org.apache.commons.jcs3.engine.logging.CacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.utils.timing.ElapsedTimer;

/**
 * This class provides remote cache services. The remote cache server propagates events from local
 * caches to other local caches. It can also store cached data, making it available to new clients.
 * <p>
 * Remote cache servers can be clustered. If the cache used by this remote cache is configured to
 * use a remote cache of type cluster, the two remote caches will communicate with each other.
 * Remote and put requests can be sent from one remote to another. If they are configured to
 * broadcast such event to their client, then remove an puts can be sent to all locals in the
 * cluster.
 * <p>
 * Gets requests are made between clustered servers if AllowClusterGet is true. You can setup several
 * clients to use one remote server and several to use another. The get local will be distributed
 * between the two servers. Since caches are usually high get and low put, this should allow you to
 * scale.
 */
public class RemoteCacheServer<K, V>
    extends UnicastRemoteObject
    implements IRemoteCacheServer<K, V>, Unreferenced
{
    public static final String DFEAULT_REMOTE_CONFIGURATION_FILE = "/remote.cache.ccf";

    /** For serialization. Don't change. */
    private static final long serialVersionUID = -8072345435941473116L;

    /** Log instance */
    private static final Log log = Log.getLog( RemoteCacheServer.class );

    /** The interval at which we will log updates. */
    private static final int logInterval = 100;

    /**
     * Removes dead event queues. Should clean out deregistered listeners.
     *
     * @param eventQMap
     */
    private static <KK, VV> void cleanupEventQMap( final Map<Long, ICacheEventQueue<KK, VV>> eventQMap )
    {
        // this does not care if the q is alive (i.e. if
        // there are active threads; it cares if the queue
        // is working -- if it has not encountered errors
        // above the failure threshold
        eventQMap.entrySet().removeIf(e -> !e.getValue().isWorking());
    }

    /**
     * Subclass can override this method to create the specific cache manager.
     *
     * @param prop the configuration object.
     * @return The cache hub configured with this configuration.
     * @throws CacheException if the configuration cannot be loaded
     */
    private static CompositeCacheManager createCacheManager( final Properties prop ) throws CacheException
    {
        final CompositeCacheManager hub = CompositeCacheManager.getUnconfiguredInstance();
        hub.configure( prop );
        return hub;
    }

    /** Number of puts into the cache. */
    private int puts;

    /** Maps cache name to CacheListeners object. association of listeners (regions). */
    private final transient ConcurrentMap<String, CacheListeners<K, V>> cacheListenersMap =
        new ConcurrentHashMap<>();

    /** Maps cluster listeners to regions. */
    private final transient ConcurrentMap<String, CacheListeners<K, V>> clusterListenersMap =
        new ConcurrentHashMap<>();

    /** The central hub */
    private transient CompositeCacheManager cacheManager;

    /** Relates listener id with a type */
    private final ConcurrentMap<Long, RemoteType> idTypeMap = new ConcurrentHashMap<>();

    /** Relates listener id with an ip address */
    private final ConcurrentMap<Long, String> idIPMap = new ConcurrentHashMap<>();

    /** Used to get the next listener id. */
    private final int[] listenerId = new int[1];

    /** Configuration settings. */
    // package protected for access by unit test code
    final IRemoteCacheServerAttributes remoteCacheServerAttributes;

    /** An optional event logger */
    private transient ICacheEventLogger cacheEventLogger;

    /**
     * Constructor for the RemoteCacheServer object. This initializes the server with the values
     * from the properties object.
     *
     * @param rcsa
     * @param config cache hub configuration
     * @throws RemoteException
     */
    protected RemoteCacheServer( final IRemoteCacheServerAttributes rcsa, final Properties config )
        throws RemoteException
    {
        super( rcsa.getServicePort() );
        this.remoteCacheServerAttributes = rcsa;
        init( config );
    }

    /**
     * Constructor for the RemoteCacheServer object. This initializes the server with the values
     * from the properties object.
     *
     * @param rcsa
     * @param config cache hub configuration
     * @param customRMISocketFactory
     * @throws RemoteException
     */
    protected RemoteCacheServer( final IRemoteCacheServerAttributes rcsa, final Properties config, final RMISocketFactory customRMISocketFactory )
        throws RemoteException
    {
        super( rcsa.getServicePort(), customRMISocketFactory, customRMISocketFactory );
        this.remoteCacheServerAttributes = rcsa;
        init( config );
    }

    /**
     * Subscribes to all remote caches.
     *
     * @param listener The feature to be added to the CacheListener attribute
     * @throws IOException
     */
    @Override
    public <KK, VV> void addCacheListener( final ICacheListener<KK, VV> listener )
        throws IOException
    {
        for (final String cacheName : cacheListenersMap.keySet())
        {
            addCacheListener( cacheName, listener );

            log.debug( "Adding listener for cache [{0}]", cacheName );
        }
    }

    /**
     * Subscribes to the specified remote cache.
     * <p>
     * If the client id is 0, then the remote cache server will increment it's local count and
     * assign an id to the client.
     *
     * @param cacheName the specified remote cache.
     * @param listener object to notify for cache changes. must be synchronized since there are
     *            remote calls involved.
     * @throws IOException
     */
    @Override
    @SuppressWarnings("unchecked") // Need to cast to specific return type from getClusterListeners()
    public <KK, VV> void addCacheListener( final String cacheName, final ICacheListener<KK, VV> listener )
        throws IOException
    {
        if ( cacheName == null || listener == null )
        {
            throw new IllegalArgumentException( "cacheName and listener must not be null" );
        }
        final CacheListeners<KK, VV> cacheListeners;

        final IRemoteCacheListener<KK, VV> ircl = (IRemoteCacheListener<KK, VV>) listener;

        final String listenerAddress = ircl.getLocalHostAddress();

        final RemoteType remoteType = ircl.getRemoteType();
        if ( remoteType == RemoteType.CLUSTER )
        {
            log.debug( "adding cluster listener, listenerAddress [{0}]", listenerAddress );
            cacheListeners = (CacheListeners<KK, VV>)getClusterListeners( cacheName );
        }
        else
        {
            log.debug( "adding normal listener, listenerAddress [{0}]", listenerAddress );
            cacheListeners = (CacheListeners<KK, VV>)getCacheListeners( cacheName );
        }
        final Map<Long, ICacheEventQueue<KK, VV>> eventQMap = cacheListeners.eventQMap;
        cleanupEventQMap( eventQMap );

        // synchronized ( listenerId )
        synchronized ( ICacheListener.class )
        {
            long id = 0;
            try
            {
                id = listener.getListenerId();
                // clients probably shouldn't do this.
                if ( id == 0 )
                {
                    // must start at one so the next gets recognized
                    final long listenerIdB = nextListenerId();
                    log.debug( "listener id={0} addded for cache [{1}], listenerAddress [{2}]",
                            listenerIdB & 0xff, cacheName, listenerAddress );
                    listener.setListenerId( listenerIdB );
                    id = listenerIdB;

                    // in case it needs synchronization
                    final String message = "Adding vm listener under new id = [" + listenerIdB + "], listenerAddress ["
                        + listenerAddress + "]";
                    logApplicationEvent( "RemoteCacheServer", "addCacheListener", message );
                    log.info( message );
                }
                else
                {
                    final String message = "Adding listener under existing id = [" + id + "], listenerAddress ["
                        + listenerAddress + "]";
                    logApplicationEvent( "RemoteCacheServer", "addCacheListener", message );
                    log.info( message );
                    // should confirm the host is the same as we have on
                    // record, just in case a client has made a mistake.
                }

                // relate the type to an id
                this.idTypeMap.put( Long.valueOf( id ), remoteType);
                if ( listenerAddress != null )
                {
                    this.idIPMap.put( Long.valueOf( id ), listenerAddress );
                }
            }
            catch ( final IOException ioe )
            {
                final String message = "Problem setting listener id, listenerAddress [" + listenerAddress + "]";
                log.error( message, ioe );

                if ( cacheEventLogger != null )
                {
                    cacheEventLogger.logError( "RemoteCacheServer", "addCacheListener", message + " - "
                        + ioe.getMessage() );
                }
            }

            final CacheEventQueueFactory<KK, VV> fact = new CacheEventQueueFactory<>();
            final ICacheEventQueue<KK, VV> q = fact.createCacheEventQueue( listener, id, cacheName, remoteCacheServerAttributes
                .getEventQueuePoolName(), remoteCacheServerAttributes.getEventQueueType() );

            eventQMap.put(Long.valueOf(listener.getListenerId()), q);

            log.info( cacheListeners );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param item
     * @param requesterId
     * @param eventName
     * @return ICacheEvent
     */
    private ICacheEvent<ICacheElement<K, V>> createICacheEvent( final ICacheElement<K, V> item, final long requesterId, final String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<>();
        }
        final String ipAddress = getExtraInfoForRequesterId( requesterId );
        return cacheEventLogger
            .createICacheEvent( "RemoteCacheServer", item.getCacheName(), eventName, ipAddress, item );
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @param eventName
     * @return ICacheEvent
     */
    private <T> ICacheEvent<T> createICacheEvent( final String cacheName, final T key, final long requesterId, final String eventName )
    {
        if ( cacheEventLogger == null )
        {
            return new CacheEvent<>();
        }
        final String ipAddress = getExtraInfoForRequesterId( requesterId );
        return cacheEventLogger.createICacheEvent( "RemoteCacheServer", cacheName, eventName, ipAddress, key );
    }

    /**
     * Frees the specified remote cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void dispose( final String cacheName )
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
    public void dispose( final String cacheName, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( cacheName, "none", requesterId, ICacheEventLogger.DISPOSE_EVENT );
        try
        {
            processDispose( cacheName, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Returns a cache value from the specified remote cache; or null if the cache or key does not
     * exist.
     *
     * @param cacheName
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key )
        throws IOException
    {
        return this.get( cacheName, key, 0 );
    }

    /**
     * Returns a cache bean from the specified cache; or null if the key does not exist.
     * <p>
     * Adding the requestor id, allows the cache to determine the source of the get.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        ICacheElement<K, V> element = null;
        final ICacheEvent<K> cacheEvent = createICacheEvent( cacheName, key, requesterId, ICacheEventLogger.GET_EVENT );
        try
        {
            element = processGet( cacheName, key, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
        return element;
    }

    /**
     * Returns the cache listener for the specified cache. Creates the cache and the cache
     * descriptor if they do not already exist.
     *
     * @param cacheName
     * @return The cacheListeners value
     */
    protected CacheListeners<K, V> getCacheListeners( final String cacheName )
    {
        return cacheListenersMap.computeIfAbsent(cacheName, key -> {
            final CompositeCache<K, V> cache = cacheManager.getCache(key);
            return new CacheListeners<>( cache );
        });
    }

    /**
     * Gets the clusterListeners attribute of the RemoteCacheServer object.
     * <p>
     * TODO may be able to remove this
     * @param cacheName
     * @return The clusterListeners value
     */
    protected CacheListeners<K, V> getClusterListeners( final String cacheName )
    {
        return clusterListenersMap.computeIfAbsent(cacheName, key -> {
            final CompositeCache<K, V> cache = cacheManager.getCache( cacheName );
            return new CacheListeners<>( cache );
        });
    }

    /**
     * Gets the eventQList attribute of the RemoteCacheServer object. This returns the event queues
     * stored in the cacheListeners object for a particular region, if the queue is not for this
     * requester.
     * <p>
     * Basically, this makes sure that a request from a particular local cache, identified by its
     * listener id, does not result in a call to that same listener.
     *
     * @param cacheListeners
     * @param requesterId
     * @return The eventQList value
     */
    private List<ICacheEventQueue<K, V>> getEventQList( final CacheListeners<K, V> cacheListeners, final long requesterId )
    {
        final List<ICacheEventQueue<K, V>> list = new ArrayList<>(cacheListeners.eventQMap.values());

        // Only return qualified event queues
        list.removeIf(q -> (!q.isWorking() || q.getListenerId() == requesterId));

        return list;
    }

    /**
     * Ip address for the client, if one is stored.
     * <p>
     * Protected for testing.
     *
     * @param requesterId
     * @return String
     */
    protected String getExtraInfoForRequesterId( final long requesterId )
    {
        return idIPMap.get( Long.valueOf( requesterId ) );
    }

    /**
     * Gets the item from the associated cache listeners.
     *
     * @param key
     * @param fromCluster
     * @param cacheDesc
     * @param element
     * @return ICacheElement
     */
    private ICacheElement<K, V> getFromCacheListeners( final K key, final boolean fromCluster, final CacheListeners<K, V> cacheDesc,
                                                 final ICacheElement<K, V> element )
    {
        ICacheElement<K, V> returnElement = element;

        if ( cacheDesc != null )
        {
            final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

            // If we have a get come in from a client and we don't have the item
            // locally, we will allow the cache to look in other non local sources,
            // such as a remote cache or a lateral.
            //
            // Since remote servers never get from clients and clients never go
            // remote from a remote call, this
            // will not result in any loops.
            //
            // This is the only instance I can think of where we allow a remote get
            // from a remote call. The purpose is to allow remote cache servers to
            // talk to each other. If one goes down, you want it to be able to get
            // data from those that were up when the failed server comes back o
            // line.

            if ( !fromCluster && this.remoteCacheServerAttributes.isAllowClusterGet() )
            {
                log.debug( "NonLocalGet. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );
                returnElement = c.get( key );
            }
            else
            {
                // Gets from cluster type remote will end up here.
                // Gets from all clients will end up here if allow cluster get is
                // false.
                log.debug( "LocalGet. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );
                returnElement = c.localGet( key );
            }
        }

        return returnElement;
    }

    /**
     * Return the keys in the cache.
     *
     * @param cacheName the name of the cache region
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet(final String cacheName) throws IOException
    {
        return processGetKeySet( cacheName );
    }

    /**
     * Gets all matching items.
     *
     * @param cacheName
     * @param pattern
     * @return Map of keys and wrapped objects
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern )
        throws IOException
    {
        return getMatching( cacheName, pattern, 0 );
    }

    /**
     * Gets all matching keys.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map of keys and wrapped objects
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String cacheName, final String pattern, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( cacheName, pattern, requesterId,
                                                    ICacheEventLogger.GETMATCHING_EVENT );
        try
        {
            return processGetMatching( cacheName, pattern, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets the item from the associated cache listeners.
     *
     * @param pattern
     * @param fromCluster
     * @param cacheDesc
     * @return Map of keys to results
     */
    private Map<K, ICacheElement<K, V>> getMatchingFromCacheListeners( final String pattern, final boolean fromCluster, final CacheListeners<K, V> cacheDesc )
    {
        Map<K, ICacheElement<K, V>> elements = null;
        if ( cacheDesc != null )
        {
            final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

            // We always want to go remote and then merge the items.  But this can lead to inconsistencies after
            // failover recovery.  Removed items may show up.  There is no good way to prevent this.
            // We should make it configurable.

            if ( !fromCluster && this.remoteCacheServerAttributes.isAllowClusterGet() )
            {
                log.debug( "NonLocalGetMatching. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );
                elements = c.getMatching( pattern );
            }
            else
            {
                // Gets from cluster type remote will end up here.
                // Gets from all clients will end up here if allow cluster get is
                // false.

                log.debug( "LocalGetMatching. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );
                elements = c.localGetMatching( pattern );
            }
        }
        return elements;
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param cacheName
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys )
        throws IOException
    {
        return this.getMultiple( cacheName, keys, 0 );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple( final String cacheName, final Set<K> keys, final long requesterId )
        throws IOException
    {
        final ICacheEvent<Serializable> cacheEvent = createICacheEvent( cacheName, (Serializable) keys, requesterId,
                                                    ICacheEventLogger.GETMULTIPLE_EVENT );
        try
        {
            return processGetMultiple( cacheName, keys, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Gets the items from the associated cache listeners.
     *
     * @param keys
     * @param elements
     * @param fromCluster
     * @param cacheDesc
     * @return Map
     */
    private Map<K, ICacheElement<K, V>> getMultipleFromCacheListeners( final Set<K> keys, final Map<K, ICacheElement<K, V>> elements, final boolean fromCluster, final CacheListeners<K, V> cacheDesc )
    {
        Map<K, ICacheElement<K, V>> returnElements = elements;

        if ( cacheDesc != null )
        {
            final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

            // If we have a getMultiple come in from a client and we don't have the item
            // locally, we will allow the cache to look in other non local sources,
            // such as a remote cache or a lateral.
            //
            // Since remote servers never get from clients and clients never go
            // remote from a remote call, this
            // will not result in any loops.
            //
            // This is the only instance I can think of where we allow a remote get
            // from a remote call. The purpose is to allow remote cache servers to
            // talk to each other. If one goes down, you want it to be able to get
            // data from those that were up when the failed server comes back on
            // line.

            if ( !fromCluster && this.remoteCacheServerAttributes.isAllowClusterGet() )
            {
                log.debug( "NonLocalGetMultiple. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );

                returnElements = c.getMultiple( keys );
            }
            else
            {
                // Gets from cluster type remote will end up here.
                // Gets from all clients will end up here if allow cluster get is
                // false.

                log.debug( "LocalGetMultiple. fromCluster [{0}] AllowClusterGet [{1}]",
                        fromCluster, this.remoteCacheServerAttributes.isAllowClusterGet() );

                returnElements = c.localGetMultiple( keys );
            }
        }

        return returnElements;
    }

    /**
     * How many put events have we received.
     *
     * @return puts
     */
    // Currently only intended for use by unit tests
    int getPutCount()
    {
        return puts;
    }

    /**
     * Gets the stats attribute of the RemoteCacheServer object.
     *
     * @return The stats value
     * @throws IOException
     */
    @Override
    public String getStats()
        throws IOException
    {
        return cacheManager.getStats();
    }

    /**
     * Initialize the RMI Cache Server from a properties object.
     *
     * @param prop the configuration properties
     * @throws RemoteException if the configuration of the cache manager instance fails
     */
    private void init( final Properties prop ) throws RemoteException
    {
        try
        {
            cacheManager = createCacheManager( prop );
        }
        catch (final CacheException e)
        {
            throw new RemoteException(e.getMessage(), e);
        }

        // cacheManager would have created a number of ICache objects.
        // Use these objects to set up the cacheListenersMap.
        cacheManager.getCacheNames().forEach(name -> {
            final CompositeCache<K, V> cache = cacheManager.getCache( name );
            cacheListenersMap.put( name, new CacheListeners<>( cache ) );
        });
    }

    /**
     * Since a non-receiving remote cache client will not register a listener, it will not have a
     * listener id assigned from the server. As such the remote server cannot determine if it is a
     * cluster or a normal client. It will assume that it is a normal client.
     *
     * @param requesterId
     * @return true is from a cluster.
     */
    private boolean isRequestFromCluster( final long requesterId )
    {
        final RemoteType remoteTypeL = idTypeMap.get( Long.valueOf( requesterId ) );
        return remoteTypeL == RemoteType.CLUSTER;
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param source
     * @param eventName
     * @param optionalDetails
     */
    protected void logApplicationEvent( final String source, final String eventName, final String optionalDetails )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logApplicationEvent( source, eventName, optionalDetails );
        }
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param cacheEvent
     */
    protected <T> void logICacheEvent( final ICacheEvent<T> cacheEvent )
    {
        if ( cacheEventLogger != null )
        {
            cacheEventLogger.logICacheEvent( cacheEvent );
        }
    }

    /**
     * Log some details.
     *
     * @param item
     */
    private void logUpdateInfo( final ICacheElement<K, V> item )
    {
        // not thread safe, but it doesn't have to be 100% accurate
        puts++;

        if ( log.isInfoEnabled() && puts % logInterval == 0 )
        {
            log.info( "puts = {0}", puts );
        }

        log.debug( "In update, put [{0}] in [{1}]",
                item::getKey, item::getCacheName);
    }

    /**
     * Returns the next generated listener id [0,255].
     *
     * @return the listener id of a client. This should be unique for this server.
     */
    private long nextListenerId()
    {
        long id = 0;
        if ( listenerId[0] == Integer.MAX_VALUE )
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
        return id;
    }

    /**
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    private void processDispose( final String cacheName, final long requesterId )
        throws IOException
    {
        log.info( "Dispose request received from listener [{0}]", requesterId );

        final CacheListeners<K, V> cacheDesc = cacheListenersMap.get( cacheName );

        // this is dangerous
        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered free-cache-op and notification.
            synchronized ( cacheDesc )
            {
                final List<ICacheEventQueue<K,V>> qlist = getEventQList( cacheDesc, requesterId );

                for (final ICacheEventQueue<K, V> element : qlist)
                {
                    element.addDisposeEvent();
                }
                cacheManager.freeCache( cacheName );
            }
        }
    }

    /**
     * Returns a cache bean from the specified cache; or null if the key does not exist.
     * <p>
     * Adding the requester id, allows the cache to determine the source of the get.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @return ICacheElement
     */
    private ICacheElement<K, V> processGet( final String cacheName, final K key, final long requesterId )
    {
        final boolean fromCluster = isRequestFromCluster( requesterId );

        log.debug( "get [{0}] from cache [{1}] requesterId = [{2}] fromCluster = {3}",
                key, cacheName, requesterId, fromCluster );

        final CacheListeners<K, V> cacheDesc = getCacheListeners( cacheName );

        return getFromCacheListeners( key, fromCluster, cacheDesc, null );
    }

    /**
     * Gets the set of keys of objects currently in the cache.
     *
     * @param cacheName
     * @return Set
     */
    protected Set<K> processGetKeySet( final String cacheName )
    {
        final CacheListeners<K, V> cacheDesc = getCacheListeners( cacheName );

        if ( cacheDesc == null )
        {
            return Collections.emptySet();
        }

        final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;
        return c.getKeySet();
    }

    /**
     * Retrieves all matching keys.
     *
     * @param cacheName
     * @param pattern
     * @param requesterId
     * @return Map of keys and wrapped objects
     */
    protected Map<K, ICacheElement<K, V>> processGetMatching( final String cacheName, final String pattern, final long requesterId )
    {
        final boolean fromCluster = isRequestFromCluster( requesterId );

        log.debug( "getMatching [{0}] from cache [{1}] requesterId = [{2}] fromCluster = {3}",
                pattern, cacheName, requesterId, fromCluster );

        CacheListeners<K, V> cacheDesc = null;
        try
        {
            cacheDesc = getCacheListeners( cacheName );
        }
        catch ( final Exception e )
        {
            log.error( "Problem getting listeners.", e );

            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RemoteCacheServer", ICacheEventLogger.GETMATCHING_EVENT, e.getMessage()
                    + cacheName + " pattern: " + pattern );
            }
        }

        return getMatchingFromCacheListeners( pattern, fromCluster, cacheDesc );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param cacheName
     * @param keys
     * @param requesterId
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    private Map<K, ICacheElement<K, V>> processGetMultiple( final String cacheName, final Set<K> keys, final long requesterId )
    {
        final boolean fromCluster = isRequestFromCluster( requesterId );

        log.debug( "getMultiple [{0}] from cache [{1}] requesterId = [{2}] fromCluster = {3}",
                keys, cacheName, requesterId, fromCluster );

        final CacheListeners<K, V> cacheDesc = getCacheListeners( cacheName );
        return getMultipleFromCacheListeners( keys, null, fromCluster, cacheDesc );
    }

    /**
     * Remove the key from the cache region and don't tell the source listener about it.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    private void processRemove( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        log.debug( "remove [{0}] from cache [{1}]", key, cacheName );

        final CacheListeners<K, V> cacheDesc = cacheListenersMap.get( cacheName );

        final boolean fromCluster = isRequestFromCluster( requesterId );

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and
            // notification.
            synchronized ( cacheDesc )
            {
                boolean removeSuccess = false;

                // No need to notify if it was not cached.
                final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

                if ( fromCluster )
                {
                    log.debug( "Remove FROM cluster, NOT updating other auxiliaries for region" );
                    removeSuccess = c.localRemove( key );
                }
                else
                {
                    log.debug( "Remove NOT from cluster, updating other auxiliaries for region" );
                    removeSuccess = c.remove( key );
                }

                log.debug( "remove [{0}] from cache [{1}] success (was it found) = {2}",
                        key, cacheName, removeSuccess );

                // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED
                if (!fromCluster || fromCluster && remoteCacheServerAttributes.isLocalClusterConsistency())
                {
                    final List<ICacheEventQueue<K,V>> qlist = getEventQList( cacheDesc, requesterId );

                    for (final ICacheEventQueue<K, V> element : qlist)
                    {
                        element.addRemoveEvent( key );
                    }
                }
            }
        }
    }

    /**
     * Remove all keys from the specified remote cache.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    private void processRemoveAll( final String cacheName, final long requesterId )
        throws IOException
    {
        final CacheListeners<K, V> cacheDesc = cacheListenersMap.get( cacheName );

        final boolean fromCluster = isRequestFromCluster( requesterId );

        if ( cacheDesc != null )
        {
            // best attempt to achieve ordered cache item removal and
            // notification.
            synchronized ( cacheDesc )
            {
                // No need to broadcast, or notify if it was not cached.
                final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

                if ( fromCluster )
                {
                    log.debug( "RemoveALL FROM cluster, NOT updating other auxiliaries for region" );
                    c.localRemoveAll();
                }
                else
                {
                    log.debug( "RemoveALL NOT from cluster, updating other auxiliaries for region" );
                    c.removeAll();
                }

                // update registered listeners
                if (!fromCluster || fromCluster && remoteCacheServerAttributes.isLocalClusterConsistency())
                {
                    final List<ICacheEventQueue<K,V>> qlist = getEventQList( cacheDesc, requesterId );

                    for (final ICacheEventQueue<K, V> q : qlist)
                    {
                        q.addRemoveAllEvent();
                    }
                }
            }
        }
    }

    /**
     * An update can come from either a local cache's remote auxiliary, or it can come from a remote
     * server. A remote server is considered a source of type cluster.
     * <p>
     * If the update came from a cluster, then we should tell the cache manager that this was a
     * remote put. This way, any lateral and remote auxiliaries configured for the region will not
     * be updated. This is basically how a remote listener works when plugged into a local cache.
     * <p>
     * If the cluster is configured to keep local cluster consistency, then all listeners will be
     * updated. This allows cluster server A to update cluster server B and then B to update its
     * clients if it is told to keep local cluster consistency. Otherwise, server A will update
     * server B and B will not tell its clients. If you cluster using lateral caches for instance,
     * this is how it will work. Updates to a cluster node, will never get to the leaves. The remote
     * cluster, with local cluster consistency, allows you to update leaves. This basically allows
     * you to have a failover remote server.
     * <p>
     * Since currently a cluster will not try to get from other cluster servers, you can scale a bit
     * with a cluster configuration. Puts and removes will be broadcasted to all clients, but the
     * get load on a remote server can be reduced.
     *
     * @param item
     * @param requesterId
     */
    private void processUpdate( final ICacheElement<K, V> item, final long requesterId )
    {
        final ElapsedTimer timer = new ElapsedTimer();
        logUpdateInfo( item );

        try
        {
            final CacheListeners<K, V> cacheDesc = getCacheListeners( item.getCacheName() );
            final boolean fromCluster = isRequestFromCluster( requesterId );

            log.debug( "In update, requesterId = [{0}] fromCluster = {1}", requesterId, fromCluster );

            // ordered cache item update and notification.
            synchronized ( cacheDesc )
            {
                try
                {
                    final CompositeCache<K, V> c = (CompositeCache<K, V>) cacheDesc.cache;

                    // If the source of this request was not from a cluster,
                    // then consider it a local update. The cache manager will
                    // try to
                    // update all auxiliaries.
                    //
                    // This requires that two local caches not be connected to
                    // two clustered remote caches. The failover runner will
                    // have to make sure of this. ALos, the local cache needs
                    // avoid updating this source. Will need to pass the source
                    // id somehow. The remote cache should update all local
                    // caches
                    // but not update the cluster source. Cluster remote caches
                    // should only be updated by the server and not the
                    // RemoteCache.
                    if ( fromCluster )
                    {
                        log.debug( "Put FROM cluster, NOT updating other auxiliaries for region. "
                                + " requesterId [{0}]", requesterId );
                        c.localUpdate( item );
                    }
                    else
                    {
                        log.debug( "Put NOT from cluster, updating other auxiliaries for region. "
                                + " requesterId [{0}]", requesterId );
                        c.update( item );
                    }
                }
                catch ( final IOException ce )
                {
                    // swallow
                    log.info( "Exception caught updating item. requesterId [{0}]: {1}",
                            requesterId, ce.getMessage() );
                }

                // UPDATE LOCALS IF A REQUEST COMES FROM A CLUSTER
                // IF LOCAL CLUSTER CONSISTENCY IS CONFIGURED
                if (!fromCluster || fromCluster && remoteCacheServerAttributes.isLocalClusterConsistency())
                {
                    final List<ICacheEventQueue<K,V>> qlist = getEventQList( cacheDesc, requesterId );
                    log.debug("qlist.size() = {0}", qlist.size());
                    for (final ICacheEventQueue<K, V> element : qlist)
                    {
                        element.addPutEvent( item );
                    }
                }
            }
        }
        catch ( final IOException e )
        {
            if ( cacheEventLogger != null )
            {
                cacheEventLogger.logError( "RemoteCacheServer", ICacheEventLogger.UPDATE_EVENT, e.getMessage()
                    + " REGION: " + item.getCacheName() + " ITEM: " + item );
            }

            log.error( "Trouble in Update. requesterId [{0}]", requesterId, e );
        }

        // TODO use JAMON for timing
        log.debug( "put took {0} ms.", timer::getElapsedTime);
    }

    /**
     * Puts a cache bean to the remote cache and notifies all listeners which <br>
     * <ol>
     * <li>have a different listener id than the originating host;</li>
     * <li>are currently subscribed to the related cache.</li>
     * </ol>
     *
     * @param item
     * @throws IOException
     */
    public void put( final ICacheElement<K, V> item )
        throws IOException
    {
        update( item );
    }

    /**
     * Frees all remote caches.
     *
     * @throws IOException
     */
    @Override
    public void release()
        throws IOException
    {
        for (final CacheListeners<K, V> cacheDesc : cacheListenersMap.values())
        {
            final List<ICacheEventQueue<K,V>> qlist = getEventQList( cacheDesc, 0 );

            for (final ICacheEventQueue<K, V> element : qlist)
            {
                element.addDisposeEvent();
            }
        }
        cacheManager.release();
    }

    /**
     * Removes the given key from the specified remote cache. Defaults the listener id to 0.
     *
     * @param cacheName
     * @param key
     * @throws IOException
     */
    @Override
    public void remove( final String cacheName, final K key )
        throws IOException
    {
        remove( cacheName, key, 0 );
    }

    /**
     * Remove the key from the cache region and don't tell the source listener about it.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param key
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void remove( final String cacheName, final K key, final long requesterId )
        throws IOException
    {
        final ICacheEvent<K> cacheEvent = createICacheEvent( cacheName, key, requesterId, ICacheEventLogger.REMOVE_EVENT );
        try
        {
            processRemove( cacheName, key, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Remove all keys from the specified remote cache.
     *
     * @param cacheName
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName )
        throws IOException
    {
        removeAll( cacheName, 0 );
    }

    /**
     * Remove all keys from the specified remote cache.
     * <p>
     * The internal processing is wrapped in event logging calls.
     *
     * @param cacheName
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void removeAll( final String cacheName, final long requesterId )
        throws IOException
    {
        final ICacheEvent<String> cacheEvent = createICacheEvent( cacheName, "all", requesterId, ICacheEventLogger.REMOVEALL_EVENT );
        try
        {
            processRemoveAll( cacheName, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }

    /**
     * Unsubscribes from all remote caches.
     *
     * @param listener
     * @throws IOException
     */
    @Override
    public <KK, VV> void removeCacheListener( final ICacheListener<KK, VV> listener )
        throws IOException
    {
        for (final String cacheName : cacheListenersMap.keySet())
        {
            removeCacheListener( cacheName, listener );

            log.info( "Removing listener for cache [{0}]", cacheName );
        }
    }

    /**
     * Unsubscribe this listener from this region. If the listener is registered, it will be removed
     * from the event queue map list.
     *
     * @param cacheName
     * @param listener
     * @throws IOException
     */
    @Override
    public <KK, VV> void removeCacheListener( final String cacheName, final ICacheListener<KK, VV> listener )
        throws IOException
    {
        removeCacheListener( cacheName, listener.getListenerId() );
    }

    /**
     * Unsubscribe this listener from this region. If the listener is registered, it will be removed
     * from the event queue map list.
     *
     * @param cacheName
     * @param listenerId
     */
    public void removeCacheListener( final String cacheName, final long listenerId )
    {
        final String message = "Removing listener for cache region = [" + cacheName + "] and listenerId [" + listenerId + "]";
        logApplicationEvent( "RemoteCacheServer", "removeCacheListener", message );
        log.info( message );

        final boolean isClusterListener = isRequestFromCluster( listenerId );

        CacheListeners<K, V> cacheDesc = null;

        if ( isClusterListener )
        {
            cacheDesc = getClusterListeners( cacheName );
        }
        else
        {
            cacheDesc = getCacheListeners( cacheName );
        }
        final Map<Long, ICacheEventQueue<K, V>> eventQMap = cacheDesc.eventQMap;
        cleanupEventQMap( eventQMap );
        final ICacheEventQueue<K, V> q = eventQMap.remove( Long.valueOf( listenerId ) );

        if ( q != null )
        {
            log.debug( "Found queue for cache region = [{0}] and listenerId [{1}]",
                    cacheName, listenerId );
            q.destroy();
            cleanupEventQMap( eventQMap );
        }
        else
        {
            log.debug( "Did not find queue for cache region = [{0}] and listenerId [{1}]",
                    cacheName, listenerId );
        }

        // cleanup
        idTypeMap.remove( Long.valueOf( listenerId ) );
        idIPMap.remove( Long.valueOf( listenerId ) );

        log.info( "After removing listener [{0}] cache region {1} listener size [{2}]",
                listenerId, cacheName, eventQMap.size() );
    }

    /**
     * Allows it to be injected.
     *
     * @param cacheEventLogger
     */
    public void setCacheEventLogger( final ICacheEventLogger cacheEventLogger )
    {
        this.cacheEventLogger = cacheEventLogger;
    }

    /**
     * Shuts down the remote server.
     *
     * @throws IOException
     */
    @Override
    public void shutdown()
        throws IOException
    {
        shutdown("", Registry.REGISTRY_PORT);
    }

    /**
     * Shuts down a server at a particular host and port. Then it calls shutdown on the cache
     * itself.
     *
     * @param host
     * @param port
     * @throws IOException
     */
    @Override
    public void shutdown( final String host, final int port )
        throws IOException
    {
        log.info( "Received shutdown request. Shutting down server." );

        synchronized (listenerId)
        {
            for (final String cacheName : cacheListenersMap.keySet())
            {
                for (int i = 0; i <= listenerId[0]; i++)
                {
                    removeCacheListener( cacheName, i );
                }

                log.info( "Removing listener for cache [{0}]", cacheName );
            }

            cacheListenersMap.clear();
            clusterListenersMap.clear();
        }
        RemoteCacheServerFactory.shutdownImpl( host, port );
        this.cacheManager.shutDown();
    }

    /**
     * Called by the RMI runtime sometime after the runtime determines that the reference list, the
     * list of clients referencing the remote object, becomes empty.
     */
    // TODO: test out the DGC.
    @Override
    public void unreferenced()
    {
        log.info( "*** Server now unreferenced and subject to GC. ***" );
    }

    /**
     * @param item
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> item )
        throws IOException
    {
        update( item, 0 );
    }

    /**
     * The internal processing is wrapped in event logging calls.
     *
     * @param item
     * @param requesterId
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> item, final long requesterId )
        throws IOException
    {
        final ICacheEvent<ICacheElement<K, V>> cacheEvent = createICacheEvent( item, requesterId, ICacheEventLogger.UPDATE_EVENT );
        try
        {
            processUpdate( item, requesterId );
        }
        finally
        {
            logICacheEvent( cacheEvent );
        }
    }
}
