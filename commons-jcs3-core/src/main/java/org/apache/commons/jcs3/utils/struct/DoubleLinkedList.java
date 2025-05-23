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

import org.apache.commons.jcs3.log.Log;

/**
 * This is a generic thread safe double linked list. It's very simple and all the operations are so
 * quick that course grained synchronization is more than acceptable.
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // Don't know how to resolve this with generics
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
     * Default constructor.
     */
    public DoubleLinkedList()
    {
    }

    /**
     * Adds a new node to the start of the link list.
     *
     * @param me The feature to be added to the First
     */
    public synchronized void addFirst(final T me)
    {
        if ( last == null )
        {
            // empty list.
            last = me;
        }
        else
        {
            first.prev = me;
            me.next = first;
        }
        first = me;
        size++;
    }

    /**
     * Adds a new node to the end of the link list.
     *
     * @param me The feature to be added to the Last
     */
    public synchronized void addLast(final T me)
    {
        if ( first == null )
        {
            // empty list.
            first = me;
        }
        else
        {
            last.next = me;
            me.prev = last;
        }
        last = me;
        size++;
    }

    // ///////////////////////////////////////////////////////////////////
    /**
     * Dump the cache entries from first to list for debugging.
     */
    public synchronized void debugDumpEntries()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "dumping Entries" );
            for (T me = first; me != null; me = (T) me.next)
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
    public synchronized T getFirst()
    {
        log.debug( "returning first node" );
        return first;
    }

    /**
     * Returns the last node from the link list, if there are any nodes.
     *
     * @return The last node.
     */
    public synchronized T getLast()
    {
        log.debug( "returning last node" );
        return last;
    }

    /**
     * Moves an existing node to the start of the link list.
     *
     * @param ln The node to set as the head.
     */
    public synchronized void makeFirst(final T ln)
    {
        if ( ln.prev == null )
        {
            // already the first node. or not a node
            return;
        }
        // splice: remove it from the list
        ln.prev.next = ln.next;

        if ( ln.next == null )
        {
            // last but not the first.
            last = (T) ln.prev;
            last.next = null;
        }
        else
        {
            // neither the last nor the first.
            ln.next.prev = ln.prev;
        }
        first.prev = ln;
        ln.next = first;
        ln.prev = null;
        first = ln;
    }

    /**
     * Moves an existing node to the end of the link list.
     *
     * @param ln The node to set as the head.
     */
    public synchronized void makeLast(final T ln)
    {
        if ( ln.next == null )
        {
            // already the last node. or not a node
            return;
        }
        // splice: remove it from the list
        if ( ln.prev != null )
        {
            ln.prev.next = ln.next;
        }
        else
        {
            // first
            first = last;
        }

        if ( last != null )
        {
            last.next = ln;
        }
        ln.prev = last;
        ln.next = null;
        last = ln;
    }

    /**
     * Removes the specified node from the link list.
     *
     * @param me Description of the Parameter
     * @return true if an element was removed.
     */
    public synchronized boolean remove(final T me)
    {
        log.debug( "removing node" );

        if ( me.next == null )
        {
            if ( me.prev == null )
            {
                // Make sure it really is the only node before setting head and
                // tail to null. It is possible that we will be passed a node
                // which has already been removed from the list, in which case
                // we should ignore it

                if ( me == first && me == last )
                {
                    first = last = null;
                }
            }
            else
            {
                // last but not the first.
                last = (T) me.prev;
                last.next = null;
                me.prev = null;
            }
        }
        else if ( me.prev == null )
        {
            // first but not the last.
            first = (T) me.next;
            first.prev = null;
            me.next = null;
        }
        else
        {
            // neither the first nor the last.
            me.prev.next = me.next;
            me.next.prev = me.prev;
            me.prev = me.next = null;
        }
        size--;

        return true;
    }

    /**
     * Remove all of the elements from the linked list implementation.
     */
    public synchronized void removeAll()
    {
        for (T me = first; me != null; )
        {
            if ( me.prev != null )
            {
                me.prev = null;
            }
            me = (T) me.next;
        }
        first = last = null;
        // make sure this will work, could be add while this is happening.
        size = 0;
    }

    /**
     * Removes the specified node from the link list.
     *
     * @return The last node if there was one to remove.
     */
    public synchronized T removeLast()
    {
        log.debug( "removing last node" );
        final T temp = last;
        if ( last != null )
        {
            remove( last );
        }
        return temp;
    }

    /**
     * Returns the size of the list.
     *
     * @return int
     */
    public synchronized int size()
    {
        return size;
    }
}
