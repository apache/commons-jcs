package org.apache.commons.jcs3.engine.behavior;

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Defines the behavior for cache element serializers. This layer of abstraction allows us to plug
 * in different serialization mechanisms, such as a compressing standard serializer.
 * <p>
 * @author Aaron Smuts
 */
public interface IElementSerializer
{
    /**
     * Turns an object into a byte array.
     *
     * @param <T> the type of the object
     * @param obj the object to serialize
     * @return byte[] a byte array containing the serialized object
     * @throws IOException if serialization fails
     */
    <T> byte[] serialize( T obj )
        throws IOException;

    /**
     * Turns a byte array into an object.
     *
     * @param bytes data bytes
     * @param loader class loader to use
     * @return Object
     * @throws IOException if de-serialization fails
     * @throws ClassNotFoundException thrown if we don't know the object.
     */
    <T> T deSerialize( byte[] bytes, ClassLoader loader )
        throws IOException, ClassNotFoundException;

    /**
     * Convenience method to write serialized object into a stream.
     * The stream data will be prepended with a four-byte length prefix.
     *
     * @param <T> the type of the object
     * @param obj the object to serialize
     * @param os the output stream
     * @return the number of bytes written
     * @throws IOException if serialization or writing fails
     * @since 3.1
     */
    default <T> int serializeTo(T obj, OutputStream os)
        throws IOException
    {
        final byte[] serialized = serialize(obj);
        final ByteBuffer buffer = ByteBuffer.allocate(4 + serialized.length);
        buffer.putInt(serialized.length);
        buffer.put(serialized);
        buffer.flip();

        os.write(buffer.array());
        return buffer.capacity();
    }

    /**
     * Convenience method to write serialized object into a channel.
     * The stream data will be prepended with a four-byte length prefix.
     *
     * @param <T> the type of the object
     * @param obj the object to serialize
     * @param oc the output channel
     * @return the number of bytes written
     * @throws IOException if serialization or writing fails
     * @since 3.1
     */
    default <T> int serializeTo(T obj, WritableByteChannel oc)
        throws IOException
    {
        final byte[] serialized = serialize(obj);
        final ByteBuffer buffer = ByteBuffer.allocate(4 + serialized.length);
        buffer.putInt(serialized.length);
        buffer.put(serialized);
        buffer.flip();

        int count = 0;
        while (buffer.hasRemaining())
        {
            count += oc.write(buffer);
        }
        return count;
    }

    /**
     * Convenience method to write serialized object into an
     * asynchronous channel.
     * The stream data will be prepended with a four-byte length prefix.
     *
     * @param <T> the type of the object
     * @param obj the object to serialize
     * @param oc the output channel
     * @param writeTimeoutMs the write timeout im milliseconds
     * @return the number of bytes written
     * @throws IOException if serialization or writing fails
     * @since 3.1
     */
    default <T> int serializeTo(T obj, AsynchronousByteChannel oc, int writeTimeoutMs)
        throws IOException
    {
        final byte[] serialized = serialize(obj);
        final ByteBuffer buffer = ByteBuffer.allocate(4 + serialized.length);
        buffer.putInt(serialized.length);
        buffer.put(serialized);
        buffer.flip();

        int count = 0;
        while (buffer.hasRemaining())
        {
            Future<Integer> bytesWritten = oc.write(buffer);
            try
            {
                count += bytesWritten.get(writeTimeoutMs, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException | ExecutionException | TimeoutException e)
            {
                throw new IOException("Write timeout exceeded " + writeTimeoutMs, e);
            }
        }

        return count;
    }

    /**
     * Convenience method to read serialized object from a stream.
     * The method expects to find a four-byte length prefix in the
     * stream data.
     *
     * @param <T> the type of the object
     * @param is the input stream
     * @param loader class loader to use
     * @throws IOException if serialization or reading fails
     * @throws ClassNotFoundException thrown if we don't know the object.
     * @since 3.1
     */
    default <T> T deSerializeFrom(InputStream is, ClassLoader loader)
        throws IOException, ClassNotFoundException
    {
        final byte[] bufferSize = new byte[4];
        int read = is.read(bufferSize);
        if (read < 0)
        {
            throw new EOFException("End of stream reached");
        }
        assert read == bufferSize.length;
        ByteBuffer size = ByteBuffer.wrap(bufferSize);

        byte[] serialized = new byte[size.getInt()];
        read = is.read(serialized);
        assert read == serialized.length;

        return deSerialize(serialized, loader);
    }

    /**
     * Convenience method to read serialized object from a channel.
     * The method expects to find a four-byte length prefix in the
     * stream data.
     *
     * @param <T> the type of the object
     * @param ic the input channel
     * @param loader class loader to use
     * @throws IOException if serialization or reading fails
     * @throws ClassNotFoundException thrown if we don't know the object.
     * @since 3.1
     */
    default <T> T deSerializeFrom(ReadableByteChannel ic, ClassLoader loader)
        throws IOException, ClassNotFoundException
    {
        final ByteBuffer bufferSize = ByteBuffer.allocate(4);
        int read = ic.read(bufferSize);
        if (read < 0)
        {
            throw new EOFException("End of stream reached (length)");
        }
        assert read == bufferSize.capacity();
        bufferSize.flip();

        final ByteBuffer serialized = ByteBuffer.allocate(bufferSize.getInt());
        while (serialized.remaining() > 0)
        {
            read = ic.read(serialized);
            if (read < 0)
            {
                throw new EOFException("End of stream reached (object)");
            }
        }
        serialized.flip();

        return deSerialize(serialized.array(), loader);
    }

    /**
     * Convenience method to read serialized object from an
     * asynchronous channel.
     * The method expects to find a four-byte length prefix in the
     * stream data.
     *
     * @param <T> the type of the object
     * @param ic the input channel
     * @param readTimeoutMs the read timeout in milliseconds
     * @param loader class loader to use
     * @throws IOException if serialization or reading fails
     * @throws ClassNotFoundException thrown if we don't know the object.
     * @since 3.1
     */
    default <T> T deSerializeFrom(AsynchronousByteChannel ic, int readTimeoutMs, ClassLoader loader)
        throws IOException, ClassNotFoundException
    {
        final ByteBuffer bufferSize = ByteBuffer.allocate(4);
        Future<Integer> readFuture = ic.read(bufferSize);

        try
        {
            int read = readFuture.get(readTimeoutMs, TimeUnit.MILLISECONDS);
            if (read < 0)
            {
                throw new EOFException("End of stream reached (length)");
            }
            assert read == bufferSize.capacity();
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new IOException("Read timeout exceeded (length)" + readTimeoutMs, e);
        }

        bufferSize.flip();

        final ByteBuffer serialized = ByteBuffer.allocate(bufferSize.getInt());
        while (serialized.remaining() > 0)
        {
            readFuture = ic.read(serialized);
            try
            {
                int read = readFuture.get(readTimeoutMs, TimeUnit.MILLISECONDS);
                if (read < 0)
                {
                    throw new EOFException("End of stream reached (object)");
                }
            }
            catch (InterruptedException | ExecutionException | TimeoutException e)
            {
                throw new IOException("Read timeout exceeded (object)" + readTimeoutMs, e);
            }
        }

        serialized.flip();

        return deSerialize(serialized.array(), loader);
    }
}
