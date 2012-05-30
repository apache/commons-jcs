package org.apache.jcs.engine.memory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.jcs.engine.stats.StatElement;
import org.apache.jcs.engine.stats.Stats;
import org.apache.jcs.engine.stats.behavior.IStatElement;
import org.apache.jcs.engine.stats.behavior.IStats;
import org.apache.jcs.utils.struct.DoubleLinkedList;

/**
 * This class contains methods that are common to memory caches using the double linked list, such
 * as the LRU, MRU, FIFO, and LIFO caches.
 * <p>
 * Children can control the expiration algorithm by controlling the update and get. The last item in
 * the list will be the one removed when the list fills. For instance LRU should more items to the
 * front as they are used. FIFO should simply add new items to the front of the list.
 */
public abstract class AbstractDoulbeLinkedListMemoryCache<K extends Serializable, V extends Serializable>
    extends AbstractMemoryCache<K, V>
{
    /** Don't change. */
    private static final long serialVersionUID = 1422569420563967389L;

    /** The logger. */
    private final static Log log = LogFactory.getLog( AbstractDoulbeLinkedListMemoryCache.class );

    /** thread-safe double linked list for lru */
    protected DoubleLinkedList<MemoryElementDescriptor<K, V>> list;

    /** number of hits */
    protected int hitCnt = 0;

    /** number of misses */
    protected int missCnt = 0;

    /** number of puts */
    private int putCnt = 0;

    /**
     * For post reflection creation initialization.
     * <p>
     * @param hub
     */
    @Override
    public synchronized void initialize( CompositeCache<K, V> hub )
    {
        super.initialize( hub );
        list = new DoubleLinkedList<MemoryElementDescriptor<K, V>>();
        log.info( "initialized MemoryCache for " + cacheName );
    }

    /**
     * This is called by super initialize.
     * <p>
     * @return new Hashtable()
     */
    @Override
    public Map<K, MemoryElementDescriptor<K, V>> createMap()
    {
        return new Hashtable<K, MemoryElementDescriptor<K, V>>();
    }

    /**
     * Calls the abstract method updateList.
     * <p>
     * If the max size is reached, an element will be put to disk.
     * <p>
     * @param ce The cache element, or entry wrapper
     * @exception IOException
     */
    @Override
    public final void update( ICacheElement<K, V> ce )
        throws IOException
    {
        putCnt++;
        ce.getElementAttributes().setLastAccessTimeNow();

        synchronized ( this )
        {
            // ABSTRACT
            MemoryElementDescriptor<K, V> newNode = adjustListForUpdate( ce );

            // this must be synchronized
            MemoryElementDescriptor<K, V> oldNode = map.put( newNode.ce.getKey(), newNode );

            // If the node was the same as an existing node, remove it.
            if ( oldNode != null && ( newNode.ce.getKey().equals( oldNode.ce.getKey() ) ) )
            {
                list.remove( oldNode );
            }
        }

        // If we are over the max spool some
        spoolIfNeeded();
    }

    /**
     * Children implement this to control the cache expiration algorithm
     * <p>
     * @param ce
     * @return MemoryElementDescriptor the new node
     * @throws IOException
     */
    protected abstract MemoryElementDescriptor<K, V> adjustListForUpdate( ICacheElement<K, V> ce )
        throws IOException;

    /**
     * If the max size has been reached, spool.
     * <p>
     * @throws Error
     */
    private void spoolIfNeeded()
        throws Error
    {
        int size = map.size();
        // If the element limit is reached, we need to spool

        if ( size <= this.cacheAttributes.getMaxObjects() )
        {
            return;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "In memory limit reached, spooling" );
        }

        // Write the last 'chunkSize' items to disk.
        int chunkSizeCorrected = Math.min( size, chunkSize );

        if ( log.isDebugEnabled() )
        {
            log.debug( "About to spool to disk cache, map size: " + size + ", max objects: "
                + this.cacheAttributes.getMaxObjects() + ", items to spool: " + chunkSizeCorrected );
        }

        // The spool will put them in a disk event queue, so there is no
        // need to pre-queue the queuing. This would be a bit wasteful
        // and wouldn't save much time in this synchronous call.
        for ( int i = 0; i < chunkSizeCorrected; i++ )
        {
            spoolLastElement();
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "update: After spool map size: " + map.size() + " linked list size = " + dumpCacheSize() );
        }
    }

    /**
     * Get an item from the cache If the item is found, it is removed from the list and added first.
     * <p>
     * @param key Identifies item to find
     * @return ICacheElement<K, V> if found, else null
     * @exception IOException
     */
    @Override
    public final synchronized ICacheElement<K, V> get( K key )
        throws IOException
    {
        ICacheElement<K, V> ce = null;

        if ( log.isDebugEnabled() )
        {
            log.debug( "getting item from cache " + cacheName + " for key " + key );
        }

        MemoryElementDescriptor<K, V> me = map.get( key );

        if ( me != null )
        {
            ce = me.ce;
            hitCnt++;
            ce.getElementAttributes().setLastAccessTimeNow();
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache hit for " + ce.getKey() );
            }

            // ABSTRACT
            adjustListForGet( me );
        }
        else
        {
            missCnt++;
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": LRUMemoryCache miss for " + key );
            }
        }

        verifyCache();
        return ce;
    }

    /**
     * Adjust the list as needed for a get. This allows children to control the algorithm
     * <p>
     * @param me
     */
    protected abstract void adjustListForGet( MemoryElementDescriptor<K, V> me );

    /**
     * This instructs the memory cache to remove the <i>numberToFree</i> according to its eviction
     * policy. For example, the LRUMemoryCache will remove the <i>numberToFree</i> least recently
     * used items. These will be spooled to disk if a disk auxiliary is available.
     * <p>
     * @param numberToFree
     * @return the number that were removed. if you ask to free 5, but there are only 3, you will
     *         get 3.
     * @throws IOException
     */
    public int freeElements( int numberToFree )
        throws IOException
    {
        int freed = 0;
        for ( ; freed < numberToFree; freed++ )
        {
            ICacheElement<K, V> element = spoolLastElement();
            if ( element == null )
            {
                break;
            }
        }
        return freed;
    }

    /**
     * This spools the last element in the LRU, if one exists.
     * <p>
     * @return ICacheElement<K, V> if there was a last element, else null.
     * @throws Error
     */
    protected ICacheElement<K, V> spoolLastElement()
        throws Error
    {
        ICacheElement<K, V> toSpool = null;
        synchronized ( this )
        {
            if ( list.getLast() != null )
            {
                toSpool = list.getLast().ce;
                if ( toSpool != null )
                {
                    cache.spoolToDisk( list.getLast().ce );
                    if ( !map.containsKey( list.getLast().ce.getKey() ) )
                    {
                        log.error( "update: map does not contain key: "
                            + list.getLast().ce.getKey() );
                        verifyCache();
                    }
                    if ( map.remove( list.getLast().ce.getKey() ) == null )
                    {
                        log.warn( "update: remove failed for key: "
                            + list.getLast().ce.getKey() );
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

            // If this is out of the sync block it can detect a mismatch
            // where there is none.
            if ( map.size() != dumpCacheSize() )
            {
                log.warn( "update: After spool, size mismatch: map.size() = " + map.size() + ", linked list size = "
                    + dumpCacheSize() );
            }
        }
        return toSpool;
    }

    /**
     * Removes an item from the cache. This method handles hierarchical removal. If the key is a
     * String and ends with the CacheConstants.NAME_COMPONENT_DELIMITER, then all items with keys
     * starting with the argument String will be removed.
     * <p>
     * @param key
     * @return true if the removal was successful
     * @exception IOException
     */
    @Override
    public synchronized boolean remove( K key )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing item for key: " + key );
        }

        boolean removed = false;

        // handle partial removal
        if ( key instanceof String && ( (String) key ).endsWith( CacheConstants.NAME_COMPONENT_DELIMITER ) )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
                    K k = entry.getKey();

                    if ( k instanceof String && ( (String) k ).startsWith( key.toString() ) )
                    {
                        list.remove( entry.getValue() );
                        itr.remove();
                        removed = true;
                    }
                }
            }
        }
        else if ( key instanceof GroupAttrName )
        {
            // remove all keys of the same name hierarchy.
            synchronized ( map )
            {
                for (Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> itr = map.entrySet().iterator(); itr.hasNext(); )
                {
                    Map.Entry<K, MemoryElementDescriptor<K, V>> entry = itr.next();
                    K k = entry.getKey();

                    if ( k instanceof GroupAttrName &&
                        ((GroupAttrName<?>)k).groupId.equals(((GroupAttrName<?>)key).groupId))
                    {
                        list.remove( entry.getValue() );
                        itr.remove();
                        removed = true;
                    }
                }
            }
        }
        else
        {
            // remove single item.
            MemoryElementDescriptor<K, V> me = map.remove( key );

            if ( me != null )
            {
                list.remove( me );
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Remove all of the elements from both the Map and the linked list implementation. Overrides
     * base class.
     * <p>
     * @throws IOException
     */
    @Override
    public synchronized void removeAll()
        throws IOException
    {
        map.clear();
        list.removeAll();
    }

    // --------------------------- internal methods (linked list implementation)
    /**
     * Adds a new node to the start of the link list.
     * <p>
     * @param ce The feature to be added to the First
     * @return MemoryElementDescriptor
     */
    protected synchronized MemoryElementDescriptor<K, V> addFirst( ICacheElement<K, V> ce )
    {
        MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<K, V>( ce );
        list.addFirst( me );
        verifyCache( ce.getKey() );
        return me;
    }

    /**
     * Adds a new node to the end of the link list.
     * <p>
     * @param ce The feature to be added to the First
     * @return MemoryElementDescriptor
     */
    protected synchronized MemoryElementDescriptor<K, V> addLast( ICacheElement<K, V> ce )
    {
        MemoryElementDescriptor<K, V> me = new MemoryElementDescriptor<K, V>( ce );
        list.addLast( me );
        verifyCache( ce.getKey() );
        return me;
    }

    // ---------------------------------------------------------- debug methods

    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void dumpCacheEntries()
    {
        log.debug( "dumpingCacheEntries" );
        for ( MemoryElementDescriptor<K, V> me = list.getFirst(); me != null; me = (MemoryElementDescriptor<K, V>) me.next )
        {
            log.debug( "dumpCacheEntries> key=" + me.ce.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     * Returns the size of the list.
     * <p>
     * @return the number of items in the map.
     */
    protected int dumpCacheSize()
    {
        return list.size();
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks consistency between
     * List and map.
     */
    protected void verifyCache()
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;
        log.debug( "verifycache[" + cacheName + "]: mapContains " + map.size() + " elements, linked list contains "
            + dumpCacheSize() + " elements" );
        log.debug( "verifycache: checking linked list by key " );
        for ( MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next )
        {
            Object key = li.ce.getKey();
            if ( !map.containsKey( key ) )
            {
                log.error( "verifycache[" + cacheName + "]: map does not contain key : " + li.ce.getKey() );
                log.error( "li.hashcode=" + li.ce.getKey().hashCode() );
                log.error( "key class=" + key.getClass() );
                log.error( "key hashcode=" + key.hashCode() );
                log.error( "key toString=" + key.toString() );
                if ( key instanceof GroupAttrName )
                {
                    GroupAttrName<?> name = (GroupAttrName<?>) key;
                    log.error( "GroupID hashcode=" + name.groupId.hashCode() );
                    log.error( "GroupID.class=" + name.groupId.getClass() );
                    log.error( "AttrName hashcode=" + name.attrName.hashCode() );
                    log.error( "AttrName.class=" + name.attrName.getClass() );
                }
                dumpMap();
            }
            else if ( map.get( li.ce.getKey() ) == null )
            {
                log.error( "verifycache[" + cacheName + "]: linked list retrieval returned null for key: "
                    + li.ce.getKey() );
            }
        }

        log.debug( "verifycache: checking linked list by value " );
        for ( MemoryElementDescriptor<K, V> li3 = list.getFirst(); li3 != null; li3 = (MemoryElementDescriptor<K, V>) li3.next )
        {
            if ( map.containsValue( li3 ) == false )
            {
                log.error( "verifycache[" + cacheName + "]: map does not contain value : " + li3 );
                dumpMap();
            }
        }

        log.debug( "verifycache: checking via keysets!" );
        for (Serializable val : map.keySet())
        {
            found = false;

            for ( MemoryElementDescriptor<K, V> li2 = list.getFirst(); li2 != null; li2 = (MemoryElementDescriptor<K, V>) li2.next )
            {
                if ( val.equals( li2.ce.getKey() ) )
                {
                    found = true;
                    break;
                }
            }
            if ( !found )
            {
                log.error( "verifycache[" + cacheName + "]: key not found in list : " + val );
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
     * Logs an error if an element that should be in the cache is not.
     * <p>
     * @param key
     */
    private void verifyCache( K key )
    {
        if ( !log.isDebugEnabled() )
        {
            return;
        }

        boolean found = false;

        // go through the linked list looking for the key
        for ( MemoryElementDescriptor<K, V> li = list.getFirst(); li != null; li = (MemoryElementDescriptor<K, V>) li.next )
        {
            if ( li.ce.getKey() == key )
            {
                found = true;
                log.debug( "verifycache(key) key match: " + key );
                break;
            }
        }
        if ( !found )
        {
            log.error( "verifycache(key)[" + cacheName + "], couldn't find key! : " + key );
        }
    }

    // --------------------------- iteration methods (iteration helpers)
    /**
     * iteration aid
     */
    public static class IteratorWrapper<K extends Serializable, V extends Serializable>
        implements Iterator<Entry<K, MemoryElementDescriptor<K, V>>>
    {
        /** The internal iterator */
        private final Iterator<Entry<K, MemoryElementDescriptor<K, V>>> i;

        /**
         * Wrapped to remove our wrapper object
         * @param m
         */
        protected IteratorWrapper(Map<K, MemoryElementDescriptor<K, V>> m)
        {
            i = m.entrySet().iterator();
        }

        /** @return i.hasNext() */
        public boolean hasNext()
        {
            return i.hasNext();
        }

        /** @return new MapEntryWrapper( (Map.Entry) i.next() ) */
        public Entry<K, MemoryElementDescriptor<K, V>> next()
        {
            // return new MapEntryWrapper<Serializable>( i.next() );
            return i.next();
        }

        /** i.remove(); */
        public void remove()
        {
            i.remove();
        }

        /**
         * @param o
         * @return i.equals( o ))
         */
        @Override
        public boolean equals( Object o )
        {
            return i.equals( o );
        }

        /** @return i.hashCode() */
        @Override
        public int hashCode()
        {
            return i.hashCode();
        }
    }

    /**
     * @author Aaron Smuts
     */
    public static class MapEntryWrapper<K extends Serializable, V extends Serializable>
        implements Map.Entry<K, ICacheElement<K, V>>
    {
        /** The internal entry */
        private final Map.Entry<K, MemoryElementDescriptor<K, V>> e;

        /**
         * @param e
         */
        private MapEntryWrapper( Map.Entry<K, MemoryElementDescriptor<K, V>> e )
        {
            this.e = e;
        }

        /**
         * @param o
         * @return e.equals( o )
         */
        @Override
        public boolean equals( Object o )
        {
            return e.equals( o );
        }

        /** @return e.getKey() */
        public K getKey()
        {
            return e.getKey();
        }

        /** @return ( (MemoryElementDescriptor) e.getValue() ).ce */
        public ICacheElement<K, V> getValue()
        {
            return e.getValue().ce;
        }

        /** @return e.hashCode() */
        @Override
        public int hashCode()
        {
            return e.hashCode();
        }

        /**
         * invalid
         * @param value
         * @return always throws
         */
        public ICacheElement<K, V> setValue(ICacheElement<K, V> value)
        {
            throw new UnsupportedOperationException( "Use normal cache methods"
                + " to alter the contents of the cache." );
        }
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     * <p>
     * @return The iterator value
     */
    @Override
    public Iterator<Entry<K, MemoryElementDescriptor<K, V>>> getIterator()
    {
        return new IteratorWrapper<K, V>( map );
    }

    /**
     * Get an Array of the keys for all elements in the memory cache
     * @return An Object[]
     */
    @Override
    public Set<K> getKeySet()
    {
        // need a better locking strategy here.
        synchronized ( this )
        {
            // may need to lock to map here?
            return new LinkedHashSet<K>(map.keySet());
        }
    }

    /**
     * This returns semi-structured information on the memory cache, such as the size, put count,
     * hit count, and miss count.
     * <p>
     * @see org.apache.jcs.engine.memory.IMemoryCache#getStatistics()
     */
    @Override
    public synchronized IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( /*add algorithm name*/"Memory Cache" );

        ArrayList<IStatElement> elems = new ArrayList<IStatElement>();

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
        IStatElement[] ses = elems.toArray( new StatElement[0] );
        stats.setStatElements( ses );

        return stats;
    }
}
