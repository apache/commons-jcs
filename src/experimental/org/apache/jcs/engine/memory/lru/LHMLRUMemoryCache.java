package org.apache.jcs.engine.memory.lru;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.memory.AbstractMemoryCache;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.control.group.GroupAttrName;

/**
 *  This is a test memory manager using the jdk1.4 LinkedHashMap.
 *  There may be some thread safety issues.
 *  So far i cannot notice any performance difference between this and the
 *  standard LRU implementation.  It needs more testing.
 *
 *@author     <a href="mailto:asmuts@yahoo.com">Aaron Smuts</a>
 *@author     <a href="mailto:jtaylor@apache.org">James Taylor</a>
 *@author     <a href="mailto:jmcnally@apache.org">John McNally</a>
 *@created    April 14, 2004
 *@version    $Id$
 */
public class LHMLRUMemoryCache
    extends AbstractMemoryCache
{
    private final static Log log = LogFactory.getLog( LRUMemoryCache.class );

    // the extended LinkedHashMap
    private Map map;

    /**
     *  For post reflection creation initialization
     *
     *@param  hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        super.initialize(hub);

        map = new LHMSpooler();

        log.info( "initialized LHMLRUMemoryCache for " + cacheName );
    }

    /**
     *  Puts an item to the cache.
     *
     *@param  ce               Description of the Parameter
     *@exception  IOException
     */
    public void update( ICacheElement ce )
        throws IOException
    {
        // Asynchronisly create a MemoryElement
        ce.getElementAttributes().setLastAccessTimeNow();
        map.put( ce.getKey(), ce );
    }

    /**
     * Remove all of the elements from both the Map and the linked
     * list implementation. Overrides base class.
     */
    public synchronized void removeAll()
        throws IOException
    {
        map.clear();
    }

    /**
     *  Get an item from the cache without affecting its last access time or
     *  position.  There is no way to do this with the LinkedHashMap!
     *
     *@param  key              Identifies item to find
     *@return                  Element mathinh key if found, or null
     *@exception  IOException
     */
    public ICacheElement getQuiet( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        ce = (ICacheElement)map.get(key);

        if ( ce != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug(cacheName + ": LRUMemoryCache quiet hit for " + key);
            }

        }
        else if ( log.isDebugEnabled() )
        {
            log.debug( cacheName + ": LRUMemoryCache quiet miss for " + key );
        }

        return ce;
    }


    /**
     *  Get an item from the cache
     *
     *@param  key              Identifies item to find
     *@return                  ICacheElement if found, else null
     *@exception  IOException
     */
    public synchronized ICacheElement get( Serializable key )
        throws IOException
    {
        ICacheElement ce = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item from cache " + cacheName + " for key " +
                       key );
        }

        ce = (ICacheElement)map.get(key);

        if ( ce != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache hit for " + key );
            }


        }
        else
        {
            log.debug( cacheName + ": LRUMemoryCache miss for " + key );
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
     *@exception  IOException
     */
    public synchronized boolean remove( Serializable key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing item for key: " + key );
        }

        boolean removed = false;

        // handle partial removal
        if ( key instanceof String && ( ( String ) key )
            .endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for ( Iterator itr = map.entrySet().iterator(); itr.hasNext();  )
                {
                    Map.Entry entry = ( Map.Entry ) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof String
                         && ( ( String ) k ).startsWith( key.toString() ) )
                    {
                        itr.remove();

                         removed = true;
                    }
                }
            }
        }
        else if ( key instanceof GroupId )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for (Iterator itr = map.entrySet().iterator(); itr.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) itr.next();
                    Object k = entry.getKey();

                    if ( k instanceof GroupAttrName
                         && ((GroupAttrName)k).groupId.equals(key) )
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
            ICacheElement ce = (ICacheElement ) map.remove( key );
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
        synchronized ( this )
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
        log.debug( "dumpingMap" );

        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
            Map.Entry e = ( Map.Entry ) itr.next();
            MemoryElementDescriptor me = ( MemoryElementDescriptor ) e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     *  Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
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
  public class LHMSpooler extends java.util.LinkedHashMap
  {

        /**
         * Initialize to a small size--for now, 1/2 of max
         * 3rd variable "true" indicates that it should be access
         * and not time goverened.  This could be configurable.
         */
        public LHMSpooler( )
        {
            super( (int)(cache.getCacheAttributes().getMaxObjects() * .5), .75F, true);
        }


        /**
         * Remove eldest.  Automatically called by LinkedHashMap.
         */
        protected boolean removeEldestEntry(Map.Entry eldest)
        {

            CacheElement element = (CacheElement) eldest.getValue();

            if ( size() <= cache.getCacheAttributes().getMaxObjects() )
            {
                return false;
            }
            else
            {

                if ( log.isDebugEnabled() )
                {
                    log.debug( "LHMLRU max size: " + cache.getCacheAttributes().getMaxObjects()
                            + ".  Spooling element, key: " + element.getKey() );
                }
                spoolToDisk(element);

                if ( log.isDebugEnabled() )
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
        private void spoolToDisk( CacheElement element )
        {
             cache.spoolToDisk(element);

             if ( log.isDebugEnabled() )
             {
                log.debug(cache.getCacheName() + "Spoolled element to disk: " + element.getKey());
            }
        }

  }

}
