package org.apache.commons.jcs.utils.zip;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/** Unit tests for the compression util */
public class CompressionUtilUnitTest
    extends TestCase
{
    /** Test method for decompressByteArray. */
    public final void testDecompressByteArray_failure()
    {
        try
        {
            // DO WORK
            CompressionUtil.decompressByteArray( null );

            // VERIFY
            fail( "excepted an IllegalArgumentException" );
        }
        catch ( IllegalArgumentException exception )
        {
            // expected
            return;
        }
    }

    /**
     * Test method for decompressByteArray.
     * <p>
     * @throws IOException
     */
    public final void testCompressDecompressByteArray_success()
        throws IOException
    {
        // SETUP
        String text = "This is some text to compress, not a lot, just a bit ";

        // DO WORK
        byte[] compressedText = CompressionUtil.compressByteArray( text.getBytes() );
        byte[] output = CompressionUtil.decompressByteArray( compressedText );

        // VERIFY
        String result = new String( output );
        assertNotNull( "decompressed output stream shouldn't have been null ", output );
        assertEquals( text, result );
    }

    /**
     * Test method for decompressByteArray.
     * <p>
     * @throws IOException
     */
    public final void testCompressDecompressGzipByteArray_success()
        throws IOException
    {
        // SETUP
        String text = " This is some text to compress, not a lot, just a bit ";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream os = new GZIPOutputStream( baos );

        os.write( text.getBytes() );
        os.flush();
        os.close();

        // DO WORK
        byte[] output = CompressionUtil.decompressGzipByteArray( baos.toByteArray() );

        // VERIFY
        String result = new String( output );
        assertNotNull( "decompressed output stream shouldn't have been null ", output );
        assertEquals( text, result );
    }
}
