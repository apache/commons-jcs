package org.apache.jcs.engine.behavior;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;

/**
 * Used to receive a cache event notification. <br>
 * <br>
 * Note: objects which implement this interface are local listeners to cache
 * changes, whereas objects which implement IRmiCacheListener are remote
 * listeners to cache changes.
 *
 */
public interface ICacheListener
{
    /** Notifies the subscribers for a cache entry update.
     * @param item
     * @throws IOException*/
    public void handlePut( ICacheElement item )
        throws IOException;

    /** Notifies the subscribers for a cache entry removal.
     * @param cacheName
     * @param key
     * @throws IOException*/
    public void handleRemove( String cacheName, Serializable key )
        throws IOException;

    /** Notifies the subscribers for a cache remove-all.
     * @param cacheName
     * @throws IOException*/
    public void handleRemoveAll( String cacheName )
        throws IOException;

    /** Notifies the subscribers for freeing up the named cache.
     * @param cacheName
     * @throws IOException*/
    public void handleDispose( String cacheName )
        throws IOException;

    /**
     * Notifies the subscribers for releasing all caches.
     *
     * @param id
     *            The new listenerId value
     */
    //  public void handleRelease() throws IOException;
    /**
     * sets unique identifier of listener home
     *
     * @param id
     *            The new listenerId value
     * @throws IOException
     */
    public void setListenerId( long id )
        throws IOException;

    /**
     * Gets the listenerId attribute of the ICacheListener object
     *
     * @return The listenerId value
     * @throws IOException
     */
    public long getListenerId()
        throws IOException;

}
