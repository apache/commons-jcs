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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.commons.jcs.access.exception.ObjectExistsException;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;

import junit.framework.TestCase;

/**
 * Tests the methods of the cache access class.
 * <p>
 * @author Aaron Smuts
 */
public class CacheAccessUnitTest
    extends TestCase
{
    /**
     * Verify that we get an object exists exception if the item is in the cache.
     * @throws Exception
     */
    public void testPutSafe()
        throws Exception
    {
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "mykey";
        String value = "myvalue";

        access.put( key, value );

        String returnedValue1 = access.get( key );
        assertEquals( "Wrong value returned.", value, returnedValue1 );

        try
        {
            access.putSafe( key, "someothervalue" );
            fail( "We should have received an exception since this key is already in the cache." );
        }
        catch ( CacheException e )
        {
            assertTrue( "Wrong type of exception.", e instanceof ObjectExistsException );
            assertTrue( "Should have the key in the error message.", e.getMessage().indexOf( "[" + key + "]" ) != -1 );
        }

        String returnedValue2 = access.get( key );
        assertEquals( "Wrong value returned.  Should still be the original.", value, returnedValue2 );
    }

    /**
     * Try to put a null key and verify that we get an exception.
     * @throws Exception
     */
    public void testPutNullKey()
        throws Exception
    {
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = null;
        String value = "myvalue";

        try
        {
            access.put( key, value );
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
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String key = "myKey";
        String value = null;

        try
        {
            access.put( key, value );
            fail( "Should not have been able to put a null object." );
        }
        catch ( CacheException e )
        {
            assertTrue( "Should have the word null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Verify that elements that go in the region after this call take the new attributes.
     * @throws Exception
     */
    public void testSetDefaultElementAttributes()
        throws Exception
    {
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(maxLife);

        access.setDefaultElementAttributes( attr );

        assertEquals( "Wrong element attributes.", attr.getMaxLife(), access.getDefaultElementAttributes()
            .getMaxLife() );

        String key = "mykey";
        String value = "myvalue";

        access.put( key, value );

        ICacheElement<String, String> element = access.getCacheElement( key );

        assertEquals( "Wrong max life.  Should have the new value.", maxLife, element.getElementAttributes()
            .getMaxLife() );
    }

    /**
     * Verify that getCacheElements returns the elements requested based on the key.
     * @throws Exception
     */
    public void testGetCacheElements()
        throws Exception
    {
        //SETUP
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );

        String keyOne = "mykeyone";
        String keyTwo = "mykeytwo";
        String keyThree = "mykeythree";
        String valueOne = "myvalueone";
        String valueTwo = "myvaluetwo";
        String valueThree = "myvaluethree";

        access.put( keyOne, valueOne );
        access.put( keyTwo, valueTwo );
        access.put( keyThree, valueThree );

        Set<String> input = new HashSet<String>();
        input.add( keyOne );
        input.add( keyTwo );

        //DO WORK
        Map<String, ICacheElement<String, String>> result = access.getCacheElements( input );

        //VERIFY
        assertEquals( "map size", 2, result.size() );
        ICacheElement<String, String> elementOne = result.get( keyOne );
        assertEquals( "value one", keyOne, elementOne.getKey() );
        assertEquals( "value one", valueOne, elementOne.getVal() );
        ICacheElement<String, String> elementTwo = result.get( keyTwo );
        assertEquals( "value two", keyTwo, elementTwo.getKey() );
        assertEquals( "value two", valueTwo, elementTwo.getVal() );
    }

    /**
     * Verify that we can get a region using the define region method.
     * @throws Exception
     */
    public void testRegionDefiniton()
        throws Exception
    {
        CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( "We should have an access class", access );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes.
     * @throws Exception
     */
    public void testRegionDefinitonWithAttributes()
        throws Exception
    {
        ICompositeCacheAttributes ca = new CompositeCacheAttributes();

        long maxIdleTime = 8765;
        ca.setMaxMemoryIdleTimeSeconds( maxIdleTime );

        CacheAccess<String, String> access = JCS.getInstance( "testRegionDefinitonWithAttributes", ca );
        assertNotNull( "We should have an access class", access );

        ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( "Wrong idle time setting.", ca.getMaxMemoryIdleTimeSeconds(), ca2.getMaxMemoryIdleTimeSeconds() );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes and
     * element attributes.
     * @throws Exception
     */
    public void testRegionDefinitonWithBothAttributes()
        throws Exception
    {
        ICompositeCacheAttributes ca = new CompositeCacheAttributes();

        long maxIdleTime = 8765;
        ca.setMaxMemoryIdleTimeSeconds( maxIdleTime );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(maxLife);

        CacheAccess<String, String> access = JCS.getInstance( "testRegionDefinitonWithAttributes", ca, attr );
        assertNotNull( "We should have an access class", access );

        ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( "Wrong idle time setting.", ca.getMaxMemoryIdleTimeSeconds(), ca2.getMaxMemoryIdleTimeSeconds() );
    }

    /**
     * Verify we can get some matching elements..
     * <p>
     * @throws Exception
     */
    public void testGetMatching_Normal()
        throws Exception
    {
        // SETUP
        int maxMemorySize = 1000;
        String keyprefix1 = "MyPrefix1";
        String keyprefix2 = "MyPrefix2";
        String memoryCacheClassName = "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache";
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( memoryCacheClassName );
        cattr.setMaxObjects( maxMemorySize );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(maxLife);

        CacheAccess<String, Integer> access = JCS.getInstance( "testGetMatching_Normal", cattr, attr );

        // DO WORK
        int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            access.put( keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        int numToInsertPrefix2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix2; i++ )
        {
            access.put( keyprefix2 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        Map<String, Integer> result1 = access.getMatching( keyprefix1 + ".+" );
        Map<String, Integer> result2 = access.getMatching( keyprefix2 + "\\S+" );

        // VERIFY
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2:", numToInsertPrefix2, result2.size() );
        //System.out.println( result1 );

        // verify that the elements are unwrapped
        for (Map.Entry<String, Integer> entry : result1.entrySet())
        {
            Object value = entry.getValue();
            assertFalse( "Should not be a cache element.", value instanceof ICacheElement );
        }
    }

    /**
     * Verify we can get some matching elements..
     * <p>
     * @throws Exception
     */
    public void testGetMatchingElements_Normal()
        throws Exception
    {
        // SETUP
        int maxMemorySize = 1000;
        String keyprefix1 = "MyPrefix1";
        String keyprefix2 = "MyPrefix2";
        String memoryCacheClassName = "org.apache.commons.jcs.engine.memory.lru.LRUMemoryCache";
        ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
        cattr.setMemoryCacheName( memoryCacheClassName );
        cattr.setMaxObjects( maxMemorySize );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLife(maxLife);

        CacheAccess<String, Integer> access = JCS.getInstance( "testGetMatching_Normal", cattr, attr );

        // DO WORK
        int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            access.put( keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        int numToInsertPrefix2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix2; i++ )
        {
            access.put( keyprefix2 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        Map<String, ICacheElement<String, Integer>> result1 = access.getMatchingCacheElements( keyprefix1 + "\\S+" );
        Map<String, ICacheElement<String, Integer>> result2 = access.getMatchingCacheElements( keyprefix2 + ".+" );

        // VERIFY
        assertEquals( "Wrong number returned 1:", numToInsertPrefix1, result1.size() );
        assertEquals( "Wrong number returned 2:", numToInsertPrefix2, result2.size() );
        //System.out.println( result1 );

        // verify that the elements are wrapped
        for (Map.Entry<String, ICacheElement<String, Integer>> entry : result1.entrySet())
        {
            Object value = entry.getValue();
            assertTrue( "Should be a cache element.", value instanceof ICacheElement );
        }
    }
}
