package org.apache.commons.jcs3.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import java.util.Set;

import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.junit.Test;

/**
 * Tests the methods of the group cache access class.
 */
public class GroupCacheAccessUnitTest
{
    /**
     * Verify we can use the group cache.
     * <p>
     * @throws Exception
     */
    @Test
    public void testGroupCache()
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
        assertEquals("Wrong number returned 1:", 10, keys1.size());

        keys2 = access.getGroupKeys( groupName2 );
        assertNotNull(keys2);
        assertEquals("Wrong number returned 2:", 50, keys2.size());

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
    public void testInvalidate()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

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
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
            final String returnedValue2 = access.getFromGroup(key + i, group + 1);
            assertEquals( "Wrong value returned.", value + i, returnedValue2 );
        }

        access.invalidateGroup(group + 0);

        for (int i = 0; i < 10; i++)
        {
            assertNull("Should not be in cache", access.getFromGroup(key + i, group + 0));
        }

        for (int i = 0; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group + 1);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }
    }

    /**
     * Verify that we can put and get an object
     * @throws Exception
     */
    @Test
    public void testPutAndGet()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        final String key = "mykey";
        final String group = "mygroup";
        final String value = "myvalue";

        access.putInGroup(key, group, value);

        final String returnedValue1 = access.getFromGroup(key, group);
        assertEquals( "Wrong value returned.", value, returnedValue1 );
    }

    /**
     * Try to put a null key and verify that we get an exception.
     * @throws Exception
     */
    @Test
    public void testPutNullKey()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

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
            assertTrue( "Should have the word null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Try to put a null value and verify that we get an exception.
     * @throws Exception
     */
    @Test
    public void testPutNullValue()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

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
            assertTrue( "Should have the word null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Verify that we can remove items from the cache
     * @throws Exception
     */
    @Test
    public void testRemove()
        throws Exception
    {
        final GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

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
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }

        access.removeFromGroup(key + 0, group);

        assertNull("Should not be in cache", access.getFromGroup(key + 0, group));

        for (int i = 1; i < 10; i++)
        {
            final String returnedValue1 = access.getFromGroup(key + i, group);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }
    }
}
