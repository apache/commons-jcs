package org.apache.commons.jcs4.auxiliary.disk;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.behavior.IElementAttributes;
import org.junit.jupiter.api.Test;

/** Simple unit tests for the Purgatory Element. */
class PurgatoryElementUnitTest
{
    /** Verify basic data */
    @Test
    void testElementAttributes_normal()
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
        assertEquals( elementAttributes, result, "Should have set the attributes on the element" );
    }

    /** Verify basic data */
    @Test
    void testSpoolable_normal()
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
        assertFalse( result, "Should not be spoolable." );
    }

    /** Verify basic data */
    @Test
    void testToString_normal()
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
        assertTrue( result.indexOf( cacheName ) != -1, "Should have the cacheName." );
        assertTrue( result.indexOf( key ) != -1, "Should have the key." );
        assertTrue( result.indexOf( value ) != -1, "Should have the value." );
    }
}
