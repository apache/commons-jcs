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

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.bdbje.behavior.IBDBJECacheAttributes;

/**
 *  Attributes for Berkeley DB JE disk cache auxiliary.
 */
public class BDBJECacheAttributes
    implements AuxiliaryCacheAttributes, IBDBJECacheAttributes
{

  private String cacheName;
  private String name;

  private String diskPath;

  private long cacheSize = -1;
  private int cachePercent = -1;

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setCacheName(java.lang.String)
   */
  public void setCacheName( String s )
  {
    cacheName = s;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getCacheName()
   */
  public String getCacheName()
  {
    return cacheName;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setName(java.lang.String)
   */
  public void setName( String s )
  {
    name = s;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#copy()
   */
  public AuxiliaryCacheAttributes copy()
  {
    try
    {
      return ( AuxiliaryCacheAttributes )this.clone();
    }
    catch ( Exception e )
    {
    }
    return this;
  }

  /**
   * Sets the diskPath attribute of  CacheAttributes object
   *
   * @param path The new diskPath value
   */
  public void setDiskPath( String path )
  {
    this.diskPath = path.trim();
  }

  /**
   * Gets the diskPath attribute of the  CacheAttributes object
   *
   * @return The diskPath value
   */
  public String getDiskPath()
  {
    return this.diskPath;
  }

  /**
   * Gets the CacheSize attribute of the  CacheAttributes object.
   * If this is not set in the cache.ccf, the default wil be used unless
   * you provide a je.properties file.
   *
   * @return The CacheSize value
   */
  public long getCacheSize()
  {
    return this.cacheSize;
  }

  /**
   * Sets the cacheSize attribute of  CacheAttributes object.
   * The minimum acceptable size is 1024.  Anything less will
   * automatically be increased to 1024.
   *
   * @param path The new cacheSize value
   */
  public void setCacheSize( long size) {
    if ( size < 1024 ) {
      size = 1024;
    }
    cacheSize = size;
  }

  /**
   * Sets the cacheSize attribute of  CacheAttributes object.
   * The minimum acceptable size is 0.  Anything less will
   * automatically be increased to 0.
   * The maximum acceptable size is 100.  Anything more will
   * automatically be decreased to 100.
   *
   * @param path The new cachePercent value
   */
   public void setCachePercent( int perc ) {
     if ( perc < 0 ) {
       perc = 0;
     }
     if ( perc > 100 ) {
       perc = 100;
     }
     cachePercent = perc;
   }

   /**
    * Gets the CachePercent attribute of the  CacheAttributes object.
    * If this is not set in the cache.ccf, the default will be used unless
    * you provide a je.properties file.  JE defaults to 93%.
    *
    * @return The CachePercent value
    */
   public int getCachePercent()
   {
     return this.cachePercent;
   }

}
