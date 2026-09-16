package org.apache.commons.jcs4.utils.struct;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import org.apache.commons.jcs4.log.Log;

/**
 * This is a generic thread-safe double linked list. It uses internal locking and sharding to achieve
 * high throughput and little lock contention
 *
 * @see java.util.concurrent.locks.ReentrantLock
 * @see org.apache.commons.jcs4.engine.memory.AbstractDoubleLinkedListMemoryCache
 */
public class DoubleLinkedList<T extends DoubleLinkedListNode>
    implements Iterable<T>
{
    /** The logger */
    private static final Log log = Log.getLog( DoubleLinkedList.class );

    /** Record size to avoid having to iterate */
    private int size;

    /** Number of shards */
    private final int shards;

    /** The locks */
    private final Lock[] lock;

    /** LRU double linked list head node */
    private DoubleLinkedListNode[] first;

    /** LRU double linked list tail node */
    private DoubleLinkedListNode[] last;

    private static class AtomicCyclicCounter
    {
        private final int max;
        private final AtomicInteger counter;

        private AtomicCyclicCounter(int max)
        {
            this.max = max;
            counter = new AtomicInteger();
        }

        private int incrementAndGet()
        {
            return counter.accumulateAndGet(1, (index, inc) -> (++index >= max ? 0 : index));
        }
    }

    /** shard to spool */
    private final AtomicCyclicCounter spoolShard;

    /**
     * Construct DoubleLinkedList
     *
     * @param shards Number of shards
     */
    public DoubleLinkedList(int shards)
    {
        this.shards = shards;
        this.lock = new Lock[shards];
        this.first = new DoubleLinkedListNode[shards];
        this.last = new DoubleLinkedListNode[shards];
        this.spoolShard = new AtomicCyclicCounter(shards);

        for (int i = 0; i < shards; i++)
        {
            first[i] = new DoubleLinkedListNode();
            first[i].setShard(i);
            last[i] = new DoubleLinkedListNode();
            last[i].setShard(i);
            first[i].next = this.last[i];
            last[i].prev = this.first[i];
            lock[i] = new ReentrantLock();
        }
    }

    /**
     * Returns the number of shards
     *
     * @return the shards
     */
    public int getShards()
    {
        return shards;
    }


    /**
     * Returns the current cache shard for the given key
     *
     * @param key the cache key
     * @return The shard
     */
    public int spreadShard(Object key)
    {
        return Math.abs(key.hashCode() % shards);
    }

    /**
     * Adds a new node to the start of the link list.
     *
     * @param me The node to be added to the front
     */
    public void addFirst(final T me)
    {
        int shard = me.getShard();
        lock[shard].lock();
        try
        {
            me.prev = first[shard];
            me.next = first[shard].next;
            first[shard].next.prev = me;
            first[shard].next = me;
            size++;
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    /**
     * Adds a new node to the end of the link list.
     *
     * @param me The node to be added to the end
     */
    public void addLast(final T me)
    {
        int shard = me.getShard();
        lock[shard].lock();
        try
        {
            me.next = last[shard];
            me.prev = last[shard].prev;
            last[shard].prev.next = me;
            last[shard].prev = me;
            size++;
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    // ///////////////////////////////////////////////////////////////////
    /**
     * Dump the cache entries from first to list for debugging.
     */
    protected void debugDumpEntries()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "dumping Entries" );
            for (T me : this)
            {
                log.debug( "dump Entries> \"{0}\"", me );
            }
        }
    }

    /**
     * Returns the first node from the link list.
     *
     * @return the first node, null if the list is empty.
     */
    @SuppressWarnings({"unchecked"}) // Don't know how to resolve this with generics
    public T getFirst()
    {
        log.debug("returning first node");
        int shard = this.spoolShard.incrementAndGet();
        lock[shard].lock();
        try
        {
            DoubleLinkedListNode f = first[shard].next;
            return (T) (f == last[shard] ? null : f);
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    /**
     * Returns the last node from the link list, if there are any nodes.
     *
     * @return The last node, null if the list is empty.
     */
    @SuppressWarnings({"unchecked"}) // Don't know how to resolve this with generics
    public T getLast()
    {
        log.debug("returning last node");
        int shard = this.spoolShard.incrementAndGet();
        lock[shard].lock();
        try
        {
            DoubleLinkedListNode l = last[shard].prev;
            return (T) (l == first[shard] ? null : l);
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    /**
     * Moves an existing node to the start of the linked list.
     *
     * @param ln The node to set as the head.
     */
    public void makeFirst(final T ln)
    {
        int shard = ln.getShard();
        lock[shard].lock();
        try
        {
            if (ln.prev != null && ln.next != null)
            {
                ln.prev.next = ln.next;
                ln.next.prev = ln.prev;
                size--;
            }
            ln.prev = first[shard];
            ln.next = first[shard].next;
            first[shard].next.prev = ln;
            first[shard].next = ln;
            size++;
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    /**
     * Moves an existing node to the end of the linked list.
     *
     * @param ln The node to set as the tail.
     */
    public void makeLast(final T ln)
    {
        int shard = ln.getShard();
        lock[shard].lock();
        try
        {
            if (ln.prev != null && ln.next != null)
            {
                ln.prev.next = ln.next;
                ln.next.prev = ln.prev;
                size--;
            }
            ln.next = last[shard];
            ln.prev = last[shard].prev;
            last[shard].prev.next = ln;
            last[shard].prev = ln;
            size++;
        }
        finally
        {
            lock[shard].unlock();
        }
    }

    /**
     * Removes the specified node from the link list.
     *
     * @param me Description of the Parameter
     * @return true if an element was removed.
     */
    public boolean remove(final T me)
    {
        log.debug("removing node");
        int shard = me.getShard();
        lock[shard].lock();
        try
        {
            if (me.prev != null && me.next != null)
            {
                me.prev.next = me.next;
                me.next.prev = me.prev;
                me.prev = me.next = null;
                size--;
            }
        }
        finally
        {
            lock[shard].unlock();
        }

        return true;
    }

    /**
     * Remove all of the elements from the linked list implementation.
     */
    public void removeAll()
    {
        for (int i = 0; i < shards; i++)
        {
            lock[i].lock();
            try
            {
                DoubleLinkedListNode me = first[i].next;
                while (me != last[i] && me.next != null)
                {
                    DoubleLinkedListNode toRemove = me;
                    me = me.next;
                    toRemove.prev = null;
                    toRemove.next = null;
                }
                first[i].next = last[i];
                last[i].prev = first[i];
            }
            finally
            {
                lock[i].unlock();
            }
        }
        size = 0;
    }

    /**
     * Removes the specified node from the link list.
     *
     * @return The last node if there was one to remove.
     */
    public T removeLast()
    {
        log.debug("removing last node");
        T me = getLast();
        if (me != null)
        {
            remove(me);
        }

        return me;
    }

    /**
     * Returns the size of the list.
     *
     * @return int
     */
    public int size()
    {
        return size;
    }

    /**
     * Return an iterator over this list of lists
     *
     * @return the iterator
     */
    @Override
    @SuppressWarnings({"unchecked"}) // Don't know how to resolve this with generics
    public Iterator<T> iterator()
    {
        return new Iterator<>()
        {
            private int shard = 0;
            private T runner = (T) first[shard];

            @Override
            public synchronized boolean hasNext()
            {
                if (shard >= shards)
                {
                    return false;
                }

                boolean rollover = false;

                lock[shard].lock();
                try
                {
                    if (runner.next == null)
                    {
                        return false;
                    }
                    rollover = runner.next == last[shard];
                    if (!rollover)
                    {
                        return true;
                    }
                }
                finally
                {
                    lock[shard].unlock();
                }

                if (rollover)
                {
                    shard++;
                    if (shard < shards)
                    {
                        runner = (T) first[shard];
                    }

                    return hasNext();
                }

                return false;
            }

            @Override
            public synchronized T next()
            {
                lock[shard].lock();
                try
                {
                    runner = (T) runner.next;
                }
                finally
                {
                    lock[shard].unlock();
                }
                return runner;
            }
        };
    }
}
