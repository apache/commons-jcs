package org.apache.jcs.access;

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

import junit.framework.TestCase;

import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;

/**
 * Tests the methods of the cache access class from which the class JCS extends.
 *
 * @author Aaron Smuts
 *
 */
public class CacheAccessUnitTest
    extends TestCase
{

    /**
     * Verify that we get an object exists exception if the item is in the
     * cache.
     *
     */
    public void testPutSafe()
    {

        CacheAccess access = null;
        try
        {
            access = CacheAccess.getAccess( "test" );

            assertNotNull( "We should have an access class", access );
        }
        catch ( CacheException e )
        {
            fail( "Shouldn't have received an error." + e.getMessage() );
        }

        String key = "mykey";
        String value = "myvalue";

        try
        {
            access.put( key, value );
        }
        catch ( CacheException e )
        {
            fail( "Should have been able to put " + e.getMessage() );
        }
        String returnedValue1 = (String) access.get( key );
        assertEquals( "Wrong value returned.", value, returnedValue1 );

        try
        {
            access.putSafe( key, "someothervalue" );
            fail( "We should have received an eception since this key is alredy in the cache." );
        }
        catch ( CacheException e )
        {
            // e.printStackTrace();
            // expected
            assertTrue( "Wrong type of exception.", e instanceof ObjectExistsException );
            assertTrue( "Should have the key in the error message.", e.getMessage().indexOf( "[" + key + "]" ) != -1 );
        }

        String returnedValue2 = (String) access.get( key );
        assertEquals( "Wrong value returned.  Shoudl still be the original.", value, returnedValue2 );
    }

    /**
     * Try to put a null key and verify that we get an exception.
     *
     */
    public void testPutNullKey()
    {

        CacheAccess access = null;
        try
        {
            access = CacheAccess.getAccess( "test" );

            assertNotNull( "We should have an access class", access );
        }
        catch ( CacheException e )
        {
            fail( "Shouldn't have received an error." + e.getMessage() );
        }

        String key = null;
        String value = "myvalue";

        try
        {
            access.put( key, value );
            fail( "Should not have been able to put a null key." );
        }
        catch ( CacheException e )
        {
            // expected
            assertTrue( "Should have the work null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Try to put a null value and verify that we get an exception.
     *
     */
    public void testPutNullValue()
    {

        CacheAccess access = null;
        try
        {
            access = CacheAccess.getAccess( "test" );

            assertNotNull( "We should have an access class", access );
        }
        catch ( CacheException e )
        {
            fail( "Shouldn't have received an error." + e.getMessage() );
        }

        String key = "myKey";
        String value = null;

        try
        {
            access.put( key, value );
            fail( "Should not have been able to put a null object." );
        }
        catch ( CacheException e )
        {
            // expected
            assertTrue( "Should have the work null in the error message.", e.getMessage().indexOf( "null" ) != -1 );
        }
    }

    /**
     * Verify that elements that go in the region after this call takethe new
     * attributes.
     *
     * @throws Exception
     *
     */
    public void testSetDefaultElementAttributes()
        throws Exception
    {

        CacheAccess access = null;

        access = CacheAccess.getAccess( "test" );

        assertNotNull( "We should have an access class", access );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( maxLife );

        access.setDefaultElementAttributes( attr );

        assertEquals( "Wrong element attributes.", attr.getMaxLifeSeconds(), access.getDefaultElementAttributes()
            .getMaxLifeSeconds() );

        String key = "mykey";
        String value = "myvalue";

        access.put( key, value );

        ICacheElement element = access.getCacheElement( key );

        assertEquals( "Wrong max life.  Should have the new value.", maxLife, element.getElementAttributes()
            .getMaxLifeSeconds() );
    }

    /**
     * Verify that we can get a region using the define region method.
     *
     * @throws Exception
     *
     */
    public void testRegionDefiniton()
        throws Exception
    {
        CacheAccess access = CacheAccess.defineRegion( "test" );
        assertNotNull( "We should have an access class", access );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes.
     *
     * @throws Exception
     *
     */
    public void testRegionDefinitonWithAttributes()
        throws Exception
    {
        ICompositeCacheAttributes ca = new CompositeCacheAttributes();

        long maxIdleTime = 8765;
        ca.setMaxMemoryIdleTimeSeconds( maxIdleTime );

        CacheAccess access = CacheAccess.defineRegion( "testRegionDefinitonWithAttributes", ca );
        assertNotNull( "We should have an access class", access );

        ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( "Wrong idle time setting.", ca.getMaxMemoryIdleTimeSeconds(), ca2.getMaxMemoryIdleTimeSeconds() );
    }

    /**
     * Verify that we can get a region using the define region method with cache attributes and elemetn attributes.
     *
     * @throws Exception
     *
     */
    public void testRegionDefinitonWithBothAttributes()
        throws Exception
    {
        ICompositeCacheAttributes ca = new CompositeCacheAttributes();

        long maxIdleTime = 8765;
        ca.setMaxMemoryIdleTimeSeconds( maxIdleTime );

        long maxLife = 9876;
        IElementAttributes attr = new ElementAttributes();
        attr.setMaxLifeSeconds( maxLife );

        CacheAccess access = CacheAccess.defineRegion( "testRegionDefinitonWithAttributes", ca, attr );
        assertNotNull( "We should have an access class", access );

        ICompositeCacheAttributes ca2 = access.getCacheAttributes();
        assertEquals( "Wrong idle time setting.", ca.getMaxMemoryIdleTimeSeconds(), ca2.getMaxMemoryIdleTimeSeconds() );
    }

}
