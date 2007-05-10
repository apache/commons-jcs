package org.apache.jcs.engine.memory.mru;

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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.memory.AbstractMemoryCache;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * A SLOW reference management system. The most recently used items move to the
 * front of the list and get spooled to disk if the cache hub is configured to
 * use a disk cache.
 * <p>
 * This class is mainly for testing the hub. It also shows that use of the
 * Collection LinkedList is far slower than JCS' own double linked list.
 */
public class MRUMemoryCache
    extends AbstractMemoryCache
{
    private static final long serialVersionUID = 5013101678192336129L;

    private final static Log log = LogFactory.getLog( MRUMemoryCache.class );

    private int hitCnt = 0;

    private int missCnt = 0;

    private int putCnt = 0;

    /**
     * Object to lock on the Field
     */
    private int[] lockMe = new int[0];

    /**
     * MRU list.
     */
    private LinkedList mrulist = new LinkedList();

    /**
     * For post reflection creation initialization
     * @param hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        super.initialize( hub );
        log.info( "Initialized MRUMemoryCache for " + cacheName );
    }

    /**
     * Puts an item to the cache.
     * @param ce
     * @exception IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        putCnt++;

        Serializable key = ce.getKey();
        ce.getElementAttributes().setLastAccessTimeNow();

        // need a more fine grained locking here
        boolean replace = false;
        if ( map.containsKey( key ) )
        {
            replace = true;
        }
        synchronized ( lockMe )
        {
            map.put( key, ce );
            if ( replace )
            {
                // the slowest method I've ever seen
                mrulist.remove( key );
            }
            mrulist.addFirst( key );
        }

        // save a microsecond on the second call.
        int size = map.size();
        // need to spool at a certain percentage synchronously
        if ( size < this.cattr.getMaxObjects() )
        {
            return;
        }
        // SPOOL LAST -- need to make this a grouping in a queue

        if ( log.isDebugEnabled() )
        {
            log.debug( "In RAM overflow" );
        }

        // write the last item to disk.
        try
        {
            // PUSH more than 1 TO DISK TO MINIMIZE THE TYPICAL spool at each
            // put.
            int chunkSizeCorrected = Math.min( size, chunkSize );

            if ( log.isDebugEnabled() )
            {
                log.debug( "update: About to spool to disk cache, map.size() = " + size
                    + ", this.cattr.getMaxObjects() = " + this.cattr.getMaxObjects() + ", chunkSizeCorrected = "
                    + chunkSizeCorrected );
            }

            // The spool will put them in a disk event queue, so there is no
            // need to pre-queue the queuing. This would be a bit wasteful
            // and wouldn't save much time in this synchronous call.
            for ( int i = 0; i < chunkSizeCorrected; i++ )
            {
                // remove the last
                spoolLastElement();
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "update: After spool,  map.size() = " + size + ", this.cattr.getMaxObjects() = "
                    + this.cattr.getMaxObjects() + ", chunkSizeCorrected = " + chunkSizeCorrected );
            }

        }
        catch ( Exception ex )
        {
            // impossible case.
            log.error( "Problem updating MRU.", ex );
            throw new IllegalStateException( ex.getMessage() );
        }
    }

    /**
     * This removes the last elemement in the list.
     * <p>
     * @return ICacheElement if there was a last element, else null.
     */
    protected ICacheElement spoolLastElement()
    {
        ICacheElement toSpool = null;

        // need a more fine grained locking here
        synchronized ( lockMe )
        {
            Serializable last = (Serializable) mrulist.removeLast();
            if ( last != null )
            {
                toSpool = (ICacheElement) map.get( last );
                map.remove( last );
            }
        }
        // Might want to rename this "overflow" incase the hub
        // wants to do something else.
        if ( toSpool != null )
        {
            cache.spoolToDisk( toSpool );
        }

        return toSpool;
    }

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i>
     * according to its eviction policy. For example, the LRUMemoryCache will
     * remove the <i>numberToFree</i> least recently used items. These will be
     * spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are
     *         only 3, you will get 3.
     * @throws IOException
     */
    public int freeElements( int numberToFree )
        throws IOException
    {
        int freed = 0;
        for ( ; freed < numberToFree; freed++ )
        {
            ICacheElement element = spoolLastElement();
            if ( element == null )
            {
                break;
            }
        }
        return freed;
    }

    /**
     * Get an item from the cache without affecting its last access time or
     * position.
     * @return Element matching key if found, or null
     * @param key
     *            Identifies item to find
     * @exception IOException
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        try
        {
            ce = (ICacheElement) map.get( key );
            if ( ce != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + ": MRUMemoryCache quiet hit for " + key );
                }
            }
            else
            {
                log.debug( cacheName + ": MRUMemoryCache quiet miss for " + key );
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem getting quietly from MRU.", e );
        }

        return ce;
    }

    /**
     * Gets an item out of the map. If it finds an item, it is removed from the
     * list and then added to the first position in the linked list.
     * @return
     * @param key
     * @exception IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;
        boolean found = false;

        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "get> key=" + key );
                log.debug( "get> key=" + key.toString() );
            }

            ce = (ICacheElement) map.get( key );
            if ( log.isDebugEnabled() )
            {
                log.debug( "ce =" + ce );
            }

            if ( ce != null )
            {
                found = true;
                ce.getElementAttributes().setLastAccessTimeNow();
                hitCnt++;
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + " -- RAM-HIT for " + key );
                }
            }
        }
        catch ( Exception e )
        {
            log.error( "Problem getting element.", e );
        }

        try
        {
            if ( !found )
            {
                // Item not found in cache.
                missCnt++;
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

        try
        {
            synchronized ( lockMe )
            {
                mrulist.remove( ce.getKey() );
                mrulist.addFirst( ce.getKey() );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error making first", e );
            return null;
        }

        return ce;
    }

    /**
     * Removes an item from the cache.
     * @return
     * @param key
     * @exception IOException
     */
    public boolean remove( Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "remove> key=" + key );
        }

        boolean removed = false;

        // handle partial removal
        if ( key instanceof String && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( lockMe )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();
                    if ( k instanceof String && k.toString().startsWith( key.toString() ) )
                    {
                        itr.remove();
                        Serializable keyR = (Serializable) entry.getKey();
                        // map.remove( keyR );
                        mrulist.remove( keyR );
                        removed = true;
                    }
                }
            }
        }
        else if ( key instanceof GroupId )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( lockMe )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( key ) )
                    {
                        itr.remove();
                        mrulist.remove( k );
                        removed = true;
                    }
                }
            }
        }
        else
        {
            // remove single item.
            if ( map.containsKey( key ) )
            {
                synchronized ( lockMe )
                {
                    map.remove( key );
                    mrulist.remove( key );
                }
                removed = true;
            }
        }
        // end else not hierarchical removal
        return removed;
    }

    /**
     * Get an Array of the keys for all elements in the memory cache
     * @return Object[]
     */
    public Object[] getKeyArray()
    {
        // need to lock to map here?
        synchronized ( lockMe )
        {
            return map.keySet().toArray();
        }
    }

    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
            // for ( Iterator itr = memCache.getIterator(); itr.hasNext();) {
            Map.Entry e = (Map.Entry) itr.next();
            ICacheElement ce = (ICacheElement) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + ce.getVal() );
        }
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        ListIterator li = mrulist.listIterator();
        while ( li.hasNext() )
        {
            Serializable key = (Serializable) li.next();
            log.debug( "dumpCacheEntries> key=" + key + ", val=" + ( (ICacheElement) map.get( key ) ).getVal() );
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jcs.engine.memory.MemoryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "MRU Memory Cache" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "List Size" );
        se.setData( "" + mrulist.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Map Size" );
        se.setData( "" + map.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Put Count" );
        se.setData( "" + putCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Hit Count" );
        se.setData( "" + hitCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Miss Count" );
        se.setData( "" + missCnt );
        elems.add( se );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }
}
