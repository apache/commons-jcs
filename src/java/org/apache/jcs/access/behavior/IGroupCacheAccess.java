package org.apache.jcs.access.behavior;

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
import java.util.Enumeration;

import org.apache.jcs.access.exception.CacheException;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IGroupCacheAccess extends ICacheAccess
{

    /**
     * Gets the g attribute of the IGroupCacheAccess object
     *
     * @return The g value
     */
    Object getFromGroup( Object name, String group );


    /** Description of the Method */
    void putInGroup( Object key, String group, Object obj )
        throws CacheException;


    /** Description of the Method */
    void putInGroup( Object key, String group, Object obj, IElementAttributes attr )
        throws CacheException;


    /**
     * DefineGroup is used to create a new group object. IElementAttributes may be set
     * on the group. If no attributes are specified, the attributes of the
     * region or group the new group is associated with are used. If group is
     * specified the new group will be associated with the group specified.
     */
    void defineGroup( String name )
        throws CacheException;


    /** Description of the Method */
    void defineGroup( String name, IElementAttributes attr )
        throws CacheException;


    /**
     * Gets the groupAttributes attribute of the IGroupCacheAccess object
     *
     * @return The groupAttributes value
     */
    IElementAttributes getGroupAttributes( String name )
        throws CacheException;


    /**
     * Gets the attributeNames attribute of the IGroupCacheAccess object
     *
     * @return The attributeNames value
     */
    Enumeration getAttributeNames( String name );


    /**
     * Sets the attribute attribute of the IGroupCacheAccess object
     *
     * @param name The new attribute value
     * @param group The new attribute value
     * @param value The new attribute value
     */
    void setAttribute( Object name, String group, Object value )
        throws CacheException;


    /**
     * Sets the attribute attribute of the IGroupCacheAccess object
     *
     * @param name The new attribute value
     * @param group The new attribute value
     * @param value The new attribute value
     * @param attr The new attribute value
     */
    void setAttribute( Object name, String group, Object value, IElementAttributes attr )
        throws CacheException;


    /**
     * Gets the attribute attribute of the IGroupCacheAccess object
     *
     * @return The attribute value
     */
    Object getAttribute( Object name, String group );

}
