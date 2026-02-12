package org.apache.commons.jcs4.access;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs4.JCS;
import org.apache.commons.jcs4.access.exception.CacheException;
import org.apache.commons.jcs4.access.exception.ObjectExistsException;
import org.apache.commons.jcs4.engine.CompositeCacheAttributes;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.TestCompositeCacheAttributes;
import org.apache.commons.jcs4.engine.TestElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.ICompositeCacheAttributes;
import org.junit.jupiter.api.Test;

/**
 * Tests the methods of the cache access class.
 */
class CacheAccessUnitTest
{
    /**
     * Verify that getCacheElements returns the elements requested based on the key.
     * @throws Exception
     */
    @Test
    void testGetCacheElements()
        throws Exception
    {
        //SETUP
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String keyOne = "mykeyone";
        final String keyTwo = "mykeytwo";
        final String keyThree = "mykeythree";
        final String keyFour = "mykeyfour";
        final String valueOne = "myvalueone";
        final String valueTwo = "myvaluetwo";
        final String valueThree = "myvaluethree";
        final String valueFour = "myvaluefour";

        access.put( keyOne, valueOne );
        access.put( keyTwo, valueTwo );
        access.put( keyThree, valueThree );

        final Set<String> input = new HashSet<>();
        input.add( keyOne );
        input.add( keyTwo );

        //DO WORK
        final Map<String, ICacheElement<String, String>> result = access.getCacheElements( input );

        //VERIFY
        assertEquals( 2, result.size(), "map size" );
        final ICacheElement<String, String> elementOne = result.get( keyOne );
        assertEquals( keyOne, elementOne.key(), "value one" );
        assertEquals( valueOne, elementOne.value(), "value one" );
        final ICacheElement<String, String> elementTwo = result.get( keyTwo );
        assertEquals( keyTwo, elementTwo.key(), "value two" );
        assertEquals( valueTwo, elementTwo.value(), "value two" );

        assertNull(access.get(keyFour));
        final String suppliedValue1 = access.get(keyFour, () -> valueFour);
        assertNotNull( suppliedValue1, "value four" );
        assertEquals( valueFour, suppliedValue1, "value four" );
        final String suppliedValue2 = access.get(keyFour);
        assertNotNull( suppliedValue2, "value four" );
        assertEquals( suppliedValue1, suppliedValue2, "value four" );
    }

