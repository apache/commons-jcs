package org.apache.commons.jcs4.utils.struct;

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
 * This is a generic double linked list. Thread safety is NOT provided by this class.
 * <p>
 * <b>THREAD SAFETY REQUIREMENT:</b> This class must be guarded by external synchronization
 * in calling code. All operations assume the caller holds appropriate locks
 * (e.g., ReentrantLock in {@link org.apache.commons.jcs4.engine.memory.AbstractDoubleLinkedListMemoryCache}).
 * <p>
 * This design eliminates double-locking problems when used with external locks and provides
 * O(1) performance for node repositioning operations.
 * <p>
 * <b>Example Usage:</b>
 * <pre>
 * Lock lock = new java.util.concurrent.locks.ReentrantLock();
 * DoubleLinkedList&lt;MyNode&gt; list = new DoubleLinkedList&lt;&gt;();
 *
 * // Caller must acquire lock before accessing
 * lock.lock();
 * try {
 *     list.addFirst(node);  // SAFE because lock is held
 * } finally {
 *     lock.unlock();
 * }
 * </pre>
 *
 * @see java.util.concurrent.locks.ReentrantLock
 * @see org.apache.commons.jcs4.engine.memory.AbstractDoubleLinkedListMemoryCache
 */
@SuppressWarnings({"unchecked", "rawtypes"}) // Don't know how to resolve this with generics
public class DoubleLinkedList<T extends DoubleLinkedListNode>
{
    /** The logger */
    private static final Log log = Log.getLog( DoubleLinkedList.class );

    /** Record size to avoid having to iterate */
    private int size;

    /** LRU double linked list head node */
    private T first;

    /** LRU double linked list tail node */
    private T last;

    /**
     * Construct DoubleLinkedList
     */
    public DoubleLinkedList()
    {
        this.first = (T) new DoubleLinkedListNode<T>(null);
        this.last = (T) new DoubleLinkedListNode<T>(null);
        this.first.next = this.last;
        this.last.prev = this.first;
    }

    /**
     * Adds a new node to the start of the link list.
     *
     * @param me The node to be added to the front
     */
    public void addFirst(final T me)
    {
        me.prev = first;
        me.next = first.next;
        first.next.prev = me;
        first.next = me;
        size++;
    }

    /**
     * Adds a new node to the end of the link list.
     *
     * @param me The node to be added to the end
     */
    public void addLast(final T me)
    {
        me.next = last;
        me.prev = last.prev;
        last.prev.next = me;
        last.prev = me;
        size++;
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
            for (T me = (T) first.next; me != last; me = (T) me.next)
            {
                log.debug( "dump Entries> payload= \"{0}\"", me.getPayload() );
            }
        }
    }

    /**
     * Removes the specified node from the link list.
     *
     * @return DoubleLinkedListNode, the first node.
     */
    public T getFirst()
    {
        log.trace( "returning first node" );
        return (T) first.next;
    }

    /**
     * Returns the last node from the link list, if there are any nodes.
     *
     * @return The last node.
     */
    public T getLast()
    {
        log.trace( "returning last node" );
        return (T) last.prev;
    }

    /**
     * Moves an existing node to the start of the linked list.
     *
     * @param ln The node to set as the head.
     */
    public void makeFirst(final T ln)
    {
        ln.prev.next = ln.next;
        ln.next.prev = ln.prev;
        ln.prev = first;
        ln.next = first.next;
        first.next.prev = ln;
        first.next = ln;
    }

    /**
     * Moves an existing node to the end of the linked list.
     *
     * @param ln The node to set as the tail.
     */
    public void makeLast(final T ln)
    {
        ln.prev.next = ln.next;
        ln.next.prev = ln.prev;
        ln.next = last;
        ln.prev = last.prev;
        last.prev.next = ln;
        last.prev = ln;
    }

    /**
     * Removes the specified node from the link list.
     *
     * @param me Description of the Parameter
     * @return true if an element was removed.
     */
    public boolean remove(final T me)
    {
        log.trace("removing node");
        me.prev.next = me.next;
        me.next.prev = me.prev;
        me.prev = me.next = null;
        size--;

        return true;
    }

    /**
     * Remove all of the elements from the linked list implementation.
     */
    public void removeAll()
    {
        for (T me = (T) first.next; me != null;)
        {
            me.prev = null;
            me.next = null;
            me = (T) me.next;
        }
        first.next = last;
        last.prev = first;
        size = 0;
    }

    /**
     * Removes the specified node from the link list.
     *
     * @return The last node if there was one to remove.
     */
    public T removeLast()
    {
        log.trace("removing last node");
        final T temp = (T) last.prev;
        if (last != first)
        {
            remove(temp);
        }
        return temp;
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
}
