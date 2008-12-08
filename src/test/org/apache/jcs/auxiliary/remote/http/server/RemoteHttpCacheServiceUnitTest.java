package org.apache.jcs.auxiliary.remote.http.server;

import java.io.Serializable;
import java.util.HashSet;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.MockCacheEventLogger;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.control.MockCompositeCacheManager;

/** Unit tests for the service. */
public class RemoteHttpCacheServiceUnitTest
    extends TestCase
{
    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testUpdate_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        CacheElement element = new CacheElement( cacheName, key, null );
        
        // DO WORK
        server.update( element, requesterId );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }
    
    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGet_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.get( "region", "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGetMatching_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMatching( "region", "pattern", 0 );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testGetMultiple_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.getMultiple( "region", new HashSet() );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testRemove_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.remove( "region", "key" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }

    /**
     * Verify event log calls.
     * <p>
     * @throws Exception
     */
    public void testRemoveAll_simple()
        throws Exception
    {
        // SETUP
        MockCompositeCacheManager manager = new  MockCompositeCacheManager();
        MockCacheEventLogger cacheEventLogger = new MockCacheEventLogger();
        
        RemoteHttpCacheServerAttributes rcsa = new RemoteHttpCacheServerAttributes();
        RemoteHttpCacheService server = new RemoteHttpCacheService( manager, rcsa, cacheEventLogger );

        // DO WORK
        server.removeAll( "region" );

        // VERIFY
        assertEquals( "Start should have been called.", 1, cacheEventLogger.startICacheEventCalls );
        assertEquals( "End should have been called.", 1, cacheEventLogger.endICacheEventCalls );
    }
}
