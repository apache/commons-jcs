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
 * A fast reference management system. The least recently used items move to the
 * end of the list and get spooled to disk if the cache hub is configured to use
 * a disk cache. Most of the cache bottelnecks ar ein IO. There are no io
 * bottlenecks here, it's all about processing power. Even though there are only
 * a few adjustments necessary to maintain the double linked list, we might want
 * to find a more efficient memory manager for large cache regions. The
 * LRUMemoryCache is most efficeint when the first element is selected. The
 * smaller teh region, the better the chance that this will be the case. < .04
 * ms per put, p3 866, 1/10 of that per get
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @version $Id$
 */
public class LRUMemoryCache implements MemoryCache, Serializable
{
    private final static Log log =
        LogFactory.getLog( LRUMemoryCache.class );

    String cacheName;

    /** Map where items are stored by key */
    protected Map map = new Hashtable();

    // LRU double linked list head/tail nodes
    private MemoryElementDescriptor first;
    private MemoryElementDescriptor last;

    private int max;

    /**
     * Region Elemental Attributes
     */
    public IElementAttributes attr;

    /**
     * Cache Attributes
     */
    public ICompositeCacheAttributes cattr;

    /**
     * The cache this store is associated with
     */
    Cache cache;

    // status
    private int status = CacheConstants.STATUS_ERROR;

    // make configurable
    private int chunkSize = 2;

    /**
     * The background memory shrinker
     */
    private ShrinkerThread shrinker;


    /**
     * Constructor for the LRUMemoryCache object
     */
    public LRUMemoryCache()
    {
        status = CacheConstants.STATUS_ERROR;
    }

    /**
     * For post reflection creation initialization
     *
     * @param hub
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
     * Puts an item to the cache.
     *
     * @param ce
     * @exception IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {

        // asynchronisly create a MemoryElement
        ce.getElementAttributes().setLastAccessTimeNow();
        addFirst( ce );
        MemoryElementDescriptor old = ( MemoryElementDescriptor ) map.put( ce.getKey(), first );

        if ( first.equals( old ) )
        {
            // the same as an existing item.
            removeNode( old );
        }

        // save a microsecond on the second call.
        int size = map.size();
        // need to spool at a certain percentage synchronously
        if ( size < this.cattr.getMaxObjects() )
        {
            return;
        }
        else
        {

            // SPOOL LAST -- need to make this a grouping in a queue
            log.debug( "IN RAM overflow" );

            // write the last item to disk.
            try
            {

                // PUSH 5 TO DISK TO MINIMIZE THE TYPICAL
                int chunkSizeCorrected = Math.min( size, chunkSize );

                if ( log.isDebugEnabled() )
                {
                    log.debug( "update: About to spool to disk cache, map.size() = " + size + ", this.cattr.getMaxObjects() = " + this.cattr.getMaxObjects() + ", chunkSizeCorrected = " + chunkSizeCorrected );
                }

                // The spool will put them in a disk event queue, so there is no
                // need to pre-queue the queuing.  This would be a bit wasteful
                // and wouldn't save much time in this synchronous call.
                for ( int i = 0; i < chunkSizeCorrected; i++ )
                {
                    // Might want to rename this "overflow" incase the hub
                    // wants to do something else.
                    cache.spoolToDisk( last.ce );
                    map.remove( last.ce.getKey() );
                    removeNode( last );
                }

                if ( log.isDebugEnabled() )
                {
                    log.debug( "update: After spool, put " + last.ce.getKey() + " on disk cache, map.size() = " + size + ", this.cattr.getMaxObjects() = " + this.cattr.getMaxObjects() + ", chunkSizeCorrected = " + chunkSizeCorrected );
                }

            }
            catch ( Exception ex )
            {
                // impossible case.
                ex.printStackTrace();
                throw new IllegalStateException( ex.getMessage() );
            }

        }

    }

    /**
     * Description of the Method
     *
     * @return
     * @param key
     * @exception IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException
    {
        MemoryElementDescriptor me = null;
        ICacheElement ce = null;
        boolean found = false;

        try
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "get: key = " + key );
            }

            me = ( MemoryElementDescriptor ) map.get( key );
            if ( log.isDebugEnabled() )
            {
                log.debug( "me =" + me );
            }

            if ( me == null )
            {

            }
            else
            {
                found = true;
                ce = me.ce;
                //ramHit++;
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
                //miss++;
                if ( log.isDebugEnabled() )
                {
                    log.debug( cacheName + " -- MISS for " + key );
                }
                return null;
                //throw new ObjectNotFoundException( key + " not found in cache" );
            }
        }
        catch ( Exception e )
        {
            log.error( "Error handling miss", e );
            return null;
        }

        try
        {
            ce.getElementAttributes().setLastAccessTimeNow();
            makeFirst( me );
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
     *
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
            //+, nonLocal="+nonLocal);
        }

        //p("remove> key="+key+", nonLocal="+nonLocal);
        boolean removed = false;

        // handle partial removal
        if ( key instanceof String
            && key.toString().endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry entry = ( Map.Entry ) itr.next();
                    Object k = entry.getKey();
                    if ( k instanceof String && k.toString().startsWith( key.toString() ) )
                    {
                        itr.remove();
                        removeNode( ( MemoryElementDescriptor ) entry.getValue() );
                        removed = true;
                    }
                }
            }
        }
        else
        {
            // remove single item.
            MemoryElementDescriptor ce = ( MemoryElementDescriptor ) map.remove( key );
            if ( ce != null )
            {
                removeNode( ce );
                removed = true;
            }
        }
        // end else not hierarchical removal
        return removed;
    }

    /**
     * Removes all cached items from the cache.
     *
     * @exception IOException
     */
    public void removeAll()
        throws IOException
    {
        map = new HashMap();
    }

