package org.apache.jcs.engine.memory.mru;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.control.Cache;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheHub;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

import org.apache.jcs.engine.memory.shrinking.ShrinkerThread;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A SLOW AS HELL reference management system. The most recently used items move
 * to the front of the list and get spooled to disk if the cache hub is
 * configured to use a disk cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class MRUMemoryCache
     implements IMemoryCache, ICache, Serializable
{
    private final static Log log =
        LogFactory.getLog( MRUMemoryCache.class );

    // MOVE TO MEMORYMANAGER
    String cacheName;

    /**
     * Storage of cache items.
     */
    protected HashMap map = new HashMap();

    /**
     * Description of the Field
     */
    protected int[] lockMe = new int[0];

    /**
     * MRU list.
     */
    protected LinkedList mrulist = new LinkedList();

    // Region Elemental Attributes
    /**
     * Description of the Field
     */
    public IElementAttributes attr;

    // Cache Attributes
    /**
     * Description of the Field
     */
    public ICompositeCacheAttributes cattr;

    // The HUB
    Cache cache;

    // status
    private int status = this.STATUS_ERROR;

    // make configurable
    private int chunkSize = 2;

    /**
     * The background memory shrinker
     */
    private ShrinkerThread shrinker;


    // for reflection
    /**
     * Constructor for the LRUMemoryCache object
     */
    public MRUMemoryCache()
    {
        // might want to consider this an error state
        status = this.STATUS_ERROR;
    }


    /**
     * Constructor for the LRUMemoryCache object
     *
     * @param cacheName
     * @param cattr
     * @param hub
     */
    public MRUMemoryCache( String cacheName, ICompositeCacheAttributes cattr, Cache cache )
    {
        initialize( cacheName, cattr, cache );
    }


    // for post reflection creation initialization
    /**
     * Description of the Method
     *
     * @param cacheName
     * @param cattr
     * @param hub
     */
    public void initialize( String cacheName, ICompositeCacheAttributes cattr, Cache cache )
    {
        this.cacheName = cacheName;
        this.cattr = cattr;
        this.cache = cache;
        status = this.STATUS_ALIVE;

        if ( cattr.getUseMemoryShrinker() )
        {
            if ( shrinker == null )
            {
                synchronized ( MRUMemoryCache.class )
                {
                    if ( shrinker == null )
                    {
                        shrinker = new ShrinkerThread( this );
                        shrinker.setPriority( shrinker.MIN_PRIORITY );
                        shrinker.start();
                    }
                }
            }
        }

    }

    /**
     * Gets the cacheType attribute of the LRUMemoryCache object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return ICacheType.MEMORY_CACHE;
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
                mrulist.remove( ( String ) key );
            }
            mrulist.addFirst( ( String ) key );
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

            log.debug( "In RAM overflow" );

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
                    Serializable last = ( Serializable ) mrulist.getLast();
                    ICacheElement ceL = ( ICacheElement ) map.get( last );
                    cache.spoolToDisk( ceL );

                    // need a more fine grained locking here
                    synchronized ( map )
                    {
                        map.remove( last );
                        mrulist.remove( last );
                    }
                }

                if ( log.isDebugEnabled() )
                {
                    log.debug( "update: After spool,  map.size() = " + size + ", this.cattr.getMaxObjects() = " + this.cattr.getMaxObjects() + ", chunkSizeCorrected = " + chunkSizeCorrected );
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
    // end update

    // TODO: Implement or modify interface, just implement
    // may need insert if we want to distinguish b/wn put and replace
    /**
     * Description of the Method
     *
     * @param key
     * @param val
     * @exception IOException
     */
    public void put( Serializable key, Serializable val )
        throws IOException
    {
        // not used
    }


    /**
     * Description of the Method
     *
     * @param key
     * @param val
     * @param attr
     * @exception IOException
     */
    public void put( Serializable key, Serializable val, IElementAttributes attr )
        throws IOException
    {
        // not used
    }


    /**
     * Gets an item from the cache.
     *
     * @return
     * @param key
     * @exception IOException
     */
    public Serializable get( Serializable key )
        throws IOException
    {
        return get( key, true );
    }


    /**
     * Description of the Method
     *
     * @return
     * @param key
     * @param container
     * @exception IOException
     */
    public Serializable get( Serializable key, boolean container )
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

            ce = ( ICacheElement ) map.get( key );
            if ( log.isDebugEnabled() )
            {
                log.debug( "ce =" + ce );
            }

            if ( ce == null )
            {

            }
            else
            {
                found = true;
                ce.getElementAttributes().setLastAccessTimeNow();
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
        if ( key instanceof String && key.toString().endsWith( NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext();  )
                {
                    Map.Entry entry = ( Map.Entry ) itr.next();
                    Object k = entry.getKey();
                    if ( k instanceof String && k.toString().startsWith( key.toString() ) )
                    {
                        itr.remove();
                        Serializable keyR = ( ICacheElement ) entry.getKey();
                        map.remove( keyR );
                        mrulist.remove( keyR );
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
        //return this.STATUS_ALIVE;
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
     * Gets the iterator attribute of the LRUMemoryCache object
     *
     * @return The iterator value
     */
    public Iterator getIterator()
    {
        //return Collections.enumeration(map.entrySet());
        return map.entrySet().iterator();
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
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext();  )
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
        ListIterator li = mrulist.listIterator();
        while ( li.hasNext() )
        {
            Serializable key = ( Serializable ) li.next();
            log.debug( "dumpCacheEntries> key=" + key + ", val=" + ( ( ICacheElement ) map.get( key ) ).getVal() );
        }
    }
}
