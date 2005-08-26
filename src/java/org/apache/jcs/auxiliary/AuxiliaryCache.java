package org.apache.jcs.auxiliary;

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
import java.util.Set;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * Tag interface for auxiliary caches. Currently this provides no additional
 * methods over what is in ICache, but I anticipate that will change. For
 * example, there will be a mechanism for determining the type
 * (disk/lateral/remote) of the auxiliary here -- and the existing getCacheType
 * will be removed from ICache.
 * 
 * @version $Id$
 */
public interface AuxiliaryCache
    extends ICache
{
    /**
     * Puts an item to the cache.
     * 
     * @param ce
     * @throws IOException
     */
    public void update( ICacheElement ce )
        throws IOException;

    /**
     * Gets an item from the cache.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public ICacheElement get( Serializable key )
        throws IOException;

    /**
     * Removes an item from the cache.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public boolean remove( Serializable key )
        throws IOException;

    /**
     * Removes all cached items from the cache.
     * 
     * @throws IOException
     */
    public void removeAll()
        throws IOException;

    /**
     * Prepares for shutdown.
     * 
     * @throws IOException
     */
    public void dispose()
        throws IOException;

    /**
     * Returns the current cache size.
     * 
     * @return
     */
    public int getSize();

    /**
     * Returns the cache status.
     * 
     * @return
     */
    public int getStatus();

    /**
     * Returns the cache name.
     * 
     * @return
     */
    public String getCacheName();

    /**
     * Gets the set of keys of objects currently in the group
     * 
     * @param group
     * @return a set of group keys
     * @throws IOException
     */
    public Set getGroupKeys( String group )
        throws IOException;

    /**
     * Returns the historical and statistical data for a region's auxiliary
     * cache.
     * 
     * @return
     */
    public IStats getStatistics();
}
