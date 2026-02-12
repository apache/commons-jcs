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

import org.apache.commons.jcs4.engine.behavior.ICompositeCacheManager;
import org.apache.commons.jcs4.engine.behavior.IElementSerializer;
import org.apache.commons.jcs4.engine.logging.behavior.ICacheEventLogger;
import org.apache.commons.jcs4.engine.match.behavior.IKeyMatcher;

/** For testing */
public class MockAuxiliaryCacheFactory
    extends AbstractAuxiliaryCacheFactory
{
    /** The name of the aux */
    public String name = "MockAuxiliaryCacheFactory";

    /**
     * Creates a mock aux.
     *
     * @param attr
     * @param cacheMgr
     * @param cacheEventLogger the cache event logger
     * @param elementSerializer the serializer for cache elements
     * @param keyMatcher the key matcher for getMatching() calls
     * @return AuxiliaryCache
     */
    @Override
    public <K, V> AuxiliaryCache<K, V>
        createCache(final AuxiliaryCacheAttributes attr,
                final ICompositeCacheManager cacheMgr, final ICacheEventLogger cacheEventLogger,
                final IElementSerializer elementSerializer, final IKeyMatcher<K> keyMatcher)
    {
        final MockAuxiliaryCache<K, V> auxCache = new MockAuxiliaryCache<>();
        auxCache.setCacheEventLogger( cacheEventLogger );
        auxCache.setElementSerializer( elementSerializer );
        auxCache.setKeyMatcher(keyMatcher);
        return auxCache;
    }

    /**
     * @return String
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * @param s
     */
    @Override
    public void setName( final String s )
    {
        this.name = s;
    }

    /**
     * Gets the class implementing the extended AuxiliaryCacheAttributes for this factory
     *
     * @return The class value
     */
    @Override
    public Class<MockAuxiliaryCacheAttributes> getAttributeClass()
    {
        return MockAuxiliaryCacheAttributes.class;
    }
}
