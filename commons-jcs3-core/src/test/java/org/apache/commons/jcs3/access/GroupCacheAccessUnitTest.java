package org.apache.commons.jcs3.access;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.junit.jupiter.api.Test;

/**
 * Tests the methods of the group cache access class.
 */
class GroupCacheAccessUnitTest
{
    /**
     * Verify we can use the group cache.
     *
     * @throws Exception
     */
    @Test
    void testGroupCache()
        throws Exception
    {
        final GroupCacheAccess<String, Integer> access = JCS.getGroupCacheInstance( "testGroup" );
        final String groupName1 = "testgroup1";
        final String groupName2 = "testgroup2";

        Set<String> keys1 = access.getGroupKeys( groupName1 );
        assertNotNull(keys1);
        assertEquals(0, keys1.size());

        Set<String> keys2 = access.getGroupKeys( groupName2 );
        assertNotNull(keys2);
        assertEquals(0, keys2.size());

        // DO WORK
        final int numToInsertGroup1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertGroup1; i++ )
        {
            access.putInGroup(String.valueOf( i ), groupName1, Integer.valueOf( i ) );
        }

        final int numToInsertGroup2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertGroup2; i++ )
        {
            access.putInGroup(String.valueOf( i ), groupName2, Integer.valueOf( i + 1 ) );
        }

        keys1 = access.getGroupKeys( groupName1 ); // Test for JCS-102
        assertNotNull(keys1);
        assertEquals( 10, keys1.size(), "Wrong number returned 1:" );

        keys2 = access.getGroupKeys( groupName2 );
        assertNotNull(keys2);
        assertEquals( 50, keys2.size(), "Wrong number returned 2:" );

        assertEquals(Integer.valueOf(5), access.getFromGroup("5", groupName1));
        assertEquals(Integer.valueOf(6), access.getFromGroup("5", groupName2));

        assertTrue(access.getGroupNames().contains(groupName1));
        assertTrue(access.getGroupNames().contains(groupName2));
    }

    /**
     * Verify that we can invalidate the group
     * @throws Exception
     */
    @Test
    void testInvalidate()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "mykey";
        final String group = "mygroup";
        final String value = "myvalue";

        for (int i = 0; i < 10; i++)
        {
            access.putInGroup(key + i, group + 0, value + i);
        }

        for (int i = 0; i < 10; i++)
        {
            access.putInGroup(key + i, group + 1, value + i);
        }

        // Make sure cache contains some data
        for (int i = 0; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group + 0);
            assertEquals( value + i, returnedValue1, "Wrong value returned." );
            final String returnedValue2 = access.getFromGroup(key + i, group + 1);
            assertEquals( value + i, returnedValue2, "Wrong value returned." );
        }

        access.invalidateGroup(group + 0);

        for (int i = 0; i < 10; i++)
        {
            assertNull( access.getFromGroup( key + i, group + 0 ), "Should not be in cache" );
        }

        for (int i = 0; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group + 1);
            assertEquals( value + i, returnedValue1, "Wrong value returned." );
        }
    }

    /**
     * Verify that we can put and get an object
     * @throws Exception
     */
    @Test
    void testPutAndGet()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "mykey";
        final String group = "mygroup";
        final String value = "myvalue";

        access.putInGroup(key, group, value);

        final String returnedValue1 = access.getFromGroup(key, group);
        assertEquals( value, returnedValue1, "Wrong value returned." );
    }

    /**
     * Try to put a null key and verify that we get an exception.
     * @throws Exception
     */
    @Test
    void testPutNullKey()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = null;
        final String group = "mygroup";
        final String value = "myvalue";

        try
        {
            access.putInGroup(key, group, value);
            fail( "Should not have been able to put a null key." );
        }
        catch ( final CacheException e )
        {
            assertTrue( e.getMessage().indexOf( "null" ) != -1, "Should have the word null in the error message." );
        }
    }

    /**
     * Try to put a null value and verify that we get an exception.
     * @throws Exception
     */
    @Test
    void testPutNullValue()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "myKey";
        final String group = "mygroup";
        final String value = null;

        try
        {
            access.putInGroup(key, group, value);
            fail( "Should not have been able to put a null object." );
        }
        catch ( final CacheException e )
        {
            assertTrue( e.getMessage().indexOf( "null" ) != -1, "Should have the word null in the error message." );
        }
    }

    /**
     * Verify that we can remove items from the cache
     * @throws Exception
     */
    @Test
    void testRemove()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "mykey";
        final String group = "mygroup";
        final String value = "myvalue";

        for (int i = 0; i < 10; i++)
        {
            access.putInGroup(key + i, group, value + i);
        }

        // Make sure cache contains some data
        for (int i = 0; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group);
            assertEquals( value + i, returnedValue1, "Wrong value returned." );
        }

        access.removeFromGroup(key + 0, group);

        assertNull( access.getFromGroup( key + 0, group ), "Should not be in cache" );

        for (int i = 1; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group);
            assertEquals( value + i, returnedValue1, "Wrong value returned." );
        }
    }
}
