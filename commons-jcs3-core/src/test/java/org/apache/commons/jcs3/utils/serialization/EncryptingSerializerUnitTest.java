package org.apache.commons.jcs3.utils.serialization;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the encrypting serializer.
 */
class EncryptingSerializerUnitTest
{
    private EncryptingSerializer serializer;

    @BeforeEach
    void setUp()
        throws Exception
    {
        this.serializer = new EncryptingSerializer();
        this.serializer.setPreSharedKey("my_secret_key");
    }

    /**
     * Verify that we don't get any errors for null input.
     *
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @Test
    void testDeserialize_NullInput()
        throws IOException, ClassNotFoundException
    {
        // DO WORK
        final Object result = serializer.deSerialize( null, null );

        // VERIFY
        assertNull( result, "Should have nothing." );
    }

    /**
     * Test different key.
     *
     * @throws Exception on error
     */
    @Test
    void testDifferentKey()
        throws Exception
    {
        // DO WORK
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        final byte[] serialized = serializer.serialize(before);
        serializer.setPreSharedKey("another_key");

        assertThrows(IOException.class, () -> serializer.deSerialize(serialized, null));
    }

    /**
     * Test simple back and forth with a string.
     * <p>
     * ))&lt;=&gt;((
     *
     * @throws Exception on error
     */
    @Test
    void testGCMBackAndForth()
        throws Exception
    {
        this.serializer.setAesCipherTransformation("AES/GCM/NoPadding");

        // DO WORK
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        final String after = serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }

    /**
     * Test serialization with a null object. Verify that we don't get an error.
     *
     * @throws Exception on error
     */
    @Test
    void testSerialize_NullInput()
        throws Exception
    {
        final String before = null;

        // DO WORK
        final byte[] serialized = serializer.serialize( before );
        final String after = (String) serializer.deSerialize( serialized, null );

        // VERIFY
        assertNull( after, "Should have nothing. after =" + after );
    }

    /**
     * Test simple back and forth with a string.
     * <p>
     * ))&lt;=&gt;((
     *
     * @throws Exception on error
     */
    @Test
    void testSimpleBackAndForth()
        throws Exception
    {
        // DO WORK
        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";
        final String after = serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( before, after, "Before and after should be the same." );
    }
}
