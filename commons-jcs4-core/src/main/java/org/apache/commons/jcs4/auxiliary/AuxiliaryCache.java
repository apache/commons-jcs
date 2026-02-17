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

import java.io.IOException;
import java.util.Set;

import org.apache.commons.jcs4.engine.behavior.ICache;

/**
 * Tag interface for auxiliary caches. Currently this provides no additional methods over what is in
 * ICache, but I anticipate that will change. For example, there will be a mechanism for determining
 * the type (disk/lateral/remote) of the auxiliary here -- and the existing getCacheType will be
 * removed from ICache.
 */
public interface AuxiliaryCache<K, V>
    extends ICache<K, V>
{
    /**
     * This returns the generic attributes for an auxiliary cache. Most implementations will cast
     * this to a more specific type.
     *
     * @return the attributes for the auxiliary cache
     */
    AuxiliaryCacheAttributes getAuxiliaryCacheAttributes();

    /**
     * Gets a set of the keys for all elements in the auxiliary cache.
     *
     * @return a set of the key type
     * TODO This should probably be done in chunks with a range passed in. This
     *       will be a problem if someone puts a 1,000,000 or so items in a
     *       region.
     * @throws IOException if access to the auxiliary cache fails
     */
    Set<K> getKeySet() throws IOException;

    /**
     * Returns the cache name.
     *
     * @return usually the region name.
     */
    @Override
    default String getCacheName()
    {
        return getAuxiliaryCacheAttributes().getCacheName();
    }
}
