package org.apache.commons.jcs.auxiliary.remote.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.remote.util.RemoteCacheRequestFactory;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteRequestType;
import org.apache.commons.jcs.engine.CacheElement;

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
        RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateGetMatchingRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        String pattern = "pattern";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetMatchingRequest( cacheName, pattern, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET_MATCHING, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateGetMultipleRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Set<Serializable> keys = Collections.emptySet();
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createGetMultipleRequest( cacheName, keys, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.GET_MULTIPLE, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateRemoveRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest<Serializable, Serializable> result = RemoteCacheRequestFactory
            .createRemoveRequest( cacheName, key, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.REMOVE, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateRemoveAllRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        long requesterId = 2;

        // DO WORK
        RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createRemoveAllRequest( cacheName, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.REMOVE_ALL, result.getRequestType() );
    }

    /** Simple test */
    public void testCreateUpdateRequest_Normal()
    {
        // SETUP
        String cacheName = "test";
        Serializable key = "key";
        long requesterId = 2;

        CacheElement<Serializable, Serializable> element =
            new CacheElement<Serializable, Serializable>( cacheName, key, null );

        // DO WORK
        RemoteCacheRequest<Serializable, Serializable> result =
            RemoteCacheRequestFactory.createUpdateRequest( element, requesterId );

        // VERIFY
        assertNotNull( "Should have a result", result );
        assertEquals( "Wrong cacheName", cacheName, result.getCacheName() );
        assertEquals( "Wrong type", RemoteRequestType.UPDATE, result.getRequestType() );
    }
}
