package org.apache.commons.jcs.engine.memory.shrinking;

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
import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.CompositeCacheAttributes;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.ElementAttributesUtils;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.control.event.ElementEventHandlerMockImpl;
import org.apache.commons.jcs.engine.control.event.behavior.ElementEventType;
import org.apache.commons.jcs.engine.memory.MockMemoryCache;

import java.io.IOException;

/**
 * This tests the functionality of the shrinker thread.
 * <p>
 * @author Aaron Smuts
 */
public class ShrinkerThreadUnitTest
    extends TestCase
{
    /** verify the check for removal
     * <p>
     * @throws IOException */
    public void testCheckForRemoval_Expired() throws IOException
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 10 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());

        String key = "key";
        String value = "value";

        ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );
        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLife(1);

        long now = System.currentTimeMillis();
        // add two seconds
        now += 2000;

        // DO WORK
        boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertTrue( "Item should have expired.", result );
    }

    /** verify the check for removal
     * <p>
     * @throws IOException */
    public void testCheckForRemoval_NotExpired() throws IOException
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 10 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());

        String key = "key";
        String value = "value";

        ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );
        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLife(1);

        long now = System.currentTimeMillis();
        // subtract two seconds
        now -= 2000;

        // DO WORK
        boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertFalse( "Item should not have expired.", result );
    }

    /** verify the check for removal
     * <p>
     * @throws IOException */
    public void testCheckForRemoval_IdleTooLong() throws IOException
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 10 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());

        String key = "key";
        String value = "value";

        ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );
        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLife(100);
        element.getElementAttributes().setIdleTime( 1 );

        long now = System.currentTimeMillis();
        // add two seconds
        now += 2000;

        // DO WORK
        boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertTrue( "Item should have expired.", result );
    }

    /** verify the check for removal
     * <p>
     * @throws IOException */
    public void testCheckForRemoval_NotIdleTooLong() throws IOException
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 10 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());

        String key = "key";
        String value = "value";

        ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );
        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLife(100);
        element.getElementAttributes().setIdleTime( 1 );

        long now = System.currentTimeMillis();
        // subtract two seconds
        now -= 2000;

        // DO WORK
        boolean result = cache.isExpired( element, now,
                ElementEventType.EXCEEDED_MAXLIFE_BACKGROUND,
                ElementEventType.EXCEEDED_IDLETIME_BACKGROUND );

        // VERIFY
        assertFalse( "Item should not have expired.", result );
    }

    /**
     * Setup cache attributes in mock. Create the shrinker with the mock. Add some elements into the
     * mock memory cache see that they get spooled.
     * <p>
     * @throws Exception
     */
    public void testSimpleShrink()
        throws Exception
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMemoryCacheName("org.apache.commons.jcs.engine.memory.MockMemoryCache");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());
        MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        String key = "key";
        String value = "value";

        ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );

        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLife(1);
        memory.update( element );

        ICacheElement<String, String> returnedElement1 = memory.get( key );
        assertNotNull( "We should have received an element", returnedElement1 );

        // set this to 2 seconds ago.
        ElementAttributesUtils.setLastAccessTime( elementAttr,  System.currentTimeMillis() - 2000 );

        // DO WORK
        ShrinkerThread<String, String> shrinker = new ShrinkerThread<String, String>( cache );
        shrinker.run();

        Thread.sleep( 500 );

        // VERIFY
        ICacheElement<String, String> returnedElement2 = memory.get( key );
        assertTrue( "Waterfall should have been called.", memory.waterfallCallCount > 0 );
        assertNull( "We not should have received an element.  It should have been spooled.", returnedElement2 );
    }

    /**
     * Add 10 to the memory cache. Set the spool per run limit to 3.
     * <p>
     * @throws Exception
     */
    public void testSimpleShrinkMultiple()
        throws Exception
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMemoryCacheName("org.apache.commons.jcs.engine.memory.MockMemoryCache");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
        cacheAttr.setMaxSpoolPerRun( 3 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());
        MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        for ( int i = 0; i < 10; i++ )
        {
            String key = "key" + i;
            String value = "value";

            ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );

            ElementAttributes elementAttr = new ElementAttributes();
            elementAttr.setIsEternal( false );
            element.setElementAttributes( elementAttr );
            element.getElementAttributes().setMaxLife(1);
            memory.update( element );

            ICacheElement<String, String> returnedElement1 = memory.get( key );
            assertNotNull( "We should have received an element", returnedElement1 );

            // set this to 2 seconds ago.
            ElementAttributesUtils.setLastAccessTime( elementAttr,  System.currentTimeMillis() - 2000 );
        }

        // DO WORK
        ShrinkerThread<String, String> shrinker = new ShrinkerThread<String, String>( cache );
        shrinker.run();

        // VERIFY
        Thread.sleep( 500 );
        assertEquals( "Waterfall called the wrong number of times.", 3, memory.waterfallCallCount );
        assertEquals( "Wrong number of elements remain.", 7, memory.getSize() );
    }

    /**
     * Add a mock event handler to the items. Verify that it gets called.
     * <p>
     * This is only testing the spooled background event
     * <p>
     * @throws Exception
     */
    public void testSimpleShrinkMultipleWithEventHandler()
        throws Exception
    {
        // SETUP
        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setCacheName("testRegion");
        cacheAttr.setMemoryCacheName("org.apache.commons.jcs.engine.memory.MockMemoryCache");
        cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
        cacheAttr.setMaxSpoolPerRun( 3 );

        CompositeCache<String, String> cache = new CompositeCache<String, String>(cacheAttr, new ElementAttributes());
        MockMemoryCache<String, String> memory = (MockMemoryCache<String, String>)cache.getMemoryCache();

        ElementEventHandlerMockImpl handler = new ElementEventHandlerMockImpl();

        for ( int i = 0; i < 10; i++ )
        {
            String key = "key" + i;
            String value = "value";

            ICacheElement<String, String> element = new CacheElement<String, String>( "testRegion", key, value );

            ElementAttributes elementAttr = new ElementAttributes();
            elementAttr.addElementEventHandler( handler );
            elementAttr.setIsEternal( false );
            element.setElementAttributes( elementAttr );
            element.getElementAttributes().setMaxLife(1);
            memory.update( element );

            ICacheElement<String, String> returnedElement1 = memory.get( key );
            assertNotNull( "We should have received an element", returnedElement1 );

            // set this to 2 seconds ago.
            ElementAttributesUtils.setLastAccessTime( elementAttr,  System.currentTimeMillis() - 2000 );
        }

        // DO WORK
        ShrinkerThread<String, String> shrinker = new ShrinkerThread<String, String>( cache );
        shrinker.run();

        // VERIFY
        Thread.sleep( 500 );
        assertEquals( "Waterfall called the wrong number of times.", 3, memory.waterfallCallCount );
        // the shrinker delegates the the composite cache on the memory cache to put the
        // event on the queue.  This make it hard to test.  TODO we need to change this to make it easier to verify.
        //assertEquals( "Event handler ExceededIdleTimeBackground called the wrong number of times.", 3, handler.getExceededIdleTimeBackgroundCount() );
        assertEquals( "Wrong number of elements remain.", 7, memory.getSize() );
    }
}
