package org.apache.jcs.access.monitor;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Exposes the simple monitoring methods to the public in a simple manner.
 */
public class MonitorAccess
    implements Serializable
{
    /** Don't change. */
    private static final long serialVersionUID = 1002037665133774391L;

    /** The logger. */
    private static final Log log = LogFactory.getLog( MonitorAccess.class );

    /** Description of the Field */
    protected CompositeCacheManager cacheMgr;

    /** Constructor for the MonitorAccess object */
    public MonitorAccess()
    {
        synchronized ( GroupCacheAccess.class )
        {
            if ( this.cacheMgr == null )
            {
                this.cacheMgr = CompositeCacheManager.getInstance();
            }
        }
    }

    /**
     * Removes all.
     * <p>
     * @param cacheName
     * @param key
     * @return an informative message about what was deleted.
     */
    public String delete( String cacheName, String key )
    {
        // some junk to return for a synchronous call
        String result = "";

        try
        {
            ICache cache = this.cacheMgr.getCache( cacheName );

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
                        String temp = (String) toke.nextElement();
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

    /**
     * Gives basic info on all the regions. Better to use getStats.
     * <p>
     * @return list of hashtables with keys (name,size,stat)
     */
    public ArrayList overview()
    {
        ArrayList data = new ArrayList();

        String[] list = this.cacheMgr.getCacheNames();
        Arrays.sort( list );
        for ( int i = 0; i < list.length; i++ )
        {
            Hashtable ht = new Hashtable();
            String name = list[i];
            ht.put( "name", name );

            ICache cache = this.cacheMgr.getCache( name );
            int size = cache.getSize();
            ht.put( "size", Integer.toString( size ) );

            int status = cache.getStatus();
            String stat = status == CacheConstants.STATUS_ALIVE
                                                               ? "ALIVE"
                                                               : status == CacheConstants.STATUS_DISPOSED
                                                                                                         ? "DISPOSED"
                                                                                                         : status == CacheConstants.STATUS_ERROR
                                                                                                                                                ? "ERROR"
                                                                                                                                                : "UNKNOWN";
            ht.put( "stat", stat );

            data.add( ht );
        }
        return data;
    }
}
