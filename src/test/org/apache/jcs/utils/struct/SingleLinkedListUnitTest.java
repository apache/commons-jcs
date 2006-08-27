package org.apache.jcs.utils.struct;

import junit.framework.TestCase;

/**
 * Tests for the simple linked list.
 * <p>
 * @author Aaron Smuts
 */
public class SingleLinkedListUnitTest
    extends TestCase
{
    /**
     * Verify that we get a null and that there are no exceptions.
     */
    public void testTakeFromEmptyList()
    {
        // SETUP
        SingleLinkedList list = new SingleLinkedList();

        // DO WORK
        Object result = list.takeFirst();

        // VERIFY
        assertNull( "Shounldn't have anything.", result );
    }

    /**
     * Verify FIFO behavior. Verifies that all items are removed.
     */
    public void testAddABunchAndTakeFromList()
    {
        // SETUP
        SingleLinkedList list = new SingleLinkedList();

        // DO WORK
        int numToPut = 100;
        for ( int i = 0; i < numToPut; i++ )
        {
            list.addLast( new Integer( i ) );
        }

        // VERIFY
        assertEquals( "Wrong nubmer in list.", numToPut, list.size() );

        for ( int i = 0; i < numToPut; i++ )
        {
            Object result = list.takeFirst();
            assertEquals( "Wrong value returned.", new Integer( i ), result );
        }

        // DO WORK
        Object result = list.takeFirst();

        // VERIFY
        assertNull( "Shounldn't have anything left.", result );
    }

    /**
     * Verify that after calling clear all items are removed adn the size is 0.
     */
    public void testAddABunchAndClear()
    {
        // SETUP
        SingleLinkedList list = new SingleLinkedList();

        // DO WORK
        int numToPut = 100;
        for ( int i = 0; i < numToPut; i++ )
        {
            list.addLast( new Integer( i ) );
        }

        // VERIFY
        assertEquals( "Wrong nubmer in list.", numToPut, list.size() );

        // DO WORK
        list.clear();
        Object result = list.takeFirst();

        // VERIFY
        assertEquals( "Wrong nubmer in list.", 0, list.size() );
        assertNull( "Shounldn't have anything left.", result );
    }
}
