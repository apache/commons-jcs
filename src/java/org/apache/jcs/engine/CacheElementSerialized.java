package org.apache.jcs.engine;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.IElementAttributes;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Either serialized value or the value should be null;
 */
public class CacheElementSerialized
    implements ICacheElementSerialized
{

    private static final long serialVersionUID = -7265084818647601874L;

    /** The name of the cache region. This is a namespace. */
    private final String cacheName;

    /** This is the cache key by which the value can be referenced. */
    private final Serializable key;

    private final byte[] serializedValue;

    /**
     * These attributes hold information about the element and what it is
     * allowed to do.
     */
    private IElementAttributes elementAttributes;

    /**
     * 
     * @param cacheNameArg
     * @param keyArg
     * @param serializedValueArg
     * @param elementAttributesArg
     */
    public CacheElementSerialized( String cacheNameArg, Serializable keyArg, byte[] serializedValueArg,
                                  IElementAttributes elementAttributesArg )
    {
        this.cacheName = cacheNameArg;
        this.key = keyArg;
        this.serializedValue = serializedValueArg;
        this.elementAttributes = elementAttributesArg;
    }

    public String getCacheName()
    {
        return this.cacheName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheElement#getKey()
     */
    public Serializable getKey()
    {
        return this.key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheElementSerialized#getSerializedValue()
     */
    public byte[] getSerializedValue()
    {
        return this.serializedValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheElement#getElementAttributes()
     */
    public IElementAttributes getElementAttributes()
    {
        return this.elementAttributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.engine.behavior.ICacheElement#setElementAttributes(org.apache.jcs.engine.behavior.IElementAttributes)
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.elementAttributes = attr;
    }

    /**
     * Backward compatibility.
     */
    public Serializable getVal()
    {
        return null;
    }

    /**
     * For debugging only.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n CacheElementSerialized: " );
        buf.append( "\n CacheName = [" + getCacheName() + "]" );
        buf.append( "\n Key = [" + getKey() + "]" );
        buf.append( "\n SerializedValue = " + getSerializedValue() );
        buf.append( "\n ElementAttributes = " + getElementAttributes() );
        return buf.toString();
    }

}
