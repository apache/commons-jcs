package org.apache.commons.jcs.auxiliary.remote;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheClient;
import org.apache.commons.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs.engine.stats.behavior.IStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used for testing the no wait.
 * <p>
 * @author Aaron Smuts
 */
public class MockRemoteCacheClient<K, V>
    extends AbstractAuxiliaryCache<K, V>
    implements IRemoteCacheClient<K, V>
{
    /** log instance */
    private static final Log log = LogFactory.getLog( MockRemoteCacheClient.class );

    /** List of ICacheElement&lt;K, V&gt; objects passed into update. */
    public List<ICacheElement<K, V>> updateList = new LinkedList<ICacheElement<K,V>>();

    /** List of key objects passed into remove. */
    public List<K> removeList = new LinkedList<K>();

    /** status to return. */
    public CacheStatus status = CacheStatus.ALIVE;

    /** Can setup values to return from get. values must be ICacheElement&lt;K, V&gt; */
    public Map<K, ICacheElement<K, V>> getSetupMap = new HashMap<K, ICacheElement<K,V>>();

    /** Can setup values to return from get. values must be Map&lt;K, ICacheElement&lt;K, V&gt;&gt; */
    public Map<Set<K>, Map<K, ICacheElement<K, V>>> getMultipleSetupMap =
        new HashMap<Set<K>, Map<K,ICacheElement<K,V>>>();

    /** The last service passed to fixCache */
    public ICacheServiceNonLocal<K, V> fixed;

    /** Attributes. */
    public RemoteCacheAttributes attributes = new RemoteCacheAttributes();

    /**
     * Stores the last argument as fixed.
     */
    @Override
    @SuppressWarnings("unchecked") // Don't know how to do this properly
    public void fixCache( ICacheServiceNonLocal<?, ?> remote )
    {
        fixed = (ICacheServiceNonLocal<K, V>)remote;
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
     * @return null
     */
    @Override
    public IRemoteCacheListener<K, V> getListener()
    {
        return null;
    }

    /**
     * Adds the argument to the updatedList.
     */
    @Override
    public void update( ICacheElement<K, V> ce )
    {
        updateList.add( ce );
    }

    /**
     * Looks in the getSetupMap for a value.
     */
    @Override
    public ICacheElement<K, V> get( K key )
    {
        log.info( "get [" + key + "]" );
        return getSetupMap.get( key );
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement&lt;K, V&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
    {
        log.info( "get [" + keys + "]" );
        return getMultipleSetupMap.get( keys );
    }

    /**
     * Adds the key to the remove list.
     */
    @Override
    public boolean remove( K key )
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
     * Prepares for shutdown.
     */
    @Override
    public void dispose()
    {
        // do nothing
    }

    /**
     * Returns the current cache size in number of elements.
     * <p>
     * @return number of elements
     */
    @Override
    public int getSize()
    {
        return 0;
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
     * Returns the cache name.
     * <p>
     * @return usually the region name.
     */
    @Override
    public String getCacheName()
    {
        return null;
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
    public IStats getStatistics()
    {
        return null;
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
     * Returns the cache stats.
     * <p>
     * @return String of important historical information.
     */
    @Override
    public String getStats()
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
     * @param pattern
     * @return Map
     * @throws IOException
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMatching(String pattern)
        throws IOException
    {
        return new HashMap<K, ICacheElement<K,V>>();
    }

    /**
     * Nothing important
     * <p>
     * @return null
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return null;
    }
}
