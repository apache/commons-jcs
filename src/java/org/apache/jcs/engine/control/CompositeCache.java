package org.apache.jcs.engine.control;


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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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

import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;

import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventQueue;
import org.apache.jcs.engine.control.event.ElementEventQueue;
import org.apache.jcs.engine.control.group.GroupId;

/**
 *  This is the primary hub for a single cache/region. It control the flow of
 *  items through the cache. The auxiliary and memory caches are plugged in
 *  here.
 *
 *@version    $Id$
 */
public class CompositeCache
     implements ICache, Serializable
{
    private final static Log log = LogFactory.getLog( CompositeCache.class );

    /**
     *  EventQueue for handling element events.
     *  1 should be enough for all the regions. Else should create as needed
     *  per region.
     */
    public static IElementEventQueue elementEventQ = new ElementEventQueue( "AllRegionQueue" );

    // Auxiliary caches.
    private AuxiliaryCache[] auxCaches;

    private boolean alive = true;

    // this is int he cacheAttr, shouldn't be used, remove
    final String cacheName;

    /**
     *  Region Elemental Attributes, default
     */
    public IElementAttributes attr;

    /**
     *  Cache Attributes, for hub and memory auxiliary
     */
    public ICompositeCacheAttributes cacheAttr;

    // Statistics

    /** Memory cache hit count */
    private int hitCountRam;

    /** Auxiliary cache hit count (number of times found in ANY auxiliary) */
    private int hitCountAux;

    /** Auxiliary hit counts broken down by auxiliary */
    private int[] auxHitCountByIndex;

    /** Count of misses where element was not found */
    private int missCountNotFound = 0;

    /** Count of misses where element was expired */
    private int missCountExpired = 0;

    /**
     *  The cache hub can only have one memory cache. This could be made more
     *  flexible in the future, but they are tied closely together. More than
     *  one doesn't make much sense.
     */
    MemoryCache memCache;

    /**
     *  Constructor for the Cache object
     *
     *@param  cacheName  The name of the region
     *@param  auxCaches  The auxiliary caches to be used by this region
     *@param  cattr      The cache attribute
     *@param  attr       The default element attributes
     */
    public CompositeCache( String cacheName,
                           ICompositeCacheAttributes cattr,
                           IElementAttributes attr )
    {
        this.cacheName = cacheName;

        this.attr = attr;
        this.cacheAttr = cattr;

        createMemoryCache( cattr );

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructed cache with name " + cacheName +
                " and cache attributes: " + cattr );
        }
        else if ( log.isInfoEnabled() )
        {
            log.info( "Constructed cache with name: " + cacheName );
        }
    }

    public void setAuxCaches( AuxiliaryCache[] auxCaches )
    {
        this.auxCaches = auxCaches;

        if ( auxCaches != null )
        {
            this.auxHitCountByIndex = new int[auxCaches.length];
        }
    }

    /**
     *  Standard update method
     *
     *@param  ce
     *@exception  IOException
     */
    public synchronized void update( ICacheElement ce )
        throws IOException
    {
        update( ce, false );
    }

    /**
     *  Standard update method
     *
     *@param  ce
     *@exception  IOException
     */
    public synchronized void localUpdate( ICacheElement ce )
        throws IOException
    {
        update( ce, true );
    }

    /**
     *  Description of the Method
     *
     *@param  ce
     *@param  updateRemoteCache
     *@exception  IOException
     */
    protected synchronized void update( ICacheElement ce, boolean localOnly )
        throws IOException
    {

        if ( ce.getKey() instanceof String
             && ce.getKey().toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            throw new IllegalArgumentException( "key must not end with "
                 + CacheConstants.NAME_COMPONENT_DELIMITER
                 + " for a put operation" );
        }
        else if ( ce.getKey() instanceof GroupId )
        {
            throw new IllegalArgumentException( "key cannot be a GroupId "
                 + " for a put operation" );
        }

        log.debug( "Updating memory cache" );

        memCache.update( ce );

        // Updates to all auxiliary caches -- remote and laterals, can add as many of each
        // as necessary.
        // could put the update criteria in each but it would a bit cumbersome
        // the disk cache would have to check the cache size, the lateral
        // would have to check the region cattr configuration

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

            // SEND TO REMOTE STORE
            if ( aux != null && aux.getCacheType() == ICache.REMOTE_CACHE )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "ce.getElementAttributes().getIsRemote() = "
                         + ce.getElementAttributes().getIsRemote() );
                }

                if ( ce.getElementAttributes().getIsRemote()
                     && ! localOnly )
                {
                    try
                    {
                        // need to make sure the group cache understands that the
                        // key is a group attribute on update
                        aux.update( ce );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Updated remote store for "
                                 + ce.getKey() + ce );
                        }
                    }
                    catch ( IOException ex )
                    {
                        log.error( "Failure in updateExclude", ex );
                    }
                }
                // SEND LATERALLY
            }
            else if ( aux != null
                 && aux.getCacheType() == ICache.LATERAL_CACHE )
            {
                // lateral can't do the checking since it is dependent on the cache region
                // restrictions
                if ( log.isDebugEnabled() )
                {
                    log.debug( "lateralcache in aux list: cattr " +
                        cacheAttr.getUseLateral() );
                }
                if ( cacheAttr.getUseLateral()
                     && ce.getElementAttributes().getIsLateral()
                     && ! localOnly )
                {
                    // later if we want a multicast, possibly delete abnormal broadcaster
                    // DISTRIBUTE LATERALLY
                    // Currently always multicast even if the value is unchanged,
                    // just to cause the cache item to move to the front.
                    aux.update( ce );
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "updated lateral cache for " + ce.getKey() );
                    }
                }
            }
            else if ( aux != null && aux.getCacheType() == ICache.DISK_CACHE )
            {
                // do nothing, the memory manager will call spool where necesary
                // TODO: add option to put all element on disk
            }
        }

        return;
    }

    /**
     *  Writes the specified element to any disk auxilliaries Might want to
     *  rename this "overflow" incase the hub wants to do something else.
     *
     *@param  ce  The CacheElement
     */
    public void spoolToDisk( ICacheElement ce )
    {

        boolean diskAvailable = false;

        // SPOOL TO DISK.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            if ( aux != null && aux.getCacheType() == ICache.DISK_CACHE )
            {

                diskAvailable = true;

                // write the last item to disk.2
                try
                {
                    // handle event, might move to a new method
                    ArrayList eventHandlers = ce.getElementAttributes().getElementEventHandlers();
                    if ( eventHandlers != null )
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Handlers are registered.  Event -- ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE" );
                        }
                        IElementEvent event = new ElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE );
                        Iterator hIt = eventHandlers.iterator();
                        while ( hIt.hasNext() )
                        {
                            IElementEventHandler hand = ( IElementEventHandler ) hIt.next();
                            //hand.handleElementEvent( event );
                            addElementEvent( hand, event );
                        }
                    }

                    aux.update( ce );
                }
                catch ( IOException ex )
                {
                    // impossible case.
                    ex.printStackTrace();
                    throw new IllegalStateException( ex.getMessage() );
                }
                catch ( Exception oee )
                {
                }
                if ( log.isDebugEnabled() )
                {
                    log.debug( "spoolToDisk done for: " + ce.getKey() + " on disk cache[" + i + "]" );
                }
            }
        }

        if ( !diskAvailable )
        {

            try
            {

                // handle event, might move to a new method
                ArrayList eventHandlers = ce.getElementAttributes().getElementEventHandlers();
                if ( eventHandlers != null )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Handlers are registered.  Event -- ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE" );
                    }
                    IElementEvent event = new ElementEvent( ce, IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE );
                    Iterator hIt = eventHandlers.iterator();
                    while ( hIt.hasNext() )
                    {
                        IElementEventHandler hand = ( IElementEventHandler ) hIt.next();
                        //hand.handleElementEvent( event );
                        addElementEvent( hand, event );
                    }
                }

            }
            catch ( Exception e )
            {
                log.error( "Trouble handling the event", e );
            }

        }

    }

    /**
     * @see ICache#get
     */
    public ICacheElement get( Serializable key )
    {
        return get( key, false );
    }

    /**
     * @see ICompositeCache#localGet
     */
    public ICacheElement localGet( Serializable key )
    {
        return get( key, true );
    }


    /**
     *  Description of the Method
     *
     *@param  key
     *@param  localOnly
     *@return
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
                        log.debug( cacheName +
                                   " - Memory cache hit, but element expired" );
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

                        if ( ! localOnly || cacheType == AuxiliaryCache.DISK_CACHE )
                        {
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "Attempting to get from aux: "
                                     + aux.getCacheName()
                                     + " which is of type: "
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
                                    log.debug( cacheName + " - Aux cache[" + i
                                               + "] hit, but element expired" );
                                }

                                missCountExpired++;

                                remove( key );

                                element = null;
                            }
                            else
                            {
                                if ( log.isDebugEnabled() )
                                {
                                    log.debug( cacheName +
                                               " - Aux cache[" + i + "] hit" );
                                }

                                // Update counters

                                hitCountAux++;
                                auxHitCountByIndex[ i ]++;

                                // Spool the item back into memory

                                memCache.update( element );
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
            log.error( e );
        }

        if ( ! found )
        {
            missCountNotFound++;

            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + " - Miss" );
            }
        }

        return element;
    }

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

                if ( maxLifeSeconds != -1
                     && ( now - createTime ) > ( maxLifeSeconds * 1000 ) )
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
                // FIXME: Does this really belong here?

                if ( ( idleTime != -1 )
                     && ( now - lastAccessTime ) > ( idleTime * 1000 ) )
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
     * Gets the set of keys of objects currently in the group
     */
    public Set getGroupKeys(String group)
    {
        HashSet allKeys = new HashSet();
        allKeys.addAll(memCache.getGroupKeys(group));
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            AuxiliaryCache aux = auxCaches[i];
            if ( aux != null )
            {
                try {
                    allKeys.addAll(aux.getGroupKeys(group));
                }
                catch ( IOException e )
                {
                    //ignore
                }
            }
        }
        return allKeys;
    }


    /**
     * @see ICache#remove
     */
    public boolean remove( Serializable key )
    {
        return remove( key, false );
    }

    /**
     * @see ICompositeCache#localRemove
     */
    public boolean localRemove( Serializable key )
    {
        return remove( key, true );
    }

    /**
     *  fromRemote: If a remove call was made on a cache with both, then the
     *  remote should have been called. If it wasn't then the remote is down.
     *  we'll assume it is down for all. If it did come from the remote then the
     *  caceh is remotely configured and lateral removal is unncessary. If it
     *  came laterally then lateral removal is unnecessary. Does this assumes
     *  that there is only one lateral and remote for the cache? Not really, the
     *  intial removal should take care of the problem if the source cache was
     *  similiarly configured. Otherwise the remote cache, if it had no
     *  laterals, would remove all the elements from remotely configured caches,
     *  but if those caches had some other wierd laterals that were not remotely
     *  configured, only laterally propagated then they would go out of synch.
     *  The same could happen for multiple remotes. If this looks necessary we
     *  will need to build in an identifier to specify the source of a removal.
     *
     *@param  key
     *@param  localOnly
     *@return
     */
    protected synchronized boolean remove( Serializable key,
                                           boolean localOnly )
    {
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
     * @see ICache#removeAll
     */
    public void removeAll()
        throws IOException
    {
        removeAll( false );
    }

    /**
     * @see ICompositeCache#removeAll
     */
    public void localRemoveAll()
        throws IOException
    {
        removeAll( true );
    }

    /**
     * Removes all cached items.
     */
    protected synchronized void removeAll( boolean localOnly )
        throws IOException
    {

        try
        {
            memCache.removeAll();
        }
        catch ( IOException ex )
        {
            log.error( ex );
        }

        // Removes from all auxiliary disk caches.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[i];

            int cacheType = aux.getCacheType();

            if ( aux != null
                 && ( cacheType == ICache.DISK_CACHE || ! localOnly ) )
            {
                try
                {
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
     *  Flushes all cache items from memory to auxilliary caches and close the
     *  auxilliary caches.
     */
    public void dispose()
    {
        dispose( false );
    }

    /**
     *  invoked only by CacheManager.
     *
     *@param  fromRemote
     */
    protected synchronized void dispose( boolean fromRemote )
    {
        // If already disposed, return immediately

        if ( ! alive )
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
                //   - The auxilliary is null
                //   - The auxilliary is not alive
                //   - The auxilliary is remote and the invocation was remote

                if ( aux == null
                     || aux.getStatus() != CacheConstants.STATUS_ALIVE
                     || fromRemote && aux.getCacheType() == REMOTE_CACHE )
                {
                    continue;
                }

                // If the auxilliary is not a lateral, or the cache attributes
                // have 'getUseLateral' set, all the elements currently in
                // memory are written to the lateral before disposing

                if ( aux.getCacheType() != ICacheType.LATERAL_CACHE
                     || this.cacheAttr.getUseLateral() )
                {
                    Iterator itr = memCache.getIterator();

                    while ( itr.hasNext() )
                    {
                        Map.Entry entry = ( Map.Entry ) itr.next();

                        ICacheElement ce = (ICacheElement) entry.getValue();
                        try
                        {
                            if ( aux.getCacheType() == ICacheType.LATERAL_CACHE
                                 && !ce.getElementAttributes().getIsLateral() )
                            {
                                continue;
                            }
                            aux.update( ce );
                        }
                        catch ( Exception e )
                        {
                            log.error( e );
                        }
                    }
                }

                // Dispose of the auxiliary

                aux.dispose();
            }
            catch ( IOException ex )
            {
                log.error( "Failure disposing of aux", ex );
            }

        }

        try {
          memCache.dispose();
        }
        catch ( IOException ex )
        {
            log.error( "Failure disposing of memCache", ex );
        }

        log.warn( "Called close for " + cacheName );

    }

    /**
     *  Though this put is extremely fast, this could bog the cache and should
     *  be avoided. The dispose method should call a version of this. Good for
     *  testing.
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
                            Map.Entry entry = ( Map.Entry ) itr.next();

                            ICacheElement ce = (ICacheElement) entry.getValue();

                            aux.update(ce);
                        }
                    }
                }
                catch ( IOException ex )
                {
                    log.error( "Failure saving aux caches", ex );
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called save for " + cacheName );
        }
    }

    /**
     *  Gets the size attribute of the Cache object
     *
     *@return    The size value
     */
    public int getSize()
    {
        return memCache.getSize();
    }

    /**
     *  Gets the cacheType attribute of the Cache object
     *
     *@return    The cacheType value
     */
    public int getCacheType()
    {
        return CACHE_HUB;
    }

    /**
     *  Gets the status attribute of the Cache object
     *
     *@return    The status value
     */
    public int getStatus()
    {
        return alive ? CacheConstants.STATUS_ALIVE : CacheConstants.STATUS_DISPOSED;
    }

    /**
     *  Gets the cacheName attribute of the Cache object
     *
     *@return    The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     *  Gets the default element attribute of the Cache object
     *
     * Should this return a copy?
     *
     *@return    The attributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return attr;
    }

    /**
     *  Sets the default element attribute of the Cache object
     *
     *@return    The attributes value
     */
    public void setElementAttributes( IElementAttributes attr)
    {
        this.attr = attr;
    }


    /**
     *  Gets the ICompositeCacheAttributes attribute of the Cache object
     *
     *@return    The ICompositeCacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttr;
    }

    /**
     *  Sets the ICompositeCacheAttributes attribute of the Cache object
     *
     *@param  cattr  The new ICompositeCacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheAttr = cattr;
        // need a better way to do this, what if it is in error
        this.memCache.initialize( this );
    }

    /**
     *  Gets the elementAttributes attribute of the Cache object
     *
     *@param  key
     *@return                     The elementAttributes value
     *@exception  CacheException
     *@exception  IOException
     */
    public IElementAttributes getElementAttributes( Serializable key )
        throws CacheException, IOException
    {
        CacheElement ce = ( CacheElement ) get( key );
        if ( ce == null )
        {
            throw new ObjectNotFoundException( "key " + key + " is not found" );
        }
        return ce.getElementAttributes();
    }


    /**
     *  Adds an ElementEvent to be handled
     *
     *@param  hand             The IElementEventHandler
     *@param  event            The IElementEventHandler IElementEvent event
     *@exception  IOException  Description of the Exception
     */
    public void addElementEvent( IElementEventHandler hand, IElementEvent event )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Adding to Q" );
        }
        elementEventQ.addElementEvent( hand, event );
    }


    /**
     *  Create the MemoryCache based on the config parameters. TODO: consider
     *  making this an auxiliary, despite its close tie to the CacheHub. TODO:
     *  might want to create a memory cache config file separate from that of
     *  the hub -- ICompositeCacheAttributes
     *
     *@param  cattr
     */
    private void createMemoryCache( ICompositeCacheAttributes cattr )
    {
        if ( memCache == null )
        {
            try
            {
                Class c = Class.forName( cattr.getMemoryCacheName() );
                memCache = ( MemoryCache ) c.newInstance();
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

    // ---------------------------------------------------- For Instrumentation

    /** Access to the memory cache for instrumentation. */
    public MemoryCache getMemoryCache()
    {
        return memCache;
    }

    /** Number of times a requested item was found in the memory cache */
    public int getHitCountRam()
    {
        return hitCountRam;
    }

    /** Number of times a requested item was found in and auxiliary cache */
    public int getHitCountAux()
    {
        return hitCountAux;
    }

    /** Number of times a requested element was not found */
    public int getMissCountNotFound()
    {
        return missCountNotFound;
    }

    /** Number of times a requested element was found but was expired */
    public int getMissCountExpired()
    {
        return missCountExpired;
    }
}
