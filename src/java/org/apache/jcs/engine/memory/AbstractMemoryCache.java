package org.apache.jcs.engine.memory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.shrinking.ShrinkerThread;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.control.group.GroupAttrName;

/**
 *  Some common code for the LRU and MRU caches.
 *
 *@author     <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 *@author     <a href="mailto:jtaylor@apache.org">James Taylor</a>
 *@author     <a href="mailto:jmcnally@apache.org">John McNally</a>
 *@created    May 13, 2002
 *@version    $Id$
 */
public abstract class AbstractMemoryCache 
    implements MemoryCache, Serializable
{
    private final static Log log =
        LogFactory.getLog( AbstractMemoryCache.class );

    protected String cacheName;

    /**
     *  Map where items are stored by key
     */
    protected Map map;

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
    protected CompositeCache cache;

    // status
    protected int status;

    // make configurable
    protected int chunkSize = 2;

    /**
     *  The background memory shrinker
     */
    private ShrinkerThread shrinker;


    /**
     *  Constructor for the LRUMemoryCache object
     */
    public AbstractMemoryCache()
    {
        status = CacheConstants.STATUS_ERROR;
        map = new Hashtable();
    }

    /**
     *  For post reflection creation initialization
     *
     *@param  hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        this.cacheName = hub.getCacheName();
        this.cattr = hub.getCacheAttributes();
        this.cache = hub;

        status = CacheConstants.STATUS_ALIVE;

        if ( cattr.getUseMemoryShrinker() && shrinker == null )
        {
            shrinker = new ShrinkerThread( this );
            shrinker.setPriority( shrinker.MIN_PRIORITY );
            shrinker.start();
        }
    }

    /**
     *  Removes an item from the cache
     *
     *@param  key              Identifies item to be removed
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public abstract boolean remove( Serializable key )
        throws IOException;

    /**
     *  Get an item from the cache
     *
     *@param  key              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public abstract ICacheElement get( Serializable key )
        throws IOException;

    /**
     *  Get an item from the cache without effecting its order or last access
     *  time
     *
     *@param  key              Description of the Parameter
     *@return                  The quiet value
     *@exception  IOException  Description of the Exception
     */
    public abstract ICacheElement getQuiet( Serializable key )
        throws IOException;

    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public abstract void update( ICacheElement ce )
        throws IOException;


    /**
     *  Get an Array of the keys for all elements in the memory cache
     *
     *@return    An Object[]
     */
    public abstract Object[] getKeyArray();


    /**
     * Removes all cached items from the cache.
     *
     * @exception IOException
     */
    public void removeAll()
        throws IOException
    {
        map = new Hashtable();
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
    public void waterfal( ICacheElement ce )
        throws IOException
    {
        this.cache.spoolToDisk( ce );
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
     *  Gets the cache hub / region taht the MemoryCache is used by
     *
     *@return    The cache value
     */
    public CompositeCache getCompositeCache()
    {
        return this.cache;
    }

    public Set getGroupKeys(String groupName)
    {
        GroupId groupId = new GroupId(getCacheName(), groupName);
        HashSet keys = new HashSet();
        synchronized ( map )
        {
            for (Iterator itr = map.entrySet().iterator(); itr.hasNext();)
            {
                Map.Entry entry = (Map.Entry) itr.next();
                Object k = entry.getKey();

                if ( k instanceof GroupAttrName
                     && ((GroupAttrName)k).groupId.equals(groupId) )
                {
                    keys.add(((GroupAttrName)k).attrName);
                }
            }
        }
        return keys;
    }
}
