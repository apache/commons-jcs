package org.apache.jcs.engine.behavior;

import java.io.IOException;
import java.io.Serializable;

/**
 * Used to receive a cache event notification. <br>
 * <br>
 * Note: objects which implement this interface are local listeners to cache
 * changes, whereas objects which implement IRmiCacheListener are remote
 * listeners to cache changes.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheListener
{
    /** Notifies the subscribers for a cache entry update. */
    public void handlePut( ICacheElement item )
        throws IOException;


    /** Notifies the subscribers for a cache entry removal. */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException;


    /** Notifies the subscribers for a cache remove-all. */
    public void handleRemoveAll( String cacheName )
        throws IOException;


    /** Notifies the subscribers for freeing up the named cache. */
    public void handleDispose( String cacheName )
        throws IOException;


    /**
     * Notifies the subscribers for releasing all caches.
     *
     * @param id The new listenerId value
     */
//  public void handleRelease() throws IOException;

    /**
     * sets unique identifier of listener home
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException;


    /**
     * Gets the listenerId attribute of the ICacheListener object
     *
     * @return The listenerId value
     */
    public byte getListenerId()
        throws IOException;

}
