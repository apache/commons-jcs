package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

/**
 * Much like the ICache interface, but htis is specifically designed for the
 * CompositeCaches. This interface guarantees a simple set of methods for use by
 * auxiliary cache implementations.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICompositeCache extends ICache
{

    /** Puts an item to the cache. */
    public void update( ICacheElement ce )
        throws IOException;


    /** Description of the Method */
    public void update( ICacheElement ce, boolean localInvocation )
        throws IOException;

    /** Allows the exclusion of non local caches. */
    public void updateExclude( ICacheElement ce, boolean excludeRemote )
        throws IOException;

    /** Description of the Method */
    public boolean remove( Serializable key )
        throws IOException;


    /** Description of the Method */
    public boolean remove( Serializable key, boolean localInvocation )
        throws IOException;


    /** allows a get request to stay local * */
    public Serializable get( Serializable key, boolean container, boolean localInvocation )
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
