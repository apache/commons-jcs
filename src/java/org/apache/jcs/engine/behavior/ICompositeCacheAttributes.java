package org.apache.jcs.engine.behavior;

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

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICompositeCacheAttributes extends Serializable
{

    /**
     * SetMaxObjects is used to set the attribute to determine the maximum
     * number of objects allowed in the memory cache. If the max number of
     * objects or the cache size is set, the default for the one not set is
     * ignored. If both are set, both are used to determine the capacity of the
     * cache, i.e., object will be removed from the cache if either limit is
     * reached. TODO: move to MemoryCache config file.
     *
     * @param size The new maxObjects value
     */
    public void setMaxObjects( int size );


    /**
     * Gets the maxObjects attribute of the ICompositeCacheAttributes object
     *
     * @return The maxObjects value
     */
    public int getMaxObjects();


    /**
     * Sets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @param useDisk The new useDisk value
     */
    public void setUseDisk( boolean useDisk );


    /**
     * Gets the useDisk attribute of the ICompositeCacheAttributes object
     *
     * @return The useDisk value
     */
    public boolean getUseDisk();


    /**
     * set whether the cache should use a lateral cache
     *
     * @param d The new useLateral value
     */
    public void setUseLateral( boolean d );


    /**
     * Gets the useLateral attribute of the ICompositeCacheAttributes object
     *
     * @return The useLateral value
     */
    public boolean getUseLateral();


    /**
     * Sets whether the cache is remote enabled
     *
     * @param isRemote The new useRemote value
     */
    public void setUseRemote( boolean isRemote );


    /**
     * returns whether the cache is remote enabled
     *
     * @return The useRemote value
     */
    public boolean getUseRemote();


    /**
     * Sets the name of the cache, referenced by the appropriate manager.
     *
     * @param s The new cacheName value
     */
    public void setCacheName( String s );


    /**
     * Gets the cacheName attribute of the ICompositeCacheAttributes object
     *
     * @return The cacheName value
     */
    public String getCacheName();


    /**
     * Sets the name of the MemoryCache, referenced by the appropriate manager.
     * TODO: create a separate memory cache attribute class.
     *
     * @param s The new memoryCacheName value
     */
    public void setMemoryCacheName( String s );


    /**
     * Gets the memoryCacheName attribute of the ICompositeCacheAttributes
     * object
     *
     * @return The memoryCacheName value
     */
    public String getMemoryCacheName();


    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @param useShrinker The new UseMemoryShrinker value
     */
    public void setUseMemoryShrinker( boolean useShrinker );

    /**
     * Whether the memory cache should perform background memory shrinkage.
     *
     * @return The UseMemoryShrinker value
     */
    public boolean getUseMemoryShrinker();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @param seconds The new MaxMemoryIdleTimeSeconds value
     */
    public void setMaxMemoryIdleTimeSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space.
     *
     * @return The MaxMemoryIdleTimeSeconds value
     */
    public long getMaxMemoryIdleTimeSeconds();

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This sets the shrinker interval.
     *
     * @param seconds The new ShrinkerIntervalSeconds value
     */
    public void setShrinkerIntervalSeconds( long seconds );

    /**
     * If UseMemoryShrinker is true the memory cache should auto-expire elements
     * to reclaim space. This gets the shrinker interval.
     *
     * @return The ShrinkerIntervalSeconds value
     */
    public long getShrinkerIntervalSeconds();


    // soultion to interface cloning
    /**
     * Description of the Method
     *
     * @return
     */
    public ICompositeCacheAttributes copy();

}
