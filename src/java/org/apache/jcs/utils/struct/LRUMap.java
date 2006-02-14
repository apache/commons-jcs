package org.apache.jcs.utils.struct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;

/**
 * This is a simple LRUMap. It implements most of the map methods. It is not
 * recommended that you use any but put, get, remove, and clear.
 * <p>
 * Children can implement the processRemovedLRU method if they want to handle
 * the removal of the lest recently used item.
 * <p>
 * This class was abstracted out of the LRU Memory cache. Put, remove, and get
 * should be thread safe. It uses a hashtable and our own double linked list.
 * <p>
 * Locking is done on the instance.
 * 
 * @author aaronsm
 * 
 */
public class LRUMap
    implements Map
{

    private final static Log log = LogFactory.getLog( LRUMap.class );

    // double linked list for lru
    private DoubleLinkedList list;

    /**
     * Map where items are stored by key
     */
    protected Map map;

    int hitCnt = 0;

    int missCnt = 0;

    int putCnt = 0;

    // if the max is less than 0, there is no limit!
    int maxObjects = -1;

    // make configurable
    private int chunkSize = 1;

    /**
     * This creates an unbounded version. Setting the max objects will result in
     * spooling on subsequent puts.
     * 
     * @param maxObjects
     */
    public LRUMap()
    {
        list = new DoubleLinkedList();
        
        // normal hshtable is faster for
        // sequential keys.
        map = new Hashtable();
        //map = new ConcurrentHashMap();
    }

    /**
     * This sets the size limit.
     * 
     * @param maxObjects
     */
    public LRUMap( int maxObjects )
    {
        this();
        this.maxObjects = maxObjects;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#size()
     */
    public int size()
    {
        return map.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#clear()
     */
    public void clear()
    {
        map.clear();
        list.removeAll();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty()
    {
        return map.size() == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey( Object key )
    {
        return map.containsKey( key );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue( Object value )
    {
        return map.containsValue( value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#values()
     */
    public Collection values()
    {
        return map.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll( Map t )
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#entrySet()
     */
    public Set entrySet()
    {
        // todo, we should return a defensive copy
        // this is not thread safe.
        return map.entrySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#keySet()
     */
    public Set keySet()
    {
        // TODO fix this, it needs to return the keys inside the wrappers.
        return map.keySet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get( Object key )
    {
        Object retVal = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item  for key " + key );
        }

        LRUElementDescriptor me = (LRUElementDescriptor) map.get( key );

        if ( me != null )
        {
            hitCnt++;
            if ( log.isDebugEnabled() )
            {
                log.debug( "LRUMap hit for " + key );
            }

            retVal = me.getPayload();

            list.makeFirst( me );
        }
        else
        {
            missCnt++;
            log.debug( "LRUMap miss for " + key );
        }

        // verifyCache();
        return retVal;
    }

    /**
     * @param key
     * @return Object
     */
    public Object getQuiet( Object key )
    {
        Object ce = null;

        LRUElementDescriptor me = (LRUElementDescriptor) map.get( key );
        if ( me != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "LRUMap quiet hit for " + key );
            }

            ce = me.getPayload();
        }
        else if ( log.isDebugEnabled() )
        {
            log.debug( "LRUMap quiet miss for " + key );
        }

        return ce;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove( Object key )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing item for key: " + key );
        }

        // remove single item.
        LRUElementDescriptor me = (LRUElementDescriptor) map.remove( key );

        if ( me != null )
        {
            list.remove( me );

            return me.getPayload();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put( Object key, Object value )
    {
        putCnt++;

        LRUElementDescriptor old = null;
        synchronized ( this )
        {
            // TODO address double synchronization of addFirst, use write lock
            addFirst( key, value );
            // this must be synchronized
            old = (LRUElementDescriptor) map.put( ( (LRUElementDescriptor) list.getFirst() ).getKey(), list.getFirst() );

            // If the node was the same as an existing node, remove it.
            if ( old != null && ( (LRUElementDescriptor) list.getFirst() ).getKey().equals( old.getKey() ) )
            {
                list.remove( old );
            }
        }

        int size = map.size();
        // If the element limit is reached, we need to spool

        if ( this.maxObjects >= 0 && size > this.maxObjects )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "In memory limit reached, removing least recently used." );
            }

            // Write the last 'chunkSize' items to disk.
            int chunkSizeCorrected = Math.min( size, getChunkSize() );

            if ( log.isDebugEnabled() )
            {
                log.debug( "About to remove the least recently used. map size: " + size + ", max objects: "
                    + this.maxObjects + ", items to spool: " + chunkSizeCorrected );
            }

            // The spool will put them in a disk event queue, so there is no
            // need to pre-queue the queuing. This would be a bit wasteful
            // and wouldn't save much time in this synchronous call.

            for ( int i = 0; i < chunkSizeCorrected; i++ )
            {
                synchronized ( this )
                {
                    if ( list.getLast() != null )
                    {
                        if ( ( (LRUElementDescriptor) list.getLast() ) != null )
                        {
                            processRemovedLRU( ( (LRUElementDescriptor) list.getLast() ).getKey(),
                                               ( (LRUElementDescriptor) list.getLast() ).getPayload() );
                            if ( !map.containsKey( ( (LRUElementDescriptor) list.getLast() ).getKey() ) )
                            {
                                log.error( "update: map does not contain key: "
                                    + ( (LRUElementDescriptor) list.getLast() ).getKey() );
                                verifyCache();
                            }
                            if ( map.remove( ( (LRUElementDescriptor) list.getLast() ).getKey() ) == null )
                            {
                                log.warn( "update: remove failed for key: "
                                    + ( (LRUElementDescriptor) list.getLast() ).getKey() );
                                verifyCache();
                            }
                        }
                        else
                        {
                            throw new Error( "update: last.ce is null!" );
                        }
                        list.removeLast();
                    }
                    else
                    {
                        verifyCache();
                        throw new Error( "update: last is null!" );
                    }
                }
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "update: After spool map size: " + map.size() );
            }
            if ( map.size() != dumpCacheSize() )
            {
                log.error( "update: After spool, size mismatch: map.size() = " + map.size() + ", linked list size = "
                    + dumpCacheSize() );
            }
        }

        if ( old != null )
        {
            return old.getPayload();
        }
        return null;
    }

    /**
     * Adds a new node to the start of the link list.
     * 
     * @param key
     * @param val
     *            The feature to be added to the First
     */
    private synchronized void addFirst( Object key, Object val )
    {

        LRUElementDescriptor me = new LRUElementDescriptor( key, val );
        list.addFirst( me );
        return;
    }

    /**
     * Returns the size of the list.
     * 
     * @return int
     */
    private int dumpCacheSize()
    {
        return list.size();
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        for ( LRUElementDescriptor me = (LRUElementDescriptor) list.getFirst(); me != null; me = (LRUElementDescriptor) me.next )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "dumpCacheEntries> key=" + me.getKey() + ", val=" + me.getPayload() );
            }
        }
    }

    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for ( Iterator itr = map.entrySet().iterator(); itr.hasNext(); )
        {
            Map.Entry e = (Map.Entry) itr.next();
            LRUElementDescriptor me = (LRUElementDescriptor) e.getValue();
            if ( log.isDebugEnabled() )
            {
                log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.getPayload() );
            }
        }
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks
     * consistency between List and map.
     * 
     */
    protected void verifyCache()
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;
        log.debug( "verifycache: mapContains " + map.size() + " elements, linked list contains " + dumpCacheSize()
            + " elements" );
        log.debug( "verifycache: checking linked list by key " );
        for ( LRUElementDescriptor li = (LRUElementDescriptor) list.getFirst(); li != null; li = (LRUElementDescriptor) li.next )
        {
            Object key = li.getKey();
            if ( !map.containsKey( key ) )
            {
                log.error( "verifycache: map does not contain key : " + li.getKey() );
                log.error( "li.hashcode=" + li.getKey().hashCode() );
                log.error( "key class=" + key.getClass() );
                log.error( "key hashcode=" + key.hashCode() );
                log.error( "key toString=" + key.toString() );
                if ( key instanceof GroupAttrName )
                {
                    GroupAttrName name = (GroupAttrName) key;
                    log.error( "GroupID hashcode=" + name.groupId.hashCode() );
                    log.error( "GroupID.class=" + name.groupId.getClass() );
                    log.error( "AttrName hashcode=" + name.attrName.hashCode() );
                    log.error( "AttrName.class=" + name.attrName.getClass() );
                }
                dumpMap();
            }
            else if ( map.get( li.getKey() ) == null )
            {
                log.error( "verifycache: linked list retrieval returned null for key: " + li.getKey() );
            }
        }

        log.debug( "verifycache: checking linked list by value " );
        for ( LRUElementDescriptor li3 = (LRUElementDescriptor) list.getFirst(); li3 != null; li3 = (LRUElementDescriptor) li3.next )
        {
            if ( map.containsValue( li3 ) == false )
            {
                log.error( "verifycache: map does not contain value : " + li3 );
                dumpMap();
            }
        }

        log.debug( "verifycache: checking via keysets!" );
        for ( Iterator itr2 = map.keySet().iterator(); itr2.hasNext(); )
        {
            found = false;
            Serializable val = null;
            try
            {
                val = (Serializable) itr2.next();
            }
            catch ( NoSuchElementException nse )
            {
                log.error( "verifycache: no such element exception" );
            }

            for ( LRUElementDescriptor li2 = (LRUElementDescriptor) list.getFirst(); li2 != null; li2 = (LRUElementDescriptor) li2.next )
            {
                if ( val.equals( li2.getKey() ) )
                {
                    found = true;
                    break;
                }
            }
            if ( !found )
            {
                log.error( "verifycache: key not found in list : " + val );
                dumpCacheEntries();
                if ( map.containsKey( val ) )
                {
                    log.error( "verifycache: map contains key" );
                }
                else
                {
                    log.error( "verifycache: map does NOT contain key, what the HECK!" );
                }
            }
        }
    }

    /**
     * Logs an error is an element that should be in the cache is not.
     * 
     * @param key
     */
    protected void verifyCache( Object key )
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;

        // go through the linked list looking for the key
        for ( LRUElementDescriptor li = (LRUElementDescriptor) list.getFirst(); li != null; li = (LRUElementDescriptor) li.next )
        {
            if ( li.getKey() == key )
            {
                found = true;
                log.debug( "verifycache(key) key match: " + key );
                break;
            }
        }
        if ( !found )
        {
            log.error( "verifycache(key), couldn't find key! : " + key );
        }
    }

    /**
     * This is called when an item is removed from the LRU. We just log some
     * information.
     * <p>
     * Children can implement this method for special behavior.
     * 
     * @param key
     * @param value
     */
    protected void processRemovedLRU( Object key, Object value )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "Removing key: [" + key + "] from LRUMap store, value = [" + value + "]" );
            log.debug( "LRUMap store size: '" + this.size() + "'." );
        }
    }

    /**
     * The chunk size is the number of items to remove when the max is reached.
     * By default it is 1.
     * 
     * @param chunkSize
     *            The chunkSize to set.
     */
    public void setChunkSize( int chunkSize )
    {
        this.chunkSize = chunkSize;
    }

    /**
     * @return Returns the chunkSize.
     */
    public int getChunkSize()
    {
        return chunkSize;
    }

    /**
     * 
     * @return IStats
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "LRUMap" );

        ArrayList elems = new ArrayList();

        IStatElement se = null;

        se = new StatElement();
        se.setName( "List Size" );
        se.setData( "" + list.size() );
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

        return stats;
    }

}