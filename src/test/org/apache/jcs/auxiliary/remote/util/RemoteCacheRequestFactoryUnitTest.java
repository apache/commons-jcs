package org.apache.jcs.auxiliary.remote.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.jcs.engine.CacheElement;

/** Unit tests for the request creator. */
public class RemoteCacheRequestFactoryUnitTest
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
        RemoteCacheRequest result = RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_GET, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateGetMatchingRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest result = RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern,
                                                                                                 requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_GET_MATCHING, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateGetMultipleRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Set keys = Collections.EMPTY_SET;
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest result = RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys,
                                                                                                 requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_GET_MULTIPLE, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateRemoveRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest result = RemoteCacheRequestFactory
            .createRemoveRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_REMOVE, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateRemoveAllRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest result = RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_REMOVE_ALL, result.getRequestType() );
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
        RemoteCacheRequest result = RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteCacheRequest.REQUEST_TYPE_UPDATE, result.getRequestType() );
    }
}
