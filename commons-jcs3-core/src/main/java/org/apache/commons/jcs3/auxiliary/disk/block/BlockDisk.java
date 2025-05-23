package org.apache.commons.jcs3.auxiliary.disk.block;

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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.utils.serialization.StandardSerializer;

/**
 * This class manages reading an writing data to disk. When asked to write a value, it returns a
 * block array. It can read an object from the block numbers in a byte array.
 */
public class BlockDisk implements AutoCloseable
{
    /** The logger */
    private static final Log log = Log.getLog(BlockDisk.class);

    /** The size of the header that indicates the amount of data stored in an occupied block. */
    public static final byte HEADER_SIZE_BYTES = 4;
    // 4 bytes is the size used for ByteBuffer.putInt(int value) and ByteBuffer.getInt()

    /** Defaults to 4kb */
    private static final int DEFAULT_BLOCK_SIZE_BYTES = 4 * 1024;

    /** Size of the blocks */
    private final int blockSizeBytes;

    /**
     * the total number of blocks that have been used. If there are no free, we will use this to
     * calculate the position of the next block.
     */
    private final AtomicInteger numberOfBlocks = new AtomicInteger();

    /** Empty blocks that can be reused. */
    private final ConcurrentLinkedQueue<Integer> emptyBlocks = new ConcurrentLinkedQueue<>();

    /** The serializer. */
    private final IElementSerializer elementSerializer;

    /** Location of the spot on disk */
    private final String filepath;

    /** File channel for multiple concurrent reads and writes */
    private final FileChannel fc;

    /** How many bytes have we put to disk */
    private final AtomicLong putBytes = new AtomicLong();

    /** How many items have we put to disk */
    private final AtomicLong putCount = new AtomicLong();

    /**
     * Constructor for the Disk object
     *
     * @param file
     * @param elementSerializer
     * @throws IOException
     */
    public BlockDisk(final File file, final IElementSerializer elementSerializer)
        throws IOException
    {
        this(file, DEFAULT_BLOCK_SIZE_BYTES, elementSerializer);
    }

    /**
     * Creates the file and set the block size in bytes.
     *
     * @param file
     * @param blockSizeBytes
     * @throws IOException
     */
    public BlockDisk(final File file, final int blockSizeBytes)
        throws IOException
    {
        this(file, blockSizeBytes, new StandardSerializer());
    }

    /**
     * Creates the file and set the block size in bytes.
     *
     * @param file
     * @param blockSizeBytes
     * @param elementSerializer
     * @throws IOException
     */
    public BlockDisk(final File file, final int blockSizeBytes, final IElementSerializer elementSerializer)
        throws IOException
    {
        this.filepath = file.getAbsolutePath();
        this.fc = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        this.numberOfBlocks.set((int) Math.ceil(1f * this.fc.size() / blockSizeBytes));

        log.info("Constructing BlockDisk, blockSizeBytes [{0}]", blockSizeBytes);

        this.blockSizeBytes = blockSizeBytes;
        this.elementSerializer = elementSerializer;
    }

    /**
     * Allocate a given number of blocks from the available set
     *
     * @param numBlocksNeeded
     * @return an array of allocated blocks
     */
    private int[] allocateBlocks(final int numBlocksNeeded)
    {
        assert numBlocksNeeded >= 1;

        final int[] blocks = new int[numBlocksNeeded];
        // get them from the empty list or take the next one
        for (int i = 0; i < numBlocksNeeded; i++)
        {
            Integer emptyBlock = emptyBlocks.poll();
            if (emptyBlock == null)
            {
                emptyBlock = Integer.valueOf(numberOfBlocks.getAndIncrement());
            }
            blocks[i] = emptyBlock.intValue();
        }

        return blocks;
    }

    /**
     * Calculates the file offset for a particular block.
     *
     * @param block number
     * @return the byte offset for this block in the file as a long
     * @since 2.0
     */
    protected long calculateByteOffsetForBlockAsLong(final int block)
    {
        return (long) block * blockSizeBytes;
    }

