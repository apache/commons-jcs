package org.apache.jcs.auxiliary.disk.block;

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

import java.io.File;

import junit.framework.TestCase;

import org.apache.jcs.utils.serialization.StandardSerializer;

/**
 * Test for the disk access layer of the Block Disk Cache.
 * <p>
 * @author Aaron Smuts
 */
public class BlockDiskUnitTest
    extends TestCase
{
    /** data file. */
    private File rafDir;

    /**
     * Creates the base directory
     */
    public BlockDiskUnitTest()
    {
        String rootDirName = "target/test-sandbox/block";
        this.rafDir = new File( rootDirName );
        this.rafDir.mkdirs();
    }

    /**
     * Test writing an element within a single block size.
     * <p>
     * @throws Exception
     */
    public void testWriteSingleBlockElement()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteSingleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        int bytes = 1 * 1024;
        int[] blocks = disk.write( new byte[bytes] );

        // VERIFY
        System.out.println( "testWriteSingleBlockElement " + disk );
        assertEquals( "Wrong number of blocks recorded.", 1, disk.getNumberOfBlocks() );
        assertEquals( "Wrong number of blocks returned.", 1, blocks.length );
        assertEquals( "Wrong block returned.", 0, blocks[0] );
    }

    /**
     * Test writing and reading an element within a single block size.
     * <p>
     * @throws Exception
     */
    public void testWriteAndReadSingleBlockElement()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteAndReadSingleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        int bytes = 1 * 1024;
        int[] blocks = disk.write( new byte[bytes] );

        byte[] result = (byte[]) disk.read( blocks );

        // VERIFY
        assertEquals( "Wrong item retured.", new byte[bytes].length, result.length );
    }

    /**
     * Test writing two elements that each fit within a single block size.
     * <p>
     * @throws Exception
     */
    public void testWriteTwoSingleBlockElements()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteSingleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        int bytes = 1 * 1024;
        int[] blocks1 = disk.write( new byte[bytes] );
        int[] blocks2 = disk.write( new byte[bytes] );

        // VERIFY
        assertEquals( "Wrong number of blocks recorded.", 2, disk.getNumberOfBlocks() );
        assertEquals( "Wrong number of blocks returned.", 1, blocks1.length );
        assertEquals( "Wrong block returned.", 0, blocks1[0] );
        assertEquals( "Wrong number of blocks returned.", 1, blocks2.length );
        assertEquals( "Wrong block returned.", 1, blocks2[0] );
    }

    /**
     * Verify that it says we need two blocks if the total size will fit.
     * <p>
     * @throws Exception
     */
    public void testCalculateBlocksNeededDouble()
        throws Exception
    {
        // SETUP
        String fileName = "testCalculateBlocksNeededDouble";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        int result = disk.calculateTheNumberOfBlocksNeeded( new byte[disk.getBlockSizeBytes() * 2
            - ( 2 * BlockDisk.HEADER_SIZE_BYTES )] );

        // Verify
        assertEquals( "Wrong number of blocks", 2, result );
    }

    /**
     * Test writing an element that takes two blocks.
     * <p>
     * @throws Exception
     */
    public void testWriteDoubleBlockElement()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteDoubleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        // byte arrays encur 27 bytes of serialization overhead.
        int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), 2 );
        int[] blocks = disk.write( new byte[bytes] );

        // VERIFY
        System.out.println( "testWriteDoubleBlockElement " + disk );
        assertEquals( "Wrong number of blocks recorded.", 2, disk.getNumberOfBlocks() );
        assertEquals( "Wrong number of blocks returned.", 2, blocks.length );
        assertEquals( "Wrong block returned.", 0, blocks[0] );
    }

    /**
     * Test writing and reading elements that do not fit within a single block.
     * <p>
     * @throws Exception
     */
    public void testWriteAndReadMultipleMultiBlockElement()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteAndReadSingleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        BlockDisk disk = new BlockDisk( file, new StandardSerializer() );

        // DO WORK
        int numBlocksPerElement = 4;
        int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), numBlocksPerElement );

        int numElements = 100;
        for ( int i = 0; i < numElements; i++ )
        {
            int[] blocks = disk.write( new byte[bytes] );
            byte[] result = (byte[]) disk.read( blocks );

            // VERIFY
            assertEquals( "Wrong item retured.", new byte[bytes].length, result.length );
            assertEquals( "Wrong number of blocks returned.", numBlocksPerElement, blocks.length );
        }
        System.out.println( "testWriteAndReadMultipleMultiBlockElement " + disk );
    }

    /**
     * Test writing and reading elements that do not fit within a single block.
     * <p>
     * @throws Exception
     */
    public void testWriteAndReadMultipleMultiBlockElement_setSize()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteAndReadSingleBlockElement";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        int blockSizeBytes = 1024;
        BlockDisk disk = new BlockDisk( file, blockSizeBytes );

        // DO WORK
        int numBlocksPerElement = 4;
        int bytes = getBytesForBlocksOfByteArrays( disk.getBlockSizeBytes(), numBlocksPerElement );

        int numElements = 100;
        for ( int i = 0; i < numElements; i++ )
        {
            int[] blocks = disk.write( new byte[bytes] );
            byte[] result = (byte[]) disk.read( blocks );

            // VERIFY
            assertEquals( "Wrong item retured.", new byte[bytes].length, result.length );
            assertEquals( "Wrong number of blocks returned.", numBlocksPerElement, blocks.length );
        }
        System.out.println( "testWriteAndReadMultipleMultiBlockElement_setSize " + disk );
        assertEquals( "Wrong number of elements.", numBlocksPerElement * numElements, disk.getNumberOfBlocks() );
    }

    /**
     * Used to get the size for byte arrays that will take up the number of blocks specified.
     * <p>
     * @param blockSize
     * @param numBlocks
     * @return num bytes.
     */
    private int getBytesForBlocksOfByteArrays( int blockSize, int numBlocks )
    {
        // byte arrays encur some bytes of serialization overhead.
        return blockSize * numBlocks - ( numBlocks * BlockDisk.HEADER_SIZE_BYTES ) - ( numBlocks * 14 );
    }

    /**
     * Verify that the block disk can handle a big string.
     * <p>
     * @throws Exception
     */
    public void testWriteAndRead_BigString()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteAndRead_BigString";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        int blockSizeBytes = 4096;//1024;
        BlockDisk disk = new BlockDisk( file, blockSizeBytes, new StandardSerializer() );        

        String string = "This is my big string ABCDEFGH";
        StringBuffer sb = new StringBuffer();
        sb.append( string );
        for ( int i = 0; i < 8; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        string = sb.toString();

        // DO WORK
        int[] blocks = disk.write( string );
        String result = (String) disk.read( blocks );

        // VERIFY
        System.out.println( string );
        System.out.println( result );
        System.out.println( disk );        
        assertEquals( "Wrong item retured.", string, result );
    }
    
    /**
     * Verify that the block disk can handle a big string.
     * <p>
     * @throws Exception
     */
    public void testWriteAndRead_BigString2()
        throws Exception
    {
        // SETUP
        String fileName = "testWriteAndRead_BigString";
        File file = new File( rafDir, fileName + ".data" );
        file.delete();
        int blockSizeBytes = 47;//4096;//1024;
        BlockDisk disk = new BlockDisk( file, blockSizeBytes, new StandardSerializer() );        

        String string = "abcdefghijklmnopqrstuvwxyz1234567890";
        string += string;
        string += string;


        // DO WORK
        int[] blocks = disk.write( string );
        String result = (String) disk.read( blocks );

        // VERIFY 
        assertEquals( "Wrong item retured.", string, result );
    }    
}
