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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests the standard serializer.
 */
public class StandardSerializerUnitTest
{
    /**
     * Test simple back and forth with a string.
     *<p>
     * @throws Exception
     */
    @Test
    public void testSimpleBackAndForth()
        throws Exception
    {
        // SETUP
        final StandardSerializer serializer = new StandardSerializer();

        final String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";

        // DO WORK
        final String after = (String) serializer.deSerialize( serializer.serialize( before ), null );

        // VERIFY
        assertEquals( "Before and after should be the same.", before, after );
    }

    /**
     * Test serialization with a null object. Verify that we don't get an error.
     *<p>
     * @throws Exception
     */
    @Test
    public void testNullInput()
        throws Exception
    {
        // SETUP
        final StandardSerializer serializer = new StandardSerializer();

        final String before = null;

        // DO WORK
        final byte[] serialized = serializer.serialize( before );
        //System.out.println( "testNullInput " + serialized );

        final String after = (String) serializer.deSerialize( serialized, null );
        //System.out.println( "testNullInput " + after );

        // VERIFY
        assertNull( "Should have nothing.", after );
    }

    /**
     * Test simple back and forth with a string.
     *<p>
     * @throws Exception
     */
    @Test
    public void testBigStringBackAndForth()
        throws Exception
    {
        // SETUP
        final StandardSerializer serializer = new StandardSerializer();

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
        assertEquals( "Before and after should be the same.", before, after );
    }
}
