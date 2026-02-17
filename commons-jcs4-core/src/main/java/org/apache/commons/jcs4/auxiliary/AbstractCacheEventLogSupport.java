package org.apache.commons.jcs4.auxiliary;

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

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.logging.CacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEvent;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger.CacheEventType;

public abstract class AbstractCacheEventLogSupport<K, V>
{
    /** An optional event logger */
    private Optional<ICacheEventLogger> cacheEventLogger = Optional.empty();

    /**
     * Creates an event.
     *
     * @param source e.g. RemoteCacheServer
     * @param region the name of the region
     * @param eventType e.g. update, get, put, remove
     * @param optionalDetails any extra message
     * @param key the cache key
     * @return ICacheEvent
     */
    protected <T> ICacheEvent<T> createICacheEvent(final String source, final String region,
            final CacheEventType eventType, final String optionalDetails, final T key)
    {
        if (getCacheEventLogger().isEmpty())
        {
            return new CacheEvent<>();
        }

        return getCacheEventLogger().get().createICacheEvent(
                source, region, eventType, optionalDetails, key);
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param item
     * @param eventType
     * @param eventLoggingExtraInfo
     * @return ICacheEvent
     */
    protected ICacheEvent<K> createICacheEvent(final ICacheElement<K, V> item,
            final CacheEventType eventType, Supplier<String> eventLoggingExtraInfo)
    {
        if (getCacheEventLogger().isEmpty())
        {
            return new CacheEvent<>();
        }
        String regionName = item == null ? null : item.cacheName();
        K key = item == null ? null : item.key();

        return createICacheEvent(getEventLogSourceName(), regionName,
                eventType, eventLoggingExtraInfo.get(), key);
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param regionName
     * @param key
     * @param eventType
     * @param eventLoggingExtraInfo
     * @return ICacheEvent
     */
    protected <T> ICacheEvent<T> createICacheEvent(final String regionName, final T key,
            final CacheEventType eventType, Supplier<String> eventLoggingExtraInfo)
    {
        if (getCacheEventLogger().isEmpty())
        {
            return new CacheEvent<>();
        }

        return createICacheEvent(getEventLogSourceName(), regionName,
                eventType, eventLoggingExtraInfo.get(), key);
    }

    /**
     * Allows it to be injected.
     *
     * @return cacheEventLogger
     */
    public Optional<ICacheEventLogger> getCacheEventLogger()
    {
        return cacheEventLogger;
    }

    /**
     * Gets the extra info for the event log.
     *
     * @return the eventLogSourceName
     */
    protected abstract String getEventLogSourceName();

    /**
     * Logs an event if an event logger is configured.
     *
     * @param source
     * @param eventType
     * @param optionalDetails
     */
    protected void logApplicationEvent(final String source, final CacheEventType eventType, final String optionalDetails)
    {
        getCacheEventLogger().ifPresent(logger -> logger.logApplicationEvent( source, eventType, optionalDetails));
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param source
     * @param eventType
     * @param errorMessage
     */
    protected void logError(final String source, final CacheEventType eventType, final String errorMessage)
    {
        getCacheEventLogger().ifPresent(logger -> logger.logError( source, eventType, errorMessage));
    }

    /**
     * Logs an event if an event logger is configured.
     *
     * @param cacheEvent
     */
    protected <T> void logICacheEvent(final ICacheEvent<T> cacheEvent)
    {
        getCacheEventLogger().ifPresent(logger -> logger.logICacheEvent( cacheEvent ));
    }

    /**
     * Allows it to be injected.
     *
     * @param cacheEventLogger
     */
    public void setCacheEventLogger(final ICacheEventLogger cacheEventLogger)
    {
        this.cacheEventLogger = Optional.ofNullable(cacheEventLogger);
    }

}
