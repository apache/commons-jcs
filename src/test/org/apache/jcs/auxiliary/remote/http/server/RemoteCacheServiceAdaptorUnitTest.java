package org.apache.jcs.auxiliary.remote.http.server;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.MockRemoteCacheService;
import org.apache.jcs.auxiliary.remote.http.client.RemoteHttpClientRequestFactory;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheRequest;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheResponse;
import org.apache.jcs.engine.CacheElement;

/** Unit tests for the adaptor. */
public class RemoteCacheServiceAdaptorUnitTest
    extends TestCase
{
    /** Verify that the service is called. */
    public void testProcessRequest_Get()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createGetRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastGetKey );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMatching()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                  requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong pattern.", pattern, remoteHttpCacheService.lastGetMatchingPattern );
    }

    /** Verify that the service is called. */
    public void testProcessRequest_GetMultiple()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Set keys = Collections.EMPTY_SET;
        long requesterId = 2;
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                  requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong keys.", keys, remoteHttpCacheService.lastGetMultipleKeys );

    }

    /** Verify that the service is called. */
    public void testProcessRequest_Update()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        CacheElement element = new CacheElement( cacheName, key, null );
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createUpdateRequest( element, requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong object.", element, remoteHttpCacheService.lastUpdate );
    }
    
    /** Verify that the service is called. */
    public void testProcessRequest_Remove()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createRemoveRequest( cacheName, key, requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong key.", key, remoteHttpCacheService.lastRemoveKey );
    }
    
    /** Verify that the service is called. */
    public void testProcessRequest_RemoveAll()
    {
        // SETUP
        RemoteCacheServiceAdaptor adaptor = new RemoteCacheServiceAdaptor();

        MockRemoteCacheService remoteHttpCacheService = new MockRemoteCacheService();
        adaptor.setRemoteCacheService( remoteHttpCacheService );

        String cacheName = "testRemoveALl";
        long requesterId = 2;
        RemoteHttpCacheRequest request = RemoteHttpClientRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // DO WORK
        RemoteHttpCacheResponse result = adaptor.processRequest( request );

        // VERIFY
        assertNotNull( "Should have a result.", result );
        assertEquals( "Wrong cacheName.", cacheName, remoteHttpCacheService.lastRemoveAllCacheName );
    }
}
