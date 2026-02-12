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

import java.util.Arrays;

import org.apache.commons.jcs4.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;

/** Either serialized value or the value should be null; */
public record CacheElementSerialized<K, V>(
        /** The name of the cache region. This is a namespace. */
        String cacheName,

        /** This is the cache key by which the value can be referenced. */
        K key,

        /** The serialized value. */
        byte[] serializedValue,

        /**
         * These attributes hold information about the element and what it is
         * allowed to do.
         */
        IElementAttributes elementAttributes
) implements ICacheElementSerialized<K, V>
{
    /** Don't change. */
    private static final long serialVersionUID = -7265084818647601874L;

    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     *
     * @return The IElementAttributes value, never null
     */
    @Override
    public IElementAttributes elementAttributes()
    {
        // create default attributes if they are null
        // this shouldn't happen, but could if a corrupt
        // object was sent over the wire.
        if ( this.elementAttributes == null )
        {
            return new ElementAttributes();
        }

        return this.elementAttributes;
    }

    /**
     * Gets the value attribute of the CacheElement object
     *
     * @return The value
     */
    @Override
    public V value()
    {
        return null;
    }

    /**
     * For debugging only.
     *
     * @return debugging string.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n CacheElementSerialized: " );
        buf.append( "\n CacheName = [").append(cacheName()).append("]");
        buf.append( "\n Key = [").append(key()).append("]");
        buf.append( "\n SerializedValue = ").append(Arrays.toString(serializedValue()));
        buf.append( "\n ElementAttributes = ").append(elementAttributes());
        return buf.toString();
    }
}
