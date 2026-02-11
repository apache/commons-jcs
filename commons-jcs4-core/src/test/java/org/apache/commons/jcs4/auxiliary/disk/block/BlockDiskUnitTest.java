package org.apache.commons.jcs4.auxiliary.disk.block;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.jcs4.utils.serialization.StandardSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the disk access layer of the Block Disk Cache.
 */
class BlockDiskUnitTest
{
    /** Data file. */
    private File rafDir;
    private BlockDisk disk;

    /**
     * Used to get the size for byte arrays that will take up the number of blocks specified.
     *
     * @param blockSize
     * @param numBlocks
     * @return num bytes.
     */
    private int getBytesForBlocksOfByteArrays( final int blockSize, final int numBlocks )
    {
        // byte arrays encur some bytes of serialization overhead.
        return blockSize * numBlocks - numBlocks * BlockDisk.HEADER_SIZE_BYTES - numBlocks * 14;
    }

    /**
     * Creates the base directory
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
        final String rootDirName = "target/test-sandbox/block";
        this.rafDir = new File( rootDirName );
        this.rafDir.mkdirs();
    }

    private void setUpBlockDisk(final String fileName) throws IOException
    {
        final File file = new File(rafDir, fileName + ".data");
        file.delete();
        this.disk = new BlockDisk(file, new StandardSerializer());
    }

    private void setUpBlockDisk(final String fileName, final int blockSize) throws IOException
    {
        final File file = new File(rafDir, fileName + ".data");
        file.delete();
        this.disk = new BlockDisk(file, blockSize, new StandardSerializer());
    }

    @AfterEach
    void tearDown()
        throws Exception
    {
        disk.close();
    }

    /**
     * Verify that it says we need two blocks if the total size will fit.
     *
     * @throws Exception
     */
    @Test
    void testCalculateBlocksNeededDouble()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testCalculateBlocksNeededDouble");

        // DO WORK
        final int result = disk.calculateTheNumberOfBlocksNeeded( new byte[disk.getBlockSizeBytes() * 2
            - 2 * BlockDisk.HEADER_SIZE_BYTES]);

