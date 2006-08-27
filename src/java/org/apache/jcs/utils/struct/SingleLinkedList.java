package org.apache.jcs.utils.struct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an basic thread safe single linked list. It provides very limited functionality. It is
 * small and fast.
 * <p>
 * @author Aaron Smuts
 */
public class SingleLinkedList
{
    private static final Log log = LogFactory.getLog( SingleLinkedList.class );

    private Object lock = new Object();

    // the head of the queue
    private Node head = new Node();

    // the end of the queue
    private Node tail = head;

    private int size = 0;

    /**
     * Takes the first item off the list.
     * <p>
     * @return null if the list is empty.
     */
    public Object takeFirst()
    {
        synchronized ( lock )
        {
            // wait until there is something to read
            if ( head == tail )
            {
                return null;
            }

            Node node = head.next;

            Object value = node.payload;

            if ( log.isDebugEnabled() )
            {
                log.debug( "head.payload = " + head.payload );
                log.debug( "node.payload = " + node.payload );
            }

            // Node becomes the new head (head is always empty)

            node.payload = null;
            head = node;

            size--;
            return value;
        }
    }

    /**
     * Adds an item to the end of the list.
     * <p>
     * @param payload
     */
    public void addLast( Object payload )
    {
        Node newNode = new Node();

        newNode.payload = payload;

        synchronized ( lock )
        {
            size++;
            tail.next = newNode;
            tail = newNode;
        }
    }

    /**
     * Removes everything.
     */
    public void clear()
    {
        synchronized ( lock )
        {
            head = tail;
            size = 0;
        }
    }

    /**
     * The list is composed of nodes.
     * <p>
     * @author Aaron Smuts
     */
    private static class Node
    {
        Node next = null;

        Object payload;
    }

    /**
     * Returns the number of elements in the list.
     * <p>
     * @return number of items in the list.
     */
    public int size()
    {
        return size;
    }
}
