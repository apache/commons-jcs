package org.apache.jcs.engine.control;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;

/**
 * This is the primary hub for a single cache/region. It control the flow of
 * items through the cache. The auxiliary and memory caches are plugged in
 * here.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @version $Id$
 */
public class Cache
    implements ICache, ICompositeCache, Serializable
{
    private final static Log log = LogFactory.getLog( Cache.class );

    // Auxiliary caches.
    private ICache[] auxCaches;
    // track hit counts for each
    private int[] auxHit;

    private boolean alive = true;

    // this is int he cacheAttr, shouldn't be used, remove
    final String cacheName;

    /**
     * Region Elemental Attributes, default
     */
    public IElementAttributes attr;

    /**
     * Cache Attributes, for hub and memory auxiliary
     */
    public ICompositeCacheAttributes cacheAttr;

    // statistics
    private static int numInstances;
    private int ramHit;
    private int miss;

    /**
     * The cache hub can only have one memory cache. This could be made more
     * flexible in the future, but they are tied closely together. More than one
     * doesn't make much sense.
     */
    MemoryCache memCache;

    /**
     * Constructor for the Cache object
     *
     * @param cacheName The name of the region
     * @param auxCaches The auxiliary caches to be used by this region
     * @param cattr The cache attribute
     * @param attr The default element attributes
     */
    public Cache( String cacheName,
                  ICache[] auxCaches,
                  ICompositeCacheAttributes cattr,
                  IElementAttributes attr )
    {
        numInstances++;

        this.cacheName = cacheName;

        this.auxCaches = auxCaches;
        if ( auxCaches != null )
        {
            this.auxHit = new int[ auxCaches.length ];
        }

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

    /**
     * Description of the Method
     *
     * @deprecated
     * @see this will become protected
     * @param ce
     */
    public void add( ICacheElement ce )
    {
        try
        {
            memCache.update( ce );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        return;
    }

    /**
     * Will no override existing items.
     *
     * @param key
     * @param val
     * @exception IOException
     * @exception ObjectExistsException
     */
    public void putSafe( Serializable key, Serializable val )
        throws IOException, ObjectExistsException
    {
        if ( this.get( key ) != null )
        {
            throw new ObjectExistsException( "Object exists for key " + key );
        }
        else
        {
            put( key, val, ( IElementAttributes ) this.attr.copy() );
        }
        return;
    }

    /**
     * Put in cache and configured auxiliaries.
     *
     * @param key
     * @param val
     * @exception IOException
     */
    public void put( Serializable key, Serializable val )
        throws IOException
    {
        put( key, val, ( IElementAttributes ) this.attr.copy() );
        return;
    }

    /**
     * Description of the Method
     *
     * @param key Cache key
     * @param val Value to cache
     * @param attr Element attributes
     * @exception IOException
     */
    public void put( Serializable key,
                     Serializable val,
                     IElementAttributes attr )
        throws IOException
    {

        if ( key == null || val == null )
        {
            NullPointerException npe =
                new NullPointerException( "key=" + key + " and val=" + val +
                                          " must not be null." );

            log.error( "Key or value was null. Exception will be thrown", npe );

            throw npe;
        }

        try
        {
            updateCaches( key, val, attr );
        }
        catch ( IOException ioe )
        {
            log.error( "Failed updating caches", ioe );
        }
        return;
    }

    /**
     * Description of the Method
     *
     * @param key Cache key
     * @param val Value to cache
     * @param attr Element attributes
     * @exception IOException
     */
    protected synchronized void updateCaches( Serializable key,
                                              Serializable val,
                                              IElementAttributes attr )
        throws IOException
    {
        updateCaches( key, val, attr, CacheConstants.INCLUDE_REMOTE_CACHE );
    }

    /**
     * Description of the Method
     *
     * @param key
     * @param val
     * @param attr
     * @param updateRemoteCache
     * @exception IOException
     */
    protected synchronized void updateCaches( Serializable key,
                                              Serializable val,
                                              IElementAttributes attr,
                                              boolean updateRemoteCache )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, val );
        ce.setElementAttributes( attr );
        updateExclude( ce, updateRemoteCache );
    }

    /**
     * Standard update method
     *
     * @param ce
     * @exception IOException
     */
    public synchronized void update( ICacheElement ce )
        throws IOException
    {
        update( ce, CacheConstants.INCLUDE_REMOTE_CACHE );
    }

    /**
     * Description of the Method
     *
     * @param updateRemoteCache Should the nonlocal caches be updated
     * @param ce
     * @exception IOException
     */
    public void update( ICacheElement ce, boolean updateRemoteCache )
        throws IOException
    {
        updateExclude( ce, updateRemoteCache );
    }

    /**
     * Description of the Method
     *
     * @param ce
     * @param updateRemoteCache
     * @exception IOException
     */
    public synchronized void updateExclude( ICacheElement ce, boolean updateRemoteCache )
        throws IOException
    {

        if ( ce.getKey() instanceof String
            && ce.getKey().toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            throw new IllegalArgumentException( "key must not end with "
                                                + CacheConstants.NAME_COMPONENT_DELIMITER
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
            ICache aux = auxCaches[ i ];

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
                    && updateRemoteCache )
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
                        handleException( ex );
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
                    && updateRemoteCache )
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
     * ICacheHub method
     *
     * @param ce The CacheElement
     */
    public synchronized void spoolToDisk( ICacheElement ce )
    {

        // SPOOL TO DISK.
        for ( int i = 0; i < auxCaches.length; i++ )
        {
            ICache aux = auxCaches[ i ];

            if ( aux != null && aux.getCacheType() == ICache.DISK_CACHE )
            {
                // write the last item to disk.2
                try
                {
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
                    log.debug( "moveToMemory -- request to put " + ce.getKey() + " on disk cache[" + i + "]" );
                }
            }
        }

    }
    // end spoolToDisk

    /**
     * Gets an item from the cache, and make it the first in the link list.
     *
     * @return The cacheElement value
     * @param key
     * @exception ObjectNotFoundException
     * @exception IOException
     */
    public Serializable getCacheElement( Serializable key )
        throws ObjectNotFoundException, IOException
    {
        return get( key, CacheConstants.LOCAL_INVOKATION );
    }
    // end get ce

    /**
     * Description of the Method
     *
     * @return
     * @param key
     */
    public Serializable get( Serializable key )
    {
        return get( key, false, CacheConstants.LOCAL_INVOKATION );
    }

    /**
     * Description of the Method
     *
     * @return
     * @param key
     * @param container
     */
    public Serializable get( Serializable key, boolean container )
    {
        return get( key, container, CacheConstants.LOCAL_INVOKATION );
    }

    /**
     * Description of the Method
     *
     * @return
     * @param key
     * @param container
     * @param invocation
     */
    public Serializable get( Serializable key,
                             boolean container,
                             boolean invocation )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "in cache get(key,container)" );
        }

        ICacheElement ce = null;
        boolean found = false;

        try
        {

            if ( log.isDebugEnabled() )
            {
                log.debug( "get: key = " + key + ", is local invocation = "
                           + ( invocation == CacheConstants.LOCAL_INVOKATION ) );
            }

            ce = memCache.get( key );

            if ( ce == null )
            {
                // Item not found in memory.  Try the auxiliary caches if local.

                for ( int i = 0; i < auxCaches.length; i++ )
                {
                    ICache aux = auxCaches[ i ];

                    if ( aux != null )
                    {

                        if ( ( invocation == CacheConstants.LOCAL_INVOKATION )
                            || aux.getCacheType() == aux.DISK_CACHE )
                        {
                            if ( log.isDebugEnabled() )
                            {
                                log.debug( "get(key,container,invocation) > in local block, aux.getCacheType() = " + aux.getCacheType() );
                            }

                            try
                            {
                                ce = ( ICacheElement ) aux.get( key, true );
                            }
                            catch ( IOException ex )
                            {
                                handleException( ex );
                            }
                        }

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "ce = " + ce );
                        }

                        if ( ce != null )
                        {
                            found = true;
                            // Item found in one of the auxiliary caches.
                            auxHit[ i ]++;

                            if ( log.isDebugEnabled() )
                            {
                                log.debug( cacheName + " -- AUX[" + i + "]-HIT for " + key );
                                log.debug( "ce.getKey() = " + ce.getKey() );
                                log.debug( "ce.getVal() = " + ce.getVal() );
                            }

                            memCache.update( ce );

                            break;
                        }
                    }
                    // end for
                }
                // end if invocation = LOCAL

            }
            else
            {
                found = true;
                ramHit++;
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + " -- RAM-HIT for " + key );
                }
            }

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        try
        {
            if ( !found )
            {
                // Item not found in all caches.
                miss++;
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + " -- MISS for " + key );
                }
                return null;
            }
        }
        catch ( Exception e )
        {
            log.error( "Error handling miss", e );
            return null;
        }

        // HUB Manages expiration
        try
        {

            if ( !ce.getElementAttributes().getIsEternal() )
            {

                long now = System.currentTimeMillis();

                // Exceeded maxLifeSeconds
                if ( ( ce.getElementAttributes().getMaxLifeSeconds() != -1 ) && ( now - ce.getElementAttributes().getCreateTime() ) > ( ce.getElementAttributes().getMaxLifeSeconds() * 1000 ) )
                {
                    if ( log.isInfoEnabled() )
                    {
                        log.info( "Exceeded maxLifeSeconds -- " + ce.getKey() );
                    }
                    this.remove( key );
                    //cache.remove( me.ce.getKey() );
                    return null;
                }
                else
                // NOT SURE IF THIS REALLY BELONGS HERE.  WHAT IS THE
                // POINT OF IDLE TIME?  SEEMS OK
                // Exceeded maxIdleTime, removal
                    if ( ( ce.getElementAttributes().getIdleTime() != -1 ) && ( now - ce.getElementAttributes().getLastAccessTime() ) > ( ce.getElementAttributes().getIdleTime() * 1000 ) )
                    {
                        if ( log.isInfoEnabled() )
                        {
                            log.info( "Exceeded maxIdleTime [ ce.getElementAttributes().getIdleTime() = " + ce.getElementAttributes().getIdleTime() + " ]-- " + ce.getKey() );
                        }
                        this.remove( key );
                        //cache.remove( me.ce.getKey() );
                        return null;
                    }
            }

        }
        catch ( Exception e )
        {
            log.error( "Error determining expiration period", e );
            return null;
        }

        if ( container )
        {
            return ce;
        }
        else
        {
            return ce.getVal();
        }

    }
    // end get



    /**
     * Removes an item from the cache.
     *
     * @return
     * @param key
     */
    public boolean remove( Serializable key )
    {
        return remove( key, CacheConstants.LOCAL_INVOKATION );
    }


    /**
     * fromRemote: If a remove call was made on a cache with both, then the
     * remote should have been called. If it wasn't then the remote is down.
     * we'll assume it is down for all. If it did come from the remote then the
     * caceh is remotely configured and lateral removal is unncessary. If it
     * came laterally then lateral removal is unnecessary. Does this assumes
     * that there is only one lateral and remote for the cache? Not really, the
     * intial removal should take care of the problem if the source cache was
     * similiarly configured. Otherwise the remote cache, if it had no laterals,
     * would remove all the elements from remotely configured caches, but if
     * those caches had some other wierd laterals that were not remotely
     * configured, only laterally propagated then they would go out of synch.
     * The same could happen for multiple remotes. If this looks necessary we
     * will need to build in an identifier to specify the source of a removal.
     *
     * @return
     * @param key
     * @param nonLocal
     */
    // can't be protected because groupcache method needs to be called from access
    public synchronized boolean remove( Serializable key, boolean nonLocal )
    {

        if ( log.isDebugEnabled() )
        {
            log.debug( "remove> key=" + key + ", nonLocal=" + nonLocal );
        }

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
            ICache aux = auxCaches[ i ];

            if ( aux == null )
            {
                continue;
            }
            // avoid notification dead loop.
            int cacheType = aux.getCacheType();

            // for now let laterals call remote remove but not vice versa
            if ( nonLocal && ( cacheType == REMOTE_CACHE || cacheType == LATERAL_CACHE ) )
            {
                continue;
            }
            try
            {
                boolean b = aux.remove( key );

                // Don't take the remote removal into account.
                if ( !removed && cacheType != REMOTE_CACHE )
                {
                    removed = b;
                }
            }
            catch ( IOException ex )
            {
                handleException( ex );
            }
        }
        return removed;
    }
    // end remove

    /**
     * Removes all cached items.
     */
    public synchronized void removeAll()
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
            ICache aux = auxCaches[ i ];

            if ( aux != null && aux.getCacheType() == ICache.DISK_CACHE )
            {
                try
                {
                    aux.removeAll();
                }
                catch ( IOException ex )
                {
                    handleException( ex );
                }
            }
        }
        return;
    }

    /**
     * Flushes all cache items from memory to auxilliary caches and close the
     * auxilliary caches.
     */
    public void dispose()
    {
        dispose( CacheConstants.LOCAL_INVOKATION );
    }

    /**
     * invoked only by CacheManager.
     *
     * @param fromRemote
     */
    protected void dispose( boolean fromRemote )
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
                    ICache aux = auxCaches[ i ];

                    if ( aux == null || fromRemote && aux.getCacheType() == REMOTE_CACHE )
                    {
                        continue;
                    }
                    if ( aux.getStatus() == CacheConstants.STATUS_ALIVE )
                    {

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "size = " + memCache.getSize() );
                        }

                        if ( !( aux.getCacheType() == ICacheType.LATERAL_CACHE && !this.cacheAttr.getUseLateral() ) )
                        {

                            Iterator itr = memCache.getIterator();

                            while ( itr.hasNext() )
                            {
                                Map.Entry entry = ( Map.Entry ) itr.next();
                                Serializable key = ( Serializable ) entry.getKey();
                                MemoryElementDescriptor me = ( MemoryElementDescriptor ) entry.getValue();
                                try
                                {
                                    if ( aux.getCacheType() == ICacheType.LATERAL_CACHE && !me.ce.getElementAttributes().getIsLateral() )
                                    {
                                        continue;
                                    }
                                    aux.put( key, me.ce.getVal(), me.ce.getElementAttributes() );
                                }
                                catch ( Exception e )
                                {
                                    log.error( e );
                                }
                            }
                        }
                        if ( aux.getCacheType() == ICache.DISK_CACHE )
                        {
                            aux.dispose();
                        }
                    }
                }
                catch ( IOException ex )
                {
                    handleException( ex );
                }
            }
        }

        log.warn( "Called close for " + cacheName );

    }

    /**
     * Though this put is extremely fast, this could bog the cache and should be
     * avoided. The dispose method should call a version of this. Good for
     * testing.
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
                    ICache aux = auxCaches[ i ];

                    if ( aux.getStatus() == CacheConstants.STATUS_ALIVE )
                    {

                        Iterator itr = memCache.getIterator();

                        while ( itr.hasNext() )
                        {
                            Map.Entry entry = ( Map.Entry ) itr.next();
                            Serializable key = ( Serializable ) entry.getKey();
                            MemoryElementDescriptor me = ( MemoryElementDescriptor ) entry.getValue();
                            //try {
                            // should call update
                            aux.put( key, me.ce.getVal(), me.ce.getElementAttributes() );
                            // remove this exception from the interface
                            //} catch( Exception e ) {
                            //  log.error( e );
                            //}
                        }
                    }
                }
                catch ( IOException ex )
                {
                    handleException( ex );
                }
            }
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "Called save for " + cacheName );
        }
    }

    /**
     * Gets the stats attribute of the Cache object
     *
     * FIXME: Remove HTML!
     *
     * @return The stats value
     */
    public String getStats()
    {
        StringBuffer stats = new StringBuffer();
        stats.append( "cacheName = " + cacheName + ", numInstances = " + numInstances );
        stats.append( "<br> ramSize = " + memCache.getSize() + "/ ramHit = " + ramHit );

        for ( int i = 0; i < auxHit.length; i++ )
        {
            stats.append( "/n<br> auxHit[" + i + "] = " + auxHit[ i ] + ", " + this.auxCaches[ i ].getClass().getName() + "" );
        }
        stats.append( "/n<br> miss = " + miss );
        stats.append( "/n<br> cacheAttr = " + cacheAttr.toString() );
        return stats.toString();
    }

    /**
     * Gets the size attribute of the Cache object
     *
     * @return The size value
     */
    public int getSize()
    {
        return memCache.getSize();
    }

    /**
     * Gets the cacheType attribute of the Cache object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return CACHE_HUB;
    }

    /**
     * Gets the status attribute of the Cache object
     *
     * @return The status value
     */
    public int getStatus()
    {
        return alive ? CacheConstants.STATUS_ALIVE : CacheConstants.STATUS_DISPOSED;
    }

    /**
     * Description of the Method
     *
     * @param ex
     */
    private void handleException( IOException ex )
    {
        ex.printStackTrace();
    }

    /**
     * Gets the cacheName attribute of the Cache object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Gets the default element attribute of the Cache object
     *
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return attr;
    }

    /**
     * Gets the ICompositeCacheAttributes attribute of the Cache object
     *
     * @return The ICompositeCacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttr;
    }

    /**
     * Sets the ICompositeCacheAttributes attribute of the Cache object
     *
     * @param cattr The new ICompositeCacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheAttr = cattr;
        // need a better way to do this, what if it is in error
        this.memCache.initialize( this );
    }

    /**
     * Gets the elementAttributes attribute of the Cache object
     *
     * @return The elementAttributes value
     * @param key
     * @exception CacheException
     * @exception IOException
     */
    public IElementAttributes getElementAttributes( Serializable key )
        throws CacheException, IOException
    {
        CacheElement ce = ( CacheElement ) getCacheElement( key );
        if ( ce == null )
        {
            throw new ObjectNotFoundException( "key " + key + " is not found" );
        }
        return ce.getElementAttributes();
    }

    /**
     * Create the MemoryCache based on the config parameters. TODO: consider
     * making this an auxiliary, despite its close tie to the CacheHub. TODO:
     * might want to create a memory cache config file separate from that of the
     * hub -- ICompositeCacheAttributes
     *
     * @return
     * @param cattr
     */
    private MemoryCache createMemoryCache( ICompositeCacheAttributes cattr )
    {
        // Create memory Cache
        if ( memCache == null )
        {
            try
            {
                Class c = Class.forName( cattr.getMemoryCacheName() );
                this.memCache = ( MemoryCache ) c.newInstance();
                this.memCache.initialize( this );
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
            log.warn( "Trying to create a memory cache after it already exists." );
        }
        return memCache;
    }
    // end createMemoryCache

}
