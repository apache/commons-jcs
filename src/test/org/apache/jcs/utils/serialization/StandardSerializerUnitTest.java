package org.apache.jcs.utils.serialization;

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
 * Tests the standard serializer.
 *
 * @author Aaron Smuts
 *
 */
public class StandardSerializerUnitTest
    extends TestCase
{

    /**
     * Test simple back and forth with a string.
     *
     * @throws Exception
     *
     */
    public void testSimpleBackAndForth()
        throws Exception
    {
        StandardSerializer serializer = new StandardSerializer();

        String before = "adsfdsafdsafdsafdsafdsafdsafdsagfdsafdsafdsfdsafdsafsa333 31231";

        String after = (String) serializer.deSerialize( serializer.serialize( before ) );

        assertEquals( "Before and after should be the same.", before, after );
    }

    /**
     * Test serialization with a null object.  Verify that we don't get an error.
     *
     * @throws Exception
     *
     */
    public void testNullInput()
        throws Exception
    {
        StandardSerializer serializer = new StandardSerializer();

        String before = null;

        byte[] serialized = serializer.serialize( before );

        System.out.println( "testNullInput " + serialized );

        String after = (String) serializer.deSerialize( serialized );

        System.out.println( "testNullInput " + after );

        assertNull( "Should have nothing.", after );

    }
}
