package org.apache.jcs.auxiliary.disk;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.CacheInfo;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.auxiliary.behavior.IAuxiliaryCacheAttributes;
import org.apache.jcs.utils.locking.ReadWriteLock;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Abstract class providing a base implementation of a disk cache, which can
 * be easily extended to implement a disk cache for a specific perstistence
 * mechanism.
 *
 * When implementing the abstract methods note that while this base class
 * handles most things, it does not acquire or release any locks.
 * Implementations should do so as neccesary. This is mainly done to minimize
 * the time speant in critical sections.
 *
 * Error handling in this class needs to be addressed. Currently if an
 * exception is thrown by the persistence mechanism, this class destroys the
 * event queue. Should it also destory purgatory? Should it dispose itself?
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public abstract class AbstractDiskCache implements ICache, Serializable
{
    private final static Log log =
        LogFactory.getLog( AbstractDiskCache.class );

    /**
     * Map where elements are stored between being added to this cache and
     * actually spooled to disk. This allows puts to the disk cache to return
     * quickly, and the more expensive operation of serializing the elements
     * to persistent storage queued for later. If the elements are pulled into
     * the memory cache while the are still in purgatory, writing to disk can
     * be cancelled.
     */
    protected Hashtable purgatory = new Hashtable();

    /**
     * The CacheEventQueue where changes will be queued for asynchronous
     * updating of the persistent storage.
     */
    protected ICacheEventQueue cacheEventQueue;

    /**
     * Each instance of a Disk cache should use this lock to synchronize reads
     * and writes to the underlying storage mechansism.
     */
    protected ReadWriteLock lock = new ReadWriteLock();

    /**
     * Indicates whether the cache is 'alive', defined as having been
     * initialized, but not yet disposed.
     */
    protected boolean alive = false;

    /**
     * Every cache will have a name, subclasses must set this when they are
     * initialized.
     */
    protected String cacheName;

    /**
     * DEBUG: Keeps a count of the number of purgatory hits for debug messages
     */
    protected int purgHits = 0;

    // ----------------------------------------------------------- constructors

    public AbstractDiskCache( String cacheName )
    {
        this.cacheName = cacheName;

        this.cacheEventQueue = new CacheEventQueue( new MyCacheListener(),
                                                    CacheInfo.listenerId,
                                                    cacheName );
    }

    // ------------------------------------------------------- interface ICache

    /**
     * @see org.apache.jcs.engine.behavior.ICache#put
     */
    public final void put( Serializable key, Serializable value )
        throws IOException
    {
        put( key, value, null );
    }

    /**
     * @see org.apache.jcs.engine.behavior.ICache#put
     */
    public final void put( Serializable key, Serializable value,
                     IElementAttributes attributes )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, value );

        ce.setElementAttributes( attributes );

        update( ce );
    }

    /**
     * Adds the provided element to the cache. Element will be added to
     * purgatory, and then queued for later writing to the serialized storage
     * mechanism.
     *
     * @see org.apache.jcs.engine.behavior.ICache#update
     */
    public final void update( ICacheElement cacheElement )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Putting element in purgatory, cacheName: " + cacheName +
                       ", key: " + cacheElement.getKey() );
        }

        try
        {
            // Wrap the CacheElement in a PurgatoryElement

            PurgatoryElement pe = new PurgatoryElement( cacheElement );

            // Indicates the the element is eligable to be spooled to disk,
            // this will remain true unless the item is pulled back into
            // memory.

            pe.setSpoolable( true );

            // Add the element to purgatory

            purgatory.put( pe.getKey(), pe );

            // Queue element for serialization

            cacheEventQueue.addPutEvent( pe );
        }
        catch ( IOException ex )
        {
            log.error( ex );

            cacheEventQueue.destroy();
        }
    }

    /**
     * @see org.apache.jcs.engine.behavior.ICache#get
     */
    public final Serializable get( Serializable key )
    {
        return get( key, true );
    }

    /**
     * @see org.apache.jcs.engine.behavior.ICache#get
     */
    public final Serializable get( Serializable key, boolean container )
    {
        // If not alive, always return null.

        if ( !alive )
        {
            return null;
        }

        PurgatoryElement pe = ( PurgatoryElement ) purgatory.get( key );

        // If the element was found in purgatory

        if ( pe != null )
        {
            purgHits++;

            if ( log.isDebugEnabled() )
            {
                if ( purgHits % 100 == 0 )
                {
                    log.debug( "Purgatory hits = " + purgHits );
                }
            }

            // The element will go back to the memory cache, so set spoolable
            // to false, which will prevent the queue listener from serializing
            // the element.

            pe.setSpoolable( false );

            log.debug( "Found element in purgatory, cacheName: " + cacheName +
                       ", key: " + key );

            if ( container )
            {
                purgatory.remove( key );

                return pe.cacheElement;
            }
            else
            {
                purgatory.remove( key );

                return ( Serializable ) pe.cacheElement.getVal();
            }
        }

        // If we reach this point, element was not found in purgatory, so get
        // it from the cache.

        try
        {
            return doGet( key );
        }
        catch ( Exception e )
        {
            log.error( e );

            cacheEventQueue.destroy();
        }

        return null;
    }

    /**
     * @see org.apache.jcs.engine.behavior.ICache#remove
     */
    public final boolean remove( Serializable key )
    {
        // Remove element from purgatory if it is there

        purgatory.remove( key );

        // Queue removal for persistent store

        try
        {
            cacheEventQueue.addRemoveEvent( key );
        }
        catch ( IOException e )
        {
            log.error( e );

            cacheEventQueue.destroy();
        }

        return false;
    }

    /**
     * @see org.apache.jcs.engine.behavior.ICache#removeAll
     */
    public final void removeAll()
    {
        // Replace purgatory with a new empty hashtable

        purgatory = new Hashtable();

        // Queue a remove all for the persistent store

        try
        {
            cacheEventQueue.addRemoveAllEvent();
        }
        catch ( IOException e )
        {
            log.error( e );

            cacheEventQueue.destroy();
        }
    }

    /**
     * Adds a dispose request to the disk cache.
     */
    public final void dispose()
    {
        alive = false;

        // Invoke any implementation specific disposal code

        doDispose();

        // FIXME: May lose the end of the queue, need to be more graceful

        cacheEventQueue.destroy();
    }

    /**
     * @see ICache#getCacheName
     */
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * Subclasses will want to override this to provide more information most
     * likely.
     *
     * @see ICache#getStats
     */
    public String getStats()
    {
        return "cacheName = " + cacheName;
    }

    /**
     * @see ICache#getStatus
     */
    public int getStatus()
    {
        return ( alive ? CacheConstants.STATUS_ALIVE : CacheConstants.STATUS_DISPOSED );
    }

    /**
     * Size cannot be determined without knowledge of the cache implementation,
     * so subclasses will need to implement this method.
     *
     * @see ICache#getSize
     */
    public abstract int getSize();

    /**
     * @see ICache#getType
     *
     * @return Always returns DISK_CACHE since subclasses should all be of
     *         that type.
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }

    /**
     * Cache that implements the CacheListener interface, and calls appropriate
     * methods in its parent class.
     */
    private class MyCacheListener implements ICacheListener
    {
        private byte listenerId = 0;

        /**
         * @see ICacheListener#getListenerID
         */
        public byte getListenerId()
            throws IOException
        {
            return this.listenerId;
        }

        /**
         * @see ICacheListener#setListenerID
         */
        public void setListenerId( byte id )
            throws IOException
        {
            this.listenerId = id;
        }

        /**
         * @see ICacheListener#handlePut
         *
         * NOTE: This checks if the element is a puratory element and behaves
         * differently depending. However since we have control over how
         * elements are added to the cache event queue.
         */
        public void handlePut( ICacheElement element )
            throws IOException
        {
            if ( alive )
            {
                // If the element is a PurgatoryElement we must check to see
                // if it is still spoolable, and remove it from purgatory.

                if ( element instanceof PurgatoryElement )
                {
                    PurgatoryElement pe = ( PurgatoryElement ) element;

                    element = pe.getCacheElement();

                    // If the element is still eligable, spool it.

                    if ( pe.isSpoolable() )
                    {
                        doUpdate( element );
                    }

                    // After the update has completed, it is safe to remove
                    // the element from purgatory.

                    purgatory.remove( element.getKey() );
                }
                else
                {
                    doUpdate( element );
                }
            }
        }

        /**
         * @see ICacheListener#handleRemove
         */
        public void handleRemove( String cacheName, Serializable key )
            throws IOException
        {
            if ( alive )
            {
                if ( doRemove( key ) )
                {
                    log.debug( "Element removed, key: " + key );
                }
            }
        }

        /**
         * @see ICacheListener#handleRemoveAll
         */
        public void handleRemoveAll( String cacheName )
            throws IOException
        {
            if ( alive )
            {
                doRemoveAll();
            }
        }

        /**
         * @see ICacheListener#handleDispose
         */
        public void handleDispose( String cacheName )
            throws IOException
        {
            if ( alive )
            {
                doDispose();
            }
        }
    }

    // ---------------------- subclasses should implement the following methods

    /**
     * Get a value from the persistent store.
     *
     * @param key Key to locate value for.
     * @return An object matching key, or null.
     */
    protected abstract Serializable doGet( Serializable key );

    /**
     * Add a cache element to the persistent store.
     */
    protected abstract void doUpdate( ICacheElement element );

    /**
     * Remove an object from the persistent store if found.
     *
     * @param key Key of object to remove.
     */
    protected abstract boolean doRemove( Serializable key );

    /**
     * Remove all objects from the persistent store.
     */
    protected abstract void doRemoveAll();

    /**
     * Dispose of the persistent store. Note that disposal of purgatory and
     * setting alive to false does NOT need to be done by this method.
     */
    protected abstract void doDispose();
}

