package org.apache.jcs.auxiliary;

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

import org.apache.jcs.engine.behavior.ICacheType;

/**
 * AuxiliaryCacheManager
 *
 * FIXME: Should not need to extend ICacheType
 *
 */
public interface AuxiliaryCacheManager
    extends ICacheType
{

    /**
     * Return the appropriate auxiliary cache for this region.
     *
     * @param cacheName
     * @return AuxiliaryCache
     */
    public AuxiliaryCache getCache( String cacheName );

    /**
     * This allows the cache manager to be plugged into the auxiliary caches,
     * rather then having them get it themselves. Cache maangers can be mocked
     * out and the auxiliaries will be easier to test.
     *
     * @param cacheName
     * @param cacheManager
     * @return AuxiliaryCache
     */
    //public AuxiliaryCache getCache( String cacheName, ICompositeCacheManager
    // cacheManager );
}
