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
package org.apache.commons.jcs.jcache.openjpa;

import org.apache.openjpa.datacache.AbstractDataCache;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.datacache.DataCachePCData;
import org.apache.openjpa.util.OpenJPAId;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OpenJPAJCacheDataCache extends AbstractDataCache
{
    private static final String OPENJPA_PREFIX = "openjpa.datacache.";

    private final Lock lock = new ReentrantLock();
    private OpenJPAJCacheDataCacheManager manager;

    @Override
    public void initialize(final DataCacheManager manager)
    {
        super.initialize(manager);
        this.manager = OpenJPAJCacheDataCacheManager.class.cast(manager);
    }

    @Override
    protected DataCachePCData getInternal(final Object oid)
    {
        Object result = null;
        if (OpenJPAId.class.isInstance(oid))
        {
            final Class<?> cls = OpenJPAId.class.cast(oid).getType();
            Cache<Object, Object> cache = manager.getOrCreateCache(OPENJPA_PREFIX, cls.getName());
            if (cache == null)
            {
                return null;
            }
            else
            {
                result = cache.get(oid);
            }
        }
        else
        {
            final CacheManager cacheManager = manager.getCacheManager();
            for (final String cacheName : cacheManager.getCacheNames())
            {
                if (!cacheName.startsWith(OPENJPA_PREFIX))
                {
                    continue;
                }

                result = cacheManager.getCache(cacheName).get(oid);
                if (result != null)
                {
                    break;
                }
            }
        }
        if (result == null)
        {
            return null;
        }
        return DataCachePCData.class.cast(result);
    }

    @Override
    protected DataCachePCData putInternal(final Object oid, final DataCachePCData pc)
    {
        manager.getOrCreateCache(OPENJPA_PREFIX, pc.getType().getName()).put(oid, pc);
        return pc;
    }

    @Override
    protected DataCachePCData removeInternal(final Object oid)
    {
        if (OpenJPAId.class.isInstance(oid))
        {
            final Object remove = manager.getOrCreateCache(OPENJPA_PREFIX, OpenJPAId.class.cast(oid).getType().getName()).getAndRemove(oid);
            if (remove == null)
            {
                return null;
            }
            return DataCachePCData.class.cast(remove);
        }
        return null;
    }

    @Override
    protected void removeAllInternal(final Class<?> cls, final boolean subclasses)
    {
        final String name;
        if (subclasses)
        {
            name = cls.getSuperclass().getName();
        }
        else
        {
            name = cls.getName();
        }
        manager.getOrCreateCache(OPENJPA_PREFIX, name).removeAll();
    }

    @Override
    protected void clearInternal()
    {
        final CacheManager cacheManager = manager.getCacheManager();
        for (final String cacheName : cacheManager.getCacheNames())
        {
            if (!cacheName.startsWith(OPENJPA_PREFIX))
            {
                continue;
            }
            cacheManager.getCache(cacheName).clear();
        }
    }

    @Override
    protected boolean pinInternal(final Object oid)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean unpinInternal(final Object oid)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLock()
    {
        lock.lock();
    }

    @Override
    public void writeUnlock()
    {
        lock.unlock();
    }
}
