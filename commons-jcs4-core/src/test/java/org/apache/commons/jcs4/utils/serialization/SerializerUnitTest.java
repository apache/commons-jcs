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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.CacheAccess;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verify that serializer functionality works.
 */
class SerializerUnitTest
{
    /**
     * Test setup
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestElementSerializer.ccf" );
    }

    @AfterAll
    public static void tearDown()
        throws Exception
    {
        JCS.shutdown();
    }

    /**
     * Verify that object reading and writing with CompressingSerializer works
     *
     * @throws Exception
     */
    @Test
    public void testReadWriteCompressingSerializer()
        throws Exception
    {
        // CompressingSerializer
        final CacheAccess<String, String> jcs = JCS.getInstance( "blockRegion1" );

        testReadWrite(jcs);
    }

    /**
     * Verify that object reading and writing with EncryptingSerializer works
     *
     * @throws Exception
     */
    @Test
    public void testReadWriteEncryptingSerializer()
        throws Exception
    {
        // EncryptingSerializer
        final CacheAccess<String, String> jcs1 = JCS.getInstance( "blockRegion2" );

        testReadWrite(jcs1);

        JCS.shutdown();

        // Re-init
        // EncryptingSerializer
        final CacheAccess<String, String> jcs2 = JCS.getInstance( "blockRegion2" );

        for ( int i = 0; i < 500; i++ )
        {
            final String res = jcs2.get( "key:" + i );
            assertNotNull( res, "[key:" + i + "] should not be null, " + jcs2.getStats() );
        }
    }

    /**
     * Verify that object reading and writing with JSONSerializer works
     *
     * @throws Exception
     */
    @Test
    public void testReadWriteJSONSerializer()
        throws Exception
    {
        // JSONSerializer
        final CacheAccess<String, String> jcs = JCS.getInstance( "blockRegion3" );

        testReadWrite(jcs);
    }

    /**
     * Verify that object reading and writing works
     *
     * @throws Exception
     */
    private void testReadWrite(CacheAccess<String, String> jcs)
        throws Exception
    {
        final int count = 500; // 100 fit in memory

        for ( int i = 0; i < count; i++ )
        {
            jcs.put( "key:" + i, "data" + i );
        }

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs.get( "key:" + i );
            assertNotNull( res, "[key:" + i + "] should not be null, " + jcs.getStats() );
        }
    }
}
