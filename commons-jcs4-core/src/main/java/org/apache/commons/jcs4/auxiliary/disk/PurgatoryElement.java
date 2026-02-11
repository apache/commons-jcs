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

import java.util.Objects;

import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;

/**
 * Implements cache elements in purgatory.
 *
 * Elements are stored in purgatory when they are spooled to the auxiliary cache, but have not yet
 * been written to disk.
 */
public class PurgatoryElement<K, V>
    implements ICacheElement<K, V>
{
    /** Don't change */
    private static final long serialVersionUID = -8152034342684135628L;

    /** Is the element ready to be spooled? */
    private boolean spoolable;

    /** Wrapped cache Element */
    private final ICacheElement<K, V> cacheElement;

    /**
     * Constructor for the PurgatoryElement&lt;K, V&gt; object
     *
     * @param cacheElement CacheElement
     */
    public PurgatoryElement( final ICacheElement<K, V> cacheElement )
    {
        this.cacheElement = cacheElement;
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
        if (obj instanceof PurgatoryElement pe)
        {
            return Objects.equals(getKey(), pe.getKey());
        }
        return false;
    }

    /**
     * Gets the wrapped cache element.
     *
     * @return ICacheElement
     */
    public ICacheElement<K, V> getCacheElement()
    {
        return cacheElement;
    }

    /**
     * @return cacheElement.getCacheName();
     * @see ICacheElement#getCacheName
     */
    @Override
    public String getCacheName()
    {
        return cacheElement.getCacheName();
    }

    /**
     * @return cacheElement.getElementAttributes();
     * @see ICacheElement#getElementAttributes
     */
    @Override
    public IElementAttributes getElementAttributes()
    {
        return cacheElement.getElementAttributes();
    }

    /**
     * @return cacheElement.getKey();
     * @see ICacheElement#getKey
     */
    @Override
    public K getKey()
    {
        return cacheElement.getKey();
    }

    /**
     * @return cacheElement.getVal();
     * @see ICacheElement#getVal
     */
    @Override
    public V getVal()
    {
        return cacheElement.getVal();
    }

    /**
     * @return a hash of the key only
     */
    @Override
    public int hashCode()
    {
        return getKey().hashCode();
    }

    /**
     * Gets the spoolable property.
     *
     * @return The spoolable value
     */
    public boolean isSpoolable()
    {
        return spoolable;
    }

    /**
     * @param attr
     * @see ICacheElement#setElementAttributes
     */
    @Override
    public void setElementAttributes( final IElementAttributes attr )
    {
        cacheElement.setElementAttributes( attr );
    }

    /**
     * Sets the spoolable property.
     *
     * @param spoolable The new spoolable value
     */
    public void setSpoolable( final boolean spoolable )
    {
        this.spoolable = spoolable;
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
        buf.append( " CacheElement = ").append(getCacheElement());
        buf.append( " CacheName = ").append(getCacheName());
        buf.append( " Key = ").append(getKey());
        buf.append( " Value = ").append(getVal());
        buf.append( " ElementAttributes = ").append(getElementAttributes());
        buf.append( "]" );
        return buf.toString();
    }
}
