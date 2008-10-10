package org.apache.jcs.engine;

import org.apache.jcs.auxiliary.remote.MockRemoteCacheListener;
import org.apache.jcs.engine.behavior.ICacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;

import junit.framework.TestCase;

/** Unit tests for the CacheEventQueueFactory */
public class CacheEventQueueFactoryUnitTest
    extends TestCase
{
    /** Test create */
    public void testCreateCacheEventQueue_Single()
    {
        // SETUP
        String eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
        ICacheListener listener = new MockRemoteCacheListener();
        long listenerId = 1;
        
        CacheEventQueueFactory factory = new CacheEventQueueFactory();
        
        // DO WORK
        ICacheEventQueue result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof CacheEventQueue );
    }
    
    /** Test create */
    public void testCreateCacheEventQueue_Pooled()
    {
        // SETUP
        String eventQueueType = ICacheEventQueue.POOLED_QUEUE_TYPE;
        ICacheListener listener = new MockRemoteCacheListener();
        long listenerId = 1;
        
        CacheEventQueueFactory factory = new CacheEventQueueFactory();
        
        // DO WORK
        ICacheEventQueue result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type", result instanceof PooledCacheEventQueue );
    }
    
    /** Test create */
    public void testCreateCacheEventQueue_Custom()
    {
        // SETUP
        String eventQueueType = MockCacheEventQueue.class.getName();
        ICacheListener listener = new MockRemoteCacheListener();
        long listenerId = 1;
        
        CacheEventQueueFactory factory = new CacheEventQueueFactory();
        
        // DO WORK
        ICacheEventQueue result = factory.createCacheEventQueue( listener, listenerId, "cacheName", "threadPoolName", eventQueueType );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertTrue( "Wrong type: " + result, result instanceof MockCacheEventQueue );
    }
}