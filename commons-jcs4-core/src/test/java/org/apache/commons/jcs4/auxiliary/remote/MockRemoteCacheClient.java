package org.apache.commons.jcs4.auxiliary.remote;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs4.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheClient;
import org.apache.commons.jcs4.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs4.engine.CacheStatus;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs4.engine.stats.Stats;
import org.apache.commons.jcs4.engine.stats.behavior.IStats;
import org.apache.commons.jcs4.log.Log;

/**
 * Used for testing the no wait.
 */
public class MockRemoteCacheClient<K, V>
    extends AbstractAuxiliaryCache<K, V>
    implements IRemoteCacheClient<K, V>
{
    /** Log instance */
    private static final Log log = Log.getLog( MockRemoteCacheClient.class );

    /** List of ICacheElement&lt;K, V&gt; objects passed into update. */
    public List<ICacheElement<K, V>> updateList = new LinkedList<>();

    /** List of key objects passed into remove. */
    public List<K> removeList = new LinkedList<>();

    /** Status to return. */
    public CacheStatus status = CacheStatus.ALIVE;

    /** Can setup values to return from get. values must be ICacheElement&lt;K, V&gt; */
    public Map<K, ICacheElement<K, V>> getSetupMap = new HashMap<>();

    /** Can setup values to return from get. values must be Map&lt;K, ICacheElement&lt;K, V&gt;&gt; */
    public Map<Set<K>, Map<K, ICacheElement<K, V>>> getMultipleSetupMap =
        new HashMap<>();

    /** The last service passed to fixCache */
    public ICacheServiceNonLocal<K, V> fixed;

    /** Attributes. */
    public RemoteCacheAttributes attributes = new RemoteCacheAttributes();

    /**
     * Prepares for shutdown.
     */
    @Override
    public void dispose()
    {
        // do nothing
    }

    /**
     * Stores the last argument as fixed.
     */
    @Override
    @SuppressWarnings("unchecked") // Don't know how to do this properly
    public void fixCache( final ICacheServiceNonLocal<?, ?> remote )
    {
        fixed = (ICacheServiceNonLocal<K, V>)remote;
    }

    /**
     * Looks in the getSetupMap for a value.
     */
    @Override
    public ICacheElement<K, V> get( final K key )
    {
        log.info( "get [" + key + "]" );
        return getSetupMap.get( key );
    }

    /**
     * Returns the setup attributes. By default they are not null.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return attributes;
    }

    /**
     * Returns the cache name.
     *
     * @return usually the region name.
     */
    @Override
    public String getCacheName()
    {
        return null;
    }

    /** @return 0 */
    @Override
    public CacheType getCacheType()
    {
        return CacheType.REMOTE_CACHE;
    }

    /**
     * @return null
     */
    @Override
    public Set<K> getKeySet( )
    {
        return null;
    }

    /**
     * @return null
     */
    @Override
    public IRemoteCacheListener<K, V> getListener()
    {
        return null;
    }

    /**
     * @return long
     */
    @Override
    public long getListenerId()
    {
        return 0;
    }

    /**
     * @param pattern
     * @return Map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching(final String pattern)
        throws IOException
    {
        return new HashMap<>();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     *
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        log.info( "get [" + keys + "]" );
        return getMultipleSetupMap.get( keys );
    }

    /**
     * Returns the current cache size in number of elements.
     *
     * @return number of elements
     */
    @Override
    public int getSize()
    {
        return 0;
    }

    /**
     * @return null
     */
    @Override
    public IStats getStatistics()
    {
        return new Stats("");
    }

    /**
     * Returns the status setup variable.
     */
    @Override
    public CacheStatus getStatus()
    {
        return status;
    }

    /**
     * Adds the key to the remove list.
     */
    @Override
    public boolean remove( final K key )
    {
        removeList.add( key );
        return false;
    }

    /**
     * Removes all cached items from the cache.
     */
    @Override
    public void removeAll()
    {
        // do nothing
    }

    /**
     * Adds the argument to the updatedList.
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
    {
        updateList.add( ce );
    }
}
