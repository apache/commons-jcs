
package org.apache.jcs.engine.behavior;

import java.io.IOException;

/**
 * Used to register interest in receiving cache changes. <br>
 * <br>
 * Note: server which implements this interface provides a local cache event
 * notification service, whereas server which implements IRmiCacheWatch provides
 * a remote cache event notification service.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheObserver
{
    /**
     * Subscribes to the specified cache.
     *
     * @param cacheName the specified cache.
     * @param obj object to notify for cache changes.
     */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException;
    //, CacheNotFoundException;

    /**
     * Subscribes to all caches.
     *
     * @param obj object to notify for all cache changes.
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException;


    /**
     * Unsubscribes from the specified cache.
     *
     * @param obj existing subscriber.
     */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException;


    /**
     * Unsubscribes from all caches.
     *
     * @param obj existing subscriber.
     */
    public void removeCacheListener( ICacheListener obj )
        throws IOException;
}
