package org.apache.commons.jcs3.auxiliary.disk.indexed;

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

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;

/** Provides thread safe access to the underlying random access file. */
public class IndexedDisk implements AutoCloseable
{
    /** The size of the header that indicates the amount of data stored in an occupied block. */
    public static final byte HEADER_SIZE_BYTES = 4;

    /** The logger */
    private static final Log log = Log.getLog(IndexedDisk.class);

    /** The serializer. */
    private final IElementSerializer elementSerializer;

    /** The path to the log directory. */
    private final String filepath;

    /** The data file. */
    private final FileChannel fc;

    /**
     * Constructor for the Disk object
     *
     * @param file
     * @param elementSerializer
     * @throws IOException
     */
    public IndexedDisk(final File file, final IElementSerializer elementSerializer)
        throws IOException
    {
        this.filepath = file.getAbsolutePath();
        this.elementSerializer = elementSerializer;
        this.fc = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
    }

    /**
     * Closes the raf.
     *
     * @throws IOException
     */
    @Override
    public void close()
        throws IOException
    {
        fc.close();
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
     * Tests if the length is 0.
     * @return true if the if the length is 0.
     * @throws IOException If an I/O error occurs.
     * @since 3.1
     */
    protected boolean isEmpty() throws IOException
    {
        return length() == 0;
    }

    /**
     * Returns the raf length.
     *
     * @return the length of the file.
     * @throws IOException If an I/O error occurs.
     */
    protected long length()
        throws IOException
    {
        return fc.size();
    }

    /**
     * Moves the data stored from one position to another. The descriptor's position is updated.
     *
     * @param ded
     * @param newPosition
     * @throws IOException
     */
    protected void move(final IndexedDiskElementDescriptor ded, final long newPosition)
        throws IOException
    {
        final ByteBuffer datalength = ByteBuffer.allocate(HEADER_SIZE_BYTES);
        fc.read(datalength, ded.pos);
        datalength.flip();
        final int length = datalength.getInt();

        if (length != ded.len)
        {
            throw new IOException("Mismatched memory and disk length (" + length + ") for " + ded);
        }

        // TODO: more checks?

        long readPos = ded.pos;
        long writePos = newPosition;

        // header len + data len
        int remaining = HEADER_SIZE_BYTES + length;
        final ByteBuffer buffer = ByteBuffer.allocate(16384);

        while (remaining > 0)
        {
            // chunk it
            final int chunkSize = Math.min(remaining, buffer.capacity());
            buffer.limit(chunkSize);
            fc.read(buffer, readPos);
            buffer.flip();
            fc.write(buffer, writePos);
            buffer.clear();

            writePos += chunkSize;
            readPos += chunkSize;
            remaining -= chunkSize;
        }

        ded.pos = newPosition;
    }

    /**
     * This reads an object from the given starting position on the file.
     * <p>
     * The first four bytes of the record should tell us how long it is. The data is read into a byte
     * array and then an object is constructed from the byte array.
     *
     * @return Serializable
     * @param ded
     * @throws IOException
     * @throws ClassNotFoundException
     */
    protected <T> T readObject(final IndexedDiskElementDescriptor ded)
        throws IOException, ClassNotFoundException
    {
        String message = null;
        boolean corrupted = false;
        final long fileLength = fc.size();
        if (ded.pos > fileLength)
        {
            corrupted = true;
            message = "Record " + ded + " starts past EOF.";
        }
        else
        {
            final ByteBuffer datalength = ByteBuffer.allocate(HEADER_SIZE_BYTES);
            fc.read(datalength, ded.pos);
            datalength.flip();
            final int datalen = datalength.getInt();
            if (ded.len != datalen)
            {
                corrupted = true;
                message = "Record " + ded + " does not match data length on disk (" + datalen + ")";
            }
            else if (ded.pos + ded.len > fileLength)
            {
                corrupted = true;
                message = "Record " + ded + " exceeds file length.";
            }
        }

        if (corrupted)
        {
            log.warn("\n The file is corrupt: \n {0}", message);
            throw new IOException("The File Is Corrupt, need to reset");
        }

        final ByteBuffer data = ByteBuffer.allocate(ded.len);
        fc.read(data, ded.pos + HEADER_SIZE_BYTES);
        data.flip();

        return elementSerializer.deSerialize(data.array(), null);
    }

    /**
     * Sets the raf to empty.
     *
     * @throws IOException
     */
    protected synchronized void reset()
        throws IOException
    {
        log.debug("Resetting Indexed File [{0}]", filepath);
        fc.truncate(0);
        fc.force(true);
    }

    /**
     * Truncates the file to a given length.
     *
     * @param length the new length of the file
     * @throws IOException
     */
    protected void truncate(final long length)
        throws IOException
    {
        log.info("Truncating file [{0}] to {1}", filepath, length);
        fc.truncate(length);
    }

    /**
     * Writes the given byte array to the Disk at the specified position.
     *
     * @param data
     * @param ded
     * @return true if we wrote successfully
     * @throws IOException
     */
    protected boolean write(final IndexedDiskElementDescriptor ded, final byte[] data)
        throws IOException
    {
        final long pos = ded.pos;
        if (log.isTraceEnabled())
        {
            log.trace("write> pos={0}", pos);
            log.trace("{0} -- data.length = {1}", fc, data.length);
        }

        if (data.length != ded.len)
        {
            throw new IOException("Mismatched descriptor and data lengths");
        }

        final ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_SIZE_BYTES);
        headerBuffer.putInt(data.length);
        // write the header
        headerBuffer.flip();
        int written = fc.write(headerBuffer, pos);
        assert written == HEADER_SIZE_BYTES;

        //write the data
        final ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        written = fc.write(dataBuffer, pos + HEADER_SIZE_BYTES);

        return written == data.length;
    }

    /**
     * Serializes the object and write it out to the given position.
     * <p>
     * TODO: make this take a ded as well.
     * @param obj
     * @param pos
     * @throws IOException
     */
    protected <T> void writeObject(final T obj, final long pos)
        throws IOException
    {
        final byte[] data = elementSerializer.serialize(obj);
        write(new IndexedDiskElementDescriptor(pos, data.length), data);
    }
}
