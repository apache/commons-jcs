package org.apache.commons.jcs.engine.memory.lru;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.engine.CacheConstants;
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.group.GroupAttrName;
import org.apache.commons.jcs.engine.control.group.GroupId;
import org.apache.commons.jcs.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *  This is a test memory manager using the jdk1.4 LinkedHashMap.
 *  There may be some thread safety issues.
 *  So far i cannot notice any performance difference between this and the
 *  standard LRU implementation.  It neeLogds more testing.
 *
 *@version    $Id$
 */
public class LHMLRUMemoryCache
    extends AbstractMemoryCache
{
  private static final Log log = LogFactory.getLog(LRUMemoryCache.class);

  // the extended LinkedHashMap
  private Map map;

  /**
   *  For post reflection creation initialization
   *
   *@param  hub
   */
  public synchronized void initialize(CompositeCache hub)
  {
    super.initialize(hub);

    map = new LHMSpooler();

    log.info("initialized LHMLRUMemoryCache for " + cacheName);
  }

  /**
   *  Puts an item to the cache.
   *
   *@param  ce               Description of the Parameter
   *@throws  IOException
   */
  public void update(ICacheElement<K, V> ce) throws IOException
  {
    // Asynchronisly create a MemoryElement
    ce.getElementAttributes().setLastAccessTimeNow();
    map.put(ce.getKey(), ce);
  }

  /**
   * Remove all of the elements from both the Map and the linked
   * list implementation. Overrides base class.
   */
  public synchronized void removeAll() throws IOException
  {
    map.clear();
  }

  /**
   *  Get an item from the cache without affecting its last access time or
   *  position.  There is no way to do this with the LinkedHashMap!
   *
   *@param  key              Identifies item to find
   *@return                  Element mathinh key if found, or null
   *@throws  IOException
   */
  public ICacheElement<K, V> getQuiet(K key) throws IOException
  {
    ICacheElement<K, V> ce = null;

    ce = (ICacheElement) map.get(key);

    if (ce != null)
    {
      if (log.isDebugEnabled())
      {
        log.debug(cacheName + ": LRUMemoryCache quiet hit for " + key);
      }

    }
    else if (log.isDebugEnabled())
    {
      log.debug(cacheName + ": LRUMemoryCache quiet miss for " + key);
    }

    return ce;
  }

  /**
   *  Get an item from the cache
   *
   *@param  key              Identifies item to find
   *@return                  ICacheElement<K, V> if found, else null
   *@throws  IOException
   */
  public synchronized ICacheElement<K, V> get(K key) throws IOException
  {
    ICacheElement<K, V> ce = null;

    if (log.isDebugEnabled())
    {
      log.debug("getting item from cache " + cacheName + " for key " +
                key);
    }

    ce = (ICacheElement) map.get(key);

    if (ce != null)
    {
      if (log.isDebugEnabled())
      {
        log.debug(cacheName + ": LRUMemoryCache hit for " + key);
      }

    }
    else
    {
      log.debug(cacheName + ": LRUMemoryCache miss for " + key);
    }

    return ce;
  }

  /**
   *  Removes an item from the cache. This method handles hierarchical
   *  removal. If the key is a String and ends with the
   *  CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
   *  starting with the argument String will be removed.
   *
   *@param  key
   *@return
   *@throws  IOException
   */
  public synchronized boolean remove(K key) throws IOException
  {
    if (log.isDebugEnabled())
    {
      log.debug("removing item for key: " + key);
    }

    boolean removed = false;

    // handle partial removal
    if (key instanceof String && ( (String) key)
        .endsWith(CacheConstants.NAME_COMPONENT_DELIMITER))
    {
      // remove all keys of the same name hierarchy.
      synchronized (map)
      {
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
          Map.Entry entry = (Map.Entry) itr.next();
          Object k = entry.getKey();

          if (k instanceof String
              && ( (String) k).startsWith(key.toString()))
          {
            itr.remove();

            removed = true;
          }
        }
      }
    }
    else if (key instanceof GroupId)
    {
      // remove all keys of the same name hierarchy.
      synchronized (map)
      {
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
          Map.Entry entry = (Map.Entry) itr.next();
          Object k = entry.getKey();

          if (k instanceof GroupAttrName
              && ( (GroupAttrName) k).groupId.equals(key))
          {
            itr.remove();

            removed = true;
          }
        }
      }
    }
    else
    {
      // remove single item.
      ICacheElement<K, V> ce = (ICacheElement) map.remove(key);
      removed = true;
    }

    return removed;
  }

  /**
   *  Get an Array of the keys for all elements in the memory cache
   *
   *@return    An Object[]
   */
  public Object[] getKeyArray()
  {
    // need a better locking strategy here.
    synchronized (this)
    {
      // may need to lock to map here?
      return map.keySet().toArray();
    }
  }

  // ---------------------------------------------------------- debug methods

  /**
   * Dump the cache map for debugging.
   */
  public void dumpMap()
  {
    log.debug("dumpingMap");

    for (Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
    {
      Map.Entry e = (Map.Entry) itr.next();
      MemoryElementDescriptor me = (MemoryElementDescriptor) e.getValue();
      log.debug("dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal());
    }
  }

  /**
   *  Dump the cache entries from first to list for debugging.
   */
  public void dumpCacheEntries()
  {
    log.debug("dumpingCacheEntries");
    //Map.Entry e = map.
    //for (  )
    //{
    //    log.debug( "dumpCacheEntries> key="
    //         + me.ce.getKey() + ", val=" + me.ce.getVal() );
    //}
  }

  private int dumpCacheSize()
  {
    int size = 0;
    size = map.size();
    return size;
  }

  // ---------------------------------------------------------- extended map

  /**
   * Implementation of removeEldestEntry in LinkedHashMap
   */
  public class LHMSpooler
      extends java.util.LinkedHashMap
  {

    /**
     * Initialize to a small size--for now, 1/2 of max
     * 3rd variable "true" indicates that it should be access
     * and not time goverened.  This could be configurable.
     */
    public LHMSpooler()
    {
      super( (int) (cache.getCacheAttributes().getMaxObjects() * .5), .75F, true);
    }

    /**
     * Remove eldest.  Automatically called by LinkedHashMap.
     */
    protected boolean removeEldestEntry(Map.Entry eldest)
    {

      CacheElement element = (CacheElement) eldest.getValue();

      if (size() <= cache.getCacheAttributes().getMaxObjects())
      {
        return false;
      }
      else
      {

        if (log.isDebugEnabled())
        {
          log.debug("LHMLRU max size: " +
                    cache.getCacheAttributes().getMaxObjects()
                    + ".  Spooling element, key: " + element.getKey());
        }
        spoolToDisk(element);

        if (log.isDebugEnabled())
        {
          log.debug("LHMLRU size: " + map.size());
        }
      }
      return true;
    }

    /**
     * Puts the element in the DiskStore
     * @param  element  The CacheElement
     */
    private void spoolToDisk(CacheElement element)
    {
      cache.spoolToDisk(element);

      if (log.isDebugEnabled())
      {
        log.debug(cache.getCacheName() + "Spoolled element to disk: " +
                  element.getKey());
      }
    }

  }

}
