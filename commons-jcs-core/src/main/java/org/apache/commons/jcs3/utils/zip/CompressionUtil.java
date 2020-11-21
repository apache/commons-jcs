package org.apache.commons.jcs3.utils.zip;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/** Compress / Decompress. */
public final class CompressionUtil
{
    /** The logger */
    private static final Log log = LogManager.getLog( CompressionUtil.class );

    /**
     * no instances.
     */
    private CompressionUtil()
    {
        // NO OP
    }

    /**
     * Decompress the byte array passed using a default buffer length of 1024.
     * <p>
     * @param input compressed byte array webservice response
     * @return uncompressed byte array
     */
    public static byte[] decompressByteArray( final byte[] input )
    {
        return decompressByteArray( input, 1024 );
    }

    /**
     * Decompress the byte array passed
     * <p>
     * @param input compressed byte array webservice response
     * @param bufferLength buffer length
     * @return uncompressed byte array
     */
    public static byte[] decompressByteArray( final byte[] input, final int bufferLength )
    {
        if ( null == input )
        {
            throw new IllegalArgumentException( "Input was null" );
        }

        // Create the decompressor and give it the data to compress
        final Inflater decompressor = new Inflater();

        decompressor.setInput( input );

        // Create an expandable byte array to hold the decompressed data
        final ByteArrayOutputStream baos = new ByteArrayOutputStream( input.length );

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
            log.error( "Problem decompressing.", ex );
        }

        decompressor.end();

        try
        {
            baos.close();
        }
        catch ( final IOException ex )
        {
            log.error( "Problem closing stream.", ex );
        }

        return baos.toByteArray();
    }

    /**
     * Compress the byte array passed
     * <p>
     * @param input byte array
     * @return compressed byte array
     * @throws IOException thrown if we can't close the output stream
     */
    public static byte[] compressByteArray( final byte[] input )
        throws IOException
    {
        return compressByteArray( input, 1024 );
    }

    /**
     * Compress the byte array passed
     * <p>
     * @param input byte array
     * @param bufferLength buffer length
     * @return compressed byte array
     * @throws IOException thrown if we can't close the output stream
     */
    public static byte[] compressByteArray( final byte[] input, final int bufferLength )
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
        final ByteArrayOutputStream bos = new ByteArrayOutputStream( input.length );

        // Compress the data
        final byte[] buf = new byte[bufferLength];
        while ( !compressor.finished() )
        {
            final int count = compressor.deflate( buf );
            bos.write( buf, 0, count );
        }

        // JCS-136 ( Details here : http://www.devguli.com/blog/eng/java-deflater-and-outofmemoryerror/ )
        compressor.end();
        bos.close();

        // Get the compressed data
        return bos.toByteArray();

    }

    /**
     * decompress a gzip byte array, using a default buffer length of 1024
     * <p>
     * @param compressedByteArray gzip-compressed byte array
     * @return decompressed byte array
     * @throws IOException thrown if there was a failure to construct the GzipInputStream
     */
    public static byte[] decompressGzipByteArray( final byte[] compressedByteArray )
        throws IOException
    {
        return decompressGzipByteArray( compressedByteArray, 1024 );
    }

    /**
     * decompress a gzip byte array, using a default buffer length of 1024
     * <p>
     * @param compressedByteArray gzip-compressed byte array
     * @param bufferlength size of the buffer in bytes
     * @return decompressed byte array
     * @throws IOException thrown if there was a failure to construct the GzipInputStream
     */
    public static byte[] decompressGzipByteArray( final byte[] compressedByteArray, final int bufferlength )
        throws IOException
    {
        final ByteArrayOutputStream uncompressedStream = new ByteArrayOutputStream();

        final GZIPInputStream compressedStream = new GZIPInputStream( new ByteArrayInputStream( compressedByteArray ) );

        final byte[] buffer = new byte[bufferlength];

        int index = -1;

        while ( ( index = compressedStream.read( buffer ) ) != -1 )
        {
            uncompressedStream.write( buffer, 0, index );
        }

        return uncompressedStream.toByteArray();
    }
}
