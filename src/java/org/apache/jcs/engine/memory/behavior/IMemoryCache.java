package org.apache.jcs.engine.memory.behavior;

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

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;

import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheType;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;

/**
 *  For the framework. Insures methods a MemoryCache needs to access.
 *
 *@author     asmuts
 *@created    January 15, 2002
 */
public interface IMemoryCache extends ICacheType
{

    // for initialization
    /**
     *  Description of the Method
     *
     *@param  cacheName  Description of the Parameter
     *@param  cattr      Description of the Parameter
     *@param  cache      Description of the Parameter
     */
    public void initialize( String cacheName,
                            ICompositeCacheAttributes cattr,
                            CompositeCache cache );

    // TODO: need a setCacheAttributes or reInitialize method

    /**
     *  Description of the Method
     *
     *@return    The size value
     */
    //public void makeFirst( MemoryElementDescriptor me );


    //public void moveToMemory( ICacheElement ce );

    /**
     *  Gets the size attribute of the IMemoryCache object
     *
     *@return    The size value
     */
    public int getSize();


    /**
     *  Get an iterator for all elements in the memory cache. This should be
     *  removed since it is fairly dangerous. Other classes should not be able
     *  to directly access items in the memory cache.
     *
     *@return        An iterator
     *@deprecated
     */
    public Iterator getIterator();


    /**
     *  Get an Array of the keys for all elements in the memory cache
     *
     *@return    An Object[]
     */
    public Object[] getKeyArray();

    /**
     *  Removes an item from the cache.
     *
     *@param  key              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public boolean remove( Serializable key )
        throws IOException;


    /**
     *  Removes all cached items from the cache.
     *
     *@exception  IOException  Description of the Exception
     */
    public void removeAll()
        throws IOException;


    /**
     *  Description of the Method
     *
     *@param  key              Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public Serializable get( Serializable key )
        throws IOException;

    /**
     *  Get an item from the cache without effecting its order or last access
     *  time
     *
     *@param  key              Description of the Parameter
     *@return                  The quiet value
     *@exception  IOException  Description of the Exception
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException;


    /**
     *  Description of the Method
     *
     *@param  key              Description of the Parameter
     *@param  container        Description of the Parameter
     *@return                  Description of the Return Value
     *@exception  IOException  Description of the Exception
     */
    public Serializable get( Serializable key, boolean container )
        throws IOException;


    /**
     *  Throws an item out of memory, if there is a disk cache it will be
     *  spooled.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void waterfal( ICacheElement ce )
        throws IOException;


    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void update( ICacheElement ce )
        throws IOException;


    /**
     *  Returns the CacheAttributes.
     *
     *@return    The cacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes();

    /**
     *  Sets the CacheAttributes.
     *
     *@param  cattr  The new cacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr );

    /**
     *  Gets the cache hub / region taht the MemoryCache is used by
     *
     *@return    The cache value
     */
    public CompositeCache getCompositeCache();


}
