package org.apache.jcs.engine.memory.shrinking;

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

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.event.ElementEventHandlerMockImpl;
import org.apache.jcs.engine.memory.MemoryCacheMockImpl;

/**
 * This tests the functionality of the shrinker thread.
 *
 * @author Aaron Smuts
 *
 */
public class ShrinkerThreadUnitTest
    extends TestCase
{

    /**
     * Setup cache attributes in mock. Create the shrinker with the mock. Add
     * some elements into the mock memory cache see that they get spooled.
     *
     * @throws Exception
     *
     */
    public void testSimpleShrink()
        throws Exception
    {
        MemoryCacheMockImpl memory = new MemoryCacheMockImpl();

        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
        cacheAttr.setMaxSpoolPerRun( 10 );

        memory.setCacheAttributes( cacheAttr );

        String key = "key";
        String value = "value";

        ICacheElement element = new CacheElement( "testRegion", key, value );

        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLifeSeconds( 1 );
        memory.update( element );

        ICacheElement returnedElement1 = memory.get( key );
        assertNotNull( "We should have received an element", returnedElement1 );

        // set this to 2 seconds ago.
        elementAttr.lastAccessTime = System.currentTimeMillis() - 2000;

        ShrinkerThread shrinker = new ShrinkerThread( memory );
        Thread runner = new Thread( shrinker );
        runner.run();

        Thread.sleep( 500 );

        ICacheElement returnedElement2 = memory.get( key );
        assertTrue( "Waterfall should have been called.", memory.waterfallCallCount > 0 );
        assertNull( "We not should have received an element.  It should have been spooled.", returnedElement2 );
    }

    /**
     * Add 10 to the memory cache. Set the spool per run limit to 3.
     *
     * @throws Exception
     */
    public void testSimpleShrinkMutiple()
        throws Exception
    {
        MemoryCacheMockImpl memory = new MemoryCacheMockImpl();

        CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
        cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
        cacheAttr.setMaxSpoolPerRun( 3 );

        memory.setCacheAttributes( cacheAttr );

        for ( int i = 0; i < 10; i++ )
        {
            String key = "key" + i;
            String value = "value";

            ICacheElement element = new CacheElement( "testRegion", key, value );

            ElementAttributes elementAttr = new ElementAttributes();
            elementAttr.setIsEternal( false );
            element.setElementAttributes( elementAttr );
            element.getElementAttributes().setMaxLifeSeconds( 1 );
            memory.update( element );

            ICacheElement returnedElement1 = memory.get( key );
            assertNotNull( "We should have received an element", returnedElement1 );

            // set this to 2 seconds ago.
            elementAttr.lastAccessTime = System.currentTimeMillis() - 2000;
        }

        ShrinkerThread shrinker = new ShrinkerThread( memory );
        Thread runner = new Thread( shrinker );
        runner.run();

        Thread.sleep( 500 );

        assertEquals( "Waterfall called the wrong number of times.", 3, memory.waterfallCallCount );

        assertEquals( "Wrong number of elements remain.", 7, memory.getSize() );
    }

    /**
     * Add a mock event handler to the items.  Verify that it gets called.
     * <p>
     * This is only testing the spooled background event
     *
     * @throws Exception
     */
    public void testSimpleShrinkMutipleWithEventHandler()
    throws Exception
{
    MemoryCacheMockImpl memory = new MemoryCacheMockImpl();

    CompositeCacheAttributes cacheAttr = new CompositeCacheAttributes();
    cacheAttr.setMaxMemoryIdleTimeSeconds( 1 );
    cacheAttr.setMaxSpoolPerRun( 3 );

    memory.setCacheAttributes( cacheAttr );

    ElementEventHandlerMockImpl handler = new ElementEventHandlerMockImpl();

    for ( int i = 0; i < 10; i++ )
    {
        String key = "key" + i;
        String value = "value";

        ICacheElement element = new CacheElement( "testRegion", key, value );

        ElementAttributes elementAttr = new ElementAttributes();
        elementAttr.addElementEventHandler( handler );
        elementAttr.setIsEternal( false );
        element.setElementAttributes( elementAttr );
        element.getElementAttributes().setMaxLifeSeconds( 1 );
        memory.update( element );

        ICacheElement returnedElement1 = memory.get( key );
        assertNotNull( "We should have received an element", returnedElement1 );

        // set this to 2 seconds ago.
        elementAttr.lastAccessTime = System.currentTimeMillis() - 2000;
    }

    ShrinkerThread shrinker = new ShrinkerThread( memory );
    Thread runner = new Thread( shrinker );
    runner.run();

    Thread.sleep( 500 );

    assertEquals( "Waterfall called the wrong number of times.", 3, memory.waterfallCallCount );

    // the shrinker delegates the the composite cache on the memory cache to put the
    // event on the queue.  This make it hard to test.  TODO we need to change this to make it easier to verify.
    //assertEquals( "Event handler ExceededIdleTimeBackground called the wrong number of times.", 3, handler.getExceededIdleTimeBackgroundCount() );

    assertEquals( "Wrong number of elements remain.", 7, memory.getSize() );
}


}
