package org.apache.jcs.engine.behavior;

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
import java.util.ArrayList;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 *  Inteface for cache element attributes classes.
 *
 *@author     asmuts
 *@created    January 15, 2002
 */
public interface IElementAttributes
{

    /**
     *  Sets the version attribute of the IAttributes object
     *
     *@param  version  The new version value
     */
    public void setVersion( long version );


    /**
     *  Sets the maxLife attribute of the IAttributes object
     *
     *@param  mls  The new {3} value
     */
    public void setMaxLifeSeconds( long mls );

    /**
     *  Sets the maxLife attribute of the IAttributes object
     *
     *@return    The {3} value
     */
    public long getMaxLifeSeconds();


    /**
     *  Sets the idleTime attribute of the IAttributes object
     *
     *@param  idle  The new idleTime value
     */
    public void setIdleTime( long idle );


    //public void setListener( int event, CacheEventListener listerner) {}

    /**
     *  Size in bytes.
     *
     *@param  size  The new size value
     */
    public void setSize( int size );


    /**
     *  Gets the size attribute of the IAttributes object
     *
     *@return    The size value
     */
    public int getSize();


    /**
     *  Gets the createTime attribute of the IAttributes object
     *
     *@return    The createTime value
     */
    public long getCreateTime();


    /**
     *  Gets the LastAccess attribute of the IAttributes object
     *
     *@return    The LastAccess value
     */
    public long getLastAccessTime();

    /**
     *  Sets the LastAccessTime as now of the IElementAttributes object
     */
    public void setLastAccessTimeNow();


    /**
     *  Gets the version attribute of the IAttributes object
     *
     *@return    The version value
     */
    public long getVersion();


    /**
     *  Gets the idleTime attribute of the IAttributes object
     *
     *@return    The idleTime value
     */
    public long getIdleTime();


    /**
     *  Gets the time left to live of the IAttributes object
     *
     *@return    The t value
     */
    public long getTimeToLiveSeconds();

    /**
     *  Returns a copy of the object.
     *
     *@return    IElementAttributes
     */
    public IElementAttributes copy();


    /**
     *  Gets the {3} attribute of the IElementAttributes object
     *
     *@return    The {3} value
     */
    public boolean getIsDistribute();

    /**
     *  Sets the isDistribute attribute of the IElementAttributes object
     *
     *@param  val  The new isDistribute value
     */
    public void setIsDistribute( boolean val );
    // lateral

    /**
     *  can this item be flushed to disk
     *
     *@return    The {3} value
     */
    public boolean getIsSpool();

    /**
     *  Sets the isSpool attribute of the IElementAttributes object
     *
     *@param  val  The new isSpool value
     */
    public void setIsSpool( boolean val );

    /**
     *  Is this item laterally distributable
     *
     *@return    The {3} value
     */
    public boolean getIsLateral();

    /**
     *  Sets the isLateral attribute of the IElementAttributes object
     *
     *@param  val  The new isLateral value
     */
    public void setIsLateral( boolean val );

    /**
     *  Can this item be sent to the remote cache
     *
     *@return    The {3} value
     */
    public boolean getIsRemote();

    /**
     *  Sets the isRemote attribute of the IElementAttributes object
     *
     *@param  val  The new isRemote value
     */
    public void setIsRemote( boolean val );

    /**
     *  can turn off expiration
     *
     *@return    The {3} value
     */
    public boolean getIsEternal();

    /**
     *  Sets the isEternal attribute of the IElementAttributes object
     *
     *@param  val  The new isEternal value
     */
    public void setIsEternal( boolean val );


    /**
     *  Adds a ElementEventHandler. Handler's can be registered for multiple
     *  events. A registered handler will be called at every recognized event.
     *
     *@param  eventHandler  The feature to be added to the ElementEventHandler
     */
    public void addElementEventHandler( IElementEventHandler eventHandler );

    /**
     *  Gets the elementEventHandlers.
     *
     *@return    The elementEventHandlers value
     */

    public ArrayList getElementEventHandlers();

}
