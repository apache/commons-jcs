package org.apache.commons.jcs3.auxiliary;

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
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs3.engine.CacheStatus;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;

/**
 * Mock auxiliary for unit tests.
 */
public class MockAuxiliaryCache<K, V>
    extends AbstractAuxiliaryCache<K, V>
{
    /** Can setup the cache type */
    public CacheType cacheType = CacheType.DISK_CACHE;

    /** Can setup status */
    public CacheStatus status = CacheStatus.ALIVE;

    /** Times getMatching was Called */
    public int getMatchingCallCount;

    /**
     * @throws IOException
     */
    @Override
    public void dispose()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param key
     * @return ICacheElement
     * @throws IOException
     */
    @Override
    public ICacheElement<K, V> get( final K key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    @Override
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return null;
    }

    /**
     * @return null
     */
    @Override
    public String getCacheName()
    {
        return null;
    }

    /**
     * @return cacheType
     */
    @Override
    public CacheType getCacheType()
    {
        return cacheType;
    }

    /** @return null */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return null;
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @see org.apache.commons.jcs3.auxiliary.disk.AbstractDiskCache#getKeySet()
     */
    @Override
    public Set<K> getKeySet() throws IOException
    {
        return null;
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
        getMatchingCallCount++;
        return new HashMap<>();
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement&lt;String, String&gt; element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    @Override
    public Map<K, ICacheElement<K, V>> getMultiple(final Set<K> keys)
    {
        return new HashMap<>();
    }

    /**
     * @return int
     */
    @Override
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
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
     * @return null
     */
    @Override
    public String getStats()
    {
        return null;
    }

    /**
     * @return int
     */
    @Override
    public CacheStatus getStatus()
    {
        return status;
    }

    /**
     * @param key
     * @return boolean
     * @throws IOException
     */
    @Override
    public boolean remove( final K key )
        throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @throws IOException
     */
    @Override
    public void removeAll()
        throws IOException
    {
        // TODO Auto-generated method stub

    }

    /**
     * @param ce
     * @throws IOException
     */
    @Override
    public void update( final ICacheElement<K, V> ce )
        throws IOException
    {
        // TODO Auto-generated method stub

    }
}
