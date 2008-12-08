package org.apache.jcs.auxiliary.remote.http.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.apache.jcs.auxiliary.remote.http.behavior.IRemoteHttpCacheConstants;
import org.apache.jcs.auxiliary.remote.http.value.RemoteHttpCacheRequest;
import org.apache.jcs.engine.CacheElement;

import junit.framework.TestCase;

/** Unit tests for the request creator. */
public class RemoteHttpClientRequestFactoryUnitTest
    extends TestCase
{
    /** Simple test */
    public void testCreateGetRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createGetRequest( cacheName, key, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_GET, result.getRequestType() );
    }
    
    /** Simple test */
    public void testCreateGetMatchingRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createGetMatchingRequest( cacheName, pattern, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MATCHING, result.getRequestType() );
    }
    
    /** Simple test */
    public void testCreateGetMultipleRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Set keys = Collections.EMPTY_SET;
        long requesterId = 2;
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createGetMultipleRequest( cacheName, keys, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_GET_MULTIPLE, result.getRequestType() );
    }
    
    
    /** Simple test */
    public void testCreateRemoveRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createRemoveRequest( cacheName, key, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE, result.getRequestType() );
    }
    
    
    /** Simple test */
    public void testCreateRemoveAllRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        long requesterId = 2;
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createRemoveAllRequest( cacheName, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_REMOVE_ALL, result.getRequestType() );
    }
    
    /** Simple test */
    public void testCreateUpdateRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;
        
        CacheElement element = new CacheElement( cacheName, key, null );
        
        // DO WORK
        RemoteHttpCacheRequest result = RemoteHttpClientRequestFactory.createUpdateRequest( element, requesterId );
        
        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", IRemoteHttpCacheConstants.REQUEST_TYPE_UPDATE, result.getRequestType() );
    }
}
