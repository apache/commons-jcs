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

import java.io.IOException;
import java.io.Serializable;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.memory.AbstractMemoryCache;
import org.apache.jcs.engine.memory.util.DoubleLinkedList;
import org.apache.jcs.engine.memory.util.MemoryElementDescriptor;

/**
 *  This is a rough implmentation of an adaptive replacement cache.
 *  ARC is a hybrid LFU / LRU that adapts to user behavior.
 *
 *  See the ARC method for more detail on how the algorithm works.
 *
 *  @see  http://www.almaden.ibm.com/StorageSystems/autonomic_storage/ARC/index.shtml
 */
public class ARCMemoryCache
    extends AbstractMemoryCache
{

  private final static Log log =
      LogFactory.getLog(ARCMemoryCache.class);

  int[] loc = new int[0];

  // maximum size
  int c = 0;

  DoubleLinkedList T1 = new DoubleLinkedList();
  DoubleLinkedList T2 = new DoubleLinkedList();
  DoubleLinkedList B1 = new DoubleLinkedList();
  DoubleLinkedList B2 = new DoubleLinkedList();

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
    log.debug("Loading Arc");
  }

  public Object[] getKeyArray()
  {
    return null;
  }

  public ICacheElement getQuiet(Serializable key) throws IOException
  {
    return get(key);
  }

  /**
   *  For post reflection creation initialization
   *
   *@param  hub
   */
  public synchronized void initialize(CompositeCache hub)
  {
    super.initialize(hub);
    c = this.cattr.getMaxObjects(); // / 2;
    target_T1 = c / 2;
    log.info("initialized LRUMemoryCache for " + cacheName);
  }

  public ICacheElement get(Serializable key) throws IOException
  {
    CacheElement ce = new CacheElement(cacheName,
                                       (Serializable) key,
                                       null);

    ICacheElement ice = null;
    try {
      ice = ARC(ce, true);
    } catch( Exception e ) {
      log.error( e );
    }
    return ice;
  }

  public void update(ICacheElement ce)
  {
    try {
      ARC(ce, false);
    } catch( Exception e ) {
      log.error( e );
    }
  }

  /**
   * This is the primary method for the ARC.  It handles both puts and gets.
   * The ARC has 4 linked lists: T1, T2, B1, and B2.  The 'T' lists are tops
   * and the 'B' lists are bottoms.  Bottom lists do not hold object, only keys.
   *
   * The T1 list is an LRU (Least Recently Used) list.  The T2 list is a near
   * LFU (Least Frequently Used) list.
   *
   * After items are removed from T1 and T2, their keys are stored in B1 and B2.
   * The number of keys in B1 and B2 is restricted to the number of max items.
   *
   * When there is a put or a get for an item whose key exists on one of the
   * bottom lists, the maximum number of items in T1 is adjusted.  If the item
   * was found on B2 (the bottom LFU list) the maximum allowed in T1 (the top
   * LRU list) is reduced.  If the item is found in B1 list (the bottom LRU) the
   * maximum allowed in T1 is increased.
   *
   * The maximum allowed in T1 will not exceed the maxSize.  The maximum in T1
   * and T2 combined will not exceed the maxSize.  The maximum number of elements
   * and keys allowed in all 4 lists will not exceed twice the maximum size.
   *
   * @param ce ICacheElement
   * @param isGet boolean
   * @return ICacheElement
   */
  public ICacheElement ARC(ICacheElement ce, boolean isGet)
  {

    cnt++;

    if (cnt % 10000 == 0)
    //if ( true )
    {
      if (log.isInfoEnabled())
      {
        StringBuffer buf = new StringBuffer();
        buf.append("\n ce.key() = " + ce.getKey());
        buf.append("\n isGet = " + isGet);
        buf.append(getStats());
        log.info(buf.toString());
      }
    }

    if (!isGet)
    {
      putCnt++;
    }

    ElementDescriptor temp = (ElementDescriptor) map.get(ce.getKey());
    boolean isHit = true;

    if (temp != null)
    {

      if (isGet)
      {
        hitCnt++;
      }

      switch (temp.listNum)
      {
        case _T1_:
          if (log.isDebugEnabled())
          {
            log.debug("T1");
          }

          log.debug("T1 to T2, before remove = " + T1.size());
          boolean stat1 = T1.remove(temp); // need to implement our own list
          log.debug("T1 to T2, after remove = " + T1.size() + " stat = " + stat1);

          temp.listNum = _T2_;
          T2.addFirst(temp);
          break;

        case _T2_:
          if (log.isDebugEnabled())
          {
            log.debug("T2");
          }

          temp.listNum = _T2_;
          T2.makeFirst(temp);
          break;

        case _B1_:

          // B1 hit: favor recency

          // adapt the target size
          target_T1 = Math.min(target_T1 + Math.max(B2.size() / B1.size(), 1),
                               c);
          if (log.isDebugEnabled())
          {
            log.debug("B1, targetT1 = " + target_T1 );
          }

          if (!isGet)
          {
            if (log.isDebugEnabled())
            {
              log.debug("B1 before remove = " + B1.size());
            }
            boolean stat3 = B1.remove(temp); // need to implement our own list
            if (log.isDebugEnabled())
            {
              log.debug("B1 after remove = " + B1.size() + " stat = " + stat3);
            }
            replace(temp);
            temp.listNum = _T2_;
            temp.ce = ce;
            T2.addFirst(temp); // seen twice recently, put on T2

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

          // adapt the target size
          target_T1 = Math.max(target_T1 - Math.max(B1.size() / B2.size(), 1),
                               0);
          if (log.isDebugEnabled())
          {
            log.debug("B2, targetT1 = " + target_T1 );
          }

          if (!isGet)
          {
            if (log.isDebugEnabled())
            {
              log.debug("B2 before remove = " + B2.size());
            }
            boolean stat4 = B2.remove(temp); // need to implement our own list
            if (log.isDebugEnabled())
            {
              log.debug("B2 after remove = " + B2.size() + " stat = " + stat4);
            }

            replace(temp);
            temp.listNum = _T2_;
            temp.ce = ce;
            T2.addFirst(temp); // seen twice recently, put on T2

            replace(temp);
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

      // was null
    }
    else
    {
      /* page is not in cache  */

      isHit = false;
      if (isGet)
      {
        missCnt++;
      }

      if (log.isDebugEnabled())
      {
        log.debug("Page is not in cache");
      }

    } // end if not in cache

    //////////////////////////////////////////////////////////////////////////////
    // Do some size Checks if this is a put
    //if (!isGet)
    //{
      if (T1.size() + B1.size() >= c)
      {
        /* B1 + T1 full? */
        if (T1.size() < c)
        {
          /* Still room in T1? */
          temp = (ElementDescriptor) B1.removeLast();
          if ( temp != null ) {
            map.remove(temp.key);
          }
          /* yes: take page off B1 */
          //temp->pointer = replace(); /* find new place to put page */
          replace(temp);
        }
        else
        {
          /* no: B1 must be empty */
          //temp = (ElementDescriptor) T1.removeLast(); /* take page off T1 */
          //map.remove(temp.ce.getKey());
          //if (temp->dirty) destage(temp); /* if dirty, evict before overwrite */
          replace(temp);
        }
      }
      else
      {
        /* B1 + T1 have less than c pages */
        if (T1.size() + T2.size() + B1.size() + B2.size() >= c)
        {
          /* cache full? */
          /* Yes, cache full: */
          if (T1.size() + T2.size() + B1.size() + B2.size() >= 2 * c)
          {
            /* cache is full: */
            /* x find and reuse B2’s LRU */
            temp = (ElementDescriptor) B2.removeLast();
            if ( temp != null ) {
              map.remove(temp.key);
            }
          }
          else
          {
            /* cache directory not full, easy case */
            ; //nop
          }
          replace(temp);
        }
        else
        {
          /* cache not full, easy case */
          ; //nop
        }
      }

      if (!isGet && !isHit)
      {
        temp = new ElementDescriptor(ce);
        temp.ce = ce;
        temp.listNum = _T1_;
        T1.addFirst(temp); /* seen once recently, put on T1 */
        this.map.put(temp.ce.getKey(), temp);
      } // end if put


    if (temp == null)
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
  public void replace(ElementDescriptor orig)
  {
    try
    {
      ElementDescriptor temp;
      if (T1.size() >= Math.max(1, target_T1))
      { // T1’s size exceeds target?
        // yes: T1 is too big
        temp = (ElementDescriptor) T1.getLast();
        if (orig == null || !orig.key.equals(temp.key))
        {
          if (log.isDebugEnabled())
          {
            log.debug("replace -- T1 to B1");
            log.debug(getInfo());
          }
          temp = (ElementDescriptor) T1.removeLast(); // grab LRU from T1
          // nullify object, temp is now just a dummy container to help
          // adjust the lru size
          try
          {
            this.waterfal(temp.ce);
          }
          catch (Exception e)
          {
            log.error(e);
          }
          temp.ce = null;
          temp.listNum = _B1_; // note that fact
          B1.addFirst(temp); // put it on B1
          //T1Length—; B1Length++; // bookkeep
        }
        else
        {
          if (log.isDebugEnabled())
          {
            log.debug("orig == temp, t1");
          }
        }
      }
      else
      // if t2 is greater than or equal to what is left in c after the target
      if ( ( T2.size() + T1.size() ) > c )
      {

        // no: T1 is not too big
        temp = (ElementDescriptor) T2.getLast();
        if (orig == null || !orig.key.equals(temp.key))
        {
          if (log.isDebugEnabled())
          {
            log.debug("replace -- T2 to B2");
            log.debug(getInfo());
          }

          temp = (ElementDescriptor) T2.removeLast(); // grab LRU page of T2
          // nullify object, temp is now just a dummy container to help
          // adjust the lru size
          try
          {
            this.waterfal(temp.ce);
          }
          catch (Exception e)
          {
            log.error(e);
          }
          temp.ce = null;
          temp.listNum = _B2_; // note that fact
          B2.addFirst(temp); // put it on B2
          //T2Length—; B2Length++; // bookkeep
        }
        else
        {
          if (log.isDebugEnabled())
          {
            log.debug("orig == temp, t2");
          }
        }
      }
    }
    catch (Exception e)
    {
      log.error(e);
    }
  }

  /**
   * remove
   *
   * @param key Serializable
   * @return boolean
   */
  public boolean remove(Serializable key)
  {
    ElementDescriptor temp = (ElementDescriptor) map.remove(key);
    if ( temp != null ) {
      int loc = temp.listNum;
      if (loc == _T1_)
      {
        T1.remove(temp);
      }
      else
      if (loc == _T2_)
      {
        T2.remove(temp);
      }
      else
      if (loc == _B1_)
      {
        B1.remove(temp);
      }
      else
      if (loc == _B2_)
      {
        B2.remove(temp);
      }
    }
    return true;
  }

  /**
   * Remove all of the elements from both the Map and the linked
   * list implementation. Overrides base class.
   */
  public synchronized void removeAll() throws IOException
  {
    map.clear();
    T1.removeAll();
    T2.removeAll();
    B1.removeAll();
    B2.removeAll();
  }

  /////////////////////////////////////////////////////////////////////////
  public String getStats()
  {
    StringBuffer buf = new StringBuffer(0);
    buf.append(getInfo());
    buf.append(getCnts());
    return buf.toString();
  }

  public String getCnts()
  {
    StringBuffer buf = new StringBuffer(0);
    buf.append("\n putCnt = " + putCnt);
    buf.append("\n hitCnt = " + hitCnt);
    buf.append("\n missCnt = " + missCnt);
    buf.append("\n -------------------------");
    if (hitCnt != 0)
    {
      // int rate = ((hitCnt + missCnt) * 100) / (hitCnt * 100) * 100;
      //buf.append("\n Hit Rate = " + rate + " %" );
    }
    return buf.toString();
  }

  public String getInfo()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("\n T1.size() = " + T1.size());
    buf.append("\n T2.size() = " + T2.size());
    buf.append("\n B1.size() = " + B1.size());
    buf.append("\n B2.size() = " + B2.size());
    buf.append("\n target_T1 = " + target_T1);
    buf.append("\n map.size() = " + map.size());
    buf.append("\n -------------------------");
    return buf.toString();
  }

/////////////////////////////////////////////////
  public class ElementDescriptor
      extends MemoryElementDescriptor
  {
    public int listNum;

    public Serializable key;

    public ElementDescriptor(ICacheElement ce)
    {
      super(ce);
      key = ce.getKey();
    }

  }

}
