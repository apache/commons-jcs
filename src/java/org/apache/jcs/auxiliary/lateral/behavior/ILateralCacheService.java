package org.apache.jcs.auxiliary.lateral.behavior;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

/**
 * Used to retrieve and update the lateral cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ILateralCacheService extends ICacheService
{

    /** Puts a cache item to the cache. */
    public void update( ICacheElement item, byte requesterId )
        throws IOException;


    /** Removes the given key from the specified cache. */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException;


    /** Remove all keys from the sepcified cache. */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException;

    public Set getGroupKeys(String cacheName, String groupName);
}