    /**
     * Prepares for shutdown.
     *
     * @exception IOException
     */
    public void dispose()
        throws IOException
    {
    }

    /**
     * Returns the cache statistics.
     *
     * @return The stats value
     */
    public String getStats()
    {
        return "";
    }

    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    public int getSize()
    {
        return this.map.size();
    }

    /**
     * Returns the cache status.
     *
     * @return The status value
     */
    public int getStatus()
    {
        return this.status;
    }

    /**
     * Returns the cache name.
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cattr.getCacheName();
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     *
     * @return The iterator value
     */
    public Iterator getIterator()
    {
        return map.entrySet().iterator();
    }

    // -------------------------------------------------------- internal mehods

    /**
     * Removes the specified node from the link list.
     *
     * @param me
     */
    private void removeNode( MemoryElementDescriptor me )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing node " + me.ce.getKey() );
        }

        if ( me.next == null )
        {
            if ( me.prev == null )
            {
                // the only node.
                first = last = null;
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
     * Adds a new node to the end of the link list. Currently not used.
     *
     * @param ce The feature to be added to the Last attribute
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
     * Adds a new node to the start of the link list.
     *
     * @param ce The feature to be added to the First attribute
     */
    private void addFirst( ICacheElement ce )
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
     * Moves an existing node to the start of the link list.
     *
     * @param ce
     */
    public synchronized void makeFirst( ICacheElement ce )
    {
        makeFirst( new MemoryElementDescriptor( ce ) );
    }

    /**
     * Moves an existing node to the start of the link list.
     *
     * @param me
     */
    public synchronized void makeFirst( MemoryElementDescriptor me )
    {

        // MemoryElementDescriptor me = new MemoryElementDescriptor(ce);

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

    /**
     * Puts an item to the cache.
     *
     * @param me
     * @exception IOException
     */
    public void waterfal( MemoryElementDescriptor me )
        throws IOException
    {
        this.cache.spoolToDisk( me.ce );
    }

    /**
     * Returns the CacheAttributes.
     *
     * @return The CacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cattr;
    }

    /**
     * Sets the CacheAttributes.
     *
     * @param cattr The new CacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cattr = cattr;
    }

    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
            //for ( Iterator itr = memCache.getIterator(); itr.hasNext();) {
            Map.Entry e = ( Map.Entry ) itr.next();
            MemoryElementDescriptor me = ( MemoryElementDescriptor ) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        for ( MemoryElementDescriptor me = first; me != null; me = me.next )
        {
            log.debug( "dumpCacheEntries> key=" + me.ce.getKey() + ", val=" + me.ce.getVal() );
        }
    }
}
