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

/**
 *  Attributes for Berkeley DB JE disk cache auxiliary.
 */
public class BDBJECacheAttributes
    implements AuxiliaryCacheAttributes
{

  private String cacheName;
  private String name;

  private String diskPath;

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

}
