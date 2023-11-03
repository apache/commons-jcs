package org.apache.commons.jcs3.auxiliary.disk;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.jcs3.engine.CacheElement;
import org.apache.commons.jcs3.engine.ElementAttributes;
import org.apache.commons.jcs3.engine.behavior.ICacheElement;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;
import org.junit.Test;

/** Simple unit tests for the Purgatory Element. */
public class PurgatoryElementUnitTest
{
    /** Verify basic data */
    @Test
    public void testElementAttributes_normal()
    {
        // SETUP
        final String cacheName = "myCacheName";
        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();

        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value );
        final PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<>( cacheElement );
        purgatoryElement.setElementAttributes( elementAttributes );

        // DO WORK
        final IElementAttributes result = cacheElement.getElementAttributes();

        // VERIFY
        assertEquals( "Should have set the attributes on the element", elementAttributes, result );
    }

    /** Verify basic data */
    @Test
    public void testSpoolable_normal()
    {
        // SETUP
        final String cacheName = "myCacheName";
        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );
        final PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<>( cacheElement );
        purgatoryElement.setSpoolable( false );

        // DO WORK
        final boolean result = purgatoryElement.isSpoolable();

        // VERIFY
        assertFalse( "Should not be spoolable.", result );
    }

    /** Verify basic data */
    @Test
    public void testToString_normal()
    {
        // SETUP
        final String cacheName = "myCacheName";
        final String key = "myKey";
        final String value = "myValue";
        final IElementAttributes elementAttributes = new ElementAttributes();
        final ICacheElement<String, String> cacheElement = new CacheElement<>( cacheName, key, value, elementAttributes );
        final PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<>( cacheElement );

        // DO WORK
        final String result = purgatoryElement.toString();

        // VERIFY
        assertTrue( "Should have the cacheName.", result.indexOf( cacheName ) != -1 );
        assertTrue( "Should have the key.", result.indexOf( key ) != -1 );
        assertTrue( "Should have the value.", result.indexOf( value ) != -1 );
    }
}
