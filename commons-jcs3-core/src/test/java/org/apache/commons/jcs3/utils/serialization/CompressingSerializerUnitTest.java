package org.apache.commons.jcs3.utils.serialization;

import java.io.IOException;

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

/**
 * Tests the compressing serializer.
 */
public class CompressingSerializerUnitTest
    extends TestCase
{
    private CompressingSerializer serializer;

    @Override
    protected void setUp() throws Exception
    {
        this.serializer = new CompressingSerializer();
    }

    /**
     * Verify that we don't get any erorrs for null input.
     * <p>
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void testDeserialize_NullInput()
        throws IOException, ClassNotFoundException
    {
        // DO WORK
        final Object result = serializer.deSerialize( null, null );

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
        // DO WORK
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

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
        final String before = null;

        // DO WORK
        final byte[] serialized = serializer.serialize( before );
        final String after = (String) serializer.deSerialize( serialized, null );

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
        assertTrue( "Compressed should be smaller. compressed size = " + compressed.length + "nonCompressed size = "
            + nonCompressed.length, compressed.length < nonCompressed.length );
    }
}
