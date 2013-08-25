package org.apache.commons.jcs.auxiliary.remote.http.client;

import junit.framework.TestCase;

import org.apache.commons.jcs.auxiliary.remote.http.client.RemoteHttpCacheAttributes;
import org.apache.commons.jcs.auxiliary.remote.http.client.RemoteHttpCacheDispatcher;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteCacheRequest;
import org.apache.commons.jcs.auxiliary.remote.value.RemoteRequestType;

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

        RemoteCacheRequest<String, String> remoteCacheRequest = new RemoteCacheRequest<String, String>();
        remoteCacheRequest.setRequestType( RemoteRequestType.REMOVE_ALL );
        String cacheName = "myCache";
        remoteCacheRequest.setCacheName( cacheName );

        String baseUrl = "http://localhost?thishasaquestionmark";

        // DO WORK
        String result = dispatcher.addParameters( remoteCacheRequest, baseUrl );

        // VERIFY
        assertEquals( "Wrong url", baseUrl + "&CacheName=" + cacheName + "&Key=&RequestType=REMOVE_ALL", result  );
    }
}