    /**
     * The number of blocks needed.
     *
     * @param data
     * @return the number of blocks needed to store the byte array
     */
    protected int calculateTheNumberOfBlocksNeeded(final byte[] data)
    {
        final int dataLength = data.length;

        final int oneBlock = blockSizeBytes - HEADER_SIZE_BYTES;

        // takes care of 0 = HEADER_SIZE_BYTES + blockSizeBytes
        if (dataLength <= oneBlock)
        {
            return 1;
        }

        int dividend = dataLength / oneBlock;

        if (dataLength % oneBlock != 0)
        {
            dividend++;
        }
        return dividend;
    }

    /**
     * Closes the file.
     *
     * @throws IOException
     */
    @Override
    public void close()
        throws IOException
    {
        this.numberOfBlocks.set(0);
        this.emptyBlocks.clear();
        fc.close();
    }

    /**
     * Add these blocks to the emptyBlock list.
     *
     * @param blocksToFree
     */
    protected void freeBlocks(final int[] blocksToFree)
    {
        if (blocksToFree != null)
        {
            for (short i = 0; i < blocksToFree.length; i++)
            {
                emptyBlocks.offer(Integer.valueOf(blocksToFree[i]));
            }
        }
    }

    /**
     * @return the average size of the an element inserted.
     */
    protected long getAveragePutSizeBytes()
    {
        final long count = this.putCount.get();

        if (count == 0)
        {
            return 0;
        }
        return this.putBytes.get() / count;
    }

    /**
     * Return the amount to put in each block. Fill them all the way, minus the header.
     *
     * @param complete
     * @param numBlocksNeeded
     * @return byte[][]
     */
    protected byte[][] getBlockChunks(final byte[] complete, final int numBlocksNeeded)
    {
        final byte[][] chunks = new byte[numBlocksNeeded][];

        if (numBlocksNeeded == 1)
        {
            chunks[0] = complete;
        }
        else
        {
            final int maxChunkSize = this.blockSizeBytes - HEADER_SIZE_BYTES;
            final int totalBytes = complete.length;
            int totalUsed = 0;
            for (short i = 0; i < numBlocksNeeded; i++)
            {
                // use the max that can be written to a block or whatever is left in the original
                // array
                final int chunkSize = Math.min(maxChunkSize, totalBytes - totalUsed);
                final byte[] chunk = new byte[chunkSize];
                // copy from the used position to the chunk size on the complete array to the chunk
                // array.
                System.arraycopy(complete, totalUsed, chunk, 0, chunkSize);
                chunks[i] = chunk;
                totalUsed += chunkSize;
            }
        }

        return chunks;
    }

    /**
     * @return the blockSizeBytes.
     */
    protected int getBlockSizeBytes()
    {
        return blockSizeBytes;
    }

    /**
     * @return the number of empty blocks.
     */
    protected int getEmptyBlocks()
    {
        return this.emptyBlocks.size();
    }

    /**
     * This is used for debugging.
     *
     * @return the file path.
     */
    protected String getFilePath()
    {
        return filepath;
    }

    /**
     * @return the numberOfBlocks.
     */
    protected int getNumberOfBlocks()
    {
        return numberOfBlocks.get();
    }

    /**
     * Returns the file length.
     *
     * @return the size of the file.
     * @throws IOException
     */
    protected long length()
        throws IOException
    {
        return fc.size();
    }

    /**
     * Reads an object that is located in the specified blocks.
     *
     * @param blockNumbers
     * @return the object instance
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected <T> T read(final int[] blockNumbers)
        throws IOException, ClassNotFoundException
    {
        final ByteBuffer data;

        if (blockNumbers.length == 1)
        {
            data = readBlock(blockNumbers[0]);
        }
        else
        {
            data = ByteBuffer.allocate(blockNumbers.length * getBlockSizeBytes());
            // get all the blocks into data
            for (short i = 0; i < blockNumbers.length; i++)
            {
                final ByteBuffer chunk = readBlock(blockNumbers[i]);
                data.put(chunk);
            }

            data.flip();
        }

        log.debug("read, total post combination data.length = {0}", () -> data.limit());

        return elementSerializer.deSerialize(data.array(), null);
    }

    /**
     * This reads the occupied data in a block.
     * <p>
     * The first four bytes of the record should tell us how long it is. The data is read into a
     * byte array and then an object is constructed from the byte array.
     *
     * @return byte[]
     * @param block
     * @throws IOException
     */
    private ByteBuffer readBlock(final int block)
        throws IOException
    {
        int datalen = 0;

        String message = null;
        boolean corrupted = false;
        final long fileLength = fc.size();

        final long position = calculateByteOffsetForBlockAsLong(block);
//        if (position > fileLength)
//        {
//            corrupted = true;
//            message = "Record " + position + " starts past EOF.";
//        }
//        else
        {
            final ByteBuffer datalength = ByteBuffer.allocate(HEADER_SIZE_BYTES);
            fc.read(datalength, position);
            datalength.flip();
            datalen = datalength.getInt();
            if (position + datalen > fileLength)
            {
                corrupted = true;
                message = "Record " + position + " exceeds file length.";
            }
        }

        if (corrupted)
        {
            log.warn("\n The file is corrupt: \n {0}", message);
            throw new IOException("The File Is Corrupt, need to reset");
        }

        final ByteBuffer data = ByteBuffer.allocate(datalen);
        fc.read(data, position + HEADER_SIZE_BYTES);
        data.flip();

        return data;
    }

