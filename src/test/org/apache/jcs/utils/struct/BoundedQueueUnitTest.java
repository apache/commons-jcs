package org.apache.jcs.utils.struct;

import junit.framework.TestCase;

/**
 * Unit tests for the bounded queue.
 * <p>
 * @author Aaron Smuts
 */
public class BoundedQueueUnitTest
    extends TestCase
{
    /**
     * Verify null returned for empty.
     */
    public void testTakeLastEmpty()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        Object result = queue.take();

        // VERIFY
        assertNull( "Result should be null", result );
    }

    /**
     * Verify that the queue returns the number of elements and the it does not exceed the max.
     */
    public void testSize()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        for ( int i = 0; i < maxSize * 2; i++ )
        {
            queue.add( "adfadsf sad " + i );
        }

        int result = queue.size();

        // VERIFY
        assertEquals( "Result size not as expected", maxSize, result );
    }

    /**
     * Verify that the items come back in the order put in.
     */
    public void testFIFOOrderedTake()
    {
        // SETUP
        int maxSize = 10;
        BoundedQueue queue = new BoundedQueue( maxSize );

        // DO WORK
        for ( int i = 0; i < maxSize; i++ )
        {
            queue.add( String.valueOf( i ) );
        }


        // VERIFY
        
        for ( int i = 0; i < maxSize; i++ )
        {
            String result = (String)queue.take();
            assertEquals( "Result not as expected",  String.valueOf( i ) ,  result  );
        }        
    }
}
