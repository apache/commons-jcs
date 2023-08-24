package org.apache.commons.jcs3.engine;

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

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.jcs3.engine.behavior.ICacheElementSerialized;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;

/** Either serialized value or the value should be null; */
public class CacheElementSerialized<K, V>
    implements ICacheElementSerialized<K, V>
{
    /** Don't change. */
    private static final long serialVersionUID = -7265084818647601874L;

    /** The serialized value. */
    private final byte[] serializedValue;

    /** The name of the cache region. This is a namespace. */
    private final String cacheName;

    /** This is the cache key by which the value can be referenced. */
    private final K key;

    /**
     * These attributes hold information about the element and what it is
     * allowed to do.
     */
    private IElementAttributes attr;

    /**
     * Constructs a usable wrapper.
     * <p>
     * @param cacheNameArg
     * @param keyArg
     * @param serializedValueArg
     * @param elementAttributesArg
     */
    public CacheElementSerialized( final String cacheName, final K key, final byte[] serializedValue,
                                   final IElementAttributes elementAttributes)
    {
        this.cacheName = cacheName;
        this.key = key;
        this.serializedValue = serializedValue;
        this.attr = elementAttributes;
    }

    /**
     * Gets the cacheName attribute of the CacheElement object
     * <p>
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Gets the key attribute of the CacheElement object
     * <p>
     * @return The key value
     */
    @Override
    public K getKey()
    {
        return this.key;
    }

    /**
     * Gets the val attribute of the CacheElement object
     * <p>
     * @return The val value
     */
    @Override
    public V getVal()
    {
        return null;
    }

    /**
     * Sets the attributes attribute of the CacheElement object
     * <p>
     * @param attr
     *            The new IElementAttributes value
     */
    @Override
    public void setElementAttributes( final IElementAttributes attr )
    {
        this.attr = attr;
    }

    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     * <p>
     * @return The IElementAttributes value, never null
     */
    @Override
    public IElementAttributes getElementAttributes()
    {
        // create default attributes if they are null
        // this shouldn't happen, but could if a corrupt
        // object was sent over the wire.
        if ( this.attr == null )
        {
            this.attr = new ElementAttributes();
        }
        return this.attr;
    }

    /** @return byte[] */
    @Override
    public byte[] getSerializedValue()
    {
        return this.serializedValue;
    }

    /**
     * @param obj other object
     * @return true if this object key equals the key of obj
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof CacheElementSerialized))
        {
            return false;
        }
        final CacheElementSerialized<?,?> other = (CacheElementSerialized<?,?>) obj;
        return Objects.equals(getKey(), other.getKey());
    }

    /**
     * For debugging only.
     * <p>
     * @return debugging string.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "\n CacheElementSerialized: " );
        buf.append( "\n CacheName = [").append(getCacheName()).append("]");
        buf.append( "\n Key = [").append(getKey()).append("]");
        buf.append( "\n SerializedValue = ").append(Arrays.toString(getSerializedValue()));
        buf.append( "\n ElementAttributes = ").append(getElementAttributes());
        return buf.toString();
    }

}
