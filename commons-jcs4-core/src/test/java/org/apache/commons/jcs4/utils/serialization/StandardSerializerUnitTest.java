package org.apache.commons.jcs4.utils.serialization;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.jcs4.io.ObjectInputStreamClassLoaderAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the standard serializer.
 */
class StandardSerializerUnitTest
{
    private StandardSerializer serializer;

    /**
     * Test setup
     *
     * @throws Exception
     */
    @BeforeEach
    void setUp()
        throws Exception
    {
	// Override filter expression for ObjectInputFilter
        System.setProperty(ObjectInputStreamClassLoaderAware.SYSTEM_PROPERTY_SERIALIZATION_FILTER,
            "!org.apache.commons.collections4.**");
        this.serializer = new StandardSerializer();
    }

    /**
     * Test simple back and forth with a string.
     *
     * @throws Exception
     */
    @Test
    void testBigStringBackAndForth()
        throws Exception
    {
        final String string = "This is my big string ABCDEFGH";
        final StringBuilder sb = new StringBuilder();
        sb.append( string );
        for ( int i = 0; i < 4; i++ )
        {
            sb.append( " " + i + sb.toString() ); // big string
        }
        final String before = sb.toString();

        // DO WORK
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }

    /**
     * Test serialization with a null object. Verify that we don't get an error.
     *
     * @throws Exception
     */
    @Test
    void testNullInput()
        throws Exception
    {
        final String before = null;

        // DO WORK
        final byte[] serialized = serializer.serialize( before );
        //System.out.println( "testNullInput " + serialized );

        final String after = (String) serializer.deSerialize( serialized, null );
        //System.out.println( "testNullInput " + after );

        // VERIFY
        assertNull( after, "Should have nothing." );
    }

    /**
     * Test simple back and forth with a string.
     *
     * @throws Exception
     */
    @Test
    void testSimpleBackAndForth()
        throws Exception
    {
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";

        // DO WORK
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }

    /**
     * Verify that we can filter classes to be deserialized
     *
     * @throws IOException
     */
    @Test
    void testDeserializationFilter()
        throws IOException
    {
        // DO WORK
        final byte[] serialized = serializer.serialize( new HashBag<String>() ); // forbidden class

        assertThrows(InvalidClassException.class, () -> serializer.deSerialize( serialized, null ));
    }

    /**
     * Test resilience of deserialization against data integrity errors (JCS-244)
     */
    @Test
    void testDeserializationResilience()
    {
        byte[] inputArray1 = { 42 };
        ByteArrayInputStream input1 = new ByteArrayInputStream(inputArray1);

        assertThrows(IOException.class, () -> serializer.deSerializeFrom( input1, null ));

        byte[] data2 = { 0x12, 0x34, 0x56, 0x78, 0x12, 0x34, 0x56, 0x78 };
        byte[] inputArray2 = ByteBuffer.allocate(12).putInt(10).put(data2).array();
        ByteArrayInputStream input2 = new ByteArrayInputStream(inputArray2);

        assertThrows(IOException.class, () -> serializer.deSerializeFrom( input2, null ));

        byte[] data3 = { 0x12, 0x34, 0x56, 0x78, 0x12, 0x34, 0x56, 0x78 };
        byte[] inputArray3 = ByteBuffer.allocate(12).putInt(8).put(data3).array();
        ByteArrayInputStream input3 = new ByteArrayInputStream(inputArray3);

        assertThrows(StreamCorruptedException.class, () -> serializer.deSerializeFrom( input3, null ));
    }
}
