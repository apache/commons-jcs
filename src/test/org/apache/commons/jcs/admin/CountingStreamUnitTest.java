package org.apache.commons.jcs.admin;

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
 * Tests for the counting only output stream.
 *
 * @author Aaron Smuts
 *
 */
public class CountingStreamUnitTest
    extends TestCase
{

    /**
     * Write a single byte and verify the count.
     *
     * @throws Exception
     */
    public void testSingleByte() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        out.write( 1 );
        assertEquals( "Wrong number of bytes written.", 1, out.getCount() );
        out.write( 1 );
        assertEquals( "Wrong number of bytes written.", 2, out.getCount() );
        out.close();
    }

    /**
     * This should count the size of the array.
     *
     * @throws Exception
     */
    public void testByteArray() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        byte[] array = new byte[]{1,2,3,4,5};
        out.write( array );
        assertEquals( "Wrong number of bytes written.", array.length, out.getCount() );
        out.close();
    }

    /**
     * This should count the len -- the third arg
     *
     * @throws Exception
     */
    public void testByteArrayLenCount() throws Exception
    {
        CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        byte[] array = new byte[]{1,2,3,4,5};
        int len = 3;
        out.write( array, 0, len );
        assertEquals( "Wrong number of bytes written.", len, out.getCount() );
        out.close();
    }
}
