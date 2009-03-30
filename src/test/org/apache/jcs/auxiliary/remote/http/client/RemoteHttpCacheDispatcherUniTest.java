package org.apache.jcs.auxiliary.remote.http.client;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;

/** Unit tests for the dispatcher. */
public class RemoteHttpCacheDispatcherUniTest
    extends TestCase
{
    /**
     * Verify that we don't get two ?'s
     */
    public void testAddParameters_withQueryString()
    {
        // SETUP
        RemoteHttpCacheAttributes remoteHttpCacheAttributes = new RemoteHttpCacheAttributes();
        RemoteHttpCacheDispatcher dispatcher = new RemoteHttpCacheDispatcher( remoteHttpCacheAttributes );

        RemoteCacheRequest remoteCacheRequest = new RemoteCacheRequest();
        remoteCacheRequest.setRequestType( RemoteCacheRequest.REQUEST_TYPE_REMOVE_ALL );
        String cacheName = "myCache";
        remoteCacheRequest.setCacheName( cacheName );

        String baseUrl = "http://localhost?thishasaquestionmark";

        // DO WORK
        String result = dispatcher.addParameters( remoteCacheRequest, baseUrl );

        // VERIFY
        assertEquals( "Wrong url", baseUrl + "&CacheName=" + cacheName + "&Key=&RequestType=RemoveAll", result  );
    }
}
