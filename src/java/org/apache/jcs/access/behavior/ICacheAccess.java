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

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheAccess
{

    // Static methods that should be implemented
    //public static void defineRegion( String name, CacheAttributes cattr,
    //                                 Attributes attr )  throws CacheException;
    //public static void defineRegion( String name, CacheAttributes cattr )
    //    throws CacheException;
    //public static void defineRegion( String name )  throws CacheException;
    //public static CacheAccess getAccess( String region );


    /** Description of the Method */
    Object get( Object name );


    /**
     * Puts in cache if an item does not exist with the name in that region.
     */
    void putSafe( Object name, Object obj )
        throws CacheException;

    /** Puts and/or overides an element with the name in that region. */
    void put( Object name, Object obj )
        throws CacheException;


    /** Description of the Method */
    void put( Object name, Object obj, IElementAttributes attr )
        throws CacheException;


    /**
     * Removes an item or all items. Should be called remove.
     *
     * @deprecated
     * @see #remove
     */
    void destroy()
        throws CacheException;

    /** Description of the Method */
    void remove()
        throws CacheException;

    /**
     * Description of the Method
     *
     * @deprecated
     * @see #remove
     */
    void destroy( Object name )
        throws CacheException;

    /** Description of the Method */
    void remove( Object name )
        throws CacheException;

//    /**
//     *  Remove either the entire region of elements or the the specified element
//     *  from other caches specified in in the cache.properties file as lateral
//     *  caches.
//     */
//    void removeLateralDirect();
//    /**
//     *  Description of the Method
//     *
//     */
//    void removeLateralDirect( Serializable key );

    /**
     * ResetAttributes allows for some of the attributes of a region to be reset
     * in particular expiration time attriubtes, time to live, default time to
     * live and idle time, and event handlers. The cacheloader object and
     * attributes set as flags can't be reset with resetAttributes, the object
     * must be destroyed and redefined to cache those parameters. Changing
     * default settings on groups and regions will not affect existing objects.
     * Only object loaded after the reset will use the new defaults. If no name
     * argument is provided, the reset is applied to the region.
     */
    void resetElementAttributes( IElementAttributes attr )
        throws CacheException;


    /** Description of the Method */
    void resetElementAttributes( Object name, IElementAttributes attr )
        throws CacheException;


    /**
     * GetElementAttributes will return an attribute object describing the current
     * attributes associated with the object name. If no name parameter is
     * available, the attributes for the region will be returned. The name
     * object must override the Object.equals and Object.hashCode methods.
     *
     * @return The elementAttributes value
     */
    IElementAttributes getElementAttributes()
        throws CacheException;


    /**
     * Gets the elementAttributes attribute of the ICacheAccess object
     *
     * @return The elementAttributes value
     */
    IElementAttributes getElementAttributes( Object name )
        throws CacheException;


    /**
     * Gets the ICompositeCacheAttributes of the cache region
     *
     * @return
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     * Sets the ICompositeCacheAttributes of the cache region
     *
     * @param cattr The new ICompositeCacheAttribute value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

}
// end interface
