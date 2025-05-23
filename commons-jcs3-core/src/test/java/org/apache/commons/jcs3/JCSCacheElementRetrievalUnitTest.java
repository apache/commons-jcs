package org.apache.commons.jcs3;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.junit.jupiter.api.Test;

/**
 */
class JCSCacheElementRetrievalUnitTest
{
    /**
     *
     * @throws Exception
     */
    @Test
    void testSimpleElementRetrieval()
        throws Exception
    {
        final CacheAccess<String, String> jcs = JCS.getInstance( "testCache1" );

        jcs.put( "test_key", "test_data" );

        final long now = System.currentTimeMillis();
        final ICacheElement<String, String> elem = jcs.getCacheElement( "test_key" );
        assertEquals( "testCache1", elem.getCacheName(), "Name wasn't right" );

        final long diff = now - elem.getElementAttributes().getCreateTime();
        assertTrue( diff >= 0, "Create time should have been at or after the call" );

    }
}
