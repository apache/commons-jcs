package org.apache.jcs.auxiliary.disk.indexed;

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

import org.apache.jcs.auxiliary.disk.indexed.behavior.IIndexedDiskElement;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Descriptor for each cache data entry stored on disk and put in purgatory. It
 * is used to set the spoolable flag so the queue knows that the element doesn't
 * need to be serialized.
 *
 * @author asmuts
 * @created January 15, 2002
 * @deprecated see PurgatoryElement
 */
class IndexedDiskElement extends CacheElement implements IIndexedDiskElement, Serializable
{

    private boolean isSpoolable = false;


    /**
     * Constructor for the DiskElement object
     *
     * @param ice
     */
    public IndexedDiskElement( ICacheElement ice )
    {
        this( ice.getCacheName(), ice.getKey(), ice.getVal() );
    }


    /**
     * Constructor for the DiskElement object
     *
     * @param cacheName
     * @param key
     * @param val
     */
    public IndexedDiskElement( String cacheName, Serializable key, Serializable val )
    {
        super( cacheName, key, val );
    }


    // lets the queue know that is ready to be spooled
    /**
     * Gets the isSpoolable attribute of the DiskElement object
     *
     * @return The isSpoolable value
     */
    public boolean getIsSpoolable()
    {
        return isSpoolable;
    }


    /**
     * Sets the isSpoolable attribute of the DiskElement object
     *
     * @param isSpoolable The new isSpoolable value
     */
    public void setIsSpoolable( boolean isSpoolable )
    {
        this.isSpoolable = isSpoolable;
    }

}
