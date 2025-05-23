package org.apache.commons.jcs3.auxiliary.disk;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheEventLogging;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCache;
import org.apache.commons.jcs3.auxiliary.disk.behavior.IDiskCacheAttributes;
import org.apache.commons.jcs3.engine.CacheEventQueueFactory;
import org.apache.commons.jcs3.engine.CacheInfo;
import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICache;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs3.engine.behavior.ICacheListener;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.utils.struct.LRUMap;

/**
 * Abstract class providing a base implementation of a disk cache, which can be easily extended to
 * implement a disk cache for a specific persistence mechanism.
 *
 * When implementing the abstract methods note that while this base class handles most things, it
 * does not acquire or release any locks. Implementations should do so as necessary. This is mainly
 * done to minimize the time spent in critical sections.
 *
 * Error handling in this class needs to be addressed. Currently if an exception is thrown by the
 * persistence mechanism, this class destroys the event queue. Should it also destroy purgatory?
 * Should it dispose itself?
 */
public abstract class AbstractDiskCache<K, V>
    extends AbstractAuxiliaryCacheEventLogging<K, V>
{
    /**
     * Cache that implements the CacheListener interface, and calls appropriate methods in its
     * parent class.
     */
    protected class MyCacheListener
        implements ICacheListener<K, V>
    {
        /** Id of the listener */
        private long listenerId;

        /**
         * @return cacheElement.getElementAttributes();
         * @throws IOException
         * @see ICacheListener#getListenerId
         */
        @Override
        public long getListenerId()
            throws IOException
        {
            return this.listenerId;
        }

        /**
         * @param cacheName
         * @throws IOException
         * @see ICacheListener#handleDispose
         */
        @Override
        public void handleDispose( final String cacheName )
            throws IOException
        {
            if (alive.get())
            {
                doDispose();
            }
        }

        /**
         * @param element
         * @throws IOException
         * @see ICacheListener#handlePut NOTE: This checks if the element is a puratory element and
         *      behaves differently depending. However since we have control over how elements are
         *      added to the cache event queue, that may not be needed ( they are always
         *      PurgatoryElements ).
         */
        @Override
        public void handlePut( ICacheElement<K, V> element )
            throws IOException
        {
            if (alive.get())
            {
                // If the element is a PurgatoryElement<K, V> we must check to see
                // if it is still spoolable, and remove it from purgatory.
                if ( element instanceof PurgatoryElement )
                {
                    final PurgatoryElement<K, V> pe = (PurgatoryElement<K, V>) element;

                    synchronized ( pe.getCacheElement() )
                    {
                        // TODO consider a timeout.
                        // we need this so that we can have multiple update
                        // threads and still have removeAll requests come in that
                        // always win
                        removeAllLock.readLock().lock();

                        try
                        {
                            // If the element has already been removed from
                            // purgatory do nothing
                            if (!purgatory.containsKey(pe.getKey()))
                            {
                                return;
                            }

                            element = pe.getCacheElement();

                            // If the element is still eligible, spool it.
                            if ( pe.isSpoolable() )
                            {
                                doUpdate( element );
                            }
                        }
                        finally
                        {
                            removeAllLock.readLock().unlock();
                        }

                        // After the update has completed, it is safe to
                        // remove the element from purgatory.
                        purgatory.remove( element.getKey() );
                    }
                }
                else
                {
                    // call the child's implementation
                    doUpdate( element );
                }
            }
            else
            {
                /*
                 * The cache is not alive, hence the element should be removed from purgatory. All
                 * elements should be removed eventually. Perhaps, the alive check should have been
                 * done before it went in the queue. This block handles the case where the disk
                 * cache fails during normal operations.
                 */
                purgatory.remove( element.getKey() );
            }
        }

        /**
         * @param cacheName
         * @param key
         * @throws IOException
         * @see ICacheListener#handleRemove
         */
        @Override
        public void handleRemove( final String cacheName, final K key )
            throws IOException
        {
            if (alive.get() && doRemove( key ) )
            {
                log.debug( "Element removed, key: " + key );
            }
        }

        /**
         * @param cacheName
         * @throws IOException
         * @see ICacheListener#handleRemoveAll
         */
        @Override
        public void handleRemoveAll( final String cacheName )
            throws IOException
        {
            if (alive.get())
            {
                doRemoveAll();
            }
        }

        /**
         * @param id
         * @throws IOException
         * @see ICacheListener#setListenerId
         */
        @Override
        public void setListenerId( final long id )
            throws IOException
        {
            this.listenerId = id;
        }
    }

    /** The logger */
    private static final Log log = Log.getLog( AbstractDiskCache.class );

    /** Generic disk cache attributes */
    private final IDiskCacheAttributes diskCacheAttributes;

    /**
     * Map where elements are stored between being added to this cache and actually spooled to disk.
     * This allows puts to the disk cache to return quickly, and the more expensive operation of
     * serializing the elements to persistent storage queued for later.
     *
     * If the elements are pulled into the memory cache while the are still in purgatory, writing to
     * disk can be canceled.
     */
    private Map<K, PurgatoryElement<K, V>> purgatory;

    /**
     * The CacheEventQueue where changes will be queued for asynchronous updating of the persistent
     * storage.
     */
    private final ICacheEventQueue<K, V> cacheEventQueue;

    /**
     * Indicates whether the cache is 'alive': initialized, but not yet disposed. Child classes must
     * set this to true.
     */
    private final AtomicBoolean alive = new AtomicBoolean();

    /** Every cache will have a name, subclasses must set this when they are initialized. */
    private final String cacheName;

    /** DEBUG: Keeps a count of the number of purgatory hits for debug messages */
    private int purgHits;

    /**
     * We lock here, so that we cannot get an update after a remove all. an individual removal locks
     * the item.
     */
    private final ReentrantReadWriteLock removeAllLock = new ReentrantReadWriteLock();

    /**
     * Constructs the abstract disk cache, create event queues and purgatory. Child classes should
     * set the alive flag to true after they are initialized.
     *
     * @param attr
     */
    protected AbstractDiskCache( final IDiskCacheAttributes attr )
    {
        this.diskCacheAttributes = attr;
        this.cacheName = attr.getCacheName();

        // create queue
        final CacheEventQueueFactory<K, V> fact = new CacheEventQueueFactory<>();
        this.cacheEventQueue = fact.createCacheEventQueue(
                new MyCacheListener(), CacheInfo.listenerId, cacheName,
                   diskCacheAttributes.getEventQueuePoolName(),
                   diskCacheAttributes.getEventQueueType() );

        // create purgatory
        initPurgatory();
    }

    /**
     * Adds a dispose request to the disk cache.
     *
     * Disposal proceeds in several steps.
     * <ol>
     * <li>Prior to this call the Composite cache dumped the memory into the disk cache. If it is
     * large then we need to wait for the event queue to finish.</li>
     * <li>Wait until the event queue is empty of until the configured ShutdownSpoolTimeLimit is
     * reached.</li>
     * <li>Call doDispose on the concrete impl.</li>
     * </ol>
     * @throws IOException
     */
    @Override
    public final void dispose()
        throws IOException
    {
        log.info( "In dispose, destroying event queue." );

        // wait for dispose and then quit if not done.
        cacheEventQueue.destroy(this.diskCacheAttributes.getShutdownSpoolTimeLimit());

        // Invoke any implementation specific disposal code
        // need to handle the disposal first.
        doDispose();

        alive.set(false);
    }

    /**
     * Dispose of the persistent store. Note that disposal of purgatory and setting alive to false
     * does NOT need to be done by this method.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @throws IOException
     */
    protected final void doDispose()
        throws IOException
    {
        super.disposeWithEventLogging();
    }

    /**
     * Gets a value from the persistent store.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @param key Key to locate value for.
     * @return An object matching key, or null.
     * @throws IOException
     */
    protected final ICacheElement<K, V> doGet( final K key )
        throws IOException
    {
        return super.getWithEventLogging( key );
    }

    /**
     * Gets a value from the persistent store.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @param pattern Used to match keys.
     * @return A map of matches.
     * @throws IOException
     */
    protected final Map<K, ICacheElement<K, V>> doGetMatching( final String pattern )
        throws IOException
    {
        return super.getMatchingWithEventLogging( pattern );
    }

    /**
     * Remove an object from the persistent store if found.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @param key Key of object to remove.
     * @return whether or no the item was present when removed
     * @throws IOException
     */
    protected final boolean doRemove( final K key )
        throws IOException
    {
        return super.removeWithEventLogging( key );
    }

    /**
     * Remove all objects from the persistent store.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @throws IOException
     */
    protected final void doRemoveAll()
        throws IOException
    {
        super.removeAllWithEventLogging();
    }

    /**
     * Add a cache element to the persistent store.
     *
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *EventLogging method on the super. The *WithEventLogging methods call the
     * abstract process* methods. The children implement the process methods.
     *
     * @param cacheElement
     * @throws IOException
     */
    protected final void doUpdate( final ICacheElement<K, V> cacheElement )
        throws IOException
    {
        super.updateWithEventLogging( cacheElement );
    }

    /**
     * Check to see if the item is in purgatory. If so, return it. If not, check to see if we have
     * it on disk.
     *
     * @param key
     * @return ICacheElement&lt;K, V&gt; or null
     * @see AuxiliaryCache#get
     */
    @Override
    public final ICacheElement<K, V> get( final K key )
    {
        // If not alive, always return null.
        if (!alive.get())
        {
            log.debug( "get was called, but the disk cache is not alive." );
            return null;
        }

        final PurgatoryElement<K, V> pe = purgatory.get( key );

        // If the element was found in purgatory
        if ( pe != null )
        {
            purgHits++;

            if ( purgHits % 100 == 0 )
            {
                log.debug( "Purgatory hits = {0}", purgHits );
            }

            // Since the element will go back to the memory cache, we could set
            // spoolable to false, which will prevent the queue listener from
            // serializing the element. This would not match the disk cache
            // behavior and the behavior of other auxiliaries. Gets never remove
            // items from auxiliaries.
            // Beyond consistency, the items should stay in purgatory and get
            // spooled since the mem cache may be set to 0. If an item is
            // active, it will keep getting put into purgatory and removed. The
            // CompositeCache now does not put an item to memory from disk if
            // the size is 0.
            // Do not set spoolable to false. Just let it go to disk. This
            // will allow the memory size = 0 setting to work well.

            log.debug( "Found element in purgatory, cacheName: {0}, key: {1}",
                    cacheName, key );

            return pe.getCacheElement();
        }

        // If we reach this point, element was not found in purgatory, so get
        // it from the cache.
        try
        {
            return doGet( key );
        }
        catch (final IOException e)
        {
            log.error( e );
            cacheEventQueue.destroy();
        }

        return null;
    }

    /**
     * @return the region name.
     * @see ICache#getCacheName
     */
    @Override
    public String getCacheName()
    {
        return cacheName;
    }

    /**
     * @see org.apache.commons.jcs3.engine.behavior.ICacheType#getCacheType
     * @return Always returns DISK_CACHE since subclasses should all be of that type.
     */
    @Override
    public CacheType getCacheType()
    {
        return CacheType.DISK_CACHE;
    }

    /**
     * This is used by the event logging.
     *
     * @return the location of the disk, either path or ip.
     */
    protected abstract String getDiskLocation();

    /**
     * Gets the extra info for the event log.
     *
     * @return disk location
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return getDiskLocation();
    }

    /**
     * The keys in the cache.
     *
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getKeySet()
     */
    @Override
    public abstract Set<K> getKeySet() throws IOException;

    /**
     * Gets items from the cache matching the given pattern. Items from memory will replace those
     * from remote sources.
     *
     * This only works with string keys. It's too expensive to do a toString on every key.
     *
     * Auxiliaries will do their best to handle simple expressions. For instance, the JDBC disk
     * cache will convert * to % and . to _
     *
     * @param pattern
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data matching the pattern.
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching( final String pattern )
        throws IOException
    {
        // this avoids locking purgatory, but it uses more memory
        final Set<K> keyArray = new HashSet<>(purgatory.keySet());

        final Set<K> matchingKeys = getKeyMatcher().getMatchingKeysFromArray(pattern, keyArray);

        // call getMultiple with the set
        final Map<K, ICacheElement<K, V>> result = processGetMultiple( matchingKeys );

        // Get the keys from disk
        final Map<K, ICacheElement<K, V>> diskMatches = doGetMatching( pattern );

        result.putAll( diskMatches );

        return result;
    }

    /**
     * Size cannot be determined without knowledge of the cache implementation, so subclasses will
     * need to implement this method.
     *
     * @return the number of items.
     * @see ICache#getSize
     */
    @Override
    public abstract int getSize();

    /**
     * Returns semi-structured data.
     *
     * @see org.apache.commons.jcs3.auxiliary.AuxiliaryCache#getStatistics()
     */
    @Override
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "Abstract Disk Cache" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        elems.add(new StatElement<>( "Purgatory Hits", Integer.valueOf(purgHits) ) );
        elems.add(new StatElement<>( "Purgatory Size", Integer.valueOf(purgatory.size()) ) );

        // get the stats from the event queue too
        final IStats eqStats = this.cacheEventQueue.getStatistics();
        elems.addAll(eqStats.getStatElements());

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * Before the event logging layer, the subclasses implemented the do* methods. Now the do*
     * methods call the *WithEventLogging method on the super. The *WithEventLogging methods call
     * the abstract process* methods. The children implement the process methods.
     *
     * For example, doGet calls getWithEventLogging, which calls processGet
     */

    /**
     * Gets basic stats for the abstract disk cache.
     *
     * @return String
     */
    @Override
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * @return the status -- alive or disposed from CacheConstants
     * @see ICache#getStatus
     */
    @Override
    public CacheStatus getStatus()
    {
        return alive.get() ? CacheStatus.ALIVE : CacheStatus.DISPOSED;
    }

    /**
     * Purgatory size of -1 means to use a HashMap with no size limit. Anything greater will use an
     * LRU map of some sort.
     *
     * TODO Currently setting this to 0 will cause nothing to be put to disk, since it will assume
     *       that if an item is not in purgatory, then it must have been plucked. We should make 0
     *       work, a way to not use purgatory.
     */
    private void initPurgatory()
    {
        // we need this so we can stop the updates from happening after a
        // remove all
        removeAllLock.writeLock().lock();

        try
        {
            synchronized (this)
            {
                if ( diskCacheAttributes.getMaxPurgatorySize() >= 0 )
                {
                    purgatory = Collections.synchronizedMap(
                            new LRUMap<>( diskCacheAttributes.getMaxPurgatorySize()));
                }
                else
                {
                    purgatory = new ConcurrentHashMap<>();
                }
            }
        }
        finally
        {
            removeAllLock.writeLock().unlock();
        }
    }

    /**
     * @return true if the cache is alive
     */
    public boolean isAlive()
    {
        return alive.get();
    }

    /**
     * Removes are not queued. A call to remove is immediate.
     *
     * @param key
     * @return whether the item was present to be removed.
     * @throws IOException
     * @see org.apache.commons.jcs3.engine.behavior.ICache#remove
     */
    @Override
    public final boolean remove( final K key )
        throws IOException
    {
        // I'm getting the object, so I can lock on the element
        // Remove element from purgatory if it is there
        final PurgatoryElement<K, V> pe = purgatory.remove( key );
        boolean present;

        if ( pe != null )
        {
            synchronized ( pe.getCacheElement() )
            {
                // no way to remove from queue, just make sure it doesn't get on
                // disk and then removed right afterwards
                pe.setSpoolable( false );

                // Remove from persistent store immediately
                present = doRemove( key );
            }
        }
        else
        {
            // Remove from persistent store immediately
            present = doRemove( key );
        }

        return present;
    }

    /**
     * @throws IOException
     * @see org.apache.commons.jcs3.engine.behavior.ICache#removeAll
     */
    @Override
    public final void removeAll()
        throws IOException
    {
        if ( this.diskCacheAttributes.isAllowRemoveAll() )
        {
            // Replace purgatory with a new empty hashtable
            initPurgatory();

            // Remove all from persistent store immediately
            doRemoveAll();
        }
        else
        {
            log.info( "RemoveAll was requested but the request was not "
                    + "fulfilled: allowRemoveAll is set to false." );
        }
    }

    /**
     * @param alive set the alive status
     */
    public void setAlive(final boolean alive)
    {
        this.alive.set(alive);
    }

    /**
     * Adds the provided element to the cache. Element will be added to purgatory, and then queued
     * for later writing to the serialized storage mechanism.
     *
     * An update results in a put event being created. The put event will call the handlePut method
     * defined here. The handlePut method calls the implemented doPut on the child.
     *
     * @param cacheElement
     * @throws IOException
     * @see org.apache.commons.jcs3.engine.behavior.ICache#update
     */
    @Override
    public final void update( final ICacheElement<K, V> cacheElement )
        throws IOException
    {
        log.debug( "Putting element in purgatory, cacheName: {0}, key: {1}",
                () -> cacheName, cacheElement::getKey);

        try
        {
            // Wrap the CacheElement in a PurgatoryElement
            final PurgatoryElement<K, V> pe = new PurgatoryElement<>( cacheElement );

            // Indicates the element is eligible to be spooled to disk,
            // this will remain true unless the item is pulled back into
            // memory.
            pe.setSpoolable( true );

            // Add the element to purgatory
            purgatory.put( pe.getKey(), pe );

            // Queue element for serialization
            cacheEventQueue.addPutEvent( pe );
        }
        catch ( final IOException ex )
        {
            log.error( "Problem adding put event to queue.", ex );
            cacheEventQueue.destroy();
        }
    }
}
