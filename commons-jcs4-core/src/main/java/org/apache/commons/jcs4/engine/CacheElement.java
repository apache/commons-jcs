package org.apache.commons.jcs4.engine;

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

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;

/**
 * Generic element wrapper. Often stuffed inside another.
 */
public record CacheElement<K, V>(
        /** The name of the cache region. This is a namespace. */
        String cacheName,

        /** This is the cache key by which the value can be referenced. */
        K key,

        /** This is the cached value, reference by the key. */
        V value,

        /**
         * These attributes hold information about the element and what it is
         * allowed to do.
         */
        IElementAttributes elementAttributes
) implements ICacheElement<K, V>
{
    /** Don't change */
    private static final long serialVersionUID = -6062305728297627263L;

    /**
     * Constructor for the CacheElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement(final String cacheName, final K key, final V val)
    {
        this(cacheName, key, val, new ElementAttributes());
    }

    /**
     * For debugging only.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return "[CacheElement: cacheName [" + cacheName
                + "], key [" + key + "], val [" + value
                + "], attr [" + elementAttributes + "]";
    }
}
