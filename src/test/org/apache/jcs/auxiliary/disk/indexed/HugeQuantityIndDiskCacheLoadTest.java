package org.apache.jcs.auxiliary.disk.indexed;

import junit.framework.TestCase;

import org.apache.jcs.JCS;

/**
 * Put a few hundred thousand entries in the disk cache.
 * 
 * @author Aaron Smuts
 * 
 */
public class HugeQuantityIndDiskCacheLoadTest
    extends TestCase
{

    /**
     * Test setup
     */
    public void setUp()
    {
        JCS.setConfigFilename( "/TestDiskCacheHuge.ccf" );
    }

    /**
     * Adds items to cache, gets them, and removes them. The item count is more
     * than the size of the memory cache, so items should spool to disk.
     * 
     * @param region
     *            Name of the region to access
     * 
     * @exception Exception
     *                If an error occurs
     */
    public void testLargeNumberOfItems()
        throws Exception
    {
        int items = 300000;
        String region = "testCache1";

        JCS jcs = JCS.getInstance( region );

        try
        {

            System.out.println( "Start: " + measureMemoryUse() );

            // Add items to cache

            for ( int i = 0; i <= items; i++ )
            {
                jcs.put( i + ":key", region + " data " + i );
            }

            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            System.out.println( "After put: " + measureMemoryUse() );

            Thread.sleep( 5000 );
            
            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            System.out.println( "After wait: " + measureMemoryUse() );

            // Test that all items are in cache

            for ( int i = 0; i <= items; i++ )
            {
                String value = (String) jcs.get( i + ":key" );

                assertEquals( region + " data " + i, value );
            }

            System.out.println( "After get: " + measureMemoryUse() );

            // // Remove all the items
            // for ( int i = 0; i <= items; i++ )
            // {
            // jcs.remove( i + ":key" );
            // }
            //
            // // Verify removal
            // for ( int i = 0; i <= items; i++ )
            // {
            // assertNull( "Removed key should be null: " + i + ":key" + "\n
            // stats " + jcs.getStats(), jcs.get( i + ":key" ) );
            // }

        }
        finally
        {
            // dump the stats to the report
            System.out.println( jcs.getStats() );
            System.out.println( "--------------------------" );
            System.out.println( "End: " + measureMemoryUse() );
        }
    }

    /**
     * Measure memory used by the VM.
     * 
     * @return
     * @throws InterruptedException
     */
    protected long measureMemoryUse()
        throws InterruptedException
    {
        System.gc();
        Thread.sleep( 3000 );
        System.gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
