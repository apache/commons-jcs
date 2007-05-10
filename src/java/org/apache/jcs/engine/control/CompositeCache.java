package org.apache.jcs.engine.control;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.ElementEventQueue;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEventQueue;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;
import org.apache.jcs.engine.stats.CacheStats;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.ICacheStats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * This is the primary hub for a single cache/region. It controls the flow of items through the
 * cache. The auxiliary and memory caches are plugged in here.
 * <p>
 * This is the core of a JCS region. Hence, this simple class is the core of JCS.
 */
public class CompositeCache
    implements ICache, Serializable
{
    private static final long serialVersionUID = -2838097410378294960L;

    private final static Log log = LogFactory.getLog( CompositeCache.class );

    /**
     * EventQueue for handling element events. 1 should be enough for all the regions. Else should
     * create as needed per region.
     */
    public static IElementEventQueue elementEventQ = new ElementEventQueue( "AllRegionQueue" );

    // Auxiliary caches.
    private AuxiliaryCache[] auxCaches = new AuxiliaryCache[0];

    private boolean alive = true;

    // this is in the cacheAttr, shouldn't be used, remove
    final String cacheName;

    /** Region Elemental Attributes, default. */
    private IElementAttributes attr;

    /** Cache Attributes, for hub and memory auxiliary. */
    private ICompositeCacheAttributes cacheAttr;

    // Statistics
    private int updateCount;

    private int removeCount;

    /** Memory cache hit count */
    private int hitCountRam;

    /** Auxiliary cache hit count (number of times found in ANY auxiliary) */
    private int hitCountAux;

    /** Auxiliary hit counts broken down by auxiliary. */
    private int[] auxHitCountByIndex;

    /** Count of misses where element was not found. */
    private int missCountNotFound = 0;

    /** Count of misses where element was expired. */
    private int missCountExpired = 0;

    /**
     * The cache hub can only have one memory cache. This could be made more flexible in the future,
     * but they are tied closely together. More than one doesn't make much sense.
     */
    private MemoryCache memCache;

    /**
     * Constructor for the Cache object
     * <p>
     * @param cacheName The name of the region
     * @param cattr The cache attribute
     * @param attr The default element attributes
     */
    public CompositeCache( String cacheName, ICompositeCacheAttributes cattr, IElementAttributes attr )
    {
        this.cacheName = cacheName;
        this.attr = attr;
        this.cacheAttr = cattr;

        createMemoryCache( cattr );

        if ( log.isInfoEnabled() )
        {
            log.info( "Constructed cache with name [" + cacheName + "] and cache attributes " + cattr );
        }
    }

    /**
     * This sets the list of auxiliary caches for this region.
     * <p>
     * @param auxCaches
     */
    public void setAuxCaches( AuxiliaryCache[] auxCaches )
    {
        this.auxCaches = auxCaches;

        if ( auxCaches != null )
        {
            this.auxHitCountByIndex = new int[auxCaches.length];
        }
    }

    /**
     * Standard update method.
     * <p>
     * @param ce
     * @exception IOException
     */
    public synchronized void update( ICacheElement ce )
        throws IOException
    {
        update( ce, false );
    }

    /**
     * Standard update method.
     * <p>
     * @param ce
     * @exception IOException
     */
    public synchronized void localUpdate( ICacheElement ce )
        throws IOException
    {
        update( ce, true );
    }

    /**
     * Put an item into the cache. If it is localOnly, then do no notify remote or lateral
     * auxiliaries.
     * <p>
     * @param cacheElement the ICacheElement
     * @param localOnly Whether the operation should be restricted to local auxiliaries.
     * @exception IOException
     */
    protected synchronized void update( ICacheElement cacheElement, boolean localOnly )
        throws IOException
    {
        // not thread safe, but just for debugging and testing.
        updateCount++;

        if ( cacheElement.getKey() instanceof String
            && cacheElement.getKey().toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            throw new IllegalArgumentException( "key must not end with " + CacheConstants.NAME_COMPONENT_DELIMITER
                + " for a put operation" );
        }
        else if ( cacheElement.getKey() instanceof GroupId )
        {
            throw new IllegalArgumentException( "key cannot be a GroupId " + " for a put operation" );
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Updating memory cache" );
        }

        memCache.update( cacheElement );

        updateAuxiliaries( cacheElement, localOnly );
    }

    /**
     * This method is responsible for updating the auxiliaries if they are present. If it is local
     * only, any lateral and remote auxiliaries will not be updated.
     * <p>
     * Before updating an auxiliary it checks to see if the element attributes permit the operation.
     * <p>
     * Disk auxiliaries are only updated if the disk cache is not merely used as a swap. If the disk
     * cache is merely a swap, then items will only go to disk when they overflow from memory.
     * <p>
     * This is called by update( cacheElement, localOnly ) after it updates the memory cache.
     * <p>
     * This is protected to make it testable.
     * <p>
     * @param cacheElement
     * @param localOnly
     * @throws IOException
     */
    protected void updateAuxiliaries( ICacheElement cacheElement, boolean localOnly )
        throws IOException
    {
        // UPDATE AUXILLIARY CACHES
        // There are 3 types of auxiliary caches: remote, lateral, and disk
        // more can be added if future auxiliary caches don't fit the model
        // You could run a database cache as either a remote or a local disk.
        // The types would describe the purpose.

        if ( log.isDebugEnabled() )
        {
            if ( auxCaches.length > 0 )
            {
                log.debug( "Updating auxilliary caches" );
            }
            else
            {
                log.debug( "No auxilliary cache to update" );
            }
        }

        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            if ( log.isDebugEnabled() )
            {
                log.debug( "Auxilliary cache type: " + aux.getCacheType() );
            }

            if ( aux == null )
            {
                continue;
            }

            // SEND TO REMOTE STORE
            if ( aux.getCacheType() == ICache.REMOTE_CACHE )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "ce.getElementAttributes().getIsRemote() = "
                        + cacheElement.getElementAttributes().getIsRemote() );
                }

                if ( cacheElement.getElementAttributes().getIsRemote() && !localOnly )
                {
                    try
                    {
                        // need to make sure the group cache understands that
                        // the key is a group attribute on update
                        aux.update( cacheElement );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Updated remote store for " + cacheElement.getKey() + cacheElement );
                        }
                    }
                    catch ( IOException ex )
                    {
                        log.error( "Failure in updateExclude", ex );
                    }
                }
                // SEND LATERALLY
            }
            else if ( aux.getCacheType() == ICache.LATERAL_CACHE )
            {
                // lateral can't do the checking since it is dependent on the
                // cache region restrictions
                if ( log.isDebugEnabled() )
                {
                    log.debug( "lateralcache in aux list: cattr " + cacheAttr.getUseLateral() );
                }
                if ( cacheAttr.getUseLateral() && cacheElement.getElementAttributes().getIsLateral() && !localOnly )
                {
                    // later if we want a multicast, possibly delete abnormal
                    // broadcaster
                    // DISTRIBUTE LATERALLY
                    // Currently always multicast even if the value is
                    // unchanged,
                    // just to cause the cache item to move to the front.
                    aux.update( cacheElement );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "updated lateral cache for " + cacheElement.getKey() );
                    }
                }
            }
            // update disk if the usage pattern permits
            else if ( aux.getCacheType() == ICache.DISK_CACHE )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "diskcache in aux list: cattr " + cacheAttr.getUseDisk() );
                }
                if ( cacheAttr.getUseDisk()
                    && ( cacheAttr.getDiskUsagePattern() == ICompositeCacheAttributes.DISK_USAGE_PATTERN_UPDATE )
                    && cacheElement.getElementAttributes().getIsSpool() )
                {
                    aux.update( cacheElement );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "updated disk cache for " + cacheElement.getKey() );
                    }
                }
            }
        }
    }

    /**
     * Writes the specified element to any disk auxilliaries. Might want to rename this "overflow"
     * incase the hub wants to do something else.
     * <p>
     * If JCS is not configured to use the disk as a swap, that is if the the
     * CompositeCacheAttribute diskUsagePattern is not SWAP_ONLY, then the item will not be spooled.
     * <p>
     * @param ce The CacheElement
     */
    public void spoolToDisk( ICacheElement ce )
    {
        // if the item is not spoolable, return
        if ( !ce.getElementAttributes().getIsSpool() )
        {
            // there is an event defined for this.
            handleElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED );
            return;
        }

        boolean diskAvailable = false;

        // SPOOL TO DISK.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            if ( aux != null && aux.getCacheType() == ICache.DISK_CACHE )
            {
                diskAvailable = true;

                if ( cacheAttr.getDiskUsagePattern() == ICompositeCacheAttributes.DISK_USAGE_PATTERN_SWAP )
                {
                    // write the last items to disk.2
                    try
                    {
                        handleElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE );

                        aux.update( ce );
                    }
                    catch ( IOException ex )
                    {
                        // impossible case.
                        log.error( "Problem spooling item to disk cache.", ex );
                        throw new IllegalStateException( ex.getMessage() );
                    }
                    catch ( Exception oee )
                    {
                        // swallow
                    }
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "spoolToDisk done for: " + ce.getKey() + " on disk cache[" + i + "]" );
                    }
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "DiskCache avaialbe, but JCS is not configured to use the DiskCache as a swap." );
                    }
                }
            }
        }

        if ( !diskAvailable )
        {
            try
            {
                handleElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE );
            }
            catch ( Exception e )
            {
                log.error( "Trouble handling the ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE  element event", e );
            }
        }
    }

    /**
     * Gets an item from the cache.
     * <p>
     * @param key
     * @return
     * @throws IOException
     * @see org.apache.jcs.engine.behavior.ICache#get(java.io.Serializable)
     */
    public ICacheElement get( Serializable key )
    {
        return get( key, false );
    }

    /**
     * Do not try to go remote or laterally for this get.
     * <p>
     * @param key
     * @return ICacheElement
     */
    public ICacheElement localGet( Serializable key )
    {
        return get( key, true );
    }

    /**
     * Look in memory, then disk, remote, or laterally for this item. The order is dependent on the
     * order in the cache.ccf file.
     * <p>
     * Do not try to go remote or laterally for this get if it is localOnly. Otherwise try to go
     * remote or lateral if such an auxiliary is configured for this region.
     * <p>
     * @param key
     * @param localOnly
     * @return ICacheElement
     */
    protected ICacheElement get( Serializable key, boolean localOnly )
    {
        ICacheElement element = null;

        boolean found = false;

        if ( log.isDebugEnabled() )
        {
            log.debug( "get: key = " + key + ", localOnly = " + localOnly );
        }

        try
        {
            // First look in memory cache
            element = memCache.get( key );

            if ( element != null )
            {
                // Found in memory cache
                if ( isExpired( element ) )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( cacheName + " - Memory cache hit, but element expired" );
                    }

                    missCountExpired++;

                    remove( key );

                    element = null;
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( cacheName + " - Memory cache hit" );
                    }

                    // Update counters
                    hitCountRam++;
                }

                found = true;
            }
            else
            {
                // Item not found in memory. If local invocation look in aux
                // caches, even if not local look in disk auxiliaries

                for ( int i = 0; i < auxCaches.length; i++ )
                {
                    AuxiliaryCache aux = auxCaches[i];

                    if ( aux != null )
                    {
                        long cacheType = aux.getCacheType();

                        if ( !localOnly || cacheType == AuxiliaryCache.DISK_CACHE )
                        {
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "Attempting to get from aux [" + aux.getCacheName() + "] which is of type: "
                                    + cacheType );
                            }

                            try
                            {
                                element = aux.get( key );
                            }
                            catch ( IOException ex )
                            {
                                log.error( "Error getting from aux", ex );
                            }
                        }

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Got CacheElement: " + element );
                        }

                        if ( element != null )
                        {
                            // Item found in one of the auxiliary caches.

                            if ( isExpired( element ) )
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( cacheName + " - Aux cache[" + i + "] hit, but element expired." );
                                }

                                missCountExpired++;

                                // This will tell the remotes to remove the item
                                // based on the element's expiration policy. The elements attributes
                                // associated with the item when it created govern its behavior
                                // everywhere.
                                remove( key );

                                element = null;
                            }
                            else
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( cacheName + " - Aux cache[" + i + "] hit" );
                                }

                                // Update counters

                                hitCountAux++;
                                auxHitCountByIndex[i]++;

                                // Spool the item back into memory
                                // only spool if the mem cache size is greater
                                // than 0, else the item will immediately get put
                                // into purgatory
                                if ( memCache.getCacheAttributes().getMaxObjects() > 0 )
                                {
                                    memCache.update( element );
                                }
                                else
                                {
                                    if ( log.isDebugEnabled() )
                                    {
                                        log.debug( "Skipping memory update since no items are allowed in memory" );
                                    }
                                }
                            }

                            found = true;

                            break;
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem encountered getting element.", e );
        }

        if ( !found )
        {
            missCountNotFound++;

            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + " - Miss" );
            }
        }

        return element;
    }

    /**
     * Determine if the element has exceeded its max life.
     * <p>
     * @param element
     * @return true if the element is expired, else false.
     */
    private boolean isExpired( ICacheElement element )
    {
        try
        {
            IElementAttributes attributes = element.getElementAttributes();

            if ( !attributes.getIsEternal() )
            {
                long now = System.currentTimeMillis();

                // Remove if maxLifeSeconds exceeded

                long maxLifeSeconds = attributes.getMaxLifeSeconds();
                long createTime = attributes.getCreateTime();

                if ( maxLifeSeconds != -1 && ( now - createTime ) > ( maxLifeSeconds * 1000 ) )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Exceeded maxLife: " + element.getKey() );
                    }

                    return true;
                }
                long idleTime = attributes.getIdleTime();
                long lastAccessTime = attributes.getLastAccessTime();

                // Remove if maxIdleTime exceeded
                // If you have a 0 size memory cache, then the last access will
                // not get updated.
                // you will need to set the idle time to -1.

                if ( ( idleTime != -1 ) && ( now - lastAccessTime ) > ( idleTime * 1000 ) )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.info( "Exceeded maxIdle: " + element.getKey() );
                    }

                    return true;
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Error determining expiration period, expiring", e );

            return true;
        }

        return false;
    }

    /**
     * Gets the set of keys of objects currently in the group.
     * <p>
     * @param group
     * @return A Set of keys, or null.
     */
    public Set getGroupKeys( String group )
    {
        HashSet allKeys = new HashSet();
        allKeys.addAll( memCache.getGroupKeys( group ) );
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            AuxiliaryCache aux = auxCaches[i];
            if ( aux != null )
            {
                try
                {
                    allKeys.addAll( aux.getGroupKeys( group ) );
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
        return allKeys;
    }

    /**
     * Removes an item from the cache.
     * <p>
     * @param key
     * @return
     * @throws IOException
     * @see org.apache.jcs.engine.behavior.ICache#remove(java.io.Serializable)
     */
    public boolean remove( Serializable key )
    {
        return remove( key, false );
    }

    /**
     * Do not propogate removeall laterally or remotely.
     * <p>
     * @param key
     * @return true if the item was already in the cache.
     */
    public boolean localRemove( Serializable key )
    {
        return remove( key, true );
    }

    /**
     * fromRemote: If a remove call was made on a cache with both, then the remote should have been
     * called. If it wasn't then the remote is down. we'll assume it is down for all. If it did come
     * from the remote then the cache is remotely configured and lateral removal is unncessary. If
     * it came laterally then lateral removal is unnecessary. Does this assume that there is only
     * one lateral and remote for the cache? Not really, the intial removal should take care of the
     * problem if the source cache was similiarly configured. Otherwise the remote cache, if it had
     * no laterals, would remove all the elements from remotely configured caches, but if those
     * caches had some other wierd laterals that were not remotely configured, only laterally
     * propagated then they would go out of synch. The same could happen for multiple remotes. If
     * this looks necessary we will need to build in an identifier to specify the source of a
     * removal.
     * <p>
     * @param key
     * @param localOnly
     * @return true if the item was in the cache, else false
     */
    protected synchronized boolean remove( Serializable key, boolean localOnly )
    {
        // not thread safe, but just for debugging and testing.
        removeCount++;

        boolean removed = false;

        try
        {
            removed = memCache.remove( key );
        }
        catch ( IOException e )
        {
            log.error( e );
        }

        // Removes from all auxiliary caches.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            if ( aux == null )
            {
                continue;
            }

            int cacheType = aux.getCacheType();

            // for now let laterals call remote remove but not vice versa

            if ( localOnly && ( cacheType == REMOTE_CACHE || cacheType == LATERAL_CACHE ) )
            {
                continue;
            }
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "Removing " + key + " from cacheType" + cacheType );
                }

                boolean b = aux.remove( key );

                // Don't take the remote removal into account.
                if ( !removed && cacheType != REMOTE_CACHE )
                {
                    removed = b;
                }
            }
            catch ( IOException ex )
            {
                log.error( "Failure removing from aux", ex );
            }
        }
        return removed;
    }

    /**
     * Clears the region. This command will be sent to all auxiliaries. Some auxiliaries, such as
     * the JDBC disk cache, can be configured to not honor removeAll requests.
     * <p>
     * @see org.apache.jcs.engine.behavior.ICache#removeAll()
     */
    public void removeAll()
        throws IOException
    {
        removeAll( false );
    }

    /**
     * Will not pass the remove message remotely.
     * <p>
     * @throws IOException
     */
    public void localRemoveAll()
        throws IOException
    {
        removeAll( true );
    }

    /**
     * Removes all cached items.
     * <p>
     * @param localOnly must pass in false to get remote and lateral aux's updated. This prevents
     *            looping.
     * @throws IOException
     */
    protected synchronized void removeAll( boolean localOnly )
        throws IOException
    {
        try
        {
            memCache.removeAll();

            if ( log.isDebugEnabled() )
            {
                log.debug( "Removed All keys from the memory cache." );
            }
        }
        catch ( IOException ex )
        {
            log.error( "Trouble updating memory cache.", ex );
        }

        // Removes from all auxiliary disk caches.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            int cacheType = aux.getCacheType();

            if ( aux != null && ( cacheType == ICache.DISK_CACHE || !localOnly ) )
            {
                try
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Removing All keys from cacheType" + cacheType );
                    }

                    aux.removeAll();
                }
                catch ( IOException ex )
                {
                    log.error( "Failure removing all from aux", ex );
                }
            }
        }
        return;
    }

    /**
     * Flushes all cache items from memory to auxilliary caches and close the auxilliary caches.
     */
    public void dispose()
    {
        dispose( false );
    }

    /**
     * Invoked only by CacheManager. This method disposes of the auxiliaries one by one. For the disk cache, the items in memory
     * are freed, meaning that they will be sent through the overflow chanel to disk.  After the
     * auxiliaries are disposed, the memory cache is dispposed.
     * <p>
     * @param fromRemote
     */
    public synchronized void dispose( boolean fromRemote )
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "In DISPOSE, [" + this.cacheName + "] fromRemote [" + fromRemote + "] \n" + this.getStats() );
        }

        // If already disposed, return immediately
        if ( !alive )
        {
            return;
        }
        alive = false;

        // Dispose of each auxilliary cache, Remote auxilliaries will be
        // skipped if 'fromRemote' is true.

        for ( int i = 0; i < auxCaches.length; i++ )
        {
            try
            {
                ICache aux = auxCaches[i];

                // Skip this auxilliary if:
                // - The auxilliary is null
                // - The auxilliary is not alive
                // - The auxilliary is remote and the invocation was remote

                if ( aux == null || aux.getStatus() != CacheConstants.STATUS_ALIVE
                    || ( fromRemote && aux.getCacheType() == REMOTE_CACHE ) )
                {
                    if ( log.isInfoEnabled() )
                    {
                        log.info( "In DISPOSE, [" + this.cacheName + "] SKIPPING auxiliary [" + aux + "] fromRemote ["
                            + fromRemote + "]" );
                    }
                    continue;
                }

                if ( log.isInfoEnabled() )
                {
                    log.info( "In DISPOSE, [" + this.cacheName + "] auxiliary [" + aux + "]" );
                }

                // IT USED TO BE THE CASE THAT (If the auxilliary is not a lateral, or the cache
                // attributes
                // have 'getUseLateral' set, all the elements currently in
                // memory are written to the lateral before disposing)
                // I changed this. It was excessive. Only the disk cache needs the items, since only
                // the disk cache
                // is in a situation to not get items on a put.

                if ( aux.getCacheType() == ICacheType.DISK_CACHE )
                {
                    int numToFree = memCache.getSize();
                    memCache.freeElements( numToFree );

                    if ( log.isInfoEnabled() )
                    {
                        log.info( "In DISPOSE, [" + this.cacheName + "] put " + numToFree + " into auxiliary " + aux );
                    }
                }

                // Dispose of the auxiliary
                aux.dispose();
            }
            catch ( IOException ex )
            {
                log.error( "Failure disposing of aux.", ex );
            }
        }

        if ( log.isInfoEnabled() )
        {
            log.info( "In DISPOSE, [" + this.cacheName + "] disposing of memory cache." );
        }
        try
        {
            memCache.dispose();
        }
        catch ( IOException ex )
        {
            log.error( "Failure disposing of memCache", ex );
        }
    }

    /**
     * Calling save cause the entire contents of the memory cache to be flushed to all auxiliaries.
     * Though this put is extremely fast, this could bog the cache and should be avoided. The
     * dispose method should call a version of this. Good for testing.
     */
    public void save()
    {
        if ( !alive )
        {
            return;
        }
        synchronized ( this )
        {
            if ( !alive )
            {
                return;
            }
            alive = false;

            for ( int i = 0; i < auxCaches.length; i++ )
            {
                try
                {
                    ICache aux = auxCaches[i];

                    if ( aux.getStatus() == CacheConstants.STATUS_ALIVE )
                    {

                        Iterator itr = memCache.getIterator();

                        while ( itr.hasNext() )
                        {
                            Map.Entry entry = (Map.Entry) itr.next();

                            ICacheElement ce = (ICacheElement) entry.getValue();

                            aux.update( ce );
                        }
                    }
                }
                catch ( IOException ex )
                {
                    log.error( "Failure saving aux caches.", ex );
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called save for [" + cacheName + "]" );
        }
    }

    /**
     * Gets the size attribute of the Cache object. This return the number of elements, not the byte
     * size.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return memCache.getSize();
    }

    /**
     * Gets the cacheType attribute of the Cache object.
     * <p>
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return CACHE_HUB;
    }

    /**
     * Gets the status attribute of the Cache object.
     * <p>
     * @return The status value
     */
    public int getStatus()
    {
        return alive ? CacheConstants.STATUS_ALIVE : CacheConstants.STATUS_DISPOSED;
    }

    /**
     * Gets stats for debugging.
     * <p>
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * This returns data gathered for this region and all the auxiliaries it currently uses.
     * <p>
     * @return Statistics and Info on the Region.
     */
    public ICacheStats getStatistics()
    {
        ICacheStats stats = new CacheStats();
        stats.setRegionName( this.getCacheName() );

        // store the composite cache stats first
        IStatElement[] elems = new StatElement[2];
        elems[0] = new StatElement();
        elems[0].setName( "HitCountRam" );
        elems[0].setData( "" + getHitCountRam() );

        elems[1] = new StatElement();
        elems[1].setName( "HitCountAux" );
        elems[1].setData( "" + getHitCountAux() );

        // store these local stats
        stats.setStatElements( elems );

        // memory + aux, memory is not considered an auxiliary internally
        int total = auxCaches.length + 1;
        IStats[] auxStats = new Stats[total];

        auxStats[0] = getMemoryCache().getStatistics();

        for ( int i = 0; i < auxCaches.length; i++ )
        {
            AuxiliaryCache aux = auxCaches[i];
            auxStats[i + 1] = aux.getStatistics();
        }

        // sore the auxiliary stats
        stats.setAuxiliaryCacheStats( auxStats );

        return stats;
    }

    /**
     * Gets the cacheName attribute of the Cache object. This is also known as the region name.
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Gets the default element attribute of the Cache object This returna a copy. It does not
     * return a reference to the attributes.
     * <p>
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes()
    {
        if ( attr != null )
        {
            return attr.copy();
        }
        return null;
    }

    /**
     * Sets the default element attribute of the Cache object.
     * <p>
     * @param attr
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = attr;
    }

    /**
     * Gets the ICompositeCacheAttributes attribute of the Cache object.
     * <p>
     * @return The ICompositeCacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttr;
    }

    /**
     * Sets the ICompositeCacheAttributes attribute of the Cache object.
     * <p>
     * @param cattr The new ICompositeCacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheAttr = cattr;
        // need a better way to do this, what if it is in error
        this.memCache.initialize( this );
    }

    /**
     * Gets the elementAttributes attribute of the Cache object.
     * <p>
     * @param key
     * @return The elementAttributes value
     * @exception CacheException
     * @exception IOException
     */
    public IElementAttributes getElementAttributes( Serializable key )
        throws CacheException, IOException
    {
        CacheElement ce = (CacheElement) get( key );
        if ( ce == null )
        {
            throw new ObjectNotFoundException( "key " + key + " is not found" );
        }
        return ce.getElementAttributes();
    }

    /**
     * Create the MemoryCache based on the config parameters. TODO: consider making this an
     * auxiliary, despite its close tie to the CacheHub. TODO: might want to create a memory cache
     * config file separate from that of the hub -- ICompositeCacheAttributes
     * <p>
     * @param cattr
     */
    private void createMemoryCache( ICompositeCacheAttributes cattr )
    {
        if ( memCache == null )
        {
            try
            {
                Class c = Class.forName( cattr.getMemoryCacheName() );
                memCache = (MemoryCache) c.newInstance();
                memCache.initialize( this );
            }
            catch ( Exception e )
            {
                log.warn( "Failed to init mem cache, using: LRUMemoryCache", e );

                this.memCache = new LRUMemoryCache();
                this.memCache.initialize( this );
            }
        }
        else
        {
            log.warn( "Refusing to create memory cache -- already exists." );
        }
    }

    /**
     * Access to the memory cache for instrumentation.
     * <p>
     * @return the MemoryCache implementation
     */
    public MemoryCache getMemoryCache()
    {
        return memCache;
    }

    /**
     * Number of times a requested item was found in the memory cache.
     * <p>
     * @return number of hits in memory
     */
    public int getHitCountRam()
    {
        return hitCountRam;
    }

    /**
     * Number of times a requested item was found in and auxiliary cache.
     * @return number of auxiliary hits.
     */
    public int getHitCountAux()
    {
        return hitCountAux;
    }

    /**
     * Number of times a requested element was not found.
     * @return number of misses.
     */
    public int getMissCountNotFound()
    {
        return missCountNotFound;
    }

    /**
     * Number of times a requested element was found but was expired.
     * @return number of found but expired gets.
     */
    public int getMissCountExpired()
    {
        return missCountExpired;
    }

    /**
     * If there are event handlers for the item, then create an event and queue it up.
     * <p>
     * This does not call handle directly; instead the handler and the event are put into a queue.
     * This prevents the event handling from blocking normal cache operations.
     * @param ce
     * @param eventType
     */
    private void handleElementEvent( ICacheElement ce, int eventType )
    {
        // handle event, might move to a new method
        ArrayList eventHandlers = ce.getElementAttributes().getElementEventHandlers();
        if ( eventHandlers != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Element Handlers are registered.  Create event type " + eventType );
            }
            IElementEvent event = new ElementEvent( ce, eventType );
            Iterator hIt = eventHandlers.iterator();
            while ( hIt.hasNext() )
            {
                IElementEventHandler hand = (IElementEventHandler) hIt.next();
                try
                {
                    addElementEvent( hand, event );
                }
                catch ( Exception e )
                {
                    log.error( "Trouble adding element event to queue", e );
                }
            }
        }
    }

    /**
     * Adds an ElementEvent to be handled to the queue.
     * @param hand The IElementEventHandler
     * @param event The IElementEventHandler IElementEvent event
     * @exception IOException Description of the Exception
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Adding event to Element Event Queue" );
        }
        elementEventQ.addElementEvent( hand, event );
    }

    /**
     * @param updateCount The updateCount to set.
     */
    public void setUpdateCount( int updateCount )
    {
        this.updateCount = updateCount;
    }

    /**
     * @return Returns the updateCount.
     */
    public int getUpdateCount()
    {
        return updateCount;
    }

    /**
     * @param removeCount The removeCount to set.
     */
    public void setRemoveCount( int removeCount )
    {
        this.removeCount = removeCount;
    }

    /**
     * @return Returns the removeCount.
     */
    public int getRemoveCount()
    {
        return removeCount;
    }

    /**
     * This returns the stats.
     * <p>
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getStats();
    }
}
