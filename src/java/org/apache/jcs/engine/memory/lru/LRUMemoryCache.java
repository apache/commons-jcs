package org.apache.jcs.engine.memory.lru;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.memory.MemoryCache;
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
 *@author     <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 *@author     <a href="mailto:jtaylor@apache.org">James Taylor</a>
 *@author     <a href="mailto:jmcnally@apache.org">John McNally</a>
 *@created    May 13, 2002
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
            ( MemoryElementDescriptor ) map.put( ce.getKey(), first );

        // If the node was the same as an existing node, remove it.

        if ( first.equals( old ) )
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

            MemoryElementDescriptor node;

            for ( int i = 0; i < chunkSizeCorrected; i++ )
            {
                synchronized ( this )
                {
                    cache.spoolToDisk( last.ce );

                    map.remove( last.ce.getKey() );

                    removeNode( last );
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "After spool map size: " + size );
            }
        }
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
    public ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item for key: " + key );
        }
        
        MemoryElementDescriptor me = (MemoryElementDescriptor) map.get(key);

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
    public boolean remove( Serializable key )
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
        /*
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
        */
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
            log.debug( "removing node " + me.ce.getKey() );
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
        return;
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
