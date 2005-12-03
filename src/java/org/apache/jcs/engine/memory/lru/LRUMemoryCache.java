package org.apache.jcs.engine.memory.lru;

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
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.memory.AbstractMemoryCache;
import org.apache.jcs.engine.memory.util.DoubleLinkedList;
import org.apache.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * A fast reference management system. The least recently used items move to the
 * end of the list and get spooled to disk if the cache hub is configured to use
 * a disk cache. Most of the cache bottelnecks are in IO. There are no io
 * bottlenecks here, it's all about processing power. 
 * <p>
 * Even though there are only
 * a few adjustments necessary to maintain the double linked list, we might want
 * to find a more efficient memory manager for large cache regions. 
 * <p>
 * The
 * LRUMemoryCache is most efficient when the first element is selected. The
 * smaller the region, the better the chance that this will be the case. < .04
 * ms per put, p3 866, 1/10 of that per get
 * 
 * @version $Id$
 */
public class LRUMemoryCache
    extends AbstractMemoryCache
{
    private final static Log log = LogFactory.getLog( LRUMemoryCache.class );

    // double linked list for lru
    private DoubleLinkedList list;

    int hitCnt = 0;

    int missCnt = 0;

    int putCnt = 0;

    /**
     * For post reflection creation initialization.
     * 
     * @param hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        super.initialize( hub );
        list = new DoubleLinkedList();
        log.info( "initialized LRUMemoryCache for " + cacheName );
    }

    /**
     * Puts an item to the cache.  Removes any pre-existing entries of the same key from the 
     * linked list and adds this one first.
     * <p>
     * If the max size is reached, an element will be put to disk.
     * 
     * @param ce
     *            The cache element, or entry wrapper
     * @exception IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        putCnt++;

        // Asynchronisly create a MemoryElement

        ce.getElementAttributes().setLastAccessTimeNow();
        MemoryElementDescriptor old = null;
        synchronized ( this )
        {
            // TODO address double synchronization of addFirst, use write lock
            addFirst( ce );
            // this must be synchronized
            old = (MemoryElementDescriptor) map.put( ( (MemoryElementDescriptor) list.getFirst() ).ce.getKey(),
                                                     (MemoryElementDescriptor) list.getFirst() );
            // If the node was the same as an existing node, remove it.
            if ( old != null && ( (MemoryElementDescriptor) list.getFirst() ).ce.getKey().equals( old.ce.getKey() ) )
            {
                list.remove( old );
            }
        }

        int size = map.size();
        // If the element limit is reached, we need to spool

        if ( size < this.cattr.getMaxObjects() )
        {
            return;
        }
        
        if ( log.isDebugEnabled() )
        {
            log.debug( "In memory limit reached, spooling" );
        }
        
        // Write the last 'chunkSize' items to disk.
        int chunkSizeCorrected = Math.min( size, chunkSize );

        if ( log.isDebugEnabled() )
        {
            log.debug( "About to spool to disk cache, map size: " + size + ", max objects: "
                + this.cattr.getMaxObjects() + ", items to spool: " + chunkSizeCorrected );
        }

        // The spool will put them in a disk event queue, so there is no
        // need to pre-queue the queuing. This would be a bit wasteful
        // and wouldn't save much time in this synchronous call.

        for ( int i = 0; i < chunkSizeCorrected; i++ )
        {
            synchronized ( this )
            {
                if ( list.getLast() != null )
                {
                    if ( ( (MemoryElementDescriptor) list.getLast() ).ce != null )
                    {
                        cache.spoolToDisk( ( (MemoryElementDescriptor) list.getLast() ).ce );
                        if ( !map.containsKey( ( (MemoryElementDescriptor) list.getLast() ).ce.getKey() ) )
                        {
                            log.error( "update: map does not contain key: "
                                + ( (MemoryElementDescriptor) list.getLast() ).ce.getKey() );
                            verifyCache();
                        }
                        if ( map.remove( ( (MemoryElementDescriptor) list.getLast() ).ce.getKey() ) == null )
                        {
                            log.warn( "update: remove failed for key: "
                                + ( (MemoryElementDescriptor) list.getLast() ).ce.getKey() );
                            verifyCache();
                        }
                    }
                    else
                    {
                        throw new Error( "update: last.ce is null!" );
                    }
                    list.removeLast();
                }
                else
                {
                    verifyCache();
                    throw new Error( "update: last is null!" );
                }
                
                // If this is out of the sync block it can detect a mismatch where there is none.
                if ( map.size() != dumpCacheSize() )
                {
                    log.warn( "update: After spool, size mismatch: map.size() = " + map.size() + ", linked list size = "
                        + dumpCacheSize() );
                }                
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "update: After spool map size: " + map.size() + " linked list size = "
                + dumpCacheSize());
        }
        
    }

    /**
     * Get an item from the cache without affecting its last access time or
     * position.
     * 
     * @param key
     *            Identifies item to find
     * @return Element mathinh key if found, or null
     * @exception IOException
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        MemoryElementDescriptor me = (MemoryElementDescriptor) map.get( key );
        if ( me != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache quiet hit for " + key );
            }

            ce = me.ce;
        }
        else if ( log.isDebugEnabled() )
        {
            log.debug( cacheName + ": LRUMemoryCache quiet miss for " + key );
        }

        return ce;
    }

    /**
     * Get an item from the cache
     * 
     * @param key
     *            Identifies item to find
     * @return ICacheElement if found, else null
     * @exception IOException
     */
    public synchronized ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item from cache " + cacheName + " for key " + key );
        }

        MemoryElementDescriptor me = (MemoryElementDescriptor) map.get( key );

        if ( me != null )
        {
            hitCnt++;
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache hit for " + key );
            }

            ce = me.ce;

            ce.getElementAttributes().setLastAccessTimeNow();
            list.makeFirst( me );
        }
        else
        {
            missCnt++;
            log.debug( cacheName + ": LRUMemoryCache miss for " + key );
        }

        verifyCache();
        return ce;
    }

    /**
     * Removes an item from the cache. This method handles hierarchical removal.
     * If the key is a String and ends with the
     * CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     * starting with the argument String will be removed.
     * 
     * @param key
     * @return
     * @exception IOException
     */
    public synchronized boolean remove( Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing item for key: " + key );
        }

        boolean removed = false;

        // handle partial removal
        if ( key instanceof String && ( (String) key ).endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof String && ( (String) k ).startsWith( key.toString() ) )
                    {
                        list.remove( (MemoryElementDescriptor) entry.getValue() );

                        itr.remove();

                        removed = true;
                    }
                }
            }
        }
        else if ( key instanceof GroupId )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof GroupAttrName && ( (GroupAttrName) k ).groupId.equals( key ) )
                    {
                        itr.remove();

                        list.remove( (MemoryElementDescriptor) entry.getValue() );

                        removed = true;
                    }
                }
            }
        }
        else
        {
            // remove single item.
            MemoryElementDescriptor me = (MemoryElementDescriptor) map.remove( key );

            if ( me != null )
            {
                list.remove( me );
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Remove all of the elements from both the Map and the linked list
     * implementation. Overrides base class.
     * @throws IOException
     */
    public synchronized void removeAll()
        throws IOException
    {
        map.clear();
        list.removeAll();
    }

    // --------------------------- iteration mehods (iteration helpers)
    /**
     * 
     * iteration aid
     *
     */
    public class IteratorWrapper
        implements Iterator
    {
        //private final Log log = LogFactory.getLog( LRUMemoryCache.class );

        private final Iterator i;

        private IteratorWrapper( Map m )
        {
            i = m.entrySet().iterator();
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            return new MapEntryWrapper( (Map.Entry) i.next() );
        }

        public void remove()
        {
            i.remove();
        }

        public boolean equals( Object o )
        {
            return i.equals( o );
        }

        public int hashCode()
        {
            return i.hashCode();
        }
    }

    public class MapEntryWrapper
        implements Map.Entry
    {
        private final Map.Entry e;

        private MapEntryWrapper( Map.Entry e )
        {
            this.e = e;
        }

        public boolean equals( Object o )
        {
            return e.equals( o );
        }

        public Object getKey()
        {
            return e.getKey();
        }

        public Object getValue()
        {
            return ( (MemoryElementDescriptor) e.getValue() ).ce;
        }

        public int hashCode()
        {
            return e.hashCode();
        }

        public Object setValue( Object value )
        {
            throw new UnsupportedOperationException( "Use normal cache methods"
                + " to alter the contents of the cache." );
        }
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     * 
     * @return The iterator value
     */
    public Iterator getIterator()
    {
        return new IteratorWrapper( map );
    }

    /**
     * Get an Array of the keys for all elements in the memory cache
     * 
     * @return An Object[]
     */
    public Object[] getKeyArray()
    {
        // need a better locking strategy here.
        synchronized ( this )
        {
            // may need to lock to map here?
            return map.keySet().toArray();
        }
    }

    // --------------------------- internal mehods (linked list implementation)
    /**
     * Adds a new node to the end of the link list. Currently not used.
     * 
     * @param ce
     *            The feature to be added to the Last
     */
    protected void addLast( CacheElement ce )
    {
        MemoryElementDescriptor me = new MemoryElementDescriptor( ce );
        list.addLast( me );
        verifyCache( ce.getKey() );
    }

    /**
     * Adds a new node to the start of the link list.
     * 
     * @param ce
     *            The feature to be added to the First
     */
    private synchronized void addFirst( ICacheElement ce )
    {

        MemoryElementDescriptor me = new MemoryElementDescriptor( ce );
        list.addFirst( me );
        return;
    }

    // ---------------------------------------------------------- debug methods

    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
            Map.Entry e = (Map.Entry) itr.next();
            MemoryElementDescriptor me = (MemoryElementDescriptor) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        for ( MemoryElementDescriptor me = (MemoryElementDescriptor) list.getFirst(); me != null; me = (MemoryElementDescriptor) me.next )
        {
            log.debug( "dumpCacheEntries> key=" + me.ce.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     * Returns the size of the list.
     * 
     * @return
     */
    private int dumpCacheSize()
    {
        return list.size();
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks
     * consistency between List and map.
     *  
     */
    private void verifyCache()
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;
        log.debug( "verifycache[" + cacheName + "]: mapContains " + map.size() + " elements, linked list contains "
            + dumpCacheSize() + " elements" );
        log.debug( "verifycache: checking linked list by key " );
        for ( MemoryElementDescriptor li = (MemoryElementDescriptor) list.getFirst(); li != null; li = (MemoryElementDescriptor) li.next )
        {
            Object key = li.ce.getKey();
            if ( !map.containsKey( key ) )
            {
                log.error( "verifycache[" + cacheName + "]: map does not contain key : " + li.ce.getKey() );
                log.error( "li.hashcode=" + li.ce.getKey().hashCode() );
                log.error( "key class=" + key.getClass() );
                log.error( "key hashcode=" + key.hashCode() );
                log.error( "key toString=" + key.toString() );
                if ( key instanceof GroupAttrName )
                {
                    GroupAttrName name = (GroupAttrName) key;
                    log.error( "GroupID hashcode=" + name.groupId.hashCode() );
                    log.error( "GroupID.class=" + name.groupId.getClass() );
                    log.error( "AttrName hashcode=" + name.attrName.hashCode() );
                    log.error( "AttrName.class=" + name.attrName.getClass() );
                }
                dumpMap();
            }
            else if ( map.get( li.ce.getKey() ) == null )
            {
                log.error( "verifycache[" + cacheName + "]: linked list retrieval returned null for key: "
                    + li.ce.getKey() );
            }
        }

        log.debug( "verifycache: checking linked list by value " );
        for ( MemoryElementDescriptor li3 = (MemoryElementDescriptor) list.getFirst(); li3 != null; li3 = (MemoryElementDescriptor) li3.next )
        {
            if ( map.containsValue( li3 ) == false )
            {
                log.error( "verifycache[" + cacheName + "]: map does not contain value : " + li3 );
                dumpMap();
            }
        }

        log.debug( "verifycache: checking via keysets!" );
        for ( Iterator itr2 = map.keySet().iterator(); itr2.hasNext(); )
        {
            found = false;
            Serializable val = null;
            try
            {
                val = (Serializable) itr2.next();
            }
            catch ( NoSuchElementException nse )
            {
                log.error( "verifycache: no such element exception" );
            }

            for ( MemoryElementDescriptor li2 = (MemoryElementDescriptor) list.getFirst(); li2 != null; li2 = (MemoryElementDescriptor) li2.next )
            {
                if ( val.equals( li2.ce.getKey() ) )
                {
                    found = true;
                    break;
                }
            }
            if ( !found )
            {
                log.error( "verifycache[" + cacheName + "]: key not found in list : " + val );
                dumpCacheEntries();
                if ( map.containsKey( val ) )
                {
                    log.error( "verifycache: map contains key" );
                }
                else
                {
                    log.error( "verifycache: map does NOT contain key, what the HECK!" );
                }
            }
        }
    }

    /**
     * Logs an error is an element that should be in the cache is not.
     * 
     * @param key
     */
    private void verifyCache( Serializable key )
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;

        // go through the linked list looking for the key
        for ( MemoryElementDescriptor li = (MemoryElementDescriptor) list.getFirst(); li != null; li = (MemoryElementDescriptor) li.next )
        {
            if ( li.ce.getKey() == key )
            {
                found = true;
                log.debug( "verifycache(key) key match: " + key );
                break;
            }
        }
        if ( !found )
        {
            log.error( "verifycache(key)[" + cacheName + "], couldn't find key! : " + key );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.memory.MemoryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "LRU Memory Cache" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "List Size" );
        se.setData( "" + list.size() );
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

        // int rate = ((hitCnt + missCnt) * 100) / (hitCnt * 100) * 100;
        //buf.append("\n Hit Rate = " + rate + " %" );

        return stats;
    }

}
