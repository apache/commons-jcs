package org.apache.jcs.engine.memory.shrinking;

import java.io.IOException;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.CacheElement;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheHub;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is mainly intended as a tester for the ShrinkerThread
 *
 * @author asmuts
 * @created February 18, 2002
 */
public class ShrinkingMemoryCache implements IMemoryCache, ICache, Serializable
{
    private final static Log log =
        LogFactory.getLog( ShrinkingMemoryCache.class );

    // MOVE TO MEMORYMANAGER
    String cacheName;

    /**
     * The item store
     */
    protected Map map = new HashMap();

    /**
     * Region Elemental Attributes
     */
    public IElementAttributes attr;

    /**
     * Cache Attributes
     */
    public ICompositeCacheAttributes cattr;

    // The HUB
    ICacheHub hub;

    // status
    private int status = this.STATUS_ERROR;

    private ShrinkerThread shrinker;

    // for reflection
    /**
     * Constructor for the ShrinkingMemoryCache object
     */
    public ShrinkingMemoryCache()
    {
        // might want to consider this an error state
        status = this.STATUS_ERROR;
    }

    // should use this method

    /**
     * Constructor for the ShrinkingMemoryCache object
     *
     * @param cacheName
     * @param cattr
     * @param hub
     */
    public ShrinkingMemoryCache( String cacheName, ICompositeCacheAttributes cattr, ICacheHub hub )
    {
        initialize( cacheName, cattr, hub );
    }


    // for post reflection creation initialization
    /**
     * Description of the Method
     *
     * @param cacheName
     * @param cattr
     * @param hub
     */
    public void initialize( String cacheName, ICompositeCacheAttributes cattr, ICacheHub hub )
    {
        this.cacheName = cacheName;
        this.cattr = cattr;
        this.hub = hub;
        status = this.STATUS_ALIVE;

        if ( cattr.getUseMemoryShrinker() )
        {
            shrinker = new ShrinkerThread( this );
            shrinker.setPriority( shrinker.MIN_PRIORITY );
            shrinker.start();
        }

        log.info( "initialized ShrinkingMemoryCache for " + cacheName );
    }

    /**
     * Gets the cacheType attribute of the ShrinkingMemoryCache object
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

        try
        {

            MemoryElementDescriptor me = new MemoryElementDescriptor( ce );
            ce.getElementAttributes().setLastAccessTimeNow();
            map.put( ce.getKey(), me );

        }
        catch ( Exception ex )
        {
            // impossible case.
            ex.printStackTrace();
            throw new IllegalStateException( ex.getMessage() );
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
            me.ce.getElementAttributes().setLastAccessTimeNow();
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
     * Description of the Method
     *
     * @param me
     * @exception IOException
     */
    /**
     * Description of the Method
     *
     * Description of the Method Puts an item to the cache.
     *
     */
    public void waterfal( MemoryElementDescriptor me )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Spooling item to disk -- " + me.ce.getKey() );
        }
        this.hub.spoolToDisk( me.ce );
    }


    /**
     * Prepares for shutdown.
     *
     * @exception IOException
     */
    public void dispose()
        throws IOException
    {
        shrinker.kill();
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
     * Gets the iterator attribute of the ShrinkingMemoryCache object
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

}
