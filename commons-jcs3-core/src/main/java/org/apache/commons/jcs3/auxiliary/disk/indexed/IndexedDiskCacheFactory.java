package org.apache.commons.jcs3.auxiliary.disk.indexed;

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

import org.apache.commons.jcs3.auxiliary.AbstractAuxiliaryCacheFactory;
import org.apache.commons.jcs3.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs3.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs3.log.Log;

/**
 * Creates disk cache instances.
 */
public class IndexedDiskCacheFactory
    extends AbstractAuxiliaryCacheFactory
{
    /** The logger. */
    private static final Log log = Log.getLog( IndexedDiskCacheFactory.class );

    /**
     * Create an instance of an IndexedDiskCache.
     *
     * @param iaca cache attributes of this cache instance
     * @param cacheMgr This allows auxiliaries to reference the manager without assuming that it is
     *            a singleton. This will allow JCS to be a non-singleton. Also, it makes it easier to
     *            test.
     * @param cacheEventLogger
     * @param elementSerializer
     * @return IndexedDiskCache
     */
    @Override
    public <K, V> IndexedDiskCache<K, V> createCache( final AuxiliaryCacheAttributes iaca, final ICompositeCacheManager cacheMgr,
                                       final ICacheEventLogger cacheEventLogger, final IElementSerializer elementSerializer )
    {
        final IndexedDiskCacheAttributes idca = (IndexedDiskCacheAttributes) iaca;
        log.debug( "Creating DiskCache for attributes = {0}", idca );

        final IndexedDiskCache<K, V> cache = new IndexedDiskCache<>( idca, elementSerializer );
        cache.setCacheEventLogger( cacheEventLogger );

        return cache;
    }
}
