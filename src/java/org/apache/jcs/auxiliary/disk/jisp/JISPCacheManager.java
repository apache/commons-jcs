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

import org.apache.jcs.auxiliary.disk.jisp.behavior.IJISPCacheAttributes;

import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class JISPCacheManager implements ICacheManager
{
    private final static Log log =
        LogFactory.getLog( JISPCacheManager.class );

    private static int clients;

    private static Hashtable caches = new Hashtable();

    private static JISPCacheManager instance;

    private static IJISPCacheAttributes defaultCattr;


    /**
     * Constructor for the JISPCacheManager object
     *
     * @param cattr
     */
    private JISPCacheManager( IJISPCacheAttributes cattr )
    {
        this.defaultCattr = cattr;
    }


    /**
     * Gets the defaultCattr attribute of the JISPCacheManager object
     *
     * @return The defaultCattr value
     */
    public IJISPCacheAttributes getDefaultCattr()
    {
        return this.defaultCattr;
    }


    /**
     * Gets the instance attribute of the JISPCacheManager class
     *
     * @return The instance value
     */
    public static JISPCacheManager getInstance( IJISPCacheAttributes cattr )
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
        if ( log.isDebugEnabled() )
        {
            log.debug( "Manager stats : " + instance.getStats() + "-- in getInstance()" );
        }
        clients++;
        return instance;
    }


    /**
     * Gets the cache attribute of the JISPCacheManager object
     *
     * @return The cache value
     */
    public ICache getCache( String cacheName )
    {
        IJISPCacheAttributes cattr = ( IJISPCacheAttributes ) defaultCattr.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }


    /**
     * Gets the cache attribute of the JISPCacheManager object
     *
     * @return The cache value
     */
    public ICache getCache( IJISPCacheAttributes cattr )
    {
        ICache raf = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            raf = ( ICache ) caches.get( cattr.getCacheName() );

            if ( raf == null )
            {
                // make use cattr
                //raf = new JISPCache( cattr.getCacheName(), cattr.getDiskPath() );
                raf = new JISPCacheNoWaitBuffer( cattr );
                caches.put( cattr.getCacheName(), raf );
            }
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "Manager stats : " + instance.getStats() );
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


    // Don't care if there is a concurrency failure ?
    /**
     * Gets the stats attribute of the JISPCacheManager object
     *
     * @return The stats value
     */
    public String getStats()
    {
        StringBuffer stats = new StringBuffer();
        Enumeration allCaches = caches.elements();

        while ( allCaches.hasMoreElements() )
        {
            ICache raf = ( ICache ) allCaches.nextElement();

            if ( raf != null )
            {
                stats.append( raf.getStats() );
                stats.append( ", " );
            }
        }

        return stats.toString();
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
