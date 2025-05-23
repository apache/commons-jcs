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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Objects;

import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;

/**
 * Generic element wrapper. Often stuffed inside another.
 */
public class CacheElement<K, V>
    implements ICacheElement<K, V>
{
    /** Don't change */
    private static final long serialVersionUID = -6062305728297627263L;

    /** The name of the cache region. This is a namespace. */
    private final String cacheName;

    /** This is the cache key by which the value can be referenced. */
    private final K key;

    /** This is the cached value, reference by the key. */
    private final V val;

    /**
     * These attributes hold information about the element and what it is
     * allowed to do.
     */
    private IElementAttributes attr;

    /**
     * Constructor for the CacheElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement( final String cacheName, final K key, final V val )
    {
        this.cacheName = cacheName;
        this.key = key;
        this.val = val;
    }

    /**
     * Constructor for the CacheElement object
     *
     * @param cacheName
     * @param key
     * @param val
     * @param attrArg
     */
    public CacheElement( final String cacheName, final K key, final V val, final IElementAttributes attrArg )
    {
        this(cacheName, key, val);
        this.attr = attrArg;
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
        if (!(obj instanceof CacheElement))
        {
            return false;
        }
        final CacheElement<?,?> other = (CacheElement<?,?>) obj;
        return Objects.equals(key, other.key);
    }

    /**
     * Gets the cacheName attribute of the CacheElement object
     *
     * @return The cacheName value
     */
    @Override
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     *
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

    /**
     * Gets the key attribute of the CacheElement object
     *
     * @return The key value
     */
    @Override
    public K getKey()
    {
        return this.key;
    }

    /**
     * Gets the val attribute of the CacheElement object
     *
     * @return The val value
     */
    @Override
    public V getVal()
    {
        return this.val;
    }

    /**
     * @return a hash of the key only
     */
    @Override
    public int hashCode()
    {
        return key.hashCode();
    }

    /**
     * Sets the attributes attribute of the CacheElement object
     *
     * @param attr
     *            The new IElementAttributes value
     */
    @Override
    public void setElementAttributes( final IElementAttributes attr )
    {
        this.attr = attr;
    }

    /**
     * For debugging only.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return "[CacheElement: cacheName [" + cacheName + "], key [" + key + "], val [" + val + "], attr [" + attr
            + "]";
    }
}
