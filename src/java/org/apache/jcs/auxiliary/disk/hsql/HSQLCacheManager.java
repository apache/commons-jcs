package org.apache.jcs.auxiliary.disk.hsql;


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

import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.auxiliary.AuxiliaryCache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 */
public class HSQLCacheManager implements AuxiliaryCacheManager
{
    private static final Log log =
        LogFactory.getLog( HSQLCacheManager.class );

    private static int clients;

    private static Hashtable caches = new Hashtable();

    private static HSQLCacheManager instance;

    private static HSQLCacheAttributes defaultCattr;


    /**
     * Constructor for the HSQLCacheManager object
     *
     * @param cattr
     */
    private HSQLCacheManager( HSQLCacheAttributes cattr )
    {
        this.defaultCattr = cattr;
    }


    /**
     * Gets the defaultCattr attribute of the HSQLCacheManager object
     *
     * @return The defaultCattr value
     */
    public HSQLCacheAttributes getDefaultCattr()
    {
        return this.defaultCattr;
    }


    /**
     * Gets the instance attribute of the HSQLCacheManager class
     *
     * @return The instance value
     */
    public static HSQLCacheManager getInstance( HSQLCacheAttributes cattr )
    {
        if ( instance == null )
        {
            synchronized ( HSQLCacheManager.class )
            {
                if ( instance == null )
                {
                    instance = new HSQLCacheManager( cattr );
                }
            }
        }

        clients++;
        return instance;
    }


    /**
     * Gets the cache attribute of the HSQLCacheManager object
     *
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        HSQLCacheAttributes cattr = ( HSQLCacheAttributes ) defaultCattr.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }


    /**
     * Gets the cache attribute of the HSQLCacheManager object
     *
     * @return The cache value
     */
    public AuxiliaryCache getCache( HSQLCacheAttributes cattr )
    {
        AuxiliaryCache raf = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            raf = ( AuxiliaryCache ) caches.get( cattr.getCacheName() );

            if ( raf == null )
            {
                raf = new HSQLCache( cattr );
                caches.put( cattr.getCacheName(), raf );
            }
        }

        return raf;
    }


    /** Description of the Method */
    public void freeCache( String name )
    {
        HSQLCache raf = ( HSQLCache ) caches.get( name );
        if ( raf != null )
        {
            raf.dispose();
        }
    }

    /**
     * Gets the cacheType attribute of the HSQLCacheManager object
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
                HSQLCache raf = ( HSQLCache ) allCaches.nextElement();
                if ( raf != null )
                {
                    raf.dispose();
                }
            }
        }
    }
}

