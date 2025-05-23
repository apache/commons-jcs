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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.apache.commons.jcs3.access.CacheAccess;
import org.junit.jupiter.api.Test;

/**
 * Simple test for the JCS class.
 */
class JCSUnitTest
{
    /** A random for key generation. */
    Random random = new Random();

    /**
     * @return builds a list
     */
    private LinkedList<HashMap<String, String>> buildList()
    {
        final LinkedList<HashMap<String, String>> list = new LinkedList<>();

        for ( int i = 0; i < 100; i++ )
        {
            list.add( buildMap() );
        }

        return list;
    }

    /**
     * @return a map
     */
    private HashMap<String, String> buildMap()
    {
        final HashMap<String, String> map = new HashMap<>();

        final byte[] keyBytes = new byte[32];
        final byte[] valBytes = new byte[128];

        for ( int i = 0; i < 10; i++ )
        {
            random.nextBytes( keyBytes );
            random.nextBytes( valBytes );

            map.put( new String( keyBytes ), new String( valBytes ) );
        }

        return map;
    }

    /**
     * @throws Exception
     */
    @Test
    void testJCS()
        throws Exception
    {
        final CacheAccess<String, LinkedList<HashMap<String, String>>> jcs = JCS.getInstance( "testCache1" );

        final LinkedList<HashMap<String, String>> list = buildList();

        jcs.put( "some:key", list );

        assertEquals( list, jcs.get( "some:key" ) );
    }
}
