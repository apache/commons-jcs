package org.apache.jcs.auxiliary.remote.behavior;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import java.rmi.Remote;

import org.apache.jcs.access.exception.ObjectExistsException;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheService;

/**
 * Used to retrieve and update the remote cache.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheService extends Remote, ICacheService
{

    /** Puts a cache item to the cache. */
    public void update( ICacheElement item, byte requesterId )
        throws ObjectExistsException, IOException;


    /** Removes the given key from the specified cache. */
    public void remove( String cacheName, Serializable key, byte requesterId )
        throws IOException;


    /** Remove all keys from the sepcified cache. */
    public void removeAll( String cacheName, byte requesterId )
        throws IOException;

    public Set getGroupKeys(String cacheName, String groupName) 
        throws IOException;
}
