package org.apache.jcs.engine;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jcs.engine.behavior.ICacheObserver;
import org.apache.jcs.engine.behavior.ICacheListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceps the requests to the underlying ICacheObserver object so that the
 * listeners can be recorded locally for remote connection recovery purposes.
 * (Durable subscription like those in JMS is not implemented at this stage for
 * it can be too expensive on the runtime.)
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CacheWatchRepairable implements ICacheObserver
{
    private final static Log log =
        LogFactory.getLog( CacheWatchRepairable.class );

    // the underlying ICacheObserver.
    private ICacheObserver cacheWatch;
    private Map cacheMap = new HashMap();

    /**
     * Replaces the underlying cache watch service and reattached all existing
     * listeners to the new cache watch.
     *
     * @param cacheWatch The new cacheWatch value
     */
    public void setCacheWatch( ICacheObserver cacheWatch )
    {
        this.cacheWatch = cacheWatch;
        synchronized ( cacheMap )
        {
            for ( Iterator itr = cacheMap.entrySet().iterator(); itr.hasNext();  )
            {
                Map.Entry entry = ( Map.Entry ) itr.next();
                String cacheName = ( String ) entry.getKey();
                Set listenerSet = ( Set ) entry.getValue();
                for ( Iterator itr2 = listenerSet.iterator(); itr2.hasNext();  )
                {
                    try
                    {
                        cacheWatch.addCacheListener( cacheName, ( ICacheListener ) itr2.next() );
                    }
                    catch ( IOException ex )
                    {
                        log.error( ex );
                    }
                }
            }
        }
    }


    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable
     * object
     *
     * @param cacheName The feature to be added to the CacheListener attribute
     * @param obj The feature to be added to the CacheListener attribute
     */
    public void addCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the remote add-listener
        // operation succeeds or fails.
        synchronized ( cacheMap )
        {
            Set listenerSet = ( Set ) cacheMap.get( cacheName );
            if ( listenerSet == null )
            {
                listenerSet = new HashSet();
                cacheMap.put( cacheName, listenerSet );
            }
            listenerSet.add( obj );
        }
        cacheWatch.addCacheListener( cacheName, obj );
    }


    /**
     * Adds a feature to the CacheListener attribute of the CacheWatchRepairable
     * object
     *
     * @param obj The feature to be added to the CacheListener attribute
     */
    public void addCacheListener( ICacheListener obj )
        throws IOException
    {
        // Record the added cache listener locally, regardless of whether the remote add-listener
        // operation succeeds or fails.
        synchronized ( cacheMap )
        {
            for ( Iterator itr = cacheMap.values().iterator(); itr.hasNext();  )
            {
                Set listenerSet = ( Set ) itr.next();
                listenerSet.add( obj );
            }
        }
        cacheWatch.addCacheListener( obj );
    }


    /** Description of the Method */
    public void removeCacheListener( String cacheName, ICacheListener obj )
        throws IOException
    {
        // Record the removal locally, regardless of whether the remote remove-listener
        // operation succeeds or fails.
        synchronized ( cacheMap )
        {
            Set listenerSet = ( Set ) cacheMap.get( cacheName );
            if ( listenerSet != null )
            {
                listenerSet.remove( obj );
            }
        }
        cacheWatch.removeCacheListener( cacheName, obj );
    }


    /** Description of the Method */
    public void removeCacheListener( ICacheListener obj )
        throws IOException
    {
        // Record the removal locally, regardless of whether the remote remove-listener
        // operation succeeds or fails.
        synchronized ( cacheMap )
        {
            for ( Iterator itr = cacheMap.values().iterator(); itr.hasNext();  )
            {
                Set listenerSet = ( Set ) itr.next();
                listenerSet.remove( obj );
            }
        }
        cacheWatch.removeCacheListener( obj );
    }
}
