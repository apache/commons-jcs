package org.apache.jcs.auxiliary;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Tag interface for auxiliary caches. Currently this provides no additional
 * methods over what is in ICache, but I anticipate that will change. For
 * example, there will be a mechanism for determining the type
 * (disk/lateral/remote) of the auxiliary here -- and the existing getCacheType
 * will be removed from ICache.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:jtaylor@apache.org">James Taylor</a>
 * @version $Id$
 */
public interface AuxiliaryCache extends ICache
{
    /** Puts an item to the cache. */
    public void update( ICacheElement ce ) throws IOException;

    /** Gets an item from the cache. */
    public ICacheElement get( Serializable key ) throws IOException;

    /** Removes an item from the cache. */
    public boolean remove( Serializable key ) throws IOException;

    /** Removes all cached items from the cache. */
    public void removeAll() throws IOException;

    /** Prepares for shutdown. */
    public void dispose() throws IOException;

    /** Returns the current cache size. */
    public int getSize();

    /** Returns the cache status. */
    public int getStatus();

    /** Returns the cache name. */
    public String getCacheName();

    /**
     * Gets the set of keys of objects currently in the group
     */
    public Set getGroupKeys(String group) throws IOException;
}
