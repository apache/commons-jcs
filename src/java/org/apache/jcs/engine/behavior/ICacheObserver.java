package org.apache.jcs.engine.behavior;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.jcs.engine.behavior.ICacheListener;

/**
 * Used to register interest in receiving cache changes. <br>
 * <br>
 * Note: server which implements this interface provides a local cache event
 * notification service, whereas server which implements IRmiCacheWatch provides
 * a remote cache event notification service.
 *  
 */
public interface ICacheObserver
{
    /**
     * Subscribes to the specified cache.
     * 
     * @param cacheName
     *            the specified cache.
     * @param obj
     *            object to notify for cache changes.
     */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException;

    //, CacheNotFoundException;

    /**
     * Subscribes to all caches.
     * 
     * @param obj
     *            object to notify for all cache changes.
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException;

    /**
     * Unsubscribes from the specified cache.
     * 
     * @param obj
     *            existing subscriber.
     */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException;

    /**
     * Unsubscribes from all caches.
     * 
     * @param obj
     *            existing subscriber.
     */
    public void removeCacheListener( ICacheListener obj )
        throws IOException;
}
