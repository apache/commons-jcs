package org.apache.jcs.engine.memory.behavior;

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheHub;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.ICompositeCache;

import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.control.CompositeCache;

/**
 *  For the framework. Insures methods a MemoryCache needs to access.
 *
 *@author     asmuts
 *@created    January 15, 2002
 */
public interface IMemoryCache extends ICacheType
{

    // for initialization
    /**
     *  Description of the Method
     *
     *@param  cacheName  Description of the Parameter
     *@param  cattr      Description of the Parameter
     *@param  cache      Description of the Parameter
     */
    public void initialize( String cacheName,
                            ICompositeCacheAttributes cattr,
                            CompositeCache cache );

    // TODO: need a setCacheAttributes or reInitialize method

    /**
     *  Description of the Method
     *
     *@return    The size value
     */
    //public void makeFirst( MemoryElementDescriptor me );


    //public void moveToMemory( ICacheElement ce );

    /**
     *  Gets the size attribute of the IMemoryCache object
     *
     *@return    The size value
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
     *  Get an Array of the keys for all elements in the memory cache
     *
     *@return    An Object[]
     */
    public Object[] getKeyArray();

    /**
     *  Removes an item from the cache.
     *
     *@param  key              Description of the Parameter
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
     *  Description of the Method
     *
     *@param  key              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public Serializable get( Serializable key )
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
     *  Description of the Method
     *
     *@param  key              Description of the Parameter
     *@param  container        Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public Serializable get( Serializable key, boolean container )
        throws IOException;


    /**
     *  Throws an item out of memory, if there is a disk cache it will be
     *  spooled.
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
     *  Returns the CacheAttributes.
     *
     *@return    The cacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     *  Sets the CacheAttributes.
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
