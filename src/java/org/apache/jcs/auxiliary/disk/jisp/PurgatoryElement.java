/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp;

import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementAttributes;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;

/**
 * Description of the Class
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
