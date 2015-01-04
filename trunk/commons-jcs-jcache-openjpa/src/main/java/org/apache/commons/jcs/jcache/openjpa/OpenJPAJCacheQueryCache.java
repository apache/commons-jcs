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

import org.apache.openjpa.datacache.AbstractQueryCache;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.datacache.QueryResult;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OpenJPAJCacheQueryCache extends AbstractQueryCache
{
    private static final String OPENJPA_PREFIX = "openjpa.querycache.";
    private static final String QUERY_CACHE_NAME = "query";

    private final Lock lock = new ReentrantLock();
    private OpenJPAJCacheDataCacheManager manager;

    @Override
    public void initialize(final DataCacheManager manager)
    {
        super.initialize(manager);
        this.manager = OpenJPAJCacheDataCacheManager.class.cast(manager);
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
    protected Collection keySet()
    {
        final Collection<QueryKey> keys = new LinkedList<QueryKey>();
        for (final Cache.Entry<Object, Object> entry : queryCache())
        {
            keys.add(QueryKey.class.cast(entry.getKey()));
        }
        return keys;
    }

    @Override
    protected QueryResult getInternal(final QueryKey qk)
    {
        return QueryResult.class.cast(queryCache().get(qk));
    }

    private Cache<Object, Object> queryCache()
    {
        return manager.getOrCreateCache(OPENJPA_PREFIX, QUERY_CACHE_NAME);
    }

    @Override
    protected QueryResult putInternal(final QueryKey qk, final QueryResult oids)
    {
        queryCache().put(qk, oids);
        return oids;
    }

    @Override
    protected QueryResult removeInternal(final QueryKey qk)
    {
        final Object remove = queryCache().getAndRemove(qk);
        if (remove == null)
        {
            return null;
        }
        return QueryResult.class.cast(remove);
    }

    @Override
    protected boolean pinInternal(final QueryKey qk)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean unpinInternal(final QueryKey qk)
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
