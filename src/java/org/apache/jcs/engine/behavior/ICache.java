package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Inteface implemented by a specific cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICache extends ICacheType
{

    /** Puts an item to the cache. */
    public void update( ICacheElement ce )
        throws IOException;

    // used to identify the soure, if none is present is assumed to be local

    // public void update(CacheElement ce, Serializable source_id ) throws IOException;

    // may need insert if we want to distinguish b/wn put and replace
    /** Description of the Method */
    public void put( Serializable key, Serializable val )
        throws IOException;


    /** Description of the Method */
    public void put( Serializable key, Serializable val, IElementAttributes attr )
        throws IOException;


    /** Gets an item from the cache. */
    public Serializable get( Serializable key )
        throws IOException;


    /** Description of the Method */
    public Serializable get( Serializable key, boolean container )
        throws IOException;


    /** Removes an item from the cache. */
    public boolean remove( Serializable key )
        throws IOException;

    // used to identify the soure, if none is present is assumed to be local

    // public boolean remove(Serializable key, Serializable source_id) throws IOException;

    /** Removes all cached items from the cache. */
    public void removeAll()
        throws IOException;


    /** Prepares for shutdown. */
    public void dispose()
        throws IOException;


    /**
     * Returns the cache statistics.
     *
     * @return The stats value
     */
    public String getStats();


    /**
     * Returns the current cache size.
     *
     * @return The size value
     */
    public int getSize();


    /**
     * Returns the cache status.
     *
     * @return The status value
     */
    public int getStatus();


    /**
     * Returns the cache name.
     *
     * @return The cacheName value
     */
    public String getCacheName();

}
