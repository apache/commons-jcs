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
