package org.apache.jcs.admin;

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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.behavior.IMemoryCache;

/**
 * A servlet which provides HTTP access to JCS. Allows a summary of regions to be viewed, and
 * removeAll to be run on individual regions or all regions. Also provides the ability to remove
 * items (any number of key arguments can be provided with action 'remove'). Should be initialized
 * with a properties file that provides at least a classpath resource loader.
 */
public class JCSAdminBean
{
    /** The logger. */
    private static final Log log = LogFactory.getLog( JCSAdminBean.class );

    /** The cache maanger. */
    private CompositeCacheManager cacheHub = CompositeCacheManager.getInstance();

    /**
     * Builds up info about each element in a region.
     * <p>
     * @param cacheName
     * @return List of CacheElementInfo objects
     * @throws Exception
     */
    public LinkedList buildElementInfo( String cacheName )
        throws Exception
    {
        CompositeCache cache = cacheHub.getCache( cacheName );

        Object[] keys = cache.getMemoryCache().getKeyArray();

        // Attempt to sort keys according to their natural ordering. If that
        // fails, get the key array again and continue unsorted.

        try
        {
            Arrays.sort( keys );
        }
        catch ( Exception e )
        {
            keys = cache.getMemoryCache().getKeyArray();
        }

        LinkedList records = new LinkedList();

        ICacheElement element;
        IElementAttributes attributes;
        CacheElementInfo elementInfo;

        DateFormat format = DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );

        long now = System.currentTimeMillis();

        for ( int i = 0; i < keys.length; i++ )
        {
            element = cache.getMemoryCache().getQuiet( (Serializable) keys[i] );

            attributes = element.getElementAttributes();

            elementInfo = new CacheElementInfo();

            elementInfo.key = String.valueOf( keys[i] );
            elementInfo.eternal = attributes.getIsEternal();
            elementInfo.maxLifeSeconds = attributes.getMaxLifeSeconds();

            elementInfo.createTime = format.format( new Date( attributes.getCreateTime() ) );

            elementInfo.expiresInSeconds = ( now - attributes.getCreateTime() - ( attributes.getMaxLifeSeconds() * 1000 ) )
                / -1000;

            records.add( elementInfo );
        }

        return records;
    }

    /**
     * Builds up data on every region.
     * <p>
     * @TODO we need a most light weight method that does not count bytes. The byte counting can
     *       really swamp a server.
     * @return list of CacheRegionInfo objects
     * @throws Exception
     */
    public LinkedList buildCacheInfo()
        throws Exception
    {
        String[] cacheNames = cacheHub.getCacheNames();

        Arrays.sort( cacheNames );

        LinkedList cacheInfo = new LinkedList();

        CacheRegionInfo regionInfo;
        CompositeCache cache;

        for ( int i = 0; i < cacheNames.length; i++ )
        {
            cache = cacheHub.getCache( cacheNames[i] );

            regionInfo = new CacheRegionInfo();

            regionInfo.cache = cache;
            regionInfo.byteCount = getByteCount( cache );

            cacheInfo.add( regionInfo );
        }

        return cacheInfo;
    }

    /**
     * Tries to estimate how much data is in a region. This is expensive. If there are any non
     * serializable objects in the region, the count will stop when it encouters the first one.
     * <p>
     * @param cache
     * @return int
     * @throws Exception
     */
    public int getByteCount( CompositeCache cache )
        throws Exception
    {
        IMemoryCache memCache = cache.getMemoryCache();

        Iterator iter = memCache.getIterator();

        CountingOnlyOutputStream counter = new CountingOnlyOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( counter );

        // non serializable objects will cause problems here
        // stop at the first non serializable exception.
        try
        {
            while ( iter.hasNext() )
            {
                ICacheElement ce = (ICacheElement) ( (Map.Entry) iter.next() ).getValue();

                out.writeObject( ce.getVal() );
            }
        }
        catch ( Exception e )
        {
            log.info( "Problem getting byte count.  Likley cause is a non serilizable object." + e.getMessage() );
        }

        // 4 bytes lost for the serialization header

        return counter.getCount() - 4;
    }

    /**
     * Clears all regions in the cache.
     * <p>
     * @throws IOException
     */
    public void clearAllRegions()
        throws IOException
    {
        String[] names = cacheHub.getCacheNames();

        for ( int i = 0; i < names.length; i++ )
        {
            cacheHub.getCache( names[i] ).removeAll();
        }
    }

    /**
     * Clears a particular cache region.
     * <p>
     * @param cacheName
     * @throws IOException
     */
    public void clearRegion( String cacheName )
        throws IOException
    {
        cacheHub.getCache( cacheName ).removeAll();
    }

    /**
     * Removes a particular item from a particular region.
     * <p>
     * @param cacheName
     * @param key
     * @throws IOException
     */
    public void removeItem( String cacheName, String key )
        throws IOException
    {
        cacheHub.getCache( cacheName ).remove( key );
    }
}
