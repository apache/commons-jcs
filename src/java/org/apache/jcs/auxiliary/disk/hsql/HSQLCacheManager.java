package org.apache.jcs.auxiliary.disk.hsql;

/*
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.jcs.auxiliary.disk.hsql.behavior.IHSQLCacheAttributes;

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
public class HSQLCacheManager implements ICacheManager
{
    private final static Log log =
        LogFactory.getLog( HSQLCacheManager.class );

    private static int clients;

    private static Hashtable caches = new Hashtable();

    private static HSQLCacheManager instance;

    private static IHSQLCacheAttributes defaultCattr;


    /**
     * Constructor for the HSQLCacheManager object
     *
     * @param cattr
     */
    private HSQLCacheManager( IHSQLCacheAttributes cattr )
    {
        this.defaultCattr = cattr;
    }


    /**
     * Gets the defaultCattr attribute of the HSQLCacheManager object
     *
     * @return The defaultCattr value
     */
    public IHSQLCacheAttributes getDefaultCattr()
    {
        return this.defaultCattr;
    }


    /**
     * Gets the instance attribute of the HSQLCacheManager class
     *
     * @return The instance value
     */
    public static HSQLCacheManager getInstance( IHSQLCacheAttributes cattr )
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
        if ( log.isDebugEnabled() )
        {
            log.debug( "Manager stats : " + instance.getStats() + " -- in getInstance()" );
        }
        clients++;
        return instance;
    }


    /**
     * Gets the cache attribute of the HSQLCacheManager object
     *
     * @return The cache value
     */
    public ICache getCache( String cacheName )
    {
        IHSQLCacheAttributes cattr = ( IHSQLCacheAttributes ) defaultCattr.copy();
        cattr.setCacheName( cacheName );
        return getCache( cattr );
    }


    /**
     * Gets the cache attribute of the HSQLCacheManager object
     *
     * @return The cache value
     */
    public ICache getCache( IHSQLCacheAttributes cattr )
    {
        ICache raf = null;

        log.debug( "cacheName = " + cattr.getCacheName() );

        synchronized ( caches )
        {
            raf = ( ICache ) caches.get( cattr.getCacheName() );

            if ( raf == null )
            {
                // make use cattr
                //raf = new HSQLCache( cattr.getCacheName(), cattr.getDiskPath() );
                raf = new HSQLCacheNoWaitBuffer( cattr );
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
        HSQLCache raf = ( HSQLCache ) caches.get( name );
        if ( raf != null )
        {
            raf.dispose();
        }
    }


    // Don't care if there is a concurrency failure ?
    /**
     * Gets the stats attribute of the HSQLCacheManager object
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

