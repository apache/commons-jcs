package org.apache.jcs.auxiliary.disk.bdbje;

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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheManager;

/**
 * This uses one berkely db for all regions.
 *
 */
public class BDBJECacheManager
    implements AuxiliaryCacheManager
{

  private final static Log log = LogFactory.getLog( BDBJECacheManager.class );

  private static int clients;

  private static BDBJECacheManager instance;

  private Hashtable caches = new Hashtable();

  private BDBJECacheAttributes defaultCacheAttributes;

  /*  right now use one berkely db for all regions */
  //private BDBJE je;

  /**
   * Constructor for the IndexedDiskCacheManager object
   *
   * @param defaultCacheAttributes Default attributes for caches managed by
   *                               the instance.
   */
  private BDBJECacheManager( BDBJECacheAttributes defaultCacheAttributes )
  {
    this.defaultCacheAttributes = defaultCacheAttributes;
    //je = new BDBJE( defaultCacheAttributes );
    if ( log.isDebugEnabled() )
    {
      log.debug( "Created JE" );
    }
  }

  /**
   * Gets the singleton instance of the manager
   *
   * @param defaultCacheAttributes If the instance has not yet been created,
   *                               it will be initialized with this set of
   *                               default attributes.
   * @return The instance value
   */
  public static BDBJECacheManager getInstance( BDBJECacheAttributes
                                               defaultCacheAttributes )
  {
    if ( instance == null )
    {
      synchronized ( BDBJECacheManager.class )
      {
        if ( instance == null )
        {
          instance = new BDBJECacheManager( defaultCacheAttributes );
        }
      }
    }

    clients++;

    return instance;
  }

  /**
   * Gets an IndexedDiskCache for the supplied name using the default
   * attributes.
   *
   * @see #getCache( IndexedDiskCacheAttributes }
   *
   * @param cacheName Name that will be used when creating attributes.
   * @return A cache.
   */
  public AuxiliaryCache getCache( String cacheName )
  {
    BDBJECacheAttributes cacheAttributes =
        ( BDBJECacheAttributes ) defaultCacheAttributes.copy();

    cacheAttributes.setCacheName( cacheName );

    return getCache( cacheAttributes );
  }

  /**
   * Get an IndexedDiskCache for the supplied attributes. Will provide an
   * existing cache for the name attribute if one has been created, or will
   * create a new cache.
   *
   * @param cacheAttributes Attributes the cache should have.
   * @return A cache, either from the existing set or newly created.
   *
   */
  public AuxiliaryCache getCache( BDBJECacheAttributes cacheAttributes )
  {
    AuxiliaryCache cache = null;

    String cacheName = cacheAttributes.getCacheName();

    log.debug( "Getting cache named: " + cacheName );

    synchronized ( caches )
    {
      // Try to load the cache from the set that have already been
      // created. This only looks at the name attribute.

      cache = ( AuxiliaryCache ) caches.get( cacheName );

      // If it was not found, create a new one using the supplied
      // attributes

      if ( cache == null )
      {
        cache = new BDBJECache( cacheAttributes );
        caches.put( cacheName, cache );
      }
    }
    if ( log.isDebugEnabled() )
    {
      log.debug( "returning cache = '" + cache + "'" );
    }
    return cache;
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

}