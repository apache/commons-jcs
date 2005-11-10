package org.apache.jcs.engine.memory.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a generic thread safe double linked list.
 */
public class DoubleLinkedList
{

    // record size to avoid having to iterate
    int size = 0;

    private final static Log log = LogFactory.getLog( DoubleLinkedList.class );

    // LRU double linked list head/tail nodes
    private DoubleLinkedListNode first;

    private DoubleLinkedListNode last;

    public DoubleLinkedList()
    {
    }

    /**
     * Adds a new node to the end of the link list.
     * 
     * @param me
     *            The feature to be added to the Last
     */
    public void addLast( DoubleLinkedListNode me )
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

    /**
     * Adds a new node to the start of the link list.
     * 
     * @param me
     *            The feature to be added to the First
     */
    public synchronized void addFirst( DoubleLinkedListNode me )
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
        return;
    }

    /**
     * Removes the specified node from the link list.
     * @return
     *  
     */
    public DoubleLinkedListNode getLast()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "returning last node" );
        }
        return last;
    }

    /**
     * Removes the specified node from the link list.
     * @return
     *  
     */
    public DoubleLinkedListNode getFirst()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "returning fist node" );
        }
        return first;
    }

    /**
     * Moves an existing node to the start of the link list.
     * 
     * @param ln
     *            Description of the Parameter
     */
    public synchronized void makeFirst( DoubleLinkedListNode ln )
    {

        if ( ln.prev == null )
        {
            // already the first node. or not a node
            return;
        }
        ln.prev.next = ln.next;

        if ( ln.next == null )
        {
            // last but not the first.
            last = ln.prev;
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
     * Remove all of the elements from the linked list implementation.
     */
    public synchronized void removeAll()
    {

        for ( DoubleLinkedListNode me = first; me != null; )
        {
            if ( me.prev != null )
            {
                me.prev = null;
            }
            DoubleLinkedListNode next = me.next;
            me = next;
        }
        first = last = null;
        // make sure this will work, could be add while this is happening.
        size = 0;
    }

    /**
     * Removes the specified node from the link list.
     * 
     * @param me
     *            Description of the Parameter
     * @return
     */
    public synchronized boolean remove( DoubleLinkedListNode me )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing node" );
        }

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
                last = me.prev;
                last.next = null;
                me.prev = null;
            }
        }
        else if ( me.prev == null )
        {
            // first but not the last.
            first = me.next;
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
     * Removes the specified node from the link list.
     * @return
     *  
     */
    public DoubleLinkedListNode removeLast()
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "removing last node" );
        }
        DoubleLinkedListNode temp = last;
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
    public int size()
    {
        return size;
    }

    /////////////////////////////////////////////////////////////////////
    /**
     * Dump the cache entries from first to list for debugging.
     */
    public void debugDumpEntries()
    {
        log.debug( "dumping Entries" );
        for ( DoubleLinkedListNode me = first; me != null; me = me.next )
        {
            log.debug( "dump Entries> payload= '" + me.payload + "'" );
        }
    }

}