    /**
     * Verify we can get some matching elements..
     *
     * @throws Exception
     */
    @Test
    void testGetMatching_Normal()
        throws Exception
    {
        // SETUP
        final int maxMemorySize = 1000;
        final String keyprefix1 = "MyPrefix1";
        final String keyprefix2 = "MyPrefix2";
        final String memoryCacheClassName = "org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache";
        final CompositeCacheAttributes cattr = TestCompositeCacheAttributes
                .withMemoryCacheNameAndMaxObjects(memoryCacheClassName, maxMemorySize);

        final long maxLife = 9876;
        final ElementAttributes attr = TestElementAttributes.withEternalFalseAndMaxLife(maxLife);

        final CacheAccess<String, Integer> access = JCS.getInstance( "testGetMatching_Normal", cattr, attr );

        // DO WORK
        final int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            access.put( keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        final int numToInsertPrefix2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix2; i++ )
        {
            access.put( keyprefix2 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        final Map<String, Integer> result1 = access.getMatching( keyprefix1 + ".+" );
        final Map<String, Integer> result2 = access.getMatching( keyprefix2 + "\\S+" );

        // VERIFY
        assertEquals( numToInsertPrefix1, result1.size(), "Wrong number returned 1:" );
        assertEquals( numToInsertPrefix2, result2.size(), "Wrong number returned 2:" );
        //System.out.println( result1 );

        // verify that the elements are unwrapped
        for (final Map.Entry<String, Integer> entry : result1.entrySet())
        {
            final Object value = entry.getValue();
            assertFalse( value instanceof ICacheElement, "Should not be a cache element." );
        }
    }

    /**
     * Verify we can get some matching elements..
     *
     * @throws Exception
     */
    @Test
    void testGetMatchingElements_Normal()
        throws Exception
    {
        // SETUP
        final int maxMemorySize = 1000;
        final String keyprefix1 = "MyPrefix1";
        final String keyprefix2 = "MyPrefix2";
        final String memoryCacheClassName = "org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache";
        final CompositeCacheAttributes cattr = TestCompositeCacheAttributes
                .withMemoryCacheNameAndMaxObjects(memoryCacheClassName, maxMemorySize);

        final long maxLife = 9876;
        final ElementAttributes attr = TestElementAttributes.withEternalFalseAndMaxLife(maxLife);

        final CacheAccess<String, Integer> access = JCS.getInstance( "testGetMatching_Normal", cattr, attr );

        // DO WORK
        final int numToInsertPrefix1 = 10;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix1; i++ )
        {
            access.put( keyprefix1 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        final int numToInsertPrefix2 = 50;
        // insert with prefix1
        for ( int i = 0; i < numToInsertPrefix2; i++ )
        {
            access.put( keyprefix2 + String.valueOf( i ), Integer.valueOf( i ) );
        }

        final Map<String, ICacheElement<String, Integer>> result1 = access.getMatchingCacheElements( keyprefix1 + "\\S+" );
        final Map<String, ICacheElement<String, Integer>> result2 = access.getMatchingCacheElements( keyprefix2 + ".+" );

        // VERIFY
        assertEquals( numToInsertPrefix1, result1.size(), "Wrong number returned 1:" );
        assertEquals( numToInsertPrefix2, result2.size(), "Wrong number returned 2:" );
        //System.out.println( result1 );

        // verify that the elements are wrapped
        for (final Map.Entry<String, ICacheElement<String, Integer>> entry : result1.entrySet())
        {
            final Object value = entry.getValue();
            assertInstanceOf( ICacheElement.class, value, "Should be a cache element." );
        }
    }

    /**
     * Try to put a null key and verify that we get an exception.
     * @throws Exception
     */
    @Test
    void testPutNullKey()
        throws Exception
    {
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = null;
        final String value = "myvalue";

        try
        {
            access.put( key, value );
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
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "myKey";
        final String value = null;

        try
        {
            access.put( key, value );
            fail( "Should not have been able to put a null object." );
        }
        catch ( final CacheException e )
        {
            assertTrue( e.getMessage().indexOf( "null" ) != -1, "Should have the word null in the error message." );
        }
    }

    /**
     * Verify that we get an object exists exception if the item is in the cache.
     * @throws Exception
     */
    @Test
    void testPutSafe()
        throws Exception
    {
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final String key = "mykey";
        final String value = "myvalue";

        access.put( key, value );

        final String returnedValue1 = access.get( key );
        assertEquals( value, returnedValue1, "Wrong value returned." );

        try
        {
            access.putSafe( key, "someothervalue" );
            fail( "We should have received an exception since this key is already in the cache." );
        }
        catch ( final CacheException e )
        {
            assertInstanceOf( ObjectExistsException.class, e, "Wrong type of exception." );
            assertTrue( e.getMessage().indexOf( "[" + key + "]" ) != -1, "Should have the key in the error message." );
        }

        final String returnedValue2 = access.get( key );
        assertEquals( value, returnedValue2, "Wrong value returned.  Should still be the original." );
    }

    /**
     * Verify that we can get a region using the define region method.
     * @throws Exception
     */
    @Test
    void testRegionDefiniton()
        throws Exception
    {
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes.
     * @throws Exception
     */
    @Test
    void testRegionDefinitonWithAttributes()
        throws Exception
    {
        final long maxIdleTime = 8765;
        final CompositeCacheAttributes ca = TestCompositeCacheAttributes
                .withMaxMemoryIdleTimeSeconds(maxIdleTime);

        final CacheAccess<String, String> access = JCS.getInstance( "testRegionDefinitonWithAttributes", ca );
        assertNotNull( access, "We should have an access class" );

        final ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( ca.maxMemoryIdleTimeSeconds(), ca2.maxMemoryIdleTimeSeconds(), "Wrong idle time setting." );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes and
     * element attributes.
     * @throws Exception
     */
    @Test
    void testRegionDefinitonWithBothAttributes()
        throws Exception
    {
        final long maxIdleTime = 8765;
        final CompositeCacheAttributes ca = TestCompositeCacheAttributes
                .withMaxMemoryIdleTimeSeconds(maxIdleTime);

        final long maxLife = 9876;
        final ElementAttributes attr = TestElementAttributes.withEternalFalseAndMaxLife(maxLife);

        final CacheAccess<String, String> access = JCS.getInstance( "testRegionDefinitonWithAttributes", ca, attr );
        assertNotNull( access, "We should have an access class" );

        final ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( ca.maxMemoryIdleTimeSeconds(), ca2.maxMemoryIdleTimeSeconds(), "Wrong idle time setting." );
    }

    /**
     * Verify that elements that go in the region after this call take the new attributes.
     * @throws Exception
     */
    @Test
    void testSetDefaultElementAttributes()
        throws Exception
    {
        final CacheAccess<String, String> access = JCS.getInstance( "test" );
        assertNotNull( access, "We should have an access class" );

        final long maxLife = 9876;
        final ElementAttributes attr = TestElementAttributes.withEternalFalseAndMaxLife(maxLife);

        access.setDefaultElementAttributes( attr );

        assertEquals( attr.maxLife(), access.getDefaultElementAttributes()
            .maxLife(), "Wrong element attributes." );

        final String key = "mykey";
        final String value = "myvalue";

        access.put( key, value );

        final ICacheElement<String, String> element = access.getCacheElement( key );

        assertEquals( maxLife, element.elementAttributes()
            .maxLife(), "Wrong max life.  Should have the new value." );
    }
}
