/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.jcs.auxiliary.disk.jisp;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;
import org.apache.jcs.auxiliary.AuxiliaryCache;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class JISPCacheManager implements AuxiliaryCacheManager
{
    private final static Log log =
        LogFactory.getLog( JISPCacheManager.class );

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
        this.defaultCattr = cattr;
    }

    /**
     * Gets the defaultCattr attribute of the JISPCacheManager object
     *
     * @return The defaultCattr value
     */
    public JISPCacheAttributes getDefaultCattr()
    {
        return this.defaultCattr;
    }

    /**
     * Gets the instance attribute of the JISPCacheManager class
     *
     * @return The instance value
     */
    public static JISPCacheManager getInstance( JISPCacheAttributes cattr )
    {
        if ( instance == null )
        {
            synchronized ( JISPCacheManager.class )
            {
                if ( instance == null )
                {
                    instance = new JISPCacheManager( cattr );
                }
            }
        }

        clients++;
        return instance;
    }

    /**
     * Gets the cache attribute of the JISPCacheManager object
     *
     * @return The cache value
     */
    public AuxiliaryCache getCache( String cacheName )
    {
        JISPCacheAttributes cattr = ( JISPCacheAttributes ) defaultCattr.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }

    /**
     * Gets the cache attribute of the JISPCacheManager object
     *
     * @return The cache value
     */
    public AuxiliaryCache getCache( JISPCacheAttributes cattr )
    {
        AuxiliaryCache raf = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            raf = ( AuxiliaryCache ) caches.get( cattr.getCacheName() );

            if ( raf == null )
            {
                raf = new JISPCache( cattr );

                caches.put( cattr.getCacheName(), raf );
            }
        }

        return raf;
    }

    /** Description of the Method */
    public void freeCache( String name )
    {
        JISPCache raf = ( JISPCache ) caches.get( name );
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
                JISPCache raf = ( JISPCache ) allCaches.nextElement();
                if ( raf != null )
                {
                    raf.dispose();
                }
            }
        }
    }
}
