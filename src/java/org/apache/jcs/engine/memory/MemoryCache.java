package org.apache.jcs.engine.memory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.Cache;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 * Not sure why we use this.  Should use teh IMemeoryCache interface.
 * I'll change it later.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @version $Id$
 */
public interface MemoryCache
{
    /**
     * Initialize the memory cache
     *
     * @param cache The cache (region) this memory store is attached to.
     */
    public void initialize( Cache cache );

    /**
     * Get the number of elements contained in the memory store
     *
     * @return Element count
     */
    public int getSize();

    /**
     * Get an iterator for all elements in the memory cache
     *
     * @return An iterator
     */
    public Iterator getIterator();


    /**
     * Get an Array of the keys for all elements in the memory cache
     *
     * @return Object[]
     */
    public Object[] getKeyArray();

    /**
     * Removes an item from the cache
     *
     * @param key Identifies item to be removed
     */
    public boolean remove( Serializable key ) throws IOException;

    /**
     * Removes all cached items from the cache.
     */
    public void removeAll() throws IOException;

    /** Get an item from the cache */
    public ICacheElement get( Serializable key )
        throws IOException;

    /** Get an item from the cache without effecting its order or last access time */
    public ICacheElement getQuiet( Serializable key )
        throws IOException;

    /** Spools the item contained in the provided element to disk */
//    public void waterfal( MemoryElementDescriptor me )
//        throws IOException;

    public void waterfal( ICacheElement ce )
        throws IOException;


    /** Puts an item to the cache. */
    public void update( ICacheElement ce ) throws IOException;

    /** Returns the CacheAttributes. */
    public ICompositeCacheAttributes getCacheAttributes();

    /** Sets the CacheAttributes. */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

}
