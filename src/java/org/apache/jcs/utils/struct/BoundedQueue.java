package org.apache.jcs.utils.struct;

/**
 * This is a bounded queue. It only allows maxSize items.
 * <p>
 * @author Aaron Smuts
 */
public class BoundedQueue
{
    private int maxSize;

    private DoubleLinkedList list = new DoubleLinkedList();

    /**
     * Initialize the bounded queue.
     * <p>
     * @param maxSize
     */
    public BoundedQueue( int maxSize )
    {
        this.maxSize = maxSize;
    }

    /**
     * Adds an item to the end of the queue, which is the front of the list.
     * <p>
     * @param object
     */
    public void add( Object object )
    {
        if ( list.size() >= maxSize )
        {
            list.removeLast();
        }
        list.addFirst( new DoubleLinkedListNode( object ) );
    }

    /**
     * Takes the last of the underlying double linked list.
     * <p>
     * @return null if it is epmpty.
     */
    public Object take()
    {
        DoubleLinkedListNode node = list.removeLast();
        if ( node != null )
        {
            return node.getPayload();
        }
        return null;
    }
    
    /**
     * Return the number of items in the queue.
     * <p>
     * @return size
     */
    public int size()
    {
        return list.size();
    }
    
    /**
     * Return true if the size is <= 0;
     * <p>
     * @return true is size <= 0;
     */
    public boolean isEmpty()
    {
        return list.size() <= 0;
    }
}
