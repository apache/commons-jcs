package org.apache.jcs.auxiliary.disk.hsql;

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
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Wrapper for items that are in purgatory -- pre-disk storage. Keeps track of
 * whether the item has been rescued from purgatory prior to being banished to
 * the disk. Avoid excessive disk writing.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class PurgatoryElement implements ICacheElement, Serializable
{

    // need speed here.  the method calls are unnecessary.   make protected
    /** Description of the Field */
    protected boolean isSpoolable = false;
    /** Description of the Field */
    protected ICacheElement ice;


    /**
     * Constructor for the PurgatoryElement object
     *
     * @param ice
     */
    public PurgatoryElement( ICacheElement ice )
    {
        this.ice = ice;
    }


// lets the queue know that is ready to be spooled
    /**
     * Gets the isSpoolable attribute of the PurgatoryElement object
     *
     * @return The isSpoolable value
     */
    public boolean getIsSpoolable()
    {
        return isSpoolable;
    }


    /**
     * Sets the isSpoolable attribute of the PurgatoryElement object
     *
     * @param isSpoolable The new isSpoolable value
     */
    public void setIsSpoolable( boolean isSpoolable )
    {
        this.isSpoolable = isSpoolable;
    }


    // ICacheElement Methods
    /**
     * Gets the cacheName attribute of the PurgatoryElement object
     *
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return ice.getCacheName();
    }


    /**
     * Gets the key attribute of the PurgatoryElement object
     *
     * @return The key value
     */
    public Serializable getKey()
    {
        return ice.getKey();
    }


    /**
     * Gets the val attribute of the PurgatoryElement object
     *
     * @return The val value
     */
    public Serializable getVal()
    {
        return ice.getVal();
    }


    /**
     * Gets the attributes attribute of the PurgatoryElement object
     *
     * @return The attributes value
     */
    public IElementAttributes getElementAttributes()
    {
        return ice.getElementAttributes();
    }


    /**
     * Sets the attributes attribute of the PurgatoryElement object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes( IElementAttributes attr )
    {
        ice.setElementAttributes( attr );
    }


    /**
     * Gets the createTime attribute of the PurgatoryElement object
     *
     * @return The createTime value
     */
    public long getCreateTime()
    {
        return ice.getCreateTime();
    }

}
