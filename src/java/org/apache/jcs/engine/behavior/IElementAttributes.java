package org.apache.jcs.engine.behavior;

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

import java.util.ArrayList;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 *  Inteface for cache element attributes classes.
 *
 */
public interface IElementAttributes
{

  /**
   *  Sets the version attribute of the IAttributes object
   *
   *@param  version  The new version value
   */
  public void setVersion( long version );

  /**
   *  Sets the maxLife attribute of the IAttributes object
   *
   *@param  mls  The new {3} value
   */
  public void setMaxLifeSeconds( long mls );

  /**
   *  Sets the maxLife attribute of the IAttributes object
   *
   *@return    The {3} value
   */
  public long getMaxLifeSeconds();

  /**
   *  Sets the idleTime attribute of the IAttributes object
   *
   *@param  idle  The new idleTime value
   */
  public void setIdleTime( long idle );

  //public void setListener( int event, CacheEventListener listerner) {}

  /**
   *  Size in bytes.
   *
   *@param  size  The new size value
   */
  public void setSize( int size );

  /**
   *  Gets the size attribute of the IAttributes object
   *
   *@return    The size value
   */
  public int getSize();

  /**
   *  Gets the createTime attribute of the IAttributes object
   *
   *@return    The createTime value
   */
  public long getCreateTime();

  /**
   *  Gets the LastAccess attribute of the IAttributes object
   *
   *@return    The LastAccess value
   */
  public long getLastAccessTime();

  /**
   *  Sets the LastAccessTime as now of the IElementAttributes object
   */
  public void setLastAccessTimeNow();

  /**
   *  Gets the version attribute of the IAttributes object
   *
   *@return    The version value
   */
  public long getVersion();

  /**
   *  Gets the idleTime attribute of the IAttributes object
   *
   *@return    The idleTime value
   */
  public long getIdleTime();

  /**
   *  Gets the time left to live of the IAttributes object
   *
   *@return    The t value
   */
  public long getTimeToLiveSeconds();

  /**
   *  Returns a copy of the object.
   *
   *@return    IElementAttributes
   */
  public IElementAttributes copy();

  /**
   *  Gets the {3} attribute of the IElementAttributes object
   *
   *@return    The {3} value
   */
  public boolean getIsDistribute();

  /**
   *  Sets the isDistribute attribute of the IElementAttributes object
   *
   *@param  val  The new isDistribute value
   */
  public void setIsDistribute( boolean val );

  // lateral

  /**
   *  can this item be flushed to disk
   *
   *@return    The {3} value
   */
  public boolean getIsSpool();

  /**
   *  Sets the isSpool attribute of the IElementAttributes object
   *
   *@param  val  The new isSpool value
   */
  public void setIsSpool( boolean val );

  /**
   *  Is this item laterally distributable
   *
   *@return    The {3} value
   */
  public boolean getIsLateral();

  /**
   *  Sets the isLateral attribute of the IElementAttributes object
   *
   *@param  val  The new isLateral value
   */
  public void setIsLateral( boolean val );

  /**
   *  Can this item be sent to the remote cache
   *
   *@return    The {3} value
   */
  public boolean getIsRemote();

  /**
   *  Sets the isRemote attribute of the IElementAttributes object
   *
   *@param  val  The new isRemote value
   */
  public void setIsRemote( boolean val );

  /**
   *  can turn off expiration
   *
   *@return    The {3} value
   */
  public boolean getIsEternal();

  /**
   *  Sets the isEternal attribute of the IElementAttributes object
   *
   *@param  val  The new isEternal value
   */
  public void setIsEternal( boolean val );

  /**
   *  Adds a ElementEventHandler. Handler's can be registered for multiple
   *  events. A registered handler will be called at every recognized event.
   *
   *@param  eventHandler  The feature to be added to the ElementEventHandler
   */
  public void addElementEventHandler( IElementEventHandler eventHandler );

  /**
   *  Gets the elementEventHandlers.
   *
   *@return    The elementEventHandlers value
   */


  /**
   *  Sets the eventHandlers of the IElementAttributes object
   *
   *@param eventHandlers value
   */
  public void addElementEventHandlers( ArrayList eventHandlers );

  public ArrayList getElementEventHandlers();

}
