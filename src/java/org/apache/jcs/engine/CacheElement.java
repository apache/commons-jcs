package org.apache.jcs.engine;

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

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Generic element wrapper. Often stuffed inside another.
 *  
 */
public class CacheElement
    implements ICacheElement, Serializable
{

    /** Description of the Field */
    public final String cacheName;

    /** Description of the Field */
    public final Serializable key;

    /** Description of the Field */
    public final Serializable val;

    /** Description of the Field */
    public ElementAttributes attr;

    // make sure it is open for spooling?
    //public boolean isLocked = false;

    /**
     * Constructor for the CacheElement object
     * 
     * @param cacheName
     * @param key
     * @param val
     */
    public CacheElement( String cacheName, Serializable key, Serializable val )
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
     */
    public CacheElement( String cacheName, Serializable key, Object val )
    {
        this( cacheName, key, (Serializable) val );
    }

    /**
     * Gets the cacheName attribute of the CacheElement object
     * 
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /**
     * Gets the key attribute of the CacheElement object
     * 
     * @return The key value
     */
    public Serializable getKey()
    {
        return this.key;
    }

    /**
     * Gets the val attribute of the CacheElement object
     * 
     * @return The val value
     */
    public Serializable getVal()
    {
        return this.val;
    }

    /**
     * Sets the attributes attribute of the CacheElement object
     * 
     * @param attr
     *            The new IElementAttributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = (ElementAttributes) attr;
    }

    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     * 
     * @return The IElementAttributes value, never null
     */
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
     * @return a hash of the key only
     * 
     */
    public int hashCode()
    {
        return key.hashCode();
    }

    /**
     * For debuggin only.
     * 
     * @return String representation
     *  
     */
    public String toString()
    {
        return "[cacheName=" + cacheName + ", key=" + key + ", val=" + val + ", attr = " + attr + "]";
    }

}
// end class