        // Verify
        assertEquals( 2, result, "Wrong number of blocks" );
    }

    @Test
    void testJCS156()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testJCS156", 4096);
        final long offset = disk.calculateByteOffsetForBlockAsLong(Integer.MAX_VALUE);
        assertTrue( offset > 0, "Must not wrap round" );
        assertEquals(Integer.MAX_VALUE * 4096L, offset);
    }

    /**
     * Test writing an element that takes 128 blocks.  There was a byte in a for loop that limited the number to 127.  I fixed this.
     *
     * @throws Exception
     */
    @Test
    void testWrite_128BlockElement()
        throws Exception
    {
        // SETUP
        final int numBlocks = 128;

        setUpBlockDisk("testWrite_128BlockElement");

        // DO WORK
        // byte arrays encur 27 bytes of serialization overhead.
        final int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), numBlocks );
        final int[] blocks = disk.write( new byte[bytes]);

        // VERIFY
        assertEquals( numBlocks, disk.getNumberOfBlocks(), "Wrong number of blocks recorded." );
        assertEquals( numBlocks, blocks.length, "Wrong number of blocks returned." );
        assertEquals( 0, blocks[0], "Wrong block returned." );
    }

    /**
     * Test writing an element that takes two blocks.
     *
     * @throws Exception
     */
    @Test
    void testWrite_DoubleBlockElement()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteDoubleBlockElement");

        // DO WORK
        // byte arrays encur 27 bytes of serialization overhead.
        final int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), 2 );
        final int[] blocks = disk.write( new byte[bytes]);

        // VERIFY
        assertEquals( 2, disk.getNumberOfBlocks(), "Wrong number of blocks recorded." );
        assertEquals( 2, blocks.length, "Wrong number of blocks returned." );
        assertEquals( 0, blocks[0], "Wrong block returned." );
    }

    /**
     * Test writing a null object within a single block size.
     *
     * @throws Exception
     */
    @Test
    void testWrite_NullBlockElement()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWrite_NullBlockElement");

        // DO WORK
        final int[] blocks = disk.write( null );

        // VERIFY
        assertEquals( 1, disk.getNumberOfBlocks(), "Wrong number of blocks recorded." );
        assertEquals( 1, blocks.length, "Wrong number of blocks returned." );
        assertEquals( 0, blocks[0], "Wrong block returned." );
    }

    /**
     * Test writing an element within a single block size.
     *
     * @throws Exception
     */
    @Test
    void testWrite_SingleBlockElement()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWrite_SingleBlockElement");

        // DO WORK
        final int bytes = 1 * 1024;
        final int[] blocks = disk.write( new byte[bytes]);

        // VERIFY
        assertEquals( 1, disk.getNumberOfBlocks(), "Wrong number of blocks recorded." );
        assertEquals( 1, blocks.length, "Wrong number of blocks returned." );
        assertEquals( 0, blocks[0], "Wrong block returned." );
    }

    /**
     * Test writing two elements that each fit within a single block size.
     *
     * @throws Exception
     */
    @Test
    void testWrite_TwoSingleBlockElements()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWrite_TwoSingleBlockElements");

        // DO WORK
        final int bytes = 1 * 1024;
        final int[] blocks1 = disk.write( new byte[bytes]);
        final int[] blocks2 = disk.write( new byte[bytes]);

        // VERIFY
        assertEquals( 2, disk.getNumberOfBlocks(), "Wrong number of blocks recorded." );
        assertEquals( 1, blocks1.length, "Wrong number of blocks returned." );
        assertEquals( 0, blocks1[0], "Wrong block returned." );
        assertEquals( 1, blocks2.length, "Wrong number of blocks returned." );
        assertEquals( 1, blocks2[0], "Wrong block returned." );
    }

    /**
     * Verify that the block disk can handle a big string.
     *
     * @throws Exception
     */
    @Test
    void testWriteAndRead_BigString()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteAndRead_BigString", 4096); //1024

        String string = "This is my big string ABCDEFGH";
        final StringBuilder sb = new StringBuilder();
        sb.append( string );
        for ( int i = 0; i < 8; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        string = sb.toString();

        // DO WORK
        final int[] blocks = disk.write( string );
        final String result = (String) disk.read( blocks );

        // VERIFY
//        System.out.println( string );
//        System.out.println( result );
//        System.out.println( disk );
        assertEquals( string, result, "Wrong item retured." );
    }

    /**
     * Verify that the block disk can handle a big string.
     *
     * @throws Exception
     */
    @Test
    void testWriteAndRead_BigString2()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteAndRead_BigString", 47); //4096; //1024

        String string = "abcdefghijklmnopqrstuvwxyz1234567890";
        string += string;
        string += string;

        // DO WORK
        final int[] blocks = disk.write( string );
        final String result = (String) disk.read( blocks );

        // VERIFY
        assertEquals( string, result, "Wrong item retured." );
    }

    /**
     * Test writing and reading an element within a single block size.
     *
     * @throws Exception
     */
    @Test
    void testWriteAndRead_SingleBlockElement()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteAndRead_SingleBlockElement");

        // DO WORK
        final int bytes = 1 * 1024;
        final int[] blocks = disk.write( new byte[bytes]);

        final byte[] result = (byte[]) disk.read( blocks );

        // VERIFY
        assertEquals( new byte[bytes].length, result.length, "Wrong item retured." );
    }

    /**
     * Test writing and reading elements that do not fit within a single block.
     *
     * @throws Exception
     */
    @Test
    void testWriteAndReadMultipleMultiBlockElement()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteAndReadSingleBlockElement");

        // DO WORK
        final int numBlocksPerElement = 4;
        final int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), numBlocksPerElement );

        final int numElements = 100;
        for ( int i = 0; i < numElements; i++ )
        {
            final int[] blocks = disk.write( new byte[bytes]);
            final byte[] result = (byte[]) disk.read( blocks );

            // VERIFY
            assertEquals( new byte[bytes].length, result.length, "Wrong item retured." );
            assertEquals( numBlocksPerElement, blocks.length, "Wrong number of blocks returned." );
        }
    }

    /**
     * Test writing and reading elements that do not fit within a single block.
     *
     * @throws Exception
     */
    @Test
    void testWriteAndReadMultipleMultiBlockElement_setSize()
        throws Exception
    {
        // SETUP
        setUpBlockDisk("testWriteAndReadSingleBlockElement", 1024);

        // DO WORK
        final int numBlocksPerElement = 4;
        final int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), numBlocksPerElement );

        final int numElements = 100;
        final Random r = new Random(System.currentTimeMillis());
        final byte[] src = new byte[bytes];
        for ( int i = 0; i < numElements; i++ )
        {
            r.nextBytes(src);  // Ensure we don't just write zeros out
            final int[] blocks = disk.write( src );
            final byte[] result = (byte[]) disk.read( blocks );

            // VERIFY
            assertEquals( src.length, result.length, "Wrong item length retured." );
            assertEquals( numBlocksPerElement, blocks.length, "Wrong number of blocks returned." );

            // We check the array contents, too, to ensure we read back what we wrote out
            for (int j = 0; j < src.length; j++) {
                assertEquals( src[j], result[j], "Mismatch at offset " + j + " in attempt # " + ( i + 1 ) );
            }
        }
        assertEquals( numBlocksPerElement * numElements, disk.getNumberOfBlocks(),
                      "Wrong number of elements. " + disk );
    }
}
