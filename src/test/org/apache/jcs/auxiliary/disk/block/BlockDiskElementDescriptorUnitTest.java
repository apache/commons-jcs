package org.apache.jcs.auxiliary.disk.block;

import junit.framework.TestCase;

/**
 * Simple tests for the element descriptor
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskElementDescriptorUnitTest
    extends TestCase
{

    /**
     * Verify that the memory used per element is reasonable.
     * <p>
     * TODO figure out a more precise expectation.
     * <p>
     * @throws Exception
     */
    public void testMemorySize()
        throws Exception
    {
        // SETUP
        long memoryBefore = measureMemoryUse();
        System.out.println( "Before: " + memoryBefore );

        int numElements = 25000;
        BlockDiskElementDescriptor[] elements = new BlockDiskElementDescriptor[numElements];

        long memoryStart = measureMemoryUse();
        System.out.println( "Start: " + memoryStart );

        // DO WORK
        for ( int i = 0; i < numElements; i++ )
        {
            BlockDiskElementDescriptor descriptor = new BlockDiskElementDescriptor();
            descriptor.setKey( new Integer( i ) );
            descriptor.setBlocks( new int[] { 1, 2 } );
            elements[i] = descriptor;
        }

        // VERIFY
        long memoryEnd = measureMemoryUse();
        System.out.println( "End: " + memoryEnd );

        long diff = memoryEnd - memoryStart;
        System.out.println( "diff: " + diff );

        long perDiff = diff / numElements;
        System.out.println( "per diff: " + perDiff );

        // about 20 bytes each
        assertTrue( "Too much was used.", perDiff < 75 );
    }

    /**
     * Measure memory used by the VM.
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
