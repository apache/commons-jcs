package org.apache.jcs.engine.memory.behavior;

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheHub;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;

import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.control.Cache;

/**
 * For the framework. Insures methods a MemoryCache needs to access.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IMemoryCache extends ICacheType
{

    // for initialization
    /** Description of the Method */
    public void initialize( String cacheName,
                            ICompositeCacheAttributes cattr,
                            Cache cache );

    // TODO: need a setCacheAttributes or reInitialize method

    /** Description of the Method */
    //public void makeFirst( MemoryElementDescriptor me );


    //public void moveToMemory( ICacheElement ce );

    /**
     * Gets the size attribute of the IMemoryCache object
     *
     * @return The size value
     */
    public int getSize();


    // get an Iterator for all the elements
    /**
     * Gets the iterator attribute of the IMemoryCache object
     *
     * @return The iterator value
     */
    public Iterator getIterator();


    /** Removes an item from the cache. */
    public boolean remove( Serializable key )
        throws IOException;


    /** Removes all cached items from the cache. */
    public void removeAll()
        throws IOException;


    /** Description of the Method */
    public Serializable get( Serializable key )
        throws IOException;


    /** Description of the Method */
    public Serializable get( Serializable key, boolean container )
        throws IOException;


    /** Puts an item to the cache. */
    public void waterfal( MemoryElementDescriptor me )
        throws IOException;


    /** Puts an item to the cache. */
    public void update( ICacheElement ce )
        throws IOException;


    /** Returns the CacheAttributes. */
    public ICompositeCacheAttributes getCacheAttributes( );
    /** Sets the CacheAttributes. */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

}
