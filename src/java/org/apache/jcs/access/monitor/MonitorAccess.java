package org.apache.jcs.access.monitor;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.CacheConstants;

/**
 * Exposes the simple monitoring methods to the public in a simple manner.
 *
 * @author asmuts
 * @created February 12, 2002
 */
public class MonitorAccess implements Serializable
{
    private static final Log log =
        LogFactory.getLog( MonitorAccess.class );

    /** Description of the Field */
    protected CompositeCacheManager cacheMgr;

    /** Constructor for the MonitorAccess object */
    public MonitorAccess()
    {
        // FIXME: Removed double-checked locking.
        if ( cacheMgr == null )
        {
            synchronized ( GroupCacheAccess.class )
            {
                if ( cacheMgr == null )
                {
                    cacheMgr = CompositeCacheManager.getInstance();
                }
            }
        }
    }

    /** Description of the Method */
    public String delete( String cacheName, String key )
    {

        // some junk to return for a synchronous call
        String result = "";

        try
        {

            ICache cache = cacheMgr.getCache( cacheName );

            if ( key != null )
            {
                if ( key.toUpperCase().equals( "ALL" ) )
                {
                    cache.removeAll();

                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "Removed all elements from " + cacheName );
                    }
                    result = "key = " + key;
                }
                else
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "key = " + key );
                    }
                    result = "key = " + key;
                    StringTokenizer toke = new StringTokenizer( key, "_" );

                    while ( toke.hasMoreElements() )
                    {
                        String temp = ( String ) toke.nextElement();
                        cache.remove( key );

                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "Removed " + temp + " from " + cacheName );
                        }
                    }
                }
            }
            else
            {
                result = "key is null";
            }

        }
        catch ( Exception e )
        {
            log.error( e );
        }

        return result;
    }

    /** Description of the Method */
    public ArrayList overview()
    {

        ArrayList data = new ArrayList();

        String[] list = cacheMgr.getCacheNames();
        Arrays.sort( list );
        for ( int i = 0; i < list.length; i++ )
        {
            Hashtable ht = new Hashtable();
            String name = list[ i ];
            ht.put( "name", name );

            ICache cache = cacheMgr.getCache( name );
            int size = cache.getSize();
            ht.put( "size", Integer.toString( size ) );

            int status = cache.getStatus();
            String stat = status == CacheConstants.STATUS_ALIVE ? "ALIVE"
                : status == CacheConstants.STATUS_DISPOSED ? "DISPOSED"
                : status == CacheConstants.STATUS_ERROR ? "ERROR"
                : "UNKNOWN";
            ht.put( "stat", stat );

            data.add( ht );
        }
        return data;
    }

}
