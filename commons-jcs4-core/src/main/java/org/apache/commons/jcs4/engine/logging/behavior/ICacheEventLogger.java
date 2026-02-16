package org.apache.commons.jcs4.engine.logging.behavior;

import org.apache.commons.jcs4.engine.logging.CacheEvent;

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

/**
 * This defines the behavior for event logging. Auxiliaries will send events to injected event
 * loggers.
 * <p>
 * In general all ICache interface methods should call the logger if one is configured. This will be
 * done on an ad hoc basis for now. Various auxiliaries may have additional events.
 */
public interface ICacheEventLogger
{
    public enum CacheEventType
    {
        /** ICache update */
        UPDATE_EVENT("update"),

        /** ICache get */
        GET_EVENT("get"),

        /** ICache getMultiple */
        GETMULTIPLE_EVENT("getMultiple"),

        /** ICache getMatching */
        GETMATCHING_EVENT("getMatching"),

        /** ICache remove */
        REMOVE_EVENT("remove"),

        /** ICache removeAll */
        REMOVEALL_EVENT("removeAll"),

        /** ICache dispose */
        DISPOSE_EVENT("dispose"),

        /** RemoteAuxiliaryCache fixCache */
        FIXCACHE_EVENT("fixCache"),

        /** JDBCDiskCache deleteExpired */
        DELETEEXPIRED_EVENT("deleteExpired"),

        /** RemoteCacheServer addCacheListener */
        ADDCACHELISTENER_EVENT("addCacheListener"),

        /** RemoteCacheServer removeCacheListener */
        REMOVECACHELISTENER_EVENT("removeCacheListener"),

        /** RemoteCacheNoWait InitiatedFailover */
        INITIATEDFAILOVER_EVENT("InitiatedFailover"),

        /** RemoteCacheNoWait RestoredPrimary */
        RESTOREDPRIMARY_EVENT("RestoredPrimary"),

        /** RemoteCacheServerFactory Naming.lookup */
        NAMINGLOOKUP_EVENT("Naming.lookup"),

        /** RemoteCacheServerFactory createRegistry */
        CREATEREGISTRY_EVENT("createRegistry"),

        /** RemoteCacheServerFactory registerServer */
        REGISTERSERVER_EVENT("registerServer"),

        /** ICache enqueue. The time in the queue. */
        ENQUEUE_EVENT("enqueue");

        public final String label;

        private CacheEventType(String label)
        {
            this.label = label;
        }
    }

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
    default <T> ICacheEvent<T> createICacheEvent( String source, String region,
            CacheEventType eventType, String optionalDetails, T key )
    {
        return new CacheEvent<>(source, region, eventType, optionalDetails, key);
    }

    /**
     * Logs an event. These are internal application events that do not correspond to ICache calls.
     *
     * @param source e.g. RemoteCacheServer
     * @param eventType e.g. update, get, put, remove
     * @param optionalDetails any extra message
     */
    default void logApplicationEvent( String source, CacheEventType eventType, String optionalDetails ) {}

    /**
     * Logs an error.
     *
     * @param source e.g. RemoteCacheServer
     * @param eventType e.g. update, get, put, remove
     * @param errorMessage any error message
     */
    default void logError( String source, CacheEventType eventType, String errorMessage ) {}

    /**
     * Logs an event.
     *
     * @param event   the event created in createICacheEvent
     */
    default <T> void logICacheEvent( ICacheEvent<T> event ) {}
}
