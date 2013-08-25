package org.apache.commons.jcs.auxiliary;

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
import java.io.Serializable;
import java.util.Set;

import org.apache.commons.jcs.engine.behavior.ICache;
import org.apache.commons.jcs.engine.behavior.IElementSerializer;
import org.apache.commons.jcs.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs.engine.stats.behavior.IStats;

/**
 * Tag interface for auxiliary caches. Currently this provides no additional methods over what is in
 * ICache, but I anticipate that will change. For example, there will be a mechanism for determining
 * the type (disk/lateral/remote) of the auxiliary here -- and the existing getCacheType will be
 * removed from ICache.
 */
public interface AuxiliaryCache<K extends Serializable, V extends Serializable>
    extends ICache<K, V>
{
    /**
     * Gets the set of keys of objects currently in the group
     * @param group
     * @return a set of group keys
     * @throws IOException
     */
    Set<K> getGroupKeys( String group )
        throws IOException;

    /**
     * Gets the set of group names currently in the cache
     * @return a set of group names
     * @throws IOException
     */
    Set<String> getGroupNames()
        throws IOException;

    /**
     * @return the historical and statistical data for a region's auxiliary cache.
     */
    IStats getStatistics();

    /**
     * This returns the generic attributes for an auxiliary cache. Most implementations will cast
     * this to a more specific type.
     * <p>
     * @return the attributes for the auxiliary cache
     */
    AuxiliaryCacheAttributes getAuxiliaryCacheAttributes();

    /**
     * Allows you to inject a custom serializer. A good example would be a compressing standard
     * serializer.
     * <p>
     * @param elementSerializer
     */
    void setElementSerializer( IElementSerializer elementSerializer );

    /**
     * Every Auxiliary must allow for the use of an event logger.
     * <p>
     * @param cacheEventLogger
     */
    void setCacheEventLogger( ICacheEventLogger cacheEventLogger );
}
