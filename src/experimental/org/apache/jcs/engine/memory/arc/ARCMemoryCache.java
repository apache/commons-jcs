package org.apache.jcs.engine.memory.arc;

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

import java.util.LinkedList;
import java.io.IOException;
import java.io.Serializable;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.memory.AbstractMemoryCache;

/**
 *  This is a rough implmentation of an adaptive replacement cache.
 *  ARC is a hybrid LFU / LRU that adapts to user behavior.
 *
 *  @see  http://www.almaden.ibm.com/StorageSystems/autonomic_storage/ARC/index.shtml
 *  TODO implement custom linked lists to cut down on removal overhead.
 */
public class ARCMemoryCache
    extends AbstractMemoryCache
{

  private final static Log log =
      LogFactory.getLog( ARCMemoryCache.class );

  int[] loc = new int[0];

  // maximum size
  int c = 0;

  LinkedList T1 = new LinkedList();
  LinkedList T2 = new LinkedList();
  LinkedList B1 = new LinkedList();
  LinkedList B2 = new LinkedList();

  private static final int _T1_ = 1;
  private static final int _T2_ = 2;
  private static final int _B1_ = 3;
  private static final int _B2_ = 4;

  // ideal size of T1
  int target_T1 = 0;

  ConcurrentHashMap map = new ConcurrentHashMap();

  int cnt = 0;

  int hitCnt = 0;
  int missCnt = 0;
  int putCnt = 0;

  public ARCMemoryCache()
  {

    log.warn( "test" );
    log.debug( "Loading Arc" );
  }

  public Object[] getKeyArray()
  {
    return null;
  }

  public ICacheElement getQuiet( Serializable key ) throws IOException
  {
    return get( key );
  }

  /**
   *  For post reflection creation initialization
   *
   *@param  hub
   */
  public synchronized void initialize( CompositeCache hub )
  {
    super.initialize( hub );
    c = this.cattr.getMaxObjects(); // / 2;
    target_T1 = c;
    log.info( "initialized LRUMemoryCache for " + cacheName );
  }

  public ICacheElement get( Serializable key ) throws IOException
  {
    CacheElement ce = new CacheElement( cacheName,
                                        ( Serializable ) key,
                                        null );
    return ARC( ce, true );
  }

  public void update( ICacheElement ce )
  {
    ARC( ce, false );
  }

  public ICacheElement ARC( ICacheElement ce, boolean isGet )
  {

    cnt++;

    if ( cnt % 10000 == 0 )
    //if ( true )
    {
      if ( log.isInfoEnabled() )
      {
        StringBuffer buf = new StringBuffer();
        buf.append( "\n ce.key() = " + ce.getKey() );
        buf.append( "\n isGet = " + isGet );
        buf.append( getStats() );
        log.info( buf.toString() );
      }
    }

    if ( !isGet )
    {
      putCnt++;
    }

    ElementDescriptor temp = ( ElementDescriptor ) map.get( ce.getKey() );

    if ( temp != null )
    {

      if ( isGet )
      {
        hitCnt++;
      }

      switch ( temp.listNum )
      {
        case _T1_:
          if ( log.isDebugEnabled() )
          {
            log.debug( "T1" );
          }

          log.debug( "T1 before remove = " + T1.size() );
          boolean stat1 = T1.remove( temp ); // need to implement our own list
          log.debug( "T1 after remove = " + T1.size() + " stat = " + stat1 );

          temp.listNum = _T2_;
          T2.addFirst( temp );
          break;

        case _T2_:
          if ( log.isDebugEnabled() )
          {
            log.debug( "T2" );
          }

          log.debug( "T2 before remove = " + T2.size() );
          boolean stat2 = T2.remove( temp ); // need to implement our own list
          log.debug( "T2 after remove = " + T2.size() + " stat = " + stat2 );

          temp.listNum = _T2_;
          T2.addFirst( temp );
          break;

        case _B1_:

          // B1 hit: favor recency
          if ( log.isDebugEnabled() )
          {
            log.debug( "B1" );
          }

          // adapt the target size
          target_T1 = Math.min( target_T1 + Math.max( B2.size() / B1.size(), 1 ),
                                c );

          if ( !isGet )
          {
            if ( log.isDebugEnabled() )
            {
              log.debug( "B1 before remove = " + B1.size() );
            }
            boolean stat3 = B1.remove( temp ); // need to implement our own list
            if ( log.isDebugEnabled() )
            {
              log.debug( "B1 after remove = " + B1.size() + " stat = " + stat3 );
            }
            //replace(temp);
            temp.listNum = _T2_;
            temp.ce = ce;
            T2.addFirst( temp ); // seen twice recently, put on T2

          }
          else
          {
            // if this is just a get, then adjust the cache
            // it is essentially a miss.
            temp = null;
            hitCnt--;
            missCnt++;
          }
          break;

        case _B2_:

          // B2 hit: favor frequency
          if ( log.isDebugEnabled() )
          {
            log.debug( "B2" );
          }

          // adapt the target size
          target_T1 = Math.max( target_T1 - Math.max( B1.size() / B2.size(), 1 ),
                                0 );
          if ( !isGet )
          {
            if ( log.isDebugEnabled() )
            {
              log.debug( "B2 before remove = " + B2.size() );
            }

            boolean stat4 = B2.remove( temp ); // need to implement our own list
            if ( log.isDebugEnabled() )
            {
              log.debug( "B2 after remove = " + B2.size() + " stat = " + stat4 );
            }

            //replace(temp);
            temp.listNum = _T2_;
            temp.ce = ce;
            T2.addFirst( temp ); // seen twice recently, put on T2
          }
          else
          {
            // if this is just a get, then adjust the cache
            // it is essentially a miss.
            temp = null;
            hitCnt--;
            missCnt++;
          }
          break;
      }

//////////////////////////////////////////////////////////////////////////////
      // was null
    }
    else
    {
    /* page is not in cache  */

      if ( isGet )
      {
        missCnt++;
      }

      if ( log.isDebugEnabled() )
      {
        log.debug( "Page is not in cache" );
      }

      if ( T1.size() + B1.size() >= c )
      {
        /* B1 + T1 full? */
        if ( T1.size() < c )
        {
          /* Still room in T1? */
          temp = ( ElementDescriptor ) B1.removeLast();
          map.remove( temp.key );
          /* yes: take page off B1 */
          //temp->pointer = replace(); /* find new place to put page */
          replace( temp );
        }
        else
        {
          /* no: B1 must be empty */
          temp = ( ElementDescriptor ) T1.removeLast(); /* take page off T1 */
          map.remove( temp.key );
          //if (temp->dirty) destage(temp); /* if dirty, evict before overwrite */
        }
      }
      else
      {
        /* B1 + T1 have less than c pages */
        if ( T1.size() + T2.size() + B1.size() + B2.size() >= c )
        {
          /* cache full? */
          /* Yes, cache full: */
          if ( T1.size() + T2.size() + B1.size() + B2.size() >= 2 * c )
          {
            /* directory is full: */
            /* x find and reuse B2’s LRU */
            temp = ( ElementDescriptor ) B2.removeLast();
            map.remove( temp.key );
          }
          else
          {
            /* cache directory not full, easy case */
            ; //nop
          }
          replace( temp );
        }
        else
        {
          /* cache not full, easy case */
          ; //nop
        }
      }

      if ( !isGet )
      {
        temp = new ElementDescriptor( ce.getKey() );
        temp.ce = ce;
        temp.listNum = _T1_;
        T1.addFirst( temp ); /* seen once recently, put on T1 */
        this.map.put( temp.key, temp );
      }

    } // end if not in cache

    if ( temp == null )
    {
      return null;
    }
    return temp.ce;
  }

  /**
   * This method doesn't so much replace as remove.  It pushes the least
   * recently used in t1 or t2 to a dummy holder. The holder keeps a dummy object
   * that stores the key so that subsequent gets and puts can help train the
   * cache.  Items are spooled if there is a disk cache at this point.
   *
   * @param orig ElementDescriptor
   */
  public void replace( ElementDescriptor orig )
  {
    try
    {
      ElementDescriptor temp;
      if ( T1.size() >= Math.max( 1, target_T1 ) )
      { // T1’s size exceeds target?
        // yes: T1 is too big
        temp = ( ElementDescriptor ) T1.getLast();
        if ( orig == null || !orig.equals( temp ) )
        {
          if ( log.isDebugEnabled() )
          {
            log.debug( "replace -- T1 to B1" );
            log.debug( getInfo() );
          }
          temp = ( ElementDescriptor ) T1.removeLast(); // grab LRU from T1
          // nullify object, temp is now just a dummy container to help
          // adjust the lru size
          try
          {
            this.waterfal( temp.ce );
          }
          catch ( Exception e )
          {
            log.error( e );
          }
          temp.ce = null;
          temp.listNum = _B1_; // note that fact
          B1.addFirst( temp ); // put it on B1
          //T1Length—; B1Length++; // bookkeep
        }
        else
        {
          if ( log.isDebugEnabled() )
          {
            log.debug( "orig == temp, t1" );
          }
        }
      }
      else
      // if t2 is greater than or equal to what is left in c after the target
      //if (T2.size() >= (c - target_T1) )
      {

        // no: T1 is not too big
        temp = ( ElementDescriptor ) T2.getLast();
        if ( orig == null || !orig.equals( temp ) )
        {
          if ( log.isDebugEnabled() )
          {
            log.debug( "replace -- T2 to B2" );
            log.debug( getInfo() );
          }

          temp = ( ElementDescriptor ) T2.removeLast(); // grab LRU page of T2
          // nullify object, temp is now just a dummy container to help
          // adjust the lru size
          try
          {
            this.waterfal( temp.ce );
          }
          catch ( Exception e )
          {
            log.error( e );
          }
          temp.ce = null;
          temp.listNum = _B2_; // note that fact
          B2.addFirst( temp ); // put it on B2
          //T2Length—; B2Length++; // bookkeep
        }
        else
        {
          if ( log.isDebugEnabled() )
          {
            log.debug( "orig == temp, t2" );
          }
        }
      }
    }
    catch ( Exception e )
    {
      log.error( e );
    }
  }

  /**
   * remove
   *
   * @param key Serializable
   * @return boolean
   */
  public boolean remove( Serializable key )
  {
    ElementDescriptor temp = ( ElementDescriptor ) map.remove( key );
    int loc = temp.listNum;
    if ( loc == _T1_ )
    {
      T1.remove( temp );
    }
    else
    if ( loc == _T2_ )
    {
      T2.remove( temp );
    }
    else
    if ( loc == _B1_ )
    {
      B1.remove( temp );
    }
    else
    if ( loc == _B2_ )
    {
      B2.remove( temp );
    }
    return true;
  }

  /////////////////////////////////////////////////////////////////////////
  public String getStats()
  {
    StringBuffer buf = new StringBuffer( 0 );
    buf.append( getInfo() );
    buf.append( getCnts() );
    return buf.toString();
  }

  public String getCnts()
  {
    StringBuffer buf = new StringBuffer( 0 );
    buf.append( "\n putCnt = " + putCnt );
    buf.append( "\n hitCnt = " + hitCnt );
    buf.append( "\n missCnt = " + missCnt );
    if ( hitCnt != 0 )
    {
      // int rate = ((hitCnt + missCnt) * 100) / (hitCnt * 100) * 100;
      //buf.append("\n Hit Rate = " + rate + " %" );
    }
    return buf.toString();
  }

  public String getInfo()
  {
    StringBuffer buf = new StringBuffer();
    buf.append( "\n T1.size() = " + T1.size() );
    buf.append( "\n T2.size() = " + T2.size() );
    buf.append( "\n B1.size() = " + B1.size() );
    buf.append( "\n B2.size() = " + B2.size() );
    buf.append( "\n target_T1 = " + target_T1 );
    buf.append( "\n map.size() = " + map.size() );
    return buf.toString();
  }

/////////////////////////////////////////////////
  public class ElementDescriptor
  {
    public int listNum;
    public ICacheElement ce;
    public Serializable key;

    public ElementDescriptor( Serializable k )
    {
      key = k;
    }

    //public boolean equals(ElementDescriptor ed)
    //{
    //  //ce.getKey().equals(ed.ce.getKey());
    //  return key.equals(ed.ce.getKey());
    //}
  }

}
