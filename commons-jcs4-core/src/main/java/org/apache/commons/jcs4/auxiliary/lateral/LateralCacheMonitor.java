package org.apache.commons.jcs4.auxiliary.lateral;

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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jcs4.auxiliary.AbstractAuxiliaryCacheMonitor;
import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.LateralTCPCacheFactory;
import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.LateralTCPCacheNoWait;
import org.apache.commons.jcs4.auxiliary.lateral.socket.tcp.behavior.ILateralTCPCacheAttributes;
import org.apache.commons.jcs4.engine.CacheStatus;
import org.apache.commons.jcs4.engine.ZombieCacheServiceNonLocal;
import org.apache.commons.jcs4.engine.behavior.ICacheServiceNonLocal;

/**
 * Used to monitor and repair any failed connection for the lateral cache service. By default the
 * monitor operates in a failure driven mode. That is, it goes into a wait state until there is an
 * error. Upon the notification of a connection error, the monitor changes to operate in a time
 * driven mode. That is, it attempts to recover the connections on a periodic basis. When all failed
 * connections are restored, it changes back to the failure driven mode.
 */
public class LateralCacheMonitor extends AbstractAuxiliaryCacheMonitor
{
    /**
     * Map of caches to monitor
     */
    private final ConcurrentHashMap<String, LateralTCPCacheNoWait<Object, Object>> caches;

    /**
     * Reference to the factory
     */
    private final LateralTCPCacheFactory factory;

    /**
     * Constructor for the LateralCacheMonitor object
     * <p>
     * It's the clients responsibility to decide how many of these there will be.
     *
     * @param factory a reference to the factory that manages the service instances
     */
    public LateralCacheMonitor(final LateralTCPCacheFactory factory)
    {
        super("JCS-LateralCacheMonitor");
        this.factory = factory;
        this.caches = new ConcurrentHashMap<>();
        setIdlePeriod(20000L);
    }

    /**
     * Add a cache to be monitored
     *
     * @param cache the cache
     */
    @SuppressWarnings("unchecked") // common map for all caches
    public void addCache(final LateralTCPCacheNoWait<?, ?> cache)
    {
        this.caches.put(cache.getCacheName(), (LateralTCPCacheNoWait<Object, Object>)cache);

        // Fix a cache where an exception occurred before it was added to this monitor.
        // For instance, where a cache failed to connect to lateral TCP server.
        if (cache.getStatus() == CacheStatus.ERROR)
        {
            if (getState() == Thread.State.NEW)
            {
                // no need to signal trigger if monitor hasn't started
                allright.compareAndSet(true, false);
            }
            else
            {
                notifyError();
            }
        }

        // if not yet started, go ahead
        if (getState() == Thread.State.NEW)
        {
            start();
        }
    }

    /**
     * Clean up all resources before shutdown
     */
    @Override
    public void dispose()
    {
        this.caches.clear();
    }

    /**
     * Main processing method for the LateralCacheMonitor object
     */
    @Override
    public void doWork()
    {
        // Monitor each cache instance one after the other.
        log.info( "Number of caches to monitor = " + caches.size() );

        caches.forEach((cacheName, cache) -> {

            if (cache.getStatus() == CacheStatus.ERROR)
            {
                log.info( "Found LateralTCPCacheNoWait in error, " + cacheName );

                final ILateralTCPCacheAttributes lca =
                        cache.getAuxiliaryCacheAttributes();

                // Get service instance
                final ICacheServiceNonLocal<Object, Object> cacheService =
                        factory.getCSNLInstance(lca, cache.getElementSerializer());

                // If we can't fix them, just skip and re-try in the
                // next round.
                if (!(cacheService instanceof ZombieCacheServiceNonLocal))
                {
                    cache.fixCache(cacheService);
                }
            }
        });
    }
}
