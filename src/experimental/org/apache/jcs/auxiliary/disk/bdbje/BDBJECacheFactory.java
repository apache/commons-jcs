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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.AuxiliaryCacheFactory;
import org.apache.jcs.engine.control.CompositeCache;

/**
 *  This factory creates Berkeley DB JE disk cache auxiliaries.
 */
public class BDBJECacheFactory
    implements AuxiliaryCacheFactory
{

  private final static Log log =
      LogFactory.getLog( BDBJECacheFactory.class );

  private String name;

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#createCache(org.apache.jcs.auxiliary.AuxiliaryCacheAttributes, org.apache.jcs.engine.control.CompositeCache)
   */
  public AuxiliaryCache createCache(
      AuxiliaryCacheAttributes attr,
      CompositeCache cache )
  {
    BDBJECacheAttributes jeattr = ( BDBJECacheAttributes ) attr;
    BDBJECacheManager jecm = BDBJECacheManager.getInstance( jeattr );
    return jecm.getCache( jeattr );
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#setName(java.lang.String)
   */
  public void setName( String s )
  {
    this.name = s;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCacheFactory#getName()
   */
  public String getName()
  {
    return this.name;
  }

}