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
import org.apache.jcs.engine.control.Cache;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.memory.shrinking.ShrinkerThread;

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
 *@created    May 13, 2002
 *@version    $Id$
 */
public class LRUMemoryCache implements MemoryCache, Serializable
{
    private final static Log log =
        LogFactory.getLog( LRUMemoryCache.class );

    String cacheName;

    /**
     *  Map where items are stored by key
     */
    protected Map map = new Hashtable();

    // LRU double linked list head/tail nodes
    private MemoryElementDescriptor first;
    private MemoryElementDescriptor last;

    private int max;

    /**
     *  Region Elemental Attributes, used as a default.
     */
    public IElementAttributes attr;

    /**
     *  Cache Attributes
     */
    public ICompositeCacheAttributes cattr;

    /**
     *  The cache region this store is associated with
     */
    Cache cache;

    // status
    private int status = CacheConstants.STATUS_ERROR;

    // make configurable
    private int chunkSize = 2;

    /**
     *  The background memory shrinker
     */
    private ShrinkerThread shrinker;

    /**
     *  Constructor for the LRUMemoryCache object
     */
    public LRUMemoryCache()
    {
        status = CacheConstants.STATUS_ERROR;
    }

    /**
     *  For post reflection creation initialization
     *
     *@param  hub
     */
    public synchronized void initialize( Cache hub )
    {
        this.cacheName = hub.getCacheName();
        this.cattr = hub.getCacheAttributes();
        this.max = this.cattr.getMaxObjects();
        this.cache = hub;

        status = CacheConstants.STATUS_ALIVE;

        log.info( "initialized LRUMemoryCache for " + cacheName );

        if ( cattr.getUseMemoryShrinker() && shrinker == null )
        {
            shrinker = new ShrinkerThread( this );
            shrinker.setPriority( shrinker.MIN_PRIORITY );
            shrinker.start();
        }
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
        MemoryElementDescriptor me = null;
        ICacheElement ce = null;

        try
        {

            me = ( MemoryElementDescriptor ) map.get( key );
            if ( me != null )
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + ": LRUMemoryCache quiet hit for " + key );
                }

                ce = me.ce;

            }
            else
            {
                log.debug( cacheName + ": LRUMemoryCache quiet miss for " + key );
            }

        }
        catch ( Exception e )
        {
            log.error( e );
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
        MemoryElementDescriptor me = null;
        ICacheElement ce = null;

        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "getting item for key: " + key );
            }

            me = ( MemoryElementDescriptor ) map.get( key );

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
        }
        catch ( Exception e )
        {
            log.error( e );
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
        else
        {
            // remove single item.
            MemoryElementDescriptor ce =
                ( MemoryElementDescriptor ) map.remove( key );

            if ( ce != null )
            {
                removeNode( ce );
                removed = true;
            }
        }

        return removed;
    }

    /**
     *  Removes all cached items from the cache.
     *
     *@exception  IOException
     */
    public void removeAll()
        throws IOException
    {
        map = new HashMap();
    }

    /**
     *  Prepares for shutdown.
     *
     *@exception  IOException
     */
    public void dispose()
        throws IOException { }

    /**
     *  Returns the current cache size.
     *
     *@return    The size value
     */
    public int getSize()
    {
        return this.map.size();
    }

    /**
     *  Returns the cache status.
     *
     *@return    The status value
     */
    public int getStatus()
    {
        return this.status;
    }

    /**
     *  Returns the cache name.
     *
     *@return    The cacheName value
     */
    public String getCacheName()
    {
        return this.cattr.getCacheName();
    }

    /**
     *  Gets the iterator attribute of the LRUMemoryCache object
     *
     *@return    The iterator value
     */
    public Iterator getIterator()
    {
        return map.entrySet().iterator();
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


//    /**
//     *  Get an Array of the keys for elements in the specified range of
//     *  the memory cache.  If the end position is greater than the size of the
//     *  Map, the method will return an array of the remaining elements after
//     *  the start position.  If the start element is greater than the size of
//     *  Map, a error will be thrown.
//     *
//     *@return    An Object[]
//     */
//    public Object[] getKeyArray(int start, int end) throws java.lang.IllegalArgumentException
//    {
//
//        int size = getSize();
//        if ( start >= size ) {
//          throw new java.lang.IllegalArgumentException( "Start value is greater than the size of the cache" );
//        }
//        int stop = Math.min( size, end );
//        int count = 0;
//
//        // need a better locking strategy here.
//        synchronized ( this )
//        {
//          Object[] result = new Object[stop-start];
//          Iterator e = this.map.keySet().iterator();
//          for (int i=0; e.hasNext(); i++)
//          {
//              if ( i >= start && i < stop ) {
//                result[count] = e.next();
//                count++;
//              }
//              if ( i >= stop ) {
//                continue;
//              }
//          }
//          return result;
//        }
//    }


    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException
     */
    public void waterfal( ICacheElement ce )
        throws IOException
    {
        this.cache.spoolToDisk( ce );
    }


    /**
     *  Returns the CacheAttributes.
     *
     *@return    The CacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cattr;
    }

    /**
     *  Sets the CacheAttributes.
     *
     *@param  cattr  The new CacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cattr = cattr;
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
        try
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
        catch ( Exception e )
        {
            log.error( "Couldn't make first", e );
        }
        return;
    }

    // ---------------------------------------------------------- debug methods

    /**
     *  Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext();  )
        {
            //for ( Iterator itr = memCache.getIterator(); itr.hasNext();) {
            Map.Entry e = ( Map.Entry ) itr.next();
            MemoryElementDescriptor me =
                ( MemoryElementDescriptor ) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey()
                 + ", val=" + me.ce.getVal() );
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
