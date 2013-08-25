package org.apache.commons.jcs.engine;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.remote.MockRemoteCacheListener;
import org.apache.commons.jcs.engine.CacheEventQueue;
import org.apache.commons.jcs.engine.CacheEventQueueFactory;
import org.apache.commons.jcs.engine.PooledCacheEventQueue;
import org.apache.commons.jcs.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs.engine.behavior.ICacheListener;

/** Unit tests for the CacheEventQueueFactory */
public class CacheEventQueueFactoryUnitTest
    extends TestCase
{
    /** Test create */
    public void testCreateCacheEventQueue_Single()
    {
        // SETUP
        String eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
        ICacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        long listenerId = 1;

        CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<String, String>();

        // DO WORK
        ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof CacheEventQueue );
    }

    /** Test create */
    public void testCreateCacheEventQueue_Pooled()
    {
        // SETUP
        String eventQueueType = ICacheEventQueue.POOLED_QUEUE_TYPE;
        ICacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        long listenerId = 1;

        CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<String, String>();

        // DO WORK
        ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof PooledCacheEventQueue );
    }

    /** Test create */
    public void testCreateCacheEventQueue_Custom()
    {
        // SETUP
        String eventQueueType = MockCacheEventQueue.class.getName();
        ICacheListener<String, String> listener = new MockRemoteCacheListener<String, String>();
        long listenerId = 1;

        CacheEventQueueFactory<String, String> factory = new CacheEventQueueFactory<String, String>();

        // DO WORK
        ICacheEventQueue<String, String> result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type: " + result, result instanceof MockCacheEventQueue );
    }
}