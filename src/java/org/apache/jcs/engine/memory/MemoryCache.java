package org.apache.jcs.engine.memory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.control.CompositeCache;

/**
 *  For the framework. Insures methods a MemoryCache needs to access. Not sure
 *  why we use this. Should use teh IMemeoryCache interface. I'll change it
 *  later.
 *
 *@author     <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 *@author     <a href="mailto:jtaylor@apache.org">James Taylor</a>
 *@created    May 13, 2002
 *@version    $Id$
 */
public interface MemoryCache
{
    /**
     *  Initialize the memory cache
     *
     *@param  cache  The cache (region) this memory store is attached to.
     */
    public void initialize( CompositeCache cache );

    /**
     *  Get the number of elements contained in the memory store
     *
     *@return    Element count
     */
    public int getSize();

    /**
     *  Get an iterator for all elements in the memory cache. This should be
     *  removed since it is fairly dangerous. Other classes should not be able
     *  to directly access items in the memory cache.
     *
     *@return        An iterator
     *@deprecated
     */
    public Iterator getIterator();


    /**
     *  Get an Array of the keys for all elements in the memory cache.
     *
     *@return    Object[]
     *@TODO      This should probably be done in chunks with a range pased in.
     *      This will be a problem if someone puts a 1,000,000 or so items in a
     *      region.
     */
    public Object[] getKeyArray();

    /**
     *  Removes an item from the cache
     *
     *@param  key              Identifies item to be removed
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public boolean remove( Serializable key )
        throws IOException;

    /**
     *  Removes all cached items from the cache.
     *
     *@exception  IOException  Description of the Exception
     */
    public void removeAll()
        throws IOException;

    /**
     *  Get an item from the cache
     *
     *@param  key              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public ICacheElement get( Serializable key )
        throws IOException;

    /**
     *  Get an item from the cache without effecting its order or last access
     *  time
     *
     *@param  key              Description of the Parameter
     *@return                  The quiet value
     *@exception  IOException  Description of the Exception
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException;

    /**
     *  Spools the item contained in the provided element to disk
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void waterfal( ICacheElement ce )
        throws IOException;


    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void update( ICacheElement ce )
        throws IOException;

    /**
     *  Returns the CacheAttributes for the region.
     *
     *@return    The cacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     *  Sets the CacheAttributes of the region.
     *
     *@param  cattr  The new cacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );


    /**
     *  Gets the cache hub / region taht the MemoryCache is used by
     *
     *@return    The cache value
     */
    public ICompositeCache getCompositeCache();

}