    /**
     * Resets the file.
     *
     * @throws IOException
     */
    protected synchronized void reset()
        throws IOException
    {
        this.numberOfBlocks.set(0);
        this.emptyBlocks.clear();
        fc.truncate(0);
        fc.force(true);
    }

    /**
     * For debugging only.
     *
     * @return String with details.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("\nBlock Disk ");
        buf.append("\n  Filepath [" + filepath + "]");
        buf.append("\n  NumberOfBlocks [" + this.numberOfBlocks.get() + "]");
        buf.append("\n  BlockSizeBytes [" + this.blockSizeBytes + "]");
        buf.append("\n  Put Bytes [" + this.putBytes + "]");
        buf.append("\n  Put Count [" + this.putCount + "]");
        buf.append("\n  Average Size [" + getAveragePutSizeBytes() + "]");
        buf.append("\n  Empty Blocks [" + getEmptyBlocks() + "]");
        try
        {
            buf.append("\n  Length [" + length() + "]");
        }
        catch (final IOException e)
        {
            // swallow
        }
        return buf.toString();
    }

    /**
     * This writes an object to disk and returns the blocks it was stored in.
     * <p>
     * The program flow is as follows:
     * <ol>
     * <li>Serialize the object.</li>
     * <li>Determine the number of blocks needed.</li>
     * <li>Look for free blocks in the emptyBlock list.</li>
     * <li>If there were not enough in the empty list. Take the nextBlock and increment it.</li>
     * <li>If the data will not fit in one block, create sub arrays.</li>
     * <li>Write the subarrays to disk.</li>
     * <li>If the process fails we should decrement the block count if we took from it.</li>
     * </ol>
     * @param object
     * @return the blocks we used.
     * @throws IOException
     */
    protected <T> int[] write(final T object)
        throws IOException
    {
        // serialize the object
        final byte[] data = elementSerializer.serialize(object);

        log.debug("write, total pre-chunking data.length = {0}", data.length);

        this.putBytes.addAndGet(data.length);
        this.putCount.incrementAndGet();

        // figure out how many blocks we need.
        final int numBlocksNeeded = calculateTheNumberOfBlocksNeeded(data);

        log.debug("numBlocksNeeded = {0}", numBlocksNeeded);

        // allocate blocks
        final int[] blocks = allocateBlocks(numBlocksNeeded);

        int offset = 0;
        final int maxChunkSize = blockSizeBytes - HEADER_SIZE_BYTES;
        final ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE_BYTES);
        final ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        for (int i = 0; i < numBlocksNeeded; i++)
        {
            headerBuffer.clear();
            final int length = Math.min(maxChunkSize, data.length - offset);
            headerBuffer.putInt(length);
            headerBuffer.flip();

            dataBuffer.position(offset).limit(offset + length);
            final ByteBuffer slice = dataBuffer.slice();

            final long position = calculateByteOffsetForBlockAsLong(blocks[i]);
            // write the header
            int written = fc.write(headerBuffer, position);
            assert written == HEADER_SIZE_BYTES;

            //write the data
            written = fc.write(slice, position + HEADER_SIZE_BYTES);
            assert written == length;

            offset += length;
        }

        //fc.force(false);

        return blocks;
    }
}
