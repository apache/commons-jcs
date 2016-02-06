package org.apache.commons.jcs.utils.serialization;

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

import java.io.IOException;

/**
 * Tests the compressing serializer.
 */
public class CompressingSerializerUnitTest
    extends TestCase
{
    /**
     * Verify that we don't get any erorrs for null input.
     * <p>
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void testDeserialize_NullInput()
        throws IOException, ClassNotFoundException
    {
        // SETUP
        CompressingSerializer serializer = new CompressingSerializer();

        // DO WORK
        Object result = serializer.deSerialize( null, null );

        // VERIFY
        assertNull( "Should have nothing.", result );
    }

    /**
     * Test simple back and forth with a string.
     * <p>
     * ))&lt;=&gt;((
     * <p>
     * @throws Exception on error
     */
    public void testSimpleBackAndForth()
        throws Exception
    {
        // SETUP
        CompressingSerializer serializer = new CompressingSerializer();

        // DO WORK
        String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( "Before and after should be the same.", before, after );
    }

    /**
     * Test serialization with a null object. Verify that we don't get an error.
     * <p>
     * @throws Exception on error
     */
    public void testSerialize_NullInput()
        throws Exception
    {
        // SETUP
        CompressingSerializer serializer = new CompressingSerializer();

        String before = null;

        // DO WORK
        byte[] serialized = serializer.serialize( before );
        String after = (String) serializer.deSerialize( serialized, null );

        // VERIFY
        assertNull( "Should have nothing. after =" + after, after );
    }

    /**
     * Verify that the compressed is smaller.
     * <p>
     * @throws Exception on error
     */
    public void testSerialize_CompareCompressedAndUncompressed()
        throws Exception
    {
        // SETUP
        CompressingSerializer serializer = new CompressingSerializer();

        // I hate for loops.
        String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdssaf dsaf sadf dsaf dsaf dsaf "
            + "dsafdsa fdsaf dsaf dsafdsa dsaf dsaf dsaf dsaf dsafdsa76f dsa798f dsa6fdsa 087f  "
            + "gh 987dsahb dsahbuhbfnui nufdsa hbv87 f8vhdsgbnfv h8fdg8dfjvn8fdwgj fdsgjb9fdsjbv"
            + "jvhjv hg98f-dsaghj j9fdsb gfsb 9fdshjbgb987fdsbfdwgh ujbhjbhb hbfdsgh fdshb "
            + "Ofdsgyfesgyfdsafdsafsa333 31231";

        // DO WORK
        byte[] compressed = serializer.serialize( before );
        byte[] nonCompressed = serializer.serializeObject( before );

        // VERIFY
        assertTrue( "Compressed should be smaller. compressed size = " + compressed.length + "nonCompressed size = "
            + nonCompressed.length, compressed.length < nonCompressed.length );
    }
}
