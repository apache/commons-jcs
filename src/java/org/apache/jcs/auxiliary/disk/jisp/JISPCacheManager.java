package org.apache.jcs.auxiliary.disk.jisp;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.auxiliary.AuxiliaryCache;

/**
 * Description of the Class
 *  
 */
public class JISPCacheManager
    implements AuxiliaryCacheManager
{
    private final static Log log = LogFactory.getLog( JISPCacheManager.class );

    private static int clients;

    private static Hashtable caches = new Hashtable();

    private static JISPCacheManager instance;

    private static JISPCacheAttributes defaultCattr;

    /**
     * Constructor for the JISPCacheManager object
     * 
     * @param cattr
     */
    private JISPCacheManager( JISPCacheAttributes cattr )
    {
        JISPCacheManager.defaultCattr = cattr;
    }

    /**
     * Gets the defaultCattr attribute of the JISPCacheManager object
     * 
     * @return The defaultCattr value
     */
    public JISPCacheAttributes getDefaultCattr()
    {
        return JISPCacheManager.defaultCattr;
    }

    /**
     * Gets the instance attribute of the JISPCacheManager class
     * @param cattr
     * 
     * @return The instance value
     */
    public static JISPCacheManager getInstance( JISPCacheAttributes cattr )
    {
        synchronized ( JISPCacheManager.class )
        {
            if ( instance == null )
            {
                instance = new JISPCacheManager( cattr );
            }
        }

        clients++;
        return instance;
    }

    /**
     * Gets the cache attribute of the JISPCacheManager object
     * @param cacheName
     * 
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        JISPCacheAttributes cattr = (JISPCacheAttributes) defaultCattr.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }

    /**
     * Gets the cache attribute of the JISPCacheManager object
     * @param cattr
     * 
     * @return The cache value
     */
    public AuxiliaryCache getCache( JISPCacheAttributes cattr )
    {
        AuxiliaryCache raf = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            raf = (AuxiliaryCache) caches.get( cattr.getCacheName() );

            if ( raf == null )
            {
                raf = new JISPCache( cattr );

                caches.put( cattr.getCacheName(), raf );
            }
        }

        return raf;
    }

    /**
     * 
     * @param name
     */
    public void freeCache( String name )
    {
        JISPCache raf = (JISPCache) caches.get( name );
        if ( raf != null )
        {
            raf.dispose();
        }
    }

    /**
     * Gets the cacheType attribute of the JISPCacheManager object
     * 
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }

    /** Description of the Method */
    public void release()
    {
        // Wait until called by the last client
        if ( --clients != 0 )
        {
            return;
        }
        synchronized ( caches )
        {
            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                JISPCache raf = (JISPCache) allCaches.nextElement();
                if ( raf != null )
                {
                    raf.dispose();
                }
            }
        }
    }
}
