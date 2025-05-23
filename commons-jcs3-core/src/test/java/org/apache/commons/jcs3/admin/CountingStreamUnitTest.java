package org.apache.commons.jcs3.admin;

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

import org.junit.jupiter.api.Test;

/**
 * Tests for the counting only output stream.
 */
class CountingStreamUnitTest
{

    /**
     * This should count the size of the array.
     *
     * @throws Exception
     */
    @Test
    void testByteArray()
        throws Exception
    {
        final CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        final byte[] array = {1,2,3,4,5};
        out.write( array );
        assertEquals( array.length, out.getCount(), "Wrong number of bytes written." );
        out.close();
    }

    /**
     * This should count the len -- the third arg
     *
     * @throws Exception
     */
    @Test
    void testByteArrayLenCount()
        throws Exception
    {
        final CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        final byte[] array = {1,2,3,4,5};
        final int len = 3;
        out.write( array, 0, len );
        assertEquals( len, out.getCount(), "Wrong number of bytes written." );
        out.close();
    }

    /**
     * Writes a single byte and verify the count.
     *
     * @throws Exception
     */
    @Test
    void testSingleByte()
        throws Exception
    {
        final CountingOnlyOutputStream out = new CountingOnlyOutputStream();
        out.write( 1 );
        assertEquals( 1, out.getCount(), "Wrong number of bytes written." );
        out.write( 1 );
        assertEquals( 2, out.getCount(), "Wrong number of bytes written." );
        out.close();
    }
}
