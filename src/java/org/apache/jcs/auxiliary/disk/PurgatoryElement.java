package org.apache.jcs.auxiliary.disk;

/*
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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Wrapper for cache elements in purgatory. Elements are stored in purgatory
 * when they are spooled to the auxilliary cache, but have not yet been
 * written to disk.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class PurgatoryElement implements ICacheElement, Serializable
{
    /**
     * Is the element ready to be spooled?
     */
    protected boolean spoolable = false;

    /**
     * Wrapped cache Element
     */
    protected ICacheElement cacheElement;

    /**
     * Constructor for the PurgatoryElement object
     *
     * @param cacheElement CacheElement to wrap.
     */
    public PurgatoryElement( ICacheElement cacheElement )
    {
        this.cacheElement = cacheElement;
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
     * Sets the spoolable property.
     *
     * @param spoolable The new spoolable value
     */
    public void setSpoolable( boolean spoolable )
    {
        this.spoolable = spoolable;
    }

    /**
     * Get the wrapped cache element.
     */
    public ICacheElement getCacheElement()
    {
        return cacheElement;
    }

    // ------------------------------------------------ interface ICacheElement

    /**
     * @see ICacheElement#getCacheName
     */
    public String getCacheName()
    {
        return cacheElement.getCacheName();
    }

    /**
     * @see ICacheElement#getKey
     */
    public Serializable getKey()
    {
        return cacheElement.getKey();
    }

    /**
     * @see ICacheElement#getVal
     */
    public Serializable getVal()
    {
        return cacheElement.getVal();
    }

    /**
     * @see ICacheElement#getElementAttributes
     */
    public IElementAttributes getElementAttributes()
    {
        return cacheElement.getElementAttributes();
    }

    /**
     * @see ICacheElement#setElementAttributes
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        cacheElement.setElementAttributes( attr );
    }

    /**
     * @see ICacheElement#getCreateTime
     */
    public long getCreateTime()
    {
        return cacheElement.getCreateTime();
    }
}
