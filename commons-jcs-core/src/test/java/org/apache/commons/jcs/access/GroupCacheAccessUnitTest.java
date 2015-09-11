package org.apache.commons.jcs.access;

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

import junit.framework.TestCase;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.exception.CacheException;

/**
 * Tests the methods of the group cache access class.
 * <p>
 * @author Aaron Smuts
 */
public class GroupCacheAccessUnitTest
    extends TestCase
{
    /**
     * Verify that we can put and get an object
     * @throws Exception
     */
    public void testPutAndGet()
        throws Exception
    {
        GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "mykey";
        String group = "mygroup";
        String value = "myvalue";

        access.putInGroup(key, group, value);

        String returnedValue1 = access.getFromGroup(key, group);
        assertEquals( "Wrong value returned.", value, returnedValue1 );
    }

    /**
     * Try to put a null key and verify that we get an exception.
     * @throws Exception
     */
    public void testPutNullKey()
        throws Exception
    {
        GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = null;
        String group = "mygroup";
        String value = "myvalue";

        try
        {
            access.putInGroup(key, group, value);
            fail( "Should not have been able to put a null key." );
        }
        catch ( CacheException e )
        {
            assertTrue( "Should have the word null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Try to put a null value and verify that we get an exception.
     * @throws Exception
     */
    public void testPutNullValue()
        throws Exception
    {
        GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "myKey";
        String group = "mygroup";
        String value = null;

        try
        {
            access.putInGroup(key, group, value);
            fail( "Should not have been able to put a null object." );
        }
        catch ( CacheException e )
        {
            assertTrue( "Should have the word null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Verify that we can remove items from the cache
     * @throws Exception
     */
    public void testRemove()
        throws Exception
    {
        GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "mykey";
        String group = "mygroup";
        String value = "myvalue";

        for (int i = 0; i < 10; i++)
        {
            access.putInGroup(key + i, group, value + i);
        }

        // Make sure cache contains some data
        for (int i = 0; i < 10; i++)
        {
            String returnedValue1 = access.getFromGroup(key + i, group);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }

        access.removeFromGroup(key + 0, group);

        assertNull("Should not be in cache", access.getFromGroup(key + 0, group));

        for (int i = 1; i < 10; i++)
        {
            String returnedValue1 = access.getFromGroup(key + i, group);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }
    }

    /**
     * Verify that we can invalidate the group
     * @throws Exception
     */
    public void testInvalidate()
        throws Exception
    {
        GroupCacheAccess<String, String> access = JCS.getGroupCacheInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "mykey";
        String group = "mygroup";
        String value = "myvalue";

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
            String returnedValue1 = access.getFromGroup(key + i, group + 0);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
            String returnedValue2 = access.getFromGroup(key + i, group + 1);
            assertEquals( "Wrong value returned.", value + i, returnedValue2 );
        }

        access.invalidateGroup(group + 0);

        for (int i = 0; i < 10; i++)
        {
            assertNull("Should not be in cache", access.getFromGroup(key + i, group + 0));
        }

        for (int i = 0; i < 10; i++)
        {
            String returnedValue1 = access.getFromGroup(key + i, group + 1);
            assertEquals( "Wrong value returned.", value + i, returnedValue1 );
        }
    }

    /**
     * Verify we can use the group cache.
     * <p>
     * @throws Exception
     */
    public void testGroupCache()
        throws Exception
    {
        GroupCacheAccess<String, Integer> access = JCS.getGroupCacheInstance( "testGroup" );
        String groupName1 = "testgroup1";
        String groupName2 = "testgroup2";

        Set<String> keys1 = access.getGroupKeys( groupName1 );
        assertNotNull(keys1);
        assertEquals(0, keys1.size());

        Set<String> keys2 = access.getGroupKeys( groupName2 );
        assertNotNull(keys2);
        assertEquals(0, keys2.size());

        // DO WORK
        int numToInsertGroup1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertGroup1; i++ )
        {
            access.putInGroup(String.valueOf( i ), groupName1, Integer.valueOf( i ) );
        }

        int numToInsertGroup2 = 50;
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
}
