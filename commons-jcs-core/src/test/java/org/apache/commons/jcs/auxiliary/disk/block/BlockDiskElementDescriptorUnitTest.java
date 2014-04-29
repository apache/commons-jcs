package org.apache.commons.jcs.auxiliary.disk.block;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
//        System.out.println( "Before: " + memoryBefore );

        int numElements = 25000;
        @SuppressWarnings("unchecked")
        BlockDiskElementDescriptor<Integer>[] elements = new BlockDiskElementDescriptor[numElements];

        long memoryStart = measureMemoryUse();
//        System.out.println( "Start: " + memoryStart );

        // DO WORK
        for ( int i = 0; i < numElements; i++ )
        {
            BlockDiskElementDescriptor<Integer> descriptor = new BlockDiskElementDescriptor<Integer>();
            descriptor.setKey( Integer.valueOf( i ) );
            descriptor.setBlocks( new int[] { 1, 2 } );
            elements[i] = descriptor;
        }

        // VERIFY
        long memoryEnd = measureMemoryUse();
//        System.out.println( "End: " + memoryEnd );

        long diff = memoryEnd - memoryStart;
//        System.out.println( "diff: " + diff );

        long perDiff = diff / numElements;
//        System.out.println( "per diff: " + perDiff );

        // about 20 bytes each
        assertTrue( "Too much was used: " + perDiff + " >= 75", perDiff < 75 );
    }

    /**
     * Measure memory used by the VM.
     * @return long
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
