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

import static org.junit.Assert.assertNotNull;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Verify that serializer functionality works.
 */
public class SerializerUnitTest
{
    /**
     * Test setup
     * <p>
     * @throws Exception
     */
    @Before
    public void setUp()
        throws Exception
    {
        JCS.setConfigFilename( "/TestElementSerializer.ccf" );
    }

    @After
    public void tearDown() throws Exception
    {
        JCS.shutdown();
    }

    /**
     * Verify that object reading and writing works
     * <p>
     * @throws Exception
     */
    @Test
    public void testReadWrite()
        throws Exception
    {
        final int count = 500; // 100 fit in memory
        // CompressingSerializer
        final CacheAccess<String, String> jcs1 = JCS.getInstance( "blockRegion1" );

        for ( int i = 0; i < count; i++ )
        {
            jcs1.put( "key:" + i, "data" + i );
        }

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs1.get( "key:" + i );
            assertNotNull( "[key:" + i + "] should not be null, " + jcs1.getStats(), res );
        }

        // EncryptingSerializer
        final CacheAccess<String, String> jcs2 = JCS.getInstance( "blockRegion2" );

        for ( int i = 0; i < count; i++ )
        {
            jcs2.put( "key:" + i, "data" + i );
        }

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs2.get( "key:" + i );
            assertNotNull( "[key:" + i + "] should not be null, " + jcs2.getStats(), res );
        }

        JCS.shutdown();

        // Re-init
        // EncryptingSerializer
        final CacheAccess<String, String> jcs3 = JCS.getInstance( "blockRegion2" );

        for ( int i = 0; i < count; i++ )
        {
            final String res = jcs3.get( "key:" + i );
            assertNotNull( "[key:" + i + "] should not be null, " + jcs3.getStats(), res );
        }
    }
}
