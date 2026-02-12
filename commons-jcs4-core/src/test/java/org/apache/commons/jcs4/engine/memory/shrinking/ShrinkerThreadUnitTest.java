package org.apache.commons.jcs4.engine.memory.shrinking;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.commons.jcs4.engine.CacheElement;
import org.apache.commons.jcs4.engine.CompositeCacheAttributes;
import org.apache.commons.jcs4.engine.ElementAttributes;
import org.apache.commons.jcs4.engine.TestCompositeCacheAttributes;
import org.apache.commons.jcs4.engine.TestElementAttributes;
import org.apache.commons.jcs4.engine.behavior.ICacheElement;
import org.apache.commons.jcs4.engine.control.CompositeCache;
import org.apache.commons.jcs4.engine.control.event.ElementEventHandlerMockImpl;
import org.apache.commons.jcs4.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs4.engine.memory.MockMemoryCache;
import org.junit.jupiter.api.Test;

/**
 * This tests the functionality of the shrinker thread.
 */
class ShrinkerThreadUnitTest
{
    /** Verify the check for removal
     *
     * @throws IOException */
    @Test
    void testCheckForRemoval_Expired()
        throws IOException
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        CompositeCacheAttributes.defaults().memoryCacheName(), 10, 10)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());

        final String key = "key";
        final String value = "value";

        final ICacheElement<String, String> element = new CacheElement<>( "testRegion", key, value,
                TestElementAttributes.withEternalFalseAndMaxLife(1));

        long now = System.currentTimeMillis();
        // add two seconds
        now += 2000;

        // DO WORK
        final boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertTrue( result, "Item should have expired." );
    }

    /** Verify the check for removal
     *
     * @throws IOException */
    @Test
    void testCheckForRemoval_IdleTooLong()
        throws IOException
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        CompositeCacheAttributes.defaults().memoryCacheName(), 10, 10)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());

        final String key = "key";
        final String value = "value";

        final ICacheElement<String, String> element = new CacheElement<>( "testRegion", key, value,
                TestElementAttributes.withEternalFalseAndMaxLifeAndMaxIdleTime(100, 1));

        long now = System.currentTimeMillis();
        // add two seconds
        now += 2000;

        // DO WORK
        final boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertTrue( result, "Item should have expired." );
    }

    /** Verify the check for removal
     *
     * @throws IOException */
    @Test
    void testCheckForRemoval_NotExpired()
        throws IOException
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        CompositeCacheAttributes.defaults().memoryCacheName(), 10, 10)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());

        final String key = "key";
        final String value = "value";

        final ICacheElement<String, String> element = new CacheElement<>( "testRegion", key, value,
                TestElementAttributes.withEternalFalseAndMaxLife(1));

        long now = System.currentTimeMillis();
        // subtract two seconds
        now -= 2000;

        // DO WORK
        final boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertFalse( result, "Item should not have expired." );
    }

    /** Verify the check for removal
     *
     * @throws IOException */
    @Test
    void testCheckForRemoval_NotIdleTooLong()
        throws IOException
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        CompositeCacheAttributes.defaults().memoryCacheName(), 10, 10)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());

        final String key = "key";
        final String value = "value";

        final ICacheElement<String, String> element = new CacheElement<>( "testRegion", key, value,
                TestElementAttributes.withEternalFalseAndMaxLifeAndMaxIdleTime(100, 1));

        long now = System.currentTimeMillis();
        // subtract two seconds
        now -= 2000;

        // DO WORK
        final boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertFalse( result, "Item should not have expired." );
    }

    /**
     * Setup cache attributes in mock. Create the shrinker with the mock. Add some elements into the
     * mock memory cache see that they get spooled.
     *
     * @throws Exception
     */
    @Test
    void testSimpleShrink()
        throws Exception
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        "org.apache.commons.jcs4.engine.memory.MockMemoryCache", 1, 10)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());
        final MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        final String key = "key";
        final String value = "value";

        final ElementAttributes elementAttr = TestElementAttributes.withEternalFalseAndMaxLife(1);
        final ICacheElement<String, String> element = new CacheElement<>("testRegion",
                key, value, elementAttr);
        memory.update( element );

        final ICacheElement<String, String> returnedElement1 = memory.get( key );
        assertNotNull( returnedElement1, "We should have received an element" );

        // set this to 2 seconds ago.
        elementAttr.mutableLastAccessTime().set(System.currentTimeMillis() - 2000);

        // DO WORK
        final ShrinkerThread<String, String> shrinker = new ShrinkerThread<>( cache );
        shrinker.run();

        Thread.sleep( 500 );

        // VERIFY
        final ICacheElement<String, String> returnedElement2 = memory.get( key );
        assertTrue( memory.waterfallCallCount > 0, "Waterfall should have been called." );
        assertNull( returnedElement2, "We not should have received an element.  It should have been spooled." );
    }

    /**
     * Add 10 to the memory cache. Set the spool per run limit to 3.
     *
     * @throws Exception
     */
    @Test
    void testSimpleShrinkMultiple()
        throws Exception
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        "org.apache.commons.jcs4.engine.memory.MockMemoryCache", 1, 3)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());
        final MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        for ( int i = 0; i < 10; i++ )
        {
            final String key = "key" + i;
            final String value = "value";

            final ElementAttributes elementAttr = TestElementAttributes.withEternalFalseAndMaxLife(1);
            final ICacheElement<String, String> element = new CacheElement<>( "testRegion",
                    key, value, elementAttr);
            memory.update( element );

            final ICacheElement<String, String> returnedElement1 = memory.get( key );
            assertNotNull( returnedElement1, "We should have received an element" );

            // set this to 2 seconds ago.
            elementAttr.mutableLastAccessTime().set(System.currentTimeMillis() - 2000);
        }

        // DO WORK
        final ShrinkerThread<String, String> shrinker = new ShrinkerThread<>( cache );
        shrinker.run();

        // VERIFY
        Thread.sleep( 500 );
        assertEquals( 3, memory.waterfallCallCount, "Waterfall called the wrong number of times." );
        assertEquals( 7, memory.getSize(), "Wrong number of elements remain." );
    }

    /**
     * Add a mock event handler to the items. Verify that it gets called.
     * <p>
     * This is only testing the spooled background event
     *
     * @throws Exception
     */
    @Test
    void testSimpleShrinkMultipleWithEventHandler()
        throws Exception
    {
        // SETUP
        final CompositeCacheAttributes cacheAttr = TestCompositeCacheAttributes
                .withMemoryCacheNameMaxMemoryIdleTimeSecondsAndMaxSpoolPerRun(
                        "org.apache.commons.jcs4.engine.memory.MockMemoryCache", 1, 3)
                .withCacheName("testRegion");

        final CompositeCache<String, String> cache = new CompositeCache<>(cacheAttr, new ElementAttributes());
        final MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        final ElementEventHandlerMockImpl handler = new ElementEventHandlerMockImpl();

        for ( int i = 0; i < 10; i++ )
        {
            final String key = "key" + i;
            final String value = "value";

            final ElementAttributes elementAttr = TestElementAttributes.withEternalFalseAndMaxLife(1);
            final ICacheElement<String, String> element = new CacheElement<>( "testRegion",
                    key, value, elementAttr);
            elementAttr.addElementEventHandler( handler );
            memory.update( element );

            final ICacheElement<String, String> returnedElement1 = memory.get( key );
            assertNotNull( returnedElement1, "We should have received an element" );

            // set this to 2 seconds ago.
            elementAttr.mutableLastAccessTime().set(System.currentTimeMillis() - 2000);
        }

        // DO WORK
        final ShrinkerThread<String, String> shrinker = new ShrinkerThread<>( cache );
        shrinker.run();

        // VERIFY
        Thread.sleep( 500 );
        assertEquals( 3, memory.waterfallCallCount, "Waterfall called the wrong number of times." );
        // the shrinker delegates the composite cache on the memory cache to put the
        // event on the queue.  This make it hard to test.  TODO we need to change this to make it easier to verify.
        //assertEquals( "Event handler ExceededIdleTimeBackground called the wrong number of times.", 3, handler.getExceededIdleTimeBackgroundCount() );
        assertEquals( 7, memory.getSize(), "Wrong number of elements remain." );
    }
}
