package org.apache.commons.jcs.engine.memory.arc;

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

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.memory.AbstractMemoryCache;
import org.apache.commons.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs.engine.stats.StatElement;
import org.apache.commons.jcs.engine.stats.Stats;
import org.apache.commons.jcs.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs.engine.stats.behavior.IStats;
import org.apache.commons.jcs.utils.struct.DoubleLinkedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This is a rough implmentation of an adaptive replacement cache. ARC is a
 * hybrid LFU / LRU that adapts to user behavior.
 * <p>
 * See the ARC method for more detail on how the algorithm works.
 * <p>
 * @see http://www.almaden.ibm.com/StorageSystems/autonomic_storage/ARC/index.shtml
 * @see http://www.almaden.ibm.com/cs/people/dmodha/ARC.pdf
 */
public class ARCMemoryCache
    extends AbstractMemoryCache
{
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog( ARCMemoryCache.class );

    // private int[] loc = new int[0];

    // maximum size
    private int maxSize = 0;

    private DoubleLinkedList T1 = new DoubleLinkedList();

    private DoubleLinkedList T2 = new DoubleLinkedList();

    private DoubleLinkedList B1 = new DoubleLinkedList();

    private DoubleLinkedList B2 = new DoubleLinkedList();

    /** id of list T1 */
    protected static final int _T1_ = 1;

    /** id of list T2 */
    protected static final int _T2_ = 2;

    /** id of list B1 */
    protected static final int _B1_ = 3;

    /** id of list B2 */
    protected static final int _B2_ = 4;

    // ideal size of T1
    private int target_T1 = 0;

    private ConcurrentHashMap map = new ConcurrentHashMap();

    private int cnt = 0;

    private int hitCnt = 0;

    private int missCnt = 0;

    private int putCnt = 0;

    /**
     * Default constructor.
     */
    public ARCMemoryCache()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( "Loading Arc" );
        }
    }

    /**
     * Get an Array of the keys for all elements in the memory cache
     * @return Object[]
     */
    public Object[] getKeyArray()
    {
        // need to lock to map here?
        synchronized ( map )
        {
            return map.keySet().toArray();
        }
    }

    public ICacheElement<K, V> getQuiet( K key )
        throws IOException
    {
        return get( key );
    }

    /**
     * For post reflection creation initialization
     * <p>
     * @param hub
     */
    public synchronized void initialize( CompositeCache hub )
    {
        super.initialize( hub );
        maxSize = this.cattr.getMaxObjects(); // / 2;
        target_T1 = maxSize / 2;
        log.info( "initialized LRUMemoryCache for " + cacheName );
    }

    /**
     * Looks for the item in the lists.
     */
    public ICacheElement<K, V> get( K key )
        throws IOException
    {
        CacheElement ce = new CacheElement( cacheName, key, null );

        ICacheElement<K, V> ice = null;
        try
        {
            ice = ARC( ce, true );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
        return ice;
    }

    /**
     * Adds an element to the cache.
     */
    public void update( ICacheElement<K, V> ce )
    {
        try
        {
            ARC( ce, false );
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

    /**
     * This is the primary method for the ARC. It handles both puts and gets.
     * <p>
     * The ARC has 4 linked lists: T1, T2, B1, and B2. The 'T' lists are tops
     * and the 'B' lists are bottoms. Bottom lists do not hold object, only
     * keys.
     * <p>
     * The T1 list is an LRU (Least Recently Used) list. The T2 list is a near
     * LFU (Least Frequently Used) list.
     * <p>
     * After items are removed from T1 and T2, their keys are stored in B1 and
     * B2. The number of keys in B1 and B2 is restricted to the number of max
     * items.
     * <p>
     * When there is a put or a get for an item whose key exists on one of the
     * bottom lists, the maximum number of items in T1 is adjusted. If the item
     * was found on B2 (the bottom LFU list) the maximum allowed in T1 (the top
     * LRU list) is reduced. If the item is found in B1 list (the bottom LRU)
     * the maximum allowed in T1 is increased.
     * <p>
     * The maximum allowed in T1 will not exceed the maxSize. The maximum in T1
     * and T2 combined will not exceed the maxSize. The maximum number of
     * elements and keys allowed in all 4 lists will not exceed twice the
     * maximum size.
     * <p>
     * All the elements are stored in a map. The lists keep track of when the
     * element was last used, or when it was deleted and from where. We first
     * look for the item in the map, if we find it, we go looking for it in the
     * lists.
     * <p>
     * @param ce
     *            ICacheElement
     * @param isGet
     *            boolean
     * @return ICacheElement
     */
    public ICacheElement<K, V> ARC( ICacheElement<K, V> ce, boolean isGet )
    {
        cnt++;
        logStatsOccassionally( ce, isGet );
        if ( !isGet )
        {
            putCnt++;
        }

        ElementDescriptor temp = (ElementDescriptor) map.get( ce.getKey() );
        boolean isHit = true;

        if ( temp != null )
        {
            if ( isGet )
            {
                hitCnt++;
            }

            // determine where the element lives.
            switch ( temp.listNum )
            {
                case _T1_:
                    handleFoundInT1( temp );
                    break;

                case _T2_:
                    handleFoundInT2( temp );
                    break;

                case _B1_:
                    temp = handleFoundInB1( ce, isGet, temp );
                    break;

                case _B2_:
                    temp = handleFoundInB2( ce, isGet, temp );
                    break;
            }
        }
        else
        {
            /* Element is not in cache */
            isHit = false;
            if ( isGet )
            {
                missCnt++;
            }
            if ( log.isDebugEnabled() )
            {
                log.debug( "Element is not in cache" );
            }
        }

        // ////////////////////////////////////////////////////////////////////////////
        // Do some size Checks if this is a put
        if ( !isGet && !isHit )
        {
            if ( T1.size() + B1.size() >= maxSize )
            {
                /* B1 + T1 full? */
                if ( T1.size() < maxSize )
                {
                    /* Still room in T1? */
                    temp = (ElementDescriptor) B1.removeLast();
                    if ( temp != null )
                    {
                        map.remove( temp.key );
                    }
                    /* yes: take page off B1 */
                    // temp->pointer = replace(); /* find new place to put page
                    // */
                    replace( temp );
                }
                else
                {
                    /* no: B1 must be empty */
                    temp = (ElementDescriptor) T1.removeLast(); /*
                                                                 * take page //
                                                                 * off // T1
                                                                 */
                    map.remove( temp.ce.getKey() );
                    // if (temp->dirty) destage(temp); /* if dirty, evict before
                    // overwrite */
                    replace( temp );
                }
            }
            else
            {
                /* B1 + T1 have less than the maxSize elements */
                if ( T1.size() + T2.size() + B1.size() + B2.size() >= maxSize )
                {
                    /* cache full? */
                    /* Yes, cache full: */
                    if ( T1.size() + T2.size() + B1.size() + B2.size() >= 2 * maxSize )
                    {
                        /* cache is full: */
                        /* x find and reuse B2�s LRU */
                        temp = (ElementDescriptor) B2.removeLast();
                        if ( temp != null )
                        {
                            map.remove( temp.key );
                        }
                    }
                    else
                    {
                        /* cache directory not full, easy case */
                        // nop
                    }
                    replace( temp );
                }
                else
                {
                    /* cache not full, easy case */
                    // nop
                }
            }
        }

        if ( !isGet && !isHit )
        {
            temp = new ElementDescriptor( ce );
            temp.ce = ce;
            temp.listNum = _T1_;
            T1.addFirst( temp );
            // seen once recently, put on T1
            this.map.put( temp.ce.getKey(), temp );
        }
        // end if put

        if ( temp == null )
        {
            return null;
        }
        return temp.ce;
    }

    /**
     * Move to T2 if the item was found in T1.
     * <p>
     * @param temp
     */
    protected void handleFoundInT1( ElementDescriptor temp )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "T1" );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "T1 to T2, before remove = " + T1.size() );
        }
        boolean stat1 = T1.remove( temp );
        // need to implement our
        // own list
        if ( log.isDebugEnabled() )
        {
            log.debug( "T1 to T2, after remove = " + T1.size() + " stat = " + stat1 );
        }

        temp.listNum = _T2_;
        T2.addFirst( temp );
    }

    /**
     * If it was found in T2, we move it to the top of the T2 list.
     * <p>
     * @param temp
     */
    protected void handleFoundInT2( ElementDescriptor temp )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "T2" );
        }

        temp.listNum = _T2_;
        T2.makeFirst( temp );
    }

    /**
     * B1 hit: favor recency
     * <p>
     * @param ce
     * @param isGet
     * @param temp
     * @return
     */
    protected ElementDescriptor handleFoundInB1( ICacheElement<K, V> ce, boolean isGet, ElementDescriptor temp )
    {
        // B1 hit: favor recency

        // adapt the target size
        target_T1 = Math.min( target_T1 + Math.max( B2.size() / B1.size(), 1 ), maxSize );
        if ( log.isDebugEnabled() )
        {
            log.debug( "B1, targetT1 = " + target_T1 );
        }

        if ( !isGet )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "B1 before remove = " + B1.size() );
            }
            boolean stat3 = B1.remove( temp ); // need to implement
            // our own list
            if ( log.isDebugEnabled() )
            {
                log.debug( "B1 after remove = " + B1.size() + " stat = " + stat3 );
            }
            replace( temp );
            temp.listNum = _T2_;
            temp.ce = ce;
            // seen twice recently, put on T2
            T2.addFirst( temp );
        }
        else
        {
            // if this is just a get, then adjust the cache
            // it is essentially a miss.
            temp = null;
            hitCnt--;
            missCnt++;
        }
        return temp;
    }

    /**
     * B2 hit: favor frequency
     * <p>
     * @param ce
     * @param isGet
     * @param temp
     * @return
     */
    protected ElementDescriptor handleFoundInB2( ICacheElement<K, V> ce, boolean isGet, ElementDescriptor temp )
    {
        // adapt the target size
        target_T1 = Math.max( target_T1 - Math.max( B1.size() / B2.size(), 1 ), 0 );
        if ( log.isDebugEnabled() )
        {
            log.debug( "B2, targetT1 = " + target_T1 );
        }

        if ( !isGet )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "B2 before remove = " + B2.size() );
            }
            boolean stat4 = B2.remove( temp ); // need to implement
            // our own list
            if ( log.isDebugEnabled() )
            {
                log.debug( "B2 after remove = " + B2.size() + " stat = " + stat4 );
            }

            replace( temp );
            temp.listNum = _T2_;
            temp.ce = ce;
            // seen twice recently, put on T2
            T2.addFirst( temp );

            replace( temp );
        }
        else
        {
            // if this is just a get, then adjust the cache
            // it is essentially a miss.
            temp = null;
            hitCnt--;
            missCnt++;
        }
        return temp;
    }

    /**
     * Prints the stats every 10000 or so operations.
     * <p>
     * @param ce
     * @param isGet
     */
    protected void logStatsOccassionally( ICacheElement<K, V> ce, boolean isGet )
    {
        if ( cnt % 10000 == 0 )
        // if ( true )
        {
            if ( log.isInfoEnabled() )
            {
                StringBuilder buf = new StringBuilder();
                buf.append( "\n ce.key() = " ).append( ce.getKey() );
                buf.append( "\n isGet = " ).append( isGet );
                buf.append( getStats() );
                log.info( buf.toString() );
            }
        }
    }

    /**
     * This method doesn't so much replace as remove. It pushes the least
     * recently used in t1 or t2 to a dummy holder. The holder keeps a dummy
     * object that stores the key so that subsequent gets and puts can help
     * train the cache. Items are spooled if there is a disk cache at this
     * point.
     * <p>
     * @param orig
     *            ElementDescriptor
     */
    public void replace( ElementDescriptor orig )
    {
        try
        {
            ElementDescriptor temp;
            if ( T1.size() >= Math.max( 1, target_T1 ) )
            {
                // T1�s size exceeds target?
                // yes: T1 is too big
                temp = (ElementDescriptor) T1.getLast();
                if ( orig == null || !orig.key.equals( temp.key ) )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "replace -- T1 to B1" );
                        log.debug( getStats() );
                    }
                    temp = (ElementDescriptor) T1.removeLast(); // grab LRU from
                    // T1
                    // nullify object, temp is now just a dummy container to
                    // help adjust the lru size
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
                    // T1Length�; B1Length++; // bookkeep
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
            // if t2 is greater than or equal to what is left in c after the
            // target
            if ( ( T2.size() + T1.size() ) > maxSize )
            {

                // no: T1 is not too big
                temp = (ElementDescriptor) T2.getLast();
                if ( orig == null || !orig.key.equals( temp.key ) )
                {
                    if ( log.isDebugEnabled() )
                    {
                        log.debug( "replace -- T2 to B2" );
                        log.debug( getStats() );
                    }

                    temp = (ElementDescriptor) T2.removeLast(); // grab LRU page
                    // of T2
                    // nullify object, temp is now just a dummy container to
                    // help
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
                    // T2Length�; B2Length++; // bookkeep
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
     * remove the element if it is in any of the lists.
     * @param key
     *            Serializable
     * @return boolean
     */
    public boolean remove( K key )
    {
        ElementDescriptor temp = (ElementDescriptor) map.remove( key );
        if ( temp != null )
        {
            int loc = temp.listNum;
            if ( loc == _T1_ )
            {
                T1.remove( temp );
            }
            else if ( loc == _T2_ )
            {
                T2.remove( temp );
            }
            else if ( loc == _B1_ )
            {
                B1.remove( temp );
            }
            else if ( loc == _B2_ )
            {
                B2.remove( temp );
            }
        }
        return true;
    }

    /**
     * Remove all of the elements from both the Map and the linked list
     * implementation. Overrides base class.
     */
    public synchronized void removeAll()
        throws IOException
    {
        map.clear();
        T1.removeAll();
        T2.removeAll();
        B1.removeAll();
        B2.removeAll();
    }

    // ///////////////////////////////////////////////////////////////////////
    /**
     * @return
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.commons.jcs.engine.memory.MemoryCache#getStatistics()
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "LRU Memory Cache" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "T1 Size" );
        se.setData( "" + T1.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "T2 Size" );
        se.setData( "" + T2.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "B1 Size" );
        se.setData( "" + B1.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "B2 Size" );
        se.setData( "" + B2.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Target T1 Size" );
        se.setData( "" + target_T1 );
        elems.add( se );

        se = new StatElement();
        se.setName( "Map Size" );
        se.setData( "" + map.size() );
        elems.add( se );

        se = new StatElement();
        se.setName( "Put Count" );
        se.setData( "" + putCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Hit Count" );
        se.setData( "" + hitCnt );
        elems.add( se );

        se = new StatElement();
        se.setName( "Miss Count" );
        se.setData( "" + missCnt );
        elems.add( se );

        // get an array and put them in the Stats object
        IStatElement[] ses = (IStatElement[]) elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        // int rate = ((hitCnt + missCnt) * 100) / (hitCnt * 100) * 100;
        // buf.append("\n Hit Rate = " + rate + " %" );

        return stats;
    }

    // ///////////////////////////////////////////////
    /**
     * @author Aaron Smuts
     */
    public class ElementDescriptor
        extends MemoryElementDescriptor
    {
        private static final long serialVersionUID = -6271920830449238031L;

        /** Where this is located */
        public int listNum;

        /** Its key */
        public K key;

        /**
         * Constructs a usable object
         * @param ce
         */
        public ElementDescriptor( ICacheElement<K, V> ce )
        {
            super( ce );
            key = ce.getKey();
        }
    }

    /**
     * This is currently not implemented. It should remove items from t2. If
     * there are none in t2, it should move them to t2 and then remove them.
     * <p>
     * (non-Javadoc)
     * @see org.apache.commons.jcs.engine.memory.MemoryCache#freeElements(int)
     */
    public int freeElements( int numberToFree )
        throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * At the start this will be 1/2 the max.
     * <p>
     * @return Returns the target_T1.
     */
    protected int getTarget_T1()
    {
        return target_T1;
    }

    /**
     * Returns the size of the list.
     * @param listNumber
     * @return -1 if the list is unknown.
     */
    protected int getListSize( int listNumber )
    {
        switch ( listNumber )
        {
            case _T1_:
                return T1.size();

            case _T2_:
                return T2.size();

            case _B1_:
                return B1.size();

            case _B2_:
                return B2.size();
        }
        return -1;
    }

}
