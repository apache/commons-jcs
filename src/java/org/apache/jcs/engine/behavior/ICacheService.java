package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.access.exception.ObjectNotFoundException;

/**
 * Used to retrieve and update the cache. <br>
 * <br>
 * Note: server which implements this interface provides a local cache service,
 * whereas server which implements IRmiCacheService provides a remote cache
 * service.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheService
{
    /** Puts a cache item to the cache. */
    public void update( ICacheElement item )
        throws ObjectExistsException, IOException;


    /**
     * Returns a cache bean from the specified cache; or null if the key does
     * not exist.
     */
    public ICacheElement get( String cacheName, Serializable key )
        throws ObjectNotFoundException, IOException;


    /** Removes the given key from the specified cache. */
    public void remove( String cacheName, Serializable key )
        throws IOException;


    /** Remove all keys from the sepcified cache. */
    public void removeAll( String cacheName )
        throws IOException;


    /** Frees the specified cache. */
    public void dispose( String cacheName )
        throws IOException;


    /** Frees all caches. */
    public void release()
        throws IOException;
}
