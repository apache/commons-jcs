package org.apache.jcs.engine;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 *  Element attribute descriptor class.
 *
 *@version    $Id: ILateralCacheTCPListener.java,v 1.2 2002/01/18 22:08:26
 */
public class ElementAttributes
    implements IElementAttributes, Serializable, Cloneable
{

  /**
   *  Is this item distributable at all.
   */
  public boolean IS_DISTRIBUTE = true;

  // lateral

  /**
   *  can this item be flushed to disk
   */
  public boolean IS_SPOOL = true;

  /**
   *  Is this item laterally distributable
   */
  public boolean IS_LATERAL = true;

  /**
   *  Can this item be sent to the remote cache
   */
  public boolean IS_REMOTE = true;

  /**
   *  can turn off expiration
   */
  public boolean IS_ETERNAL = true;

  /**
   *  Description of the Field
   */
  public long version = 0;

  /**
   *  Max life seconds
   */
  public long mls = -1;

  /**
   *  Description of the Field
   */
  public long idle = -1;

  /**
   *  The byte size of teh field. Must be manually set.
   */
  public int size = 0;

  /**
   *  The creation time
   */
  public long createTime = 0;

  /**
   *  The last access time
   */
  public long lastAccessTime = 0;

  /**
   *  The last access time
   */
  public ArrayList eventHandlers;

  /**
   *  Constructor for the IElementAttributes object
   */
  public ElementAttributes()
  {
    this.createTime = System.currentTimeMillis();
    this.lastAccessTime = this.createTime;
  }

  /**
   *  Constructor for the IElementAttributes object
   *
   *@param  attr
   */
  private ElementAttributes( ElementAttributes attr )
  {

    IS_ETERNAL = attr.IS_ETERNAL;

    // waterfal onto disk, for pure disk set memory to 0
    IS_SPOOL = attr.IS_SPOOL;

    IS_DISTRIBUTE = attr.IS_DISTRIBUTE;

    // lateral
    IS_LATERAL = attr.IS_LATERAL;

    // central rmi store
    IS_REMOTE = attr.IS_REMOTE;

    mls = attr.mls;
    // timetolive
    idle = attr.idle;
    size = attr.size;

  }

  /**
   *  Description of the Method
   *
   *@return
   */
  public IElementAttributes copy()
  {
    try
    {
//            ElementAttributes attr = ( ElementAttributes ) this.clone();
//            attr.createTime = System.currentTimeMillis();
//            attr.setLastAccessTimeNow();
//            return attr;

      // need to make this more efficient.  Just want to insure
      // a proper copy
      ElementAttributes attr = new ElementAttributes();
      attr.setIdleTime( this.getIdleTime() );
      attr.setIsEternal( this.getIsEternal() );
      attr.setIsDistribute( this.getIsDistribute() );
      attr.setIsLateral( this.getIsLateral() );
      attr.setIsRemote( this.getIsRemote() );
      attr.setIsSpool( this.getIsSpool() );
      attr.setMaxLifeSeconds( this.getMaxLifeSeconds() );
      attr.addElementEventHandlers( this.eventHandlers );
      return attr;
    }
    catch ( Exception e )
    {
      return new ElementAttributes();
    }
  }

  /**
   *  Description of the Method
   *
   * @return a clone of these attributes
   */
  public Object clone2()
  {

    try
    {
      ByteArrayOutputStream baos =
          new ByteArrayOutputStream( 100 );
      ObjectOutputStream oos = new
          ObjectOutputStream( baos );
      oos.writeObject( this );
      byte buf[] = baos.toByteArray();
      oos.close();

      // deserialize byte array into ArrayList

      ByteArrayInputStream bais =
          new ByteArrayInputStream( buf );
      ObjectInputStream ois = new
          ObjectInputStream( bais );
      ElementAttributes attr =
          ( ElementAttributes ) ois.readObject();
      ois.close();

      attr.createTime = System.currentTimeMillis();
      return attr;
    }
    catch ( Exception e )
    {
    }
    return null;
  }

  /**
   *  Sets the version attribute of the IElementAttributes object
   *
   *@param  version  The new version value
   */
  public void setVersion( long version )
  {
    this.version = version;
  }

  /**
   *  Sets the maxLifeSeconds attribute of the IElementAttributes object
   *
   *@param  mls  The new {3} value
   */
  public void setMaxLifeSeconds( long mls )
  {
    this.mls = mls;
  }

  /**
   *  Gets the {3} attribute of the ElementAttributes object
   *
   *@return    The {3} value
   */
  public long getMaxLifeSeconds()
  {
    return this.mls;
  }

  /**
   *  Sets the idleTime attribute of the IElementAttributes object
   *
   *@param  idle  The new idleTime value
   */
  public void setIdleTime( long idle )
  {
    this.idle = idle;
  }

  //public void setListener( int event, CacheEventListener listerner) {}

  /**
   *  Size in bytes.
   *
   *@param  size  The new size value
   */
  public void setSize( int size )
  {
    this.size = size;
  }

  /**
   *  Gets the size attribute of the IElementAttributes object
   *
   *@return    The size value
   */
  public int getSize()
  {
    return size;
  }

  /**
   *  Gets the createTime attribute of the IElementAttributes object
   *
   *@return    The createTime value
   */
  public long getCreateTime()
  {
    return createTime;
  }

  /**
   *  Sets the createTime attribute of the IElementAttributes object
   */
  public void setCreateTime()
  {
    createTime = System.currentTimeMillis();
  }

  /**
   *  Gets the version attribute of the IElementAttributes object
   *
   *@return    The version value
   */
  public long getVersion()
  {
    return version;
  }

  /**
   *  Gets the idleTime attribute of the IElementAttributes object. Keeping
   *  track of this will require storing the last access time. This could get
   *  expensive.
   *
   *@return    The idleTime value
   */
  public long getIdleTime()
  {
    return this.idle;
  }

  /**
   *  If the returned value is negative, the item has expired
   *
   *@return    The timeToLive value
   */
  public long getTimeToLiveSeconds()
  {
    long now = System.currentTimeMillis();
    return ( this.getCreateTime() + ( this.getMaxLifeSeconds() * 1000 ) ) - now;
  }

  /**
   *  Gets the LastAccess attribute of the IAttributes object
   *
   *@return    The LastAccess value
   */
  public long getLastAccessTime()
  {
    return this.lastAccessTime;
  }

  /**
   *  Sets the LastAccessTime as now of the IElementAttributes object
   */
  public void setLastAccessTimeNow()
  {
    this.lastAccessTime = System.currentTimeMillis();
  }

  /**
   *  Gets the {3} attribute of the IElementAttributes object
   *
   *@return    The {3} value
   */
  public boolean getIsDistribute()
  {
    return this.IS_DISTRIBUTE;
  }

  /**
   *  Sets the isDistribute attribute of the ElementAttributes object
   *
   *@param  val  The new isDistribute value
   */
  public void setIsDistribute( boolean val )
  {
    this.IS_DISTRIBUTE = val;
  }

  /**
   *  can this item be flushed to disk
   *
   *@return    The {3} value
   */
  public boolean getIsSpool()
  {
    return this.IS_SPOOL;
  }

  /**
   *  Sets the isSpool attribute of the ElementAttributes object
   *
   *@param  val  The new isSpool value
   */
  public void setIsSpool( boolean val )
  {
    this.IS_SPOOL = val;
  }

  /**
   *  Is this item laterally distributable
   *
   *@return    The {3} value
   */
  public boolean getIsLateral()
  {
    return this.IS_LATERAL;
  }

  /**
   *  Sets the isLateral attribute of the ElementAttributes object
   *
   *@param  val  The new isLateral value
   */
  public void setIsLateral( boolean val )
  {
    this.IS_LATERAL = val;
  }

  /**
   *  Can this item be sent to the remote cache
   *
   *@return    The {3} value
   */
  public boolean getIsRemote()
  {
    return this.IS_REMOTE;
  }

  /**
   *  Sets the isRemote attribute of the ElementAttributes object
   *
   *@param  val  The new isRemote value
   */
  public void setIsRemote( boolean val )
  {
    this.IS_REMOTE = val;
  }

  /**
   *  can turn off expiration
   *
   *@return    The {3} value
   */
  public boolean getIsEternal()
  {
    return this.IS_ETERNAL;
  }

  /**
   *  Sets the isEternal attribute of the ElementAttributes object
   *
   *@param  val  The new isEternal value
   */
  public void setIsEternal( boolean val )
  {
    this.IS_ETERNAL = val;
  }

  /**
   *  Adds a ElementEventHandler. Handler's can be registered for multiple
   *  events. A registered handler will be called at every recognized event.
   *
   * The alternative would be to register handlers for each event.  Or maybe
   * The handler interface should ahve a method to return whether it cares
   * about certain events.
   *
   *@param  eventHandler  The ElementEventHandler to be added to the list.
   */
  public void addElementEventHandler( IElementEventHandler eventHandler )
  {
    // lazy here, no concurrency problems expected
    if ( this.eventHandlers == null )
    {
      this.eventHandlers = new ArrayList();
    }
    this.eventHandlers.add( eventHandler );
  }

  /**
   *  Sets the eventHandlers of the IElementAttributes object
   *
   *@param  eventHandlers value
   */
  public void addElementEventHandlers( ArrayList eventHandlers )
  {
    if ( eventHandlers == null )
    {
      return;
    }

    for ( Iterator iter = eventHandlers.iterator(); iter.hasNext(); )
    {
      addElementEventHandler( ( IElementEventHandler ) iter.next() );
    }
  }

  /**
   *  Gets the elementEventHandlers. Returns null if none exist. Makes
   *  checking easy.
   *
   *@return    The elementEventHandlers value
   */

  public ArrayList getElementEventHandlers()
  {
    return this.eventHandlers;
  }

  /**
   *  For logging and debugging the element IElementAttributes.
   *
   *@return
   */
  public String toString()
  {
    StringBuffer dump = new StringBuffer();

    dump.append( "[ IS_LATERAL = " ).append( IS_LATERAL )
        .append( ", IS_SPOOL = " ).append( IS_SPOOL )
        .append( ", IS_REMOTE = " ).append( IS_REMOTE )
        .append( ", IS_ETERNAL = " ).append( IS_ETERNAL )
        .append( ", MaxLifeSeconds = " ).append( this.getMaxLifeSeconds() )
        .append( ", IdleTime = " ).append( this.getIdleTime() )
        .append( ", CreateTime = " ).append( this.getCreateTime() )
        .append( ", LastAccessTime = " ).append( this.getLastAccessTime() )
        .append( ", getTimeToLiveSeconds() = " ).append( String.valueOf(
        getTimeToLiveSeconds() ) )
        .append( ", createTime = " ).append( String.valueOf( createTime ) )
        .append( " ]" );

    return dump.toString();
  }
}
