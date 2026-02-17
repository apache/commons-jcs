package org.apache.commons.jcs4.auxiliary.disk;

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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;

/**
 * Implements cache elements in purgatory.
 *
 * Elements are stored in purgatory when they are spooled to the auxiliary cache, but have not yet
 * been written to disk.
 */
public record PurgatoryElement<K, V>(
        /** Wrapped cache Element */
        ICacheElement<K, V> cacheElement,

        /** Is the element ready to be spooled? */
        AtomicBoolean mutableSpoolable
) implements ICacheElement<K, V>
{
    /** Don't change */
    private static final long serialVersionUID = -8152034342684135628L;

    /**
     * Constructor for the PurgatoryElement&lt;K, V&gt; object
     *
     * @param cacheElement CacheElement
     */
    public PurgatoryElement(final ICacheElement<K, V> cacheElement)
    {
        this(cacheElement, new AtomicBoolean());
    }

    /**
     * @return cacheElement.cacheName();
     * @see ICacheElement#cacheName
     */
    @Override
    public String cacheName()
    {
        return cacheElement.cacheName();
    }

    /**
     * @return cacheElement.elementAttributes();
     * @see ICacheElement#elementAttributes
     */
    @Override
    public IElementAttributes elementAttributes()
    {
        return cacheElement.elementAttributes();
    }

    /**
     * @return cacheElement.key();
     * @see ICacheElement#key
     */
    @Override
    public K key()
    {
        return cacheElement.key();
    }

    /**
     * @return cacheElement.value();
     * @see ICacheElement#value
     */
    @Override
    public V value()
    {
        return cacheElement.value();
    }

    /**
     * @return a hash of the key only
     */
    @Override
    public int hashCode()
    {
        return key().hashCode();
    }

    /**
     * Gets the spoolable property.
     *
     * @return The spoolable value
     */
    public boolean isSpoolable()
    {
        return mutableSpoolable.get();
    }

    /**
     * Sets the spoolable property.
     *
     * @param spoolable The new spoolable value
     */
    public void setSpoolable( final boolean spoolable )
    {
        this.mutableSpoolable.set(spoolable);
    }

    /**
     * @return debug string
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append( "[PurgatoryElement: " );
        buf.append( " isSpoolable = ").append(isSpoolable());
        buf.append( " CacheElement = ").append(cacheElement());
        buf.append( " CacheName = ").append(cacheName());
        buf.append( " Key = ").append(key());
        buf.append( " Value = ").append(value());
        buf.append( " ElementAttributes = ").append(elementAttributes());
        buf.append( "]" );
        return buf.toString();
    }
}
