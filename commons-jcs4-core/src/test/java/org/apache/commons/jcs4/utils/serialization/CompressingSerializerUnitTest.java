package org.apache.commons.jcs4.utils.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the compressing serializer.
 */
class CompressingSerializerUnitTest
{
    private CompressingSerializer serializer;

    @BeforeEach
    void setUp()
        throws Exception
    {
        this.serializer = new CompressingSerializer();
    }

    /**
     * Verify that the compressed is smaller.
     *
     * @throws Exception on error
     */
    @Test
    void testSerialize_CompareCompressedAndUncompressed()
        throws Exception
    {
        // I hate for loops.
        final String before = """
        	adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdssaf dsaf sadf dsaf dsaf dsaf\s\
        	dsafdsa fdsaf dsaf dsafdsa dsaf dsaf dsaf dsaf dsafdsa76f dsa798f dsa6fdsa 087f\s\s\
        	gh 987dsahb dsahbuhbfnui nufdsa hbv87 f8vhdsgbnfv h8fdg8dfjvn8fdwgj fdsgjb9fdsjbv\
        	jvhjv hg98f-dsaghj j9fdsb gfsb 9fdshjbgb987fdsbfdwgh ujbhjbhb hbfdsgh fdshb\s\
        	Ofdsgyfesgyfdsafdsafsa333 31231""";

        // DO WORK
        final byte[] compressed = serializer.serialize( before );
        final byte[] nonCompressed = new StandardSerializer().serialize( before );

        // VERIFY
        assertTrue( compressed.length < nonCompressed.length,
                    "Compressed should be smaller. compressed size = " + compressed.length + "nonCompressed size = "
                        + nonCompressed.length );
    }

    /**
     * Test method for decompressByteArray.
     *
     * @throws IOException
     */
    @Test
    public final void testCompressDecompressByteArray_success()
        throws IOException
    {
        // SETUP
        final String text = "This is some text to compress, not a lot, just a bit ";

        // DO WORK
        final byte[] compressedText = CompressingSerializer.compressByteArray( text.getBytes(), 1024 );
        final byte[] output = CompressingSerializer.decompressByteArray( compressedText, 1024 );

        // VERIFY
        final String result = new String( output );
        assertNotNull( output, "decompressed output stream shouldn't have been null " );
        assertEquals( text, result );
    }

    /** Test method for decompressByteArray. */
    @Test
    public final void testDecompressByteArray_failure()
    {
        assertThrows(IllegalArgumentException.class, () -> CompressingSerializer.decompressByteArray( null, 0 ),
                "excepted an IllegalArgumentException");
    }

    /** Test method for decompressByteArray. */
    @Test
    public final void testDecompressByteArray_gibberish()
    {
        final String text = "This is some text to decompress, not a lot, just a bit ";

        assertThrows(IOException.class, () -> CompressingSerializer.decompressByteArray( text.getBytes(), 0 ),
                "excepted an IOException");
    }
}
