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

import java.io.IOException;
import java.io.Serializable;

/**
 * Used to receive a cache event notification. <br>
 * <br>
 * Note: objects which implement this interface are local listeners to cache
 * changes, whereas objects which implement IRmiCacheListener are remote
 * listeners to cache changes.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ICacheListener
{
    /** Notifies the subscribers for a cache entry update. */
    public void handlePut( ICacheElement item )
        throws IOException;


    /** Notifies the subscribers for a cache entry removal. */
    public void handleRemove( String cacheName, Serializable key )
        throws IOException;


    /** Notifies the subscribers for a cache remove-all. */
    public void handleRemoveAll( String cacheName )
        throws IOException;


    /** Notifies the subscribers for freeing up the named cache. */
    public void handleDispose( String cacheName )
        throws IOException;


    /**
     * Notifies the subscribers for releasing all caches.
     *
     * @param id The new listenerId value
     */
//  public void handleRelease() throws IOException;

    /**
     * sets unique identifier of listener home
     *
     * @param id The new listenerId value
     */
    public void setListenerId( byte id )
        throws IOException;


    /**
     * Gets the listenerId attribute of the ICacheListener object
     *
     * @return The listenerId value
     */
    public byte getListenerId()
        throws IOException;

}
