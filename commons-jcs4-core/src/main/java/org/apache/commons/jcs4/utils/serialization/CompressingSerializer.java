package org.apache.commons.jcs4.utils.serialization;

import java.io.ByteArrayOutputStream;

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

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.jcs4.engine.behavior.IElementSerializer;

/**
 * Performs default serialization and de-serialization. It gzips the value.
 */
public class CompressingSerializer extends StandardSerializer
{
    /** Wrapped serializer */
    private final IElementSerializer serializer;

    /**
     * Default constructor
     */
    public CompressingSerializer()
    {
        this(new StandardSerializer());
    }

    /**
     * Wrapper constructor
     *
     * @param serializer The wrapped serializer
     * @since 3.1
     */
    public CompressingSerializer(final IElementSerializer serializer)
    {
        this.serializer = serializer;
    }

    /**
     * Uses default de-serialization to turn a byte array into an object. Decompresses the value
     * first. All exceptions are converted into IOExceptions.
     *
     * @param data data bytes
     * @param loader class loader to use
     * @return Object
     * @throws IOException on i/o problem
     * @throws ClassNotFoundException if class is not found during deserialization
     */
    @Override
    public <T> T deSerialize( final byte[] data, final ClassLoader loader )
        throws IOException, ClassNotFoundException
    {
        if ( data == null )
        {
            return null;
        }

        final byte[] decompressedByteArray = decompressByteArray( data, 1024 );
        return serializer.deSerialize(decompressedByteArray, loader);
    }

    /**
     * Serializes an object using default serialization. Compresses the byte array.
     *
     * @param obj object
     * @return byte[]
     * @throws IOException on i/o problem
     */
    @Override
    public <T> byte[] serialize( final T obj )
        throws IOException
    {
        final byte[] uncompressed = serializer.serialize(obj);
        return compressByteArray( uncompressed, 1024 );
    }

    /**
     * Compress the byte array passed
     *
     * @param input byte array
     * @param bufferLength buffer length
     * @return compressed byte array
     * @throws IOException thrown if we can't close the output stream
     */
    protected static byte[] compressByteArray( final byte[] input, final int bufferLength )
        throws IOException
    {
        // Compressor with highest level of compression
        final Deflater compressor = new Deflater();
        compressor.setLevel( Deflater.BEST_COMPRESSION );

        // Give the compressor the data to compress
        compressor.setInput( input );
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // It is not necessary that the compressed data will be smaller than
        // the uncompressed data.
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream( input.length ))
        {
            // Compress the data
            final byte[] buf = new byte[bufferLength];
            while ( !compressor.finished() )
            {
                final int count = compressor.deflate( buf );
                bos.write( buf, 0, count );
            }

            // Get the compressed data
            return bos.toByteArray();
        }
        finally
        {
            // JCS-136 ( Details here : http://www.devguli.com/blog/eng/java-deflater-and-outofmemoryerror/ )
            compressor.end();
        }
    }

    /**
     * Decompress the byte array passed
     *
     * @param input compressed byte array webservice response
     * @param bufferLength buffer length
     * @return uncompressed byte array
     * @throws IOException thrown if we can't close the output stream
     */
    public static byte[] decompressByteArray( final byte[] input, final int bufferLength )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "Input was null" );
        }

        // Create the decompressor and give it the data to compress
        final Inflater decompressor = new Inflater();

        decompressor.setInput( input );

        // Create an expandable byte array to hold the decompressed data
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream( input.length ))
        {
            // Decompress the data
            final byte[] buf = new byte[bufferLength];

            try
            {
                while ( !decompressor.finished() )
                {
                    final int count = decompressor.inflate( buf );
                    baos.write( buf, 0, count );
                }
            }
            catch ( final DataFormatException ex )
            {
                throw new IOException("Problem decompressing.", ex);
            }

            return baos.toByteArray();
        }
        finally
        {
            decompressor.end();
        }
    }
}
