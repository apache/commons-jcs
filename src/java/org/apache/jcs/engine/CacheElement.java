package org.apache.jcs.engine;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Generic element wrapper. Often stuffed inside another.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheElement implements ICacheElement, Serializable
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
        this( cacheName, key, ( Serializable ) val );
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
     * @param attr The new IElementAttributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        this.attr = ( ElementAttributes ) attr;
    }


    /**
     * Gets the IElementAttributes attribute of the CacheElement object
     *
     * @return The IElementAttributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return this.attr;
    }

    /** Description of the Method */
    public int hashCode()
    {
        return key.hashCode();
    }


    /** Description of the Method */
    public String toString()
    {
        return "[cacheName=" + cacheName + ", key=" + key + ", val=" + val + ", attr = " + attr + "]";
    }

}
// end class


