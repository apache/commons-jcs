package org.apache.jcs.auxiliary.disk.indexed;

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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.disk.indexed.behavior.IIndexedDiskCacheAttributes;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheManager;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Cache manager for IndexedDiskCaches.
 *
 * @author <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class IndexedDiskCacheManager implements ICacheManager
{
    private final static Log log =
        LogFactory.getLog( IndexedDiskCacheManager.class );

    private static int clients;

    private static IndexedDiskCacheManager instance;

    private Hashtable caches = new Hashtable();

    private IIndexedDiskCacheAttributes defaultCacheAttributes;

    /**
     * Constructor for the IndexedDiskCacheManager object
     *
     * @param defaultCacheAttributes Default attributes for caches managed by
     *                               the instance.
     */
    private IndexedDiskCacheManager(
        IIndexedDiskCacheAttributes defaultCacheAttributes )
    {
        this.defaultCacheAttributes = defaultCacheAttributes;
    }

    /**
     * Gets the singleton instance of the manager
     *
     * @param defaultCacheAttributes If the instance has not yet been created,
     *                               it will be initialized with this set of
     *                               default attributes.
     * @return The instance value
     */
    public static IndexedDiskCacheManager getInstance(
        IIndexedDiskCacheAttributes defaultCacheAttributes )
    {
        if ( instance == null )
        {
            synchronized ( IndexedDiskCacheManager.class )
            {
                if ( instance == null )
                {
                    instance =
                        new IndexedDiskCacheManager( defaultCacheAttributes );
                }
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "Get instance, manager stats: " + instance.getStats() );
        }

        clients++;

        return instance;
    }

    /**
     * Gets an IndexedDiskCache for the supplied name using the default
     * attributes.
     *
     * @see getCache( IIndexedDiskCacheAttributes }
     *
     * @param cacheName Name that will be used when creating attributes.
     * @return A cache.
     */
    public ICache getCache( String cacheName )
    {
        IIndexedDiskCacheAttributes cacheAttributes =
            ( IIndexedDiskCacheAttributes ) defaultCacheAttributes.copy();

        cacheAttributes.setCacheName( cacheName );

        return getCache( cacheAttributes );
    }

    /**
     * Get an IndexedDiskCache for the supplied attributes. Will provide an
     * existing cache for the name attribute if one has been created, or will
     * create a new cache.
     *
     * @param cattr Attributes the cache should have.
     * @return A cache, either from the existing set or newly created.
     *
     */
    public ICache getCache( IIndexedDiskCacheAttributes cacheAttributes )
    {
        ICache cache = null;

        String cacheName = cacheAttributes.getCacheName();

        log.debug( "Getting cache named: " + cacheName );

        synchronized ( caches )
        {
            // Try to load the cache from the set that have already been
            // created. This only looks at the name attribute.

            cache = ( ICache ) caches.get( cacheName );

            // If it was not found, create a new one using the supplied
            // attributes

            if ( cache == null )
            {
                cache = new IndexedDiskCache( cacheAttributes );

                caches.put( cacheName, cache );
            }
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "After getCache, manager stats: " + instance.getStats() );
        }

        return cache;
    }

    /**
     * Disposes the cache with the given name, if found in the set of managed
     * caches.
     *
     * @param cacheName Name of cache to dispose.
     */
    public void freeCache( String cacheName )
    {
        ICache cache = ( ICache ) caches.get( cacheName );

        if ( cache != null )
        {
            try
            {
                cache.dispose();
            }
            catch ( Exception e )
            {
                log.error( "Failure disposing cache: " + cacheName, e );
            }
        }
    }

    /**
     * Returns the stats of all the managed caches.
     *
     * @return String of stats from each managed cache, seperated by commas.
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
     * Gets the cacheType attribute of the DiskCacheManager object
     *
     * @return The cacheType value
     */
    public int getCacheType()
    {
        return DISK_CACHE;
    }

    /**
     * Releases the cache manager instance. When all clients have released the
     * cache manager, all contained caches will be disposed.
     */
    public void release()
    {
        clients--;

        if ( --clients != 0 )
        {
            return;
        }

        synchronized ( caches )
        {
            Enumeration allCaches = caches.elements();

            while ( allCaches.hasMoreElements() )
            {
                ICache cache = ( ICache ) allCaches.nextElement();

                if ( cache != null )
                {
                    try
                    {
                        cache.dispose();
                    }
                    catch ( Exception e )
                    {
                        log.error( "Failure disposing cache: " +
                            cache.getCacheName(), e );
                    }
                }
            }
        }
    }
}
