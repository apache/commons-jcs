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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.memory.AbstractMemoryCache;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.control.group.GroupAttrName;

/**
 *  A fast reference management system. The least recently used items move to
 *  the end of the list and get spooled to disk if the cache hub is configured
 *  to use a disk cache. Most of the cache bottelnecks are in IO. There are no
 *  io bottlenecks here, it's all about processing power. Even though there are
 *  only a few adjustments necessary to maintain the double linked list, we
 *  might want to find a more efficient memory manager for large cache regions.
 *  The LRUMemoryCache is most efficeint when the first element is selected. The
 *  smaller the region, the better the chance that this will be the case. < .04
 *  ms per put, p3 866, 1/10 of that per get
 *
 *@version    $Id$
 */
public class LRUMemoryCache
    extends AbstractMemoryCache
{
    private final static Log log =
        LogFactory.getLog( LRUMemoryCache.class );

    // LRU double linked list head/tail nodes
    private MemoryElementDescriptor first;
    private MemoryElementDescriptor last;

    /**
     *  For post reflection creation initialization
     *
     *@param  hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        super.initialize(hub);
        log.info( "initialized LRUMemoryCache for " + cacheName );
    }

    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // Asynchronisly create a MemoryElement

        ce.getElementAttributes().setLastAccessTimeNow();
        addFirst( ce );
        MemoryElementDescriptor old =
            ( MemoryElementDescriptor ) map.put( first.ce.getKey(), first );

        // If the node was the same as an existing node, remove it.

        if ( old != null && first.ce.getKey().equals( old.ce.getKey() ) )
        {
            removeNode( old );
        }

        int size = map.size();
        // If the element limit is reached, we need to spool

        if ( size < this.cattr.getMaxObjects() )
        {
            return;
        }
        else
        {
            log.debug( "In memory limit reached, spooling" );

            // Write the last 'chunkSize' items to disk.

            int chunkSizeCorrected = Math.min( size, chunkSize );

            if ( log.isDebugEnabled() )
            {
                log.debug( "About to spool to disk cache, map size: " + size
                     + ", max objects: " + this.cattr.getMaxObjects()
                     + ", items to spool: " + chunkSizeCorrected );
            }

            // The spool will put them in a disk event queue, so there is no
            // need to pre-queue the queuing.  This would be a bit wasteful
            // and wouldn't save much time in this synchronous call.

            for ( int i = 0; i < chunkSizeCorrected; i++ )
            {
                synchronized ( this )
                {
                    if ( last != null ) 
                    {
                        if ( last.ce != null )
                        {
                            cache.spoolToDisk( last.ce );
                            if ( !map.containsKey(last.ce.getKey()) )
                            {
                                log.error("update: map does not contain key: " + last.ce.getKey());
                                verifyCache();
                            }
                            if ( map.remove(last.ce.getKey()) == null )
                            {
                                log.warn("update: remove failed for key: " + last.ce.getKey() );
                                verifyCache();
                            }
                        }
                        else
                        {
                            throw new Error("update: last.ce is null!");
                        }
                        removeNode( last );
                    } 
                    else 
                    {
                        verifyCache();
                        throw new Error("update: last is null!");
                    }
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug("update: After spool map size: " + map.size());
            }
            if ( map.size() != dumpCacheSize() )
            {
                log.error("update: After spool, size mismatch: map.size() = "
                          + map.size() + ", linked list size = " +
                          dumpCacheSize());
            }
        }
    }

    /**
     * Remove all of the elements from both the Map and the linked
     * list implementation. Overrides base class.
     */ 
    public synchronized void removeAll()
        throws IOException
    {
        map.clear();
        for ( MemoryElementDescriptor me = first; me != null; ) 
        {
            if ( me.prev != null )
            {
                me.prev = null;
            }
            MemoryElementDescriptor next = me.next;
            me = next;
        }
        first = last = null;
    }

    /**
     *  Get an item from the cache without affecting its last access time or
     *  position.
     *
     *@param  key              Identifies item to find
     *@return                  Element mathinh key if found, or null
     *@exception  IOException
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        MemoryElementDescriptor me = (MemoryElementDescriptor) map.get(key);
        if ( me != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug(cacheName + ": LRUMemoryCache quiet hit for " + key);
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
     *  Get an item from the cache
     *
     *@param  key              Identifies item to find
     *@return                  ICacheElement if found, else null
     *@exception  IOException
     */
    public synchronized ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item from cache " + cacheName + " for key " +
                       key );
        }

        MemoryElementDescriptor me = (MemoryElementDescriptor)map.get(key);
        
        if ( me != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache hit for " + key );
            }
            
            ce = me.ce;
            
            ce.getElementAttributes().setLastAccessTimeNow();
            makeFirst( me );
        }
        else
        {
            log.debug( cacheName + ": LRUMemoryCache miss for " + key );
        }
        
        verifyCache();
        return ce;
    }

    /**
     *  Removes an item from the cache. This method handles hierarchical
     *  removal. If the key is a String and ends with the
     *  CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     *  starting with the argument String will be removed.
     *
     *@param  key
     *@return
     *@exception  IOException
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
        if ( key instanceof String && ( ( String ) key )
            .endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext();  )
                {
                    Map.Entry entry = ( Map.Entry ) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof String
                         && ( ( String ) k ).startsWith( key.toString() ) )
                    {
                        itr.remove();

                        removeNode( ( MemoryElementDescriptor )
                            entry.getValue() );

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
                for (Iterator itr = map.entrySet().iterator(); itr.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof GroupAttrName
                         && ((GroupAttrName)k).groupId.equals(key) )
                    {
                        itr.remove();

                        removeNode( ( MemoryElementDescriptor )
                            entry.getValue() );

                        removed = true;
                    }
                }
            }
        }
        else
        {
            // remove single item.
            MemoryElementDescriptor me =
                ( MemoryElementDescriptor ) map.remove( key );

            if ( me != null )
            {
                removeNode( me );
                removed = true;
            }
        }

        return removed;
    }

    public class IteratorWrapper
        implements Iterator
    {
        private final Log log = LogFactory.getLog( LRUMemoryCache.class );
        private final Iterator i;

        private IteratorWrapper(Map m)
        {
            i = m.entrySet().iterator();
        }
        public boolean hasNext()
        {
            return i.hasNext();
        }
        public Object next()
        {
            return new MapEntryWrapper((Map.Entry)i.next());
        }
        public void remove()
        {
            i.remove();
        } 
        public boolean equals(Object o)
        {
            return i.equals(o);
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
        private MapEntryWrapper(Map.Entry e)
        {
            this.e = e;
        }

        public boolean equals(Object o)
        {
            return e.equals(o);
        }
        public Object getKey()
        {
            return e.getKey();
        }
        public Object getValue()
        {
            return ((MemoryElementDescriptor)e.getValue()).ce;
        }
        public int hashCode()
        {
            return e.hashCode();
        }
        public Object setValue(Object value)
        {
            throw new UnsupportedOperationException("Use normal cache methods"
                + " to alter the contents of the cache.");
        }
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     *
     * @return The iterator value
     */
    public Iterator getIterator()
    {
        return new IteratorWrapper(map);
    }

    /**
     *  Get an Array of the keys for all elements in the memory cache
     *
     *@return    An Object[]
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
     *  Removes the specified node from the link list.
     *
     *@param  me  Description of the Parameter
     */
    private synchronized void removeNode( MemoryElementDescriptor me )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing node " + me.ce.getKey() + " from cache " +
                       cacheName );
        }

        if ( me.next == null )
        {
            if ( me.prev == null )
            {
                // Make sure it really is the only node before setting head and
                // tail to null. It is possible that we will be passed a node
                // which has already been removed from the list, in which case
                // we should ignore it

                if ( me == first && me == last )
                {
                    first = last = null;
                }
            }
            else
            {
                // last but not the first.
                last = me.prev;
                last.next = null;
                me.prev = null;
            }
        }
        else if ( me.prev == null )
        {
            // first but not the last.
            first = me.next;
            first.prev = null;
            me.next = null;
        }
        else
        {
            // neither the first nor the last.
            me.prev.next = me.next;
            me.next.prev = me.prev;
            me.prev = me.next = null;
        }
    }

    /**
     *  Adds a new node to the end of the link list. Currently not used.
     *
     *@param  ce  The feature to be added to the Last
     */
    private void addLast( CacheElement ce )
    {
        MemoryElementDescriptor me = new MemoryElementDescriptor( ce );

        if ( first == null )
        {
            // empty list.
            first = me;
        }
        else
        {
            last.next = me;
            me.prev = last;
        }
        last = me;
        verifyCache(ce.getKey());
    }

    /**
     *  Adds a new node to the start of the link list.
     *
     *@param  ce  The feature to be added to the First
     */
    private synchronized void addFirst( ICacheElement ce )
    {

        MemoryElementDescriptor me = new MemoryElementDescriptor( ce );

        if ( last == null )
        {
            // empty list.
            last = me;
        }
        else
        {
            first.prev = me;
            me.next = first;
        }
        first = me;
        return;
    }

    /**
     *  Moves an existing node to the start of the link list.
     *
     *@param  ce  Description of the Parameter
     */
    public void makeFirst( ICacheElement ce )
    {
        makeFirst( new MemoryElementDescriptor( ce ) );
    }

    /**
     *  Moves an existing node to the start of the link list.
     *
     *@param  me  Description of the Parameter
     */
    public synchronized void makeFirst( MemoryElementDescriptor me )
    {
        if ( me.prev == null )
        {
            // already the first node.
            return;
        }
        me.prev.next = me.next;
        
        if ( me.next == null )
        {
            // last but not the first.
            last = me.prev;
            last.next = null;
        }
        else
        {
            // neither the last nor the first.
            me.next.prev = me.prev;
        }
        first.prev = me;
        me.next = first;
        me.prev = null;
        first = me;
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
            Map.Entry e = ( Map.Entry ) itr.next();
            MemoryElementDescriptor me = ( MemoryElementDescriptor ) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     *  Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        for ( MemoryElementDescriptor me = first; me != null; me = me.next )
        {
            log.debug( "dumpCacheEntries> key="
                 + me.ce.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    private int dumpCacheSize() 
    {
        int size = 0;
        for ( MemoryElementDescriptor me = first; me != null;  me = me.next )
        {
            size++;
        }
        return size; 
    }

    private void verifyCache() 
    {
        if ( !log.isDebugEnabled() ) 
            return;

        boolean found = false;
        log.debug("verifycache[" + cacheName + "]: mapContains " + map.size() + " elements, linked list contains " 
                  + dumpCacheSize() + " elements" );
        log.debug("verifycache: checking linked list by key ");
        for ( MemoryElementDescriptor li = first; li != null; li = li.next ) 
        {
            Object key = li.ce.getKey();
            if ( !map.containsKey(key) ) 
            {
                log.error("verifycache[" + cacheName + "]: map does not contain key : " + li.ce.getKey());
                log.error("li.hashcode=" + li.ce.getKey().hashCode());
                log.error("key class=" + key.getClass());
                log.error("key hashcode=" + key.hashCode());
                log.error("key toString=" + key.toString());
                if ( key instanceof GroupAttrName ) 
                {
                    GroupAttrName name = (GroupAttrName) key;
                    log.error("GroupID hashcode=" + name.groupId.hashCode());
                    log.error("GroupID.class=" + name.groupId.getClass());
                    log.error("AttrName hashcode=" + name.attrName.hashCode());
                    log.error("AttrName.class=" + name.attrName.getClass());
                }
                dumpMap();
            }
            else if ( map.get(li.ce.getKey()) == null ) 
            {
                log.error("verifycache[" + cacheName +
                          "]: linked list retrieval returned null for key: " +
                          li.ce.getKey());
            }
        }

        log.debug("verifycache: checking linked list by value ");
        for ( MemoryElementDescriptor li3 = first; li3 != null; li3 = li3.next ) 
        {
            if ( map.containsValue(li3) == false ) 
            {
                log.error("verifycache[" + cacheName + "]: map does not contain value : " + li3);
                dumpMap();
            } 
        }

        log.debug("verifycache: checking via keysets!");
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
                log.error("verifycache: no such element exception");
            }                

            for ( MemoryElementDescriptor li2 = first; li2 != null; li2 = li2.next ) 
            {
                if ( val.equals(li2.ce.getKey()) )
                {
                    found = true;
                    break;
                }
            }
            if ( !found ) 
            {
                log.error("verifycache[" + cacheName + "]: key not found in list : " + val );
                dumpCacheEntries();
                if ( map.containsKey(val) ) 
                {
                    log.error("verifycache: map contains key");
                }
                else 
                {
                    log.error("verifycache: map does NOT contain key, what the HECK!");
                }
            }
        }
    }

    private void verifyCache(Serializable key) 
    {
        if ( !log.isDebugEnabled() ) 
            return;

        boolean found = false;

        // go through the linked list looking for the key
        for ( MemoryElementDescriptor li = first; li != null; li = li.next ) 
        {
            if ( li.ce.getKey() == key ) 
            {
                found = true;
                log.debug("verifycache(key) key match: " + key );
                break;
            }
        }
        if ( !found ) 
        {
            log.error("verifycache(key)[" + cacheName + "], couldn't find key! : " + key );
        }
    }
}

/**
 * needed for memory cache element LRU linked lisk
 */
class MemoryElementDescriptor implements Serializable
{
    /** Description of the Field */
    public MemoryElementDescriptor prev, next;
    /** Description of the Field */
    public ICacheElement ce;

    /**
     * Constructor for the MemoryElementDescriptor object
     *
     * @param ce
     */
    public MemoryElementDescriptor( ICacheElement ce )
    {
        this.ce = ce;
    }
}
