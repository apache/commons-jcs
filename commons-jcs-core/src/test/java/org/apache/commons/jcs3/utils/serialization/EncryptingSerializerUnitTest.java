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
 * Tests the encrypting serializer.
 */
public class EncryptingSerializerUnitTest
    extends TestCase
{
    private EncryptingSerializer serializer;

    @Override
    protected void setUp() throws Exception
    {
        this.serializer = new EncryptingSerializer();
        this.serializer.setPreSharedKey("my_secret_key");
        this.serializer.setAesCipherTransformation("AES");
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
}
