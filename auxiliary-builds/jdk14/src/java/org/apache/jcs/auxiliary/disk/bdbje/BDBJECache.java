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

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.disk.AbstractDiskCache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.auxiliary.disk.behavior.IDiskCacheAttributes;

/**
 *  One BDBJECache per regions.  For now they share one underlying Berekeley DB.
 */
public class BDBJECache
    extends AbstractDiskCache
{

  private final static Log log =
      LogFactory.getLog( BDBJECache.class );

  /*  right now we are using one berkely db for all regions */
  private BDBJE je;

  public BDBJECache( BDBJECacheAttributes attr )
  {
    super( (IDiskCacheAttributes)attr );
    this.je = new BDBJE( attr );
    if ( log.isDebugEnabled() )
    {
      log.debug( "constructed BDBJECache" );
    }
    // Initialization finished successfully, so set alive to true.
    alive = true;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.AuxiliaryCache#getGroupKeys(java.lang.String)
   */
  public Set getGroupKeys( String groupName )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.engine.behavior.ICache#getSize()
   */
  public int getSize()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doGet(java.io.Serializable)
   */
  protected ICacheElement doGet( Serializable key )
  {
    if ( log.isDebugEnabled() )
    {
      log.debug( "doGet, key '" + key + "'" );
    }
    ICacheElement retVal = null;
    try
    {
      retVal = je.get( key );
    }
    catch ( Exception e )
    {
      log.error( e );
    }
    return retVal;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doUpdate(org.apache.jcs.engine.behavior.ICacheElement)
   */
  protected void doUpdate( ICacheElement element )
  {
    if ( log.isDebugEnabled() )
    {
      log.debug( "doUpdate, key '" + element.getKey() + "'" );
    }
    try
    {
      je.update( element );
    }
    catch ( Exception e )
    {
      log.error( e );
    }
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doRemove(java.io.Serializable)
   */
  protected boolean doRemove( Serializable key )
  {
    if ( log.isDebugEnabled() )
    {
      log.debug( "doRemove, key '" + key + "'" );
    }
    try
    {
      je.remove( key );
    }
    catch ( Exception e )
    {
      log.error( e );
      return false;
    }
    return true;
  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doRemoveAll()
   */
  protected void doRemoveAll()
  {
    if ( log.isDebugEnabled() )
    {
      log.debug( "doRemoveAll" );
    }
    try
    {
      je.removeAll();
    }
    catch ( Exception e )
    {
      log.error( e );
    }

  }

  /* (non-Javadoc)
   * @see org.apache.jcs.auxiliary.disk.AbstractDiskCache#doDispose()
   */
  protected void doDispose()
  {
    je.dispose();
  }

}
