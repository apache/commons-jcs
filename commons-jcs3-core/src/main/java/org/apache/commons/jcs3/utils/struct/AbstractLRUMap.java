package org.apache.commons.jcs3.utils.struct;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.commons.jcs3.engine.control.group.GroupAttrName;
import org.apache.commons.jcs3.engine.stats.StatElement;
import org.apache.commons.jcs3.engine.stats.Stats;
import org.apache.commons.jcs3.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs3.engine.stats.behavior.IStats;
import org.apache.commons.jcs3.log.Log;

/**
 * This is a simple LRUMap. It implements most of the map methods. It is not recommended that you
 * use any but put, get, remove, and clear.
 * <p>
 * Children can implement the processRemovedLRU method if they want to handle the removal of the
 * least recently used item.
 * </p>
 * <p>
 * This class was abstracted out of the LRU Memory cache. Put, remove, and get should be thread
 * safe. It uses a hashtable and our own double linked list.
 * </p>
 * <p>
 * Locking is done on the instance.
 * </p>
 */
public abstract class AbstractLRUMap<K, V>
    implements Map<K, V>
{
    /** The logger */
    private static final Log log = Log.getLog( AbstractLRUMap.class );

    /** Double linked list for lru */
    private final DoubleLinkedList<LRUElementDescriptor<K, V>> list;

    /** Map where items are stored by key. */
    private final Map<K, LRUElementDescriptor<K, V>> map;

    /** Lock to keep map and list synchronous */
    private final Lock lock = new ReentrantLock();

    /** Stats */
    private long hitCnt;

    /** Stats */
    private long missCnt;

    /** Stats */
    private long putCnt;

    /**
     * This creates an unbounded version. Setting the max objects will result in spooling on
     * subsequent puts.
     */
    public AbstractLRUMap()
    {
        list = new DoubleLinkedList<>();

        // normal hashtable is faster for
        // sequential keys.
        map = new ConcurrentHashMap<>();
    }

    /**
     * This removes all the items. It clears the map and the double linked list.
     *
     * @see Map#clear()
     */
    @Override
    public void clear()
    {
        lock.lock();
        try
        {
            map.clear();
            list.removeAll();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns true if the map contains an element for the supplied key.
     *
     * @see Map#containsKey(Object)
     */
    @Override
    public boolean containsKey( final Object key )
    {
        return map.containsKey( key );
    }

    /**
     * This is an expensive operation that determines if the object supplied is mapped to any key.
     *
     * @see Map#containsValue(Object)
     */
    @Override
    public boolean containsValue( final Object value )
    {
        return map.containsValue( value );
    }

    /**
     * Dump the cache entries from first to list for debugging.
     */
    @SuppressWarnings("unchecked") // No generics for public fields
    public void dumpCacheEntries()
    {
        if (log.isTraceEnabled())
        {
            log.trace("dumpingCacheEntries");
            for (LRUElementDescriptor<K, V> me = list.getFirst(); me != null; me = (LRUElementDescriptor<K, V>) me.next)
            {
                log.trace("dumpCacheEntries> key={0}, val={1}", me.getKey(), me.getPayload());
            }
        }
    }

    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        if (log.isTraceEnabled())
        {
            log.trace("dumpingMap");
            map.forEach((key, value) -> log.trace("dumpMap> key={0}, val={1}", key, value.getPayload()));
        }
    }

    /**
     * This returns a set of entries. Our LRUMapEntry is used since the value stored in the
     * underlying map is a node in the double linked list. We wouldn't want to return this to the
     * client, so we construct a new entry with the payload of the node.
     * <p>
     * TODO we should return out own set wrapper, so we can avoid the extra object creation if it
     * isn't necessary.
     *
     * @see Map#entrySet()
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet()
    {
        lock.lock();
        try
        {
            return map.entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(
                            entry.getKey(), entry.getValue().getPayload()))
                    .collect(Collectors.toSet());
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * @param key
     * @return Object
     */
    @Override
    public V get( final Object key )
    {
        final V retVal;

        log.debug( "getting item  for key {0}", key );

        final LRUElementDescriptor<K, V> me = map.get( key );

        if ( me == null )
        {
            missCnt++;
            retVal = null;
        }
        else
        {
            hitCnt++;
            retVal = me.getPayload();
            list.makeFirst( me );
        }

        if ( me == null )
        {
            log.debug( "LRUMap miss for {0}", key );
        }
        else
        {
            log.debug( "LRUMap hit for {0}", key );
        }

        // verifyCache();
        return retVal;
    }

    /**
     * This gets an element out of the map without adjusting it's position in the LRU. In other
     * words, this does not count as being used. If the element is the last item in the list, it
     * will still be the last time in the list.
     *
     * @param key
     * @return Object
     */
    public V getQuiet( final Object key )
    {
        V ce = null;
        final LRUElementDescriptor<K, V> me = map.get( key );

        if ( me != null )
        {
            ce = me.getPayload();
        }

        if ( me == null )
        {
            log.debug( "LRUMap quiet miss for {0}", key );
        }
        else
        {
            log.debug( "LRUMap quiet hit for {0}", key );
        }

        return ce;
    }

    /**
     * @return IStats
     */
    public IStats getStatistics()
    {
        final IStats stats = new Stats();
        stats.setTypeName( "LRUMap" );

        final ArrayList<IStatElement<?>> elems = new ArrayList<>();

        elems.add(new StatElement<>( "List Size", Integer.valueOf(list.size()) ) );
        elems.add(new StatElement<>( "Map Size", Integer.valueOf(map.size()) ) );
        elems.add(new StatElement<>( "Put Count", Long.valueOf(putCnt) ) );
        elems.add(new StatElement<>( "Hit Count", Long.valueOf(hitCnt) ) );
        elems.add(new StatElement<>( "Miss Count", Long.valueOf(missCnt) ) );

        stats.setStatElements( elems );

        return stats;
    }

    /**
     * Returns true if the map is empty.
     *
     * @see Map#isEmpty()
     */
    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    /**
     * @return map.keySet();
     */
    @Override
    public Set<K> keySet()
    {
        return map.values().stream()
                .map(LRUElementDescriptor::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * This is called when an item is removed from the LRU. We just log some information.
     * <p>
     * Children can implement this method for special behavior.
     * @param key
     * @param value
     */
    protected void processRemovedLRU(final K key, final V value )
    {
        log.debug( "Removing key: [{0}] from LRUMap store, value = [{1}]", key, value );
        log.debug( "LRUMap store size: \"{0}\".", this.size() );
    }

    /**
     * @param key
     * @param value
     * @return Object
     */
    @Override
    public V put(final K key, final V value)
    {
        putCnt++;

        LRUElementDescriptor<K, V> old = null;
        final LRUElementDescriptor<K, V> me = new LRUElementDescriptor<>(key, value);

        lock.lock();
        try
        {
            list.addFirst( me );
            old = map.put(key, me);

            // If the node was the same as an existing node, remove it.
            if ( old != null && key.equals(old.getKey()))
            {
                list.remove( old );
            }
        }
        finally
        {
            lock.unlock();
        }

        // If the element limit is reached, we need to spool
        if (shouldRemove())
        {
            log.debug( "In memory limit reached, removing least recently used." );

            // The spool will put them in a disk event queue, so there is no
            // need to pre-queue the queuing. This would be a bit wasteful
            // and wouldn't save much time in this synchronous call.
            while (shouldRemove())
            {
                lock.lock();
                try
                {
                    final LRUElementDescriptor<K, V> last = list.getLast();
                    if (last == null) {
                        verifyCache();
                        throw new Error("update: last is null!");
                    }
                    processRemovedLRU(last.getKey(), last.getPayload());
                    if (map.remove(last.getKey()) == null)
                    {
                        log.warn("update: remove failed for key: {0}",
                                last::getKey);
                        verifyCache();
                    }
                    list.removeLast();
                }
                finally
                {
                    lock.unlock();
                }
            }

            log.debug( "update: After spool map size: {0}", map::size);
            if ( map.size() != list.size() )
            {
                log.error("update: After spool, size mismatch: map.size() = {0}, "
                        + "linked list size = {1}",
                        map::size, list::size);
            }
        }

        if ( old != null )
        {
            return old.getPayload();
        }
        return null;
    }

    /**
     * @param source
     */
    @Override
    public void putAll( final Map<? extends K, ? extends V> source )
    {
        if ( source != null )
        {
            source.forEach(this::put);
        }
    }

    /**
     * @param key
     * @return Object removed
     */
    @Override
    public V remove( final Object key )
    {
        log.debug( "removing item for key: {0}", key );

        // remove single item.
        lock.lock();
        try
        {
            final LRUElementDescriptor<K, V> me = map.remove(key);

            if (me != null)
            {
                list.remove(me);
                return me.getPayload();
            }
        }
        finally
        {
            lock.unlock();
        }

        return null;
    }

    protected abstract boolean shouldRemove();

    /**
     * This simply returns the number of elements in the map.
     *
     * @see Map#size()
     */
    @Override
    public int size()
    {
        return map.size();
    }

    /**
     * @return map.values();
     */
    @Override
    public Collection<V> values()
    {
        return map.values().stream()
                .map(LRUElementDescriptor::getPayload)
                .collect(Collectors.toList());
    }

    /**
     * Checks to see if all the items that should be in the cache are. Checks consistency between
     * List and map.
     */
    @SuppressWarnings("unchecked") // No generics for public fields
    protected void verifyCache()
    {
        if ( !log.isTraceEnabled() )
        {
            return;
        }

        log.trace( "verifycache: mapContains {0} elements, linked list "
                + "contains {1} elements", map.size(), list.size() );
        log.trace( "verifycache: checking linked list by key" );
        for (LRUElementDescriptor<K, V> li = list.getFirst(); li != null; li = (LRUElementDescriptor<K, V>) li.next )
        {
            final K key = li.getKey();
            if ( !map.containsKey( key ) )
            {
                log.error( "verifycache: map does not contain key : {0}", li.getKey() );
                log.error( "li.hashCode={0}", li.getKey().hashCode() );
                log.error( "key class={0}", key.getClass() );
                log.error( "key hashCode={0}", key.hashCode() );
                log.error( "key toString={0}", key.toString() );
                if ( key instanceof GroupAttrName )
                {
                    final GroupAttrName<?> name = (GroupAttrName<?>) key;
                    log.error( "GroupID hashCode={0}", name.groupId.hashCode() );
                    log.error( "GroupID.class={0}", name.groupId.getClass() );
                    log.error( "AttrName hashCode={0}", name.attrName.hashCode() );
                    log.error( "AttrName.class={0}", name.attrName.getClass() );
                }
                dumpMap();
            }
            else if ( map.get( li.getKey() ) == null )
            {
                log.error( "verifycache: linked list retrieval returned null for key: {0}",
                        li.getKey() );
            }
        }

        log.trace( "verifycache: checking linked list by value " );
        for (LRUElementDescriptor<K, V> li3 = list.getFirst(); li3 != null; li3 = (LRUElementDescriptor<K, V>) li3.next )
        {
            if (!map.containsValue(li3))
            {
                log.error( "verifycache: map does not contain value : {0}", li3 );
                dumpMap();
            }
        }

        log.trace( "verifycache: checking via keysets!" );
        map.keySet().stream()
            .filter(key -> {
                for (LRUElementDescriptor<K, V> li2 = list.getFirst(); li2 != null; li2 = (LRUElementDescriptor<K, V>) li2.next )
                {
                    if ( key.equals( li2.getKey() ) )
                    {
                        return true;
                    }
                }

                log.error( "verifycache: key not found in list : {0}", key );
                dumpCacheEntries();
                if ( map.containsKey( key ) )
                {
                    log.error( "verifycache: map contains key" );
                }
                else
                {
                    log.error( "verifycache: map does NOT contain key, what the HECK!" );
                }

                return false;
            })
            .findFirst();
    }
}
